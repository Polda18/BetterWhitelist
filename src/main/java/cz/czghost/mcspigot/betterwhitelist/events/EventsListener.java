package cz.czghost.mcspigot.betterwhitelist.events;

import cz.czghost.mcspigot.betterwhitelist.BetterWhitelist;
import cz.czghost.mcspigot.betterwhitelist.utils.AlreadyInWhitelistException;
import cz.czghost.mcspigot.betterwhitelist.utils.InvalidEntryException;
import cz.czghost.mcspigot.betterwhitelist.utils.OnlineUUIDException;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Core function of this plugin - event listener for player connecting
 */
public class EventsListener implements Listener {
    private BetterWhitelist plugin;

    /**
     * Constructor: creates this event listener for registration withing the plugin
     * @param plugin Plugin instance that is used within the handler for access to configuration
     */
    public EventsListener(BetterWhitelist plugin) {
        this.plugin = plugin;
    }

    /**
     * Event that is fired when player joins the server
     * @param event The event handle containing informations necessary for the function
     * @throws IOException Fired when there was an input/output error
     * @throws AlreadyInWhitelistException Fired when the player was somehow found in whitelist (online mode update, failsafe mechanism)
     * @throws OnlineUUIDException Fired when an online UUID couldn't be found (online mode update, failsafe mechanism)
     * @throws InvalidEntryException Fired when specified old player wasn't found in the whitelist (online mode update, failsafe mechanism)
     */
    @EventHandler
    public void onPlayerConnect(PlayerLoginEvent event) throws IOException, AlreadyInWhitelistException, OnlineUUIDException, InvalidEntryException {
        // Check if the server runs in an online mode or not
        boolean online_mode = Bukkit.getOnlineMode();

        // Check if the whitelist is enabled
        if(!plugin.whitelistIsEnabled()) {
            return;         // Do nothing if whitelist isn't enabled
        }

        // Get the player and his UUID
        UUID uuid_joined = event.getPlayer().getUniqueId();
        String player = event.getPlayer().getName();
        String uuid_whitelisted = plugin.getWhitelist().getConfig()
                .getString(player + ((online_mode) ? ".online_uuid" : ".offline_uuid"));

        // Check other players if not found in online mode
        if(!uuid_joined.toString().equals(uuid_whitelisted) && online_mode) {
            boolean uuid_match = false;
            String old_player_name = "";
            for(String entry : plugin.getWhitelist().getConfig().getKeys(false)) {
                uuid_whitelisted = plugin.getWhitelist().getConfig().getString(entry + ".online_uuid");
                assert uuid_whitelisted != null;
                uuid_match = (uuid_joined.compareTo(UUID.fromString(uuid_whitelisted)) == 0);

                if(uuid_match) {
                    old_player_name = entry;
                    break;
                }
            }

            // If match is found, update entries and announce name change to the console.
            if(uuid_match) {
                plugin.getWhitelist().addEntry(player);
                plugin.getWhitelist().deleteEntry(old_player_name);

                plugin.getLogger().log(Level.INFO, ChatColor.translateAlternateColorCodes('&',
                        Objects.requireNonNull(plugin.getLanguage().getConfig()
                                .getString("messages.online_player_update"))
                                .replace("(old_player)", old_player_name)
                                .replace("(new_player)", player)));
            }
        }

        // Kick the player if the name/UUID is not found in the whitelist or there's a mismatch
        if(uuid_whitelisted == null || uuid_joined.compareTo(UUID.fromString(uuid_whitelisted)) != 0) {
            String kick_message;

            if(plugin.getConfig().getBoolean("custom-kick-message-enabled"))
                kick_message = ChatColor.translateAlternateColorCodes('&',
                        Objects.requireNonNull(plugin.getConfig().getString("custom-kick-message")));
            else kick_message = ChatColor.translateAlternateColorCodes('&',
                    Objects.requireNonNull(plugin.getLanguage().getConfig().getString("messages.kick")));

            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, kick_message);
        }
    }
}
