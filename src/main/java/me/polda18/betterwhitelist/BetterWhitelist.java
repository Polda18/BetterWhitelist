package me.polda18.betterwhitelist;

import me.polda18.betterwhitelist.commands.Autocomplete;
import me.polda18.betterwhitelist.commands.WhitelistCommand;
import me.polda18.betterwhitelist.config.Language;
import me.polda18.betterwhitelist.config.Whitelist;
import me.polda18.betterwhitelist.utils.InvalidEntryException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.management.InstanceAlreadyExistsException;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;

public final class BetterWhitelist extends JavaPlugin {
    private HashMap<String, Language> languages;
    private boolean enabled;
    private String lang_code;
    private Whitelist whitelist;

    private void saveDefaultLanguage(String filename) {
        File language_file = new File(this.getDataFolder(), "languages/" + filename);
        if(!language_file.exists()) {
            this.saveResource("languages/" + filename, false);
        }
    }

    public Map<String, String> listAvailableLanguages() {
        Map<String, String> available_languages = new HashMap<>();

        for(String lang_code : languages.keySet()) {
            available_languages.put(lang_code, languages.get(lang_code).getConfig().getString("name"));
        }

        return available_languages;
    }

    public void setWhitelistEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean whitelistIsEnabled() {
        return this.enabled;
    }

    public Whitelist getWhitelist() {
        return this.whitelist;
    }

    public Language getLanguage() {
        Language language = this.languages.get(lang_code);

        if(language == null) {
            this.lang_code = "en";
            this.getConfig().set("language", "en");
            this.saveConfig();

            this.getLogger().log(Level.WARNING,
                    "Language specified inside config wasn't found, reverting back to English");
        }

        return this.languages.get(lang_code);
    }

    public void setLanguage(String lang_code) throws InvalidEntryException {
        if(this.languages.get(lang_code) == null) {
            throw new InvalidEntryException("Language not found");
        }

        this.lang_code = lang_code;
    }

    @Override
    public void onEnable() {
        this.languages = new HashMap<>();

        // ______      _   _            _    _ _     _ _       _ _     _
        // | ___ \    | | | |          | |  | | |   (_) |     | (_)   | |
        // | |_/ / ___| |_| |_ ___ _ __| |  | | |__  _| |_ ___| |_ ___| |_
        // | ___ \/ _ \ __| __/ _ \ '__| |/\| | '_ \| | __/ _ \ | / __| __|
        // | |_/ /  __/ |_| ||  __/ |  \  /\  / | | | | ||  __/ | \__ \ |_
        // \____/ \___|\__|\__\___|_|   \/  \/|_| |_|_|\__\___|_|_|___/\__|
        //
        //
        this.getLogger().log(Level.INFO, "______      _   _            _    _ _     _ _       _ _     _   ");
        this.getLogger().log(Level.INFO, "| ___ \\    | | | |          | |  | | |   (_) |     | (_)   | |  ");
        this.getLogger().log(Level.INFO, "| |_/ / ___| |_| |_ ___ _ __| |  | | |__  _| |_ ___| |_ ___| |_ ");
        this.getLogger().log(Level.INFO, "| ___ \\/ _ \\ __| __/ _ \\ '__| |/\\| | '_ \\| | __/ _ \\ | / __| __|");
        this.getLogger().log(Level.INFO, "| |_/ /  __/ |_| ||  __/ |  \\  /\\  / | | | | ||  __/ | \\__ \\ |_ ");
        this.getLogger().log(Level.INFO, "\\____/ \\___|\\__|\\__\\___|_|   \\/  \\/|_| |_|_|\\__\\___|_|_|___/\\__|");
        this.getLogger().log(Level.INFO, "");
        this.getLogger().log(Level.INFO, "");

        // Register new command
        this.getCommand("betterwhitelist").setExecutor(new WhitelistCommand(this));
        this.getCommand("betterwhitelist").setTabCompleter(new Autocomplete(this));

        // Get default configurations from resources if non-existent
        this.getLogger().log(Level.INFO, "Loading config...");
        this.saveDefaultConfig();
        // Get language files from resources if non-existent
        this.saveDefaultLanguage("cs.yml");
        this.saveDefaultLanguage("en.yml");
        // New languages be put before this line
        this.getLogger().log(Level.INFO, "Config loaded");

        this.enabled = this.getConfig().getBoolean("enabled", false);
        this.lang_code = this.getConfig().getString("language", "en");

        // Get whitelist
        File wl_file = new File(this.getDataFolder(), "whitelist.yml");
        FileConfiguration wl_config = YamlConfiguration.loadConfiguration(wl_file);
        try {
            this.whitelist = new Whitelist(this, wl_file, wl_config);
        } catch (InstanceAlreadyExistsException e) {
            this.getLogger().log(Level.SEVERE, "Multiple instances detected!");
            e.printStackTrace();
        }

        ArrayList<Path> language_filenames = new ArrayList<>();

        Path lang_dir = FileSystems.getDefault()
                .getPath(this.getDataFolder().getPath(), "languages");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(lang_dir, "*.yml")) {
            for(Path entry : stream) {
                language_filenames.add(entry);
                // Debug code
                this.getLogger().log(Level.INFO, "Language file '" + entry.toString() + "' loaded.");
            }
        } catch (IOException x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can // only be thrown by newDirectoryStream.
            this.getLogger().log(Level.SEVERE, "An error occured. Make sure languages directory is accessible.", x);
        }

        Iterator<Path> languages_iter = language_filenames.iterator();

        while(languages_iter.hasNext()) {
            File file = new File(languages_iter.next().toString());
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String code = file.getName().substring(0, file.getName().lastIndexOf('.'));
            languages.put(code, new Language(code, config, file));
            this.getLogger().log(Level.INFO, "Language '" + code + "' registered.");
        }

        this.getLogger().log(Level.INFO, ChatColor.translateAlternateColorCodes('&',
                this.getLanguage().getConfig().getString("messages.language")
                        .replace("(language)", this.getLanguage().getConfig().getString("name"))));

        if(this.enabled) {
            this.getLogger().log(Level.INFO, ChatColor.translateAlternateColorCodes('&',
                    this.languages.get(lang_code).getConfig().getString("messages.enabled")));
        }
    }

    @Override
    public void onDisable() {
        this.saveConfig();

        try {
            this.whitelist.getConfig().save(this.whitelist.getFile());
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, ChatColor.translateAlternateColorCodes('&',
                    this.languages.get(lang_code).getConfig().getString("messages.error.saving")));

            e.printStackTrace();
        }

        if(this.enabled) {
            this.getLogger().log(Level.INFO, ChatColor.translateAlternateColorCodes('&',
                    this.languages.get(this.lang_code).getConfig().getString("messages.disabled")));
        }
    }
}
