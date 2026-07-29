# Midnight Client

Fabric client for Minecraft 26.1.2 using Mojang's official mappings and Java 25.

## Build

```bash
./gradlew build
```

The distributable JAR is written to `build/libs/MidnightClient-1.5.0.jar`.

The project pins Fabric Loom 1.16.3 so both the included wrapper and Gradle 9.4
can resolve the build plugin consistently.

## Screen API integration

Use `HackMcClient.modules().all()` to list modules. Each `Module` exposes its
stable `id`, display `name`, `description`, `category`, and enabled state.
Call `toggle()` or `setEnabled(boolean)` from the Screen API.

Midnight Client contains more than 30 client-side quality-of-life modules,
including a 30-second rolling performance monitor. Module states persist in
`config/midnight-modules.json`; HUD positions persist separately through the
Screen API.

The module browser supports live search across module names, descriptions, and
categories. Hold `C` for zoom when the Zoom module is enabled. Toggle Sprint is
available as an opt-in movement convenience.

Four original persistent title-screen atmospheres are included: Noir City,
Red Eclipse, Obsidian Cavern, and Winter. Use the top-right atmosphere button
to cycle them; rain, stars, embers, and snow change with the selected scene.

The companion launcher installs the compatible Sodium, Lithium, FerriteCore,
ImmediatelyFast, EntityCulling, MoreCulling, and Dynamic FPS releases. These
specialized upstream projects provide the renderer, simulation, allocation, and
culling optimizations instead of duplicating them with fragile mixins.

Movement cheats, world cheats, and No Fall are not included.
