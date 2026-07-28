package dev.hackmc.client.module.impl;

import dev.hackmc.client.module.Module;
import dev.hackmc.client.module.ModuleCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

public final class NukerModule extends Module {
	private static final int RADIUS = 4;

	public NukerModule() {
		super("nuker", "Nuker", "Breaks nearby blocks in survival reach.", ModuleCategory.WORLD, false);
	}

	@Override
	public void tick() {
		if (MC.player == null || MC.level == null || MC.gameMode == null) {
			return;
		}

		BlockPos center = MC.player.blockPosition();
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, -RADIUS, -RADIUS),
				center.offset(RADIUS, RADIUS, RADIUS))) {
			var state = MC.level.getBlockState(pos);
			if (state.isAir() || state.is(Blocks.BEDROCK) || MC.player.distanceToSqr(pos.getCenter()) > 25.0) {
				continue;
			}
			MC.gameMode.startDestroyBlock(pos.immutable(), Direction.UP);
			MC.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
			return;
		}
	}
}
