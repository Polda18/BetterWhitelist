package me.polda18.betterwhitelist.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class Language {
    private String code;
    private FileConfiguration config;
    private File file;

    public static final String[] DEFAULT_LANGUAGE_FILES = {
            "cs.yml",
            "en.yml"
            // Add new languages before this line
    };

    public Language(String code, FileConfiguration config, File file) {
        this.code = code;
        this.config = config;
        this.file = file;
    }

    public String getCode() {
        return this.code;
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public File getFile() {
        return this.file;
    }
}
