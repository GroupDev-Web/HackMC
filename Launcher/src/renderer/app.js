const $ = selector => document.querySelector(selector);
const $$ = selector => [...document.querySelectorAll(selector)];

const state = {
  account: null,
  settings: null,
  searchTimer: null,
  launching: false
};

const themes = ["noir", "eclipse", "cavern", "winter"];

function setTheme(name) {
  const theme = themes.includes(name) ? name : themes[0];
  $(".app-shell").dataset.theme = theme;
  localStorage.setItem("midnight-theme", theme);
  $("#themeButton").title = `Theme: ${theme}. Click to change.`;
  $("#themeLabel").textContent = theme.replace("-", " ").toUpperCase();
  $$("[data-theme-choice]").forEach(button => {
    button.classList.toggle("active", button.dataset.themeChoice === theme);
  });
}

function cycleTheme() {
  const current = $(".app-shell").dataset.theme;
  setTheme(themes[(themes.indexOf(current) + 1) % themes.length]);
  toast(`Background: ${$(".app-shell").dataset.theme}`);
}

function createAmbientField() {
  const field = $("#ambientField");
  const particles = [];
  for (let index = 0; index < 54; index++) {
    const particle = document.createElement("i");
    particle.style.setProperty("--x", `${(index * 37) % 101}%`);
    particle.style.setProperty("--y", `${(index * 53) % 83}%`);
    particle.style.setProperty("--delay", `${-((index * 0.43) % 8)}s`);
    particle.style.setProperty("--duration", `${4 + (index % 7) * 0.7}s`);
    particle.style.setProperty("--drift", `${(index % 2 ? 1 : -1) * (8 + index % 19)}px`);
    particles.push(particle);
  }
  field.replaceChildren(...particles);
}

function showPage(name) {
  $$(".page").forEach(page => page.classList.toggle("active", page.id === `${name}Page`));
  $$("[data-page]").forEach(button => button.classList.toggle("active", button.dataset.page === name));
  if (name === "mods") {
    loadMods($("#modSearch").value);
    loadInstalled();
  }
}

function toast(message) {
  const element = $("#toast");
  element.textContent = message;
  element.classList.add("show");
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => element.classList.remove("show"), 3200);
}

function setAccount(account) {
  state.account = account;
  $("#accountButton").textContent = account?.name || "Sign in";
  $("#launchText").textContent = account ? "LAUNCH MIDNIGHT" : "SIGN IN TO PLAY";
}

async function toggleAccount() {
  try {
    if (state.account) {
      await window.midnight.logout();
      setAccount(null);
      toast("Signed out.");
    } else {
      const result = await window.midnight.login();
      setAccount(result.account);
      toast(`Signed in as ${result.account.name}.`);
    }
  } catch (error) {
    toast(error.message || "Microsoft sign-in failed.");
  }
}

async function launch() {
  if (!state.account) return toggleAccount();
  if (state.launching) return;
  state.launching = true;
  $("#launchButton").disabled = true;
  $("#launchText").textContent = "PREPARING…";
  $("#launchProgress").classList.remove("hidden");
  try {
    await window.midnight.launch();
    $("#launchText").textContent = "GAME RUNNING";
  } catch (error) {
    toast(error.message || "Minecraft failed to launch.");
    $("#statusText").textContent = error.message;
    $("#launchText").textContent = "LAUNCH MIDNIGHT";
  } finally {
    state.launching = false;
    $("#launchButton").disabled = false;
  }
}

function createModCard(project) {
  const card = document.createElement("article");
  card.className = "mod-card";
  const icon = document.createElement("img");
  icon.src = project.icon_url || "../../assets/midnight-logo.png";
  icon.alt = "";
  icon.loading = "lazy";
  const copy = document.createElement("div");
  copy.className = "mod-copy";
  const title = document.createElement("strong");
  title.textContent = project.title;
  const description = document.createElement("p");
  description.textContent = project.description;
  const meta = document.createElement("span");
  meta.className = "mod-meta";
  meta.textContent = `by ${project.author}  •  ${Number(project.downloads).toLocaleString()} downloads`;
  copy.append(title, description, meta);
  const button = document.createElement("button");
  button.className = "install-button";
  button.textContent = "INSTALL";
  button.addEventListener("click", async () => {
    button.disabled = true;
    button.textContent = "…";
    try {
      const result = await window.midnight.installMod(project.project_id);
      button.textContent = "DONE";
      renderInstalled(result.installed);
      toast(`${project.title} installed with required dependencies.`);
    } catch (error) {
      button.disabled = false;
      button.textContent = "RETRY";
      toast(error.message || "Mod installation failed.");
    }
  });
  card.append(icon, copy, button);
  return card;
}

