package gui;

import contactmanager.Contact;
import org.assertj.core.internal.cglib.core.Local;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by David on 16-May-17.
 */
public class DetailsFrame {
    private JPanel mainPanel;
    private JTextField contactFirstNameTextField;
    private JTextField contactSurnameTextField;
    private JTextField contactPrimaryEmailTextField;
    private JTextField contactBirthdayTextField;
    private JButton editPhoneButton;
    private JButton editContactButton;
    private JTable phonesTable;
    private JTextField phoneTypeTextField;
    private JTextField phoneNumberTextField;
    private JTextField phoneCountryCodeTextField;
    private JButton button3;
    private JButton phoneAddButton;
    private JLabel surnameLabel;
    private JLabel emailLabel;
    private JLabel birthdayLabel;
    private JLabel typeLabel;
    private JLabel numberLabel;
    private JLabel countryCodeLabel;
    private JButton xButton;

    public final static String FrameID = "FrameID";

    public DetailsFrame(JTabbedPane parentPane) {
        phonesTable.setModel(new PhoneNumbersTableModel());
        phonesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        phonesTable.setVisible(true);
        phonesTable.getTableHeader().setReorderingAllowed(false);

        xButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentPane.remove(mainPanel);
            }
        });
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

        LocalDate birthday = contact.getBirthday();
        contactBirthdayTextField.setText(birthday != null ? birthday.format(DateTimeFormatter.ISO_DATE): null);
    }
}
