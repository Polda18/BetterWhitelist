package me.polda18.betterwhitelist.commands;

import me.polda18.betterwhitelist.BetterWhitelist;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Autocomplete implements TabCompleter {
    private final BetterWhitelist plugin;

    public Autocomplete(BetterWhitelist plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        ArrayList<String> list = new ArrayList<>();
        boolean isConsole = sender instanceof ConsoleCommandSender;

        if(!command.getName().equalsIgnoreCase("betterwhitelist")
                || (!isConsole && !sender.hasPermission("betterwhitelist.execute"))) {
            return null;
            // Make sure this autocomplete only returns when the command invoked belongs to this plugin
            // and the command sender has appropriate permissions to run the command.
            // Since this plugin is one-purpose, only one command belongs in here.
        }

        switch(args.length) {
            case 1:         // First argument
                list.add("on");
                list.add("off");
                list.add("add");
                list.add("remove");
                list.add("reload");
                list.add("list");
                list.add("lang");
                list.add("import");
                break;
            case 2:         // Second argument (if available)
                switch(args[0].toLowerCase()) {
                    case "add":         // User wants to add a player to whitelist
                        list.add("ExamplePlayer");      // Give an example player :)
                        break;
                    case "remove":      // User wants to remove player from whitelist
                        for (String player :
                                plugin.getWhitelist().getConfig()
                                        .getConfigurationSection("").getKeys(false)) {
                            list.add(player);           // Get all whitelisted players
                        }
                        break;
                    case "lang":        // User wants to view or change set language
                        // Give a list of available languages
                        for(String lang_code: plugin.listAvailableLanguages().keySet()) {
                            list.add(lang_code);
                        }
                        break;
                    case "import":      // User wants to import vanilla whitelist
                        // No further informations can be provided here, fall back to default
                    case "on":          // User wants to enable whitelist
                        // No further informations can be provided here, fall back to default
                    case "off":         // User wants to disable whitelist
                        // No further informations can be provided here, fall back to default
                    case "reload":      // User wants to reload whitelist from a config
                        // No further informations can be provided here, fall back to default
                    case "list":        // User wants to list players in whitelist
                        // No further informations can be provided here, fall back to default
                    default:            // User entered invalid argument
                        // Invalid argument provided, default to no list given
                }
                break;
            default:
                // No further informations provided, return empty list
        }

        return list;
    }
}
