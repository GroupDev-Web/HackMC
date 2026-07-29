# Midnight Launcher

Electron launcher for Midnight Client with Microsoft authentication, Fabric
installation, Minecraft asset/library downloads, and an integrated Modrinth mod
browser.

## Run it

Requirements:

- Node.js 22 or newer
- Java 25 for Minecraft 26.1.2
- A Microsoft account that owns Minecraft: Java Edition

```bash
npm install
npm start
```

The first launch downloads Minecraft libraries and assets into Electron's app
data directory. The bundled Midnight Client jar is copied from
`../Client/build/libs/MidnightClient-1.5.0.jar`, and Fabric API is installed
automatically from Modrinth.

## Package it

```bash
npm run dist:linux
npm run dist:win
```

Packaged files are written to `dist/`. Run the Windows build on Windows (or in
an appropriately configured cross-build environment).

## Features

- Feather-inspired, frameless dark interface with Play, Mods, and Settings
- Microsoft/Xbox/Minecraft authentication through MSMC
- Minecraft launch and asset downloads through minecraft-launcher-core
- Automatic Fabric Loader profile installation
- Automatic performance pack: Sodium, Lithium, FerriteCore, ImmediatelyFast,
  EntityCulling, MoreCulling, and Dynamic FPS
- Search, install, dependency resolution, listing, and safe removal of Fabric
  mods from Modrinth
- Enable and disable installed mods without deleting them
- Four persistent original visual themes with rain, stars, embers, and snow
- Encrypted refresh-token storage through Electron's OS-backed `safeStorage`
- Configurable Java path, memory, resolution, game version, and loader version

The renderer is sandboxed and has no Node.js access. It can only invoke the
small set of operations exposed by the preload bridge.