async function loadMods(query = "") {
  const grid = $("#modGrid");
  grid.replaceChildren(Object.assign(document.createElement("div"), { className: "empty", textContent: "Loading Modrinth…" }));
  try {
    const result = await window.midnight.searchMods(query, 0);
    grid.replaceChildren();
    if (!result.hits.length) {
      grid.append(Object.assign(document.createElement("div"), { className: "empty", textContent: "No compatible Fabric mods found." }));
      return;
    }
    result.hits.forEach(project => grid.append(createModCard(project)));
  } catch (error) {
    grid.replaceChildren(Object.assign(document.createElement("div"), { className: "empty", textContent: error.message || "Modrinth is unavailable." }));
  }
}

function renderInstalled(files) {
  const list = $("#installedList");
  list.replaceChildren();
  if (!files.length) {
    list.append(Object.assign(document.createElement("div"), { className: "empty", textContent: "No mods installed." }));
    return;
  }
  files.forEach(filename => {
    const row = document.createElement("div");
    row.className = "installed-item";
    const name = document.createElement("span");
    const disabled = filename.toLowerCase().endsWith(".disabled");
    name.textContent = disabled ? filename.slice(0, -9) : filename;
    name.classList.toggle("disabled-mod", disabled);
    const toggle = document.createElement("button");
    toggle.textContent = disabled ? "○" : "●";
    toggle.title = disabled ? "Enable mod" : "Disable mod";
    toggle.className = disabled ? "mod-state disabled" : "mod-state";
    toggle.addEventListener("click", async () => {
      try {
        renderInstalled(await window.midnight.toggleMod(filename));
      } catch (error) {
        toast(error.message || "Could not change mod state.");
      }
    });
    const remove = document.createElement("button");
    remove.textContent = "×";
    remove.title = "Move to Trash";
    remove.addEventListener("click", async () => {
      try {
        renderInstalled(await window.midnight.removeMod(filename));
        toast(`${filename} moved to Trash.`);
      } catch (error) {
        toast(error.message || "Could not remove mod.");
      }
    });
    const actions = document.createElement("div");
    actions.className = "installed-actions";
    actions.append(toggle, remove);
    row.append(name, actions);
    list.append(row);
  });
}

async function loadInstalled() {
  try {
    renderInstalled(await window.midnight.listMods());
  } catch (error) {
    toast(error.message);
  }
}

function fillSettings(settings) {
  const form = $("#settingsForm");
  Object.entries(settings).forEach(([key, value]) => {
    if (form.elements[key]) form.elements[key].value = value;
  });
  $("#versionLabel").textContent = settings.gameVersion;
  $("#modsVersion").textContent = settings.gameVersion;
}

async function initialize() {
  createAmbientField();
  setTheme(localStorage.getItem("midnight-theme") || "noir");
  const initial = await window.midnight.state();
  state.settings = initial.settings;
  setAccount(initial.account);
  fillSettings(initial.settings);
  renderInstalled(initial.installed);
}

$$("[data-page]").forEach(button => button.addEventListener("click", () => showPage(button.dataset.page)));
$("#accountButton").addEventListener("click", toggleAccount);
$("#themeButton").addEventListener("click", cycleTheme);
$$("[data-theme-choice]").forEach(button => {
  button.addEventListener("click", () => setTheme(button.dataset.themeChoice));
});
$("#launchButton").addEventListener("click", launch);
$("#folderButton").addEventListener("click", window.midnight.openGameFolder);
$("#minimizeButton").addEventListener("click", window.midnight.minimize);
$("#maximizeButton").addEventListener("click", window.midnight.maximize);
$("#closeButton").addEventListener("click", window.midnight.close);
$("#refreshInstalled").addEventListener("click", loadInstalled);

$("#modSearch").addEventListener("input", event => {
  clearTimeout(state.searchTimer);
  state.searchTimer = setTimeout(() => loadMods(event.target.value.trim()), 350);
});

$$(".filter").forEach(button => button.addEventListener("click", () => {
  $$(".filter").forEach(item => item.classList.remove("active"));
  button.classList.add("active");
  const query = button.dataset.query || "";
  $("#modSearch").value = query;
  loadMods(query);
}));

$("#settingsForm").addEventListener("submit", async event => {
  event.preventDefault();
  const values = Object.fromEntries(new FormData(event.currentTarget));
  try {
    state.settings = await window.midnight.saveSettings(values);
    fillSettings(state.settings);
    toast("Launcher settings saved.");
  } catch (error) {
    toast(error.message || "Could not save settings.");
  }
});

window.midnight.onStatus(payload => {
  $("#statusText").textContent = payload.message || "";
  if (payload.progress != null) {
    $("#launchProgress").classList.remove("hidden");
    $("#launchProgress div").style.width = `${Math.max(0, Math.min(100, payload.progress))}%`;
  }
  if (payload.type === "close") {
    $("#launchText").textContent = "LAUNCH MIDNIGHT";
    $("#launchProgress").classList.add("hidden");
  }
});

initialize().catch(error => toast(error.message || "Launcher initialization failed."));
