package workers;

import contactmanager.Contact;
import contactmanager.PhoneNumber;
import contactmanager.PhoneNumberManager;
import gui.*;

import javax.swing.*;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

/**
 * Worker class for adding contact's phone number to DB
 */
public class AddPhoneNumberWorker extends SwingWorker<Void, Void> {
    private Contact contact;
    private PhoneNumber phone;
    private DetailsFrame detailsFrame;

    public AddPhoneNumberWorker(Contact contact, PhoneNumber phone, DetailsFrame detailsFrame) {
        if(contact == null) {
            throw new IllegalArgumentException("Contact is null.");
        }
        if(detailsFrame == null) {
            throw new IllegalArgumentException("Details panel is null.");
        }

        this.contact = contact;
        this.phone = phone;
        this.detailsFrame = detailsFrame;
    }

    @Override
    protected Void doInBackground() throws Exception {
        PhoneNumberManager phoneManager = Main.getPhoneNumberManager();
        phoneManager.addPhone(contact, phone);
        return null;
    }

    @Override
    protected void done() {
        try {
            get();
            PhoneNumbersTableModel pnmodel = detailsFrame.getPhoneNumbersTableModel();
            pnmodel.addPhoneNumber(phone);
            detailsFrame.clearPhoneData();
        } catch (InterruptedException e) {
            throw new AssertionError();
        } catch (ExecutionException e) {
            JOptionPane.showMessageDialog(detailsFrame.getMainPanel(),
                    ResourceBundle.getBundle("messages").getString("connectionError"));
        } finally {
            detailsFrame.setPhoneNumbersButtonsEnabled(true);
        }
    }
}
