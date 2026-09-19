package com.shulkertoplayici.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shulkertoplayici.config.ModConfig;
import net.minecraft.text.Text;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private static final Gson GSON = new Gson();
    private static final Map<String, Map<String, String>> TRANSLATIONS = new HashMap<>();

    static {
        loadLang("en", "/assets/shulker_toplayici/lang/en_us.json");
        loadLang("tr", "/assets/shulker_toplayici/lang/tr_tr.json");
        loadLang("es", "/assets/shulker_toplayici/lang/es_es.json");
    }

    private static void loadLang(String code, String resourcePath) {
        try (InputStream in = LanguageManager.class.getResourceAsStream(resourcePath)) {
            if (in != null) {
                try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    Map<String, String> map = GSON.fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
                    if (map != null) {
                        TRANSLATIONS.put(code, map);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Text get(String key, Object... args) {
        String raw = getRaw(key, args);
        return Text.literal(raw);
    }

    public static String getRaw(String key, Object... args) {
        String currentLang = ModConfig.get().language;
        if (currentLang == null || currentLang.isEmpty() || currentLang.equalsIgnoreCase("auto")) {
            currentLang = "en";
        } else {
            currentLang = currentLang.toLowerCase();
        }

        Map<String, String> langMap = TRANSLATIONS.get(currentLang);
        if (langMap == null || !langMap.containsKey(key)) {
            langMap = TRANSLATIONS.get("en");
        }

        if (langMap != null && langMap.containsKey(key)) {
            String formatStr = langMap.get(key);
            try {
                if (args != null && args.length > 0) {
                    return String.format(formatStr, args);
                }
                return formatStr;
            } catch (Exception e) {
                return formatStr;
            }
        }

        return key;
    }
}
