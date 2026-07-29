package dev.hackmc.client.module.impl;

import dev.hackmc.client.module.Module;
import dev.hackmc.client.module.ModuleCategory;

public final class ToggleSprintModule extends Module {
	public ToggleSprintModule() {
		super("toggle_sprint", "Toggle Sprint",
				"Keeps sprint active while moving forward when vanilla permits it.",
				ModuleCategory.MOVEMENT, false);
	}

	@Override
	public void tick() {
		if (MC.player == null) {
			return;
		}
		boolean canSprint = MC.options.keyUp.isDown()
				&& !MC.player.isCrouching()
				&& !MC.player.horizontalCollision
				&& (MC.player.getFoodData().getFoodLevel() > 6 || MC.player.getAbilities().mayfly);
		if (canSprint) {
			MC.player.setSprinting(true);
		}
	}
}
