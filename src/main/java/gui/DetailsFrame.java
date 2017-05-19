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
    private JComboBox<RBComboItemPhoneType> phoneTypeComboBox;
    private JTextField phoneNumberTextField;
    private JTextField phoneCountryCodeTextField;
    private JButton contactSaveButton;
    private JButton contactEditButton;
    private JButton clearPhoneDataButton;
    private JButton phoneNumberUpsertButton;
    private JButton phoneNumberEditButton;
    private JButton phoneNumberDeleteButton;
    private JLabel firstNameLabel;
    private JLabel surnameLabel;
    private JLabel emailLabel;
    private JLabel birthdayLabel;
    private JLabel typeLabel;
    private JLabel numberLabel;
    private JLabel countryCodeLabel;
    private JButton xButton;

    private Contact contact;
    private Long phoneInEditID;
    private static final Logger logger = LoggerFactory.getLogger(DetailsFrame.class.getName());
    private ResourceBundle rbMessages;
    private ResourceBundle rbGui;
    public final static String FrameID = "FrameID";

    public DetailsFrame(JTabbedPane parentPane) {
        rbMessages = ResourceBundle.getBundle("messages");
        rbGui = ResourceBundle.getBundle("gui_names");

        phonesTable.setModel(new PhoneNumbersTableModel());
        phonesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        phonesTable.setVisible(true);
        phonesTable.getTableHeader().setReorderingAllowed(false);
        phonesTable.getTableHeader().setReorderingAllowed(false);
        phonesTable.getTableHeader().setDefaultRenderer(new TableHeaderRenderer(phonesTable));

        phoneTypeComboBox.addItem(new RBComboItemPhoneType("FAMILY"));
        phoneTypeComboBox.addItem(new RBComboItemPhoneType("FRIENDS"));
        phoneTypeComboBox.addItem(new RBComboItemPhoneType("WORK"));
        RBComboItemPhoneType nullRBComboItemPhoneType = new RBComboItemPhoneType(null);
        phoneTypeComboBox.addItem(nullRBComboItemPhoneType);
        phoneTypeComboBox.setSelectedItem(nullRBComboItemPhoneType);
        phoneTypeComboBox.setEditable(false);

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
        contactSaveButton.setText(rbGui.getString("SAVE_CONTACT"));
        contactEditButton.setText(rbGui.getString("EDIT_CONTACT"));
        clearPhoneDataButton.setText(rbGui.getString("CLEAR_DATA"));
        phoneNumberUpsertButton.setText(rbGui.getString("ADD_PHONE"));
        phoneNumberEditButton.setText(rbGui.getString("EDIT_PHONE"));
        phoneNumberDeleteButton.setText(rbGui.getString("DELETE_PHONE"));

        firstNameLabel.setText(rbGui.getString("FIRST_NAME"));
        surnameLabel.setText(rbGui.getString("SURNAME"));
        emailLabel.setText(rbGui.getString("PRIMARY_EMAIL"));
        birthdayLabel.setText(rbGui.getString("BIRTHDAY"));
        typeLabel.setText(rbGui.getString("CONTACT_TYPE"));
        numberLabel.setText(rbGui.getString("NUMBER"));
        countryCodeLabel.setText(rbGui.getString("COUNTRY_CODE"));
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
            JOptionPane.showMessageDialog(phoneNumberDeleteButton, rbMessages.getString("NO_ROW_SELECTED"));
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
            JOptionPane.showMessageDialog(phoneNumberDeleteButton, rbMessages.getString("NO_ROW_SELECTED"));
            return;
        }
        setEditMode(true);

        PhoneNumber phone = getPhoneNumbersTableModel().getPhoneNumberAt(selectedRow);
        phoneCountryCodeTextField.setText(phone.getCountryCode());
        phoneNumberTextField.setText(phone.getNumber());
        phoneTypeComboBox.setSelectedItem(new RBComboItemPhoneType(phone.getPhoneType()));
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

        if (phoneNumberUpsertButton.getText().equals(rbGui.getString("CONFIRM_EDIT"))) {
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
                .phoneType(((RBComboItemPhoneType) phoneTypeComboBox.getSelectedItem()).getValue())
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
        phoneTypeComboBox.setSelectedItem(new RBComboItemPhoneType(null));
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
            clearPhoneDataButton.setText(rbGui.getString("CANCEL_EDIT"));
            phoneNumberUpsertButton.setText(rbGui.getString("CONFIRM_EDIT"));
        } else {
            clearPhoneDataButton.setText(rbGui.getString("CLEAR_DATA"));
            phoneNumberUpsertButton.setText(rbGui.getString("ADD_PHONE"));
        }
    }

    private void setContactTextFieldsEnabled(boolean value) {
        contactFirstNameTextField.setEnabled(value);
        contactSurnameTextField.setEnabled(value);
        contactPrimaryEmailTextField.setEnabled(value);
        contactBirthdayDatePicker.getComponent(1).setEnabled(value);

        if (contactFirstNameTextField.isEnabled()) {
            contactEditButton.setText(rbGui.getString("CANCEL_EDIT"));
        } else {
            contactEditButton.setText(rbGui.getString("EDIT_CONTACT"));
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
