package common;

/**
 * Created by David Frankl on 24-Mar-17.
 */
public class ValidationException extends RuntimeException {
    /**
     * Constructs an instance of ValidationException with the specified detail message.
     *
     * @param msg the detail message.
     */
    public ValidationException(String msg) {
        super(msg);
    }
}