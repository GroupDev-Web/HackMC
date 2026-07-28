package dev.hackmc.client.module.impl;

import dev.hackmc.client.module.Module;
import dev.hackmc.client.module.ModuleCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class FpsCounterModule extends Module {
	public FpsCounterModule() {
		super("fps_counter", "FPS Counter", "Shows the current frame rate.", ModuleCategory.RENDER, true);
	}

	@Override
	public void render(GuiGraphicsExtractor graphics) {
		graphics.text(MC.font, MC.getFps() + " FPS", 0, 0, 0xFFF5F7FF, true);
	}
}
