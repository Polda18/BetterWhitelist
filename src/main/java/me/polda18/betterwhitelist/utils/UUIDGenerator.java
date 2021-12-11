package me.polda18.betterwhitelist.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UUIDGenerator {
    private static JsonObject getMojangJSON(String player) throws IOException {
        String jsonS = "";          // Initialize JSON string

        // Get API URL for selected player and make connection
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + player);
        URLConnection conn = url.openConnection();
        conn.connect();

        // Get buffer and read input data into prepared JSON string
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        while((inputLine = in.readLine()) != null) {
            jsonS += inputLine;
        }

        // Create JSON object from acquired JSON string
        Gson gson = new Gson();
        return gson.fromJson(jsonS, JsonObject.class);
    }

    public static String lookupMojangPlayerName(String player) throws IOException {
        JsonObject jsonObject = getMojangJSON(player);

        if(jsonObject == null) {
            return null;
        }

        // Return the correct current name of the player
        return (jsonObject.get("name").isJsonNull() || jsonObject.get("name").getAsString().equals(""))
                ? null : jsonObject.get("name").getAsString();
    }

    public static UUID lookupMojangPlayerUUID(String player) throws IOException {
        // Get the player JSON object from Mojang API
        JsonObject jsonObject = getMojangJSON(player);

        if(jsonObject == null) {
            return null;    // No player found
        }

        // Get the UUID from the JSON object
        String uuid_s = (jsonObject.get("id").isJsonNull() || jsonObject.get("id").getAsString().equals(""))
                ? null : jsonObject.get("id").getAsString();

        // If null, return null
        if(uuid_s == null) {
            return null;
        }

        // Return the UUID in correct form
        return UUID.fromString(new StringBuilder(uuid_s).insert(8, '-').insert(13, '-')
                .insert(18, '-').insert(23, '-').toString());
    }

    public static UUID generateOfflineUUIDFromPlayerName(String player) {
        // Generate the UUID from the offline player
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player).getBytes(StandardCharsets.UTF_8));
    }
}