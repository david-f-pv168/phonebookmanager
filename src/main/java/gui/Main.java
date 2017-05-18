package gui;

import contactmanager.*;
import javax.sql.DataSource;
import java.awt.*;
import java.time.Clock;

/**
 * Created by David Frankl on 14-May-17.
 *
 */
public class Main {
    private static DataSource db;
    private static ContactManagerImpl contactManager;
    private static PhoneNumberManagerImpl phoneManager;

    public static void main(String[] args) {
        db = getDB();
        createUI();
    }

    private static void createUI() {
        EventQueue.invokeLater(MainJFrame::new);
    }

    private synchronized static DataSource getDB() {
        if (db == null) {
            db = DBUtils.createMemoryDatabaseWithTables(true);
        }

        return db;
    }

    public static ContactManagerImpl getContactManager() {
        if (contactManager == null) {
            contactManager = new ContactManagerImpl(Clock.systemDefaultZone());
            contactManager.setDataSource(getDB());
        }

        return contactManager;
    }

    public static PhoneNumberManager getPhoneNumberManager() {
        if (phoneManager == null) {
            phoneManager = new PhoneNumberManagerImpl();
            phoneManager.setDataSource(getDB());
        }

        return phoneManager;
    }
}
