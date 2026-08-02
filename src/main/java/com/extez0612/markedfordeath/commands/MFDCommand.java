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
        if (args.length == 0) {
            if (!sender.hasPermission("markedfordeath.admin")) {
                sender.sendMessage(plugin.getLangManager().get("commands.no-permission"));
                return true;
            }
            sender.sendMessage(plugin.getLangManager().get("commands.usage-mfd"));
            return true;
        }

        String sub = args[0].toLowerCase();

        // ── Kit editing subcommands: their own permission (markedfordeath.kitedit) ─
        if (sub.equals("kitedit")) {
            if (!sender.hasPermission("markedfordeath.kitedit")) {
                sender.sendMessage(plugin.getLangManager().get("commands.no-permission"));
                return true;
            }
            return plugin.getKitEditCommand().onCommand(sender, cmd, "kitedit", tail(args));
        }

        if (sub.equals("kiteditgui")) {
            if (!sender.hasPermission("markedfordeath.kitedit")) {
                sender.sendMessage(plugin.getLangManager().get("commands.no-permission"));
                return true;
            }
            return plugin.getKitEditGUICommand().onCommand(sender, cmd, "kiteditgui", tail(args));
        }

        // ── Everything else is an admin subcommand ──────────────────────────
        if (!sender.hasPermission("markedfordeath.admin")) {
            sender.sendMessage(plugin.getLangManager().get("commands.no-permission"));
            return true;
        }

        switch (sub) {

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

        if (args.length == 1) {
            String q = args[0].toLowerCase();
            if (sender.hasPermission("markedfordeath.admin")) {
                list.addAll(Arrays.asList(
                        "start", "stop", "reload", "help", "selectrunner"));
            }
            if (sender.hasPermission("markedfordeath.kitedit")) {
                list.addAll(Arrays.asList("kitedit", "kiteditgui"));
            }
            list.removeIf(s -> !s.startsWith(q));
            return list;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("kitedit")
                && sender.hasPermission("markedfordeath.kitedit")) {
            return plugin.getKitEditCommand().onTabComplete(sender, cmd, alias, tail(args));
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("selectrunner")
                && sender.hasPermission("markedfordeath.admin")) {
            for (Player p : Bukkit.getOnlinePlayers()) list.add(p.getName());
            String q = args[1].toLowerCase();
            list.removeIf(s -> !s.toLowerCase().startsWith(q));
            return list;
        }

        return Collections.emptyList();
    }

    private String[] tail(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }
}