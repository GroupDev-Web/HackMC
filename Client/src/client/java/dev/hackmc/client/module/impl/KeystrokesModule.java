package dev.hackmc.client.module.impl;

import dev.hackmc.client.module.Module;
import dev.hackmc.client.module.ModuleCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class KeystrokesModule extends Module {
	public KeystrokesModule() {
		super("keystrokes", "Keystrokes", "Shows movement and mouse input.", ModuleCategory.RENDER, true);
	}

	@Override
	public void render(GuiGraphicsExtractor graphics) {
		int x = 0;
		int y = 0;
		key(graphics, "W", x + 18, y, MC.options.keyUp.isDown());
		key(graphics, "A", x, y + 18, MC.options.keyLeft.isDown());
		key(graphics, "S", x + 18, y + 18, MC.options.keyDown.isDown());
		key(graphics, "D", x + 36, y + 18, MC.options.keyRight.isDown());
		key(graphics, "LMB", x, y + 36, MC.options.keyAttack.isDown(), 26);
		key(graphics, "RMB", x + 28, y + 36, MC.options.keyUse.isDown(), 26);
	}

	private static void key(GuiGraphicsExtractor graphics, String label, int x, int y, boolean down) {
		key(graphics, label, x, y, down, 16);
	}

	private static void key(GuiGraphicsExtractor graphics, String label, int x, int y, boolean down, int width) {
		graphics.fill(x, y, x + width, y + 16, down ? 0xB0FFFFFF : 0x780A0D14);
		int color = down ? 0xFF11141B : 0xFFF5F7FF;
		graphics.centeredText(MC.font, label, x + width / 2, y + 4, color);
	}
}
