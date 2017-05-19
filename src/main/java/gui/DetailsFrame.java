package gui;

import common.ValidationException;
import contactmanager.Contact;
import contactmanager.DBUtils;
import contactmanager.PhoneNumber;
import org.jdatepicker.impl.JDatePickerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workers.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;

import static gui.guiUtils.*;

/**
 * Frame representing a contact details tab
 */
public class DetailsFrame extends JFrame {
    private JPanel mainPanel;
    private JTextField contactFirstNameTextField;
    private JTextField contactSurnameTextField;
    private JTextField contactPrimaryEmailTextField;
    private JDatePickerImpl contactBirthdayDatePicker;
    private JTable phonesTable;
    private JTextField phoneTypeTextField;
    private JTextField phoneNumberTextField;
    private JTextField phoneCountryCodeTextField;
    private JButton contactSaveButton;
    private JButton contactEditButton;
    private JButton clearPhoneDataButton;
    private JButton phoneNumberUpsertButton;
    private JButton phoneNumberEditButton;
    private JButton phoneNumberDeleteButton;
    private JLabel surnameLabel;
    private JLabel emailLabel;
    private JLabel birthdayLabel;
    private JLabel typeLabel;
    private JLabel numberLabel;
    private JLabel countryCodeLabel;
    private JButton xButton;
    private JLabel firstNameLabel;


    private Contact contact;
    private Long phoneInEditID;
    private static final Logger logger = LoggerFactory.getLogger(DetailsFrame.class.getName());
    private ResourceBundle rb_messages;
    private ResourceBundle rb_gui;
    public final static String FrameID = "FrameID";

    public DetailsFrame(JTabbedPane parentPane) {
        rb_messages = ResourceBundle.getBundle("messages");
        rb_gui = ResourceBundle.getBundle("gui_names");

        phonesTable.setModel(new PhoneNumbersTableModel());
        phonesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        phonesTable.setVisible(true);
        phonesTable.getTableHeader().setReorderingAllowed(false);
        phonesTable.getTableHeader().setReorderingAllowed(false);
        phonesTable.getTableHeader().setDefaultRenderer(new TableHeaderRenderer(phonesTable));

        xButton.addActionListener(e -> {
            parentPane.remove(mainPanel);
            logger.debug("Closing tab for contact ID: " + contact.getID().toString());
        });
        setContactTextFieldsEnabled(false);

        repaintTitledComponents();
        contactSaveButton.addActionListener(event -> contactSaveButtonPressed());
        contactEditButton.addActionListener(event -> contactEditButtonPressed());
        clearPhoneDataButton.addActionListener(event -> clearPhoneDataButtonPressed());
        phoneNumberUpsertButton.addActionListener(event -> phoneNumberUpsertButtonPressed());
        phoneNumberEditButton.addActionListener(event -> phoneNumberEditButtonPressed());
        phoneNumberDeleteButton.addActionListener(event -> phoneNumberDeleteButtonPressed());
    }

    private void createUIComponents() {
        contactBirthdayDatePicker = createDatePicker();
    }

    private void repaintTitledComponents() {
        contactSaveButton.setText(rb_gui.getString("SAVE_CONTACT"));
        contactEditButton.setText(rb_gui.getString("EDIT_CONTACT"));
        clearPhoneDataButton.setText(rb_gui.getString("CLEAR_DATA"));
        phoneNumberUpsertButton.setText(rb_gui.getString("ADD_PHONE"));
        phoneNumberEditButton.setText(rb_gui.getString("EDIT_PHONE"));
        phoneNumberDeleteButton.setText(rb_gui.getString("DELETE_PHONE"));

        firstNameLabel.setText(rb_gui.getString("FIRST_NAME"));
        surnameLabel.setText(rb_gui.getString("SURNAME"));
        emailLabel.setText(rb_gui.getString("PRIMARY_EMAIL"));
        birthdayLabel.setText(rb_gui.getString("BIRTHDAY"));
        typeLabel.setText(rb_gui.getString("CONTACT_TYPE"));
        numberLabel.setText(rb_gui.getString("NUMBER"));
        countryCodeLabel.setText(rb_gui.getString("COUNTRY_CODE"));
    }

    public PhoneNumbersTableModel getPhoneNumbersTableModel() {
        return (PhoneNumbersTableModel) phonesTable.getModel();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void contactEditButtonPressed() {
        setContactTextFieldsEnabled(!contactFirstNameTextField.isEnabled());

        if (!contactFirstNameTextField.isEnabled()) {
            setContact(contact);
        }
    }

    private void contactSaveButtonPressed() {
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

    private void phoneNumberDeleteButtonPressed() {
        int selectedRow = phonesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(phoneNumberDeleteButton, rb_messages.getString("NO_ROW_SELECTED"));
            return;
        }

        setPhoneNumbersButtonsEnabled(false);

        PhoneNumbersTableModel model = getPhoneNumbersTableModel();
        PhoneNumber phone = model.getPhoneNumberAt(selectedRow);

        new RemovePhoneNumberWorker(phone, this).execute();
    }

    private void phoneNumberEditButtonPressed() {
        int selectedRow = phonesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(phoneNumberDeleteButton, rb_messages.getString("NO_ROW_SELECTED"));
            return;
        }
        setEditMode(true);

        PhoneNumber phone = getPhoneNumbersTableModel().getPhoneNumberAt(selectedRow);
        phoneCountryCodeTextField.setText(phone.getCountryCode());
        phoneNumberTextField.setText(phone.getNumber());
        phoneTypeTextField.setText(phone.getPhoneType());
        phoneInEditID = phone.getID();
    }


    private void phoneNumberUpsertButtonPressed() {
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

        if (phoneNumberUpsertButton.getText().equals(rb_gui.getString("CONFIRM_EDIT"))) {
            phone.setID(phoneInEditID);
            new EditPhoneNumberWorker(phone, this).execute();
        } else {
            new AddPhoneNumberWorker(contact, phone, this).execute();
        }

    }

    private void clearPhoneDataButtonPressed() {
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
            clearPhoneDataButton.setText(rb_gui.getString("CANCEL_EDIT"));
            phoneNumberUpsertButton.setText(rb_gui.getString("CONFIRM_EDIT"));
        } else {
            clearPhoneDataButton.setText(rb_gui.getString("CLEAR_DATA"));
            phoneNumberUpsertButton.setText(rb_gui.getString("ADD_PHONE"));
        }
    }

    private void setContactTextFieldsEnabled(boolean value) {
        contactFirstNameTextField.setEnabled(value);
        contactSurnameTextField.setEnabled(value);
        contactPrimaryEmailTextField.setEnabled(value);
        contactBirthdayDatePicker.getComponent(1).setEnabled(value);

        if (contactFirstNameTextField.isEnabled()) {
            contactEditButton.setText(rb_gui.getString("CANCEL_EDIT"));
        } else {
            contactEditButton.setText(rb_gui.getString("EDIT_CONTACT"));
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
