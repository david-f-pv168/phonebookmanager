package workers;

import contactmanager.PhoneNumber;
import contactmanager.PhoneNumberManager;
import gui.DetailsFrame;
import gui.Main;
import gui.PhoneNumbersTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import static contactmanager.CheckHelpers.checkDetailsFrameNotNull;
import static contactmanager.CheckHelpers.checkPhoneNotNull;

/**
 * Worker class for removing contact from DB
 */
public class RemovePhoneNumberWorker extends SwingWorker<Void, Void> {
    private DetailsFrame detailsFrame;
    private PhoneNumber phone;
    private static final Logger logger = LoggerFactory.getLogger(RemovePhoneNumberWorker.class.getName());

    public RemovePhoneNumberWorker(PhoneNumber phone, DetailsFrame detailsFrame) {
        checkPhoneNotNull(phone, logger);
        checkDetailsFrameNotNull(detailsFrame, logger);

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
            PhoneNumbersTableModel model = detailsFrame.getPhoneNumbersTableModel();
            model.removePhoneNumber(phone);
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
