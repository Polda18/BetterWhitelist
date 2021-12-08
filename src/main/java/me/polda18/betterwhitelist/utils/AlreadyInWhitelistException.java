package me.polda18.betterwhitelist.utils;

public class AlreadyInWhitelistException extends Exception {
    public AlreadyInWhitelistException(String msg) {
        super(msg);
    }
}
