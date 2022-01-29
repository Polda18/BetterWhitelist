package me.polda18.betterwhitelist.commands;

import me.polda18.betterwhitelist.BetterWhitelist;
import me.polda18.betterwhitelist.config.WhitelistEntry;
import me.polda18.betterwhitelist.utils.AlreadyInWhitelistException;
import me.polda18.betterwhitelist.utils.InvalidEntryException;
import me.polda18.betterwhitelist.utils.OnlineUUIDException;
import me.polda18.betterwhitelist.utils.UUIDGenerator;
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
     * Prints out the usage into console or chat, depending on where it was used. This is a private method.
     * @param sender Command sender parameter passed from a parent public method.
     */
    private void getUsage(CommandSender sender) {
        ArrayList<String> usage = (ArrayList<String>) plugin.getLanguage().getConfig().getStringList("messages.usage");

        for (String line :
                usage) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
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
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        Objects.requireNonNull(plugin.getLanguage().getConfig()
                                .getString("messages.error.permission"))));
                return true;        // Command sender has no requested permission to execute
            }

            // Just a command alone is sent - display usage
            this.getUsage(sender);
        } else {
            // Resolve subcommands and their arguments
            switch(args[0].toLowerCase()) {
                case "on":
                    // Check admin permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.admin")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.error.permission"))));
                    }

                    // Turn whitelist on
                    if(plugin.whitelistIsEnabled()) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.error.whitelist.already-enabled"))));
                    } else {
                        // Set the enabled flag in plugin structure to true
                        plugin.setWhitelistEnabled(true);

                        // Save config
                        plugin.getConfig().set("enabled", true);
                        plugin.saveConfig();

                        // Send message
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.enabled"))));
                    }
                    break;
                case "off":
                    // Check admin permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.admin")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.error.permission"))));
                        break;      // Command sender has no admin permission
                    }

                    // Turn whitelist off
                    if(plugin.whitelistIsEnabled()) {
                        // Set the enabled flag in plugin structure to false
                        plugin.setWhitelistEnabled(false);

                        // Save config
                        plugin.getConfig().set("enabled", false);
                        plugin.saveConfig();

                        // Send message
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.disabled"))));
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.error.whitelist.already-disabled"))));
                    }
                    break;
                case "status":
                    // Check status of the whitelist
                    if(!(sender instanceof ConsoleCommandSender)
                            && !sender.hasPermission("betterwhitelist.admin")) {
                        // Command sender has no admin permission to execute it
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.error.permission"))));
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.status.msg"))
                                        .replace("(status)", Objects.requireNonNull((plugin.whitelistIsEnabled())
                                                ? plugin.getLanguage().getConfig()
                                                        .getString("messages.status.enabled")
                                                : plugin.getLanguage().getConfig()
                                                        .getString("messages.status.disabled")))));
                    }
                    break;
                case "add":
                    // Check add permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.add")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.error.permission"))));
                        break;      // Command sender has no add permission to execute
                    }

                    // Add player to whitelist
                    if(args.length < 2) {
                        // Player not specified
                        this.getUsage(sender);
                    } else {
                        try {
                            WhitelistEntry entry = plugin.getWhitelist().addEntry(args[1]);

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
                                }
                            }
                        } catch (AlreadyInWhitelistException e) {
                            try {
                                WhitelistEntry entry = plugin.getWhitelist().getEntry(args[1]);

                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        plugin.getLanguage().getConfig()
                                                .getString("messages.error.already-in-whitelist")
                                                .replace("(player)", entry.getPlayer())));
                            } catch (IOException ex) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        Objects.requireNonNull(plugin.getLanguage().getConfig()
                                                .getString("messages.error.internal"))));
                                ex.printStackTrace();
                            }

                        } catch (OnlineUUIDException e) {
                            try {
                                WhitelistEntry entry = plugin.getWhitelist().getEntry(args[1]);

                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        Objects.requireNonNull(plugin.getLanguage().getConfig()
                                                .getString("messages.error.not-found.in-mojang"))
                                                .replace("(player)", entry.getPlayer())));
                            } catch (IOException ex) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        Objects.requireNonNull(plugin.getLanguage().getConfig()
                                                .getString("messages.error.internal"))));
                                ex.printStackTrace();
                            }
                        } catch (IOException e) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    Objects.requireNonNull(plugin.getLanguage().getConfig()
                                            .getString("messages.error.internal"))));
                            e.printStackTrace();
                        }
                    }
                    break;
                case "remove":
                    // Check remove permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.remove")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.error.permission"))));
                        break;      // Command sender has no remove permission to execute
                    }

                    // Remove player from whitelist
                    if(args.length < 2) {
                        // Player not specified
                        this.getUsage(sender);
                    } else {
                        // Remove from whitelist
                        try {
                            WhitelistEntry entry = plugin.getWhitelist().getEntry(args[1]);
                            plugin.getWhitelist().deleteEntry(entry.getPlayer());

                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    Objects.requireNonNull(plugin.getLanguage().getConfig()
                                            .getString("messages.removed"))
                                            .replace("(player)", entry.getPlayer())));
                        } catch (IOException e) {
                            // An internal error occured
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    Objects.requireNonNull(plugin.getLanguage().getConfig()
                                            .getString("messages.error.internal"))));
                            e.printStackTrace();
                        } catch (InvalidEntryException e) {
                            try {
                                WhitelistEntry entry = plugin.getWhitelist().getEntry(args[1]);

                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        Objects.requireNonNull(plugin.getLanguage().getConfig()
                                                .getString("messages.error.not-found.in-whitelist"))
                                                .replace("(player)", entry.getPlayer())));
                            } catch (IOException ex) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        Objects.requireNonNull(plugin.getLanguage().getConfig()
                                                .getString("messages.error.internal"))));
                                ex.printStackTrace();
                            }
                        }
                    }
                    break;
                case "reload":
                    // Check admin permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.admin")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.error.permission"))));
                        break;      // Command sender has no admin permission to execute
                    }

                    // Reload all plugin configuration files
                    plugin.reloadAllConfigs();

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            Objects.requireNonNull(plugin.getLanguage().getConfig()
                                    .getString("messages.reload"))));

                    break;
                case "list":
                    // Check list permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.list")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.error.permission"))));
                        break;      // Command sender has no list permission to execute
                    }

                    // List all players in whitelist
                    FileConfiguration config = plugin.getWhitelist().getConfig();
                    HashMap<String, HashMap<String, String>> hm = new HashMap<>();

                    for(String key : Objects.requireNonNull(config.getConfigurationSection("")).getKeys(false)) {
                        HashMap<String, String> entry = new HashMap<>();

                        entry.put("online_uuid", config.getString(key + ".online_uuid"));
                        entry.put("offline_uuid", config.getString(key + ".offline_uuid"));

                        hm.put(key, entry);
                    }

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            Objects.requireNonNull(plugin.getLanguage().getConfig()
                                    .getString("messages.list.header"))));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            Objects.requireNonNull(plugin.getLanguage().getConfig()
                                    .getString("messages.list.separator"))));

                    if(hm.isEmpty()) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.list.empty"))));
                    }

                    hm.forEach((key, value) -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            Objects.requireNonNull(plugin.getLanguage().getConfig()
                                    .getString("messages.list.line"))
                                    .replace("(player)", key)
                                    .replace("(mojang)", Objects.requireNonNull((value.get("online_uuid") != null)
                                            ? plugin.getLanguage().getConfig()
                                            .getString("messages.mojang-player.true")
                                            : plugin.getLanguage().getConfig()
                                            .getString("messages.mojang-player.false"))))));

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            Objects.requireNonNull(plugin.getLanguage().getConfig()
                                    .getString("messages.list.separator"))));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            Objects.requireNonNull(plugin.getLanguage().getConfig()
                                    .getString("messages.list.footer"))));
                    break;
                case "lang":
                    // Check admin permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.admin")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.error.permission"))));
                        break;      // Command sender has no admin permission to execute
                    }

                    // Check or change language
                    if(args.length < 2) {
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
                            plugin.setLanguage(args[1].toLowerCase());

                            // Save the config
                            plugin.getConfig().set("language", args[1]);
                            plugin.saveConfig();

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
                    break;
                case "import":
                    // Check admin permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.admin")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.error.permission"))));
                        break;      // Command sender has no admin permission to execute
                    }

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
                    break;
                default:
                    // Check any of requested permission
                    if(!isConsole && !sender.hasPermission("betterwhitelist.admin")
                            && !sender.hasPermission("betterwhitelist.add")
                            && !sender.hasPermission("betterwhitelist.remove")
                            && !sender.hasPermission("betterwhitelist.list")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(plugin.getLanguage().getConfig()
                                        .getString("messages.error.permission"))));
                        break;      // Command sender has no requested permission to execute
                    }

                    // Subcommand not recognized, display usage
                    this.getUsage(sender);
            }
        }

        return true;
    }
}
