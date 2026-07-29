const fs = require("node:fs/promises");
const path = require("node:path");
const { pipeline } = require("node:stream/promises");
const { Readable } = require("node:stream");
const crypto = require("node:crypto");

const API = "https://api.modrinth.com/v2";
const USER_AGENT = "MidnightLauncher/1.2.0 (midnight-client)";

class ModrinthService {
  constructor(modsDirectory) {
    this.modsDirectory = modsDirectory;
  }

  async search(query, gameVersion = "26.1.2", offset = 0) {
    const facets = JSON.stringify([
      ["project_type:mod"],
      ["categories:fabric"],
      [`versions:${gameVersion}`],
      ["client_side:required", "client_side:optional"]
    ]);
    const url = new URL(`${API}/search`);
    url.searchParams.set("query", query || "");
    url.searchParams.set("facets", facets);
    url.searchParams.set("index", query ? "relevance" : "downloads");
    url.searchParams.set("limit", "24");
    url.searchParams.set("offset", String(offset));
    return this.requestJson(url);
  }

  async install(projectId, gameVersion = "26.1.2", visited = new Set()) {
    if (visited.has(projectId)) return [];
    visited.add(projectId);
    const versionsUrl = new URL(`${API}/project/${encodeURIComponent(projectId)}/version`);
    versionsUrl.searchParams.set("loaders", JSON.stringify(["fabric"]));
    versionsUrl.searchParams.set("game_versions", JSON.stringify([gameVersion]));
    const versions = await this.requestJson(versionsUrl);
    if (!versions.length) throw new Error(`No Fabric ${gameVersion} version is available.`);
    const version = versions.find(item => item.version_type === "release") || versions[0];
    return this.installResolvedVersion(version, gameVersion, visited);
  }

  async installVersion(versionId, gameVersion, visited) {
    const version = await this.requestJson(`${API}/version/${encodeURIComponent(versionId)}`);
    if (visited.has(version.project_id)) return [];
    visited.add(version.project_id);
    return this.installResolvedVersion(version, gameVersion, visited);
  }

  async installResolvedVersion(version, gameVersion, visited) {
    const installed = [];
    for (const dependency of version.dependencies || []) {
      if (dependency.dependency_type !== "required") continue;
      if (dependency.project_id) {
        installed.push(...await this.install(dependency.project_id, gameVersion, visited));
      } else if (dependency.version_id) {
        installed.push(...await this.installVersion(dependency.version_id, gameVersion, visited));
      }
    }
    installed.push(await this.downloadVersionFile(version));
    return installed;
  }

  async listInstalled() {
    await fs.mkdir(this.modsDirectory, { recursive: true });
    return (await fs.readdir(this.modsDirectory))
      .filter(name => name.toLowerCase().endsWith(".jar") || name.toLowerCase().endsWith(".jar.disabled"))
      .sort((a, b) => a.localeCompare(b));
  }

  async remove(filename, trashItem) {
    const safeName = path.basename(filename);
    if (
      safeName !== filename ||
      (!safeName.toLowerCase().endsWith(".jar") && !safeName.toLowerCase().endsWith(".jar.disabled"))
    ) {
      throw new Error("Invalid mod filename.");
    }
    await trashItem(path.join(this.modsDirectory, safeName));
    return this.listInstalled();
  }

  async toggle(filename) {
    const safeName = path.basename(filename);
    const lower = safeName.toLowerCase();
    if (safeName !== filename || (!lower.endsWith(".jar") && !lower.endsWith(".jar.disabled"))) {
      throw new Error("Invalid mod filename.");
    }
    const disabled = lower.endsWith(".jar.disabled");
    const targetName = disabled ? safeName.slice(0, -".disabled".length) : `${safeName}.disabled`;
    await fs.rename(path.join(this.modsDirectory, safeName), path.join(this.modsDirectory, targetName));
    return this.listInstalled();
  }

  async downloadVersionFile(version) {
    const file = version.files.find(item => item.primary) || version.files[0];
    if (!file) throw new Error(`Version ${version.name} has no downloadable file.`);
    const source = new URL(file.url);
    if (
      source.protocol !== "https:" ||
      (source.hostname !== "modrinth.com" && !source.hostname.endsWith(".modrinth.com"))
    ) {
      throw new Error("Rejected an unexpected Modrinth download host.");
    }
    await fs.mkdir(this.modsDirectory, { recursive: true });
    const filename = path.basename(file.filename).replace(/[^a-zA-Z0-9._+() -]/g, "_");
    const destination = path.join(this.modsDirectory, filename);
    const temporary = `${destination}.download`;
    try {
      await this.verifyHash(destination, file.hashes);
      await this.recordManagedFile(version.project_id, filename);
      return filename;
    } catch {
      await fs.rm(destination, { force: true });
    }
    const disabledDestination = `${destination}.disabled`;
    try {
      await this.verifyHash(disabledDestination, file.hashes);
      await this.recordManagedFile(version.project_id, `${filename}.disabled`);
      return `${filename}.disabled`;
    } catch {
      await fs.rm(disabledDestination, { force: true });
    }
    const response = await fetch(source, { headers: { "User-Agent": USER_AGENT } });
    if (!response.ok || !response.body) throw new Error(`Download failed (${response.status}).`);
    try {
      await pipeline(Readable.fromWeb(response.body), require("node:fs").createWriteStream(temporary));
      await this.verifyHash(temporary, file.hashes);
    } catch (error) {
      await fs.rm(temporary, { force: true });
      throw error;
    }
    await fs.rename(temporary, destination);
    await this.recordManagedFile(version.project_id, filename);
    return filename;
  }

  async verifyHash(filename, hashes = {}) {
    const algorithm = hashes.sha512 ? "sha512" : hashes.sha1 ? "sha1" : null;
    if (!algorithm) return;
    const data = await fs.readFile(filename);
    const actual = crypto.createHash(algorithm).update(data).digest("hex");
    if (actual !== hashes[algorithm]) {
      throw new Error("The downloaded mod failed its Modrinth checksum.");
    }
  }

  async recordManagedFile(projectId, filename) {
    if (!projectId) return;
    const manifestPath = path.join(this.modsDirectory, ".midnight-managed.json");
    let manifest = {};
    try {
      manifest = JSON.parse(await fs.readFile(manifestPath, "utf8"));
    } catch {}
    const previous = manifest[projectId];
    if (previous && previous !== filename && path.basename(previous) === previous) {
      await fs.rm(path.join(this.modsDirectory, previous), { force: true });
    }
    manifest[projectId] = filename;
    const temporary = `${manifestPath}.tmp`;
    await fs.writeFile(temporary, JSON.stringify(manifest, null, 2));
    await fs.rename(temporary, manifestPath);
  }

  async requestJson(url) {
    const response = await fetch(url, { headers: { "User-Agent": USER_AGENT } });
    if (!response.ok) throw new Error(`Modrinth request failed (${response.status}).`);
    return response.json();
  }
}

module.exports = { ModrinthService };
