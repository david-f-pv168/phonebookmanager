package contactmanager;

import common.IllegalEntityException;
import common.ServiceFailureException;
import common.ValidationException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static contactmanager.CheckHelpers.*;

/**
 * @author  David Frankl
 */
public class PhoneNumberManagerImpl implements PhoneNumberManager {

	private static final Logger logger = LoggerFactory.getLogger(PhoneNumberManagerImpl.class.getName());
	private DataSource dataSource;
	private String msg;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public List<PhoneNumber> getPhoneNumbers(Contact contact) {
		checkDataSourceNotNull(dataSource, logger);
		Connection connection = null;
		PreparedStatement st = null;

		checkContactNotNull(contact, logger);
		checkContactIDNotNull(contact, logger);

		try {
			connection = dataSource.getConnection();
			st = connection.prepareStatement(
					"SELECT id, number, country_code, phone_type FROM PhoneNumber WHERE contact_id = ?");
			st.setLong(1, contact.getID());
			return executeQueryForMultiplePhones(st);
		} catch (SQLException ex) {
			msg = String.format("Error when getting all phones from DB for contact %s", contact.getFirstName());
			logger.error(msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.closeQuietly(connection, st);
		}
	}

	public PhoneNumber getPhoneNumber(Long ID) {
		checkDataSourceNotNull(dataSource, logger);

		if (ID == null) {
			String errText = "Phone ID is null.";
			logger.error(errText);
			throw new IllegalArgumentException(errText);
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
			msg = "Error when getting phone with ID: " + ID + " from the DB.";
			logger.error(msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.closeQuietly(connection, statement);
		}
	}

	public void addPhone(Contact contact, PhoneNumber phone) {
		checkDataSourceNotNull(dataSource, logger);
		validatePhone(phone);

		checkContactNotNull(contact, logger);
		checkContactIDNotNull(contact, logger);

		if (phone.getID() != null) {
			String errText = "Phone ID is already set.";
			logger.error(errText);
			throw new IllegalEntityException(errText);
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
			DBUtils.checkUpdatesCount(count, phone, true);

			Long id = DBUtils.getId(st.getGeneratedKeys());
			phone.setID(id);
			connection.commit();
			logger.info("Added phone with ID: " + phone.getID().toString());
		} catch (SQLException ex) {
			msg = String.format("Error when inserting %s's phone into DB.", contact.getFirstName());
			logger.error(msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.doRollbackQuietly(connection);
			DBUtils.closeQuietly(connection, st);
		}
	}

	public void removePhone(PhoneNumber phone) {
		checkDataSourceNotNull(dataSource, logger);
		checkPhoneNotNull(phone, logger);
		if (phone.getID() == null) {
			String errText = "Phone ID is null.";
			logger.error(errText);
			throw new IllegalEntityException(errText);
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
			logger.info("Removed phone with ID: " + phone.getID().toString());
		} catch (SQLException ex) {
			msg = "Error when deleting phone from the DB";
			logger.error(msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.doRollbackQuietly(connection);
			DBUtils.closeQuietly(connection, st);
		}
	}

	public void updatePhone(PhoneNumber phone) {
		checkDataSourceNotNull(dataSource, logger);
		validatePhone(phone);

		if (phone.getID() == null) {
			String errText = "Phone ID is null.";
			logger.error(errText);
			throw new IllegalEntityException(errText);
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
			logger.info("Updated phone with ID: " + phone.getID().toString());
		} catch (SQLException ex) {
			msg = "Error when updating phone in the DB.";
			logger.error(msg, ex);
			throw new ServiceFailureException(msg, ex);
		} finally {
			DBUtils.doRollbackQuietly(connection);
			DBUtils.closeQuietly(connection, st);
		}
	}

	/**
	 * Validates phone.
	 *
	 * @param phone: Phone to be validated.
	 * @throws IllegalArgumentException if phone is null.
	 * @throws ValidationException if either phone number or country code is null.
	 */
	public void validatePhone(PhoneNumber phone) {
		checkPhoneNotNull(phone, logger);

		if (phone.getNumber() == null || phone.getCountryCode() == null) {
			throw new ValidationException(ResourceBundle.getBundle("messages").getString("CODE_NUMBER_EMPTY"));
		}
	}

	static private PhoneNumber executeQueryForSinglePhone(PreparedStatement st) throws SQLException {
		ResultSet set = st.executeQuery();
		if (set.next()) {
			PhoneNumber phone = rowToPhoneNumber(set);
			if (set.next()) {
				String msg = "DB integrity error: two or more contacts have the same ID";
				logger.error(msg);
				throw new ServiceFailureException(msg);
			}

			logger.info("Retrieved phone with ID: " + phone.getID().toString());
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

		logger.info(String.format("Retrieved %d phone numbers", phones.size()));
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