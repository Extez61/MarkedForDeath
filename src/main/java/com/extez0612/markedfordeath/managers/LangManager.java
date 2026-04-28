package com.extez0612.markedfordeath.managers;

import com.extez0612.markedfordeath.MarkedForDeath;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class LangManager {

    private final MarkedForDeath plugin;
    private FileConfiguration lang;

    public LangManager(MarkedForDeath plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        String language = plugin.getConfig().getString("language", "tr");
        File langFile = new File(plugin.getDataFolder(), "lang/" + language + ".yml");
        if (!langFile.exists()) {
            langFile = new File(plugin.getDataFolder(), "lang/en.yml");
        }
        lang = YamlConfiguration.loadConfiguration(langFile);
    }

    /** Prefix + message, color translated */
    public String get(String key) {
        String prefix = c(lang.getString("prefix", "&8[&cMFD&8] &r"));
        String msg    = c(lang.getString(key, "&cMissing: " + key));
        return prefix + msg;
    }

    /** No prefix */
    public String getRaw(String key) {
        return c(lang.getString(key, "&cMissing: " + key));
    }

    /** With replacements, with prefix */
    public String get(String key, String... kv) {
        String msg = get(key);
        for (int i = 0; i + 1 < kv.length; i += 2) msg = msg.replace(kv[i], kv[i + 1]);
        return msg;
    }

    /** With replacements, no prefix */
    public String getRaw(String key, String... kv) {
        String msg = getRaw(key);
        for (int i = 0; i + 1 < kv.length; i += 2) msg = msg.replace(kv[i], kv[i + 1]);
        return msg;
    }

    private String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}