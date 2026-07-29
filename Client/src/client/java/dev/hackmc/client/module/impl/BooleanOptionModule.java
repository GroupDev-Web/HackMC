package dev.hackmc.client.module.impl;

import dev.hackmc.client.module.Module;
import dev.hackmc.client.module.ModuleCategory;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class BooleanOptionModule extends Module {
	private final BooleanSupplier getter;
	private final Consumer<Boolean> setter;
	private final boolean enabledValue;
	private boolean previousValue;

	public BooleanOptionModule(String id, String name, String description, ModuleCategory category,
			BooleanSupplier getter, Consumer<Boolean> setter, boolean enabledValue) {
		super(id, name, description, category, false);
		this.getter = getter;
		this.setter = setter;
		this.enabledValue = enabledValue;
	}

	@Override
	protected void onEnable() {
		previousValue = getter.getAsBoolean();
		setter.accept(enabledValue);
	}

	@Override
	protected void onDisable() {
		setter.accept(previousValue);
	}
}
