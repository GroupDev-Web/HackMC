package dev.hackmc.screenapi.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HudManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type POSITION_MAP = new TypeToken<Map<String, Position>>() { }.getType();
	private static final Map<String, HudWidget> WIDGETS = new LinkedHashMap<>();

	private HudManager() {
	}

	public static HudWidget register(HudWidget widget) {
		WIDGETS.put(widget.id(), widget);
		loadWidget(widget);
		return widget;
	}

	public static void unregister(String id) {
		WIDGETS.remove(id);
	}

	public static List<HudWidget> widgets() {
		return List.copyOf(WIDGETS.values());
	}

	public static List<HudWidget> visibleWidgets() {
		return WIDGETS.values().stream().filter(HudWidget::visible).toList();
	}

	public static void render(GuiGraphicsExtractor graphics) {
		WIDGETS.values().forEach(widget -> widget.render(graphics, false));
	}

	public static void renderEditor(GuiGraphicsExtractor graphics) {
		WIDGETS.values().stream().filter(HudWidget::visible)
				.forEach(widget -> widget.render(graphics, true));
	}

	public static void save() {
		try {
			Path file = configFile();
			Files.createDirectories(file.getParent());
			Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
			Map<String, Position> positions = new LinkedHashMap<>();
			WIDGETS.values().forEach(widget -> positions.put(widget.id(), new Position(widget.x(), widget.y())));
			try (Writer writer = Files.newBufferedWriter(temporary)) {
				GSON.toJson(positions, POSITION_MAP, writer);
			}
			try {
				Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.ATOMIC_MOVE);
			} catch (Exception unsupportedAtomicMove) {
				Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception ignored) {
			// A layout failure must never crash the game or block the HUD.
		}
	}

	public static void resetAll() {
		WIDGETS.values().forEach(HudWidget::reset);
		save();
	}

	private static void loadWidget(HudWidget widget) {
		Path file = configFile();
		if (!Files.isRegularFile(file)) {
			return;
		}
		try (Reader reader = Files.newBufferedReader(file)) {
			Map<String, Position> positions = GSON.fromJson(reader, POSITION_MAP);
			Position position = positions == null ? null : positions.get(widget.id());
			if (position != null) {
				widget.moveTo(position.x, position.y);
			}
		} catch (Exception ignored) {
			// Invalid user config falls back to the registered default.
		}
	}

	private static Path configFile() {
		return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("screenapi-hud.json");
	}

	private record Position(int x, int y) {
	}
}
