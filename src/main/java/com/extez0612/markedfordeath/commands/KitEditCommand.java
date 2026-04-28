package com.extez0612.markedfordeath.commands;

import com.extez0612.markedfordeath.MarkedForDeath;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class KitEditCommand implements CommandExecutor, TabCompleter {

    private final MarkedForDeath plugin;
    private final Map<UUID, String> editing = new HashMap<>();

    public KitEditCommand(MarkedForDeath plugin) { this.plugin = plugin; }

    public boolean isEditing(Player p)     { return editing.containsKey(p.getUniqueId()); }
    public String  getEditingKit(Player p) { return editing.get(p.getUniqueId()); }

    public void clearAllEditing() {
        for (Map.Entry<UUID, String> entry : new HashMap<>(editing).entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p != null && p.isOnline()) {
                plugin.getKitManager().saveKit(entry.getValue(), p.getInventory());
            }
        }
        editing.clear();
    }

    public void forceLeave(Player p) {
        String kit = editing.remove(p.getUniqueId());
        if (kit != null) {
            plugin.getKitManager().saveKit(kit, p.getInventory());
            plugin.getGameManager().clearSavedInventory(p);
        }
    }

    // ── Public API (used by GUI) ───────────────────────────────────────────

    /**
     * Herhangi bir kiti düzenleyen ilk oyuncunun adını döner.
     * Çevrimdışı oyuncuları temizler.
     */
    public String getEditorName() {
        for (Map.Entry<UUID, String> entry : new HashMap<>(editing).entrySet()) {
            Player editor = Bukkit.getPlayer(entry.getKey());
            if (editor == null || !editor.isOnline()) {
                editing.remove(entry.getKey());
                continue;
            }
            return editor.getName();
        }
        return null;
    }

    /**
     * Belirli bir kiti düzenleyen oyuncunun adını döner; yoksa null.
     * Çevrimdışı oyuncuları temizler.
     */
    public String getEditorNameForKit(String kitType) {
        for (Map.Entry<UUID, String> entry : new HashMap<>(editing).entrySet()) {
            if (!kitType.equals(entry.getValue())) continue;
            Player editor = Bukkit.getPlayer(entry.getKey());
            if (editor == null || !editor.isOnline()) {
                editing.remove(entry.getKey());
                continue;
            }
            return editor.getName();
        }
        return null;
    }

    public void enterEditMode(Player p, String kitType) {
        if (editing.containsKey(p.getUniqueId())) {
            String current = editing.get(p.getUniqueId());
            if (current.equals(kitType)) {
                p.sendMessage(plugin.getLangManager().get(
                        "commands.kitedit-already-editing", "{kit}", current));
            } else {
                p.sendMessage(plugin.getLangManager().get(
                        "commands.kitedit-switch-blocked", "{kit}", current));
            }
            return;
        }

        // Sadece aynı kit başkası tarafından düzenleniyorsa engelle
        String editorName = getEditorNameForKit(kitType);
        if (editorName != null) {
            p.sendMessage(plugin.getLangManager().get(
                    "commands.kitedit-busy", "{player}", editorName));
            return;
        }

        plugin.getGameManager().saveInventory(p);
        editing.put(p.getUniqueId(), kitType);

        p.getInventory().clear();
        Map<Integer, ItemStack> kit = plugin.getKitManager().getKit(kitType);
        for (Map.Entry<Integer, ItemStack> e : kit.entrySet()) {
            if (e.getKey() >= 0 && e.getKey() < 36) {
                p.getInventory().setItem(e.getKey(), e.getValue().clone());
            }
        }

        p.setGameMode(GameMode.CREATIVE);
        p.updateInventory();
        p.sendMessage(plugin.getLangManager().get(
                "commands.kitedit-enter", "{kit}", kitType));
    }

    /**
     * Oyuncuyu kit düzenleme modundan çıkarır, kiti kaydeder ve envanteri geri yükler.
     */
    public void leaveEditMode(Player p) {
        if (!editing.containsKey(p.getUniqueId())) {
            p.sendMessage(plugin.getLangManager().get("commands.kitedit-not-editing"));
            return;
        }
        String kit = editing.remove(p.getUniqueId());
        plugin.getKitManager().saveKit(kit, p.getInventory());
        plugin.getGameManager().restoreInventory(p);
        p.sendMessage(plugin.getLangManager().get("commands.kitedit-leave"));
    }

    // ── Command handler ────────────────────────────────────────────────────

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangManager().get("commands.player-only"));
            return true;
        }
        Player p = (Player) sender;

        if (!p.hasPermission("markedfordeath.kitedit")) {
            p.sendMessage(plugin.getLangManager().get("commands.no-permission"));
            return true;
        }
        if (args.length == 0) {
            p.sendMessage(plugin.getLangManager().get("commands.kitedit-usage"));
            return true;
        }

        // ── Leave ──────────────────────────────────────────────────────────
        if (args[0].equalsIgnoreCase("leave")) {
            leaveEditMode(p);
            return true;
        }

        // ── Kit tipini çöz ─────────────────────────────────────────────────
        final String kitType;
        switch (args[0].toLowerCase()) {
            case "runner":              kitType = "runner";   break;
            case "guardians":
            case "guardian":            kitType = "guardian"; break;
            case "imposter":            kitType = "imposter"; break;
            default:
                p.sendMessage(plugin.getLangManager().get("commands.kitedit-usage"));
                return true;
        }

        enterEditMode(p, kitType);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd,
                                      String alias, String[] args) {
        if (!(sender instanceof Player)
                || !sender.hasPermission("markedfordeath.kitedit")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String q = args[0].toLowerCase();
            List<String> list = new ArrayList<>(Arrays.asList(
                    "runner", "guardians", "imposter", "leave"));
            list.removeIf(s -> !s.startsWith(q));
            return list;
        }
        return Collections.emptyList();
    }
}