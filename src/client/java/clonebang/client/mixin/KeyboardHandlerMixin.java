package clonebang.client.mixin;

import clonebang.client.CloneBangClient;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
	@Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
	private void clonebang$handleToolShortcuts(long window, int action, KeyEvent event, CallbackInfo info) {
		if (action != 1 && action != 2) {
			return;
		}
		if (CloneBangClient.handleToolShortcutKey(event)) {
			info.cancel();
		}
	}
}
