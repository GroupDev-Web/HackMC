package dev.hackmc.screenapi.hud;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public final class HudWidget {
	private final String id;
	private final String title;
	private final int defaultX;
	private final int defaultY;
	private final int width;
	private final int height;
	private final BooleanSupplier visible;
	private final HudRenderer renderer;
	private int x;
	private int y;

	public HudWidget(String id, String title, int x, int y, int width, int height,
			BooleanSupplier visible, HudRenderer renderer) {
		this.id = Objects.requireNonNull(id);
		this.title = Objects.requireNonNull(title);
		this.defaultX = x;
		this.defaultY = y;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.visible = Objects.requireNonNull(visible);
		this.renderer = Objects.requireNonNull(renderer);
	}

	public String id() {
		return id;
	}

	public String title() {
		return title;
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

	public boolean visible() {
		return visible.getAsBoolean();
	}

	public void moveTo(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void reset() {
		moveTo(defaultX, defaultY);
	}

	void render(net.minecraft.client.gui.GuiGraphicsExtractor graphics, boolean editing) {
		if (editing || visible()) {
			renderer.render(graphics, x, y, editing);
		}
	}
}
