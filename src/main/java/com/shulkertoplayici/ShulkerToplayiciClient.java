package com.shulkertoplayici;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.shulkertoplayici.config.ModConfig;
import com.shulkertoplayici.util.LanguageManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShulkerToplayiciClient implements ClientModInitializer {
    public static final String MOD_ID = "shulker_toplayici";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyBinding toggleKeyBinding;
    public static volatile boolean isListeningForKeybind = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[ShulkerSnap] Loading mod...");

        ModConfig config = ModConfig.load();

        int initialKey = config.keyCode != 0 ? config.keyCode : GLFW.GLFW_KEY_J;
        toggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shulker_toplayici.toggle",
                InputUtil.Type.KEYSYM,
                initialKey,
                "key.shulker_toplayici.category"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isListeningForKeybind) {
                while (toggleKeyBinding.wasPressed()) {
                    ModConfig cfg = ModConfig.get();
                    cfg.enabled = !cfg.enabled;
                    cfg.save();

                    if (client.player != null) {
                        Text stateMsg = cfg.enabled
                                ? LanguageManager.get("message.shulker_toplayici.enabled")
                                : LanguageManager.get("message.shulker_toplayici.disabled");

                        client.player.sendMessage(LanguageManager.get("message.shulker_toplayici.prefix").copy().append(stateMsg), true);
                    }
                }
            }

            com.shulkertoplayici.core.ShulkerLooterEngine.checkAndLoot();
        });

        registerCommands();

        LOGGER.info("[ShulkerSnap] Successfully initialized! Supported versions: 1.20 - 1.21.x");
    }

    public static boolean handleKeyInput(int key, int scancode) {
        if (!isListeningForKeybind) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            isListeningForKeybind = false;
            if (client.player != null) {
                client.player.sendMessage(LanguageManager.get("message.shulker_toplayici.prefix")
                        .copy().append(LanguageManager.get("message.shulker_toplayici.keybind_cancelled")), false);
            }
            return true;
        }

        isListeningForKeybind = false;
        InputUtil.Key inputKey = InputUtil.fromKeyCode(key, scancode);
        String name = inputKey.getLocalizedText().getString();

        ModConfig config = ModConfig.get();
        config.keyCode = key;
        config.keyName = name;
        config.save();

        if (toggleKeyBinding != null) {
            toggleKeyBinding.setBoundKey(inputKey);
            KeyBinding.updateKeysByCode();
        }

        if (client.player != null) {
            client.player.sendMessage(LanguageManager.get("message.shulker_toplayici.prefix")
                    .copy().append(LanguageManager.get("message.shulker_toplayici.keybind_set", name)), false);
        }

        return true;
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("shulker")
                    .then(ClientCommandManager.literal("toggle")
                            .executes(context -> {
                                ModConfig config = ModConfig.get();
                                config.enabled = !config.enabled;
                                config.save();

                                Text stateMsg = config.enabled
                                        ? LanguageManager.get("message.shulker_toplayici.enabled")
                                        : LanguageManager.get("message.shulker_toplayici.disabled");

                                context.getSource().sendFeedback(LanguageManager.get("message.shulker_toplayici.prefix").copy().append(stateMsg));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("keybind")
                            .executes(context -> {
                                isListeningForKeybind = true;
                                context.getSource().sendFeedback(LanguageManager.get("message.shulker_toplayici.prefix")
                                        .copy().append(LanguageManager.get("message.shulker_toplayici.keybind_listening")));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("keybinds")
                            .executes(context -> {
                                isListeningForKeybind = true;
                                context.getSource().sendFeedback(LanguageManager.get("message.shulker_toplayici.prefix")
                                        .copy().append(LanguageManager.get("message.shulker_toplayici.keybind_listening")));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("bind")
                            .executes(context -> {
                                isListeningForKeybind = true;
                                context.getSource().sendFeedback(LanguageManager.get("message.shulker_toplayici.prefix")
                                        .copy().append(LanguageManager.get("message.shulker_toplayici.keybind_listening")));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("delay")
                            .then(ClientCommandManager.argument("ms", IntegerArgumentType.integer(0, 5000))
                                    .executes(context -> {
                                        int ms = IntegerArgumentType.getInteger(context, "ms");
                                        ModConfig config = ModConfig.get();
                                        config.delayMs = ms;
                                        config.save();

                                        context.getSource().sendFeedback(LanguageManager.get("message.shulker_toplayici.prefix")
                                                .copy().append(LanguageManager.get("message.shulker_toplayici.delay_set", ms)));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("autoclose")
                            .executes(context -> {
                                ModConfig config = ModConfig.get();
                                config.autoClose = !config.autoClose;
                                config.save();

                                Text msg = config.autoClose
                                        ? LanguageManager.get("message.shulker_toplayici.autoclose_on")
                                        : LanguageManager.get("message.shulker_toplayici.autoclose_off");

                                context.getSource().sendFeedback(LanguageManager.get("message.shulker_toplayici.prefix").copy().append(msg));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("chests")
                            .executes(context -> {
                                ModConfig config = ModConfig.get();
                                config.lootChests = !config.lootChests;
                                config.save();

                                Text msg = config.lootChests
                                        ? LanguageManager.get("message.shulker_toplayici.chests_on")
                                        : LanguageManager.get("message.shulker_toplayici.chests_off");

                                context.getSource().sendFeedback(LanguageManager.get("message.shulker_toplayici.prefix").copy().append(msg));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("shulkers")
                            .executes(context -> {
                                ModConfig config = ModConfig.get();
                                config.lootShulkers = !config.lootShulkers;
                                config.save();

                                Text msg = config.lootShulkers
                                        ? LanguageManager.get("message.shulker_toplayici.shulkers_on")
                                        : LanguageManager.get("message.shulker_toplayici.shulkers_off");

                                context.getSource().sendFeedback(LanguageManager.get("message.shulker_toplayici.prefix").copy().append(msg));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("lang")
                            .then(ClientCommandManager.argument("language", StringArgumentType.word())
                                    .executes(context -> {
                                        String rawLang = StringArgumentType.getString(context, "language").toLowerCase();
                                        String selected = "en";
                                        if (rawLang.startsWith("tr")) {
                                            selected = "tr";
                                        } else if (rawLang.startsWith("es")) {
                                            selected = "es";
                                        } else if (rawLang.startsWith("en")) {
                                            selected = "en";
                                        }

                                        ModConfig config = ModConfig.get();
                                        config.language = selected;
                                        config.save();

                                        context.getSource().sendFeedback(LanguageManager.get("message.shulker_toplayici.prefix")
                                                .copy().append(LanguageManager.get("message.shulker_toplayici.lang_set")));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("language")
                            .then(ClientCommandManager.argument("language", StringArgumentType.word())
                                    .executes(context -> {
                                        String rawLang = StringArgumentType.getString(context, "language").toLowerCase();
                                        String selected = "en";
                                        if (rawLang.startsWith("tr")) {
                                            selected = "tr";
                                        } else if (rawLang.startsWith("es")) {
                                            selected = "es";
                                        } else if (rawLang.startsWith("en")) {
                                            selected = "en";
                                        }

                                        ModConfig config = ModConfig.get();
                                        config.language = selected;
                                        config.save();

                                        context.getSource().sendFeedback(LanguageManager.get("message.shulker_toplayici.prefix")
                                                .copy().append(LanguageManager.get("message.shulker_toplayici.lang_set")));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("status")
                            .executes(context -> {
                                ModConfig config = ModConfig.get();
                                String on = LanguageManager.getRaw("message.shulker_toplayici.state_on");
                                String off = LanguageManager.getRaw("message.shulker_toplayici.state_off");

                                StringBuilder sb = new StringBuilder();
                                sb.append(LanguageManager.getRaw("message.shulker_toplayici.status_header")).append("\n");
                                sb.append(LanguageManager.getRaw("message.shulker_toplayici.status_overall", config.enabled ? on : off)).append("\n");
                                sb.append(LanguageManager.getRaw("message.shulker_toplayici.status_keybind", config.keyName)).append("\n");
                                sb.append(LanguageManager.getRaw("message.shulker_toplayici.status_delay", config.delayMs)).append("\n");
                                sb.append(LanguageManager.getRaw("message.shulker_toplayici.status_autoclose", config.autoClose ? on : off)).append("\n");
                                sb.append(LanguageManager.getRaw("message.shulker_toplayici.status_shulkers", config.lootShulkers ? on : off)).append("\n");
                                sb.append(LanguageManager.getRaw("message.shulker_toplayici.status_chests", config.lootChests ? on : off)).append("\n");
                                sb.append(LanguageManager.getRaw("message.shulker_toplayici.status_lang", config.language.toUpperCase()));

                                context.getSource().sendFeedback(Text.literal(sb.toString()));
                                return 1;
                            }))
            );
        });
    }
}
