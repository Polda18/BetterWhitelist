package me.polda18.betterwhitelist.commands;

import me.polda18.betterwhitelist.BetterWhitelist;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.logging.Level;

public class WhitelistCommand implements CommandExecutor {
    private BetterWhitelist plugin;

    public WhitelistCommand(BetterWhitelist plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean isConsole = sender instanceof ConsoleCommandSender;

        if(!command.getName().equals("betterwhitelist") && !label.equals("betterwhitelist") && !label.equals("bw")) {
            // Executed command isn't for this plugin
            return true;
        }

        if(!isConsole && !sender.hasPermission("betterwhitelist.execute")) {
            // Command sender is not a console and has no permission to execute this command
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getLanguage().getConfig().getString("messages.error.permission")));
            return true;
        }

        if(args.length < 1) {
            // Just a command alone is sent - display usage
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getLanguage().getConfig().getString("messages.usage")));
        } else {
            // Resolve subcommands and their arguments
            switch(args[0]) {
                case "on":
                    // TODO: Turn whitelist on
                case "off":
                    // TODO: Turn whitelist off
                case "add":
                    // TODO: Add player to whitelist
                case "remove":
                    // TODO: Remove player from whitelist
                default:
                    // Subcommand not recognized, display usage
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getLanguage().getConfig().getString("messages.usage")));
            }
        }

        return true;
    }
}
