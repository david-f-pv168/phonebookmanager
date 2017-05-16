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
 * Worker class for editing contact
 */
public class EditContactWorker extends SwingWorker<Void, Void> {
    private MainJFrame mainJFrame;
    private Contact contact;

    public EditContactWorker(Contact contact, MainJFrame mainJFrame) {
        if(contact == null) {
            throw new IllegalArgumentException("Contact is null");
        }
        if(mainJFrame == null) {
            throw new IllegalArgumentException("Form is null.");
        }
        this.contact = contact;
        this.mainJFrame = mainJFrame;
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
        } catch (InterruptedException e) {
            throw new AssertionError();
        } catch (ExecutionException e) {
            JOptionPane.showMessageDialog(mainJFrame,
                    ResourceBundle.getBundle("messages").getString("connectionError"));
        } finally {
            mainJFrame.setContactsButtonsEnabled(true);
        }
    }
}
