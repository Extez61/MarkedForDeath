package com.extez0612.markedfordeath.listeners;

import com.extez0612.markedfordeath.MarkedForDeath;
import com.extez0612.markedfordeath.commands.KitEditCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerQuitEvent;

public class KitEditListener implements Listener {

    private final MarkedForDeath plugin;
    private final KitEditCommand kitEdit;

    public KitEditListener(MarkedForDeath plugin, KitEditCommand kitEdit) {
        this.plugin  = plugin;
        this.kitEdit = kitEdit;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (!kitEdit.isEditing(p)) return;

        /**
         * FIX: Eski kod saveKit() + restoreInventory() yapıyordu.
         *
         * restoreInventory() disconnect olan oyuncu için tamamen anlamsız:
         *   1. Oyuncu sunucudan ayrılıyor — envanter değişikliği kaybolacak
         *   2. GameMode değişikliği kaybolacak
         *   3. Gereksiz I/O ve Map manipülasyonu
         *
         * forceLeave():
         *   - Kiti kaydeder (önemli)
         *   - editing Map'ten çıkarır (memory leak önler)
         *   - savedInventory entry'sini temizler (memory leak önler)
         *   - restoreInventory ÇAĞIRMAZ (gereksiz)
         */
        kitEdit.forceLeave(p);
    }
}