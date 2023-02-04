package cz.czghost.mcspigot.betterwhitelist.utils;

/**
 * Exception for entry that wasn't found in whitelist
 */
public class InvalidEntryException extends Exception {
    /**
     * Constructor: creates the exception
     * @param msg Exception message
     */
    public InvalidEntryException(String msg) {
        super(msg);
    }
}
