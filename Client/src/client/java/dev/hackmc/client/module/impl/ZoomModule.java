package dev.hackmc.client.module.impl;

import dev.hackmc.client.module.Module;
import dev.hackmc.client.module.ModuleCategory;

public final class ZoomModule extends Module {
	private static final int ZOOM_FOV = 28;
	private boolean keyDown;
	private boolean applied;
	private int previousFov;

	public ZoomModule() {
		super("zoom", "Zoom", "Hold C for a competitive zoom.", ModuleCategory.RENDER, true);
	}

	public void setKeyDown(boolean keyDown) {
		this.keyDown = keyDown;
	}

	@Override
	public void tick() {
		if (keyDown && !applied) {
			previousFov = MC.options.fov().get();
			MC.options.fov().set(Math.min(previousFov, ZOOM_FOV));
			applied = true;
		} else if (!keyDown && applied) {
			restore();
		}
	}

	@Override
	protected void onDisable() {
		keyDown = false;
		restore();
	}

	private void restore() {
		if (applied) {
			MC.options.fov().set(previousFov);
			applied = false;
		}
	}
}
