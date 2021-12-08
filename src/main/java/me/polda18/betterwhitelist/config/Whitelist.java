package me.polda18.betterwhitelist.config;

import me.polda18.betterwhitelist.BetterWhitelist;
import me.polda18.betterwhitelist.utils.AlreadyInWhitelistException;
import me.polda18.betterwhitelist.utils.InvalidEntryException;
import me.polda18.betterwhitelist.utils.OnlineUUIDException;
import me.polda18.betterwhitelist.utils.UUIDGenerator;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import javax.management.InstanceAlreadyExistsException;
import java.io.File;
import java.io.IOException;
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

    public WhitelistEntry getEntry(String player) throws IOException {
        String mojang_player = UUIDGenerator.lookupMojangPlayerName(player);

        if(Bukkit.getOnlineMode() && mojang_player != null) {
            player = mojang_player;     // Server in online mode and player is in Mojang database
        }

        String online_uuid_s = this.config.getString(player + ".online_uuid");
        String offline_uuid_s = this.config.getString(player + ".offline_uuid");

        UUID online_uuid = (online_uuid_s != null) ? UUID.fromString(online_uuid_s) : null;
        UUID offline_uuid = (offline_uuid_s != null) ? UUID.fromString(offline_uuid_s) : null;

        return new WhitelistEntry(player, online_uuid, offline_uuid);
    }

    public void deleteEntry(String player) throws InvalidEntryException, IOException {
        String mojang_player = UUIDGenerator.lookupMojangPlayerName(player);

        if(Bukkit.getOnlineMode() && mojang_player != null) {
            player = mojang_player;     // Server in online mode and player is in Mojang database
        }

        WhitelistEntry entry = this.getEntry(player);

        if(entry.getOnlineUUID() == null && entry.getOfflineUUID() == null) {
            throw new InvalidEntryException("Specified player not found in whitelist");
        }

        this.config.set(player, null);
        this.config.save(this.file);
    }

    public WhitelistEntry addEntry(String player) throws OnlineUUIDException, IOException, AlreadyInWhitelistException {
        String mojang_player = UUIDGenerator.lookupMojangPlayerName(player);    // Get the correct player name
        UUID online_uuid = UUIDGenerator.lookupMojangPlayerUUID(player);        // Online UUID is always fetched

        if(online_uuid == null && Bukkit.getOnlineMode()) {
            throw new OnlineUUIDException("Player not found in Mojang user database");
        }

        if(Bukkit.getOnlineMode() && mojang_player != null) {
            player = mojang_player;     // If in online mode and Mojang user found, use as player name
        }

        UUID offline_uuid = UUIDGenerator.generateOfflineUUIDFromPlayerName(mojang_player);

        WhitelistEntry entry = this.getEntry(player);
        if(entry.getOnlineUUID() != null || entry.getOfflineUUID() != null) {
            throw new AlreadyInWhitelistException("Player is already in whitelist");
        }

        this.config.set(player + ".online_uuid", (online_uuid != null) ? online_uuid.toString() : null);
        this.config.set(player + ".offline_uuid", offline_uuid.toString());
        this.config.save(this.file);

        return this.getEntry(player);
    }
}
