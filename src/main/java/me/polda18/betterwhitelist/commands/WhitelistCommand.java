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
import java.util.ArrayList;
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
            this.getUsage(sender);
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
                        this.getUsage(sender);
                    } else {
                        if(plugin.getWhitelist().getEntry(args[1]) != null) {
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
                                    json_builder.append("{}").append("\n");
                                } else {
                                    Stream<String> stream = Files.lines(file.toPath(), StandardCharsets.UTF_8);
                                    stream.forEach(s -> json_builder.append(s).append("\n"));
                                }

                                JsonParser parser = new JsonParser();
                                JsonElement whitelist = parser.parse(json_builder.toString());
                                if(!(whitelist instanceof JsonObject)) {
                                    throw new JsonParseException("Loaded whitelist is in incorrect format");
                                }

                                JsonObject definition = new JsonObject();
                                definition.addProperty("online_uuid", uuid_online);
                                definition.addProperty("offline_uuid", uuid_offline);
                                ((JsonObject) whitelist).add(player, definition);

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
                    // Remove player from whitelist
                    if(args.length < 2) {
                        // Player not specified
                        this.getUsage(sender);
                    } else {
                        // Remove from whitelist
                        if(plugin.getWhitelist().getEntry(args[1]) == null) {
                            // Player specified wasn't found in the whitelist
                            String msg = plugin.getLanguage().getConfig().getString("messages.error.not-found.in-whitelist");
                            msg.replace("(player)", args[1]);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                        } else {
                            try {
                                // Remove from whitelist
                                plugin.getWhitelist().deleteEntry(args[1]);

                                String player_msg = plugin.getLanguage().getConfig().getString("messages.added");
                                player_msg.replace("(player)", args[1]);

                                // Save whitelist into JSON data
                                File file = new File(plugin.getDataFolder(), "whitelist.json");
                                StringBuilder json_builder = new StringBuilder();

                                if(!file.exists()) {
                                    throw new IOException("Whitelist JSON file is missing!");
                                } else {
                                    Stream<String> stream = Files.lines(file.toPath(), StandardCharsets.UTF_8);
                                    stream.forEach(s -> json_builder.append(s).append("\n"));
                                }

                                JsonParser parser = new JsonParser();
                                JsonElement whitelist = parser.parse(json_builder.toString());
                                if(!(whitelist instanceof JsonObject)) {
                                    throw new JsonParseException("Loaded whitelist is in incorrect format");
                                }

                                ((JsonObject) whitelist).remove(args[1]);
                            } catch (IOException e) {
                                // An internal error occured
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        plugin.getLanguage().getConfig().getString("messages.error.internal")));
                                e.printStackTrace();
                            } catch (JsonParseException e) {
                                // Loaded whitelist is in incorrect format
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        plugin.getLanguage().getConfig().getString("messages.error.parse")));
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case "reload":
                    // TODO: Reload plugin config and whitelist
                    break;
                default:
                    // Subcommand not recognized, display usage
                    this.getUsage(sender);
            }
        }

        return true;
    }
}
