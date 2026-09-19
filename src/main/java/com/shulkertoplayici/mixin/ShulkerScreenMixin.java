package com.shulkertoplayici.mixin;

import com.shulkertoplayici.core.ShulkerLooterEngine;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class ShulkerScreenMixin {

    @Inject(method = "init", at = @At("TAIL"))
    private void onScreenInit(CallbackInfo ci) {
        ShulkerLooterEngine.checkAndLoot();
    }

    @Inject(method = "handledScreenTick", at = @At("HEAD"))
    private void onHandledScreenTick(CallbackInfo ci) {
        ShulkerLooterEngine.checkAndLoot();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ShulkerLooterEngine.checkAndLoot();
    }
}
