package dev.hackmc.client.screen;

import dev.hackmc.client.HackMcClient;
import dev.hackmc.client.module.Module;
import dev.hackmc.screenapi.component.GlassButton;
import dev.hackmc.screenapi.component.GlassModuleCard;
import dev.hackmc.screenapi.screen.GlossyScreen;
import dev.hackmc.screenapi.theme.ScreenFonts;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

public final class ModuleScreen extends GlossyScreen {
	private static final int COLUMNS = 3;
	private final Screen parent;
	private int footerY;
	private int scrollRow;
	private int maxScrollRow;
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;
	private String query = "";
	private EditBox searchBox;
	private boolean rebuildForSearch;

	public ModuleScreen(Screen parent) {
		super("Midnight Client");
		this.parent = parent;
	}

	@Override
	protected void buildScreen(int x, int y, int width, int height) {
		panelX = x;
		panelY = y;
		panelWidth = width;
		panelHeight = height;
		footerY = y + height - 29;
		List<Module> modules = HackMcClient.modules().all().stream()
				.filter(this::matchesSearch)
				.toList();
		int totalRows = (modules.size() + COLUMNS - 1) / COLUMNS;
		int visibleRows = 3;
		maxScrollRow = Math.max(0, totalRows - visibleRows);
		scrollRow = Math.max(0, Math.min(scrollRow, maxScrollRow));
		int first = scrollRow * COLUMNS;
		List<Module> visibleModules = modules.subList(first,
				Math.min(first + visibleRows * COLUMNS, modules.size()));
		int gap = 7;
		int columnWidth = (width - 56 - gap * 2) / COLUMNS;
		int startX = x + 18;
		searchBox = new EditBox(font, x + 18, y + 39, width - 36, 22,
				Component.literal("Search modules"));
		searchBox.setHint(Component.literal("Search modules…"));
		searchBox.setMaxLength(64);
		searchBox.setBordered(false);
		searchBox.setValue(query);
		searchBox.setResponder(value -> {
			query = value;
			rebuildForSearch = true;
		});
		addRenderableWidget(searchBox);

		int startY = y + 69;
		int rows = (visibleModules.size() + 2) / 3;
		int contentBottom = y + height - 42;
		int cardHeight = Math.min(75, Math.max(34,
				(contentBottom - startY - gap * (rows - 1)) / rows));

		for (int i = 0; i < visibleModules.size(); i++) {
			Module module = visibleModules.get(i);
			int column = i % COLUMNS;
			int row = i / COLUMNS;
			int controlX = startX + column * (columnWidth + gap);
			int controlY = startY + row * (cardHeight + gap);
			addControl(new GlassModuleCard(controlX, controlY, columnWidth, cardHeight, module.name(),
					module.category().name(), module.description(),
					module::isEnabled, module::setEnabled, theme));
		}

		addControl(new GlassButton(x + 18, y + height - 34, 124, 22, "HUD Editor",
				() -> minecraft.setScreen(parent), theme));
		addControl(new GlassButton(x + width - 94, y + height - 34, 76, 22, "Done", this::onClose, theme));
	}

	private boolean matchesSearch(Module module) {
		if (query.isBlank()) {
			return true;
		}
		String needle = query.toLowerCase(Locale.ROOT).trim();
		return module.name().toLowerCase(Locale.ROOT).contains(needle)
				|| module.description().toLowerCase(Locale.ROOT).contains(needle)
				|| module.category().name().toLowerCase(Locale.ROOT).contains(needle);
	}

	@Override
	public void tick() {
		super.tick();
		if (rebuildForSearch) {
			rebuildForSearch = false;
			rebuildWidgets();
			if (searchBox != null) {
				searchBox.setFocused(true);
			}
		}
	}

	@Override
	protected void extractGlassContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		dev.hackmc.screenapi.render.GlassRenderer.roundedOutline(graphics,
				panelX + 18, panelY + 39, panelWidth - 36, 22, 6,
				searchBox != null && searchBox.isFocused() ? theme.accentBright() : theme.panelEdge());
		long matches = HackMcClient.modules().all().stream().filter(this::matchesSearch).count();
		String scrollText = matches + " MODULES  •  TYPE TO SEARCH  •  SCROLL";
		graphics.text(font, ScreenFonts.text(scrollText), (width - font.width(scrollText)) / 2,
				footerY + 7, theme.mutedText(), false);
		if (maxScrollRow > 0) {
			int trackX = panelX + panelWidth - 15;
			int trackY = panelY + 46;
			int trackHeight = panelHeight - 91;
			int thumbHeight = Math.max(18, trackHeight * 3 / (maxScrollRow + 3));
			int thumbY = trackY + (trackHeight - thumbHeight) * scrollRow / maxScrollRow;
			dev.hackmc.screenapi.render.GlassRenderer.roundedRect(graphics, trackX, trackY, 3, trackHeight, 2, 0x403E465A);
			dev.hackmc.screenapi.render.GlassRenderer.roundedRect(graphics, trackX, thumbY, 3, thumbHeight, 2, theme.accent());
		}
		if (HackMcClient.modules().all().stream().noneMatch(this::matchesSearch)) {
			String empty = "No modules match \"" + query + "\"";
			graphics.centeredText(font, ScreenFonts.text(empty), width / 2,
					panelY + panelHeight / 2, theme.mutedText());
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (mouseX < panelX || mouseX > panelX + panelWidth || mouseY < panelY || mouseY > panelY + panelHeight) {
			return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
		}
		int previous = scrollRow;
		if (scrollY < 0) {
			scrollRow = Math.min(maxScrollRow, scrollRow + 1);
		} else if (scrollY > 0) {
			scrollRow = Math.max(0, scrollRow - 1);
		}
		if (scrollRow != previous) {
			rebuildWidgets();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}
}
