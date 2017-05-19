package workers;

/**
 * Created by David on 15-May-17.
 */

import contactmanager.Contact;
import contactmanager.ContactManager;
import gui.ContactsTableModel;
import gui.Main;
import gui.MainJFrame;

import javax.swing.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

/**
 * Worker class for getting Customer data from DB
 */
public class SearchContactWorker extends SwingWorker<List<Contact>, Void> {
    private String part;
    private MainJFrame.SearchType type;
    private MainJFrame mainJFrame;

    public SearchContactWorker(String part, MainJFrame.SearchType type, MainJFrame mainJFrame) {
        if(mainJFrame == null) {
            throw new IllegalArgumentException("Form is null");
        }

        if(type == null) {
            throw new IllegalArgumentException("Type is null");
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
        } catch (InterruptedException e1) {
            throw new AssertionError();
        } catch (ExecutionException e1) {
            JOptionPane.showMessageDialog(mainJFrame, ResourceBundle.getBundle("messages").getString("connectionError"));
        }
    }
}
