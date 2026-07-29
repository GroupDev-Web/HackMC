const fs = require("node:fs/promises");
const path = require("node:path");

class JsonStore {
  constructor(file, defaults = {}) {
    this.file = file;
    this.defaults = defaults;
  }

  async read() {
    try {
      return { ...this.defaults, ...JSON.parse(await fs.readFile(this.file, "utf8")) };
    } catch {
      return { ...this.defaults };
    }
  }

  async write(value) {
    await fs.mkdir(path.dirname(this.file), { recursive: true });
    const temporary = `${this.file}.tmp`;
    await fs.writeFile(temporary, JSON.stringify(value, null, 2), "utf8");
    await fs.rename(temporary, this.file);
    return value;
  }

  async update(patch) {
    return this.write({ ...(await this.read()), ...patch });
  }
}

module.exports = { JsonStore };
