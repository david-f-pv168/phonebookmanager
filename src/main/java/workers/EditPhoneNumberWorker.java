package workers;

import contactmanager.*;
import gui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import static contactmanager.CheckHelpers.checkDetailsFrameNotNull;
import static contactmanager.CheckHelpers.checkPhoneNotNull;

/**
 * Worker class for editing contact's phone number
 */
public class EditPhoneNumberWorker extends SwingWorker<Void, Void> {
    private PhoneNumber phone;
    private DetailsFrame detailsFrame;
    private static final Logger logger = LoggerFactory.getLogger(EditPhoneNumberWorker.class.getName());

    public EditPhoneNumberWorker(PhoneNumber phone, DetailsFrame detailsFrame) {
        checkPhoneNotNull(phone, logger);
        checkDetailsFrameNotNull(detailsFrame, logger);

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
        } catch (InterruptedException ex) {
            logger.error("Interrupted exception error.", ex);
            throw new AssertionError();
        } catch (ExecutionException ex) {
            logger.error("Connection error", ex);
            JOptionPane.showMessageDialog(detailsFrame.getMainPanel(),
                    ResourceBundle.getBundle("messages").getString("connectionError"));
        } finally {
            detailsFrame.setEditMode(false);
            detailsFrame.setPhoneNumbersButtonsEnabled(true);
            detailsFrame.clearPhoneData();
        }
    }
}
