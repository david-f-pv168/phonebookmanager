package contactmanager;

import common.IllegalEntityException;
import common.ServiceFailureException;
import common.ValidationException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author  David Frankl
 */
public class PhoneNumberManagerImpl implements PhoneNumberManager {

	private static final Logger logger = Logger.getLogger(
			PhoneNumberManagerImpl.class.getName());

	private DataSource dataSource;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private void checkDataSource() {
		if (dataSource == null) {
			throw new IllegalStateException("DataSource is not set.");
		}
	}

	public List<PhoneNumber> getPhoneNumbers(Contact contact) {
		checkDataSource();
		Connection connection = null;
		PreparedStatement st = null;

		if (contact == null) {
			throw new IllegalArgumentException("Contact is null.");
		}

		if (contact.getID() == null) {
			throw new IllegalEntityException("Contact ID is null.");
		}

		try {
			connection = dataSource.getConnection();
			st = connection.prepareStatement(
					"SELECT id, number, country_code, phone_type FROM PhoneNumber WHERE contact_id = ?");
			st.setLong(1, contact.getID());
			return executeQueryForMultiplePhones(st);
		} catch (SQLException ex) {
			String msg = String.format("Error when getting all phones from DB for contact %s", contact.getFirstName());
			logger.log(Level.SEVERE, msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.closeQuietly(connection, st);
		}
	}

	public PhoneNumber getPhoneNumber(Long ID) {
		checkDataSource();

		if (ID == null) {
			throw new IllegalArgumentException("ID is null.");
		}

		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(
					"SELECT id, number, country_code, phone_type FROM PhoneNumber WHERE id = ?");
			statement.setLong(1, ID);
			return executeQueryForSinglePhone(statement);
		} catch (SQLException ex) {
			String msg = "Error when getting phone with ID: " + ID + " from the DB.";
			logger.log(Level.SEVERE, msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.closeQuietly(connection, statement);
		}
	}

	public void addPhone(Contact contact, PhoneNumber phone) {
		checkDataSource();
		validatePhone(phone);

		if (contact == null) {
			throw new IllegalArgumentException("Contact is null.");
		}

		if (phone.getID() != null) {
			throw new IllegalEntityException("Phone ID is already set.");
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
					"INSERT INTO PhoneNumber (number, country_code, phone_type, contact_id) VALUES (?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			st.setString(1, phone.getNumber());
			st.setString(2, phone.getCountryCode());
			st.setString(3, phone.getPhoneType());
			st.setLong(4, contact.getID());

			int count = st.executeUpdate();
			DBUtils.checkUpdatesCount(count, contact, true);

			Long id = DBUtils.getId(st.getGeneratedKeys());
			phone.setID(id);
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

	public void removePhone(PhoneNumber phone) {
		checkDataSource();
		if (phone == null) {
			throw new IllegalArgumentException("Phone is null.");
		}
		if (phone.getID() == null) {
			throw new IllegalEntityException("Phone ID is null.");
		}
		Connection connection = null;
		PreparedStatement st = null;

		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			st = connection.prepareStatement(
					"DELETE FROM PhoneNumber WHERE id = ?");
			st.setLong(1, phone.getID());

			int count = st.executeUpdate();
			DBUtils.checkUpdatesCount(count, phone, false);
			connection.commit();
		} catch (SQLException ex) {
			String msg = "Error when deleting phone from the DB";
			logger.log(Level.SEVERE, msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.doRollbackQuietly(connection);
			DBUtils.closeQuietly(connection, st);
		}
	}

	public void updatePhone(PhoneNumber phone) {
		checkDataSource();
		validatePhone(phone);

		if (phone.getID() == null) {
			throw new IllegalEntityException("Phone ID is null.");
		}
		Connection connection = null;
		PreparedStatement st = null;

		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			st = connection.prepareStatement(
					"UPDATE PhoneNumber SET number = ?, country_code = ?, phone_type = ? WHERE id = ?");
			st.setString(1, phone.getNumber());
			st.setString(2, phone.getCountryCode());
			st.setString(3, phone.getPhoneType());
			st.setLong(4, phone.getID());

			int count = st.executeUpdate();
			DBUtils.checkUpdatesCount(count, phone, false);
			connection.commit();
		} catch (SQLException ex) {
			String msg = "Error when updating phone in the DB.";
			logger.log(Level.SEVERE, msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.doRollbackQuietly(connection);
			DBUtils.closeQuietly(connection, st);
		}
	}

	/**
	 * Validates phone.
	 *
	 * @param phone: Contact to be validated.
	 * @throws IllegalArgumentException if contact is null.
	 * @throws ValidationException if either phone number or country code is null.
	 */
	public void validatePhone(PhoneNumber phone) {
		if (phone == null) {
			throw new IllegalArgumentException("Phone is null.");
		}

		if (phone.getNumber() == null || phone.getCountryCode() == null) {
			throw new ValidationException("Either phone number or country code is null.");
		}
	}

	static private PhoneNumber executeQueryForSinglePhone(PreparedStatement st) throws SQLException {
		ResultSet set = st.executeQuery();
		if (set.next()) {
			PhoneNumber phone = rowToPhoneNumber(set);
			if (set.next()) {
				throw new ServiceFailureException(
						"DB integrity error: two or more contacts have the same ID");
			}
			return phone;
		} else {
			return null;
		}
	}

	private static List<PhoneNumber> executeQueryForMultiplePhones(PreparedStatement st) throws SQLException {
		ResultSet set = st.executeQuery();
		List<PhoneNumber> phones = new ArrayList<>();

		while (set.next()) {
			phones.add(rowToPhoneNumber(set));
		}
		return phones;
	}

	static private PhoneNumber rowToPhoneNumber(ResultSet set) throws SQLException {
		PhoneNumber phone = new PhoneNumber();
		phone.setID(set.getLong("id"));
		phone.setNumber(set.getString("number"));
		phone.setCountryCode(set.getString("country_code"));
		phone.setPhoneType(set.getString("phone_type"));

		return phone;
	}
}