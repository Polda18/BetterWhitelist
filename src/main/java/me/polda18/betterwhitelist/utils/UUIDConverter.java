package me.polda18.betterwhitelist.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class UUIDConverter {
    public static UUID getOnlineUUIDFromPlayerName(String player) throws IOException {
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
        JsonObject jsonObject = gson.fromJson(jsonS, JsonObject.class);

        // Get the ID
        UUID uuid = UUID.fromString((jsonObject.get("id").isJsonNull() || jsonObject.get("id").getAsString().equals(""))
                ? null : jsonObject.get("id").getAsString());

        return uuid;
    }

    public static UUID getOfflineUUIDFromPlayerName(String player) {
        try {
            // Get hashing function
            MessageDigest md = MessageDigest.getInstance("MD5");
            // Digest hash
            byte[] messageDigest = md.digest(("Offline:" + player).getBytes());

            // Convert to signed number data
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert number into string of hexadecimal digits
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            // Convert string into UUID
            return UUID.fromString(hashtext);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("An internal error occured.", e);
        }
    }
}
