package contactmanager;

import common.IllegalEntityException;
import common.ServiceFailureException;
import common.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.net.URL;
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
public class PhoneNumberManagerImplTest {

    private ContactManagerImpl contactManager;
    private PhoneNumberManagerImpl phoneManager;
    private DataSource ds;

    @Before
    public void setUp() throws SQLException, java.net.MalformedURLException {
        ds = Main.createMemoryDatabaseWithTables(false);
        phoneManager = new PhoneNumberManagerImpl();
        phoneManager.setDataSource(ds);

        contactManager = new ContactManagerImpl(Clock.systemDefaultZone());
        contactManager.setDataSource(ds);
        prepareTestSharedData();
    }

    @After
    public void tearDown() throws SQLException, java.net.MalformedURLException {
        DBUtils.executeSqlScript(ds, Main.class.getResource("/dropTables.sql"));
    }

    private Contact.Builder sample_house_builder() {
        return new Contact.Builder()
                .ID(null)
                .firstName("Gregory")
                .surname("House")
                .primaryEmail("gregory.house@md.com")
                .birthday(LocalDate.parse("2000-01-01"));
    }

    private PhoneNumber.Builder sample_czk_phone_builder() {
        return new PhoneNumber.Builder()
                .ID(null)
                .countryCode("+420")
                .number("777888999")
                .phoneType("Family");
    }

    private PhoneNumber.Builder sample_svk_phone_builder() {
        return new PhoneNumber.Builder()
                .ID(null)
                .countryCode("+421")
                .number("999888777")
                .phoneType("Family");
    }

    // Prepare test data
    private Contact contactWithNullID, contactNotInDB;
    private PhoneNumber phoneWithNullID, phoneWithID, phoneNotInDB;

    private void prepareTestSharedData() {
        contactWithNullID = sample_house_builder().build();
        contactNotInDB = sample_house_builder().ID(-1L).build();
        assertThat(contactManager.getContact(contactNotInDB.getID())).isNull();

        phoneWithID = new PhoneNumber.Builder().ID(1L).number("test").countryCode("test").build();
        phoneWithNullID = new PhoneNumber.Builder().ID(null).number("test").countryCode("test").build();
        phoneNotInDB = new PhoneNumber.Builder().ID(0L).number("test").countryCode("test").build();
        assertThat(phoneManager.getPhoneNumber(phoneNotInDB.getID())).isNull();
    }

    @Test
    public void getPhoneNumbers() {
        Contact c = sample_house_builder().build();
        PhoneNumber p1 = sample_czk_phone_builder().build();
        PhoneNumber p2 = sample_svk_phone_builder().build();

        contactManager.createContact(c);
        phoneManager.addPhone(c, p1);
        phoneManager.addPhone(c, p2);

        assertThat(phoneManager.getPhoneNumbers(c))
                .usingFieldByFieldElementComparator()
                .containsOnly(p1, p2);
    }

