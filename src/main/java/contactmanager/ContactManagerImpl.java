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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author  David Frankl
 */
public class ContactManagerImpl implements ContactManager {

	private static final Logger logger = Logger.getLogger(
			ContactManagerImpl.class.getName());

	private DataSource dataSource;
	private final Clock clock;

	public ContactManagerImpl(Clock clock) {
		this.clock = clock;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private void checkDataSource() {
		if (dataSource == null) {
			throw new IllegalStateException("DataSource is not set.");
		}
	}

	public void createContact(Contact contact) {
		checkDataSource();
		validateContact(contact);
		if (contact.getID() != null) {
			throw new IllegalEntityException("Contact ID is already set.");
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

			Long id = DBUtils.getId(st.getGeneratedKeys());
			contact.setID(id);
			connection.commit();
		} catch (SQLException ex) {
			String msg = String.format("Error when inserting contact %s into DB.", contact.getFirstName());
			logger.log(Level.SEVERE, msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.doRollbackQuietly(connection);
			DBUtils.closeQuietly(connection, st);
		}
	}

	public Contact getContact(Long ID) {

		checkDataSource();

		if (ID == null) {
			throw new IllegalArgumentException("ID is null.");
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
			String msg = String.format("Error when getting contact with ID: %s from the DB.", ID);
			logger.log(Level.SEVERE, msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.closeQuietly(connection, statement);
		}
    }

	public void updateContact(Contact contact) {
		checkDataSource();
		validateContact(contact);

		if (contact.getID() == null) {
			throw new IllegalEntityException("Contact ID is null.");
		}
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
		} catch (SQLException ex) {
			String msg = String.format("Error when updating contact: %s  in the DB.", contact.getFirstName());
			logger.log(Level.SEVERE, msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.doRollbackQuietly(connection);
			DBUtils.closeQuietly(connection, st);
		}
	}

	public void deleteContact(Contact contact) {
		checkDataSource();
		if (contact == null) {
			throw new IllegalArgumentException("Contact is null.");
		}
		if (contact.getID() == null) {
			throw new IllegalEntityException("Contact ID is null.");
		}
		Connection connection = null;
		PreparedStatement st = null;

		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			st = connection.prepareStatement(
					"DELETE FROM Contact WHERE id = ?");
			st.setLong(1, contact.getID());

			int count = st.executeUpdate();
			DBUtils.checkUpdatesCount(count, contact, false);
			connection.commit();
		} catch (SQLException ex) {
			String msg = String.format("Error when deleting contact: %s from the DB", contact.getFirstName());
			logger.log(Level.SEVERE, msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.doRollbackQuietly(connection);
			DBUtils.closeQuietly(connection, st);
		}
	}

	public List<Contact> findAllContacts() {
		checkDataSource();
		Connection connection = null;
		PreparedStatement st = null;

		try {
			connection = dataSource.getConnection();
			st = connection.prepareStatement(
					"SELECT id, first_name, surname, primary_email, birthday FROM Contact");
			return executeQueryForMultipleContacts(st);
		} catch (SQLException ex) {
			String msg = "Error when getting all contacts from DB";
			logger.log(Level.SEVERE, msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.closeQuietly(connection, st);
		}
	}

	public List<Contact> findContactsByName(String name) {
		checkDataSource();
		Connection connection = null;
		PreparedStatement st = null;

		if (name == null) {
			throw new IllegalArgumentException("Name characters are null.");
		}

		try {
			connection = dataSource.getConnection();
			st = connection.prepareStatement(
					"SELECT id, first_name, surname, primary_email, birthday FROM Contact WHERE first_name LIKE ?");
			st.setString(1, name + "%");
			return executeQueryForMultipleContacts(st);
		} catch (SQLException ex) {
			String msg = String.format("Error when finding all contacts starting with name: %s", name);
			logger.log(Level.SEVERE, msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.closeQuietly(connection, st);
		}
	}

	public List<Contact> findContactsByNumber(String number) {
		checkDataSource();
		Connection connection = null;
		PreparedStatement st = null;

		if (number == null) {
			throw new IllegalArgumentException("Number characters are null.");
		}

		try {
			connection = dataSource.getConnection();
			st = connection.prepareStatement(
					"SELECT c.id, c.first_name, c.surname, c.primary_email, c.birthday FROM Contact AS c " +
							"JOIN PhoneNumber AS p ON c.id = p.contact_id WHERE number LIKE ?");
			st.setString(1, number + "%");
			return executeQueryForMultipleContacts(st);
		} catch (SQLException ex) {
			String msg = String.format("Error when finding all contacts starting with number: %s", number);
			logger.log(Level.SEVERE, msg, ex);
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
	private void validateContact(Contact contact) {
		if (contact == null) {
			throw new IllegalArgumentException("Contact is null.");
		}

		if (contact.getFirstName() == null && contact.getSurname() == null) {
			throw new ValidationException("Both first name and surname is null.");
		}

		LocalDate today = LocalDate.now(clock);
		if (contact.getBirthday().isAfter(today)) {
			throw new ValidationException("Birthday is in future");
		}
	}

	static private Contact executeQueryForSingleContact(PreparedStatement st) throws SQLException {
		ResultSet set = st.executeQuery();
		if (set.next()) {
			Contact contact = rowToContact(set);
			if (set.next()) {
				throw new ServiceFailureException(
						"DB integrity error: two or more contacts have the same ID");
			}
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