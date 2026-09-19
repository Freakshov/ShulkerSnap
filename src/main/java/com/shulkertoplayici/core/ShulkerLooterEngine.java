package com.shulkertoplayici.core;

import com.shulkertoplayici.config.ModConfig;
import com.shulkertoplayici.util.LanguageManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;

public class ShulkerLooterEngine {
    private static int lastProcessedSyncId = -1;
    private static long lastLootTime = 0;
    private static int fullInventoryStallCount = 0;

    public static void checkAndLoot() {
        ModConfig config = ModConfig.get();
        if (!config.enabled) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        ClientPlayerInteractionManager interactionManager = client.interactionManager;

        if (player == null || interactionManager == null) {
            return;
        }

        ScreenHandler handler = player.currentScreenHandler;
        if (handler == null) {
            lastProcessedSyncId = -1;
            fullInventoryStallCount = 0;
            return;
        }

        int containerSlotCount = 0;
        if (handler instanceof ShulkerBoxScreenHandler) {
            if (!config.lootShulkers) {
                return;
            }
            containerSlotCount = 27;
        } else if (handler instanceof GenericContainerScreenHandler genericHandler) {
            if (!config.lootChests) {
                return;
            }
            containerSlotCount = genericHandler.getRows() * 9;
        } else {
            return;
        }

        List<Integer> slotsWithItems = new ArrayList<>();
        int availableSlots = Math.min(containerSlotCount, handler.slots.size());
        for (int i = 0; i < availableSlots; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (stack != null && !stack.isEmpty()) {
                slotsWithItems.add(i);
            }
        }

        if (slotsWithItems.isEmpty()) {
            if (lastProcessedSyncId == handler.syncId) {
                if (config.autoClose) {
                    player.closeHandledScreen();
                    lastProcessedSyncId = -1;
                }
            }
            return;
        }

        long now = System.currentTimeMillis();
        if (config.delayMs > 0 && (now - lastLootTime) < config.delayMs) {
            return;
        }

        boolean hasPlayerSpace = false;
        int totalSlots = handler.slots.size();
        for (int pSlot = containerSlotCount; pSlot < totalSlots; pSlot++) {
            ItemStack pStack = handler.getSlot(pSlot).getStack();
            if (pStack == null || pStack.isEmpty()) {
                hasPlayerSpace = true;
                break;
            }
        }

        if (!hasPlayerSpace) {
            boolean canMerge = false;
            for (int s : slotsWithItems) {
                ItemStack cStack = handler.getSlot(s).getStack();
                if (cStack != null && !cStack.isEmpty() && cStack.isStackable()) {
                    for (int pSlot = containerSlotCount; pSlot < totalSlots; pSlot++) {
                        ItemStack pStack = handler.getSlot(pSlot).getStack();
                        if (pStack != null && ItemStack.canCombine(cStack, pStack) && pStack.getCount() < pStack.getMaxCount()) {
                            canMerge = true;
                            break;
                        }
                    }
                }
                if (canMerge) {
                    break;
                }
            }

            if (!canMerge) {
                fullInventoryStallCount++;
                if (fullInventoryStallCount > 3) {
                    if (config.chatNotifications && fullInventoryStallCount == 4) {
                        player.sendMessage(LanguageManager.get("message.shulker_toplayici.prefix")
                                .copy().append(LanguageManager.get("message.shulker_toplayici.inv_full")), true);
                    }
                    return;
                }
            }
        } else {
            fullInventoryStallCount = 0;
        }

        lastLootTime = now;
        lastProcessedSyncId = handler.syncId;

        if (config.delayMs <= 0) {
            int count = slotsWithItems.size();
            for (int slotId : slotsWithItems) {
                interactionManager.clickSlot(handler.syncId, slotId, 0, SlotActionType.QUICK_MOVE, player);
            }

            if (config.chatNotifications && count > 0) {
                player.sendMessage(LanguageManager.get("message.shulker_toplayici.prefix")
                        .copy().append(LanguageManager.get("message.shulker_toplayici.looted", count)), true);
            }

            if (config.autoClose) {
                player.closeHandledScreen();
                lastProcessedSyncId = -1;
            }
        } else {
            int targetSlot = slotsWithItems.get(0);
            interactionManager.clickSlot(handler.syncId, targetSlot, 0, SlotActionType.QUICK_MOVE, player);
        }
    }
}
