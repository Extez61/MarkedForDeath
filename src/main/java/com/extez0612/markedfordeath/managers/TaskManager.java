package com.extez0612.markedfordeath.managers;

import com.extez0612.markedfordeath.MarkedForDeath;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TaskManager {

    public static final class TaskEntry {
        public final String key;
        public final String display;

        TaskEntry(String key, String display) {
            this.key     = key;
            this.display = display;
        }
    }

    private final MarkedForDeath plugin;
    private final List<TaskEntry> tasks  = new ArrayList<>();
    private final Random          random = new Random();

    public TaskManager(MarkedForDeath plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        tasks.clear();
        boolean tr = "tr".equals(plugin.getConfig().getString("language", "en"));

        add("lava-death",    tr ? "Lavda yanarak öl"           : "Die in lava");
        add("drowning",      tr ? "Suda boğularak öl"          : "Drown in water");
        add("magma-block",   tr ? "Magma bloğuna öl"           : "Die on a magma block");
        add("creeper",       tr ? "Creeper'a patlayarak öl"    : "Die to a creeper explosion");
        add("suffocation",   tr ? "Bloğa sıkışarak öl"         : "Die by suffocation");
        add("skeleton",      tr ? "İskelete öl"                : "Die to a skeleton");
        add("zombie",        tr ? "Zombiye öl"                 : "Die to a zombie");
        add("spider",        tr ? "Örümceğe öl"               : "Die to a spider");
        add("iron-golem",    tr ? "Demir goleme öl"            : "Die to an iron golem");
        add("fall-damage",   tr ? "Düşerek öl"                : "Die from fall damage");
        add("cactus",        tr ? "Kaktüse öl"                : "Die to a cactus");
        add("berry-bush",    tr ? "Tatlı meyve çalısına öl"   : "Die to a berry bush");
        add("anvil",         tr ? "Örs düşerek öl"            : "Die to a falling anvil");
        add("lightning",     tr ? "Yıldırıma çarpılarak öl"   : "Die to lightning");
        add("wither-effect", tr ? "Solma etkisiyle öl"        : "Die from wither effect");
    }

    private void add(String key, String display) {
        if (plugin.getConfig().getBoolean("tasks." + key, true)) {
            tasks.add(new TaskEntry(key, display));
        }
    }

    public TaskEntry getRandomTaskEntry() {
        if (tasks.isEmpty()) return new TaskEntry("unknown", "???");
        return tasks.get(random.nextInt(tasks.size()));
    }

    public String getRandomTask() {
        return getRandomTaskEntry().display;
    }
}