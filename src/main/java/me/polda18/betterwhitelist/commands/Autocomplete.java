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

/**
 * Autocomplete command in chat
 */
public class Autocomplete implements TabCompleter {
    private final BetterWhitelist plugin;

    /**
     * Constructor: creates this autocompletion handler to be registered in the plugin
     * @param plugin Plugin instance to be used for access to configurations
     */
    public Autocomplete(BetterWhitelist plugin) {
        this.plugin = plugin;
    }

    /**
     * Fires when the autocompletion event happens
     * @param sender Command sender that is writing the command
     * @param command Command definition itself
     * @param alias Alias definition of the command itself
     * @param args List of arguments in the command being written
     * @return List of suggestions for next autocompletion
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        ArrayList<String> list = new ArrayList<>();
        boolean isConsole = sender instanceof ConsoleCommandSender;

        if(!command.getName().equalsIgnoreCase("whitelist")) {
            return null;
            // Make sure this autocomplete only returns when the command invoked belongs to this plugin
            // Since this plugin is one-purpose, only one command belongs in here.
        }

        switch(args.length) {
            case 1:         // First argument
                if(isConsole || sender.hasPermission("betterwhitelist.admin"))
                    list.add("on");         // Add to list only if command sender has admin permission
                if(isConsole || sender.hasPermission("betterwhitelist.admin"))
                    list.add("off");        // Add to list only if command sender has admin permission
                if(isConsole || sender.hasPermission("betterwhitelist.add"))
                    list.add("add");        // Add to list only if command sender has add permission
                if(isConsole || sender.hasPermission("betterwhitelist.remove"))
                    list.add("remove");     // Add to list only if command sender has remove permission
                if(isConsole || sender.hasPermission("betterwhitelist.admin"))
                    list.add("reload");     // Add to list only if command sender has admin permission
                if(isConsole || sender.hasPermission("betterwhitelist.list"))
                    list.add("list");       // Add to list only if command sender has list permission
                if(isConsole || sender.hasPermission("betterwhitelist.admin"))
                    list.add("lang");       // Add to list only if command sender has admin permission
                if(isConsole || sender.hasPermission("betterwhitelist.admin"))
                    list.add("import");     // Add to list only if command sender has admin permission
                if(isConsole || sender.hasPermission("betterwhitelist.admin"))
                    list.add("status");     // Add to list only if command sender has admin permission
                if(isConsole || sender.hasPermission("betterwhitelist.admin"))
                    list.add("version");    // Add to list only if command sender has admin permission

                if(list.isEmpty()) {
                    return null;            // If the list is empty (sender has no permission), no list is given
                }
                break;
            case 2:         // Second argument (if available)
                switch(args[0].toLowerCase()) {
                    case "add":         // User wants to add a player to whitelist
                        if(isConsole || sender.hasPermission("betterwhitelist.add")) {
                            list.add("<ExamplePlayer>");    // Give an example player :)
                        } else {
                            return null;
                        }
                        break;
                    case "remove":      // User wants to remove player from whitelist
                        if(isConsole || sender.hasPermission("betterwhitelist.remove")) {
                            // Get all whitelisted players
                            list.addAll(plugin.getWhitelist().getConfig().getKeys(false));
                        } else {
                            return null;
                        }
                        break;
                    case "lang":        // User wants to view or change set language
                        // Give a list of available languages
                        if(isConsole || sender.hasPermission("betterwhitelist.admin")) {
                            list.addAll(plugin.listAvailableLanguages().keySet());
                        } else {
                            return null;
                        }
                        break;
                    default:            // User entered invalid subcommand or subcommand has no arguments
                        // Invalid subcommand provided or subcommand has no arguments, default to no list given
                        return null;
                }
                break;
            default:
                // No further informations provided, return no list
                return null;
        }

        return list;
    }
}
