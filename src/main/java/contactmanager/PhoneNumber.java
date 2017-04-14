package contactmanager;

import java.util.Objects;

/**
 * This entity class represents phone number. Phone number has number part,
 * country code and type. Phone numbers are associated with contacts:
 * a phone number can belong only to one contact.
 *
 * @author David Frankl
 */

public class PhoneNumber {

    private Long ID;
    private String number;
	private String countryCode;
	private String phoneType;

    public PhoneNumber() {
    }

    public PhoneNumber(Builder phoneNumberBuilder) {
        this.ID = phoneNumberBuilder.ID;
        this.number = phoneNumberBuilder.number;
        this.countryCode = phoneNumberBuilder.countryCode;
        this.phoneType = phoneNumberBuilder.phoneType;
    }

    public Long getID() {
        return ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(String phoneType) {
        this.phoneType = phoneType;
    }

    @Override
    public String toString() {
        return "PhoneNUmber{"
                + "ID: " + ID
                + ", full number: " + countryCode + " " + number
                + ", phone type: " + phoneType
                + '}';
    }

    /**
     * Returns true if obj represents the same contact. Two objects are considered
     * to represent the same contact when both are instances of {@link PhoneNumber}
     * class, both have assigned some id and this id is the same.
     *
     *
     * @param obj the reference object with which to compare.
     * @return true if obj represents the same contact.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PhoneNumber other = (PhoneNumber) obj;
        return !(obj != this && this.ID == null) && Objects.equals(this.ID, other.ID);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.ID);
    }


    /**
     * Builder for class PhoneNumber
     */
    public static class Builder {

        private Long ID;
        private String number;
        private String countryCode;
        private String phoneType;

        public Builder ID(Long ID) {
            this.ID = ID;
            return this;
        }

        public Builder number(String number) {
            this.number = number;
            return this;
        }

        public Builder countryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public Builder phoneType(String phoneType) {
            this.phoneType = phoneType;
            return this;
        }

        public PhoneNumber build() {
            return new PhoneNumber(this);
        }
    }
}
