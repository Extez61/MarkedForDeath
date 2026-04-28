package com.extez0612.markedfordeath.managers;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.extez0612.markedfordeath.MarkedForDeath;
import com.extez0612.markedfordeath.utils.VersionUtil;

import java.util.*;

public class GameManager {

    private final MarkedForDeath plugin;

    private boolean gameRunning     = false;
    private boolean waitingForTouch = false;
    private boolean startedByTouch  = false;
    private boolean endingGame      = false;

    private UUID runnerUUID   = null;
    private UUID imposterUUID = null;
    private final Set<UUID> allPlayerUUIDs = new LinkedHashSet<>();
    private final Set<UUID> guardianUUIDs  = new LinkedHashSet<>();

    private String currentTaskKey     = null;
    private String currentTaskDisplay = null;
    private int    timeLeft           = 0;
    private long   startTime          = 0;

    private BukkitTask timerTask     = null;
    private BukkitTask actionBarTask = null;

    private final Set<UUID>              spectators       = new HashSet<>();
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, GameMode>    savedGameModes   = new HashMap<>();

    private long lastReminderTime = 0L;

    // ── keepInventory yönetimi ─────────────────────────────────────────────
    /** Oyun başlamadan önceki keepInventory değeri (geri yüklemek için). */
    private final Map<String, Boolean> originalKeepInventory = new HashMap<>();

    public GameManager(MarkedForDeath plugin) { this.plugin = plugin; }

    // ── Accessors ──────────────────────────────────────────────────────────

    public boolean isGameRunning()        { return gameRunning; }
    public boolean isWaitingForTouch()    { return waitingForTouch; }
    public boolean isGameStartedByTouch() { return startedByTouch; }

    public Player getRunner() {
        return runnerUUID != null ? Bukkit.getPlayer(runnerUUID) : null;
    }

    public Player getImposter() {
        return imposterUUID != null ? Bukkit.getPlayer(imposterUUID) : null;
    }

