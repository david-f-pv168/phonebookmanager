package gui;

import contactmanager.*;

import javax.sql.DataSource;
import java.awt.*;
import java.time.Clock;

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
            db = DBUtils.createDatabaseWithTables(true);
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

    public static PhoneNumberManagerImpl getPhoneNumberManager() {
        if (phoneManager == null) {
            phoneManager = new PhoneNumberManagerImpl();
            phoneManager.setDataSource(getDB());
        }

        return phoneManager;
    }
}
