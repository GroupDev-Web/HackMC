package dev.hackmc.screenapi.component;

import dev.hackmc.screenapi.render.GlassRenderer;
import dev.hackmc.screenapi.theme.ScreenFonts;
import dev.hackmc.screenapi.theme.ScreenTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class GlassModuleCard extends GlossyWidget {
	private final String category;
	private final String description;
	private final BooleanSupplier enabled;
	private final Consumer<Boolean> changed;
	private float enabledProgress;

	public GlassModuleCard(int x, int y, int width, int height, String name, String category, String description,
			BooleanSupplier enabled, Consumer<Boolean> changed, ScreenTheme theme) {
		super(x, y, width, height, ScreenFonts.text(name), theme);
		this.category = category;
		this.description = description;
		this.enabled = enabled;
		this.changed = changed;
		this.enabledProgress = enabled.getAsBoolean() ? 1.0F : 0.0F;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		enabledProgress += ((enabled.getAsBoolean() ? 1.0F : 0.0F) - enabledProgress) * 0.3F;
		int surface = GlassRenderer.lerpColor(theme.control(), theme.controlHover(), hoverProgress);
		GlassRenderer.roundedRect(graphics, getX(), getY(), width, height, 7, theme.panelEdge());
		GlassRenderer.roundedRect(graphics, getX() + 1, getY() + 1, width - 2, height - 2, 6, surface);

		if (height >= 66) {
			String initial = getMessage().getString().substring(0, 1).toUpperCase();
			GlassRenderer.roundedRect(graphics, getX() + 8, getY() + 7, 22, 22, 6,
					GlassRenderer.lerpColor(0x553B4358, theme.accent(), enabledProgress));
			graphics.centeredText(Minecraft.getInstance().font, ScreenFonts.text(initial),
					getX() + 19, getY() + 14, theme.text());
			String displayName = Minecraft.getInstance().font.plainSubstrByWidth(
					getMessage().getString(), width - 44);
			graphics.text(Minecraft.getInstance().font, ScreenFonts.text(displayName),
					getX() + 36, getY() + 8, theme.text(), false);
			graphics.text(Minecraft.getInstance().font, ScreenFonts.text(category.toUpperCase()),
					getX() + 36, getY() + 19, theme.mutedText(), false);
			String summary = Minecraft.getInstance().font.plainSubstrByWidth(description, width - 16);
			if (summary.length() < description.length() && summary.length() > 1) {
				summary = summary.substring(0, summary.length() - 1) + "…";
			}
			graphics.text(Minecraft.getInstance().font, ScreenFonts.text(summary),
					getX() + 8, getY() + 35, theme.mutedText(), false);
		} else {
			graphics.centeredText(Minecraft.getInstance().font, getMessage(),
					getX() + width / 2, getY() + 7, theme.text());
			if (height >= 43) {
				graphics.centeredText(Minecraft.getInstance().font, ScreenFonts.text(category.toUpperCase()),
						getX() + width / 2, getY() + 18, theme.mutedText());
			}
		}

		int footer = GlassRenderer.lerpColor(0x70252C3D, 0xE329B66F, enabledProgress);
		GlassRenderer.roundedRect(graphics, getX() + 4, getBottom() - 13, width - 8, 9, 4, footer);
		graphics.centeredText(Minecraft.getInstance().font,
				ScreenFonts.text(enabled.getAsBoolean() ? "ENABLED" : "DISABLED"),
				getX() + width / 2, getBottom() - 13, theme.text());
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		changed.accept(!enabled.getAsBoolean());
	}
}
