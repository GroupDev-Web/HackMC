package dev.hackmc.screenapi.theme;

public record ScreenTheme(
		int backdrop,
		int panel,
		int panelEdge,
		int glossTop,
		int glossBottom,
		int accent,
		int accentBright,
		int text,
		int mutedText,
		int control,
		int controlHover,
		int shadow,
		int radius
) {
	public static final ScreenTheme OBSIDIAN_GLASS = new ScreenTheme(
			0x54030810,
			0xB0161B27,
			0x70FFFFFF,
			0x42FFFFFF,
			0x06FFFFFF,
			0xFF7C5CFC,
			0xFF9E89FF,
			0xFFF8F9FF,
			0xFFADB4C7,
			0x9A252C3D,
			0xC0384259,
			0x70000000,
			8
	);
}
