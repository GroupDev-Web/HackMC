package dev.hackmc.client.module.impl;

import dev.hackmc.client.module.Module;
import dev.hackmc.client.module.ModuleCategory;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsPreset;

public final class FpsBoostModule extends Module {
	private GraphicsPreset oldGraphics;
	private CloudStatus oldClouds;
	private boolean oldShadows;
	private boolean oldAo;
	private int oldRenderDistance;

	public FpsBoostModule() {
		super("fps_boost", "FPS Boost", "Applies reversible low-overhead video settings.", ModuleCategory.PERFORMANCE, false);
	}

	@Override
	protected void onEnable() {
		oldGraphics = MC.options.graphicsPreset().get();
		oldClouds = MC.options.cloudStatus().get();
		oldShadows = MC.options.entityShadows().get();
		oldAo = MC.options.ambientOcclusion().get();
		oldRenderDistance = MC.options.renderDistance().get();

		MC.options.graphicsPreset().set(GraphicsPreset.FAST);
		MC.options.cloudStatus().set(CloudStatus.OFF);
		MC.options.entityShadows().set(false);
		MC.options.ambientOcclusion().set(false);
		MC.options.renderDistance().set(Math.min(oldRenderDistance, 8));
	}

	@Override
	protected void onDisable() {
		if (oldGraphics == null) {
			return;
		}
		MC.options.graphicsPreset().set(oldGraphics);
		MC.options.cloudStatus().set(oldClouds);
		MC.options.entityShadows().set(oldShadows);
		MC.options.ambientOcclusion().set(oldAo);
		MC.options.renderDistance().set(oldRenderDistance);
	}
}
