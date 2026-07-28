package dev.hackmc.client.module.impl;

import dev.hackmc.client.module.Module;
import dev.hackmc.client.module.ModuleCategory;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public final class NoFallModule extends Module {
	public NoFallModule() {
		super("no_fall", "No Fall", "Prevents ordinary fall damage.", ModuleCategory.PLAYER, false);
	}

	@Override
	public void tick() {
		if (MC.player == null || MC.player.fallDistance < 2.5) {
			return;
		}
		MC.player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(true, false));
		MC.player.fallDistance = 0.0;
	}
}
