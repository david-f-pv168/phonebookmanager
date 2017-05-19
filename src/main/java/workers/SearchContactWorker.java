package workers;

/**
 * Created by David on 15-May-17.
 */

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
 * Worker class for getting Customer data from DB
 */
public class SearchContactWorker extends SwingWorker<List<Contact>, Void> {
    private String part;
    private MainJFrame.SearchType type;
    private MainJFrame mainJFrame;
    private static final Logger logger = LoggerFactory.getLogger(SearchContactWorker.class.getName());


    public SearchContactWorker(String part, MainJFrame.SearchType type, MainJFrame mainJFrame) {
        checkMainFrameNotNull(mainJFrame, logger);

        if(type == null) {
            String msg = "Type is null";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.part = part;
        this.type = type;
        this.mainJFrame = mainJFrame;
    }

    @Override
    protected List<Contact> doInBackground() throws Exception {
        ContactManager contactManager = Main.getContactManager();

        if (type == MainJFrame.SearchType.NAME) {
            return contactManager.findContactsByName(part);
        } else {
            return contactManager.findContactsByNumber(part);
        }
    }

    @Override
    protected void done() {
        ContactsTableModel ctmodel = mainJFrame.getContactsTableModel();
        try {
            List<Contact> contacts = get();
            ctmodel.removeAllContacts();
            contacts.forEach(ctmodel::addContact);
            ctmodel.fireTableDataChanged();
        } catch (InterruptedException ex) {
            logger.error("Interrupted exception error.", ex);
            throw new AssertionError();
        } catch (ExecutionException ex) {
            logger.error("Connection error", ex);
            JOptionPane.showMessageDialog(mainJFrame, ResourceBundle.getBundle("messages").getString("connectionError"));
        }
    }
}
