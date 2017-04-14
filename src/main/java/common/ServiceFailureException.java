package common;

/**
 * This exception represents service failure.
 *
 * @author David Frankl
 */
public class ServiceFailureException extends RuntimeException {

    /**
     * Creates new instance of ServiceFailureException with
     * the specified detail message.
     *
     * @param msg: the detail message.
     */
    public ServiceFailureException(String msg) {
        super(msg);
    }

    /**
     * Creates new instance of ServiceFailureException with
     * the specified cause.
     *
     * @param cause: the cause message.
     */
    public ServiceFailureException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates new instance of ServiceFailureException with
     * the specified detail message and cause.
     *
     * @param msg: the detail message.
     * @param cause: the cause message.
     */
    public ServiceFailureException(String msg, Throwable cause) {
        super(msg, cause);
    }

}