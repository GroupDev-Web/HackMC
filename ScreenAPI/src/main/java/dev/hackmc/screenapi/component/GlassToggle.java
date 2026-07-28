package dev.hackmc.screenapi.component;

import dev.hackmc.screenapi.render.GlassRenderer;
import dev.hackmc.screenapi.theme.ScreenFonts;
import dev.hackmc.screenapi.theme.ScreenTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class GlassToggle extends GlossyWidget {
	private final BooleanSupplier value;
	private final Consumer<Boolean> changed;
	private float toggleProgress;

	public GlassToggle(int x, int y, int width, int height, String label, BooleanSupplier value,
			Consumer<Boolean> changed, ScreenTheme theme) {
		super(x, y, width, height, ScreenFonts.text(label), theme);
		this.value = value;
		this.changed = changed;
		this.toggleProgress = value.getAsBoolean() ? 1.0F : 0.0F;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		toggleProgress += ((value.getAsBoolean() ? 1.0F : 0.0F) - toggleProgress) * 0.32F;
		int surface = GlassRenderer.lerpColor(theme.control(), theme.controlHover(), hoverProgress);
		GlassRenderer.roundedRect(graphics, getX(), getY(), width, height, 6, surface);
		graphics.text(Minecraft.getInstance().font, getMessage(), getX() + 10, getY() + (height - 8) / 2,
				theme.text(), false);

		int trackWidth = 31;
		int trackHeight = 16;
		int trackX = getRight() - trackWidth - 8;
		int trackY = getY() + (height - trackHeight) / 2;
		int track = GlassRenderer.lerpColor(0x80383E50, theme.accent(), toggleProgress);
		GlassRenderer.roundedRect(graphics, trackX, trackY, trackWidth, trackHeight, 8, track);
		int knobX = trackX + 2 + Math.round(toggleProgress * (trackWidth - 15));
		GlassRenderer.roundedRect(graphics, knobX, trackY + 2, 12, 12, 6, 0xFFF9FAFF);
		graphics.fill(knobX + 3, trackY + 3, knobX + 9, trackY + 4, 0x55FFFFFF);
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		changed.accept(!value.getAsBoolean());
	}
}
