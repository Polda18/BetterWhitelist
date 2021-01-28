package me.polda18.betterwhitelist.commands;

import com.google.gson.*;
import me.polda18.betterwhitelist.BetterWhitelist;
import me.polda18.betterwhitelist.utils.OnlineUUIDException;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.stream.Stream;

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
                    // Turn whitelist on
                    if(plugin.whitelistIsEnabled()) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.getLanguage().getConfig().getString("messages.error.whitelist.already-enabled")));
                    } else {
                        // Set the enabled flag in plugin structure
                        plugin.setWhitelistEnabled(true);

                        // Save config
                        plugin.getConfig().set("enabled", true);
                        plugin.saveConfig();
                    }
                    break;
                case "off":
                    // Turn whitelist off
                    if(plugin.whitelistIsEnabled()) {
                        // Set the enabled flag in plugin structure
                        plugin.setWhitelistEnabled(false);

                        // Save config
                        plugin.getConfig().set("enabled", false);
                        plugin.saveConfig();
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.getLanguage().getConfig().getString("messages.error.whitelist.already-disabled")));
                    }
                    break;
                case "add":
                    // Add player to whitelist
                    if(args.length < 2) {
                        // Player not specified
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.getLanguage().getConfig().getString("messages.usage")));
                    } else {
                        if(plugin.getWhitelist().getEntry(args[1]) == null) {
                            // Player is already in whitelist
                            String msg = plugin.getLanguage().getConfig().getString("messages.error.already-in-whitelist");
                            msg.replace("(player)", args[1]);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                        } else {
                            try {
                                // Add into whitelist
                                plugin.getWhitelist().addEntry(args[1]);

                                String player = args[1];
                                String uuid_online = plugin.getWhitelist().getEntry(args[1]).getOnline_uuid().toString();
                                String uuid_offline = plugin.getWhitelist().getEntry(args[1]).getOffline_uuid().toString();

                                String player_msg = plugin.getLanguage().getConfig().getString("messages.added");
                                player_msg.replace("(player)", player);

                                String uuid_online_msg = plugin.getLanguage().getConfig().getString("messages.uuid.online");
                                uuid_online_msg.replace("(uuid)", uuid_online);
                                String uuid_offline_msg = plugin.getLanguage().getConfig().getString("messages.uuid.offline");
                                uuid_offline_msg.replace("(uuid)", uuid_offline);

                                // Save whitelist into JSON data
                                File file = new File(plugin.getDataFolder(), "whitelist.json");
                                StringBuilder json_builder = new StringBuilder();
                                if(!file.exists()) {
                                    file.createNewFile();
                                    json_builder.append("[]").append("\n");
                                } else {
                                    try (Stream<String> stream = Files.lines(file.toPath(), StandardCharsets.UTF_8)) {
                                        stream.forEach(s -> json_builder.append(s).append("\n"));
                                    } catch (IOException e) {
                                        // An exception occured
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                                plugin.getLanguage().getConfig().getString("messages.error.internal")));
                                        e.printStackTrace();
                                    }
                                }

                                JsonParser parser = new JsonParser();
                                JsonElement whitelist = parser.parse(json_builder.toString());
                                if(!(whitelist instanceof JsonArray)) {
                                    throw new JsonParseException("Loaded whitelist is in incorrect format");
                                }

                                JsonObject definition = new JsonObject();
                                definition.addProperty("online_uuid", uuid_online);
                                definition.addProperty("offline_uuid", uuid_offline);
                                JsonObject person = new JsonObject();
                                person.add(player, definition);

                                ((JsonArray) whitelist).add(person);
                                Files.writeString(file.toPath(), whitelist.toString());
                            } catch (IOException e) {
                                // An internal error occured
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        plugin.getLanguage().getConfig().getString("messages.error.internal")));
                                e.printStackTrace();
                            } catch (OnlineUUIDException e) {
                                // Server in online mode, but provided player isn't in Mojang's database
                                String msg = plugin.getLanguage().getConfig().getString("messages.error.not-found.in-mojang");
                                msg.replace("(player)", args[1]);
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                            } catch (JsonParseException e) {
                                // Loaded whitelist is in incorrect format
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        plugin.getLanguage().getConfig().getString("messages.error.parse")));
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case "remove":
                    // TODO: Remove player from whitelist
                    if(args.length < 2) {
                        // Player not specified
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.getLanguage().getConfig().getString("messages.usage")));
                    } else {
                        // TODO: remove player from whitelist
                    }
                default:
                    // Subcommand not recognized, display usage
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getLanguage().getConfig().getString("messages.usage")));
            }
        }

        return true;
    }
}
