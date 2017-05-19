package gui;

import contactmanager.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class PhoneNumbersTableModel extends AbstractTableModel {

    private final static Logger logger = LoggerFactory.getLogger(PhoneNumbersTableModel.class);

    private List<PhoneNumber> phones = new ArrayList<>();

    private enum Column {
        COUNTRY_CODE(String.class, PhoneNumber::getCountryCode),
        NUMBER(String.class, PhoneNumber::getNumber),
        CONTACT_TYPE(String.class, PhoneNumber::getPhoneType);

        private Class<?> type;
        private Function<PhoneNumber, ?> dataFunction;

        Column(Class<?> type, Function<PhoneNumber, ?> dataFunction) {
            this.type = type;
            this.dataFunction = dataFunction;
        }
    }

    private PhoneNumber findPhoneNumberWithID(Long ID) {
        for(PhoneNumber phone : phones) {
            if (Objects.equals(phone.getID(), ID)) {
                return phone;
            }
        }
        String msg = "Phone with ID " + ID.toString() + "was not found.";
        logger.error(msg);
        throw new IllegalArgumentException(msg);
    }

    public void editPhoneNumber(PhoneNumber editedPhone) {
        PhoneNumber phone = findPhoneNumberWithID(editedPhone.getID());

        phone.setCountryCode(editedPhone.getCountryCode());
        phone.setNumber(editedPhone.getNumber());
        phone.setPhoneType(editedPhone.getPhoneType());

        fireTableDataChanged();
    }

    public PhoneNumber getPhoneNumberAt(int index) {
        return phones.get(index);
    }

    public void addPhoneNumber(PhoneNumber phone) {
        phones.add(phone);
        int lastRow = phones.size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }

    public void removePhoneNumber(PhoneNumber phone) {
        phones.remove(phone);
        int lastRow = phones.size() - 1;
        fireTableRowsDeleted(lastRow, lastRow);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Column.values()[columnIndex].type;
    }

    @Override
    public int getRowCount() {
        return phones.size();
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
        PhoneNumber phone = phones.get(rowIndex);
        return Column.values()[columnIndex].dataFunction.apply(phone);
    }
}

