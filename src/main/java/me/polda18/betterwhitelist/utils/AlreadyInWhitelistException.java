package me.polda18.betterwhitelist.utils;

/**
 * Exception declaration for entry already in whitelist
 */
public class AlreadyInWhitelistException extends Exception {
    /**
     * Constructor: creates the exception
     * @param msg Exception message
     */
    public AlreadyInWhitelistException(String msg) {
        super(msg);
    }
}
