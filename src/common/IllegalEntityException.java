package common;

/**
 * This exception is thrown when an invalid entity is used for operation.
 *
 * @author David Frankl
 */
public class IllegalEntityException extends RuntimeException {
    /**
     * Creates new instance of IllegalEntityException without message.
     */
    public IllegalEntityException() {
    }

    /**
     * Creates new instance of IllegalEntityException with
     * the specified detail message.
     *
     * @param msg: the detail message.
     */
    public IllegalEntityException(String msg) {
        super(msg);
    }

    /**
     * Creates new instance of IllegalEntityException with the
     * specified detail message and cause.
     *
     * @param msg: the detail message.
     * @param cause: the cause
     */
    public IllegalEntityException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
