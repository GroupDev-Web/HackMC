package dev.hackmc.screenapi.theme;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public final class ScreenFonts {
	public static final Identifier NOTO_SANS = Identifier.fromNamespaceAndPath("screenapi", "noto_sans");

	private ScreenFonts() {
	}

	public static Component text(String value) {
		return Component.literal(value).withStyle(style -> style.withFont(new FontDescription.Resource(NOTO_SANS)));
	}
}
