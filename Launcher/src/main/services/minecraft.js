const fs = require("node:fs/promises");
const path = require("node:path");
const { Client } = require("minecraft-launcher-core");

class MinecraftService {
  constructor({ root, accountService, resourcesPath, sendStatus }) {
    this.root = root;
    this.accountService = accountService;
    this.resourcesPath = resourcesPath;
    this.sendStatus = sendStatus;
    this.launcher = new Client();
    this.bindEvents();
  }

  modsDirectory() {
    return path.join(this.root, "mods");
  }

  async launch(settings) {
    const authorization = await this.accountService.authorization();
    const gameVersion = settings.gameVersion || "26.1.2";
    const loaderVersion = settings.loaderVersion || "0.19.3";
    const profile = await this.ensureFabricProfile(gameVersion, loaderVersion);
    await this.installBundledClient();
    this.sendStatus("Preparing Minecraft assets and libraries…", 5);
    const process = await this.launcher.launch({
      authorization,
      root: this.root,
      version: {
        number: gameVersion,
        type: "release",
        custom: profile
      },
      memory: {
        min: settings.memoryMin || "2G",
        max: settings.memoryMax || "4G"
      },
      javaPath: settings.javaPath || undefined,
      window: {
        width: settings.width || "1280",
        height: settings.height || "720"
      },
      customArgs: [
        "-XX:+UseG1GC",
        "-XX:+ParallelRefProcEnabled",
        "-XX:MaxGCPauseMillis=50",
        "-XX:+DisableExplicitGC",
        "-XX:G1ReservePercent=20",
        "-XX:InitiatingHeapOccupancyPercent=15"
      ]
    });
    if (!process) {
      throw new Error("Minecraft Launcher Core could not start the game. Check the Java path and launcher log.");
    }
    return process;
  }

  async ensureFabricProfile(gameVersion, loaderVersion) {
    const id = `fabric-loader-${loaderVersion}-${gameVersion}`;
    const directory = path.join(this.root, "versions", id);
    const jsonPath = path.join(directory, `${id}.json`);
    try {
      await fs.access(jsonPath);
      return id;
    } catch {
      this.sendStatus("Installing Fabric Loader…", 2);
    }
    const endpoint = `https://meta.fabricmc.net/v2/versions/loader/${encodeURIComponent(gameVersion)}/${encodeURIComponent(loaderVersion)}/profile/json`;
    const response = await fetch(endpoint);
    if (!response.ok) throw new Error(`Fabric profile download failed (${response.status}).`);
    const profile = await response.json();
    await fs.mkdir(directory, { recursive: true });
    await fs.writeFile(jsonPath, JSON.stringify(profile, null, 2));
    return id;
  }

  async installBundledClient() {
    const candidates = [
      path.join(this.resourcesPath, "client", "MidnightClient-1.5.0.jar"),
      path.resolve(__dirname, "../../../../Client/build/libs/MidnightClient-1.5.0.jar")
    ];
    let source;
    for (const candidate of candidates) {
      try {
        await fs.access(candidate);
        source = candidate;
        break;
      } catch {}
    }
    if (!source) return;
    await fs.mkdir(this.modsDirectory(), { recursive: true });
    await fs.rm(path.join(this.modsDirectory(), "MidnightClient-1.3.0.jar"), { force: true });
    await fs.rm(path.join(this.modsDirectory(), "MidnightClient-1.4.0.jar"), { force: true });
    await fs.copyFile(source, path.join(this.modsDirectory(), "MidnightClient-1.5.0.jar"));
  }

  bindEvents() {
    this.launcher.on("progress", event => {
      const total = Number(event.total || 0);
      const task = Number(event.task || 0);
      this.sendStatus(event.type || "Downloading Minecraft…", total ? Math.round(task / total * 100) : null);
    });
    this.launcher.on("download-status", event => {
      const current = Number(event.current || 0);
      const total = Number(event.total || 0);
      const percent = total > 0 ? Math.round(current / total * 100) : null;
      this.sendStatus(`Downloading ${event.name || "file"}…`, percent);
    });
    this.launcher.on("data", line => this.sendStatus(String(line).trim(), null, "log"));
    this.launcher.on("debug", line => this.sendStatus(String(line).trim(), null, "debug"));
    this.launcher.on("close", code => this.sendStatus(`Minecraft exited with code ${code}.`, 0, "close"));
  }
}

module.exports = { MinecraftService };
