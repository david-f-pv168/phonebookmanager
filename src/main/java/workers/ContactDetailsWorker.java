package workers;

import contactmanager.Contact;
import contactmanager.PhoneNumber;
import contactmanager.PhoneNumberManager;
import gui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import static contactmanager.CheckHelpers.checkContactNotNull;
import static contactmanager.CheckHelpers.checkMainFrameNotNull;

/**
 * Worker class for creating contact details tab.
 */
public class ContactDetailsWorker extends SwingWorker<List<PhoneNumber>, Void> {
    private MainJFrame mainJFrame;
    private DetailsFrame detailsFrame;
    private Contact contact;

    private static final Logger logger = LoggerFactory.getLogger(ContactDetailsWorker.class.getName());

    public ContactDetailsWorker(Contact contact, MainJFrame mainJFrame) {
        checkContactNotNull(contact, logger);
        checkMainFrameNotNull(mainJFrame, logger);

        this.mainJFrame = mainJFrame;
        this.contact = contact;
        this.detailsFrame = new DetailsFrame(mainJFrame.getContactsPane());
        logger.debug("Displaying details for contact with ID: " + contact.getID().toString());
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
        } catch (InterruptedException ex) {
            logger.error("Interrupted exception error.", ex);
            throw new AssertionError();
        } catch (ExecutionException ex) {
            logger.error("Connection error", ex);
            JOptionPane.showMessageDialog(mainJFrame, ResourceBundle.getBundle("messages").getString("connectionError"));
        }
    }
}