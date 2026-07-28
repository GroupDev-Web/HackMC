# HackMC Client

Fabric client for Minecraft 26.1.2 using Mojang's official mappings and Java 25.

## Build

```bash
./gradlew build
```

The distributable JAR is written to `build/libs/HackMC-1.0.0.jar`.

The project pins Fabric Loom 1.16.3 so both the included wrapper and Gradle 9.4
can resolve the build plugin consistently.

## Screen API integration

Use `HackMcClient.modules().all()` to list modules. Each `Module` exposes its
stable `id`, display `name`, `description`, `category`, and enabled state.
Call `toggle()` or `setEnabled(boolean)` from the Screen API.

The stable module IDs are:

- `fly`
- `fps_counter`
- `cps_counter`
- `keystrokes`
- `nuker`
- `no_fall`
- `fps_boost`
