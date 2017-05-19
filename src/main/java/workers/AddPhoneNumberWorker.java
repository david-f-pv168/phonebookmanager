package workers;

import contactmanager.Contact;
import contactmanager.PhoneNumber;
import contactmanager.PhoneNumberManager;
import gui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import static contactmanager.CheckHelpers.checkContactNotNull;
import static contactmanager.CheckHelpers.checkDetailsFrameNotNull;
import static contactmanager.CheckHelpers.checkPhoneNotNull;

/**
 * Worker class for adding contact's phone number to DB
 */
public class AddPhoneNumberWorker extends SwingWorker<Void, Void> {
    private Contact contact;
    private PhoneNumber phone;
    private DetailsFrame detailsFrame;

    private static final Logger logger = LoggerFactory.getLogger(AddPhoneNumberWorker.class.getName());

    public AddPhoneNumberWorker(Contact contact, PhoneNumber phone, DetailsFrame detailsFrame) {
        checkContactNotNull(contact, logger);
        checkPhoneNotNull(phone, logger);
        checkDetailsFrameNotNull(detailsFrame, logger);

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
        } catch (InterruptedException ex) {
            logger.error("Interrupted exception error.",ex);
            throw new AssertionError();
        } catch (ExecutionException ex) {
            logger.error("Connection error", ex);
            JOptionPane.showMessageDialog(detailsFrame.getMainPanel(),
                    ResourceBundle.getBundle("messages").getString("connectionError"));
        } finally {
            detailsFrame.setPhoneNumbersButtonsEnabled(true);
        }
    }
}
