package clonebang.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class StructureSaveScreen extends Screen {
	private final CloneSelection selection;
	private EditBox nameField;

	protected StructureSaveScreen(CloneSelection selection) {
		super(Component.translatable("screen.clonebang.save_structure"));
		this.selection = selection;
	}

	@Override
	protected void init() {
		nameField = new EditBox(font, width / 2 - 120, height / 2 - 22, 240, 20, Component.translatable("screen.clonebang.structure_name"));
		nameField.setValue(SavedStructure.defaultName());
		addRenderableWidget(nameField);
		addRenderableWidget(Button.builder(Component.translatable("screen.clonebang.save"), button -> save())
				.bounds(width / 2 - 120, height / 2 + 8, 116, 20)
				.build());
		addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
				.bounds(width / 2 + 4, height / 2 + 8, 116, 20)
				.build());
	}

	private void save() {
		String name = nameField.getValue().replaceAll("[^A-Za-z0-9_\\-]", "_");
		if (name.isBlank()) {
			name = SavedStructure.defaultName();
		}
		SavedStructure.fromSelection(name, selection).save();
		onClose();
	}
}
