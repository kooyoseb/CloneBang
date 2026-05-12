package clonebang.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CloneToolHelpScreen extends Screen {
	protected CloneToolHelpScreen() {
		super(Component.translatable("screen.clonebang.help"));
	}

	@Override
	protected void init() {
		int center = width / 2;
		int y = 34;
		for (int i = 1; i <= 7; i++) {
			addRenderableWidget(Button.builder(Component.translatable("help.clonebang.line" + i), button -> {
			}).bounds(center - 180, y, 360, 20).build());
			y += 23;
		}
		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
				.bounds(center - 75, height - 34, 150, 20)
				.build());
	}
}
