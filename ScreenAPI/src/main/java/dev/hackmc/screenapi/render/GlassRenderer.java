package dev.hackmc.screenapi.render;

import dev.hackmc.screenapi.theme.ScreenTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class GlassRenderer {
	private GlassRenderer() {
	}

	public static void roundedRect(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
			int radius, int color) {
		if (width <= 0 || height <= 0 || (color >>> 24) == 0) {
			return;
		}
		int safeRadius = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
		for (int row = 0; row < height; row++) {
			int inset = cornerInset(row, height, safeRadius);
			graphics.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
		}
	}

	public static void roundedOutline(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
			int radius, int color) {
		if (width <= 0 || height <= 0 || (color >>> 24) == 0) {
			return;
		}
		int safeRadius = Math.max(1, Math.min(radius, Math.min(width, height) / 2));
		for (int row = 0; row < height; row++) {
			int inset = cornerInset(row, height, safeRadius);
			int left = x + inset;
			int right = x + width - inset;
			if (row == 0 || row == height - 1) {
				graphics.fill(left, y + row, right, y + row + 1, color);
			} else {
				graphics.fill(left, y + row, Math.min(right, left + 1), y + row + 1, color);
				if (right - 1 > left) {
					graphics.fill(right - 1, y + row, right, y + row + 1, color);
				}
			}
		}
	}

	public static void glassPanel(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
			ScreenTheme theme) {
		roundedRect(graphics, x + 2, y + 3, width, height, theme.radius(), theme.shadow());
		roundedRect(graphics, x, y, width, height, theme.radius(), theme.panelEdge());
		roundedRect(graphics, x + 1, y + 1, width - 2, height - 2, theme.radius() - 1, theme.panel());

		int glossHeight = Math.max(4, height / 3);
		for (int row = 0; row < glossHeight; row++) {
			float progress = row / (float) glossHeight;
			int color = lerpColor(theme.glossTop(), theme.glossBottom(), progress);
			int inset = cornerInset(row + 1, height - 2, Math.max(0, theme.radius() - 1));
			graphics.fill(x + 1 + inset, y + 1 + row, x + width - 1 - inset, y + 2 + row, color);
		}
		graphics.fill(x + theme.radius(), y + 1, x + width - theme.radius(), y + 2, 0x2FFFFFFF);
	}

	public static int withAlpha(int color, int alpha) {
		return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
	}

	public static int lerpColor(int from, int to, float amount) {
		float t = Math.max(0.0F, Math.min(1.0F, amount));
		int a = lerp(from >>> 24, to >>> 24, t);
		int r = lerp((from >>> 16) & 255, (to >>> 16) & 255, t);
		int g = lerp((from >>> 8) & 255, (to >>> 8) & 255, t);
		int b = lerp(from & 255, to & 255, t);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private static int lerp(int from, int to, float amount) {
		return Math.round(from + (to - from) * amount);
	}

	private static int cornerInset(int row, int height, int radius) {
		if (radius <= 0) {
			return 0;
		}
		int edgeDistance = Math.min(row, height - 1 - row);
		if (edgeDistance >= radius) {
			return 0;
		}
		double dy = radius - edgeDistance - 0.5;
		return Math.max(0, radius - (int) Math.sqrt(Math.max(0.0, radius * radius - dy * dy)));
	}
}
