package me.polda18.betterwhitelist.config;

import me.polda18.betterwhitelist.BetterWhitelist;
import me.polda18.betterwhitelist.utils.OnlineUUIDException;
import me.polda18.betterwhitelist.utils.UUIDConverter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import javax.management.InstanceAlreadyExistsException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class Whitelist {
    private static Whitelist instance = null;

    private BetterWhitelist plugin;
    private File file;
    private FileConfiguration config;
    private HashMap<String, WhitelistEntry> hash_map;

    public Whitelist(BetterWhitelist plugin, File file, FileConfiguration config) throws InstanceAlreadyExistsException {
        if(Whitelist.instance == null) {
            this.plugin = plugin;
            this.file = file;
            this.config = config;
            this.hash_map = new HashMap<>();

            Whitelist.instance = this;
        } else {
            throw new InstanceAlreadyExistsException("No more instances allowed");
        }
    }

    public File getFile() {
        return this.file;
    }

    public FileConfiguration getConfig() {
        return this.getConfig();
    }

    public WhitelistEntry getEntry(String player) {
        return this.hash_map.get(player);
    }

    public boolean deleteEntry(String player) {
        return (this.hash_map.remove(player) != null);
    }

    public boolean addEntry(String player) throws OnlineUUIDException, IOException {
        UUID online_uuid = (Bukkit.getOnlineMode()) ? UUIDConverter.getOnlineUUIDFromPlayerName(player) : null;
        UUID offline_uuid = UUIDConverter.getOfflineUUIDFromPlayerName(player);

        if(online_uuid == null && Bukkit.getOnlineMode()) {
            throw new OnlineUUIDException("Player not found in Mojang user database");
        }

        return (this.hash_map.put(player, new WhitelistEntry(player, online_uuid, offline_uuid)) != null);
    }
}
