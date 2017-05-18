package gui;

import contactmanager.Contact;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.util.Properties;

/**
 * Created by David on 18-May-17.
 */
public class guiUtils {

    /**
     * Returns nonempty text from JTextField or null in case it is an empty String.
     *
     * @param textField: textfield to return the value from.
     * @return nonempty string from textField or null.
     */
    public static String getNonEmptyTextOrNull(JTextField textField) {
        return textField.getText().equals("") ? null: textField.getText();
    }

    /**
     * Creates initalised JDatePicker object.
     *
     * @return initalised JDatePicker object.
     */
    public static JDatePickerImpl createDatePicker() {
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");

        JDatePanelImpl jDatePanel = new JDatePanelImpl(model, p);
        return new JDatePickerImpl(jDatePanel, new DateLabelFormatter());
    }


    /**
     * Constructs a string from contact's first and second name (excludes null values)
     * which will be used as a title for its details tab
     *
     * @param contact the contact for which the title is being constructed
     * @return constructed title from contact's name
     */
    public static String getPaneTitleFromContact(Contact contact) {
        String title = "";

        if (contact.getFirstName() != null) {
            title = contact.getFirstName();
        }
        String surname = contact.getSurname();
        if (surname != null) {
            if (title.equals("")) {
                title = surname;
            } else {
                title += " " + surname;
            }
        }

        return title;
    }
}