    public List<Player> getAllPlayers() {
        List<Player> result = new ArrayList<>(allPlayerUUIDs.size());
        for (UUID uuid : allPlayerUUIDs) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) result.add(p);
        }
        return result;
    }

    public List<Player> getGuardians() {
        List<Player> result = new ArrayList<>(guardianUUIDs.size());
        for (UUID uuid : guardianUUIDs) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) result.add(p);
        }
        return result;
    }

    public Set<UUID>    getSpectators()           { return spectators; }
    public String       getCurrentTask()          { return currentTaskDisplay; }
    public String       getCurrentTaskKey()       { return currentTaskKey; }
    public int          getTimeLeft()             { return timeLeft; }
    public boolean      isSpectator(Player p)     { return spectators.contains(p.getUniqueId()); }
    public boolean      isPlayerInGame(UUID uuid) { return allPlayerUUIDs.contains(uuid); }

    public void setRunner(Player p) {
        this.runnerUUID = p.getUniqueId();
        broadcast(plugin.getLangManager().get("game.runner-selected",
                "{player}", p.getName()));
    }

    private void clearPotionEffects(Player p) {
        for (PotionEffect effect : new ArrayList<>(p.getActivePotionEffects())) {
            p.removePotionEffect(effect.getType());
        }
    }

    // ── keepInventory yardımcıları ─────────────────────────────────────────

    /**
     * Config'deki keep-inventory değerini tüm dünyalara uygular;
     * mevcut değerleri saklar.
     */
    private void applyKeepInventory() {
        boolean keepInv = plugin.getConfig().getBoolean("game.keep-inventory", true);
        originalKeepInventory.clear();
        for (World world : Bukkit.getWorlds()) {
            Boolean current = world.getGameRuleValue(GameRule.KEEP_INVENTORY);
            originalKeepInventory.put(world.getName(), current != null && current);
            world.setGameRule(GameRule.KEEP_INVENTORY, keepInv);
        }
    }

    /** Oyun öncesi keepInventory değerlerini geri yükler. */
    private void restoreKeepInventory() {
        for (World world : Bukkit.getWorlds()) {
            Boolean original = originalKeepInventory.get(world.getName());
            if (original != null) {
                world.setGameRule(GameRule.KEEP_INVENTORY, original);
            }
        }
        originalKeepInventory.clear();
    }

    // ── Start ──────────────────────────────────────────────────────────────
    public boolean startGame(Player sender) {
        if (gameRunning || waitingForTouch) {
            sender.sendMessage(plugin.getLangManager().get("game.already-running"));
            return false;
        }

        int minPlayers = plugin.getConfig().getInt("game.min-players", 3);
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());

        if (online.size() < minPlayers) {
            sender.sendMessage(plugin.getLangManager().get("game.not-enough-players",
                    "{min}", String.valueOf(minPlayers)));
            return false;
        }

        cancelTasks();
        allPlayerUUIDs.clear();
        guardianUUIDs.clear();
        spectators.clear();
        imposterUUID    = null;
        endingGame      = false;
        startedByTouch  = false;
        waitingForTouch = true;
        startTime       = System.currentTimeMillis();
        lastReminderTime = 0L;

        // keepInventory uygula
        applyKeepInventory();

        Player runnerPlayer = runnerUUID != null ? Bukkit.getPlayer(runnerUUID) : null;
        if (runnerPlayer == null || !runnerPlayer.isOnline()) {
            runnerPlayer = online.get(new Random().nextInt(online.size()));
        }
        runnerUUID = runnerPlayer.getUniqueId();

        List<Player> nonRunners = new ArrayList<>(online);
        nonRunners.remove(runnerPlayer);

        boolean twoPlayerMode = (online.size() == 2);
        if (!twoPlayerMode && !nonRunners.isEmpty()) {
            Player imp = nonRunners.get(new Random().nextInt(nonRunners.size()));
            imposterUUID = imp.getUniqueId();
        }

        allPlayerUUIDs.add(runnerUUID);
        for (Player p : nonRunners) {
            allPlayerUUIDs.add(p.getUniqueId());
            guardianUUIDs.add(p.getUniqueId());
        }

        TaskManager.TaskEntry te = plugin.getTaskManager().getRandomTaskEntry();
        currentTaskKey     = te.key;
        currentTaskDisplay = te.display;
        timeLeft = plugin.getConfig().getInt("game.duration", 600);

        final Player finalRunner = runnerPlayer;
        for (UUID uuid : new ArrayList<>(allPlayerUUIDs)) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) continue;
            clearPotionEffects(p);
            giveKit(p);
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            p.setSaturation(20f);
            if (!p.equals(finalRunner)) p.teleport(finalRunner.getLocation());
        }

        if (twoPlayerMode) {
            broadcast(plugin.getLangManager().get("game.two-player-no-imposter"));
        }

        broadcast(plugin.getLangManager().get("game.waiting-for-start"));

        Player impPlayer = getImposter();
        if (impPlayer != null && impPlayer.isOnline()) {
            VersionUtil.sendTitle(impPlayer,
                    plugin.getLangManager().getRaw("game.imposter-title-reveal"),
                    plugin.getLangManager().getRaw("game.imposter-subtitle-reveal"),
                    10, 80, 20);
            Player runnerP = getRunner();
            if (plugin.getConfig().getBoolean("game.show-imposter-to-runner-in-chat", false)
                    && runnerP != null && runnerP.isOnline()) {
                runnerP.sendMessage(plugin.getLangManager().get(
                        "game.summary-imposter", "{player}", impPlayer.getName()));
            }
        }

        startActionBar();
        return true;
    }

    // ── Touch trigger ──────────────────────────────────────────────────────
    public void onRunnerTouched(Player toucher) {
        if (!waitingForTouch || startedByTouch) return;
        Player runner = getRunner();
        if (runner == null || toucher.equals(runner)) return;
        if (!isPlayerInGame(toucher.getUniqueId())) return;

        startedByTouch  = true;
        waitingForTouch = false;
        gameRunning     = true;
        startTime       = System.currentTimeMillis();

        broadcast(plugin.getLangManager().get("game.started"));
        startTimer();
    }

    // ── Runner events ──────────────────────────────────────────────────────
    public void onRunnerDeath() {
        if (gameRunning || waitingForTouch) endGame(false);
    }

    public void onRunnerTaskComplete() {
        if (gameRunning || waitingForTouch) endGame(true);
    }

    public void onTimeUp() {
        broadcast(plugin.getLangManager().get("game.time-up"));
        endGame(false);
    }

    // ── End ────────────────────────────────────────────────────────────────
    private void endGame(boolean runnerWon) {
        if (endingGame) return;
        endingGame = true;

        gameRunning     = false;
        waitingForTouch = false;
        cancelTasks();

        // keepInventory'yi hemen geri yükle
        restoreKeepInventory();

        long elapsed = startTime > 0
                ? (System.currentTimeMillis() - startTime) / 1000
                : 0;

        String titleKey    = runnerWon ? "game.runner-won-title"          : "game.runner-died-title";
        String subtitleKey = runnerWon ? "game.runner-won-subtitle"       : "game.runner-died-subtitle";
        String resultKey   = runnerWon ? "game.summary-result-runner-won" : "game.summary-result-guardians-won";

        String runnerName = resolveNameOrFallback(runnerUUID, "?");
        String impName    = imposterUUID != null
                ? resolveNameOrFallback(imposterUUID, "?")
                : plugin.getLangManager().getRaw("game.no-imposter");
        String taskName   = currentTaskDisplay != null ? currentTaskDisplay : "?";

        for (Player p : Bukkit.getOnlinePlayers()) {
            VersionUtil.sendTitle(p,
                    plugin.getLangManager().getRaw(titleKey),
                    plugin.getLangManager().getRaw(subtitleKey),
                    10, 80, 20);
            p.sendMessage(plugin.getLangManager().get("game.summary-header"));
            p.sendMessage(plugin.getLangManager().get("game.summary-runner",   "{player}", runnerName));
            p.sendMessage(plugin.getLangManager().get("game.summary-task",     "{task}",   taskName));
            p.sendMessage(plugin.getLangManager().get("game.summary-imposter", "{player}", impName));
            p.sendMessage(plugin.getLangManager().get("game.summary-duration", "{time}",   String.valueOf(elapsed)));
            p.sendMessage(plugin.getLangManager().get(resultKey));
            p.sendMessage(plugin.getLangManager().get("game.summary-footer"));
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Oyun katılımcıları (spectator olmayanlar) — ışınlanma hedefleri
            List<Player> nonSpectatorPlayers = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!spectators.contains(p.getUniqueId())) {
                    nonSpectatorPlayers.add(p);
                }
            }

            Random rng = new Random();

            for (Player p : Bukkit.getOnlinePlayers()) {
                clearPotionEffects(p);
                p.getInventory().clear();
                p.setGameMode(GameMode.SURVIVAL);

                // Oyun sırasında katılan spectator'lar: rastgele oyuncuya ışınla
                if (spectators.contains(p.getUniqueId())) {
                    if (!nonSpectatorPlayers.isEmpty()) {
                        Player target = nonSpectatorPlayers.get(
                                rng.nextInt(nonSpectatorPlayers.size()));
                        p.teleport(target.getLocation());
                    }
                    // Eğer herkes spectator ise olduğu yerde kalır (ışınlanma yok)
                }
            }

            resetState();
        }, 100L);
    }

    public void forceStop() {
        endingGame = false;
        cancelTasks();
        gameRunning     = false;
        waitingForTouch = false;
        startedByTouch  = false;

        // keepInventory'yi geri yükle
        restoreKeepInventory();

        plugin.getKitEditCommand().clearAllEditing();

        for (Player p : Bukkit.getOnlinePlayers()) {
            clearPotionEffects(p);
            p.getInventory().clear();
            if (p.getGameMode() == GameMode.SPECTATOR
                    || p.getGameMode() == GameMode.CREATIVE) {
                p.setGameMode(GameMode.SURVIVAL);
            }
        }

        savedInventories.clear();
        savedGameModes.clear();
        resetState();
    }

    private void resetState() {
        runnerUUID         = null;
        imposterUUID       = null;
        currentTaskKey     = null;
        currentTaskDisplay = null;
        startTime          = 0;
        endingGame         = false;
        startedByTouch     = false;
        waitingForTouch    = false;
        guardianUUIDs.clear();
        allPlayerUUIDs.clear();
        spectators.clear();
    }

    // ── Action bar & timer ─────────────────────────────────────────────────
    private void startActionBar() {
        if (actionBarTask != null) actionBarTask.cancel();

        actionBarTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!waitingForTouch && !gameRunning) { cancel(); return; }

                Player runner = getRunner();

                if (waitingForTouch && runner != null && runner.isOnline()) {
                    boolean anyClose = false;
                    for (UUID uuid : allPlayerUUIDs) {
                        if (uuid.equals(runnerUUID)) continue;
                        Player p = Bukkit.getPlayer(uuid);
                        if (p == null || !p.isOnline()) continue;
                        if (p.getWorld().equals(runner.getWorld())
                                && p.getLocation().distanceSquared(runner.getLocation()) < 225.0) {
                            anyClose = true;
                            break;
                        }
                    }
                    if (!anyClose) {
                        long now = System.currentTimeMillis();
                        if (now - lastReminderTime > 15_000L) {
                            broadcast(plugin.getLangManager().get("game.reminder-start"));
                            lastReminderTime = now;
                        }
                    }
                }

                pushActionBar();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void startTimer() {
        if (timerTask != null) timerTask.cancel();

        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameRunning) { cancel(); return; }
                timeLeft--;
                pushActionBar();
                if (timeLeft <= 0) { cancel(); onTimeUp(); }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void pushActionBar() {
        if (allPlayerUUIDs.isEmpty()) return;

        String visibility = plugin.getConfig().getString("game.task-visibility", "runner_only");
        String timeStr    = formatTime(timeLeft);

        String taskPart = currentTaskDisplay != null
                ? plugin.getLangManager().getRaw("game.task-actionbar", "{task}", currentTaskDisplay)
                : "";

        String fullBar;
        if (!taskPart.isEmpty()) {
            fullBar = gameRunning
                    ? taskPart + ChatColor.GRAY + " | " + ChatColor.GREEN + timeStr
                    : taskPart;
        } else {
            fullBar = gameRunning ? ChatColor.GREEN + timeStr : "";
        }

        String timeBar = plugin.getLangManager().getRaw("game.time-left", "{time}", timeStr);

        for (UUID uuid : allPlayerUUIDs) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) continue;

            if (uuid.equals(runnerUUID)) {
                if (!fullBar.isEmpty()) VersionUtil.sendActionBar(p, fullBar);
            } else if (uuid.equals(imposterUUID)
                    && ("runner_and_imposter".equals(visibility) || "everyone".equals(visibility))) {
                if (!fullBar.isEmpty()) VersionUtil.sendActionBar(p, fullBar);
            } else if ("everyone".equals(visibility)) {
                if (!fullBar.isEmpty()) VersionUtil.sendActionBar(p, fullBar);
            } else {
                if (gameRunning) VersionUtil.sendActionBar(p, timeBar);
            }
        }
    }

    private String formatTime(int totalSeconds) {
        if (totalSeconds < 0) totalSeconds = 0;
        return (totalSeconds / 60) + ":" + String.format("%02d", totalSeconds % 60);
    }

    private void cancelTasks() {
        if (timerTask     != null) { timerTask.cancel();     timerTask     = null; }
        if (actionBarTask != null) { actionBarTask.cancel(); actionBarTask = null; }
    }

    @SuppressWarnings("deprecation")
    private String resolveNameOrFallback(UUID uuid, String fallback) {
        if (uuid == null) return fallback;
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) return online.getName();
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        return name != null ? name : fallback;
    }

    // ── Kit helpers ────────────────────────────────────────────────────────
    private void giveKit(Player p) {
        p.getInventory().clear();
        p.setGameMode(GameMode.SURVIVAL);

        final String type;
        UUID uid = p.getUniqueId();
        if (uid.equals(runnerUUID))        type = "runner";
        else if (uid.equals(imposterUUID)) type = "imposter";
        else                               type = "guardian";

        Map<Integer, ItemStack> kit = plugin.getKitManager().getKit(type);
        PlayerInventory inv = p.getInventory();

        for (Map.Entry<Integer, ItemStack> e : kit.entrySet()) {
            if (e.getKey() >= 0 && e.getKey() < 36) {
                inv.setItem(e.getKey(), e.getValue().clone());
            }
        }

        if (uid.equals(runnerUUID) && inv.getItem(8) == null) {
            inv.setItem(8, new ItemStack(Material.COOKED_BEEF, 64));
        }

        p.updateInventory();
    }

    // ── Inventory save / restore ───────────────────────────────────────────
    public void saveInventory(Player p) {
        savedInventories.put(p.getUniqueId(), p.getInventory().getContents().clone());
        savedGameModes.put(p.getUniqueId(), p.getGameMode());
    }

    public void restoreInventory(Player p) {
        ItemStack[] contents = savedInventories.remove(p.getUniqueId());
        GameMode    gm       = savedGameModes.remove(p.getUniqueId());
        if (contents != null) p.getInventory().setContents(contents);
        if (gm       != null) p.setGameMode(gm);
        p.updateInventory();
    }

    public boolean hasSavedInventory(Player p) {
        return savedInventories.containsKey(p.getUniqueId());
    }

    public void clearSavedInventory(Player p) {
        savedInventories.remove(p.getUniqueId());
        savedGameModes.remove(p.getUniqueId());
    }

    private void broadcast(String msg) {
        for (Player p : Bukkit.getOnlinePlayers()) p.sendMessage(msg);
    }
}