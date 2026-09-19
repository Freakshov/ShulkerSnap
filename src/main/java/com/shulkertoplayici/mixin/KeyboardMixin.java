package com.shulkertoplayici.mixin;

import com.shulkertoplayici.ShulkerToplayiciClient;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKeyInput(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == 1) {
            if (ShulkerToplayiciClient.handleKeyInput(key, scancode)) {
                ci.cancel();
            }
        }
    }
}
