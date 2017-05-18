package gui;

import common.ValidationException;
import contactmanager.Contact;
import contactmanager.DBUtils;
import org.jdatepicker.impl.JDatePickerImpl;
import workers.EditContactWorker;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static gui.guiUtils.*;

/**
 * Created by David on 16-May-17.
 */
public class DetailsFrame {
    private JPanel mainPanel;
    private JTextField contactFirstNameTextField;
    private JTextField contactSurnameTextField;
    private JTextField contactPrimaryEmailTextField;
    private JDatePickerImpl contactBirthdayDatePicker;
    private JButton contactSaveButton;
    private JButton contactEditButton;
    private JTable phonesTable;
    private JTextField phoneTypeTextField;
    private JTextField phoneNumberTextField;
    private JTextField phoneCountryCodeTextField;
    private JButton clearDataButton;
    private JButton phoneNumberAddButton;
    private JLabel surnameLabel;
    private JLabel emailLabel;
    private JLabel birthdayLabel;
    private JLabel typeLabel;
    private JLabel numberLabel;
    private JLabel countryCodeLabel;
    private JButton xButton;
    private JButton phoneNumberEditButton;
    private JButton phoneNumberDeleteButton;

    private String contactFirstNameCache = "";
    private String contactSurnameCache = "";
    private String contactPrimaryEmailCache = "";
    private Calendar contactBirthdayCache = new GregorianCalendar();

    public final static String FrameID = "FrameID";

    public DetailsFrame(JTabbedPane parentPane) {
        phonesTable.setModel(new PhoneNumbersTableModel());
        phonesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        phonesTable.setVisible(true);
        phonesTable.getTableHeader().setReorderingAllowed(false);

        xButton.addActionListener(e -> parentPane.remove(mainPanel));
        setContactTextFieldsEnabled(false);

        contactEditButton.addActionListener(this::contactEditButtonPressed);
        contactSaveButton.addActionListener(this::contactSaveButtonPressed);
    }

    private void createUIComponents() {
        contactBirthdayDatePicker = createDatePicker();
    }

    public void contactEditButtonPressed(ActionEvent event) {
        setContactTextFieldsEnabled(!contactFirstNameTextField.isEnabled());

        if (contactFirstNameTextField.isEnabled()) {
            setContactTextFieldsFromCache();
        } else {
            cacheContactTextFields();
        }
    }

    private void contactSaveButtonPressed(ActionEvent event) {
        if (!contactFirstNameTextField.isEnabled()) {
            return;
        }

        setContactTextFieldsEnabled(false);
        Contact contact = getContactFromTextFields();
        try {
            Main.getContactManager().validateContact(contact);
        }
        catch (ValidationException ex) {
            setContactTextFieldsEnabled(true);
            JOptionPane.showMessageDialog(mainPanel, ex.getMessage());
            return;
        }

        MainJFrame topFrame = (MainJFrame) SwingUtilities.getWindowAncestor(mainPanel);
        new EditContactWorker(contact, topFrame, this).execute();
    }

    public PhoneNumbersTableModel getPhonesTableModel() {
        return (PhoneNumbersTableModel) phonesTable.getModel();
    }

    public void setPhonesButtonsEnabled(Boolean value) {
        phoneNumberAddButton.setEnabled(value);
        phoneNumberDeleteButton.setEnabled(value);
        phoneNumberEditButton.setEnabled(value);
        clearDataButton.setEnabled(value);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public PhoneNumbersTableModel getPhoneNumbersTableModel() {
        return (PhoneNumbersTableModel) phonesTable.getModel();
    }

    public void setContact(Contact contact) {
        contactFirstNameTextField.setText(contact.getFirstName());
        contactSurnameTextField.setText(contact.getSurname());
        contactPrimaryEmailTextField.setText(contact.getPrimaryEmail());

        GregorianCalendar cal = new GregorianCalendar();
        Date birthday = DBUtils.toSqlDate(contact.getBirthday());

        if (birthday != null) {
            cal.setTime(birthday);
            contactBirthdayDatePicker.getJFormattedTextField().setValue(cal);
        } else {
            contactBirthdayDatePicker.getJFormattedTextField().setValue(null);
        }
    }

    private void setContactTextFieldsEnabled(boolean value) {
        contactFirstNameTextField.setEnabled(value);
        contactSurnameTextField.setEnabled(value);
        contactPrimaryEmailTextField.setEnabled(value);
        contactBirthdayDatePicker.getComponent(1).setEnabled(value);

        if (contactFirstNameTextField.isEnabled()) {
            contactEditButton.setText("Cancel edit");
        } else {
            contactEditButton.setText("Edit contact");
        }
    }

    private void setContactTextFieldsFromCache() {
        contactFirstNameCache = contactFirstNameTextField.getText();
        contactSurnameCache = contactSurnameTextField.getText();
        contactPrimaryEmailCache = contactPrimaryEmailTextField.getText();
        contactBirthdayCache = (GregorianCalendar) contactBirthdayDatePicker.getJFormattedTextField().getValue();
    }

    private void cacheContactTextFields() {
        contactFirstNameTextField.setText(contactFirstNameCache);
        contactSurnameTextField.setText(contactSurnameCache);
        contactPrimaryEmailTextField.setText(contactPrimaryEmailCache);
        contactBirthdayDatePicker.getJFormattedTextField().setValue(contactBirthdayCache);
    }

    private Contact getContactFromTextFields() {
        String birthday = contactBirthdayDatePicker.getJFormattedTextField().getText();

        return new Contact.Builder()
                .ID((Long) mainPanel.getClientProperty(FrameID))
                .firstName(getNonEmptyTextOrNull(contactFirstNameTextField))
                .surname(getNonEmptyTextOrNull(contactSurnameTextField))
                .primaryEmail(getNonEmptyTextOrNull(contactPrimaryEmailTextField))
                .birthday(birthday.equals("") ? null: LocalDate.parse(birthday))
                .build();
    }
}
