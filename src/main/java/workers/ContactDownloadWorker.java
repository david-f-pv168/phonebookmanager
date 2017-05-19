package workers;

import contactmanager.Contact;
import contactmanager.ContactManager;
import gui.ContactsTableModel;
import gui.Main;
import gui.MainJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import static contactmanager.CheckHelpers.checkMainFrameNotNull;

/**
 * Worker class for getting Contact data from DB
 */
public class ContactDownloadWorker extends SwingWorker<List<Contact>, Void> {
    private MainJFrame mainJFrame;
    private static final Logger logger = LoggerFactory.getLogger(ContactDownloadWorker.class.getName());

    public ContactDownloadWorker(MainJFrame mainJFrame) {
        checkMainFrameNotNull(mainJFrame, logger);
        this.mainJFrame = mainJFrame;
    }

    @Override
    protected List<Contact> doInBackground() throws Exception {
        ContactManager contactManager = Main.getContactManager();

        return contactManager.findAllContacts();
    }

    @Override
    protected void done() {
        ContactsTableModel contactsTableModel = mainJFrame.getContactsTableModel();
        try {
            get().forEach(contactsTableModel::addContact);
        } catch (InterruptedException ex) {
            logger.error("Interrupted exception error.", ex);
            throw new AssertionError();
        } catch (ExecutionException ex) {
            logger.error("Connection error", ex);
            JOptionPane.showMessageDialog(mainJFrame, ResourceBundle.getBundle("messages").getString("CONNECTION_ERROR"));
        }
        mainJFrame.setContactsButtonsEnabled(true);
    }
}
