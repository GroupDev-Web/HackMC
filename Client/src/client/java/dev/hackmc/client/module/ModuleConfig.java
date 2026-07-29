package dev.hackmc.client.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ModuleConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type BOOLEAN_MAP = new TypeToken<Map<String, Boolean>>() { }.getType();

	private ModuleConfig() {
	}

	static void load(List<Module> modules) {
		Path file = file();
		if (!Files.isRegularFile(file)) {
			return;
		}
		try (Reader reader = Files.newBufferedReader(file)) {
			Map<String, Boolean> values = GSON.fromJson(reader, BOOLEAN_MAP);
			if (values == null) {
				return;
			}
			for (Module module : modules) {
				Boolean enabled = values.get(module.id());
				if (enabled != null) {
					module.setEnabled(enabled);
				}
			}
		} catch (Exception ignored) {
			// A malformed user config falls back to safe module defaults.
		}
	}

	static void save(List<Module> modules) {
		Path file = file();
		Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
		try {
			Files.createDirectories(file.getParent());
			Map<String, Boolean> values = new LinkedHashMap<>();
			modules.forEach(module -> values.put(module.id(), module.isEnabled()));
			try (Writer writer = Files.newBufferedWriter(temporary)) {
				GSON.toJson(values, BOOLEAN_MAP, writer);
			}
			try {
				Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.ATOMIC_MOVE);
			} catch (Exception unsupportedAtomicMove) {
				Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception ignored) {
			// Configuration persistence must never break the render or tick loop.
		}
	}

	private static Path file() {
		return Minecraft.getInstance().gameDirectory.toPath()
				.resolve("config")
				.resolve("midnight-modules.json");
	}
}
