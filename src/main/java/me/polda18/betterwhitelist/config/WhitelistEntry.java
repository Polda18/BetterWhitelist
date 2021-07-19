package me.polda18.betterwhitelist.config;

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

    public String getPlayer() {
        return player;
    }

    public UUID getOnlineUUID() {
        return online_uuid;
    }

    public UUID getOfflineUUID() {
        return offline_uuid;
    }
}
