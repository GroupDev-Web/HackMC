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
			0x24030810,
			0x68161B27,
			0x52FFFFFF,
			0x28FFFFFF,
			0x02FFFFFF,
			0xFFE6323B,
			0xFFFF626A,
			0xFFF8F9FF,
			0xFFADB4C7,
			0x00252C3D,
			0x48384259,
			0x38000000,
			8
	);
}
