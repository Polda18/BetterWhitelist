package me.polda18.betterwhitelist.config;

import com.sun.jdi.InternalException;
import me.polda18.betterwhitelist.BetterWhitelist;
import me.polda18.betterwhitelist.utils.InvalidEntryException;
import me.polda18.betterwhitelist.utils.OnlineUUIDException;
import me.polda18.betterwhitelist.utils.UUIDConverter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import javax.management.InstanceAlreadyExistsException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class Whitelist {
    private static Whitelist instance = null;

    private BetterWhitelist plugin;
    private File file;
    private FileConfiguration config;

    public Whitelist(BetterWhitelist plugin, File file, FileConfiguration config) throws InstanceAlreadyExistsException {
        if(Whitelist.instance == null) {
            this.plugin = plugin;
            this.file = file;
            this.config = config;

            Whitelist.instance = this;
        } else {
            throw new InstanceAlreadyExistsException("No more instances allowed");
        }
    }

    public File getFile() {
        return this.file;
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public WhitelistEntry getEntry(String player) {
        String online_uuid_s = this.config.getString(player + ".online_uuid");
        String offline_uuid_s = this.config.getString(player + ".offline_uuid");

        UUID online_uuid = (online_uuid_s != null) ? UUID.fromString(online_uuid_s) : null;
        UUID offline_uuid = (offline_uuid_s != null) ? UUID.fromString(offline_uuid_s) : null;

        return new WhitelistEntry(player, online_uuid, offline_uuid);
    }

    public void deleteEntry(String player) throws InvalidEntryException {
        WhitelistEntry entry = this.getEntry(player);

        if(entry.getOnlineUUID() == null && entry.getOfflineUUID() == null) {
            throw new InvalidEntryException("Specified player not found in whitelist");
        }

        this.config.set(player, null);
    }

    public void addEntry(String player) throws OnlineUUIDException, IOException {
        UUID online_uuid = UUIDConverter.getOnlineUUIDFromPlayerName(player);       // Online UUID is always fetched
        UUID offline_uuid = UUIDConverter.getOfflineUUIDFromPlayerName(player);

        if(online_uuid == null && Bukkit.getOnlineMode()) {
            throw new OnlineUUIDException("Player not found in Mojang user database");
        }

        this.config.set(player + ".online_uuid", (online_uuid != null) ? online_uuid.toString() : null);
        this.config.set(player + ".offline_uuid", offline_uuid.toString());
    }
}
