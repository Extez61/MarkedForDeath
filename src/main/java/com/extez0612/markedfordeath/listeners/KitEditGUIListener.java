package com.extez0612.markedfordeath.listeners;

import com.extez0612.markedfordeath.MarkedForDeath;
import com.extez0612.markedfordeath.commands.KitEditCommand;
import com.extez0612.markedfordeath.commands.KitEditGUICommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KitEditGUIListener implements Listener {

    private final MarkedForDeath plugin;

    public KitEditGUIListener(MarkedForDeath plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!KitEditGUICommand.GUI_TITLE.equals(e.getView().getTitle())) return;

        e.setCancelled(true);

        Player         p       = (Player) e.getWhoClicked();
        KitEditCommand kitEdit = plugin.getKitEditCommand();
        int            slot    = e.getRawSlot();

        // Hangi kit slotuna tıklandığını bul
        String targetKit = null;
        if      (slot == KitEditGUICommand.SLOT_RUNNER)   targetKit = "runner";
        else if (slot == KitEditGUICommand.SLOT_GUARDIAN) targetKit = "guardian";
        else if (slot == KitEditGUICommand.SLOT_IMPOSTER) targetKit = "imposter";
        else if (slot == KitEditGUICommand.SLOT_LEAVE) {
            p.closeInventory();
            kitEdit.leaveEditMode(p);
            return;
        }

        // Cam dolgu veya başka slot — işlem yok
        if (targetKit == null) return;

        // Kendi düzenlediği kitten farklı bir kite geçmeye çalışıyor
        if (kitEdit.isEditing(p) && !targetKit.equals(kitEdit.getEditingKit(p))) {
            String barrierName = ChatColor.RED
                    + plugin.getLangManager().getRaw("gui.switch-blocked-name");
            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemMeta  meta    = barrier.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(barrierName);
                barrier.setItemMeta(meta);
            }
            e.getInventory().setItem(slot, barrier);
            p.sendMessage(plugin.getLangManager().get(
                    "commands.kitedit-switch-blocked", "{kit}", kitEdit.getEditingKit(p)));
            playNo(p);
            return;
        }

        // Aynı kit başka biri tarafından düzenleniyorsa engelle
        String editorName = kitEdit.getEditorNameForKit(targetKit);
        if (editorName != null && !kitEdit.isEditing(p)) {
            p.sendMessage(plugin.getLangManager().get(
                    "commands.kitedit-busy", "{player}", editorName));
            playNo(p);
            return;
        }

        // Normal giriş — GUI kapat, edit moduna gir
        p.closeInventory();
        kitEdit.enterEditMode(p, targetKit);
    }

    // ── Yardımcı ──────────────────────────────────────────────────────────

    private void playNo(Player p) {
        try {
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        } catch (Exception ignored) {}
    }
}