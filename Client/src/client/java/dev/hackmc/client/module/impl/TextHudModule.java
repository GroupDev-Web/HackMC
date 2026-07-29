package dev.hackmc.client.module.impl;

import dev.hackmc.client.module.HudModule;
import dev.hackmc.client.module.Module;
import dev.hackmc.client.module.ModuleCategory;
import dev.hackmc.screenapi.render.GlassRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.Function;

public final class TextHudModule extends Module implements HudModule {
	private final Function<Minecraft, String> text;
	private final int hudWidth;

	public TextHudModule(String id, String name, String description, boolean enabled, int hudWidth,
			Function<Minecraft, String> text) {
		super(id, name, description, ModuleCategory.RENDER, enabled);
		this.hudWidth = hudWidth;
		this.text = text;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics) {
		String value;
		try {
			value = text.apply(MC);
		} catch (Exception ignored) {
			value = "—";
		}
		GlassRenderer.roundedRect(graphics, 0, 0, hudWidth, hudHeight(), 5, 0xA51A1B18);
		graphics.centeredText(MC.font, value, hudWidth / 2, 5, 0xFFF6F6F2);
	}

	@Override
	public int hudWidth() {
		return hudWidth;
	}

	@Override
	public int hudHeight() {
		return 18;
	}
}
