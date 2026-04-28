package com.extez0612.markedfordeath.commands;

import com.extez0612.markedfordeath.MarkedForDeath;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

public class KitEditGUICommand implements CommandExecutor {

    /** Title used to identify our GUI inventory. */
    public static final String GUI_TITLE = ChatColor.DARK_RED + "" + ChatColor.BOLD + "Kit Editör";

    private static final String RUNNER_URL   = "https://s.namemc.com/i/191e70233757776d.png";
    private static final String GUARDIAN_URL = "https://s.namemc.com/i/d2e5986e5df342a6.png";
    private static final String IMPOSTER_URL = "https://s.namemc.com/i/b83566a78f523170.png";

    /*
     * ── 27-slot simetrik yerleşim (3 satır × 9 sütun) ───────────────────
     *
     *  Satır 0 (0-8)  : Tümü cam dolgu
     *  Satır 1 (9-17) : G G [Runner=11] G [Guardian=13] G [Imposter=15] G G
     *  Satır 2 (18-26): G G G G [Leave=22] G G G G
     */
    public static final int SLOT_RUNNER   = 11;
    public static final int SLOT_GUARDIAN = 13;
    public static final int SLOT_IMPOSTER = 15;
    public static final int SLOT_LEAVE    = 22;

    private final MarkedForDeath plugin;

    public KitEditGUICommand(MarkedForDeath plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangManager().get("commands.player-only"));
            return true;
        }
        Player p = (Player) sender;
        if (!p.hasPermission("markedfordeath.kitedit")) {
            p.sendMessage(plugin.getLangManager().get("commands.no-permission"));
            return true;
        }
        openGUI(p);
        return true;
    }

    // ── GUI builder ────────────────────────────────────────────────────────

    public void openGUI(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);
        KitEditCommand kitEdit = plugin.getKitEditCommand();

        // Tüm slotları cam dolgu ile kapat
        ItemStack filler = named(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), " ");
        for (int i = 0; i < 27; i++) inv.setItem(i, filler);

        String runnerName   = plugin.getLangManager().getRaw("gui.kit-runner");
        String guardianName = plugin.getLangManager().getRaw("gui.kit-guardian");
        String imposterName = plugin.getLangManager().getRaw("gui.kit-imposter");
        String leaveName    = ChatColor.RED + plugin.getLangManager().getRaw("gui.leave");

        inv.setItem(SLOT_RUNNER,   buildKitItem(p, kitEdit, "runner",   runnerName,   RUNNER_URL));
        inv.setItem(SLOT_GUARDIAN, buildKitItem(p, kitEdit, "guardian", guardianName, GUARDIAN_URL));
        inv.setItem(SLOT_IMPOSTER, buildKitItem(p, kitEdit, "imposter", imposterName, IMPOSTER_URL));
        inv.setItem(SLOT_LEAVE,    buildLeaveItem(leaveName, kitEdit.isEditing(p)));

        p.openInventory(inv);
    }

    // ── Item builder'ları ──────────────────────────────────────────────────

    /**
     * Tüm kit slotları her zaman kafa olarak gösterilir.
     * Düzenlenen kit yeşil + lore ile, diğerleri sarı ile gösterilir.
     * Bariyer dönüşümü yalnızca tıklama anında (KitEditGUIListener) yapılır.
     */
    private ItemStack buildKitItem(Player p, KitEditCommand kitEdit,
                                   String kit, String displayName, String textureUrl) {
        if (kitEdit.isEditing(p) && kit.equals(kitEdit.getEditingKit(p))) {
            // Şu an düzenlenen kit: yeşil renk + "editing" lore
            ItemStack skull = createSkull(ChatColor.GREEN + displayName, textureUrl);
            addLore(skull, ChatColor.YELLOW + plugin.getLangManager().getRaw("gui.editing-lore"));
            return skull;
        }
        // Diğer kitler: normal sarı kafa — tıklamada bariyer olabilir (listener yapar)
        return createSkull(ChatColor.YELLOW + displayName, textureUrl);
    }

    private ItemStack buildLeaveItem(String displayName, boolean isEditing) {
        ItemStack item = named(new ItemStack(Material.RED_CONCRETE), displayName);
        if (!isEditing) {
            addLore(item, ChatColor.GRAY + plugin.getLangManager().getRaw("gui.not-editing-lore"));
        }
        return item;
    }

    // ── Kafa (skull) oluşturucu ────────────────────────────────────────────

    private ItemStack createSkull(String displayName, String textureUrl) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta  = (SkullMeta) skull.getItemMeta();
        if (meta == null) return skull;

        String b64 = Base64.getEncoder().encodeToString(
                ("{\"textures\":{\"SKIN\":{\"url\":\"" + textureUrl + "\"}}}").getBytes(StandardCharsets.UTF_8)
        );

        try {
            Class<?> gpClass   = Class.forName("com.mojang.authlib.GameProfile");
            Class<?> propClass = Class.forName("com.mojang.authlib.properties.Property");

            Object profile    = gpClass.getConstructor(UUID.class, String.class)
                    .newInstance(UUID.randomUUID(), "mfd_kit");
            Object properties = gpClass.getMethod("getProperties").invoke(profile);
            Object property   = propClass.getConstructor(String.class, String.class)
                    .newInstance("textures", b64);

            properties.getClass().getMethod("put", Object.class, Object.class)
                    .invoke(properties, "textures", property);

            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Exception ignored) {
            // Doku uygulanamadı — düz kafa yine de kullanılabilir
        }

        meta.setDisplayName(displayName);
        skull.setItemMeta(meta);
        return skull;
    }

    // ── Yardımcılar ────────────────────────────────────────────────────────

    private ItemStack named(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); item.setItemMeta(meta); }
        return item;
    }

    private void addLore(ItemStack item, String... lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
    }
}