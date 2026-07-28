package dev.hackmc.screenapi.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;

@FunctionalInterface
public interface HudRenderer {
	void render(GuiGraphicsExtractor graphics, int x, int y, boolean editing);
}
