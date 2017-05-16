package workers;

import contactmanager.Contact;
import contactmanager.ContactManager;
import gui.ContactsTableModel;
import gui.Main;
import gui.MainJFrame;

import javax.swing.*;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

/**
 * Worker class for adding contact to DB
 */
public class AddContactWorker extends SwingWorker<Void, Void> {
    private MainJFrame mainJFrame;
    private Contact contact;

    public AddContactWorker(Contact contact, MainJFrame mainJFrame) {
        if(contact == null) {
            throw new IllegalArgumentException("Contact is null.");
        }
        if(mainJFrame == null) {
            throw new IllegalArgumentException("Form is null.");
        }
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
        } catch (InterruptedException e) {
            throw new AssertionError();
        } catch (ExecutionException e) {
            JOptionPane.showMessageDialog(mainJFrame, ResourceBundle.getBundle("messages").getString("connectionError"));
        } finally {
            mainJFrame.setContactsButtonsEnabled(true);
        }
    }
}
