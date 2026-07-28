package dev.hackmc.client.module.impl;

import dev.hackmc.client.module.Module;
import dev.hackmc.client.module.ModuleCategory;

public final class FlyModule extends Module {
	private boolean originalMayFly;
	private boolean originalFlying;
	private float originalSpeed;

	public FlyModule() {
		super("fly", "Fly", "Enables creative-style flight.", ModuleCategory.MOVEMENT, false);
	}

	@Override
	protected void onEnable() {
		if (MC.player == null) {
			return;
		}
		var abilities = MC.player.getAbilities();
		originalMayFly = abilities.mayfly;
		originalFlying = abilities.flying;
		originalSpeed = abilities.getFlyingSpeed();
	}

	@Override
	public void tick() {
		if (MC.player == null) {
			return;
		}
		var abilities = MC.player.getAbilities();
		abilities.mayfly = true;
		abilities.flying = true;
		abilities.setFlyingSpeed(0.08F);
	}

	@Override
	protected void onDisable() {
		if (MC.player == null) {
			return;
		}
		var abilities = MC.player.getAbilities();
		abilities.mayfly = originalMayFly;
		abilities.flying = originalFlying;
		abilities.setFlyingSpeed(originalSpeed);
		MC.player.onUpdateAbilities();
	}
}
