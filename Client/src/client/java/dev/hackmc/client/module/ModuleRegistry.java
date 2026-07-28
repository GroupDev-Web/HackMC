package dev.hackmc.client.module;

import dev.hackmc.client.module.impl.CpsCounterModule;
import dev.hackmc.client.module.impl.FlyModule;
import dev.hackmc.client.module.impl.FpsBoostModule;
import dev.hackmc.client.module.impl.FpsCounterModule;
import dev.hackmc.client.module.impl.KeystrokesModule;
import dev.hackmc.client.module.impl.NoFallModule;
import dev.hackmc.client.module.impl.NukerModule;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;
import java.util.Optional;

public final class ModuleRegistry {
	private final List<Module> modules = List.of(
			new FlyModule(),
			new FpsCounterModule(),
			new CpsCounterModule(),
			new KeystrokesModule(),
			new NukerModule(),
			new NoFallModule(),
			new FpsBoostModule()
	);

	public List<Module> all() {
		return modules;
	}

	public Optional<Module> find(String id) {
		return modules.stream().filter(module -> module.id().equals(id)).findFirst();
	}

	public void tick() {
		modules.stream().filter(Module::isEnabled).forEach(Module::tick);
	}

	public void render(GuiGraphicsExtractor graphics) {
		modules.stream().filter(Module::isEnabled).forEach(module -> module.render(graphics));
	}
}
