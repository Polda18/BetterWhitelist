package cz.czghost.mcspigot.betterwhitelist.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Static class to generate UUIDs for specified players
 */
public class UUIDGenerator {
    /**
     * Private method to build a JSON deserialized object from serialized JSON string returned by Mojang database
     * @param player Specified player for lookup
     * @return JSON object containing deserialized results of the lookup
     * @throws IOException Fires when there was an input/output error trying to contact Mojang database
     */
    private static JsonObject getMojangJSON(String player) throws IOException {
        // Get API URL for selected player and make connection
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + player);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");    // We have to use a GET method here
        con.setConnectTimeout(5000);    // Set a timeout for both connect and read
        con.setReadTimeout(5000);
        int status = con.getResponseCode();     // Read status code

        // Build JSON object string from given response body
        BufferedReader in = new BufferedReader((status > 299)
                ? new InputStreamReader(con.getErrorStream())
                : new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        // Check for "Too many requests" error
        if(status == 429)
            throw new RatelimitException("Too many requests hit", con.getHeaderFieldInt("RetryAfter", 3600));

        // Create JSON object from acquired JSON string
        Gson gson = new Gson();
        return gson.fromJson(content.toString(), JsonObject.class);
    }

    /**
     * Method to lookup the Mojang player name in online database.
     * Mojang stores the exact capitalization of specified player name, and plugin in online mode server resolves
     * the correct capitalization of the specified player name and patches the specified string with it.
     * @param player Specified player to lookup
     * @return Correct capitalization of the specified player in Mojang's database
     * @throws IOException Fired when there was an error trying to contact Mojang's database
     */
    public static String lookupMojangPlayerName(String player) throws IOException {
        JsonObject jsonObject = getMojangJSON(player);

        // Retrieved object is null -> return null
        if(jsonObject == null) {
            return null;
        }

        // Retrieve correct current name
        JsonElement name = jsonObject.get("name");

        // Return the correct current name of the player
        return (jsonObject.get("name") == null
                || jsonObject.get("name").isJsonNull()
                || jsonObject.get("name").getAsString().equals(""))
                ? null : jsonObject.get("name").getAsString();
    }

    /**
     * Main method to lookup the player's online mode UUID in Mojang's database
     * @param player Specified player to lookup
     * @return Online mode UUID for the specified player if found
     * @throws IOException Fired when there was an error trying to contact Mojang's database
     */
    public static UUID lookupMojangPlayerUUID(String player) throws IOException {
        // Get the player JSON object from Mojang API
        JsonObject jsonObject = getMojangJSON(player);

        if(jsonObject == null) {
            return null;    // No player found
        }

        // Get the UUID from the JSON object
        String uuid_s = (jsonObject.get("id") == null
                || jsonObject.get("id").isJsonNull()
                || jsonObject.get("id").getAsString().equals(""))
                ? null : jsonObject.get("id").getAsString();

        // If null, return null
        if(uuid_s == null) {
            return null;
        }

        // Return the UUID in correct form
//        return UUID.fromString(new StringBuilder(uuid_s).insert(8, '-').insert(13, '-')
//                .insert(18, '-').insert(23, '-').toString());
        return UUID.fromString(uuid_s.replaceFirst(
                "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})",
                "$1-$2-$3-$4-$5"));
    }

    /**
     * Main method to get player's offline mode UUID
     * @param player Speficied player to generate their offline UUID from their nick
     * @return Generated offline mode UUID
     */
    public static UUID generateOfflineUUIDFromPlayerName(String player) {
        // Generate the UUID from the offline player
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player).getBytes(StandardCharsets.UTF_8));
    }
}
