package dev.hackmc.screenapi.component;

import dev.hackmc.screenapi.render.GlassRenderer;
import dev.hackmc.screenapi.theme.ScreenFonts;
import dev.hackmc.screenapi.theme.ScreenTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public final class GlassSlider extends GlossyWidget {
	private final double min;
	private final double max;
	private final DoubleSupplier value;
	private final DoubleConsumer changed;

	public GlassSlider(int x, int y, int width, int height, String label, double min, double max,
			DoubleSupplier value, DoubleConsumer changed, ScreenTheme theme) {
		super(x, y, width, height, ScreenFonts.text(label), theme);
		this.min = min;
		this.max = max;
		this.value = value;
		this.changed = changed;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		GlassRenderer.roundedRect(graphics, getX(), getY(), width, height, 6,
				GlassRenderer.lerpColor(theme.control(), theme.controlHover(), hoverProgress));
		graphics.text(Minecraft.getInstance().font, getMessage(), getX() + 10, getY() + 6, theme.text(), false);
		String display = String.format("%.2f", value.getAsDouble());
		graphics.text(Minecraft.getInstance().font, ScreenFonts.text(display),
				getRight() - Minecraft.getInstance().font.width(display) - 10, getY() + 6, theme.mutedText(), false);

		int start = getX() + 10;
		int end = getRight() - 10;
		int trackY = getBottom() - 9;
		double progress = (value.getAsDouble() - min) / (max - min);
		int fillEnd = start + (int) Math.round((end - start) * progress);
		GlassRenderer.roundedRect(graphics, start, trackY, end - start, 3, 2, 0x70353D52);
		GlassRenderer.roundedRect(graphics, start, trackY, Math.max(3, fillEnd - start), 3, 2, theme.accent());
		GlassRenderer.roundedRect(graphics, fillEnd - 3, trackY - 3, 7, 9, 4, 0xFFF9FAFF);
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		update(event.x());
	}

	@Override
	protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
		update(event.x());
	}

	private void update(double mouseX) {
		double progress = Math.max(0.0, Math.min(1.0, (mouseX - getX() - 10.0) / (width - 20.0)));
		changed.accept(min + (max - min) * progress);
	}
}
