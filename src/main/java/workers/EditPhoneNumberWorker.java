package workers;

import contactmanager.*;
import gui.*;

import javax.swing.*;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

/**
 * Worker class for editing contact's phone number
 */
public class EditPhoneNumberWorker extends SwingWorker<Void, Void> {
    private PhoneNumber phone;
    private DetailsFrame detailsFrame;

    public EditPhoneNumberWorker(PhoneNumber phone, DetailsFrame detailsFrame) {
        if(phone == null) {
            throw new IllegalArgumentException("Contact's phone number is null");
        }
        if(detailsFrame == null) {
            throw new IllegalArgumentException("Form is null.");
        }
        this.phone = phone;
        this.detailsFrame = detailsFrame;
    }

    @Override
    protected Void doInBackground() throws Exception {
        PhoneNumberManager phonemanager = Main.getPhoneNumberManager();
        phonemanager.updatePhone(phone);
        return null;
    }

    @Override
    protected void done() {
        try {
            get();
            PhoneNumbersTableModel model = detailsFrame.getPhoneNumbersTableModel();
            model.editPhoneNumber(phone);
            detailsFrame.clearPhoneData();
        } catch (InterruptedException e) {
            throw new AssertionError();
        } catch (ExecutionException e) {
            JOptionPane.showMessageDialog(detailsFrame.getMainPanel(),
                    ResourceBundle.getBundle("messages").getString("connectionError"));
        } finally {
            detailsFrame.setEditMode(false);
            detailsFrame.setPhoneNumbersButtonsEnabled(true);
            detailsFrame.clearPhoneData();
        }
    }
}
