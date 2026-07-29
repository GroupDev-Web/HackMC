package dev.hackmc.client.module;

import dev.hackmc.client.module.impl.CpsCounterModule;
import dev.hackmc.client.module.impl.BooleanOptionModule;
import dev.hackmc.client.module.impl.FpsCounterModule;
import dev.hackmc.client.module.impl.KeystrokesModule;
import dev.hackmc.client.module.impl.PerformanceHudModule;
import dev.hackmc.client.module.impl.TextHudModule;
import dev.hackmc.client.module.impl.ZoomModule;
import dev.hackmc.client.module.impl.ToggleSprintModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.BlockHitResult;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public final class ModuleRegistry {
	private static final Minecraft MC = Minecraft.getInstance();
	private static final long SESSION_STARTED = System.currentTimeMillis();
	private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

	private final List<Module> modules = List.of(
			new FpsCounterModule(),
			new CpsCounterModule(),
			new KeystrokesModule(),
			hud("coordinates", "Coordinates", "Shows your block position.", true, 112,
					mc -> mc.player == null ? "XYZ —" : "XYZ " + mc.player.blockPosition().toShortString()),
			hud("direction", "Direction", "Shows the direction you are facing.", false, 88,
					mc -> mc.player == null ? "Direction —" : capitalize(mc.player.getDirection().getName())),
			hud("clock", "Clock", "Shows local system time.", false, 64,
					mc -> LocalTime.now().format(CLOCK)),
			hud("ping", "Ping", "Shows your server latency.", false, 72, ModuleRegistry::pingText),
			hud("speed", "Speed", "Shows horizontal movement speed.", false, 88,
					mc -> mc.player == null ? "0.00 m/s" :
							String.format("%.2f m/s", mc.player.getDeltaMovement().horizontalDistance() * 20.0)),
			hud("biome", "Biome", "Shows the current biome.", false, 112, ModuleRegistry::biomeText),
			hud("memory", "Memory", "Shows Java heap usage.", false, 92, mc -> {
				Runtime runtime = Runtime.getRuntime();
				return (runtime.totalMemory() - runtime.freeMemory()) / 1_048_576L + " MB";
			}),
			hud("session_time", "Session Time", "Shows time since the client started.", false, 94,
					mc -> formatDuration(System.currentTimeMillis() - SESSION_STARTED)),
			hud("server_address", "Server", "Shows the connected server address.", false, 130,
					mc -> mc.getCurrentServer() == null ? "Singleplayer" : mc.getCurrentServer().ip),
			hud("durability", "Durability", "Shows held-item durability.", false, 96, mc -> {
				if (mc.player == null || !mc.player.getMainHandItem().isDamageableItem()) return "Durability —";
				var stack = mc.player.getMainHandItem();
				return "Durability " + (stack.getMaxDamage() - stack.getDamageValue());
			}),
			hud("armor_status", "Armor Status", "Shows equipped armor count.", false, 82, mc -> {
				if (mc.player == null) return "Armor —";
				int count = 0;
				for (var slot : List.of(
						net.minecraft.world.entity.EquipmentSlot.HEAD,
						net.minecraft.world.entity.EquipmentSlot.CHEST,
						net.minecraft.world.entity.EquipmentSlot.LEGS,
						net.minecraft.world.entity.EquipmentSlot.FEET)) {
					if (!mc.player.getItemBySlot(slot).isEmpty()) count++;
				}
				return "Armor " + count + "/4";
			}),
			hud("light_level", "Light Level", "Shows local block light.", false, 76,
					mc -> mc.player == null || mc.level == null ? "Light —" :
							"Light " + mc.level.getMaxLocalRawBrightness(mc.player.blockPosition())),
			hud("day_counter", "Day Counter", "Shows the current world day.", false, 70,
					mc -> mc.level == null ? "Day —" : "Day " + (mc.level.getLevelData().getGameTime() / 24_000L)),
			hud("target_block", "Target Block", "Shows the block under your crosshair.", false, 120,
					ModuleRegistry::targetBlockText),
			hud("chunk_coordinates", "Chunk Coordinates", "Shows your current chunk.", false, 88,
					mc -> mc.player == null ? "Chunk —" : "Chunk "
							+ (mc.player.blockPosition().getX() >> 4) + ", "
							+ (mc.player.blockPosition().getZ() >> 4)),
			hud("health", "Health", "Shows exact health.", false, 76,
					mc -> mc.player == null ? "Health —" : String.format("Health %.1f", mc.player.getHealth())),
			hud("food", "Food", "Shows exact hunger.", false, 68,
					mc -> mc.player == null ? "Food —" : "Food " + mc.player.getFoodData().getFoodLevel()),
			hud("experience", "Experience", "Shows your experience level.", false, 68,
					mc -> mc.player == null ? "Level —" : "Level " + mc.player.experienceLevel),
			new PerformanceHudModule(),
			hud("rotation", "Rotation", "Shows precise yaw and pitch.", false, 112,
					mc -> mc.player == null ? "Yaw —  Pitch —" :
							String.format("Yaw %.1f  Pitch %.1f", mc.player.getYRot(), mc.player.getXRot())),
			hud("player_count", "Player Count", "Shows the number of players online.", false, 82,
					mc -> mc.getConnection() == null ? "Players —" :
							"Players " + mc.getConnection().getOnlinePlayers().size()),
			hud("held_count", "Held Item Count", "Shows the held stack count.", false, 78,
					mc -> mc.player == null || mc.player.getMainHandItem().isEmpty() ? "Count —" :
							"Count " + mc.player.getMainHandItem().getCount()),
			hud("world_time", "World Time", "Shows the current in-game time.", false, 78,
					mc -> mc.level == null ? "Time —" : formatWorldTime(mc.level.getLevelData().getGameTime())),
			hud("frame_time", "Frame Time", "Shows the current frame duration in milliseconds.", false, 82,
					mc -> String.format("%.1f ms", 1_000.0 / Math.max(1, mc.getFps()))),
			hud("dimension", "Dimension", "Shows the current dimension.", false, 100,
					mc -> mc.level == null ? "Dimension —" :
							capitalize(mc.level.dimension().identifier().getPath().replace('_', ' '))),
			hud("effects", "Potion Effects", "Shows the number of active status effects.", false, 76,
					mc -> mc.player == null ? "Effects —" : "Effects " + mc.player.getActiveEffects().size()),
			hud("target_distance", "Target Distance", "Shows distance to the crosshair target.", false, 92,
					ModuleRegistry::targetDistanceText),
			hud("velocity", "Velocity", "Shows precise horizontal and vertical velocity.", false, 116,
					mc -> mc.player == null ? "Velocity —" : String.format("H %.2f  V %.2f",
							mc.player.getDeltaMovement().horizontalDistance() * 20.0,
							mc.player.getDeltaMovement().y * 20.0)),
			hud("memory_percent", "Memory Percent", "Shows used heap as a percentage of allocated heap.", false, 86,
					mc -> {
						Runtime runtime = Runtime.getRuntime();
						long used = runtime.totalMemory() - runtime.freeMemory();
						return "Memory " + Math.round(used * 100.0 / runtime.maxMemory()) + "%";
					}),
			new ZoomModule(),
			new ToggleSprintModule(),
			option("no_view_bobbing", "No View Bobbing", "Keeps the camera steady while walking.",
					ModuleCategory.RENDER, () -> MC.options.bobView().get(), value -> MC.options.bobView().set(value), false),
			option("hide_shadows", "Hide Shadows", "Disables entity shadows.", ModuleCategory.PERFORMANCE,
					() -> MC.options.entityShadows().get(), value -> MC.options.entityShadows().set(value), false),
			option("hide_vignette", "Hide Vignette", "Removes the dark screen-edge vignette.", ModuleCategory.RENDER,
					() -> MC.options.vignette().get(), value -> MC.options.vignette().set(value), false),
			option("hide_lightning", "Hide Lightning Flash", "Reduces bright lightning flashes.", ModuleCategory.RENDER,
					() -> MC.options.hideLightningFlash().get(), value -> MC.options.hideLightningFlash().set(value), true),
			option("high_contrast", "High Contrast", "Enables high-contrast interface resources.", ModuleCategory.RENDER,
					() -> MC.options.highContrast().get(), value -> MC.options.highContrast().set(value), true),
			option("auto_jump", "Auto Jump", "Automatically jumps at one-block obstacles.", ModuleCategory.MOVEMENT,
					() -> MC.options.autoJump().get(), value -> MC.options.autoJump().set(value), true),
			option("fast_leaves", "Fast Leaves", "Uses faster opaque leaf rendering.", ModuleCategory.PERFORMANCE,
					() -> MC.options.cutoutLeaves().get(), value -> MC.options.cutoutLeaves().set(value), false),
			option("simple_transparency", "Simple Transparency", "Uses less expensive transparency rendering.",
					ModuleCategory.PERFORMANCE, () -> MC.options.improvedTransparency().get(),
					value -> MC.options.improvedTransparency().set(value), false),
			option("no_ambient_occlusion", "No Ambient Occlusion", "Disables smooth block shading.",
					ModuleCategory.PERFORMANCE, () -> MC.options.ambientOcclusion().get(),
					value -> MC.options.ambientOcclusion().set(value), false)
	);

	public ModuleRegistry() {
		ModuleConfig.load(modules);
		modules.forEach(module -> module.onChanged(() -> ModuleConfig.save(modules)));
	}

	private static TextHudModule hud(String id, String name, String description, boolean enabled, int width,
			java.util.function.Function<Minecraft, String> text) {
		return new TextHudModule(id, name, description, enabled, width, text);
	}

	private static BooleanOptionModule option(String id, String name, String description, ModuleCategory category,
			java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter, boolean value) {
		return new BooleanOptionModule(id, name, description, category, getter, setter, value);
	}

	private static String pingText(Minecraft mc) {
		if (mc.player == null || mc.getConnection() == null) return "Ping —";
		var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
		return info == null ? "Ping —" : info.getLatency() + " ms";
	}

	private static String biomeText(Minecraft mc) {
		if (mc.player == null || mc.level == null) return "Biome —";
		return mc.level.getBiome(mc.player.blockPosition()).unwrapKey()
				.map(key -> capitalize(key.identifier().getPath().replace('_', ' ')))
				.orElse("Biome —");
	}

	private static String targetBlockText(Minecraft mc) {
		if (!(mc.hitResult instanceof BlockHitResult hit) || mc.level == null) return "Target —";
		return mc.level.getBlockState(hit.getBlockPos()).getBlock().getName().getString();
	}

	private static String targetDistanceText(Minecraft mc) {
		if (mc.player == null || mc.hitResult == null) return "Distance —";
		double distance = mc.hitResult.getLocation().distanceTo(mc.player.getEyePosition());
		return String.format("Distance %.2f", distance);
	}

	private static String formatDuration(long millis) {
		long seconds = millis / 1000L;
		return String.format("%02d:%02d:%02d", seconds / 3600L, seconds / 60L % 60L, seconds % 60L);
	}

	private static String formatWorldTime(long dayTime) {
		long minutes = Math.floorMod(dayTime + 6_000L, 24_000L) * 1_440L / 24_000L;
		return String.format("Time %02d:%02d", minutes / 60L, minutes % 60L);
	}

	private static String capitalize(String value) {
		return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}

	public List<Module> all() {
		return modules;
	}

	public Optional<Module> find(String id) {
		return modules.stream().filter(module -> module.id().equals(id)).findFirst();
	}

	public void tick() {
		modules.stream().filter(Module::isEnabled).forEach(Module::tick);
	}

	public void render(GuiGraphicsExtractor graphics) {
		modules.stream().filter(Module::isEnabled).forEach(module -> module.render(graphics));
	}
}
