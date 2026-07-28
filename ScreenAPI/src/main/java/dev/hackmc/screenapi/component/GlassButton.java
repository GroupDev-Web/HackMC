package dev.hackmc.screenapi.component;

import dev.hackmc.screenapi.render.GlassRenderer;
import dev.hackmc.screenapi.theme.ScreenFonts;
import dev.hackmc.screenapi.theme.ScreenTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

public final class GlassButton extends GlossyWidget {
	private final Runnable action;

	public GlassButton(int x, int y, int width, int height, String label, Runnable action, ScreenTheme theme) {
		super(x, y, width, height, ScreenFonts.text(label), theme);
		this.action = action;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int surface = GlassRenderer.lerpColor(theme.control(), theme.controlHover(), hoverProgress);
		GlassRenderer.roundedRect(graphics, getX(), getY(), width, height, 6, theme.panelEdge());
		GlassRenderer.roundedRect(graphics, getX() + 1, getY() + 1, width - 2, height - 2, 5, surface);
		graphics.fill(getX() + 7, getY() + 1, getRight() - 7, getY() + 2, 0x35FFFFFF);
		graphics.centeredText(Minecraft.getInstance().font, getMessage(), getX() + width / 2,
				getY() + (height - 8) / 2, theme.text());
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		action.run();
	}
}
