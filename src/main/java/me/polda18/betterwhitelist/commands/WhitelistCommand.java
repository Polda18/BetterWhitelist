package me.polda18.betterwhitelist.commands;

import com.google.gson.*;
import com.sun.jdi.InternalException;
import me.polda18.betterwhitelist.BetterWhitelist;
import me.polda18.betterwhitelist.config.Whitelist;
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Stream;

public class WhitelistCommand implements CommandExecutor {
    private BetterWhitelist plugin;

    public WhitelistCommand(BetterWhitelist plugin) {
        this.plugin = plugin;
    }

    private void getUsage(CommandSender sender) {
        ArrayList<String> usage = (ArrayList<String>) plugin.getLanguage().getConfig().getStringList("messages.usage");

        for (String line :
                usage) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean isConsole = sender instanceof ConsoleCommandSender;

        if(!command.getName().equalsIgnoreCase("betterwhitelist")
                && !label.equalsIgnoreCase("betterwhitelist")
                && !label.equalsIgnoreCase("bw")) {
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
            this.getUsage(sender);
        } else {
            // Resolve subcommands and their arguments
            switch(args[0].toLowerCase()) {
                case "on":
                    // Turn whitelist on
                    if(plugin.whitelistIsEnabled()) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.getLanguage().getConfig().getString("messages.error.whitelist.already-enabled")));
                    } else {
                        // Set the enabled flag in plugin structure to true
                        plugin.setWhitelistEnabled(true);

                        // Save config
                        plugin.getConfig().set("enabled", true);
                        plugin.saveConfig();

                        // Send message
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.getLanguage().getConfig().getString("messages.enabled")));
                    }
                    break;
                case "off":
                    // Turn whitelist off
                    if(plugin.whitelistIsEnabled()) {
                        // Set the enabled flag in plugin structure to false
                        plugin.setWhitelistEnabled(false);

                        // Save config
                        plugin.getConfig().set("enabled", false);
                        plugin.saveConfig();

                        // Send message
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.getLanguage().getConfig().getString("messages.disabled")));
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.getLanguage().getConfig().getString("messages.error.whitelist.already-disabled")));
                    }
                    break;
                case "add":
                    // Add player to whitelist
                    if(args.length < 2) {
                        // Player not specified
                        this.getUsage(sender);
                    } else {
                        try {
                            WhitelistEntry entry = plugin.getWhitelist().addEntry(args[1]);

                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    plugin.getLanguage().getConfig().getString("messages.added")
                                            .replace("(player)", entry.getPlayer())));

                            if(!Bukkit.getOnlineMode()) {
                                boolean is_mojang_player = (entry.getOnlineUUID() != null);

                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        plugin.getLanguage().getConfig()
                                                .getString("messages.mojang-player.message")
                                                .replace("(mojang)",
                                                        (is_mojang_player)
                                                                ? plugin.getLanguage().getConfig()
                                                                        .getString("messages.mojang-player.true")
                                                                : plugin.getLanguage().getConfig()
                                                                        .getString("messages.mojang-player.false"))));

                                String mojang_player = UUIDGenerator.lookupMojangPlayerName(entry.getPlayer());

                                if(mojang_player != null) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                            plugin.getLanguage().getConfig()
                                                    .getString("messages.mojang-player.name")
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
                                        plugin.getLanguage().getConfig().getString("messages.error.internal")));
                                ex.printStackTrace();
                            }

                        } catch (OnlineUUIDException e) {
                            try {
                                WhitelistEntry entry = plugin.getWhitelist().getEntry(args[1]);

                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        plugin.getLanguage().getConfig()
                                                .getString("messages.error.not-found.in-mojang")
                                                .replace("(player)", entry.getPlayer())));
                            } catch (IOException ex) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        plugin.getLanguage().getConfig().getString("messages.error.internal")));
                                ex.printStackTrace();
                            }
                        } catch (IOException e) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    plugin.getLanguage().getConfig().getString("messages.error.internal")));
                            e.printStackTrace();
                        }
                    }
                    break;
                case "remove":
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
                                    plugin.getLanguage().getConfig().getString("messages.removed")
                                            .replace("(player)", entry.getPlayer())));
                        } catch (IOException e) {
                            // An internal error occured
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    plugin.getLanguage().getConfig().getString("messages.error.internal")));
                            e.printStackTrace();
                        } catch (InvalidEntryException e) {
                            try {
                                WhitelistEntry entry = plugin.getWhitelist().getEntry(args[1]);

                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        plugin.getLanguage().getConfig()
                                                .getString("messages.error.not-found.in-whitelist")
                                                .replace("(player)", entry.getPlayer())));
                            } catch (IOException ex) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        plugin.getLanguage().getConfig().getString("messages.error.internal")));
                                ex.printStackTrace();
                            }
                        }
                    }
                    break;
                case "reload":
                    plugin.reloadConfig();

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getLanguage().getConfig().getString("messages.reload")));

                    break;
                case "list":
                    FileConfiguration config = plugin.getWhitelist().getConfig();
                    HashMap<String, HashMap<String, String>> hm = new HashMap<>();

                    for(String key : config.getConfigurationSection("").getKeys(false)) {
                        HashMap<String, String> entry = new HashMap<>();

                        entry.put("online_uuid", config.getString(key + ".online_uuid"));
                        entry.put("offline_uuid", config.getString(key + ".offline_uuid"));

                        hm.put(key, entry);
                    }

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getLanguage().getConfig().getString("messages.list.header")));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getLanguage().getConfig().getString("messages.list.separator")));

                    if(hm.isEmpty()) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.getLanguage().getConfig().getString("messages.list.empty")));
                    }

                    hm.forEach((key, value) -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getLanguage().getConfig().getString("messages.list.line")
                                    .replace("(player)", key)
                                    .replace("(mojang)", (value.get("online_uuid") != null)
                                            ? plugin.getLanguage().getConfig()
                                                    .getString("messages.mojang-player.true")
                                            : plugin.getLanguage().getConfig()
                                                    .getString("messages.mojang-player.false")))));

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getLanguage().getConfig().getString("messages.list.separator")));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getLanguage().getConfig().getString("messages.list.footer")));
                    break;
                case "lang":
                    if(args.length < 2) {
                        // Language not specified, send current language
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.getLanguage().getConfig().getString("messages.language")
                                        .replace("(language)",
                                                plugin.getLanguage().getConfig().getString("name"))));
                    } else {
                        try {
                            // Set the plugin language
                            plugin.setLanguage(args[1].toLowerCase());

                            // Save the config
                            plugin.getConfig().set("language", args[1]);
                            plugin.saveConfig();

                            // Send message
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    plugin.getLanguage().getConfig().getString("messages.language")
                                            .replace("(language)",
                                                    plugin.getLanguage().getConfig().getString("name"))));
                        } catch (InvalidEntryException e) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    plugin.getLanguage().getConfig().getString("messages.error.language")));
                        }
                    }
                    break;
                case "import":
                    Set<OfflinePlayer> vanilla_whitelist = Bukkit.getWhitelistedPlayers();

                    for (OfflinePlayer player : vanilla_whitelist) {
                        try {
                            WhitelistEntry entry = plugin.getWhitelist().addEntry(player.getName());
                        } catch (OnlineUUIDException | AlreadyInWhitelistException | IOException e) {
                            e.printStackTrace();
                        }
                    }

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getLanguage().getConfig().getString("messages.import")));
                    break;
                default:
                    // Subcommand not recognized, display usage
                    this.getUsage(sender);
            }
        }

        return true;
    }
}
