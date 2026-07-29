package dev.hackmc.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.hackmc.client.module.ModuleRegistry;
import dev.hackmc.client.module.HudModule;
import dev.hackmc.client.module.impl.ZoomModule;
import dev.hackmc.client.screen.ModuleScreen;
import dev.hackmc.client.screen.HackMcHomeScreen;
import dev.hackmc.screenapi.hud.HudEditorScreen;
import dev.hackmc.screenapi.hud.HudManager;
import dev.hackmc.screenapi.hud.HudWidget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class HackMcClient implements ClientModInitializer {
	private static final ModuleRegistry MODULES = new ModuleRegistry();

	public static ModuleRegistry modules() {
		return MODULES;
	}

	@Override
	public void onInitializeClient() {
		int hudIndex = 0;
		for (var module : MODULES.all()) {
			if (!(module instanceof HudModule hud)) {
				continue;
			}
			int column = hudIndex / 7;
			int row = hudIndex % 7;
			int x = 8 + column * 138;
			int y = 8 + row * 28;
			HudManager.register(new HudWidget("hackmc:" + module.id(), module.name(), x, y,
					hud.hudWidth(), hud.hudHeight(), module::isEnabled,
					(graphics, drawX, drawY, editing) -> module.renderAt(graphics, drawX, drawY)));
			hudIndex++;
		}

		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath("hackmc", "controls"));
		KeyMapping openClient = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.hackmc.open_client", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
		KeyMapping zoomKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.hackmc.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, category));
		ZoomModule zoom = (ZoomModule) MODULES.find("zoom").orElseThrow();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			zoom.setKeyDown(zoomKey.isDown());
			MODULES.tick();
			if (client.screen instanceof TitleScreen) {
				client.setScreen(new HackMcHomeScreen());
			}
			while (openClient.consumeClick()) {
				if (client.screen instanceof HudEditorScreen editor) {
					editor.onClose();
				} else {
					client.setScreen(new HudEditorScreen(client.screen, "MIDNIGHT CLIENT", ModuleScreen::new));
				}
			}
		});
	}
}
