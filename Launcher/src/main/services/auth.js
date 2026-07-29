const { Auth } = require("msmc");
const { safeStorage } = require("electron");

class AccountService {
  constructor(store) {
    this.store = store;
    this.minecraftToken = null;
  }

  async summary() {
    const state = await this.store.read();
    return state.account || null;
  }

  async login() {
    const auth = new Auth("select_account");
    const xbox = await auth.launch("electron", {
      width: 520,
      height: 700,
      resizable: false
    });
    return this.persist(xbox);
  }

  async authorization() {
    if (this.minecraftToken?.validate()) {
      return this.minecraftToken.mclc();
    }
    const state = await this.store.read();
    if (!state.refreshToken) {
      throw new Error("Sign in with Microsoft before launching.");
    }
    const refreshToken = this.decrypt(state.refreshToken);
    const xbox = await new Auth("none").refresh(refreshToken);
    return (await this.persist(xbox)).authorization;
  }

  async logout() {
    this.minecraftToken = null;
    await this.store.update({ account: null, refreshToken: null });
  }

  async persist(xbox) {
    const minecraft = await xbox.getMinecraft();
    this.minecraftToken = minecraft;
    const profile = minecraft.profile;
    const account = {
      id: profile.id,
      name: profile.name,
      avatar: `https://mc-heads.net/avatar/${encodeURIComponent(profile.id)}/64`
    };
    await this.store.update({
      account,
      refreshToken: this.encrypt(xbox.save())
    });
    return { account, authorization: minecraft.mclc() };
  }

  encrypt(value) {
    if (!safeStorage.isEncryptionAvailable()) {
      throw new Error("Secure OS credential storage is unavailable.");
    }
    return safeStorage.encryptString(value).toString("base64");
  }

  decrypt(value) {
    return safeStorage.decryptString(Buffer.from(value, "base64"));
  }
}

module.exports = { AccountService };
