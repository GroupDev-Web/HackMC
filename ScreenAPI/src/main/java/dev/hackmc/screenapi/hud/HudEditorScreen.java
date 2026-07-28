package dev.hackmc.screenapi.hud;

import dev.hackmc.screenapi.render.GlassRenderer;
import dev.hackmc.screenapi.screen.GlossyScreen;
import dev.hackmc.screenapi.theme.ScreenFonts;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.function.Function;

public final class HudEditorScreen extends GlossyScreen {
	private static final int SNAP = 4;
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
		addControl(new dev.hackmc.screenapi.component.GlassButton(x + width - 104, y + 10, 86, 20,
				"Reset layout", HudManager::resetAll, theme));
		if (modsScreenFactory != null) {
			addControl(new dev.hackmc.screenapi.component.GlassButton(
					x + (width - 92) / 2, y + (height - 26) / 2, 92, 26,
					"Mods", () -> minecraft.setScreen(modsScreenFactory.apply(this)), theme));
		}
	}

	@Override
	protected void extractGlassContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int brandWidth = font.width(brand);
		graphics.text(font, ScreenFonts.text(brand), (width - brandWidth) / 2, 19, theme.text(), false);
		graphics.fill((width - brandWidth) / 2, 30, (width + brandWidth) / 2, 31, theme.accent());
		graphics.text(font, ScreenFonts.text("Drag HUD cards anywhere • positions snap to a 4px grid"),
				(width - font.width("Drag HUD cards anywhere • positions snap to a 4px grid")) / 2,
				48, theme.mutedText(), false);
		for (HudWidget widget : HudManager.widgets()) {
			boolean selected = widget == dragging || contains(widget, mouseX, mouseY);
			int edge = selected ? theme.accentBright() : 0x78FFFFFF;
			GlassRenderer.roundedOutline(graphics, widget.x() - 3, widget.y() - 3,
					widget.width() + 6, widget.height() + 6, 5, GlassRenderer.withAlpha(edge, selected ? 155 : 70));
			graphics.text(font, ScreenFonts.text(widget.title()), widget.x(), widget.y() - 13,
					selected ? theme.text() : theme.mutedText(), false);
		}
		HudManager.renderEditor(graphics);
		graphics.fill(width / 2, 38, width / 2 + 1, height - 12, 0x20FFFFFF);
		graphics.fill(12, height / 2, width - 12, height / 2 + 1, 0x20FFFFFF);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		for (int i = HudManager.widgets().size() - 1; i >= 0; i--) {
			HudWidget widget = HudManager.widgets().get(i);
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
