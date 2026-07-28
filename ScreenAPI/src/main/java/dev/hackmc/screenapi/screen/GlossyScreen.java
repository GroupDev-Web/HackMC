package dev.hackmc.screenapi.screen;

import dev.hackmc.screenapi.component.GlossyWidget;
import dev.hackmc.screenapi.render.GlassRenderer;
import dev.hackmc.screenapi.theme.ScreenFonts;
import dev.hackmc.screenapi.theme.ScreenTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class GlossyScreen extends Screen {
	protected final ScreenTheme theme;
	private final List<GlossyWidget> glossyWidgets = new ArrayList<>();
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;

	protected GlossyScreen(String title) {
		this(title, ScreenTheme.OBSIDIAN_GLASS);
	}

	protected GlossyScreen(String title, ScreenTheme theme) {
		super(ScreenFonts.text(title));
		this.theme = theme;
	}

	@Override
	protected final void init() {
		glossyWidgets.clear();
		panelWidth = Math.min(500, Math.max(280, width - 48));
		panelHeight = Math.min(360, Math.max(210, height - 48));
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;
		buildScreen(panelX, panelY, panelWidth, panelHeight);
	}

	protected abstract void buildScreen(int x, int y, int width, int height);

	protected final <T extends GlossyWidget> T addControl(T widget) {
		glossyWidgets.add(widget);
		return addRenderableWidget(widget);
	}

	public final ScreenTheme theme() {
		return theme;
	}

	@Override
	public void tick() {
		glossyWidgets.forEach(GlossyWidget::tick);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(0, 0, width, height, theme.backdrop());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		extractBackground(graphics, mouseX, mouseY, delta);
		GlassRenderer.glassPanel(graphics, panelX, panelY, panelWidth, panelHeight, theme);
		graphics.text(font, title, panelX + 18, panelY + 15, theme.text(), false);
		graphics.fill(panelX + 18, panelY + 32, panelX + panelWidth - 18, panelY + 33, 0x24FFFFFF);
		extractGlassContents(graphics, mouseX, mouseY, delta);
		super.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	protected void extractGlassContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	protected final Component noto(String text) {
		return ScreenFonts.text(text);
	}
}
