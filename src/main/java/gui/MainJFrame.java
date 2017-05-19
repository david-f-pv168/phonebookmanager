package gui;

import common.ValidationException;
import contactmanager.Contact;
import org.jdatepicker.impl.JDatePickerImpl;
import workers.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Created by David on 13-May-17.
 */
public class MainJFrame extends JFrame{
    private JPanel mainPanel;
    private JTabbedPane contactsPane;
    private JTable contactsTable;
    private JButton contactDetailsButton;
    private JButton contactDeleteButton;
    private JLabel nameLabel;
    private JLabel surnameLabel;
    private JLabel emailLabel;
    private JLabel birthdayLabel;
    private JTextField contactFirstNameTextField;
    private JTextField contactSurnameTextField;
    private JTextField contactPrimaryEmailTextField;
    private JDatePickerImpl contactBirthdayDatePicker;
    private JButton contactAddButton;
    private JButton clearDataButton;
    private JTextField searchByPhoneTextField;
    private JTextField searchByNameTextField;
    private JButton searchByPhoneButton;
    private JButton searchByNameButton;

    public enum SearchType {NAME, PHONE}

    public MainJFrame() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        setTitle("Contacts");
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        new ContactDownloadWorker(this).execute();

        contactDetailsButton.addActionListener(this::contactDetailsButtonPressed);
        contactDeleteButton.addActionListener(this::contactDeleteButtonPressed);
        contactAddButton.addActionListener(this::contactAddButtonPressed);
        clearDataButton.addActionListener(this::clearDataButtonPressed);

        contactsTable.setModel(new ContactsTableModel());
        contactsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactsTable.setVisible(true);
        contactsTable.getTableHeader().setReorderingAllowed(false);
        contactsTable.getTableHeader().setDefaultRenderer(new TableHeaderRenderer(contactsTable));

        searchByNameButton.addActionListener(this:: searchByNameButtonPressed);
        searchByPhoneButton.addActionListener(this:: searchByPhoneButtonPressed);
    }

    /**
     * Executes ContactDetailsWorker on a contact if the contact is not
     * yet displayed on details tab. Focuses on the contact otherwise.
     *
     * @param event: click event
     */
    private void contactDetailsButtonPressed(ActionEvent event) {
        int selectedRow = contactsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(contactDeleteButton, ResourceBundle.getBundle("messages").getString("noDataSelected"));
            return;
        }

        ContactsTableModel model = (ContactsTableModel) contactsTable.getModel();
        Contact contact = model.getContactAt(selectedRow);

        JPanel existingContactTab = findContactsTab(contact);
        if (existingContactTab != null) {
            contactsPane.setSelectedComponent(existingContactTab);
        } else {
            new ContactDetailsWorker(contact,this).execute();
        }
    }

    private void contactDeleteButtonPressed(ActionEvent event) {
        int selectedRow = contactsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(contactDeleteButton, ResourceBundle.getBundle("messages").getString("noDataSelected"));
            return;
        }

        setContactsButtonsEnabled(false);

        ContactsTableModel model = getContactsTableModel();
        Contact contact = model.getContactAt(selectedRow);

        new RemoveContactWorker(contact, this).execute();
    }

    private void contactAddButtonPressed(ActionEvent event) {
        setContactsButtonsEnabled(false);

        Contact contact = getContactFromAddForm();
        try {
            Main.getContactManager().validateContact(contact);
        }
        catch (ValidationException ex) {
            setContactsButtonsEnabled(true);
            JOptionPane.showMessageDialog(this, ex.getMessage());
            return;
        }

        new AddContactWorker(contact,this).execute();
    }

    private void clearDataButtonPressed(ActionEvent event) {
        clearNewContactData();
    }

    private void searchByNameButtonPressed(ActionEvent event) {
        new SearchContactWorker(searchByNameTextField.getText(), SearchType.NAME, this).execute();
    }

    private void searchByPhoneButtonPressed(ActionEvent event) {
        String phone_part = searchByPhoneTextField.getText();

        if (phone_part.equals("")) {
            // Return all contacts by searching for empty string by name
            new SearchContactWorker(phone_part, SearchType.NAME, this).execute();
        } else {
            new SearchContactWorker(phone_part, SearchType.PHONE, this).execute();
        }
    }

    public void setContactsButtonsEnabled(boolean value) {
        contactAddButton.setEnabled(value);
        contactDetailsButton.setEnabled(value);
        contactDeleteButton.setEnabled(value);
        clearDataButton.setEnabled(value);
    }

    public ContactsTableModel getContactsTableModel() {
        return (ContactsTableModel) contactsTable.getModel();
    }

    public JTabbedPane getContactsPane() {
        return contactsPane;
    }

    public void clearNewContactData() {
        contactFirstNameTextField.setText("");
        contactSurnameTextField.setText("");
        contactPrimaryEmailTextField.setText("");
        contactBirthdayDatePicker.getModel().setValue(null);
    }

    public JPanel findContactsTab(Contact contact) {
        for (Component tab: contactsPane.getComponents()) {
            Long panelID = (Long) ((JPanel) tab).getClientProperty(DetailsFrame.FrameID);
            if (panelID != null && Objects.equals(contact.getID(), panelID)) {
                return (JPanel) tab;
            }
        }

        return null;
    }

    private Contact getContactFromAddForm() {
        String birthday = contactBirthdayDatePicker.getJFormattedTextField().getText();

        return new Contact.Builder()
                .firstName(guiUtils.getNonEmptyTextOrNull(contactFirstNameTextField))
                .surname(guiUtils.getNonEmptyTextOrNull(contactSurnameTextField))
                .primaryEmail(guiUtils.getNonEmptyTextOrNull(contactPrimaryEmailTextField))
                .birthday(birthday.equals("") ? null: LocalDate.parse(birthday))
                .build();
    }

    private void createUIComponents() {
        contactBirthdayDatePicker = guiUtils.createDatePicker();
    }
}
