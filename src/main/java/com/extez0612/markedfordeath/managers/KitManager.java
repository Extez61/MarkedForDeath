package com.extez0612.markedfordeath.managers;

import com.extez0612.markedfordeath.MarkedForDeath;
import com.extez0612.markedfordeath.utils.VersionUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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
        ConfigurationSection section =
                plugin.getConfig().getConfigurationSection("kits." + kitType + ".items");
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
                        ((PotionMeta) meta).setBasePotionData(
                                new PotionData(pt,
                                        is.getBoolean("extended", false),
                                        is.getBoolean("upgraded", false)));
                    } catch (Exception ignored) {}
                }
                item.setItemMeta(meta);
            }
            result.put(slot, item);
        }
        return result;
    }

    public void saveKit(String kitType, PlayerInventory inv) {
        String path = "kits." + kitType + ".items";
        plugin.getConfig().set(path, null);

        for (int i = 0; i < 36; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;

            plugin.getConfig().set(path + "." + i + ".material", item.getType().name());
            plugin.getConfig().set(path + "." + i + ".amount",   item.getAmount());

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            if (VersionUtil.isUnbreakable(meta))
                plugin.getConfig().set(path + "." + i + ".unbreakable", true);

            if (meta instanceof PotionMeta) {
                PotionData pd = ((PotionMeta) meta).getBasePotionData();
                plugin.getConfig().set(path + "." + i + ".potion-type", pd.getType().name());
                plugin.getConfig().set(path + "." + i + ".extended",    pd.isExtended());
                plugin.getConfig().set(path + "." + i + ".upgraded",    pd.isUpgraded());
            }
        }
        plugin.saveConfig();
    }
}