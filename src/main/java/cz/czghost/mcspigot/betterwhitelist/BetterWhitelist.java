package cz.czghost.mcspigot.betterwhitelist;

import cz.czghost.mcspigot.betterwhitelist.commands.Autocomplete;
import cz.czghost.mcspigot.betterwhitelist.utils.InvalidEntryException;
import cz.czghost.mcspigot.betterwhitelist.commands.WhitelistCommand;
import cz.czghost.mcspigot.betterwhitelist.config.Language;
import cz.czghost.mcspigot.betterwhitelist.config.Whitelist;
import cz.czghost.mcspigot.betterwhitelist.events.EventsListener;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.management.InstanceAlreadyExistsException;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;

/**
 * Plugin skeleton that wraps around the entire structure - this is the main class that is run by the server
 */
public final class BetterWhitelist extends JavaPlugin {
    private HashMap<String, Language> languages;
    private boolean enabled;
    private String lang_code;
    private Whitelist whitelist;

    /**
     * Private method to save the default language filename
     * @param filename Language filename specified
     */
    private void saveDefaultLanguage(String filename) {
        File language_file = new File(this.getDataFolder(), "languages/" + filename);
        if(!language_file.exists()) {
            this.saveResource("languages/" + filename, false);
        }
    }

    /**
     * List the available languages that the server has installed
     * @return List of the available languages in form of the language locale code mapped to the language name
     */
    @NotNull
    public Map<String, String> listAvailableLanguages() {
        Map<String, String> available_languages = new HashMap<>();

        for(String lang_code : languages.keySet()) {
            available_languages.put(lang_code, languages.get(lang_code).getConfig().getString("name"));
        }

        return available_languages;
    }

    /**
     * Sets the whitelist enabled or disabled
     * @param enabled Boolean value, true for whitelist enabled, false for whitelist disabled
     */
    public void setWhitelistEnabled(boolean enabled) {
        this.enabled = enabled;
        getConfig().set("enabled", enabled);
        saveConfig();
    }

    /**
     * Returns the whitelist enabled status
     * @return Whitelist enabled status - true for enabled, false for disabled
     */
    public boolean whitelistIsEnabled() {
        return this.enabled;
    }

    /**
     * Get the whitelist instance
     * @return Whitelist instance
     */
    public Whitelist getWhitelist() {
        return this.whitelist;
    }

    /**
     * Get currently selected language
     * @return Currently selected language
     */
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

    /**
     * Reload all configurations - the plugin configuration, whitelist and languages specifications
     */
    public void reloadAllConfigs() {
        // Reload config
        this.reloadConfig();

        // Reload whitelist
        try {
            this.whitelist.getConfig().load(whitelist.getFile());
        } catch (IOException | InvalidConfigurationException e) {
            this.getLogger().log(Level.SEVERE, "An error occured when reloading whitelist.");
            e.printStackTrace();
        }

        // Reload language files
        for(Language language : languages.values()) {
            try {
                language.getConfig().load(language.getFile());
            } catch (IOException | InvalidConfigurationException e) {
                this.getLogger().log(Level.SEVERE, "An error occured when reloading a language file.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets the language to specified language
     * @param lang_code Specified language locale code
     * @throws InvalidEntryException Fired when specified language was not found
     */
    public void setLanguage(String lang_code) throws InvalidEntryException {
        if(this.languages.get(lang_code) == null) {
            throw new InvalidEntryException("Language not found");
        }

        this.lang_code = lang_code;
        getConfig().set("language", lang_code);
        saveConfig();
    }

    /**
     * When plugin is enabled, this method runs all necessary checks and prints their results into console
     */
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
        Objects.requireNonNull(this.getCommand("whitelist")).setExecutor(new WhitelistCommand(this));
        Objects.requireNonNull(this.getCommand("whitelist")).setTabCompleter(new Autocomplete(this));

        // Get events listener
        this.getServer().getPluginManager().registerEvents(new EventsListener(this), this);

        // Get default configurations from resources if non-existent
        this.getLogger().log(Level.INFO, "Loading config...");
        this.saveDefaultConfig();
        // Get language files from resources if non-existent
        for(String language : Language.DEFAULT_LANGUAGE_FILES) {
            this.saveDefaultLanguage(language);
        }

        // Print out the configuration load
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

        for (Path language_filename : language_filenames) {
            File file = new File(language_filename.toString());
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String code = file.getName().substring(0, file.getName().lastIndexOf('.'));
            languages.put(code, new Language(code, config, file));
            this.getLogger().log(Level.INFO, "Language '" + code + "' registered.");
        }

        this.getLogger().log(Level.INFO, ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(this.getLanguage().getConfig().getString("messages.language"))
                        .replace("(language)", Objects.requireNonNull(this.getLanguage().getConfig()
                                .getString("name")))));

        if(this.enabled) {
            this.getLogger().log(Level.INFO, ChatColor.translateAlternateColorCodes('&',
                    Objects.requireNonNull(this.languages.get(lang_code).getConfig()
                            .getString("messages.enabled"))));
        }
    }

    /**
     * When plugin is disabled, it saves all the configuration changes into their respected files
     */
    @Override
    public void onDisable() {
        this.saveConfig();

        try {
            this.whitelist.getConfig().save(this.whitelist.getFile());
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, ChatColor.translateAlternateColorCodes('&',
                    Objects.requireNonNull(this.languages.get(lang_code).getConfig()
                            .getString("messages.error.saving"))));

            e.printStackTrace();
        }

        if(this.enabled) {
            this.getLogger().log(Level.INFO, ChatColor.translateAlternateColorCodes('&',
                    Objects.requireNonNull(this.languages.get(this.lang_code).getConfig()
                            .getString("messages.disabled"))));
        }
    }
}
