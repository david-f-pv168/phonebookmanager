package gui;

import java.util.ResourceBundle;

/**
 * Class representing a combo item for phone type combo box
 */
public class RBComboItemPhoneType {
    private String value;
    private String label;

    public RBComboItemPhoneType(String value) {
        this.value = value;

        if (value == null) {
            this.label = null;
        } else {
            this.label = ResourceBundle.getBundle("gui_names").getString(value);
        }
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RBComboItemPhoneType comboItem = (RBComboItemPhoneType) o;

        if (label != null ? !label.equals(comboItem.label) : comboItem.label != null) return false;
        if (value != null ? !value.equals(comboItem.value) : comboItem.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }
}