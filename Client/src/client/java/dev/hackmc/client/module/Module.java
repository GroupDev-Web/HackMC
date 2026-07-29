package dev.hackmc.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class Module {
	protected static final Minecraft MC = Minecraft.getInstance();

	private final String id;
	private final String name;
	private final String description;
	private final ModuleCategory category;
	private boolean enabled;
	private Runnable changed = () -> {};

	protected Module(String id, String name, String description, ModuleCategory category, boolean enabled) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.category = category;
		this.enabled = enabled;
	}

	public final String id() {
		return id;
	}

	public final String name() {
		return name;
	}

	public final String description() {
		return description;
	}

	public final ModuleCategory category() {
		return category;
	}

	public final boolean isEnabled() {
		return enabled;
	}

	public final void toggle() {
		setEnabled(!enabled);
	}

	public final void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			return;
		}
		this.enabled = enabled;
		if (enabled) {
			onEnable();
		} else {
			onDisable();
		}
		changed.run();
	}

	final void onChanged(Runnable changed) {
		this.changed = changed;
	}

	protected void onEnable() {
	}

	protected void onDisable() {
	}

	public void tick() {
	}

	public void render(GuiGraphicsExtractor graphics) {
	}

	public void renderAt(GuiGraphicsExtractor graphics, int x, int y) {
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		render(graphics);
		graphics.pose().popMatrix();
	}
}
