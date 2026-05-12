package clonebang.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class CloneToolSettingsScreen extends Screen {
	private final CloneBangConfig config;
	private EditBox excludedBlockField;
	private int closeGuardTicks = 12;
	private boolean waitingForOpenShortcutRelease = true;

	protected CloneToolSettingsScreen(CloneBangConfig config) {
		super(Component.translatable("screen.clonebang.settings"));
		this.config = config;
	}

	@Override
	protected void init() {
		int center = width / 2;
		int y = 36;
		addRenderableWidget(toggle(center - 155, y, "screen.clonebang.outline_only", config.outlineOnly, button -> {
			config.outlineOnly = !config.outlineOnly;
			config.save();
			refresh();
		}));
		addRenderableWidget(toggle(center + 5, y, "screen.clonebang.preview_after_selection", config.previewAfterSelection, button -> {
			config.previewAfterSelection = !config.previewAfterSelection;
			config.save();
			refresh();
		}));
		y += 26;
		addRenderableWidget(toggle(center - 155, y, "screen.clonebang.overlap_blocks", config.overlapBlocks, button -> {
			config.overlapBlocks = !config.overlapBlocks;
			config.save();
			refresh();
		}));
		addRenderableWidget(toggle(center + 5, y, "screen.clonebang.allow_blocks", config.allowBlocks, button -> {
			config.allowBlocks = !config.allowBlocks;
			config.save();
			refresh();
		}));
		y += 26;
		addRenderableWidget(toggle(center - 155, y, "screen.clonebang.allow_entities", config.allowEntities, button -> {
			config.allowEntities = !config.allowEntities;
			config.save();
			refresh();
		}));
		addRenderableWidget(Button.builder(Component.translatable("screen.clonebang.shape", Component.translatable("screen.clonebang.shape." + config.shape.name().toLowerCase())), button -> {
			CloneBangConfig.Shape[] values = CloneBangConfig.Shape.values();
			config.shape = values[(config.shape.ordinal() + 1) % values.length];
			config.save();
			refresh();
		}).bounds(center + 5, y, 150, 20).build());
		y += 34;
		addRenderableWidget(Button.builder(Component.translatable("screen.clonebang.render_mode", Component.translatable("screen.clonebang.render_mode." + config.selectionRenderMode.name().toLowerCase())), button -> {
			CloneBangConfig.SelectionRenderMode[] values = CloneBangConfig.SelectionRenderMode.values();
			config.selectionRenderMode = values[(config.selectionRenderMode.ordinal() + 1) % values.length];
			config.save();
			refresh();
		}).bounds(center - 155, y, 310, 20).build());
		y += 30;
		Button pluginStatus = Button.builder(Component.translatable(PluginBridgeClient.statusTranslationKey()), button -> {
		}).bounds(center - 155, y, 310, 20).build();
		pluginStatus.active = false;
		addRenderableWidget(pluginStatus);
		y += 26;
		excludedBlockField = new EditBox(font, center - 155, y, 230, 20, Component.translatable("screen.clonebang.excluded_block"));
		excludedBlockField.setHint(Component.literal("minecraft:air"));
		addRenderableWidget(excludedBlockField);
		addRenderableWidget(Button.builder(Component.translatable("screen.clonebang.add"), button -> addExcludedBlock()).bounds(center + 80, y, 75, 20).build());
		y += 26;
		addRenderableWidget(Button.builder(Component.translatable("screen.clonebang.open_data"), button -> CloneBangClient.openDataFolder()).bounds(center - 155, y, 100, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("screen.clonebang.reset_config"), button -> {
			config.reset();
			refresh();
		}).bounds(center - 50, y, 100, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("screen.clonebang.clear_data"), button -> CloneBangClient.clearData()).bounds(center + 55, y, 100, 20).build());
		y += 28;
		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose()).bounds(center - 75, y, 150, 20).build());
	}

	@Override
	public void tick() {
		if (closeGuardTicks > 0) {
			closeGuardTicks--;
		}
		if (!isShiftOrAltDown()) {
			waitingForOpenShortcutRelease = false;
		}
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.isEscape() && !canCloseFromKeyboard()) {
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return canCloseFromKeyboard();
	}

	private Button toggle(int x, int y, String key, boolean value, Button.OnPress action) {
		return Button.builder(Component.translatable(key, Component.translatable(value ? "options.on" : "options.off")), action)
				.bounds(x, y, 150, 20)
				.build();
	}

	private void addExcludedBlock() {
		Identifier id = Identifier.tryParse(excludedBlockField.getValue().trim());
		if (id != null && BuiltInRegistries.BLOCK.containsKey(id)) {
			config.excludedBlocks.add(id);
			config.save();
			excludedBlockField.setValue("");
		}
	}

	private void refresh() {
		clearWidgets();
		init();
	}

	private boolean canCloseFromKeyboard() {
		return closeGuardTicks <= 0 && !waitingForOpenShortcutRelease;
	}

	private boolean isShiftOrAltDown() {
		Minecraft client = Minecraft.getInstance();
		return InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
				|| InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT)
				|| InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_LEFT_ALT)
				|| InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_RIGHT_ALT);
	}
}
