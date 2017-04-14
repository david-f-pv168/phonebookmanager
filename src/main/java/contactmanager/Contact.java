package contactmanager;

import java.time.LocalDate;
import java.util.Objects;

/**
 * This entity class represents Contact. Contact has first name,
 * surname, email, and date of birth. Contacts are associated with phone numbers:
 * one contact can have many phone numbers.
 *
 * @author David Frankl
 */
public class Contact {

    private Long ID;
    private String firstName;
	private String surname;
    private String primaryEmail;
	private LocalDate birthday;

    public Contact() {
    }

    public Contact(Builder builder) {
        this.ID = builder.ID;
        this.firstName = builder.firstName;
        this.surname = builder.surname;
        this.primaryEmail = builder.primaryEmail;
        this.birthday = builder.birthday;
    }

    public Long getID() {
        return ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public void setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "Contact{"
                + "ID: " + ID
                + ", first name: " + firstName
                + ", surname: " + surname
                + ", email: " + primaryEmail
                + ", birthday: " + birthday
                + '}';
    }

    /**
     * Returns true if obj represents the same contact. Two objects are considered
     * to represent the same contact when both are instances of {@link Contact}
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
        final Contact other = (Contact) obj;
        return !(obj != this && this.ID == null) && Objects.equals(this.ID, other.ID);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.ID);
    }

    /**
     * Builder for class Contact
     */
    public static class Builder {

        private Long ID;
        private String firstName;
        private String surname;
        private String primaryEmail;
        private LocalDate birthday;

        public Builder(){
        }

        public Builder ID(Long ID) {
            this.ID = ID;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder surname(String surname) {
            this.surname = surname;
            return this;
        }

        public Builder primaryEmail(String email) {
            this.primaryEmail = email;
            return this;
        }

        public Builder birthday(LocalDate birthday) {
            this.birthday = birthday;
            return this;
        }

        public Contact build() {
            return new Contact(this);
        }
    }
}

