package com.hdu.fact.async.util;


/**
 * <p>
 *
 *
 *
 * </p>
 *
 * @author zhou
 */
public final class ValidationUtils {

    public static void checkNotNull(Object ref) {
        if (ref != null) {
            return;
        }
        throw new NullPointerException();
    }

    public static void checkNotNull(Object ref, String message) {
        if (ref != null) {
            return;
        }
        throw new NullPointerException(message);
    }


    public static void checkState(boolean exps) {
        if (exps) {
            return;
        }
        throw new IllegalStateException();
    }

    public static void checkState(boolean exps, String message) {
        if (exps) {
            return;
        }
        throw new IllegalStateException(message);
    }

}
 