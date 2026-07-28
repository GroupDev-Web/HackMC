package dev.hackmc.screenapi;

import com.mojang.blaze3d.platform.InputConstants;
import dev.hackmc.screenapi.hud.HudEditorScreen;
import dev.hackmc.screenapi.hud.HudManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class ScreenApi implements ClientModInitializer {
	public static final String MOD_ID = "screenapi";

	@Override
	public void onInitializeClient() {
		HudElementRegistry.addLast(id("hud"), (graphics, tickCounter) -> HudManager.render(graphics));

		KeyMapping.Category category = KeyMapping.Category.register(id("controls"));
		KeyMapping editorKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.screenapi.open_hud_editor", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, category));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (editorKey.consumeClick()) {
				client.setScreen(new HudEditorScreen(client.screen));
			}
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
