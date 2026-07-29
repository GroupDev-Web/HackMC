package dev.hackmc.client.screen;

import dev.hackmc.screenapi.component.GlassButton;
import dev.hackmc.screenapi.render.GlassRenderer;
import dev.hackmc.screenapi.theme.ScreenFonts;
import dev.hackmc.screenapi.theme.ScreenTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class HackMcHomeScreen extends Screen {
	private static final List<Identifier> BACKGROUNDS = List.of(
			Identifier.fromNamespaceAndPath("hackmc", "textures/gui/noir_city.png"),
			Identifier.fromNamespaceAndPath("hackmc", "textures/gui/midnight_eclipse.png"),
			Identifier.fromNamespaceAndPath("hackmc", "textures/gui/obsidian_cavern.png"),
			Identifier.fromNamespaceAndPath("hackmc", "textures/gui/winter_home.png")
	);
	private static final Identifier LOGO =
			Identifier.fromNamespaceAndPath("screenapi", "textures/gui/midnight_logo.png");
	private static final ScreenTheme THEME = ScreenTheme.OBSIDIAN_GLASS;
	private final List<Snowflake> snowflakes = new ArrayList<>();
	private int logoSize;
	private int logoY;
	private int backgroundIndex;

	public HackMcHomeScreen() {
		super(ScreenFonts.text("Midnight Client"));
		backgroundIndex = loadBackground();
		Random random = new Random(0x4841434B4D43L);
		for (int i = 0; i < 90; i++) {
			snowflakes.add(new Snowflake(
					random.nextFloat(),
					random.nextFloat(),
					8.0F + random.nextFloat() * 18.0F,
					1 + random.nextInt(3),
					random.nextFloat() * 20.0F + 4.0F,
					random.nextFloat() * 6.28318F
			));
		}
	}

	@Override
	protected void init() {
		int centerX = width / 2;
		logoSize = Math.min(92, Math.max(62, height / 5));
		int compositionHeight = logoSize + 119;
		logoY = Math.max(14, (height - compositionHeight) / 2);
		int startY = logoY + logoSize + 30;
		addRenderableWidget(new GlassButton(centerX - 92, startY, 184, 24, "Singleplayer",
				() -> minecraft.setScreen(new SelectWorldScreen(this)), THEME));
		addRenderableWidget(new GlassButton(centerX - 92, startY + 29, 184, 24, "Multiplayer",
				() -> minecraft.setScreen(new JoinMultiplayerScreen(this)), THEME));
		addRenderableWidget(new GlassButton(centerX - 92, startY + 58, 89, 22, "Options",
				() -> minecraft.setScreen(new OptionsScreen(this, minecraft.options, false)), THEME));
		addRenderableWidget(new GlassButton(centerX + 3, startY + 58, 89, 22, "Quit",
				minecraft::stop, THEME));
		addRenderableWidget(new GlassButton(width - 36, 8, 28, 20, "◐",
				this::nextBackground, THEME));
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.blit(BACKGROUNDS.get(backgroundIndex), 0, 0, width, height, 0, 1, 0, 1);
		graphics.fill(0, 0, width, height, 0x28030A14);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		renderAtmosphere(graphics);

		int logoX = (width - logoSize) / 2;
		graphics.blit(LOGO, logoX, logoY, logoX + logoSize, logoY + logoSize, 0, 1, 0, 1);
		graphics.centeredText(font, ScreenFonts.text("MIDNIGHT CLIENT"), width / 2,
				logoY + logoSize + 3, 0xFFFFFFFF);

		graphics.text(font, ScreenFonts.text("Midnight Client 1.5.0"), 8, height - 14, 0xBFFFFFFF, false);
		super.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	private void renderAtmosphere(GuiGraphicsExtractor graphics) {
		double seconds = System.currentTimeMillis() / 1000.0;
		for (Snowflake flake : snowflakes) {
			switch (backgroundIndex) {
				case 0 -> renderRain(graphics, flake, seconds);
				case 1 -> renderStar(graphics, flake, seconds);
				case 2 -> renderEmber(graphics, flake, seconds);
				default -> renderSnow(graphics, flake, seconds);
			}
		}
	}

	private void renderRain(GuiGraphicsExtractor graphics, Snowflake flake, double seconds) {
		int x = (int) (flake.startX() * width);
		int y = (int) ((flake.startY() * (height + 40) + seconds * flake.speed() * 4.0) % (height + 40)) - 20;
		graphics.fill(x, y, x + 1, y + 6 + flake.size() * 2, 0x6897B5C8);
	}

	private void renderStar(GuiGraphicsExtractor graphics, Snowflake flake, double seconds) {
		int x = (int) (flake.startX() * width);
		int y = (int) (flake.startY() * height * 0.65);
		int alpha = 70 + (int) (Math.abs(Math.sin(seconds * 0.8 + flake.phase())) * 150);
		GlassRenderer.roundedRect(graphics, x, y, flake.size(), flake.size(), 1,
				GlassRenderer.withAlpha(0xFFFFFFFF, alpha));
	}

	private void renderEmber(GuiGraphicsExtractor graphics, Snowflake flake, double seconds) {
		double travel = seconds * flake.speed() * 0.55;
		int y = height - (int) ((flake.startY() * (height + 24) + travel) % (height + 24));
		int x = (int) (flake.startX() * width + Math.sin(seconds + flake.phase()) * flake.drift());
		GlassRenderer.roundedRect(graphics, x, y, flake.size(), flake.size(), 1, 0xBFFF3442);
	}

	private void renderSnow(GuiGraphicsExtractor graphics, Snowflake flake, double seconds) {
		double travel = seconds * flake.speed();
		int y = (int) ((flake.startY() * (height + 24) + travel) % (height + 24)) - 12;
		double sway = Math.sin(seconds * 0.7 + flake.phase()) * flake.drift();
		int x = (int) (flake.startX() * width + sway);
		int alpha = flake.size() == 1 ? 150 : flake.size() == 2 ? 190 : 225;
		GlassRenderer.roundedRect(graphics, x, y, flake.size(), flake.size(), flake.size() / 2,
				GlassRenderer.withAlpha(0xFFFFFFFF, alpha));
	}

	private void nextBackground() {
		backgroundIndex = (backgroundIndex + 1) % BACKGROUNDS.size();
		saveBackground(backgroundIndex);
	}

	private static int loadBackground() {
		try {
			int index = Integer.parseInt(Files.readString(backgroundFile()).trim());
			return Math.floorMod(index, BACKGROUNDS.size());
		} catch (Exception ignored) {
			return 0;
		}
	}

	private static void saveBackground(int index) {
		Path file = backgroundFile();
		Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
		try {
			Files.createDirectories(file.getParent());
			Files.writeString(temporary, Integer.toString(index));
			try {
				Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.ATOMIC_MOVE);
			} catch (Exception unsupportedAtomicMove) {
				Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception ignored) {
			// Theme persistence is cosmetic and must never break the title screen.
		}
	}

	private static Path backgroundFile() {
		return net.minecraft.client.Minecraft.getInstance().gameDirectory.toPath()
				.resolve("config")
				.resolve("midnight-background.txt");
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private record Snowflake(float startX, float startY, float speed, int size, float drift, float phase) {
	}
}
