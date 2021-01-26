package me.polda18.betterwhitelist.utils;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class UUIDConverter {
    public static UUID getOnlineUUIDFromPlayerName(String player) {
        // TODO: Get UUID from Mojang
    }

    public static UUID getOfflineUUIDFromPlayerName(String player) {
        return UUID.fromString("Offline:" + player);
    }
}
