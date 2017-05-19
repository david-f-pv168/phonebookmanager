package contactmanager;

import common.IllegalEntityException;
import gui.DetailsFrame;
import org.slf4j.Logger;
import javax.sql.DataSource;
import javax.swing.*;

/**
 * Helper methods for checking various objects are not null
 */
public class CheckHelpers {
    public static void checkContactNotNull(Contact contact, Logger logger) throws IllegalArgumentException {
        if (contact == null) {
            String msg = "Contact is null.";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    public static void checkContactIDNotNull(Contact contact, Logger logger) throws IllegalEntityException{
        if (contact.getID() == null) {
            String msg = "Contact ID is null.";
            logger.error(msg);
            throw new IllegalEntityException(msg);
        }
    }

    public static void checkDataSourceNotNull(DataSource ds, Logger logger) throws IllegalStateException {
        if (ds == null) {
            String msg = "Data source is not set";
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
    }

    public static void checkPhoneNotNull(PhoneNumber phone, Logger logger) throws IllegalArgumentException {
        if (phone == null) {
            String msg = "Phone is null.";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    public static void checkMainFrameNotNull(JFrame frame, Logger logger) throws IllegalArgumentException {
        if(frame == null) {
            String msg = "Main frame is null";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    public static void checkDetailsFrameNotNull(DetailsFrame frame, Logger logger) throws IllegalArgumentException {
        if(frame == null) {
            String msg = "Details frame is null";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

}
