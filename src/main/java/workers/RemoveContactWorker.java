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
 * Worker class for removing contact from DB
 */
public class RemoveContactWorker extends SwingWorker<Void, Void> {
    private MainJFrame mainJFrame;
    private Contact contact;
    private static final Logger logger = LoggerFactory.getLogger(RemoveContactWorker.class.getName());

    public RemoveContactWorker(Contact contact, MainJFrame mainJFrame) {
        checkContactNotNull(contact, logger);
        checkMainFrameNotNull(mainJFrame, logger);

        this.mainJFrame = mainJFrame;
        this.contact = contact;
    }

    @Override
    protected Void doInBackground() throws Exception {
        ContactManager contactManager = Main.getContactManager();
        contactManager.deleteContact(contact);
        return null;
    }

    @Override
    protected void done() {
        try {
            get();
            ContactsTableModel model = mainJFrame.getContactsTableModel();
            model.removeContact(contact);
            JPanel contactDetailsTab = mainJFrame.findContactsTab(contact);
            mainJFrame.getContactsPane().remove(contactDetailsTab);

        } catch (InterruptedException ex) {
            logger.error("Interrupted exception error.", ex);
            throw new AssertionError();
        } catch (ExecutionException ex) {
            logger.error("Connection error", ex);
            JOptionPane.showMessageDialog(mainJFrame,
                    ResourceBundle.getBundle("messages").getString("connectionError"));
        } finally {
            mainJFrame.setContactsButtonsEnabled(true);
        }
    }
}
