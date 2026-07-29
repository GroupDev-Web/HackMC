const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("midnight", {
  state: () => ipcRenderer.invoke("launcher:state"),
  login: () => ipcRenderer.invoke("auth:login"),
  logout: () => ipcRenderer.invoke("auth:logout"),
  saveSettings: settings => ipcRenderer.invoke("settings:save", settings),
  launch: () => ipcRenderer.invoke("game:launch"),
  openGameFolder: () => ipcRenderer.invoke("game:open-folder"),
  searchMods: (query, offset) => ipcRenderer.invoke("mods:search", query, offset),
  installMod: projectId => ipcRenderer.invoke("mods:install", projectId),
  listMods: () => ipcRenderer.invoke("mods:list"),
  toggleMod: filename => ipcRenderer.invoke("mods:toggle", filename),
  removeMod: filename => ipcRenderer.invoke("mods:remove", filename),
  minimize: () => ipcRenderer.invoke("window:minimize"),
  maximize: () => ipcRenderer.invoke("window:maximize"),
  close: () => ipcRenderer.invoke("window:close"),
  onStatus: callback => {
    const listener = (_event, payload) => callback(payload);
    ipcRenderer.on("launcher:status", listener);
    return () => ipcRenderer.removeListener("launcher:status", listener);
  }
});
