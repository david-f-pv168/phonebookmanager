package contactmanager;

import common.IllegalEntityException;
import common.ServiceFailureException;
import common.ValidationException;
import javax.sql.DataSource;
import java.sql.*;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static contactmanager.CheckHelpers.*;

/**
 * @author  David Frankl
 */
public class ContactManagerImpl implements ContactManager {

	private static final Logger logger = LoggerFactory.getLogger(ContactManagerImpl.class.getName());

	private DataSource dataSource;
	private final Clock clock;
	private String msg;

	public ContactManagerImpl(Clock clock) {
		this.clock = clock;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void createContact(Contact contact) {
		checkDataSourceNotNull(dataSource, logger);
		validateContact(contact);
		if (contact.getID() != null) {
			msg = "Contact ID is already set.";
			logger.error(msg);
			throw new IllegalEntityException(msg);
		}
		Connection connection = null;
		PreparedStatement st = null;

		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			st = connection.prepareStatement(
					"INSERT INTO Contact (first_name, surname, primary_email, birthday) VALUES (?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			st.setString(1, contact.getFirstName());
			st.setString(2, contact.getSurname());
			st.setString(3, contact.getPrimaryEmail());
			st.setDate(4, DBUtils.toSqlDate(contact.getBirthday()));

			int count = st.executeUpdate();
			DBUtils.checkUpdatesCount(count, contact, true);

			Long ID = DBUtils.getId(st.getGeneratedKeys());
			contact.setID(ID);
			connection.commit();
			logger.info("Contact with id " + ID.toString() + "created");
		} catch (SQLException ex) {
			msg = String.format("Error when inserting contact %s into DB.", contact.getFirstName());
			logger.error(msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.doRollbackQuietly(connection);
			DBUtils.closeQuietly(connection, st);
		}
	}

	public Contact getContact(Long ID) {

		checkDataSourceNotNull(dataSource, logger);

		if (ID == null) {
			msg = "Contact ID is null.";
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}

		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(
					"SELECT id, first_name, surname, primary_email, birthday FROM Contact WHERE id = ?");
			statement.setLong(1, ID);
			return executeQueryForSingleContact(statement);
		} catch (SQLException ex) {
			msg = String.format("Error when getting contact with ID: %s from the DB.", ID);
			logger.error(msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.closeQuietly(connection, statement);
		}
    }

	public void updateContact(Contact contact) {
		checkDataSourceNotNull(dataSource, logger);
		validateContact(contact);
		checkContactIDNotNull(contact, logger);

		Connection connection = null;
		PreparedStatement st = null;

		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			st = connection.prepareStatement(
					"UPDATE Contact SET first_name = ?, surname = ?, primary_email = ?, birthday = ? WHERE id = ?");
			st.setString(1, contact.getFirstName());
			st.setString(2, contact.getSurname());
			st.setString(3, contact.getPrimaryEmail());
			st.setDate(4, DBUtils.toSqlDate(contact.getBirthday()));
			st.setLong(5, contact.getID());

			int count = st.executeUpdate();
			DBUtils.checkUpdatesCount(count, contact, false);
			connection.commit();
			logger.info("Updated contact with id " + contact.toString());
		} catch (SQLException ex) {
			msg = String.format("Error when updating contact: %s  in the DB.", contact.getFirstName());
			logger.error(msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.doRollbackQuietly(connection);
			DBUtils.closeQuietly(connection, st);
		}
	}

	public void deleteContact(Contact contact) {
		checkDataSourceNotNull(dataSource, logger);
		checkContactNotNull(contact, logger);
		checkContactIDNotNull(contact, logger);

		Connection connection = null;
		PreparedStatement st = null;

		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			st = connection.prepareStatement("DELETE FROM Contact WHERE id = ?");
			st.setLong(1, contact.getID());

			int count = st.executeUpdate();
			DBUtils.checkUpdatesCount(count, contact, false);
			connection.commit();
			logger.info("Deleted contact with id " + contact.toString());
		} catch (SQLException ex) {
			msg = String.format("Error when deleting contact: %s from the DB", contact.getFirstName());
			logger.error(msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.doRollbackQuietly(connection);
			DBUtils.closeQuietly(connection, st);
		}
	}

	public List<Contact> findAllContacts() {
		checkDataSourceNotNull(dataSource, logger);
		Connection connection = null;
		PreparedStatement st = null;

		logger.info("Retrieving all contacts");

		try {
			connection = dataSource.getConnection();
			st = connection.prepareStatement(
					"SELECT id, first_name, surname, primary_email, birthday FROM Contact");
			return executeQueryForMultipleContacts(st);
		} catch (SQLException ex) {
			msg = "Error when getting all contacts from DB";
			logger.error(msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.closeQuietly(connection, st);
		}
	}

	public List<Contact> findContactsByName(String name) {
		checkDataSourceNotNull(dataSource, logger);
		Connection connection = null;
		PreparedStatement st = null;

		logger.info("Retrieving contacts starting with '" + name + "'");

		if (name == null) {
			msg = "Name characters are null.";
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}

		try {
			connection = dataSource.getConnection();
			st = connection.prepareStatement(
					"SELECT id, first_name, surname, primary_email, birthday FROM Contact WHERE first_name LIKE ?");
			st.setString(1, name + "%");
			return executeQueryForMultipleContacts(st);
		} catch (SQLException ex) {
			msg = String.format("Error when finding all contacts starting with name: %s", name);
			logger.error(msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.closeQuietly(connection, st);
		}
	}

	public List<Contact> findContactsByNumber(String number) {
		checkDataSourceNotNull(dataSource, logger);
		Connection connection = null;
		PreparedStatement st = null;

		logger.info("Retrieving contacts with number starting with '" + number + "'");

		if (number == null) {
			msg = "Number characters are null.";
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}

		try {
			connection = dataSource.getConnection();
			st = connection.prepareStatement(
					"SELECT c.id, c.first_name, c.surname, c.primary_email, c.birthday FROM Contact AS c " +
							"JOIN PhoneNumber AS p ON c.id = p.contact_id WHERE number LIKE ?");
			st.setString(1, number + "%");
			return executeQueryForMultipleContacts(st);
		} catch (SQLException ex) {
			msg = String.format("Error when finding all contacts starting with number: %s", number);
			logger.error(msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.closeQuietly(connection, st);
		}
	}

	/**
	 * Validates contact
	 *
	 * @param contact: Contact to be validated
	 * @throws IllegalArgumentException if contact is null
	 * @throws ValidationException if both first name and surname are null or
	 * when birthday is after current LocalDate
	 */
	public void validateContact(Contact contact) {
		checkContactNotNull(contact, logger);

		if (contact.getFirstName() == null && contact.getSurname() == null) {
			throw new ValidationException("Both first name and surname is null");
		}

		LocalDate today = LocalDate.now(clock);
		if (contact.getBirthday() != null && contact.getBirthday().isAfter(today)) {
			throw new ValidationException("Birthday is in future");
		}
	}

	static private Contact executeQueryForSingleContact(PreparedStatement st) throws SQLException {
		ResultSet set = st.executeQuery();
		if (set.next()) {
			Contact contact = rowToContact(set);
			if (set.next()) {
				String msg = "DB integrity error: two or more contacts have the same ID";
				logger.error(msg);
				throw new ServiceFailureException(msg);
			}

			logger.info("Retrieved contact with id " + contact.getID().toString());
			return contact;
		} else {
			return null;
		}
	}

	private static List<Contact> executeQueryForMultipleContacts(PreparedStatement st) throws SQLException {
		ResultSet set = st.executeQuery();
		List<Contact> contacts = new ArrayList<>();

		while (set.next()) {
			contacts.add(rowToContact(set));
		}
		logger.info(String.format("Retrieved %d contacts", contacts.size()));
		return contacts;
	}

	static private Contact rowToContact(ResultSet set) throws SQLException {
		Contact contact = new Contact();
		contact.setID(set.getLong("id"));
		contact.setFirstName(set.getString("first_name"));
		contact.setSurname(set.getString("surname"));
		contact.setPrimaryEmail(set.getString("primary_email"));
		contact.setBirthday(DBUtils.toLocalDate(set.getDate("birthday")));

		return contact;
	}
}