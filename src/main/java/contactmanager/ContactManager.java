package contactmanager;

import common.IllegalEntityException;
import common.ServiceFailureException;
import common.ValidationException;
import java.util.List;

/**
 * This service allows managing contacts and contact numbers
 *
 * @author David Frankl
 */
public interface ContactManager {

	/**
	 * Stores new contact into database. ID for the new contact is automatically
	 * generated and stored into ID attribute.
	 *
	 * @param contact: contact to be created.
	 * @throws IllegalArgumentException when contact is null.
	 * @throws IllegalEntityException when contact is has already assigned ID.
	 * @throws ValidationException when the contact breaks validation rules(first
	 * and second names are both null, birthday is after current LocalDate future).
	 * @throws ServiceFailureException when db operation fails.
	 */
	void createContact(Contact contact);

	/**
	 * Returns contact with given ID.
	 *
	 * @param ID primary key of requested contact.
	 * @return contact with given ID or null if such contact does not exist.
	 * @throws IllegalArgumentException when given ID is null.
	 * @throws ServiceFailureException when db operation fails.
	 */
    Contact getContact(Long ID);

	/**
	 * Updates the phone in database.
	 *
	 * @param contact: contact to be updated in database.
	 * @throws IllegalArgumentException when contact is null.
	 * @throws IllegalEntityException when contact has null ID or does
	 * not exist in the database.
	 * @throws ValidationException when contact breaks validation rules(first
	 * and second names are both null, birthday is after current LocalDate future).
	 * @throws ServiceFailureException when db operation fails.
	 */
	void updateContact(Contact contact);

	/**
	 * Removes the contact from the DB (and all of its corresponding phones).
	 *
	 * @param contact: contact to be removed from DB.
	 * @throws IllegalArgumentException when contact is null.
	 * @throws IllegalEntityException when given contact has
	 * null ID or do not exist in database.
	 * @throws ServiceFailureException when db operation fails.
	 */
	void deleteContact(Contact contact);

	/**
	 * Returns list of all contacts in the database.
	 *
	 * @return list of all contacts in database.
	 * @throws ServiceFailureException when db operation fails.
	 */
	List<Contact> findAllContacts();

	/**
	 * Returns list of all contacts which name starts with name param.
	 *
	 * @param name: Characters or full name by which to search for contacts.
	 * @return list of all contacts with name starting with name param.
	 * @throws IllegalArgumentException when name is null.
	 * @throws ServiceFailureException when db operation fails.
	 */
	List<Contact> findContactsByName(String name);

	/**
	 * Returns list of all contacts which phone starts with number.
	 *
	 * @param number: Characters or full number by which to search for contacts.
	 * @return list of all contacts with phone starting with number.
	 * @throws IllegalArgumentException when number is null.
	 * @throws ServiceFailureException when db operation fails.
	 */
	List<Contact> findContactsByNumber(String number);
}