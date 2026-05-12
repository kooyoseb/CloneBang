package clonebang;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloneBang implements ModInitializer {
	public static final String MOD_ID = "clonebang";
	public static final String TOOL_NAME_KEY = "item.clonebang.clone_tool";
	public static final int PLUGIN_API_PROTOCOL = 1;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				Commands.literal("CloneTool")
						.executes(context -> {
							ItemStack stack = new ItemStack(Items.BLAZE_ROD);
							stack.set(DataComponents.CUSTOM_NAME, Component.translatable(TOOL_NAME_KEY));
							context.getSource().getPlayerOrException().getInventory().add(stack);
							context.getSource().sendSuccess(() -> Component.translatable("message.clonebang.tool_given"), false);
							return 1;
						})
		));

		LOGGER.info("CloneBang loaded");
	}
}
