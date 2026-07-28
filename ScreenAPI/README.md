# HackMC Screen API

A client-side UI library for Minecraft 26.1.2, Fabric Loader 0.19.3, Mojang
mappings, and Java 25.

## Features

- Glossy translucent screens with rounded panels, layered highlights, borders,
  and shadows
- Animated glass buttons, toggles, and sliders
- Bundled Noto Sans resource font with its SIL Open Font License
- Theme records for complete color replacement
- Draggable HUD widgets with a 4-pixel snapping grid
- Persistent HUD positions in `config/screenapi-hud.json`
- Automatic bounds clamping and layout reset
- `H` opens the HUD editor by default

## Creating a screen

```java
public final class SettingsScreen extends GlossyScreen {
    public SettingsScreen() {
        super("Settings");
    }

    @Override
    protected void buildScreen(int x, int y, int width, int height) {
        addControl(new GlassToggle(
                x + 18, y + 46, width - 36, 30, "Example",
                state::get, state::set, theme()
        ));

        addControl(new GlassSlider(
                x + 18, y + 84, width - 36, 38, "Opacity",
                0.0, 1.0, opacity::get, opacity::set, theme()
        ));
    }
}
```

## Registering a HUD widget

```java
HudManager.register(new HudWidget(
        "example:fps", "FPS", 8, 8, 64, 10,
        () -> enabled,
        (graphics, x, y, editing) ->
                graphics.text(Minecraft.getInstance().font, "120 FPS", x, y, 0xFFFFFFFF, true)
));
```

The `editing` flag is true inside the HUD editor, allowing a widget to render a
preview even when live data is unavailable.

## Using Noto Sans

Pass `ScreenFonts.text("Text")` to any Minecraft text method accepting a
`Component`. A custom typeface is selected through the component's font style,
so it remains compatible with Minecraft's normal text shaping and rendering.

## Build

```bash
./gradlew build
```

The standalone library JAR is generated at
`build/libs/screenapi-1.0.0.jar`. The HackMC client includes it as a nested
Fabric mod automatically.
