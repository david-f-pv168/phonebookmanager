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
public class ContactDownloadWorker extends SwingWorker<List<Contact>, Void> {
    private MainJFrame mainJFrame;

    public ContactDownloadWorker(MainJFrame mainJFrame) {
        if(mainJFrame == null) {
            throw new IllegalArgumentException("Form is null");
        }
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
        } catch (InterruptedException e1) {
            throw new AssertionError();
        } catch (ExecutionException e1) {
            JOptionPane.showMessageDialog(mainJFrame, ResourceBundle.getBundle("messages").getString("connectionError"));
        }
        mainJFrame.setContactsButtonsEnabled(true);
    }
}
