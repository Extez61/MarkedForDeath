package com.extez0612.markedfordeath.managers;

import com.extez0612.markedfordeath.MarkedForDeath;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * config.yml için otomatik şema güncelleme (migration) sistemi.
 *
 * Nasıl çalışır:
 *  - config.yml'nin en üstünde "config-version" adında dahili, sayısal bir alan bulunur.
 *  - Eklenti her açıldığında diskteki config.yml'nin versiyonu, jar içindeki
 *    (güncel) config.yml şablonunun versiyonundan küçükse migration tetiklenir.
 *  - Migration, jar'daki GÜNCEL config.yml şablonunu satır satır okur — bütün
 *    yorumlar, başlıklar ve biçimlendirme AYNEN korunur — ve her "key: value"
 *    satırı için, o anahtar eski (diskteki) config'de de varsa değerini
 *    kullanıcının eski değeriyle değiştirir.
 *
 *  Sonuç:
 *    • Devam eden ayarlar   -> kullanıcının eski değeri korunur (kaybolmaz).
 *    • Yeni eklenen ayarlar -> yeni şablonda zaten olduğu için otomatik eklenir.
 *    • Kaldırılan ayarlar   -> yeni şablonda satırı olmadığı için otomatik silinir.
 *
 *  Migration öncesinde eski dosya "config.yml.bak" olarak yedeklenir.
 */
public class ConfigUpdater {

    private final MarkedForDeath plugin;

    public ConfigUpdater(MarkedForDeath plugin) {
        this.plugin = plugin;
    }

    /** onEnable içinde saveDefaultConfig()'ten HEMEN SONRA çağrılmalı. */
    public void updateIfNeeded() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            // saveDefaultConfig() zaten en güncel şablonu oluşturdu, yapacak bir şey yok.
            return;
        }

        String defaultContent = readResource("config.yml");
        if (defaultContent == null) {
            plugin.getLogger().warning("Dahili config.yml şablonu bulunamadı, migration atlanıyor.");
            return;
        }

        YamlConfiguration defaultCfg = new YamlConfiguration();
        try {
            defaultCfg.loadFromString(defaultContent);
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().warning("Dahili config.yml şablonu ayrıştırılamadı, migration atlanıyor: " + e.getMessage());
            return;
        }

        YamlConfiguration oldCfg = YamlConfiguration.loadConfiguration(configFile);

        int oldVersion = oldCfg.getInt("config-version", 0);
        int newVersion = defaultCfg.getInt("config-version", 1);

        if (oldVersion >= newVersion) {
            return; // Zaten güncel (veya daha yeni — dokunma).
        }

        String merged = mergeConfigs(defaultContent, oldCfg);

        try {
            File backup = new File(plugin.getDataFolder(), "config.yml.bak");
            Files.copy(configFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().warning("config.yml yedeklenemedi (migration yine de devam ediyor): " + e.getMessage());
        }

        try (Writer w = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
            w.write(merged);
        } catch (IOException e) {
            plugin.getLogger().warning("config.yml güncellenemedi: " + e.getMessage());
            return;
        }

        plugin.getLogger().info("config.yml v" + oldVersion + " -> v" + newVersion
                + " sürümüne güncellendi. Eski ayarlarınız korundu (yedek: config.yml.bak).");
    }

    // ── Satır bazlı birleştirme ──────────────────────────────────────────

    private String mergeConfigs(String defaultContent, YamlConfiguration oldCfg) {
        String[] lines = defaultContent.split("\n", -1);
        StringBuilder out = new StringBuilder();

        // İndent seviyesine göre güncel yol (path) yığını, örn: game -> duration
        Deque<StackEntry> stack = new ArrayDeque<>();

        for (String line : lines) {
            String noCr = line.endsWith("\r") ? line.substring(0, line.length() - 1) : line;
            String trimmed = noCr.trim();

            // Boş satır veya yorum -> aynen kopyala
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                out.append(line).append("\n");
                continue;
            }

            int indent = leadingSpaces(noCr);
            while (!stack.isEmpty() && stack.peek().indent >= indent) stack.pop();

            int colon = noCr.indexOf(':');
            if (colon < 0) {
                out.append(line).append("\n");
                continue;
            }

            String key = noCr.substring(indent, colon).trim();
            String rawValue = colon + 1 < noCr.length() ? noCr.substring(colon + 1) : "";
            String valueTrimmed = rawValue.trim();

            String path = buildPath(stack, key);

            if (valueTrimmed.isEmpty()) {
                // Bu bir bölüm başlığı (alt anahtarları var) — değiştirme, yığına ekle.
                stack.push(new StackEntry(indent, key));
                out.append(line).append("\n");
                continue;
            }

            // config-version her zaman yeni şablondaki (güncel) değeri kullanmalı.
            if (!"config-version".equals(path)
                    && oldCfg.contains(path)
                    && oldCfg.get(path) != null
                    && !(oldCfg.get(path) instanceof ConfigurationSection)) {
                Object oldValue = oldCfg.get(path);
                out.append(noCr, 0, colon + 1).append(' ').append(serialize(oldValue)).append("\n");
            } else {
                out.append(line).append("\n");
            }
        }

        return out.toString();
    }

    private static final class StackEntry {
        final int indent;
        final String key;
        StackEntry(int indent, String key) { this.indent = indent; this.key = key; }
    }

    private String buildPath(Deque<StackEntry> stack, String key) {
        StringBuilder sb = new StringBuilder();
        StackEntry[] arr = stack.toArray(new StackEntry[0]);
        for (int i = arr.length - 1; i >= 0; i--) {
            sb.append(arr[i].key).append('.');
        }
        sb.append(key);
        return sb.toString();
    }

    private int leadingSpaces(String s) {
        int i = 0;
        while (i < s.length() && s.charAt(i) == ' ') i++;
        return i;
    }

    private String serialize(Object value) {
        if (value instanceof String) {
            String s = (String) value;
            if (s.isEmpty() || s.matches(".*[:#].*") || !s.equals(s.trim())) {
                return "\"" + s.replace("\"", "\\\"") + "\"";
            }
            return s;
        }
        return String.valueOf(value);
    }

    private String readResource(String name) {
        try (InputStream in = plugin.getResource(name)) {
            if (in == null) return null;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
            return bos.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            return null;
        }
    }
}