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

import java.util.Arrays;

public class KitEditGUICommand implements CommandExecutor {

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

    /** Son açılan GUI'nin (dile göre üretilen) tam başlığı — listener eşleştirmesi için. */
    private String lastTitle;

    public KitEditGUICommand(MarkedForDeath plugin) {
        this.plugin    = plugin;
        this.lastTitle = buildTitle();
    }

    /**
     * GUI başlığını aktif dile göre üretir (lang dosyasındaki "gui.title" anahtarı).
     * Türkçe seçiliyken her zaman Türkçe, İngilizce seçiliyken her zaman İngilizce görünür.
     */
    private String buildTitle() {
        return ChatColor.DARK_RED + "" + ChatColor.BOLD
                + plugin.getLangManager().getRaw("gui.title");
    }

    /** Bu GUI'yi tanımlamak için kullanılan güncel başlık (listener bunu kullanır). */
    public String getGuiTitle() { return lastTitle; }

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
        // Dil her değişebileceği (reload) için başlığı her açılışta tazele.
        lastTitle = buildTitle();

        Inventory inv = Bukkit.createInventory(null, 27, lastTitle);
        KitEditCommand kitEdit = plugin.getKitEditCommand();

        // Tüm slotları cam dolgu ile kapat
        ItemStack filler = named(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), " ");
        for (int i = 0; i < 27; i++) inv.setItem(i, filler);

        String runnerName   = plugin.getLangManager().getRaw("gui.kit-runner");
        String guardianName = plugin.getLangManager().getRaw("gui.kit-guardian");
        String imposterName = plugin.getLangManager().getRaw("gui.kit-imposter");
        String leaveName    = ChatColor.RED + plugin.getLangManager().getRaw("gui.leave");

        // Runner  -> Oyuncu kafası (Steve)
        // Guardian -> Örümcek ağı
        // Imposter -> Demir kılıç
        inv.setItem(SLOT_RUNNER,   buildKitItem(p, kitEdit, "runner",   runnerName,   Material.PLAYER_HEAD));
        inv.setItem(SLOT_GUARDIAN, buildKitItem(p, kitEdit, "guardian", guardianName, Material.COBWEB));
        inv.setItem(SLOT_IMPOSTER, buildKitItem(p, kitEdit, "imposter", imposterName, Material.IRON_SWORD));
        inv.setItem(SLOT_LEAVE,    buildLeaveItem(leaveName, kitEdit.isEditing(p)));

        p.openInventory(inv);
    }

    // ── Item builder'ları ──────────────────────────────────────────────────

    /**
     * Tüm kit slotları verilen sabit material ile gösterilir.
     * Düzenlenen kit yeşil + lore ile, diğerleri sarı ile gösterilir.
     * Bariyer dönüşümü yalnızca tıklama anında (KitEditGUIListener) yapılır.
     */
    private ItemStack buildKitItem(Player p, KitEditCommand kitEdit,
                                   String kit, String displayName, Material material) {
        if (kitEdit.isEditing(p) && kit.equals(kitEdit.getEditingKit(p))) {
            // Şu an düzenlenen kit: yeşil renk + "editing" lore
            ItemStack item = named(new ItemStack(material), ChatColor.GREEN + displayName);
            addLore(item, ChatColor.YELLOW + plugin.getLangManager().getRaw("gui.editing-lore"));
            return item;
        }
        // Diğer kitler: normal sarı ikon — tıklamada bariyer olabilir (listener yapar)
        return named(new ItemStack(material), ChatColor.YELLOW + displayName);
    }

    private ItemStack buildLeaveItem(String displayName, boolean isEditing) {
        ItemStack item = named(new ItemStack(Material.RED_CONCRETE), displayName);
        if (!isEditing) {
            addLore(item, ChatColor.GRAY + plugin.getLangManager().getRaw("gui.not-editing-lore"));
        }
        return item;
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