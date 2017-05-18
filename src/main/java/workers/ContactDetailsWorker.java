package workers;

import contactmanager.Contact;
import contactmanager.PhoneNumber;
import contactmanager.PhoneNumberManager;
import gui.*;

import javax.swing.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

/**
 * Created by David on 16-May-17.
 */
public class ContactDetailsWorker extends SwingWorker<List<PhoneNumber>, Void> {
    private MainJFrame mainJFrame;
    private DetailsFrame detailsFrame;
    private Contact contact;

    public ContactDetailsWorker(Contact contact, MainJFrame mainJFrame) {
        if(contact == null) {
            throw new IllegalArgumentException("Contact is null.");
        }
        if(mainJFrame == null) {
            throw new IllegalArgumentException("Form is null.");
        }
        this.mainJFrame = mainJFrame;
        this.contact = contact;
        this.detailsFrame = new DetailsFrame(mainJFrame.getContactsPane());
    }

    @Override
    protected List<PhoneNumber> doInBackground() throws Exception {
        PhoneNumberManager phoneManager = Main.getPhoneNumberManager();

        return phoneManager.getPhoneNumbers(contact);
    }

    @Override
    protected void done() {
        PhoneNumbersTableModel phonesTableModel = detailsFrame.getPhoneNumbersTableModel();
        try {
            get().forEach(phonesTableModel::addPhoneNumber);
            detailsFrame.setContact(contact);

            JPanel detailsMainPanel = detailsFrame.getMainPanel();
            detailsMainPanel.putClientProperty(DetailsFrame.FrameID, contact.getID());

            mainJFrame.getContactsPane().addTab(guiUtils.getPaneTitleFromContact(contact), detailsMainPanel);
            mainJFrame.getContactsPane().setSelectedComponent(detailsMainPanel);
        } catch (InterruptedException e) {
            throw new AssertionError();
        } catch (ExecutionException e) {
            JOptionPane.showMessageDialog(mainJFrame, ResourceBundle.getBundle("messages").getString("connectionError"));
        }
    }
}