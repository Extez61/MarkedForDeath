package com.extez0612.markedfordeath.managers;

import com.extez0612.markedfordeath.MarkedForDeath;
import com.extez0612.markedfordeath.utils.VersionUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;

public class KitManager {

    private final MarkedForDeath plugin;

    public KitManager(MarkedForDeath plugin) {
        this.plugin = plugin;
    }

    /** kitType: "runner" | "guardian" | "imposter" */
    public Map<Integer, ItemStack> getKit(String kitType) {
        Map<Integer, ItemStack> result = new HashMap<>();
        FileConfiguration kits = plugin.getKitsConfig();
        // Path in kits.yml: <kitType>.items  (no 'kits.' prefix)
        ConfigurationSection section = kits.getConfigurationSection(kitType + ".items");
        if (section == null) return result;

        for (String slotStr : section.getKeys(false)) {
            int slot;
            try { slot = Integer.parseInt(slotStr); }
            catch (NumberFormatException e) { continue; }

            ConfigurationSection is = section.getConfigurationSection(slotStr);
            if (is == null) continue;

            Material mat = Material.matchMaterial(is.getString("material", "AIR"));
            if (mat == null || mat == Material.AIR) continue;

            ItemStack item = new ItemStack(mat, is.getInt("amount", 1));
            ItemMeta  meta = item.getItemMeta();

            if (meta != null) {
                if (is.getBoolean("unbreakable", false)) VersionUtil.setUnbreakable(meta);

                if (meta instanceof PotionMeta && is.contains("potion-type")) {
                    try {
                        PotionType pt = PotionType.valueOf(
                                is.getString("potion-type", "WATER").toUpperCase());
                        applyPotionType((PotionMeta) meta, pt,
                                is.getBoolean("extended", false),
                                is.getBoolean("upgraded", false));
                    } catch (Exception ignored) {}
                }
                item.setItemMeta(meta);
            }
            result.put(slot, item);
        }
        return result;
    }

    public void saveKit(String kitType, PlayerInventory inv) {
        FileConfiguration kits = plugin.getKitsConfig();
        // Path in kits.yml: <kitType>.items  (no 'kits.' prefix)
        String path = kitType + ".items";
        kits.set(path, null);

        for (int i = 0; i < 36; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;

            kits.set(path + "." + i + ".material", item.getType().name());
            kits.set(path + "." + i + ".amount",   item.getAmount());

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            if (VersionUtil.isUnbreakable(meta))
                kits.set(path + "." + i + ".unbreakable", true);

            if (meta instanceof PotionMeta) {
                readPotionType(kits, path + "." + i, (PotionMeta) meta);
            }
        }
        plugin.saveKitsConfig();
    }

    // ── Potion helper'ları ─────────────────────────────────────────────────
    // PotionData (setBasePotionData/getBasePotionData) 1.20.5+ itibarıyla
    // deprecated ve kaldırılmaya işaretli. Yeni sunucularda setBasePotionType
    // kullanılmalı; VersionUtil.isNewPotionApi() ile eski/yeni API arasında
    // reflection sonucuna göre seçim yapılıyor, böylece derleme sırasında
    // deprecated API'ye doğrudan referans verilmiyor (warning oluşmuyor)
    // ve eski sürümlerle geriye dönük uyumluluk korunuyor.

    @SuppressWarnings({"deprecation", "removal"})
    private void applyPotionType(PotionMeta meta, PotionType pt,
                                 boolean extended, boolean upgraded) {
        if (VersionUtil.isNewPotionApi()) {
            meta.setBasePotionType(pt);
        } else {
            meta.setBasePotionData(new PotionData(pt, extended, upgraded));
        }
    }

    @SuppressWarnings({"deprecation", "removal"})
    private void readPotionType(FileConfiguration kits, String path, PotionMeta meta) {
        if (VersionUtil.isNewPotionApi()) {
            PotionType pt = meta.getBasePotionType();
            if (pt != null) {
                kits.set(path + ".potion-type", pt.name());
                kits.set(path + ".extended",    false);
                kits.set(path + ".upgraded",    false);
            }
        } else {
            PotionData pd = meta.getBasePotionData();
            kits.set(path + ".potion-type", pd.getType().name());
            kits.set(path + ".extended",    pd.isExtended());
            kits.set(path + ".upgraded",    pd.isUpgraded());
        }
    }
}