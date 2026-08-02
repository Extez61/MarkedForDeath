package com.extez0612.markedfordeath;

import com.extez0612.markedfordeath.commands.KitEditCommand;
import com.extez0612.markedfordeath.commands.KitEditGUICommand;
import com.extez0612.markedfordeath.commands.MFDCommand;
import com.extez0612.markedfordeath.listeners.GameListener;
import com.extez0612.markedfordeath.listeners.KitEditGUIListener;
import com.extez0612.markedfordeath.listeners.KitEditListener;
import com.extez0612.markedfordeath.listeners.PlayerJoinListener;
import com.extez0612.markedfordeath.managers.ConfigUpdater;
import com.extez0612.markedfordeath.managers.GameManager;
import com.extez0612.markedfordeath.managers.KitManager;
import com.extez0612.markedfordeath.managers.LangManager;
import com.extez0612.markedfordeath.managers.TaskManager;
import com.extez0612.markedfordeath.managers.UpdateChecker;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class MarkedForDeath extends JavaPlugin {

    private static MarkedForDeath instance;

    private GameManager        gameManager;
    private KitManager         kitManager;
    private TaskManager        taskManager;
    private LangManager        langManager;
    private KitEditCommand     kitEditCommand;
    private KitEditGUICommand  kitEditGUICommand;
    private UpdateChecker      updateChecker;
    private ConfigUpdater      configUpdater;

    // ── kits.yml ───────────────────────────────────────────────────────────
    private FileConfiguration kitsConfig;
    private File              kitsFile;

    @Override
    public void onEnable() {
        instance = this;

        // 1) Dosya yoksa güncel şablonu diske yaz.
        saveDefaultConfig();
        // 2) Dosya zaten vardıysa (eski sürümden yükseltme), eski ayarları
        //    kaybetmeden yeni şemaya taşı (kaldırılan anahtarlar silinir,
        //    eklenen anahtarlar varsayılan değerle eklenir).
        configUpdater = new ConfigUpdater(this);
        configUpdater.updateIfNeeded();
        // 3) Diskteki (olası şekilde güncellenmiş) dosyayı belleğe yükle.
        reloadConfig();

        saveResource("lang/tr.yml", false);
        saveResource("lang/en.yml", false);

        loadKitsConfig();

        langManager       = new LangManager(this);
        kitManager        = new KitManager(this);
        taskManager       = new TaskManager(this);
        gameManager       = new GameManager(this);
        kitEditCommand    = new KitEditCommand(this);
        kitEditGUICommand = new KitEditGUICommand(this);
        updateChecker      = new UpdateChecker(this);
        updateChecker.checkAsync();

        MFDCommand mfdCmd = new MFDCommand(this);

        getCommand("markedfordeath").setExecutor(mfdCmd);
        getCommand("markedfordeath").setTabCompleter(mfdCmd);
        getCommand("kitedit").setExecutor(kitEditCommand);
        getCommand("kitedit").setTabCompleter(kitEditCommand);
        getCommand("kiteditgui").setExecutor(kitEditGUICommand);

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
        reloadKitsConfig();
        langManager.reload();
        taskManager.reload();
        updateChecker.checkAsync();
    }

    // ── kits.yml helpers ───────────────────────────────────────────────────

    /**
     * Copies the default kits.yml from the jar if it doesn't exist on disk,
     * then loads it into memory.
     */
    private void loadKitsConfig() {
        kitsFile = new File(getDataFolder(), "kits.yml");
        if (!kitsFile.exists()) {
            saveResource("kits.yml", false);
        }
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
    }

    /** Re-reads kits.yml from disk (called by /mfd reload). */
    private void reloadKitsConfig() {
        if (kitsFile == null) kitsFile = new File(getDataFolder(), "kits.yml");
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
    }

    /** Flushes the in-memory kitsConfig back to kits.yml. */
    public void saveKitsConfig() {
        try {
            kitsConfig.save(kitsFile);
        } catch (IOException e) {
            getLogger().warning("Could not save kits.yml: " + e.getMessage());
        }
    }

    public FileConfiguration getKitsConfig() { return kitsConfig; }

    // ── Accessors ──────────────────────────────────────────────────────────
    public static MarkedForDeath getInstance()      { return instance; }
    public GameManager       getGameManager()       { return gameManager; }
    public KitManager        getKitManager()        { return kitManager; }
    public TaskManager       getTaskManager()       { return taskManager; }
    public LangManager       getLangManager()       { return langManager; }
    public KitEditCommand    getKitEditCommand()    { return kitEditCommand; }
    public KitEditGUICommand getKitEditGUICommand() { return kitEditGUICommand; }
    public UpdateChecker     getUpdateChecker()     { return updateChecker; }
    public ConfigUpdater     getConfigUpdater()     { return configUpdater; }
}