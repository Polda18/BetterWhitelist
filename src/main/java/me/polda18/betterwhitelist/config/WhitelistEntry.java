package me.polda18.betterwhitelist.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class WhitelistEntry {
    String player;
    UUID online_uuid;
    UUID offline_uuid;

    public WhitelistEntry(String player, UUID online_uuid, UUID offline_uuid) {
        this.player = player;
        this.online_uuid = online_uuid;
        this.offline_uuid = offline_uuid;
    }
}
