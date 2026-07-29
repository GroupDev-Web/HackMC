const path = require("node:path");
const fs = require("node:fs/promises");
const {
  app,
  BrowserWindow,
  ipcMain,
  shell,
  session
} = require("electron");
const { JsonStore } = require("./services/store");
const { AccountService } = require("./services/auth");
const { MinecraftService } = require("./services/minecraft");
const { ModrinthService } = require("./services/modrinth");

let mainWindow;
let store;
let accountService;
let minecraftService;
let modrinthService;

const DEFAULTS = {
  account: null,
  refreshToken: null,
  settings: {
    gameVersion: "26.1.2",
    loaderVersion: "0.19.3",
    memoryMin: "2G",
    memoryMax: "4G",
    width: "1280",
    height: "720",
    javaPath: ""
  }
};

const PERFORMANCE_PACK = [
  "sodium",
  "lithium",
  "ferrite-core",
  "immediatelyfast",
  "entityculling",
  "moreculling",
  "dynamic-fps"
];

function validateSender(event) {
  const url = event.senderFrame?.url || "";
  if (!url.startsWith("file://")) {
    throw new Error("Rejected IPC from an untrusted frame.");
  }
}

function handle(channel, listener) {
  ipcMain.handle(channel, async (event, ...args) => {
    validateSender(event);
    return listener(...args);
  });
}

function sendStatus(message, progress = null, type = "status") {
  if (!mainWindow?.isDestroyed()) {
    mainWindow.webContents.send("launcher:status", { message, progress, type });
  }
}

async function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1180,
    height: 760,
    minWidth: 920,
    minHeight: 620,
    frame: false,
    show: false,
    backgroundColor: "#090d12",
    title: "Midnight Launcher",
    webPreferences: {
      preload: path.join(__dirname, "../preload.js"),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true
    }
  });

  mainWindow.webContents.setWindowOpenHandler(() => ({ action: "deny" }));
  mainWindow.webContents.on("will-navigate", event => event.preventDefault());
  await mainWindow.loadFile(path.join(__dirname, "../renderer/index.html"));
  mainWindow.once("ready-to-show", () => mainWindow.show());
}

function registerIpc() {
  handle("launcher:state", async () => {
    const state = await store.read();
    return {
      account: state.account,
      settings: state.settings,
      installed: await modrinthService.listInstalled(),
      gameDirectory: minecraftService.root
    };
  });
  handle("auth:login", () => accountService.login());
  handle("auth:logout", () => accountService.logout());
  handle("settings:save", async input => {
    const current = (await store.read()).settings;
    const clean = {
      gameVersion: String(input.gameVersion || current.gameVersion).slice(0, 32),
      loaderVersion: String(input.loaderVersion || current.loaderVersion).slice(0, 32),
      memoryMin: /^\d+[GM]$/.test(input.memoryMin) ? input.memoryMin : current.memoryMin,
      memoryMax: /^\d+[GM]$/.test(input.memoryMax) ? input.memoryMax : current.memoryMax,
      width: /^\d{3,5}$/.test(input.width) ? input.width : current.width,
      height: /^\d{3,5}$/.test(input.height) ? input.height : current.height,
      javaPath: String(input.javaPath || "").slice(0, 4096)
    };
    await store.update({ settings: clean });
    return clean;
  });
  handle("game:launch", async () => {
    const state = await store.read();
    sendStatus("Installing Fabric API…", 1);
    await modrinthService.install("fabric-api", state.settings.gameVersion);
    for (let index = 0; index < PERFORMANCE_PACK.length; index++) {
      const project = PERFORMANCE_PACK[index];
      sendStatus(`Optimizing: ${project}…`, 3 + Math.round(index / PERFORMANCE_PACK.length * 12));
      try {
        await modrinthService.install(project, state.settings.gameVersion);
      } catch (error) {
        sendStatus(`Skipped ${project}: ${error.message}`, null, "debug");
      }
    }
    await minecraftService.launch(state.settings);
    return true;
  });
  handle("game:open-folder", async () => {
    await fs.mkdir(minecraftService.root, { recursive: true });
    const error = await shell.openPath(minecraftService.root);
    if (error) throw new Error(error);
    return minecraftService.root;
  });
  handle("mods:search", (query, offset = 0) => {
    if (typeof query !== "string" || query.length > 120) throw new Error("Invalid search.");
    return store.read().then(state => modrinthService.search(query, state.settings.gameVersion, Number(offset) || 0));
  });
  handle("mods:install", async projectId => {
    if (!/^[\w-]{3,64}$/.test(projectId)) throw new Error("Invalid project ID.");
    const state = await store.read();
    const files = await modrinthService.install(projectId, state.settings.gameVersion);
    return { files, installed: await modrinthService.listInstalled() };
  });
  handle("mods:list", () => modrinthService.listInstalled());
  handle("mods:toggle", filename => modrinthService.toggle(filename));
  handle("mods:remove", filename => modrinthService.remove(filename, target => shell.trashItem(target)));
  handle("window:minimize", () => mainWindow.minimize());
  handle("window:maximize", () => mainWindow.isMaximized() ? mainWindow.unmaximize() : mainWindow.maximize());
  handle("window:close", () => mainWindow.close());
}

app.whenReady().then(async () => {
  const dataRoot = path.join(app.getPath("userData"), "minecraft");
  await fs.mkdir(dataRoot, { recursive: true });
  store = new JsonStore(path.join(app.getPath("userData"), "state.json"), DEFAULTS);
  accountService = new AccountService(store);
  minecraftService = new MinecraftService({
    root: dataRoot,
    accountService,
    resourcesPath: process.resourcesPath,
    sendStatus
  });
  modrinthService = new ModrinthService(minecraftService.modsDirectory());
  registerIpc();

  session.defaultSession.setPermissionRequestHandler((_webContents, _permission, callback) => callback(false));
  await createWindow();
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") app.quit();
});

app.on("activate", () => {
  if (BrowserWindow.getAllWindows().length === 0) createWindow();
});
