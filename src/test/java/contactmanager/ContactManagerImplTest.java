package contactmanager;

import common.IllegalEntityException;
import common.ServiceFailureException;
import common.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link ContactManagerImpl}.
 *
 * Created by David Frankl on 15-Mar-17.
 */
public class ContactManagerImplTest {

    private ContactManagerImpl contactManager;
    private PhoneNumberManagerImpl phoneManager;
    private DataSource ds;

    // Clock mock object set to always return date as 01-01-2017
    private final static LocalDate NOW = LocalDate.of(2017, 1, 1);

    private static Instant prepareClockMock(LocalDate now) {
        return now.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    @Before
    public void setUp() throws SQLException, java.net.MalformedURLException {
        ds = DBUtils.createMemoryDatabaseWithTables(false);
        contactManager = new ContactManagerImpl(Clock.fixed(prepareClockMock(NOW), ZoneId.of("UTC")));
        contactManager.setDataSource(ds);

        phoneManager = new PhoneNumberManagerImpl();
        phoneManager.setDataSource(ds);

        prepareTestSharedData();
    }

    @After
    public void tearDown() throws SQLException, java.net.MalformedURLException {
        DBUtils.executeSqlScript(ds, DBUtils.class.getResource("/dropTables.sql"));
    }

    private Contact.Builder sample_house_builder() {
        return new Contact.Builder()
                .ID(null)
                .firstName("Gregory")
                .surname("House")
                .primaryEmail("gregory.house@md.com")
                .birthday(LocalDate.parse("2000-01-01"));
    }

    private Contact.Builder sample_cuddy_builder() {
        return new Contact.Builder()
                .ID(null)
                .firstName("Lisa")
                .surname("Cuddy")
                .primaryEmail("lisa.cuddy@md.com")
                .birthday(LocalDate.parse("2001-01-01"));
    }

    private PhoneNumber.Builder sample_czk_phone_builder() {
        return new PhoneNumber.Builder()
                .ID(null)
                .countryCode("+420")
                .number("777888999")
                .phoneType("Family");
    }

    // Prepare test data
    private Contact contactWithNullID, contactWithID, contactNotInDB,
            contactWithNullNames, contactBorfAfterCurrentLocalTime;

    private void prepareTestSharedData() {
        contactWithID = sample_house_builder().ID(1L).build();
        contactWithNullID = sample_house_builder().build();
        contactWithNullNames = sample_house_builder().firstName(null).surname(null).build();
        contactBorfAfterCurrentLocalTime = sample_house_builder().birthday(LocalDate.MAX).build();
        contactNotInDB = sample_house_builder().ID(-1L).build();
        assertThat(contactManager.getContact(contactNotInDB.getID())).isNull();
    }

    @Test
    public void createContact() {
        Contact contact = sample_house_builder().build();
        contactManager.createContact(contact);

        Long contactID = contact.getID();
        assertThat(contactID).isNotNull();

        assertThat(contactManager.getContact(contactID))
                .isNotSameAs(contact)
                .isEqualToComparingFieldByField(contact);
    }

    @Test
    public void createContactWithNullBirthday() {
        Contact contact = sample_house_builder().birthday(null).build();
        contactManager.createContact(contact);

        Long contactID = contact.getID();
        assertThat(contactID).isNotNull();

        assertThat(contactManager.getContact(contactID))
                .isNotSameAs(contact)
                .isEqualToComparingFieldByField(contact);
    }

    @Test
    public void createContactWithID() {
        assertThatThrownBy(() -> contactManager.createContact(contactWithID))
                .isInstanceOf(IllegalEntityException.class);
    }

    @Test
    public void createNullContact() {
        assertThatThrownBy(() -> contactManager.createContact(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void createContactWithNullNames() {
        assertThatThrownBy(() -> contactManager.createContact(contactWithNullNames))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void createContactBornAfterCurrentLocalTime() {
        assertThatThrownBy(() -> contactManager.createContact(contactBorfAfterCurrentLocalTime))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void getContactWithNullID() {
        assertThatThrownBy(() -> contactManager.getContact(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void updateContact() {
        Contact contactForUpdate = sample_house_builder().build();
        Contact anotherContact = sample_house_builder().firstName("Lisa").build();
        contactManager.createContact(contactForUpdate);
        contactManager.createContact(anotherContact);

        contactForUpdate.setFirstName("New Name");
        contactManager.updateContact(contactForUpdate);

        assertThat(contactManager.getContact(contactForUpdate.getID()))
                .isEqualToComparingFieldByField(contactForUpdate);

        assertThat(contactManager.getContact(anotherContact.getID()))
                .isEqualToComparingFieldByField(anotherContact);
    }

    @Test
    public void updateNullContact() {
        assertThatThrownBy(() -> contactManager.updateContact(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void updateContactWithNullID() {
        assertThatThrownBy(() -> contactManager.updateContact(contactWithNullID))
                .isInstanceOf(IllegalEntityException.class);
    }

    @Test
    public void updateContactWithNullNames() {
        assertThatThrownBy(() -> contactManager.updateContact(contactWithNullNames))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void updateContactNotInDB() {
        assertThatThrownBy(() -> contactManager.updateContact(contactNotInDB))
                .isInstanceOf(IllegalEntityException.class);
    }

    @Test
    public void updateBornAfterCurrentLocalTime() {
        assertThatThrownBy(() -> contactManager.updateContact(contactBorfAfterCurrentLocalTime))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void deleteContact() {
        Contact contact = sample_house_builder().build();

        contactManager.createContact(contact);
        assertThat(contactManager.getContact(contact.getID())).isNotNull();

        contactManager.deleteContact(contact);
        assertThat(contactManager.getContact(contact.getID())).isNull();
    }

    @Test
    public void deleteNullContact() {
        assertThatThrownBy(() -> contactManager.deleteContact(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void deleteContactWithNullID() {
        assertThatThrownBy(() -> contactManager.deleteContact(contactWithNullID))
                .isInstanceOf(IllegalEntityException.class);
    }

    @Test
    public void deleteContactNotInDB() {
        assertThatThrownBy(() -> contactManager.deleteContact(contactNotInDB))
                .isInstanceOf(IllegalEntityException.class);
    }

    @Test
    public void findAllContacts() {
        Contact c1 = sample_cuddy_builder().build();
        Contact c2 = sample_house_builder().build();

        assertThat(contactManager.findAllContacts()).isEmpty();

        contactManager.createContact(c1);
        contactManager.createContact(c2);

        assertThat(contactManager.findAllContacts())
                .usingFieldByFieldElementComparator()
                .containsOnly(c1,c2);
    }

    @Test
    public void findContactsByName() {
        assertThat(contactManager.findAllContacts()).isEmpty();

        String searched_name = "Randy";

        Contact c1 = sample_house_builder().firstName(searched_name).surname("Marsh").build();
        Contact c2 = sample_house_builder().firstName(searched_name).surname("Broflovski").build();
        Contact c3 = sample_house_builder().firstName("Mr").surname("Hat").build();

        contactManager.createContact(c1);
        contactManager.createContact(c2);
        contactManager.createContact(c3);

        assertThat(contactManager.findContactsByName(searched_name))
                .usingFieldByFieldElementComparator()
                .containsOnly(c1, c2);
    }

    @Test
    public void findContactsByNullName() {
        assertThatThrownBy(() -> contactManager.findContactsByName(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void findContactsByNumber() {
        assertThat(contactManager.findAllContacts()).isEmpty();

        Contact c1 = sample_house_builder().build();
        Contact c2 = sample_cuddy_builder().build();
        Contact c3 = sample_house_builder().firstName("Wilson").build();

        contactManager.createContact(c1);
        contactManager.createContact(c2);
        contactManager.createContact(c3);

        String phoneNumberStartsWith = "777";

        PhoneNumber p1 = sample_czk_phone_builder().number(phoneNumberStartsWith + "123456").build();
        PhoneNumber p2 = sample_czk_phone_builder().number(phoneNumberStartsWith + "456789").build();
        PhoneNumber p3 = sample_czk_phone_builder().number("888" + "123456").build();

        phoneManager.addPhone(c1, p1);
        phoneManager.addPhone(c2, p2);
        phoneManager.addPhone(c3, p3);

        assertThat(contactManager.findContactsByNumber(phoneNumberStartsWith))
                .usingFieldByFieldElementComparator()
                .containsOnly(c1, c2);
    }

    @Test
    public void findContactsByNullNumber() {
        assertThatThrownBy(() -> contactManager.findContactsByNumber(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @FunctionalInterface
    private interface Operation<T> {
        void callOn(T subjectOfOperation);
    }

    private void testExpectedServiceFailureException(Operation<ContactManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        contactManager.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.callOn(contactManager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void createContactWithCorruptedDataSource() throws SQLException {
        Contact c = sample_house_builder().build();
        testExpectedServiceFailureException((contactManager) -> contactManager.createContact(c));
    }

    @Test
    public void getContactWithCorruptedDataSource() throws SQLException {
        Contact c = sample_house_builder().build();
        contactManager.createContact(c);
        testExpectedServiceFailureException((contactManager) -> contactManager.getContact(c.getID()));
    }

    @Test
    public void updateContactWithCorruptedDataSource() throws SQLException {
        Contact c = sample_house_builder().build();
        contactManager.createContact(c);
        testExpectedServiceFailureException((contactManager) -> contactManager.updateContact(c));
    }

    @Test
    public void deleteContactWithCorruptedDataSource() throws SQLException {
        Contact c = sample_house_builder().build();
        contactManager.createContact(c);
        testExpectedServiceFailureException((contactManager) -> contactManager.deleteContact(c));
    }

    @Test
    public void findAllContactsWithCorruptedDataSource() throws SQLException {
        Contact c = sample_house_builder().build();
        contactManager.createContact(c);
        testExpectedServiceFailureException(ContactManager::findAllContacts);
    }

    @Test
    public void findContactsByNameWithCorruptedDataSource() throws SQLException {
        Contact c = sample_house_builder().build();
        contactManager.createContact(c);
        testExpectedServiceFailureException((contactManager) -> contactManager.findContactsByName(c.getFirstName()));
    }

    @Test
    public void findContactsByNumberWithCorruptedDataSource() throws SQLException {
        Contact c = sample_house_builder().build();
        contactManager.createContact(c);

        PhoneNumber p = sample_czk_phone_builder().build();
        phoneManager.addPhone(c, p);

        testExpectedServiceFailureException((contactManager) -> contactManager.findContactsByNumber(p.getNumber()));
    }
}

