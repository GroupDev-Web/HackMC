package dev.hackmc.screenapi.hud;

import dev.hackmc.screenapi.render.GlassRenderer;
import dev.hackmc.screenapi.screen.GlossyScreen;
import dev.hackmc.screenapi.theme.ScreenFonts;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

public final class HudEditorScreen extends GlossyScreen {
	private static final int SNAP = 4;
	private static final Identifier CLIENT_LOGO =
			Identifier.fromNamespaceAndPath("screenapi", "textures/gui/midnight_logo.png");
	private final Screen parent;
	private final String brand;
	private final Function<Screen, Screen> modsScreenFactory;
	private HudWidget dragging;
	private double dragOffsetX;
	private double dragOffsetY;

	public HudEditorScreen(Screen parent) {
		this(parent, "HUD Editor", null);
	}

	public HudEditorScreen(Screen parent, String brand, Function<Screen, Screen> modsScreenFactory) {
		super("HUD Editor");
		this.parent = parent;
		this.brand = brand;
		this.modsScreenFactory = modsScreenFactory;
	}

	@Override
	protected void buildScreen(int x, int y, int width, int height) {
		boolean clamped = false;
		for (HudWidget widget : HudManager.widgets()) {
			int safeX = Math.max(4, Math.min(this.width - widget.width() - 4, widget.x()));
			int safeY = Math.max(16, Math.min(this.height - widget.height() - 4, widget.y()));
			if (safeX != widget.x() || safeY != widget.y()) {
				widget.moveTo(safeX, safeY);
				clamped = true;
			}
		}
		if (clamped) {
			HudManager.save();
		}
		addControl(new dev.hackmc.screenapi.component.GlassButton(this.width - 100, 10, 88, 20,
				"Reset layout", HudManager::resetAll, theme));
		if (modsScreenFactory != null) {
			addControl(new dev.hackmc.screenapi.component.GlassButton(
					(this.width - 176) / 2, this.height / 2 + 32, 176, 34,
					"Mods", () -> minecraft.setScreen(modsScreenFactory.apply(this)), theme));
		}
	}

	@Override
	protected void extractGlassContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int brandWidth = font.width(brand);
		int logoSize = Math.min(90, Math.max(58, height / 5));
		int logoX = (width - logoSize) / 2;
		int logoY = Math.max(28, height / 2 - logoSize - 30);
		graphics.blit(CLIENT_LOGO, logoX, logoY, logoX + logoSize, logoY + logoSize, 0, 1, 0, 1);
		graphics.text(font, ScreenFonts.text(brand), (width - brandWidth) / 2, logoY + logoSize + 3, theme.text(), false);
		for (HudWidget widget : HudManager.visibleWidgets()) {
			boolean selected = widget == dragging || contains(widget, mouseX, mouseY);
			if (selected) {
				GlassRenderer.roundedOutline(graphics, widget.x() - 3, widget.y() - 3,
						widget.width() + 6, widget.height() + 6, 5,
						GlassRenderer.withAlpha(theme.accentBright(), 155));
			}
		}
		HudManager.renderEditor(graphics);
		graphics.fill(width / 2, 38, width / 2 + 1, height - 12, 0x20FFFFFF);
		graphics.fill(12, height / 2, width - 12, height / 2 + 1, 0x20FFFFFF);
	}

	@Override
	protected boolean showPanel() {
		return false;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		var visibleWidgets = HudManager.visibleWidgets();
		for (int i = visibleWidgets.size() - 1; i >= 0; i--) {
			HudWidget widget = visibleWidgets.get(i);
			if (contains(widget, event.x(), event.y())) {
				dragging = widget;
				dragOffsetX = event.x() - widget.x();
				dragOffsetY = event.y() - widget.y();
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (dragging == null) {
			return super.mouseDragged(event, dragX, dragY);
		}
		int targetX = snap((int) Math.round(event.x() - dragOffsetX));
		int targetY = snap((int) Math.round(event.y() - dragOffsetY));
		targetX = Math.max(4, Math.min(width - dragging.width() - 4, targetX));
		targetY = Math.max(16, Math.min(height - dragging.height() - 4, targetY));
		dragging.moveTo(targetX, targetY);
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (dragging != null) {
			dragging = null;
			HudManager.save();
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public void onClose() {
		HudManager.save();
		minecraft.setScreen(parent);
	}

	private static boolean contains(HudWidget widget, double x, double y) {
		return x >= widget.x() && x <= widget.x() + widget.width()
				&& y >= widget.y() && y <= widget.y() + widget.height();
	}

	private static int snap(int value) {
		return Math.round(value / (float) SNAP) * SNAP;
	}
}
