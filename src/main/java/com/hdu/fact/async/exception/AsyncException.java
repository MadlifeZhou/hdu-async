package com.hdu.fact.async.exception;


/**
 * <p>
 *
 * 对Exception的再封装
 *
 * </p>
 *
 * @author zhou
 */
public class AsyncException extends RuntimeException {

    private static final long serialVersionUID = -2128834565845654572L;

    public AsyncException() {
        super();
    }

    public AsyncException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsyncException(String message) {
        super(message);
    }

    public AsyncException(Throwable cause) {
        super(cause);
    }

}
 