package dev.hackmc.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.hackmc.client.module.ModuleRegistry;
import dev.hackmc.client.screen.ModuleScreen;
import dev.hackmc.screenapi.hud.HudEditorScreen;
import dev.hackmc.screenapi.hud.HudManager;
import dev.hackmc.screenapi.hud.HudWidget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class HackMcClient implements ClientModInitializer {
	private static final ModuleRegistry MODULES = new ModuleRegistry();

	public static ModuleRegistry modules() {
		return MODULES;
	}

	@Override
	public void onInitializeClient() {
		var fps = MODULES.find("fps_counter").orElseThrow();
		var cps = MODULES.find("cps_counter").orElseThrow();
		var keys = MODULES.find("keystrokes").orElseThrow();
		HudManager.register(new HudWidget("hackmc:fps", "FPS Counter", 8, 8, 64, 10,
				fps::isEnabled, (graphics, x, y, editing) -> fps.renderAt(graphics, x, y)));
		HudManager.register(new HudWidget("hackmc:cps", "CPS Counter", 8, 24, 82, 10,
				cps::isEnabled, (graphics, x, y, editing) -> cps.renderAt(graphics, x, y)));
		HudManager.register(new HudWidget("hackmc:keystrokes", "Keystrokes", 8, 42, 54, 52,
				keys::isEnabled, (graphics, x, y, editing) -> keys.renderAt(graphics, x, y)));

		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath("hackmc", "controls"));
		KeyMapping openClient = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.hackmc.open_client", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			MODULES.tick();
			while (openClient.consumeClick()) {
				client.setScreen(new HudEditorScreen(client.screen, "HACKMC", ModuleScreen::new));
			}
		});
	}
}
