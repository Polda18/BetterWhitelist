package me.polda18.betterwhitelist.events;

import me.polda18.betterwhitelist.BetterWhitelist;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;
import java.util.logging.Level;

public class EventsListener implements Listener {
    private BetterWhitelist plugin;

    public EventsListener(BetterWhitelist plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Check if the server runs in an online mode or not
        boolean online_mode = Bukkit.getOnlineMode();

        // Check if the whitelist is enabled
        if(!plugin.whitelistIsEnabled()) {
            return;         // Do nothing if whitelist isn't enabled
        }

        // Get the player and his UUID
        Player player = event.getPlayer();
        String uuid = plugin.getWhitelist().getConfig()
                .getString(player.getName() + ((online_mode) ? ".online_uuid" : ".offline_uuid"));

        // Kick the player if the name/UUID is not found in the whitelist or there's a mismatch
        if(uuid == null || player.getUniqueId().compareTo(UUID.fromString(uuid)) != 0) {
            player.kickPlayer(ChatColor.translateAlternateColorCodes('&',
                    plugin.getLanguage().getConfig().getString("messages.kick")));
        }
    }
}
