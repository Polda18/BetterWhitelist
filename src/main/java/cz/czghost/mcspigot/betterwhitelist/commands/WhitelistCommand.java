package cz.czghost.mcspigot.betterwhitelist.commands;

import cz.czghost.mcspigot.betterwhitelist.BetterWhitelist;
import cz.czghost.mcspigot.betterwhitelist.utils.*;
import cz.czghost.mcspigot.betterwhitelist.config.WhitelistEntry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Command handler to handle command execution
 */
public class WhitelistCommand implements CommandExecutor {
    private BetterWhitelist plugin;

    /**
     * Constructor: creates this command handler to be registered within the plugin
     * @param plugin Plugin instance used for access to configuration
     */
    public WhitelistCommand(BetterWhitelist plugin) {
        this.plugin = plugin;
    }

    /**
     * Prints out a permission error message
     * @param sender Who sent this command
     */
    private void noPermissionMessage(CommandSender sender) {
        // Command sender has no permission to run this command
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(plugin.getLanguage().getConfig()
                        .getString("messages.error.permission"))));
    }

    /**
     * Prints out an internal error message
     * @param sender Who sent this command
     */
    private void internalErrorMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(plugin.getLanguage().getConfig()
                        .getString("messages.error.internal"))));
    }

    /**
     * Prints out the usage into console or chat, depending on where it was used. This is a private method.
     * @param sender Who sent this command
     */
    private void getUsage(CommandSender sender) {
        ArrayList<String> usage = (ArrayList<String>) plugin.getLanguage().getConfig().getStringList("messages.usage");

        for (String line :
                usage) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }

    /**
     * Whitelist on subcommand
     * @param sender Who sent this command
     */
    private void subcommandOn(CommandSender sender) {
        // Turn whitelist on
        if(plugin.whitelistIsEnabled()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    Objects.requireNonNull(plugin.getLanguage().getConfig()
                            .getString("messages.error.whitelist.already-enabled"))));
        } else {
            // Set the enabled flag in plugin structure to true and save config
            plugin.setWhitelistEnabled(true);

            // Send message
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    Objects.requireNonNull(plugin.getLanguage().getConfig()
                            .getString("messages.enabled"))));
        }
    }

    /**
     * Whitelist off subcommand
     * @param sender Who sent this command
     */
    private void subcommandOff(CommandSender sender) {
        // Turn whitelist off
        if(plugin.whitelistIsEnabled()) {
            // Set the enabled flag in plugin structure to false and save config
            plugin.setWhitelistEnabled(false);

            // Send message
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    Objects.requireNonNull(plugin.getLanguage().getConfig()
                            .getString("messages.disabled"))));
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    Objects.requireNonNull(plugin.getLanguage().getConfig()
                            .getString("messages.error.whitelist.already-disabled"))));
        }
    }

    /**
     * Status subcommand
     * @param sender Who sent this command
     */
    private void subcommandStatus(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                .getString("messages.status.msg"))
                        .replace("(status)", Objects.requireNonNull((plugin.whitelistIsEnabled())
                                ? plugin.getLanguage().getConfig()
                                .getString("messages.status.enabled")
                                : plugin.getLanguage().getConfig()
                                .getString("messages.status.disabled")))));
    }

    /**
     * Add subcommand
     * @param sender Who sent this command
     * @param args   Additional arguments
     */
    private void subcommandAdd(CommandSender sender, String[] args) {
        // Check arguments
        if(args.length < 1) {
            // Player not specified
            this.getUsage(sender);
        } else {
            for(String player : args) {
                // Adds each player specified
                try {
                    WhitelistEntry entry = plugin.getWhitelist().addEntry(player);

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            Objects.requireNonNull(plugin.getLanguage().getConfig()
                                            .getString("messages.added"))
                                    .replace("(player)", entry.getPlayer())));

                    if(!Bukkit.getOnlineMode()) {
                        boolean is_mojang_player = (entry.getOnlineUUID() != null);

                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                                .getString("messages.mojang-player.message"))
                                        .replace("(mojang)",
                                                Objects.requireNonNull((is_mojang_player)
                                                        ? plugin.getLanguage().getConfig()
                                                        .getString("messages.mojang-player.true")
                                                        : plugin.getLanguage().getConfig()
                                                        .getString("messages.mojang-player.false")))));

                        String mojang_player = UUIDGenerator.lookupMojangPlayerName(entry.getPlayer());

                        if(mojang_player != null) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    Objects.requireNonNull(plugin.getLanguage().getConfig()
                                                    .getString("messages.mojang-player.name"))
                                            .replace("(player)", mojang_player)));
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    Objects.requireNonNull(plugin.getLanguage().getConfig()
                                            .getString("messages.mojang-player.disclaimer"))));
                        }
                    }
                } catch (AlreadyInWhitelistException e) {
                    try {
                        WhitelistEntry entry = plugin.getWhitelist().getEntry(player);

                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                                .getString("messages.error.already-in-whitelist"))
                                        .replace("(player)", entry.getPlayer())));
                    } catch (IOException ex) {
                        internalErrorMessage(sender);
                        ex.printStackTrace();
                    }
                } catch (OnlineUUIDException e) {
                    try {
                        WhitelistEntry entry = plugin.getWhitelist().getEntry(player);

                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                                .getString("messages.error.not-found.in-mojang"))
                                        .replace("(player)", entry.getPlayer())));
                    } catch (IOException ex) {
                        internalErrorMessage(sender);
                        ex.printStackTrace();
                    }
                } catch (RatelimitException e) {
                    int retryAfter = e.getRetryAfter();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            Objects.requireNonNull(plugin.getLanguage().getConfig()
                                            .getString("messages.error.ratelimit"))
                                    .replace("(retry_time)", String.valueOf(retryAfter))));
                    e.printStackTrace();
                } catch (Exception e) {
                    internalErrorMessage(sender);
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Remove subcommand
     * @param sender Who sent this command
     * @param args   Additional arguments
     */
    private void subcommandRemove(CommandSender sender, String[] args) {
        // Check arguments
        if(args.length < 1) {
            // Player not specified
            this.getUsage(sender);
        } else {
            for(String player : args) {
                // Remove from whitelist
                try {
                    WhitelistEntry entry = plugin.getWhitelist().getEntry(player);
                    plugin.getWhitelist().deleteEntry(entry.getPlayer());

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            Objects.requireNonNull(plugin.getLanguage().getConfig()
                                            .getString("messages.removed"))
                                    .replace("(player)", entry.getPlayer())));
                } catch (IOException e) {
                    // An internal error occured
                    internalErrorMessage(sender);
                    e.printStackTrace();
                } catch (InvalidEntryException e) {
                    try {
                        WhitelistEntry entry = plugin.getWhitelist().getEntry(player);

                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                                .getString("messages.error.not-found.in-whitelist"))
                                        .replace("(player)", entry.getPlayer())));
                    } catch (IOException ex) {
                        internalErrorMessage(sender);
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Reload subcommand
     * @param sender Who sent this command
     */
    private void subcommandReload(CommandSender sender) {
        // Reload all plugin configuration files
        plugin.reloadAllConfigs();

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(plugin.getLanguage().getConfig()
                        .getString("messages.reload"))));

    }

    /**
     * List subcommand
     * @param sender   Who sent this command
     * @param argument Additional argument
     */
    private void subcommandList(CommandSender sender, String argument) {
        // Get all players in whitelist
        FileConfiguration config = plugin.getWhitelist().getConfig();
        //HashMap<String, HashMap<String, String>> hm = new HashMap<>();
        List<String> playerlist = new ArrayList<>(config.getKeys(false).stream().toList());
        int maxpages = (int) Math.ceil(playerlist.size() / 5.0);

        // Sort all players by alphabet
        playerlist.sort(Comparator.naturalOrder());

        // Retrieve page number
        int page = (argument.startsWith("p:")) ? Integer.parseInt(argument.substring(2)) : 1;

        // Print out header
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(plugin.getLanguage().getConfig()
                        .getString("messages.list.header"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(plugin.getLanguage().getConfig()
                        .getString("messages.list.separator"))));

        // Check for empty list
        if(playerlist.isEmpty()) {
            // Player list is empty
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    Objects.requireNonNull(plugin.getLanguage().getConfig()
                            .getString("messages.list.empty"))));
            return;
        }

        // Check if page number exceeds the max number of pages -> trim to last page if it does
        if(page > maxpages) page = maxpages;

        // List players in whitelist by current page
        for(int i = 5 * (page - 1); i < Math.min(5 * page, playerlist.size()); ++i) {
            // Get one player
            String player = playerlist.get(i);

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    Objects.requireNonNull(plugin.getLanguage().getConfig()
                                    .getString("messages.list.line"))
                                            .replace("(player)", player)
                                            .replace("(mojang)",
                                                Objects.requireNonNull(
                                                        (config.getString(player + ".online_uuid") != null)
                                                        ? plugin.getLanguage().getConfig()
                                                                .getString("messages.mojang-player.true")
                                                        : plugin.getLanguage().getConfig()
                                                                .getString("messages.mojang-player.false")))));
        }

        // Print out footer
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(plugin.getLanguage().getConfig()
                        .getString("messages.list.separator"))));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(plugin.getLanguage().getConfig()
                        .getString("messages.list.footer"))
                        .replace("(page)", String.valueOf(page))
                        .replace("(maxpage)", String.valueOf(maxpages))));
    }

    /**
     * Language subcommand
     * @param sender   Who sent this command
     * @param argument Additional argument
     */
    private void subcommandLang(CommandSender sender, String argument) {
        // Check arguments
        if(argument.isEmpty()) {
            // Language not specified, send current language
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    Objects.requireNonNull(plugin.getLanguage().getConfig()
                                    .getString("messages.language"))
                            .replace("(language)",
                                    Objects.requireNonNull(plugin.getLanguage().getConfig()
                                            .getString("name")))));
        } else {
            try {
                // Set the plugin language
                plugin.setLanguage(argument.toLowerCase());

                // Send message
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.language"))
                                .replace("(language)",
                                        Objects.requireNonNull(plugin.getLanguage().getConfig()
                                                .getString("name")))));
            } catch (InvalidEntryException e) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        Objects.requireNonNull(plugin.getLanguage().getConfig()
                                .getString("messages.error.language"))));
            }
        }
    }

    /**
     * Import subcommand
     * @param sender Who sent this command
     */
    private void subcommandImport(CommandSender sender) {
        // Import data from vanilla whitelist
        Set<OfflinePlayer> vanilla_whitelist = Bukkit.getWhitelistedPlayers();

        for (OfflinePlayer player : vanilla_whitelist) {
            try {
                WhitelistEntry entry = plugin.getWhitelist().addEntry(player.getName());
                plugin.getLogger().log(Level.INFO, ChatColor.translateAlternateColorCodes('&',
                        Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.import_player"))
                                .replace("(player)", entry.getPlayer())));
            } catch (OnlineUUIDException | AlreadyInWhitelistException | IOException e) {
                e.printStackTrace();
            }
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(plugin.getLanguage().getConfig()
                        .getString("messages.import"))));
    }

    /**
     * Version subcommand
     * @param sender Who sent this command
     */
    private void subcommandVersion(CommandSender sender) {
        // Send message with version
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(Objects.requireNonNull(plugin.getLanguage().getConfig()
                                .getString("messages.version"))
                        .replace("(version)",
                                plugin.getDescription().getVersion()))));

    }

    /**
     * Event listener for command execution
     * @param sender Command sender parameter containing informations necessary to figure out further actions
     * @param command Command definition itself
     * @param label Label of the command or alias
     * @param args Arguments list
     * @return Suppresses the command printing to chat
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, @NotNull String[] args) {
        boolean isConsole = sender instanceof ConsoleCommandSender;

        if(!command.getName().equalsIgnoreCase("whitelist")
                && !label.equalsIgnoreCase("whitelist")
                && !label.equalsIgnoreCase("wl")) {
            // Executed command isn't for this plugin
            return true;
        }

        if(args.length < 1) {
            // Check any requested permission
            if(!isConsole && !sender.hasPermission("betterwhitelist.admin")
                    && !sender.hasPermission("betterwhitelist.add")
                    && !sender.hasPermission("betterwhitelist.remove")
                    && !sender.hasPermission("betterwhitelist.list")) {
                noPermissionMessage(sender);
                return true;        // Command sender has no requested permission to execute
            }

            // Just a command alone is sent - display usage
            this.getUsage(sender);
        } else {
            // Resolve subcommands and their arguments
            switch(args[0].toLowerCase()) {
                case "on":      // Enable whitelist
                    // Check admin permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.admin")) {
                        noPermissionMessage(sender);
                    }

                    // No arguments, execute
                    subcommandOn(sender);
                    break;
                case "off":     // Disable whitelist
                    // Check admin permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.admin")) {
                        noPermissionMessage(sender);
                        break;      // Command sender has no admin permission
                    }

                    // No arguments, execute
                    subcommandOff(sender);
                    break;
                case "status":      // Check whitelist enabled/disabled status
                    // Check admin permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.admin")) {
                        noPermissionMessage(sender);
                        break;      // Command sender has no admin permission
                    }

                    // No arguments, execute
                    subcommandStatus(sender);
                    break;
                case "add":     // Add player to whitelist
                    // Check add permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.add")) {
                        noPermissionMessage(sender);
                        break;      // Command sender has no add permission to execute
                    }

                    // Retrieve arguments for this subcommand
                    String[] playerlist_to_add = new String[args.length - 1];
                    for(int i = 0; i < args.length - 1; ++i) {
                        playerlist_to_add[i] = args[i + 1];
                    }

                    // Execute
                    subcommandAdd(sender, playerlist_to_add);
                    break;
                case "remove":      // Remove player from whitelist
                    // Check remove permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.remove")) {
                        noPermissionMessage(sender);
                        break;      // Command sender has no remove permission to execute
                    }

                    // Retrieve arguments for this subcommand
                    String[] playerlist_to_remove = new String[args.length - 1];
                    for(int i = 0; i < args.length - 1; ++i) {
                        playerlist_to_remove[i] = args[i + 1];
                    }

                    // Execute
                    subcommandRemove(sender, playerlist_to_remove);
                    break;
                case "reload":      // Reload all config files
                    // Check admin permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.admin")) {
                        noPermissionMessage(sender);
                        break;      // Command sender has no admin permission to execute
                    }

                    // No arguments, execute
                    subcommandReload(sender);
                    break;
                case "list":        // List players in whitelist
                    // Check list permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.list")) {
                        noPermissionMessage(sender);
                        break;      // Command sender has no list permission to execute
                    }

                    // Retrieve additional argument
                    String argument = (args.length < 2) ? "p:1" : args[1];

                    // Execute
                    subcommandList(sender, argument);
                    break;
                case "lang":        // Check or change language
                    // Check admin permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.admin")) {
                        noPermissionMessage(sender);
                        break;      // Command sender has no admin permission to execute
                    }

                    // Execute with arguments given
                    subcommandLang(sender, (args.length < 2) ? "" : args[1]);
                    break;
                case "import":      // Import players from vanilla whitelist
                    // Check admin permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.admin")) {
                        noPermissionMessage(sender);
                        break;      // Command sender has no admin permission to execute
                    }

                    // No arguments, execute
                    subcommandImport(sender);
                    break;
                case "version":     // Display plugin version
                    // Check admin permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.admin")) {
                        noPermissionMessage(sender);
                        break;      // Command sender has no admin permission to execute
                    }

                    // No arguments, execute
                    subcommandVersion(sender);
                    break;
                default:
                    // Check any of requested permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.admin")
                            && !sender.hasPermission("betterwhitelist.add")
                            && !sender.hasPermission("betterwhitelist.remove")
                            && !sender.hasPermission("betterwhitelist.list")) {
                        noPermissionMessage(sender);
                        break;      // Command sender has no requested permission to execute
                    }

                    // Subcommand not recognized, display usage
                    this.getUsage(sender);
            }
        }

        return true;
    }
}
