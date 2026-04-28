package com.extez0612.markedfordeath.listeners;

import com.extez0612.markedfordeath.MarkedForDeath;
import com.extez0612.markedfordeath.managers.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class PlayerJoinListener implements Listener {

    private final MarkedForDeath plugin;
    private final GameManager    gm;

    public PlayerJoinListener(MarkedForDeath plugin) {
        this.plugin = plugin;
        this.gm     = plugin.getGameManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // ── Compass hint: shown only once ever per player (saved to notified.yml) ──
        if ((p.isOp() || p.hasPermission("markedfordeath.admin"))
                && !plugin.isNotified(p.getUniqueId())
                && !hasCompassPlugin()) {
            p.sendMessage(plugin.getLangManager().getRaw("compass-hint"));
            plugin.markNotified(p.getUniqueId());
        }

        // ── Game in progress: put late-joiners into spectator ─────────────
        if (gm.isGameRunning() || gm.isWaitingForTouch()) {
            if (!gm.isPlayerInGame(p.getUniqueId())) {
                gm.getSpectators().add(p.getUniqueId());
                p.setGameMode(GameMode.SPECTATOR);
                p.sendMessage(plugin.getLangManager().get("game.new-player-spectator"));
            }
        }
    }

    private boolean hasCompassPlugin() {
        for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
            if (pl.getName().toLowerCase().contains("compass")) return true;
        }
        return false;
    }
}