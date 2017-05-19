package workers;

import contactmanager.Contact;
import contactmanager.ContactManager;
import gui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import static contactmanager.CheckHelpers.checkContactNotNull;
import static contactmanager.CheckHelpers.checkDetailsFrameNotNull;
import static contactmanager.CheckHelpers.checkMainFrameNotNull;

/**
 * Worker class for editing contact
 */
public class EditContactWorker extends SwingWorker<Void, Void> {
    private MainJFrame mainJFrame;
    private DetailsFrame detailsFrame;
    private Contact contact;
    private static final Logger logger = LoggerFactory.getLogger(EditContactWorker.class.getName());

    public EditContactWorker(Contact contact, MainJFrame mainJFrame, DetailsFrame detailsFrame) {
        checkContactNotNull(contact, logger);
        checkMainFrameNotNull(mainJFrame, logger);
        checkDetailsFrameNotNull(detailsFrame, logger);

        this.contact = contact;
        this.mainJFrame = mainJFrame;
        this.detailsFrame = detailsFrame;
    }

    @Override
    protected Void doInBackground() throws Exception {
        ContactManager contactManager = Main.getContactManager();
        contactManager.updateContact(contact);
        return null;
    }

    @Override
    protected void done() {
        try {
            get();
            ContactsTableModel model = mainJFrame.getContactsTableModel();
            model.editContact(contact);
            mainJFrame.clearNewContactData();

            int index = mainJFrame.getContactsPane().indexOfComponent(detailsFrame.getMainPanel());
            mainJFrame.getContactsPane().setTitleAt(index, guiUtils.getPaneTitleFromContact(contact));
        } catch (InterruptedException ex) {
            logger.error("Interrupted exception error.", ex);
            throw new AssertionError();
        } catch (ExecutionException ex) {
            logger.error("Connection error", ex);
            JOptionPane.showMessageDialog(mainJFrame,
                    ResourceBundle.getBundle("messages").getString("connectionError"));
        }
    }
}
