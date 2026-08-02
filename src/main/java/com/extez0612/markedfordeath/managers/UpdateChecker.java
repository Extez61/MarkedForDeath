package com.extez0612.markedfordeath.managers;

import com.extez0612.markedfordeath.MarkedForDeath;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modrinth API üzerinden eklentinin en son yayınlanan versiyonunu kontrol eder.
 * Sonuç bellekte tutulur; checkAsync() sadece onEnable'da bir kez çağrılır,
 * böylece her oyuncu girişinde tekrar API isteği atılmaz.
 */
public class UpdateChecker {

    private static final String API_URL      = "https://api.modrinth.com/v2/project/markedfordeath/version";
    private static final String DOWNLOAD_URL = "https://modrinth.com/plugin/markedfordeath";

    private final MarkedForDeath plugin;
    private volatile String  latestVersion = null;
    private volatile boolean checked       = false;

    public UpdateChecker(MarkedForDeath plugin) { this.plugin = plugin; }

    public void checkAsync() {
        if (!plugin.getConfig().getBoolean("update-checker.enabled", true)) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(API_URL).openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent",
                        "extez0612/MarkedForDeath/" + plugin.getDescription().getVersion());
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);

                if (con.getResponseCode() != 200) return;

                StringBuilder sb = new StringBuilder();
                try (BufferedReader r = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = r.readLine()) != null) sb.append(line);
                }

                // Modrinth /version listesi en yeni sürüm en üstte olacak şekilde döner.
                Matcher m = Pattern.compile("\"version_number\"\\s*:\\s*\"([^\"]+)\"").matcher(sb);
                if (m.find()) latestVersion = m.group(1);
            } catch (Exception ignored) {
                // Ağ hatası, API kapalı vb. — sessizce yok say.
            } finally {
                checked = true;
            }
        });
    }

    /**
     * Sadece Modrinth'teki sürüm, eklentinin şu anki sürümünden GERÇEKTEN büyükse
     * true döner. Eşit ya da küçük (örn. dev/local sürüm daha yeniyse) durumlarda
     * hiçbir uyarı gösterilmez.
     */
    public boolean isUpdateAvailable() {
        if (!checked || latestVersion == null) return false;
        return compareVersions(latestVersion, plugin.getDescription().getVersion()) > 0;
    }

    /**
     * İki versiyon string'ini nokta ile ayırıp sayısal olarak karşılaştırır.
     * Örn: "1.10" > "1.9", "1.2.1" > "1.2", "1.2" == "1.2.0".
     * Sayısal olmayan segmentler (örn. "-SNAPSHOT") 0 kabul edilir.
     * Dönüş: pozitif -> v1 daha yeni, negatif -> v1 daha eski, 0 -> eşit.
     */
    private int compareVersions(String v1, String v2) {
        String[] p1 = v1.split("[.\\-+]");
        String[] p2 = v2.split("[.\\-+]");
        int len = Math.max(p1.length, p2.length);

        for (int i = 0; i < len; i++) {
            int n1 = parsePart(i < p1.length ? p1[i] : "0");
            int n2 = parsePart(i < p2.length ? p2[i] : "0");
            if (n1 != n2) return Integer.compare(n1, n2);
        }
        return 0;
    }

    private int parsePart(String part) {
        try {
            return Integer.parseInt(part.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getLatestVersion()      { return latestVersion; }
    public static String getDownloadUrl() { return DOWNLOAD_URL; }
}