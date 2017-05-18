package gui;

import common.ValidationException;
import contactmanager.Contact;
import contactmanager.DBUtils;
import contactmanager.PhoneNumber;
import org.jdatepicker.impl.JDatePickerImpl;
import workers.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;

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
    private JButton clearPhoneDataButton;
    private JButton phoneNumberUpsertButton;
    private JLabel surnameLabel;
    private JLabel emailLabel;
    private JLabel birthdayLabel;
    private JLabel typeLabel;
    private JLabel numberLabel;
    private JLabel countryCodeLabel;
    private JButton xButton;
    private JButton phoneNumberEditButton;
    private JButton phoneNumberDeleteButton;

    private Contact contact;
    private Long phoneInEditID;

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

        phoneNumberUpsertButton.addActionListener(this::phoneNumberUpsertButtonPressed);
        phoneNumberDeleteButton.addActionListener(this:: phoneNumberDeleteButtonPressed);
        clearPhoneDataButton.addActionListener(this::clearPhoneDataButtonPressed);
        phoneNumberEditButton.addActionListener(this:: phoneNumberEditButtonPressed);
    }

    private void createUIComponents() {
        contactBirthdayDatePicker = createDatePicker();
    }

    public void contactEditButtonPressed(ActionEvent event) {
        setContactTextFieldsEnabled(!contactFirstNameTextField.isEnabled());

        if (!contactFirstNameTextField.isEnabled()) {
            setContact(contact);
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

    private void phoneNumberDeleteButtonPressed(ActionEvent event) {
        int selectedRow = phonesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(phoneNumberDeleteButton, ResourceBundle.getBundle("messages").getString("noDataSelected"));
            return;
        }

        setPhoneNumbersButtonsEnabled(false);

        PhoneNumbersTableModel model = getPhoneNumbersTableModel();
        PhoneNumber phone = model.getPhoneNumberAt(selectedRow);

        new RemovePhoneNumberWorker(phone, this).execute();
    }

    private void phoneNumberEditButtonPressed(ActionEvent event) {
        int selectedRow = phonesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(phoneNumberDeleteButton, ResourceBundle.getBundle("messages").getString("noDataSelected"));
            return;
        }
        setEditMode(true);

        PhoneNumber phone = getPhoneNumbersTableModel().getPhoneNumberAt(selectedRow);
        phoneCountryCodeTextField.setText(phone.getCountryCode());
        phoneNumberTextField.setText(phone.getNumber());
        phoneTypeTextField.setText(phone.getPhoneType());
        phoneInEditID = phone.getID();
    }


    private void phoneNumberUpsertButtonPressed(ActionEvent event) {
        setPhoneNumbersButtonsEnabled(false);

        PhoneNumber phone = getPhoneNumberFromAddForm();
        try {
            Main.getPhoneNumberManager().validatePhone(phone);
        }
        catch (ValidationException ex) {
            setPhoneNumbersButtonsEnabled(true);
            JOptionPane.showMessageDialog(mainPanel, ex.getMessage());
            return;
        }

        if (phoneNumberUpsertButton.getText().equals("Confirm edit")) {
            phone.setID(phoneInEditID);
            new EditPhoneNumberWorker(phone, this).execute();
        } else {
            new AddPhoneNumberWorker(contact, phone, this).execute();
        }

    }

    public void clearPhoneDataButtonPressed(ActionEvent event) {
        clearPhoneData();
        setEditMode(false);
    }

    private PhoneNumber getPhoneNumberFromAddForm() {
        return new PhoneNumber.Builder()
                .countryCode(getNonEmptyTextOrNull(phoneCountryCodeTextField))
                .number(getNonEmptyTextOrNull(phoneNumberTextField))
                .phoneType(getNonEmptyTextOrNull(phoneTypeTextField))
                .build();
    }

    public void setPhoneNumbersButtonsEnabled(Boolean value) {
        phoneNumberUpsertButton.setEnabled(value);
        phoneNumberDeleteButton.setEnabled(value);
        phoneNumberEditButton.setEnabled(value);
        clearPhoneDataButton.setEnabled(value);
    }

    public void clearPhoneData() {
        phoneCountryCodeTextField.setText("");
        phoneNumberTextField.setText("");
        phoneTypeTextField.setText("");
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public PhoneNumbersTableModel getPhoneNumbersTableModel() {
        return (PhoneNumbersTableModel) phonesTable.getModel();
    }

    public void setContact(Contact contact) {
        this.contact = contact;
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

    public void setEditMode(Boolean inEditMode) {
        if (inEditMode) {
            clearPhoneDataButton.setText("Cancel edit");
            phoneNumberUpsertButton.setText("Confirm edit");
        } else {
            clearPhoneDataButton.setText("Clear data");
            phoneNumberUpsertButton.setText("Add phone");
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
