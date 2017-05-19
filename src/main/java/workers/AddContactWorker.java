package workers;

import contactmanager.Contact;
import contactmanager.ContactManager;
import gui.ContactsTableModel;
import gui.Main;
import gui.MainJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import static contactmanager.CheckHelpers.checkContactNotNull;
import static contactmanager.CheckHelpers.checkMainFrameNotNull;

/**
 * Worker class for adding contact to DB
 */
public class AddContactWorker extends SwingWorker<Void, Void> {
    private MainJFrame mainJFrame;
    private Contact contact;

    private static final Logger logger = LoggerFactory.getLogger(AddContactWorker.class.getName());

    public AddContactWorker(Contact contact, MainJFrame mainJFrame) {
        checkContactNotNull(contact, logger);
        checkMainFrameNotNull(mainJFrame, logger);

        this.mainJFrame = mainJFrame;
        this.contact = contact;
    }

    @Override
    protected Void doInBackground() throws Exception {
        ContactManager contactManager = Main.getContactManager();
        contactManager.createContact(contact);
        return null;
    }

    @Override
    protected void done() {
        try {
            get();
            ContactsTableModel ctmodel = mainJFrame.getContactsTableModel();
            ctmodel.addContact(contact);
            mainJFrame.clearNewContactData();
        } catch (InterruptedException ex) {
            logger.error("Interrupted exception error.", ex);
            throw new AssertionError();
        } catch (ExecutionException ex) {
            logger.error("Connection error", ex);
            JOptionPane.showMessageDialog(mainJFrame, ResourceBundle.getBundle("messages").getString("connectionError"));
        } finally {
            mainJFrame.setContactsButtonsEnabled(true);
        }
    }
}
