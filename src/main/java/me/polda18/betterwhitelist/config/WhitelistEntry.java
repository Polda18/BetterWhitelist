package me.polda18.betterwhitelist.config;

import java.util.UUID;

/**
 * Wrapper for whitelist entry specification - makes it easier to maintain and group
 * all the necessary informations about the whitelisted player
 */
public class WhitelistEntry {
    private String player;
    private UUID online_uuid;
    private UUID offline_uuid;

    /**
     * Constructor: creates this entry from specified arguments
     * @param player Specified player
     * @param online_uuid Their online UUID
     * @param offline_uuid Their offline UUID
     */
    public WhitelistEntry(String player, UUID online_uuid, UUID offline_uuid) {
        this.player = player;
        this.online_uuid = online_uuid;
        this.offline_uuid = offline_uuid;
    }

    /**
     * Get the specified player
     * @return Stored player name
     */
    public String getPlayer() {
        return player;
    }

    /**
     * Get the specified player's online UUID
     * @return Stored player's online UUID
     */
    public UUID getOnlineUUID() {
        return online_uuid;
    }

    /**
     * Get the specified player's offline UUID
     * @return Stored player's offline UUID
     */
    public UUID getOfflineUUID() {
        return offline_uuid;
    }
}
