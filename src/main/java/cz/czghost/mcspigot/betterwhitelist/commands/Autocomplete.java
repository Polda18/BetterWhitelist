package cz.czghost.mcspigot.betterwhitelist.commands;

import cz.czghost.mcspigot.betterwhitelist.BetterWhitelist;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            // Since this plugin is a single-purpose, only one command belongs in here.
        }

        if(args.length > 1) {
            switch (args[0].toLowerCase()) {
                case "add":         // User wants to add a player to whitelist
                    if (isConsole || sender.hasPermission("betterwhitelist.add")) {
                        if (args[args.length - 1].isEmpty())
                            list.add("ExamplePlayer");      // Give an example player :)
                        else list.add(args[args.length - 1]);
                    } else {
                        return null;
                    }
                    break;
                case "remove":      // User wants to remove player from whitelist
                    if (isConsole || sender.hasPermission("betterwhitelist.remove")) {
                        // Get all whitelisted players
                        list.addAll(plugin.getWhitelist().getConfig().getKeys(false));

                        list = (ArrayList<String>) list.stream()
                                .filter(s -> s.startsWith(args[args.length - 1])).collect(Collectors.toList());
                    } else {
                        return null;
                    }
                    break;
                case "lang":        // User wants to view or change set language
                    // Give a list of available languages
                    if (isConsole || sender.hasPermission("betterwhitelist.admin")) {
                        list.addAll(plugin.listAvailableLanguages().keySet());

                        list = (ArrayList<String>) list.stream()
                                .filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
                    } else {
                        return null;
                    }
                    break;
                case "list":        // User wants to list players
                    // Provide a page number specification
                    if (isConsole || sender.hasPermission("betterwhitelist.list")) {
                        list.add("p:");

                        list = (ArrayList<String>) list.stream()
                                .filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
                    } else {
                        return null;
                    }
                    break;
                default:            // User entered invalid subcommand or subcommand has no arguments
                    // Invalid subcommand provided or subcommand has no arguments, default to no list given
                    return null;
            }
        } else {
            if (isConsole || sender.hasPermission("betterwhitelist.admin")) {
                // Add to list only if command sender has admin permissions
                list.add("on");
                list.add("off");
                list.add("reload");
                list.add("lang");
                list.add("import");
                list.add("status");
                list.add("version");
            }
            if (isConsole || sender.hasPermission("betterwhitelist.add"))
                list.add("add");        // Add to list only if command sender has add permission
            if (isConsole || sender.hasPermission("betterwhitelist.remove"))
                list.add("remove");     // Add to list only if command sender has remove permission
            if (isConsole || sender.hasPermission("betterwhitelist.list"))
                list.add("list");       // Add to list only if command sender has list permission
            if (list.isEmpty()) {
                return null;            // If the list is empty (sender has no permission), no list is given
            }

            // List is not empty, filter through start of the argument
            list = (ArrayList<String>) list.stream()
                    .filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        }

        return list;
    }
}
