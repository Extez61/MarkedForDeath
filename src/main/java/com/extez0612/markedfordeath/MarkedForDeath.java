package com.extez0612.markedfordeath;

import com.extez0612.markedfordeath.commands.KitEditCommand;
import com.extez0612.markedfordeath.commands.KitEditGUICommand;
import com.extez0612.markedfordeath.commands.MFDCommand;
import com.extez0612.markedfordeath.listeners.GameListener;
import com.extez0612.markedfordeath.listeners.KitEditGUIListener;
import com.extez0612.markedfordeath.listeners.KitEditListener;
import com.extez0612.markedfordeath.listeners.PlayerJoinListener;
import com.extez0612.markedfordeath.managers.GameManager;
import com.extez0612.markedfordeath.managers.KitManager;
import com.extez0612.markedfordeath.managers.LangManager;
import com.extez0612.markedfordeath.managers.TaskManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MarkedForDeath extends JavaPlugin {

    private static MarkedForDeath instance;

    private GameManager    gameManager;
    private KitManager     kitManager;
    private TaskManager    taskManager;
    private LangManager    langManager;
    private KitEditCommand kitEditCommand;

    // ── Persistent compass-hint tracking ──────────────────────────────────
    private final Set<UUID> notifiedPlayers = new HashSet<>();
    private File notifiedFile;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("lang/tr.yml", false);
        saveResource("lang/en.yml", false);

        loadNotifiedPlayers();

        langManager    = new LangManager(this);
        kitManager     = new KitManager(this);
        taskManager    = new TaskManager(this);
        gameManager    = new GameManager(this);
        kitEditCommand = new KitEditCommand(this);

        MFDCommand        mfdCmd    = new MFDCommand(this);
        KitEditGUICommand guiCmd    = new KitEditGUICommand(this);

        getCommand("markedfordeath").setExecutor(mfdCmd);
        getCommand("markedfordeath").setTabCompleter(mfdCmd);
        getCommand("kitedit").setExecutor(kitEditCommand);
        getCommand("kitedit").setTabCompleter(kitEditCommand);
        getCommand("kiteditgui").setExecutor(guiCmd);

        getServer().getPluginManager().registerEvents(
                new GameListener(this, kitEditCommand), this);
        getServer().getPluginManager().registerEvents(
                new KitEditListener(this, kitEditCommand), this);
        getServer().getPluginManager().registerEvents(
                new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(
                new KitEditGUIListener(this), this);

        getLogger().info("MarkedForDeath enabled! made by ~extez061");
        getLogger().info("https://modrinth.com/user/Extez0612");
    }

    @Override
    public void onDisable() {
        if (gameManager != null
                && (gameManager.isGameRunning() || gameManager.isWaitingForTouch())) {
            gameManager.forceStop();
        }
        getLogger().info("MarkedForDeath disabled.");
    }

    public void reloadPlugin() {
        reloadConfig();
        langManager.reload();
        taskManager.reload();
    }

    // ── Notified helpers ───────────────────────────────────────────────────

    private void loadNotifiedPlayers() {
        notifiedFile = new File(getDataFolder(), "notified.yml");
        if (!notifiedFile.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(notifiedFile);
        List<String> list = cfg.getStringList("notified");
        for (String s : list) {
            try { notifiedPlayers.add(UUID.fromString(s)); }
            catch (IllegalArgumentException ignored) {}
        }
    }

    public boolean isNotified(UUID uuid) {
        return notifiedPlayers.contains(uuid);
    }

    public void markNotified(UUID uuid) {
        if (notifiedPlayers.add(uuid)) {
            YamlConfiguration cfg = new YamlConfiguration();
            List<String> list = new ArrayList<>();
            for (UUID u : notifiedPlayers) list.add(u.toString());
            cfg.set("notified", list);
            try { cfg.save(notifiedFile); }
            catch (IOException e) { getLogger().warning("Could not save notified.yml: " + e.getMessage()); }
        }
    }

    // ── Accessors ──────────────────────────────────────────────────────────
    public static MarkedForDeath getInstance() { return instance; }
    public GameManager    getGameManager()     { return gameManager; }
    public KitManager     getKitManager()      { return kitManager; }
    public TaskManager    getTaskManager()     { return taskManager; }
    public LangManager    getLangManager()     { return langManager; }
    public KitEditCommand getKitEditCommand()  { return kitEditCommand; }
}