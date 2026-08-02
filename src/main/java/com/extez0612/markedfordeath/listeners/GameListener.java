package com.extez0612.markedfordeath.listeners;

import com.extez0612.markedfordeath.MarkedForDeath;
import com.extez0612.markedfordeath.commands.KitEditCommand;
import com.extez0612.markedfordeath.managers.GameManager;
import com.extez0612.markedfordeath.utils.VersionUtil;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameListener implements Listener {

    private final MarkedForDeath plugin;
    private final GameManager    gm;
    @SuppressWarnings("unused")
    private final KitEditCommand kitEdit;

    /** Kısıtlama mesajlarının chat'i spamlamasını önlemek için oyuncu başına küçük bir cooldown. */
    private final Map<UUID, Long> lastRestrictionWarning = new HashMap<>();
    private static final long RESTRICTION_WARNING_COOLDOWN_MS = 2000L;

    public GameListener(MarkedForDeath plugin, KitEditCommand kitEdit) {
        this.plugin  = plugin;
        this.gm      = plugin.getGameManager();
        this.kitEdit = kitEdit;
    }

    // ── Runner ölümü ───────────────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player dead = e.getEntity();

        if (!gm.isGameRunning() && !gm.isWaitingForTouch()) return;
        Player runner = gm.getRunner();
        if (runner == null || !dead.equals(runner)) return;

        EntityDamageEvent cause = dead.getLastDamageCause();

        // Guardian runnerı öldürdüyse guardianlar kaybeder (taskDone = true)
        boolean killedByGuardian = isKilledByGuardian(cause);
        boolean taskDone = isCorrectCause(cause) || killedByGuardian;

        Location deathLoc = dead.getLocation().clone();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!dead.isOnline()) {
                    if (taskDone) gm.onRunnerTaskComplete();
                    else          gm.onRunnerDeath();
                    return;
                }

                dead.spigot().respawn();

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!gm.isGameRunning() && !gm.isWaitingForTouch()) return;

                        if (dead.isOnline()) {
                            VersionUtil.sendTitle(dead,
                                    plugin.getLangManager().getRaw("game.spectator-title"),
                                    plugin.getLangManager().getRaw("game.spectator-subtitle"),
                                    10, 60, 20);
                        }

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (dead.isOnline()) {
                                    dead.setGameMode(GameMode.SPECTATOR);
                                    dead.teleport(deathLoc);
                                }
                                if (taskDone) gm.onRunnerTaskComplete();
                                else          gm.onRunnerDeath();
                            }
                        }.runTaskLater(plugin, 10L);
                    }
                }.runTaskLater(plugin, 2L);
            }
        }.runTaskLater(plugin, 1L);
    }

    // ── KALDIRILDI: onEntityDamage (hasar engeli) ──────────────────────────
    // Guardian artık runnerı serbestçe öldürebilir.
    // Öldürürse guardian takımı kaybeder → isKilledByGuardian() ile tespit edilir.

    // ── WaitingForTouch: runner bir oyuncuya vurduğunda oyun başlar ────────
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player victim  = (Player) e.getEntity();
        Entity damager = e.getDamager();

        if (!gm.isWaitingForTouch() || gm.isGameStartedByTouch()) return;
        Player runner = gm.getRunner();
        if (runner == null) return;

        if (damager instanceof Player
                && damager.equals(runner)
                && gm.isPlayerInGame(victim.getUniqueId())) {
            gm.onRunnerTouched(victim);
        }
    }

    // ── Item drop engeli ───────────────────────────────────────────────────
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (!gm.isGameRunning() && !gm.isWaitingForTouch()) return;
        if (plugin.getConfig().getBoolean("game.allow-item-drop", true)) return;

        Player p = e.getPlayer();
        if (gm.isPlayerInGame(p.getUniqueId())) {
            e.setCancelled(true);
            p.sendMessage(plugin.getLangManager().get("game.no-drop"));
        }
    }

    // ── Dokunma öncesi eylem engeli ─────────────────────────────────────────
    // Runner birine vurup oyunu başlatana kadar (waitingForTouch && !startedByTouch)
    // hiçbir oyuncu: blok koyamaz, elindeki hiçbir eşyayla etkileşime giremez
    // (olta atma, ender pearl/iksir fırlatma, kova kullanma, yemek yeme, vb.)
    // ve hiçbir mermi/varlık fırlatamaz. game.restrict-actions-before-touch ile
    // kapatılabilir (varsayılan: true, yani engelli).

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!isPreTouchRestrictionActive()) return;

        Player p = e.getPlayer();
        if (!gm.isPlayerInGame(p.getUniqueId())) return;

        e.setCancelled(true);
        warn(p, "game.block-restricted");
    }

    /** Sağ tıkla yapılan her türlü eşya etkileşimi: olta atma, pearl/iksir fırlatma, kova, yemek, vb. */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!isPreTouchRestrictionActive()) return;

        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        if (!gm.isPlayerInGame(p.getUniqueId())) return;

        e.setCancelled(true);
        warn(p, "game.action-restricted");
    }

    /** Güvenlik ağı: pearl, iksir, olta kancası gibi her fırlatılan varlığı da engeller. */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (!isPreTouchRestrictionActive()) return;
        if (!(e.getEntity().getShooter() instanceof Player)) return;

        Player p = (Player) e.getEntity().getShooter();
        if (!gm.isPlayerInGame(p.getUniqueId())) return;

        e.setCancelled(true);
        warn(p, "game.action-restricted");
    }

    private boolean isPreTouchRestrictionActive() {
        if (!gm.isWaitingForTouch() || gm.isGameStartedByTouch()) return false;
        return plugin.getConfig().getBoolean("game.restrict-actions-before-touch", true);
    }

    /** Kısıtlama mesajını en fazla RESTRICTION_WARNING_COOLDOWN_MS'de bir gönderir (spam önleme). */
    private void warn(Player p, String langKey) {
        long now = System.currentTimeMillis();
        Long last = lastRestrictionWarning.get(p.getUniqueId());
        if (last != null && now - last < RESTRICTION_WARNING_COOLDOWN_MS) return;
        lastRestrictionWarning.put(p.getUniqueId(), now);
        p.sendMessage(plugin.getLangManager().get(langKey));
    }

    // ── Guardian öldürme tespiti ───────────────────────────────────────────
    /**
     * Runnerı doğrudan veya ok ile öldüren kişinin bir guardian olup olmadığını
     * kontrol eder. Runner ve Imposter dışındaki tüm oyuncular guardian sayılır.
     */
    private boolean isKilledByGuardian(EntityDamageEvent ev) {
        if (ev == null) return false;
        Player killer = null;

        if (ev instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) ev).getDamager();
            if (damager instanceof Player) {
                killer = (Player) damager;
            } else if (damager instanceof Projectile) {
                ProjectileSource shooter = ((Projectile) damager).getShooter();
                if (shooter instanceof Player) killer = (Player) shooter;
            }
        }
        if (killer == null) return false;

        UUID killerUUID   = killer.getUniqueId();
        Player runner     = gm.getRunner();
        Player imposter   = gm.getImposter();
        UUID runnerUUID   = runner   != null ? runner.getUniqueId()   : null;
        UUID imposterUUID = imposter != null ? imposter.getUniqueId() : null;

        // Oyun içindeki, runner ve imposter olmayan kişi = guardian
        return gm.isPlayerInGame(killerUUID)
                && !killerUUID.equals(runnerUUID)
                && !killerUUID.equals(imposterUUID);
    }

    // ── Görev eşleştirici ─────────────────────────────────────────────────
    private boolean isCorrectCause(EntityDamageEvent ev) {
        if (ev == null) return false;
        String key = gm.getCurrentTaskKey();
        if (key == null) return false;

        EntityDamageEvent.DamageCause dc = ev.getCause();

        switch (key) {
            case "lava-death":    return dc == EntityDamageEvent.DamageCause.LAVA;
            case "drowning":      return dc == EntityDamageEvent.DamageCause.DROWNING;
            case "magma-block":   return dc == EntityDamageEvent.DamageCause.HOT_FLOOR;
            case "suffocation":   return dc == EntityDamageEvent.DamageCause.SUFFOCATION;
            case "fall-damage":   return dc == EntityDamageEvent.DamageCause.FALL;
            case "anvil":         return dc == EntityDamageEvent.DamageCause.FALLING_BLOCK;
            case "lightning":     return dc == EntityDamageEvent.DamageCause.LIGHTNING;
            case "wither-effect": return dc == EntityDamageEvent.DamageCause.WITHER;

            case "cactus":
                return dc == EntityDamageEvent.DamageCause.CONTACT
                        || dc.name().equalsIgnoreCase("CACTUS");

            case "berry-bush":
                return dc == EntityDamageEvent.DamageCause.CONTACT;

            case "creeper":
                return dc == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                        && ev instanceof EntityDamageByEntityEvent
                        && ((EntityDamageByEntityEvent) ev).getDamager() instanceof Creeper;

            case "skeleton":   return isMobKill(ev, Skeleton.class)
                    || isProjectileFromMob(ev, Skeleton.class);
            case "zombie":     return isMobKill(ev, Zombie.class);
            case "spider":     return isMobKill(ev, Spider.class);
            case "iron-golem": return isMobKill(ev, IronGolem.class);

            default: return false;
        }
    }

    private boolean isMobKill(EntityDamageEvent ev, Class<? extends Entity> mobClass) {
        return ev.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && ev instanceof EntityDamageByEntityEvent
                && mobClass.isInstance(((EntityDamageByEntityEvent) ev).getDamager());
    }

    private boolean isProjectileFromMob(EntityDamageEvent ev, Class<? extends Entity> mobClass) {
        if (ev.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) return false;
        if (!(ev instanceof EntityDamageByEntityEvent)) return false;
        Entity damager = ((EntityDamageByEntityEvent) ev).getDamager();
        if (!(damager instanceof Projectile)) return false;
        ProjectileSource shooter = ((Projectile) damager).getShooter();
        return mobClass.isInstance(shooter);
    }
}