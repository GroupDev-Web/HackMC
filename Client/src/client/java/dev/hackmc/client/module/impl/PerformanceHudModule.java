package dev.hackmc.client.module.impl;

import dev.hackmc.client.module.HudModule;
import dev.hackmc.client.module.Module;
import dev.hackmc.client.module.ModuleCategory;
import dev.hackmc.screenapi.render.GlassRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Arrays;

public final class PerformanceHudModule extends Module implements HudModule {
	private static final int SAMPLE_COUNT = 120;
	private final int[] samples = new int[SAMPLE_COUNT];
	private int cursor;
	private int collected;
	private long lastSample;
	private long lastSummary;
	private int average;
	private int onePercentLow;

	public PerformanceHudModule() {
		super("performance_monitor", "Performance Monitor",
				"Shows current, average, and one-percent-low FPS.", ModuleCategory.PERFORMANCE, false);
	}

	@Override
	public void render(GuiGraphicsExtractor graphics) {
		long now = System.nanoTime();
		if (now - lastSample >= 250_000_000L) {
			samples[cursor] = Math.max(0, MC.getFps());
			cursor = (cursor + 1) % samples.length;
			collected = Math.min(collected + 1, samples.length);
			lastSample = now;
		}
		if (now - lastSummary >= 1_000_000_000L) {
			updateSummary();
			lastSummary = now;
		}

		GlassRenderer.roundedRect(graphics, 0, 0, hudWidth(), hudHeight(), 5, 0xA51A1B18);
		String text = MC.getFps() + " FPS  AVG " + average + "  1% " + onePercentLow;
		graphics.centeredText(MC.font, text, hudWidth() / 2, 5, 0xFFF6F6F2);
	}

	private void updateSummary() {
		if (collected == 0) {
			average = MC.getFps();
			onePercentLow = average;
			return;
		}
		int[] sorted = Arrays.copyOf(samples, collected);
		Arrays.sort(sorted);
		long total = 0;
		for (int value : sorted) {
			total += value;
		}
		average = Math.round(total / (float) collected);
		int lowCount = Math.max(1, (int) Math.ceil(collected * 0.01));
		long lowTotal = 0;
		for (int index = 0; index < lowCount; index++) {
			lowTotal += sorted[index];
		}
		onePercentLow = Math.round(lowTotal / (float) lowCount);
	}

	@Override
	public int hudWidth() {
		return 148;
	}

	@Override
	public int hudHeight() {
		return 18;
	}
}
