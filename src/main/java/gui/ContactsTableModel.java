package gui;

/**
 * Created by David on 14-May-17.
 */

import contactmanager.Contact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.List;
import java.util.Objects;

/**
 * Table model for Contact entities Vojtech Sassmann on 3. 5. 2017.
 */
public class ContactsTableModel extends AbstractTableModel {

    final static Logger log = LoggerFactory.getLogger(ContactsTableModel.class);

    private List<Contact> contacts = new ArrayList<>();

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
        fireTableDataChanged();
    }

    private enum Column {
        FIRST_NAME(String.class, Contact::getFirstName),
        SURNAME(String.class, Contact::getSurname),
        PRIMARY_EMAIL(String.class, Contact::getPrimaryEmail),
        BIRTHDAY(LocalDate.class, Contact::getBirthday);

        private Class<?> type;
        private Function<Contact, ?> dataFunction;

        Column(Class<?> type, Function<Contact, ?> dataFunction) {
            this.type = type;
            this.dataFunction = dataFunction;
        }
    }

    private Contact findContactWithID(Long ID) {
        for(Contact cont : contacts) {
            if (Objects.equals(cont.getID(), ID)) {
                return cont;
            }
        }
        throw new IllegalArgumentException("Contact with ID " + ID.toString() + "was not found.");
    }

    public void editContact(Contact editedContact) {
        editedContact.setFirstName(editedContact.getFirstName());
        editedContact.setSurname(editedContact.getSurname());
        editedContact.setPrimaryEmail(editedContact.getPrimaryEmail());
        editedContact.setBirthday(editedContact.getBirthday());

        fireTableDataChanged();
    }

    public Contact getContactAt(int index) {
        return contacts.get(index);//TODO :
    }

    public void addContact(Contact contact) {
        contacts.add(contact);
        int lastRow = contacts.size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }

    public void removeContact(Contact contact) {
        contacts.remove(contact);
        int lastRow = contacts.size() - 1;
        fireTableRowsDeleted(lastRow, lastRow);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Column.values()[columnIndex].type;
    }

    @Override
    public int getRowCount() {
        return contacts.size();
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public String getColumnName(int column) {
        return Column.values()[column].name();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Contact contact = contacts.get(rowIndex);
        return Column.values()[columnIndex].dataFunction.apply(contact);
    }
}

