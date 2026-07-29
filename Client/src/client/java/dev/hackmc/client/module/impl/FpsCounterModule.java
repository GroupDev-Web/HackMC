package dev.hackmc.client.module.impl;

import dev.hackmc.client.module.Module;
import dev.hackmc.client.module.ModuleCategory;
import dev.hackmc.client.module.HudModule;
import dev.hackmc.screenapi.render.GlassRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class FpsCounterModule extends Module implements HudModule {
	public FpsCounterModule() {
		super("fps_counter", "FPS Counter", "Shows the current frame rate.", ModuleCategory.RENDER, true);
	}

	@Override
	public void render(GuiGraphicsExtractor graphics) {
		GlassRenderer.roundedRect(graphics, 0, 0, hudWidth(), hudHeight(), 5, 0xA51A1B18);
		graphics.centeredText(MC.font, Integer.toString(MC.getFps()), hudWidth() / 2, 5, 0xFFF6F6F2);
	}

	@Override
	public int hudWidth() {
		return 64;
	}

	@Override
	public int hudHeight() {
		return 18;
	}
}
