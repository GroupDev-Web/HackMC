package dev.hackmc.client.module.impl;

import dev.hackmc.client.module.Module;
import dev.hackmc.client.module.ModuleCategory;
import dev.hackmc.client.module.HudModule;
import dev.hackmc.screenapi.render.GlassRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayDeque;
import java.util.Deque;

public final class CpsCounterModule extends Module implements HudModule {
	private final Deque<Long> leftClicks = new ArrayDeque<>();
	private final Deque<Long> rightClicks = new ArrayDeque<>();
	private boolean leftWasDown;
	private boolean rightWasDown;

	public CpsCounterModule() {
		super("cps_counter", "CPS Counter", "Shows left and right clicks per second.", ModuleCategory.RENDER, true);
	}

	@Override
	public void tick() {
		long now = System.currentTimeMillis();
		boolean leftDown = MC.options.keyAttack.isDown();
		boolean rightDown = MC.options.keyUse.isDown();
		if (leftDown && !leftWasDown) {
			leftClicks.addLast(now);
		}
		if (rightDown && !rightWasDown) {
			rightClicks.addLast(now);
		}
		leftWasDown = leftDown;
		rightWasDown = rightDown;
		removeExpired(leftClicks, now);
		removeExpired(rightClicks, now);
	}

	private static void removeExpired(Deque<Long> clicks, long now) {
		while (!clicks.isEmpty() && now - clicks.peekFirst() > 1_000L) {
			clicks.removeFirst();
		}
	}

	@Override
	public void render(GuiGraphicsExtractor graphics) {
		GlassRenderer.roundedRect(graphics, 0, 0, hudWidth(), hudHeight(), 5, 0xA51A1B18);
		graphics.centeredText(MC.font, leftClicks.size() + " | " + rightClicks.size() + " CPS",
				hudWidth() / 2, 5, 0xFFF6F6F2);
	}

	@Override
	public int hudWidth() {
		return 82;
	}

	@Override
	public int hudHeight() {
		return 18;
	}
}
