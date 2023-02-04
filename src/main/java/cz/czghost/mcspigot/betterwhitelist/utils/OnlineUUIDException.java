package cz.czghost.mcspigot.betterwhitelist.utils;

/**
 * Exception for online UUID not found in Mojang's database
 */
public class OnlineUUIDException extends Exception {
    /**
     * Constructor: creates the exception
     * @param msg Exception message
     */
    public OnlineUUIDException(String msg) {
        super(msg);
    }
}
