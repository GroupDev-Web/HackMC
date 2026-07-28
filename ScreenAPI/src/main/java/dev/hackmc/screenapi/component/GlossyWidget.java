package dev.hackmc.screenapi.component;

import dev.hackmc.screenapi.theme.ScreenTheme;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class GlossyWidget extends AbstractWidget {
	protected final ScreenTheme theme;
	protected float hoverProgress;

	protected GlossyWidget(int x, int y, int width, int height, Component message, ScreenTheme theme) {
		super(x, y, width, height, message);
		this.theme = theme;
	}

	public void tick() {
		float target = isHoveredOrFocused() ? 1.0F : 0.0F;
		hoverProgress += (target - hoverProgress) * 0.28F;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
