package clonebang.client;

import clonebang.CloneBang;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.lwjgl.glfw.GLFW;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;

public class CloneBangClient implements ClientModInitializer {
	public static final CloneSelection SELECTION = new CloneSelection();
	public static CloneBangConfig config;
	public static SavedStructure loadedStructure;
	public static boolean placementPreview;
	private static KeyMapping settingsKey;
	private static KeyMapping saveKey;
	private static KeyMapping loadKey;
	private static KeyMapping helpKey;
	private static KeyMapping copyKey;
	private static KeyMapping pasteKey;
	private static KeyMapping undoKey;
	private static KeyMapping redoKey;
	private static KeyMapping clearSelectionKey;
	private static int particleTick;
	private static boolean settingsComboWasDown;
	private static boolean helpComboWasDown;
	private static boolean loadComboWasDown;
	private static boolean placeComboWasDown;
	private static boolean pendingSettingsScreen;
	private static boolean pendingHelpScreen;
	private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(CloneBang.MOD_ID, "controls"));

	@Override
	public void onInitializeClient() {
		config = CloneBangConfig.load();
		PluginBridgeClient.initialize();
		registerKeys();
		registerSelectionInput();
		LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(CloneBangClient::renderNativeSelection);
		ClientTickEvents.END_CLIENT_TICK.register(CloneBangClient::tickKeys);
	}

	private static void registerKeys() {
		settingsKey = bind("key.clonebang.settings", GLFW.GLFW_KEY_UNKNOWN);
		saveKey = bind("key.clonebang.save", GLFW.GLFW_KEY_S);
		loadKey = bind("key.clonebang.load", GLFW.GLFW_KEY_E);
		helpKey = bind("key.clonebang.help", GLFW.GLFW_KEY_H);
		copyKey = bind("key.clonebang.copy", GLFW.GLFW_KEY_C);
		pasteKey = bind("key.clonebang.paste", GLFW.GLFW_KEY_V);
		undoKey = bind("key.clonebang.undo", GLFW.GLFW_KEY_Z);
		redoKey = bind("key.clonebang.redo", GLFW.GLFW_KEY_Y);
		clearSelectionKey = bind("key.clonebang.clear_selection", GLFW.GLFW_KEY_D);
	}

	private static KeyMapping bind(String translationKey, int key) {
		return KeyMappingHelper.registerKeyMapping(new KeyMapping(
				translationKey,
				InputConstants.Type.KEYSYM,
				key,
				KEY_CATEGORY
		));
	}

	private static void registerSelectionInput() {
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!world.isClientSide() || !isHoldingTool(player.getItemInHand(hand))) {
				return InteractionResult.PASS;
			}
			if (!config.allowBlocks) {
				warn("message.clonebang.blocks_disabled");
				return InteractionResult.FAIL;
			}
			BlockPos pos = config.overlapBlocks ? hitResult.getBlockPos() : hitResult.getBlockPos().relative(hitResult.getDirection());
			BlockState state = world.getBlockState(hitResult.getBlockPos());
			if (config.isExcluded(state.getBlock())) {
				warn("message.clonebang.excluded_block");
				return InteractionResult.FAIL;
			}
			if (hand == InteractionHand.MAIN_HAND) {
				if (player.isShiftKeyDown()) {
					SELECTION.setSecond(pos);
					message("message.clonebang.second_pos", posText(pos));
				} else {
					SELECTION.setFirst(pos);
					message("message.clonebang.first_pos", posText(pos));
				}
			}
			return InteractionResult.SUCCESS;
		});

		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (!world.isClientSide() || !isHoldingTool(player.getItemInHand(hand))) {
				return InteractionResult.PASS;
			}
			Minecraft client = Minecraft.getInstance();
			if (client.hitResult instanceof BlockHitResult blockHitResult) {
				BlockPos pos = config.overlapBlocks ? blockHitResult.getBlockPos() : blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
				SELECTION.setSecond(pos);
				message("message.clonebang.second_pos", posText(pos));
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		});
	}

	private static void tickKeys(Minecraft client) {
		if (client.player == null || !isHoldingTool(client.player.getMainHandItem())) {
			return;
		}
		boolean shift = InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
		boolean alt = InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_LEFT_ALT) || InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_RIGHT_ALT);
		boolean control = InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL) || InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL);
		boolean eDown = InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_E) || loadKey.isDown();

		handleECombos(client, shift, alt, eDown);
		handleDeferredScreens(client, shift, alt);
		if (client.screen != null) {
			return;
		}

		spawnSelectionParticles(client);

		boolean settingsComboDown = shift && (alt || settingsKey.isDown());
		if (settingsComboDown && !settingsComboWasDown) {
			pendingSettingsScreen = true;
		}
		settingsComboWasDown = settingsComboDown;

		boolean helpComboDown = shift && helpKey.isDown();
		if (helpComboDown && !helpComboWasDown) {
			pendingHelpScreen = true;
		}
		helpComboWasDown = helpComboDown;

		while (settingsKey.consumeClick()) {
		}
		while (helpKey.consumeClick()) {
		}
		while (saveKey.consumeClick()) {
			if (shift && SELECTION.isComplete()) {
				client.setScreen(new StructureSaveScreen(SELECTION));
			}
		}
		while (loadKey.consumeClick()) {
		}
		while (copyKey.consumeClick()) {
			if (control) {
				copyCloneCommand(client);
			}
		}
		while (pasteKey.consumeClick()) {
			if (control) {
				pasteCloneCommand(client);
			}
		}
		while (undoKey.consumeClick()) {
			if (control) {
				message("message.clonebang.undo_ready");
			}
		}
		while (redoKey.consumeClick()) {
			if (control) {
				message("message.clonebang.redo_ready");
			}
		}
		while (clearSelectionKey.consumeClick()) {
			if (alt) {
				SELECTION.clear();
				placementPreview = false;
				message("message.clonebang.selection_cleared");
			}
		}
	}

	private static void handleDeferredScreens(Minecraft client, boolean shift, boolean alt) {
		boolean openingModifiersReleased = !shift && !alt && !helpKey.isDown() && !settingsKey.isDown();
		if (!openingModifiersReleased || client.screen != null) {
			return;
		}
		if (pendingSettingsScreen) {
			pendingSettingsScreen = false;
			pendingHelpScreen = false;
			client.setScreen(new CloneToolSettingsScreen(config));
		} else if (pendingHelpScreen) {
			pendingHelpScreen = false;
			client.setScreen(new CloneToolHelpScreen());
		}
	}

	private static void handleECombos(Minecraft client, boolean shift, boolean alt, boolean eDown) {
		boolean loadComboDown = shift && eDown && !alt;
		boolean placeComboDown = alt && eDown;
		boolean canReplaceScreen = client.screen == null || client.screen instanceof InventoryScreen;

		if (loadComboDown && !loadComboWasDown && canReplaceScreen) {
			if (!SELECTION.isComplete()) {
				client.setScreen(new StructureLoadScreen());
			}
		}
		if (placeComboDown && !placeComboWasDown && canReplaceScreen) {
			if (client.screen instanceof InventoryScreen) {
				client.setScreen(null);
			}
			startPlacementPreview();
		}

		loadComboWasDown = loadComboDown;
		placeComboWasDown = placeComboDown;
	}

	public static boolean handleToolShortcutKey(KeyEvent event) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || !isHoldingTool(client.player.getMainHandItem()) || event.key() != GLFW.GLFW_KEY_E) {
			return false;
		}
		boolean shift = event.hasShiftDown();
		boolean alt = event.hasAltDown()
				|| InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_LEFT_ALT)
				|| InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_RIGHT_ALT);
		boolean canReplaceScreen = client.screen == null || client.screen instanceof InventoryScreen;
		if (shift && !alt && canReplaceScreen) {
			if (!SELECTION.isComplete()) {
				client.setScreen(new StructureLoadScreen());
			}
			return true;
		}
		if (alt && canReplaceScreen) {
			if (client.screen instanceof InventoryScreen) {
				client.setScreen(null);
			}
			startPlacementPreview();
			return true;
		}
		return false;
	}

	private static void startPlacementPreview() {
		if (loadedStructure == null) {
			if (SELECTION.isComplete()) {
				loadedStructure = SavedStructure.fromSelection("picked_selection", SELECTION);
				message("message.clonebang.picked_selection");
			} else {
				warn("message.clonebang.no_loaded_structure");
				return;
			}
		}
		placementPreview = true;
		message("message.clonebang.placement_preview");
	}

	public static void loadStructure(SavedStructure structure) {
		loadedStructure = structure;
		CloneSelection loaded = structure.toSelection();
		SELECTION.setFirst(loaded.first());
		SELECTION.setSecond(loaded.second());
		message("message.clonebang.structure_loaded", structure.name());
	}

	public static void openDataFolder() {
		try {
			Files.createDirectories(CloneBangConfig.rootDir());
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().open(CloneBangConfig.rootDir().toFile());
			}
		} catch (IOException e) {
			CloneBang.LOGGER.warn("Failed to open moddata folder", e);
		}
	}

	public static void clearData() {
		try {
			Files.createDirectories(CloneBangConfig.structuresDir());
			try (var paths = Files.list(CloneBangConfig.structuresDir())) {
				for (var path : paths.toList()) {
					Files.deleteIfExists(path);
				}
			}
			loadedStructure = null;
			message("message.clonebang.data_reset");
		} catch (IOException e) {
			CloneBang.LOGGER.warn("Failed to clear CloneBang data", e);
		}
	}

	private static void copyCloneCommand(Minecraft client) {
		if (!SELECTION.isComplete()) {
			warn("message.clonebang.no_selection");
			return;
		}
		String command = SELECTION.cloneCommand(targetPos(client));
		client.keyboardHandler.setClipboard(command);
		message("message.clonebang.command_copied", "/" + command);
	}

	private static void pasteCloneCommand(Minecraft client) {
		if (!SELECTION.isComplete()) {
			warn("message.clonebang.no_selection");
			return;
		}
		client.player.connection.sendCommand(SELECTION.cloneCommand(targetPos(client)));
		message("message.clonebang.command_sent");
	}

	private static BlockPos targetPos(Minecraft client) {
		if (client.hitResult != null && client.hitResult.getType() == HitResult.Type.BLOCK) {
			BlockHitResult hit = (BlockHitResult) client.hitResult;
			return hit.getBlockPos().relative(hit.getDirection());
		}
		return client.player.blockPosition();
	}

	private static void spawnSelectionParticles(Minecraft client) {
		if (config.selectionRenderMode != CloneBangConfig.SelectionRenderMode.PARTICLE || client.level == null || !SELECTION.isComplete() || ++particleTick % 8 != 0) {
			return;
		}
		BlockPos min = SELECTION.min();
		BlockPos max = SELECTION.max().offset(1, 1, 1);
		int step = Math.max(1, Math.max(Math.max(SELECTION.width(), SELECTION.height()), SELECTION.depth()) / 24);
		for (int x = min.getX(); x <= max.getX(); x += step) {
			particle(client, x, min.getY(), min.getZ());
			particle(client, x, min.getY(), max.getZ());
			particle(client, x, max.getY(), min.getZ());
			particle(client, x, max.getY(), max.getZ());
		}
		for (int y = min.getY(); y <= max.getY(); y += step) {
			particle(client, min.getX(), y, min.getZ());
			particle(client, min.getX(), y, max.getZ());
			particle(client, max.getX(), y, min.getZ());
			particle(client, max.getX(), y, max.getZ());
		}
		for (int z = min.getZ(); z <= max.getZ(); z += step) {
			particle(client, min.getX(), min.getY(), z);
			particle(client, min.getX(), max.getY(), z);
			particle(client, max.getX(), min.getY(), z);
			particle(client, max.getX(), max.getY(), z);
		}
	}

	private static void particle(Minecraft client, double x, double y, double z) {
		client.level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.0, 0.0);
	}

	private static void renderNativeSelection(LevelRenderContext context) {
		if (config.selectionRenderMode != CloneBangConfig.SelectionRenderMode.NATIVE || !SELECTION.isComplete() || context.poseStack() == null || context.bufferSource() == null) {
			return;
		}
		BlockPos min = SELECTION.min();
		BlockPos max = SELECTION.max();
		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().position();
		AABB box = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1.0, max.getY() + 1.0, max.getZ() + 1.0)
				.move(-camera.x, -camera.y, -camera.z);
		PoseStack matrices = context.poseStack();
		VertexConsumer consumer = context.bufferSource().getBuffer(RenderTypes.linesTranslucent());
		int color = placementPreview ? 0xFF55AAFF : 0xFF66E6FF;
		ShapeRenderer.renderShape(matrices, consumer, Shapes.create(box), 0.0, 0.0, 0.0, color, 1.0F);
	}

	public static boolean isHoldingTool(ItemStack stack) {
		return stack.is(Items.BLAZE_ROD) && stack.getCustomName() != null
				&& stack.getHoverName().getString().equals(Component.translatable(CloneBang.TOOL_NAME_KEY).getString());
	}

	private static String posText(BlockPos pos) {
		return pos.getX() + " " + pos.getY() + " " + pos.getZ();
	}

	private static void message(String key, Object... args) {
		Minecraft client = Minecraft.getInstance();
		if (client.player != null) {
			client.player.sendSystemMessage(Component.translatable(key, args));
		}
	}

	private static void warn(String key, Object... args) {
		message(key, args);
	}
}
