package com.extez0612.markedfordeath.commands;

import com.extez0612.markedfordeath.MarkedForDeath;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class MFDCommand implements CommandExecutor, TabCompleter {

    private final MarkedForDeath plugin;

    public MFDCommand(MarkedForDeath plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {
        if (!sender.hasPermission("markedfordeath.admin")) {
            sender.sendMessage(
                    plugin.getLangManager().get("commands.no-permission"));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(
                    plugin.getLangManager().get("commands.usage-mfd"));
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "start":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(
                            plugin.getLangManager().get("commands.player-only"));
                    return true;
                }
                plugin.getGameManager().startGame((Player) sender);
                break;

            case "stop":
                if (!plugin.getGameManager().isGameRunning()
                        && !plugin.getGameManager().isWaitingForTouch()) {
                    sender.sendMessage(
                            plugin.getLangManager().get("game.not-running"));
                    return true;
                }
                plugin.getGameManager().forceStop();
                sender.sendMessage(
                        plugin.getLangManager().get("game.stopped"));
                break;

            case "reload":
                plugin.reloadPlugin();
                sender.sendMessage(
                        plugin.getLangManager().get("commands.reload"));
                break;

            case "help":
                sender.sendMessage(plugin.getLangManager().get("commands.help-header"));
                sender.sendMessage(plugin.getLangManager().get("commands.help-start"));
                sender.sendMessage(plugin.getLangManager().get("commands.help-stop"));
                sender.sendMessage(plugin.getLangManager().get("commands.help-selectrunner"));
                sender.sendMessage(plugin.getLangManager().get("commands.help-reload"));
                sender.sendMessage(plugin.getLangManager().get("commands.help-kitedit"));
                // ── YENİ: kiteditgui ───────────────────────────────────────
                sender.sendMessage(plugin.getLangManager().get("commands.help-kiteditgui"));
                break;

            case "selectrunner":
                if (args.length < 2) {
                    sender.sendMessage(
                            plugin.getLangManager().get("commands.usage-mfd"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getLangManager().get(
                            "commands.player-not-found",
                            "{player}", args[1]));
                    return true;
                }
                plugin.getGameManager().setRunner(target);
                break;

            default:
                sender.sendMessage(
                        plugin.getLangManager().get("commands.usage-mfd"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd,
                                      String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (!sender.hasPermission("markedfordeath.admin")) return list;

        if (args.length == 1) {
            list.addAll(Arrays.asList(
                    "start", "stop", "reload", "help", "selectrunner"));
        } else if (args.length == 2
                && args[0].equalsIgnoreCase("selectrunner")) {
            for (Player p : Bukkit.getOnlinePlayers())
                list.add(p.getName());
        }

        String q = args[args.length - 1].toLowerCase();
        list.removeIf(s -> !s.toLowerCase().startsWith(q));
        return list;
    }
}