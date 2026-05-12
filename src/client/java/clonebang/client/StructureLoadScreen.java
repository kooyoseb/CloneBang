package clonebang.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class StructureLoadScreen extends Screen {
	private List<SavedStructure> structures = List.of();

	protected StructureLoadScreen() {
		super(Component.translatable("screen.clonebang.load_structure"));
	}

	@Override
	protected void init() {
		structures = SavedStructure.list();
		int y = 36;
		for (SavedStructure structure : structures.stream().limit(8).toList()) {
			addRenderableWidget(Button.builder(Component.literal(structure.name()), button -> {
				CloneBangClient.loadStructure(structure);
				onClose();
			}).bounds(width / 2 - 130, y, 260, 20).build());
			y += 24;
		}
		if (structures.isEmpty()) {
			addRenderableWidget(Button.builder(Component.translatable("screen.clonebang.no_structures"), button -> {
			}).bounds(width / 2 - 130, y, 260, 20).build());
		}
		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
				.bounds(width / 2 - 75, height - 34, 150, 20)
				.build());
	}
}
