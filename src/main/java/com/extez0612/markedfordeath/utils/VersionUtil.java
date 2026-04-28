package com.extez0612.markedfordeath.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class VersionUtil {

    private static final boolean NEW_POTION_API;

    static {
        boolean newApi;
        try {
            PotionMeta.class.getMethod("setBasePotionType", PotionType.class);
            newApi = true;
        } catch (NoSuchMethodException e) {
            newApi = false;
        }
        NEW_POTION_API = newApi;
    }

    public static boolean isNewPotionApi() {
        return NEW_POTION_API;
    }

    // ── Item meta ──────────────────────────────────────────────────────────

    public static void setUnbreakable(ItemMeta meta) {
        try {
            meta.setUnbreakable(true);
        } catch (Exception ignored) {
        }
    }

    public static boolean isUnbreakable(ItemMeta meta) {
        try {
            return meta.isUnbreakable();
        } catch (Exception e) {
            return false;
        }
    }

    // ── Player UI ──────────────────────────────────────────────────────────

    public static void sendActionBar(Player player, String message) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(message));
        } catch (Exception ignored) {
        }
    }

    public static void sendTitle(Player player, String title, String subtitle,
                                 int fadeIn, int stay, int fadeOut) {
        try {
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        } catch (Exception ignored) {
        }
    }
}