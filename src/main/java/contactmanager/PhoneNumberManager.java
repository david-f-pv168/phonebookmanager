package contactmanager;

import common.IllegalEntityException;
import common.ServiceFailureException;
import common.ValidationException;

import java.util.List;

/**
 * This service allows managing contact numbers
 *
 * @author David Frankl
 */
public interface PhoneNumberManager {

	/**
	 * Returns list of phones belonging to a contact.
	 *
	 * @param contact: contact to which the phones belongs to.
	 * @throws IllegalArgumentException when the contact is null.
	 * @throws IllegalEntityException when the contact ID is null.
	 * @throws ServiceFailureException when db operation fails.
	 */
	List<PhoneNumber> getPhoneNumbers(Contact contact);

	/**
	 * Returns phone with given ID.
	 *
	 * @param ID primary key of requested phone.
	 * @return phone with given ID or null if such phone does not exist.
	 * @throws IllegalArgumentException when given ID is null.
	 * @throws ServiceFailureException when db operation fails.
	 */
	PhoneNumber getPhoneNumber(Long ID);

	/**
	 * Adds phone belonging to existing contact into database. ID for the new
	 * phone is automatically generated and stored into ID attribute.
	 *
	 * @param phone: phone to be added to the DB.
	 * @param contact: contact to which the phone belongs to.
	 * @throws IllegalArgumentException when the contact or the phone is null.
	 * @throws IllegalEntityException when the phone has already assigned ID or
	 * the contact ID is null.
	 * @throws ValidationException when the phone breaks validation rules(
	 * number or country code are null).
	 * @throws ServiceFailureException when db operation fails.
	 */
	void addPhone(Contact contact, PhoneNumber phone);

	/**
	 * Removes the phone from the DB (and therefore from corresponding contact).
	 *
	 * @param phone: phone to be removed from DB.
	 * @throws IllegalArgumentException when phone is null.
	 * @throws IllegalEntityException when given phone has
	 * null ID or do not exist in database.
	 * @throws ServiceFailureException when db operation fails.
	 */
	void removePhone(PhoneNumber phone);

	/**
	 * Updates the phone in database.
	 *
	 * @param phone: phone to be updated in database.
	 * @throws IllegalArgumentException when phone is null.
	 * @throws IllegalEntityException when phone has null ID or does
	 * not exist in the database.
	 * @throws ValidationException when phone breaks validation rules
	 * (number or country code are null).
	 * @throws ServiceFailureException when db operation fails.
	 */
	void updatePhone(PhoneNumber phone);
}