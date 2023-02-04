package cz.czghost.mcspigot.betterwhitelist.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

/**
 * Language entry specification
 */
public class Language {
    private String code;
    private FileConfiguration config;
    private File file;

    /**
     * List of default files available - should reflect files in "resources/languages" directory.
     */
    public static final String[] DEFAULT_LANGUAGE_FILES = {
            "cs.yml",
            "en.yml"
            // Add new languages before this line
    };

    /**
     * Constructor: creates a language entry.
     * @param code Language locale code
     * @param config The file configuration handler that embeds the language into the plugin
     * @param file Handler to the file itself, containing said language
     */
    public Language(String code, FileConfiguration config, File file) {
        this.code = code;
        this.config = config;
        this.file = file;
    }

    /**
     * Get the language locale code
     * @return Stored locale code
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Get the file configuration handler that embeds the language into the plugin
     * @return Stored file configuration handler
     */
    public FileConfiguration getConfig() {
        return this.config;
    }

    /**
     * Get the handler to the file itself, containing language definition
     * @return Stored file handler
     */
    public File getFile() {
        return this.file;
    }
}
