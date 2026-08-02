package com.extez0612.markedfordeath.listeners;

import com.extez0612.markedfordeath.MarkedForDeath;
import com.extez0612.markedfordeath.managers.GameManager;
import com.extez0612.markedfordeath.managers.UpdateChecker;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class PlayerJoinListener implements Listener {

    /** Güncelleme bildirimi, oyuncu girdikten kaç tick sonra gösterilsin (4.5s = 90 tick). */
    private static final long UPDATE_NOTICE_DELAY_TICKS = 90L;

    private final MarkedForDeath plugin;
    private final GameManager    gm;

    public PlayerJoinListener(MarkedForDeath plugin) {
        this.plugin = plugin;
        this.gm     = plugin.getGameManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        boolean isAdmin = p.isOp() || p.hasPermission("markedfordeath.admin");

        // ── Compass hint: her girişte gösterilir (kalıcı takip yok) ──────
        if (isAdmin && !hasCompassPlugin()) {
            p.sendMessage(plugin.getLangManager().getRaw("compass-hint"));
        }

        // ── Update notice: yeni versiyon varsa, girişten 4.5sn sonra gösterilir ──
        if (isAdmin
                && plugin.getConfig().getBoolean("update-checker.enabled", true)
                && plugin.getUpdateChecker().isUpdateAvailable()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (p.isOnline()) sendUpdateNotice(p);
            }, UPDATE_NOTICE_DELAY_TICKS);
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

    /** OP/admin oyuncuya yeni versiyon bilgisini ve tıklanabilir indirme linkini gönderir. */
    private void sendUpdateNotice(Player p) {
        p.sendMessage(plugin.getLangManager().get("update-available",
                "{current}", plugin.getDescription().getVersion(),
                "{latest}",  plugin.getUpdateChecker().getLatestVersion()));

        TextComponent link = new TextComponent(ChatColor.translateAlternateColorCodes('&',
                plugin.getLangManager().getRaw("update-download")));
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, UpdateChecker.getDownloadUrl()));
        link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(UpdateChecker.getDownloadUrl()).create()));
        p.spigot().sendMessage(link);
    }

    private boolean hasCompassPlugin() {
        for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
            if (pl.getName().toLowerCase().contains("compass")) return true;
        }
        return false;
    }
}