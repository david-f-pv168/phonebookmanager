package workers;

import contactmanager.PhoneNumber;
import contactmanager.PhoneNumberManager;
import gui.DetailsFrame;
import gui.Main;
import gui.PhoneNumbersTableModel;
import javax.swing.*;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

/**
 * Worker class for removing contact from DB
 */
public class RemovePhoneNumberWorker extends SwingWorker<Void, Void> {
    private DetailsFrame detailsFrame;
    private PhoneNumber phone;

    public RemovePhoneNumberWorker(PhoneNumber phone, DetailsFrame detailsFrame) {
        if(phone == null) {
            throw new IllegalArgumentException("Phone is null");
        }
        if(detailsFrame == null) {
            throw new IllegalArgumentException("Form is null");
        }
        this.detailsFrame = detailsFrame;
        this.phone = phone;
    }

    @Override
    protected Void doInBackground() throws Exception {
        PhoneNumberManager phoneManager = Main.getPhoneNumberManager();
        phoneManager.removePhone(phone);
        return null;
    }

    @Override
    protected void done() {
        try {
            get();
            PhoneNumbersTableModel model = detailsFrame.getPhonesTableModel();
            model.removePhoneNumber(phone);
        } catch (InterruptedException e) {
            throw new AssertionError();
        } catch (ExecutionException e) {
            JOptionPane.showMessageDialog(detailsFrame.getMainPanel(),
                    ResourceBundle.getBundle("messages").getString("connectionError"));
        } finally {
            detailsFrame.setPhonesButtonsEnabled(true);
        }
    }
}
