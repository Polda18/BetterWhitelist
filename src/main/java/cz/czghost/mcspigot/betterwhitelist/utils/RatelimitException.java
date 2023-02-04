package cz.czghost.mcspigot.betterwhitelist.utils;

public class RatelimitException extends RuntimeException {
    private final int retryAfter;

    /**
     * Constructor - creates new Ratelimit exception
     * @param msg Message to be given for the ratelimit
     * @param retryAfter Specifies in seconds how long to wait until ratelimit expires
     */
    public RatelimitException(String msg, int retryAfter) {
        super(msg);

        this.retryAfter = retryAfter;
    }

    /**
     * Gets ratelimit expiration
     * @return Expiration time in seconds
     */
    public int getRetryAfter() {
        return retryAfter;
    }
}
