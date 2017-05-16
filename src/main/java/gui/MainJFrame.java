package gui;

import common.ValidationException;
import contactmanager.Contact;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import workers.AddContactWorker;
import workers.ContactDownloadWorker;
import workers.RemoveContactWorker;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.Properties;
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
    private JLabel Name;
    private JLabel Surname;
    private JLabel Email;
    private JLabel Birthday;
    private JTextField contactFirstNameTextField;
    private JTextField contactSurnameTextField;
    private JTextField contactPrimaryEmailTextField;
    private JDatePickerImpl contactBirthdayDatePicker;
    private JButton contactAddButton;
    private JButton clearDataButton;


    public MainJFrame() {
        init();
    }

    public void init() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        pack();
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
    }

    private void contactDetailsButtonPressed(ActionEvent event) {

    }

    private void contactDeleteButtonPressed(ActionEvent event) {
        int selectedRow = contactsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(contactDeleteButton, ResourceBundle.getBundle("messages").getString("noDataSelected"));
            return;
        }

        setContactsButtonsEnabled(false);

        ContactsTableModel model = (ContactsTableModel) contactsTable.getModel();
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

    private Contact getContactFromAddForm() {
        String birthday = contactBirthdayDatePicker.getJFormattedTextField().getText();

        return new Contact.Builder()
                .firstName(getNonEmptyTextOrNull(contactFirstNameTextField))
                .surname(getNonEmptyTextOrNull(contactSurnameTextField))
                .primaryEmail(getNonEmptyTextOrNull(contactPrimaryEmailTextField))
                .birthday(birthday.equals("") ? null: LocalDate.parse(birthday))
                .build();
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

    public void clearNewContactData() {
        contactFirstNameTextField.setText("");
        contactSurnameTextField.setText("");
        contactPrimaryEmailTextField.setText("");
        contactBirthdayDatePicker.getModel().setValue(null);
    }

    private void createUIComponents() {
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");

        JDatePanelImpl jDatePanel = new JDatePanelImpl(model, p);
        contactBirthdayDatePicker = new JDatePickerImpl(jDatePanel, new DateLabelFormatter());
    }

    /**
     * Returns nonempty text from JTextField or null in case it is an empty String.
     *
     * @param textField: textfield to return the value from.
     * @return: nonempty string from textField or null.
     */
    private String getNonEmptyTextOrNull(JTextField textField) {
        return textField.getText().equals("") ? null: textField.getText();
    }
}
