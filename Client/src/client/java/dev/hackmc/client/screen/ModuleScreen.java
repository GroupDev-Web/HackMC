package dev.hackmc.client.screen;

import dev.hackmc.client.HackMcClient;
import dev.hackmc.client.module.Module;
import dev.hackmc.screenapi.component.GlassButton;
import dev.hackmc.screenapi.component.GlassToggle;
import dev.hackmc.screenapi.screen.GlossyScreen;
import dev.hackmc.screenapi.theme.ScreenFonts;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

public final class ModuleScreen extends GlossyScreen {
	private final Screen parent;
	private int footerY;

	public ModuleScreen(Screen parent) {
		super("HackMC");
		this.parent = parent;
	}

	@Override
	protected void buildScreen(int x, int y, int width, int height) {
		footerY = y + height - 29;
		List<Module> modules = HackMcClient.modules().all();
		int gap = 8;
		int columnWidth = (width - 44 - gap) / 2;
		int startX = x + 18;
		int startY = y + 45;

		for (int i = 0; i < modules.size(); i++) {
			Module module = modules.get(i);
			int column = i % 2;
			int row = i / 2;
			int controlX = startX + column * (columnWidth + gap);
			int controlY = startY + row * 39;
			addControl(new GlassToggle(controlX, controlY, columnWidth, 31, module.name(),
					module::isEnabled, module::setEnabled, theme));
		}

		addControl(new GlassButton(x + 18, y + height - 34, 124, 22, "HUD Editor",
				() -> minecraft.setScreen(parent), theme));
		addControl(new GlassButton(x + width - 94, y + height - 34, 76, 22, "Done", this::onClose, theme));
	}

	@Override
	protected void extractGlassContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.text(font, ScreenFonts.text("26.1.2 • Fabric • MojMap"), (width - font.width("26.1.2 • Fabric • MojMap")) / 2,
				footerY + 7, theme.mutedText(), false);
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}
}