    @Test
    public void getPhoneNumbersNullContact() {
        assertThatThrownBy(() -> phoneManager.getPhoneNumbers(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void getPhoneNumbersContactWithNullID() {
        assertThatThrownBy(() -> phoneManager.getPhoneNumbers(contactWithNullID))
                .isInstanceOf(IllegalEntityException.class);
    }

    @Test
    public void getPhoneWithNullID() {
        assertThatThrownBy(() -> phoneManager.getPhoneNumber(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void addPhone() {
        Contact c = sample_house_builder().build();
        PhoneNumber p = sample_czk_phone_builder().build();

        contactManager.createContact(c);
        phoneManager.addPhone(c, p);

        Long phoneID = p.getID();
        assertThat(phoneID).isNotNull();

        assertThat(phoneManager.getPhoneNumber(phoneID))
                .isNotSameAs(p)
                .isEqualToComparingFieldByField(p);
    }

    @Test
    public void addNullPhone() {
        Contact c = sample_house_builder().build();
        contactManager.createContact(c);

        assertThatThrownBy(() -> phoneManager.addPhone(c, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void addPhoneToNullContact() {
        assertThatThrownBy(() -> phoneManager.addPhone(null, phoneWithNullID))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void addPhoneWithID() {
        Contact c = sample_house_builder().build();
        contactManager.createContact(c);

        assertThatThrownBy(() -> phoneManager.addPhone(c, phoneWithID))
                .isInstanceOf(IllegalEntityException.class);
    }

    @Test
    public void addPhoneToNotInDBContact() {
        assertThat(contactManager.getContact(contactNotInDB.getID())).isNull();

        assertThatThrownBy(() -> phoneManager.addPhone(contactNotInDB, phoneWithNullID))
                .isInstanceOf(ServiceFailureException.class);
    }

    @Test
    public void addPhoneToContactWithNullID() {
        assertThat(contactWithNullID.getID()).isNull();

        assertThatThrownBy(() -> phoneManager.addPhone(contactWithNullID, phoneWithNullID))
                .isInstanceOf(IllegalEntityException.class);
    }

    @Test
    public void addPhoneWithNullCountryCode() {
        Contact c = sample_house_builder().build();
        PhoneNumber p = sample_czk_phone_builder().countryCode(null).build();

        contactManager.createContact(c);

        assertThatThrownBy(() -> phoneManager.addPhone(c, p))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void addPhoneWithNullPhoneNumber() {
        Contact c = sample_house_builder().build();
        PhoneNumber p = sample_czk_phone_builder().number(null).build();

        contactManager.createContact(c);

        assertThatThrownBy(() -> phoneManager.addPhone(c, p))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void removePhone() {
        Contact c = sample_house_builder().build();
        PhoneNumber p = sample_czk_phone_builder().build();

        contactManager.createContact(c);
        phoneManager.addPhone(c, p);

        Long phoID = p.getID();
        assertThat(phoneManager.getPhoneNumber(phoID)).isNotNull();

        phoneManager.removePhone(p);
        assertThat(phoneManager.getPhoneNumber(phoID)).isNull();
    }

    @Test
    public void removeNullPhone() {
        assertThatThrownBy(() -> phoneManager.removePhone(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void removePhoneWithNullID() {
        assertThatThrownBy(() -> phoneManager.removePhone(phoneWithNullID))
                .isInstanceOf(IllegalEntityException.class);
    }

    @Test
    public void removePhoneNotInDB() {
        assertThatThrownBy(() -> phoneManager.removePhone(phoneNotInDB))
                .isInstanceOf(IllegalEntityException.class);
    }

    @Test
    public void updatePhone() {
        Contact c = sample_house_builder().build();
        PhoneNumber phoneForUpdate = sample_czk_phone_builder().build();
        PhoneNumber anotherPhone = sample_svk_phone_builder().build();

        contactManager.createContact(c);
        phoneManager.addPhone(c, phoneForUpdate);
        phoneManager.addPhone(c, anotherPhone);

        phoneForUpdate.setNumber("New Number");
        phoneManager.updatePhone(phoneForUpdate);

        assertThat(phoneManager.getPhoneNumber(phoneForUpdate.getID()))
                .isEqualToComparingFieldByField(phoneForUpdate);

        assertThat(phoneManager.getPhoneNumber(anotherPhone.getID()))
                .isEqualToComparingFieldByField(anotherPhone);
    }

    @Test
    public void updateNullPhone() {
        assertThatThrownBy(() -> phoneManager.updatePhone(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void updatePhoneWithNullID() {
        assertThatThrownBy(() -> phoneManager.updatePhone(phoneWithNullID))
                .isInstanceOf(IllegalEntityException.class);
    }

    @Test
    public void updateNotInDBPhone() {
        assertThatThrownBy(() -> phoneManager.updatePhone(phoneNotInDB))
                .isInstanceOf(IllegalEntityException.class);
    }

    @Test
    public void updatePhoneWithNullCountryCode() {
        PhoneNumber p = sample_czk_phone_builder().countryCode(null).build();

        assertThatThrownBy(() -> phoneManager.updatePhone(p))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void updatePhoneWithNullNumber() {
        PhoneNumber p = sample_czk_phone_builder().number(null).build();

        assertThatThrownBy(() -> phoneManager.updatePhone(p))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void checkPhonesDeletedOnContactDeletion() {
        Contact c = sample_house_builder().build();
        contactManager.createContact(c);

        PhoneNumber p = sample_svk_phone_builder().build();
        phoneManager.addPhone(c, p);

        Long pID = p.getID();

        contactManager.deleteContact(c);
        assertThat(phoneManager.getPhoneNumber(pID)).isNull();
    }

    @FunctionalInterface
    private interface Operation<T> {
        void callOn(T subjectOfOperation);
    }

    private void testExpectedServiceFailureException(Operation<PhoneNumberManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        phoneManager.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.callOn(phoneManager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void getPhoneNumbersWithCorruptedDataSource() throws SQLException {
        Contact c = sample_house_builder().build();
        contactManager.createContact(c);

        PhoneNumber p = sample_czk_phone_builder().build();
        phoneManager.addPhone(c, p);

        testExpectedServiceFailureException((phoneManager) -> phoneManager.getPhoneNumbers(c));
    }

    @Test
    public void getPhoneNumberWithCorruptedDataSource() throws SQLException {
        Contact c = sample_house_builder().build();
        contactManager.createContact(c);

        PhoneNumber p = sample_svk_phone_builder().build();
        phoneManager.addPhone(c, p);

        testExpectedServiceFailureException((contactManager) -> contactManager.getPhoneNumber(p.getID()));
    }

    @Test
    public void addPhoneWithCorruptedDataSource() throws SQLException {
        Contact c = sample_house_builder().build();
        contactManager.createContact(c);
        PhoneNumber p = sample_svk_phone_builder().build();

        testExpectedServiceFailureException((contactManager) -> contactManager.addPhone(c, p));
    }

    @Test
    public void removePhoneWithCorruptedDataSource() throws SQLException {
        Contact c = sample_house_builder().build();
        contactManager.createContact(c);
        PhoneNumber p = sample_svk_phone_builder().build();
        phoneManager.addPhone(c, p);

        testExpectedServiceFailureException((contactManager) -> contactManager.removePhone(p));
    }

    @Test
    public void updatePhoneWithCorruptedDataSource() throws SQLException {
        Contact c = sample_house_builder().build();
        contactManager.createContact(c);
        PhoneNumber p = sample_svk_phone_builder().build();
        phoneManager.addPhone(c, p);

        testExpectedServiceFailureException((contactManager) -> contactManager.updatePhone(p));
    }
}

