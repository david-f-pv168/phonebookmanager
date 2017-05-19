package web;

import contactmanager.Contact;
import contactmanager.ContactManager;
import common.ServiceFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Servlet for managing contacts.
 *
 * @author David Frankl
 */
@WebServlet(ContactsServlet.URL_MAPPING + "/*")
public class ContactsServlet extends HttpServlet {

    private static final String LIST_JSP = "/list.jsp";
    public static final String URL_MAPPING = "/contacts";

    private final static Logger logger = LoggerFactory.getLogger(ContactsServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.debug("GET ...");
        showContactsList(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //support non-ASCII characters in form
        request.setCharacterEncoding("utf-8");
        //action specified by pathInfo
        String action = request.getPathInfo();
        logger.debug("POST ... {}",action);
        switch (action) {
            case "/add":
                //getting POST parameters from form
                String firstName = request.getParameter("firstName");
                String surname = request.getParameter("surname");
                String primaryEmail = request.getParameter("primaryEmail");
                String birthdayString = request.getParameter("birthday");
                LocalDate birthday = birthdayString.equals("") ? null : LocalDate.parse(birthdayString);

                //form data validity check
                if (firstName == null || firstName.length() == 0 && surname == null || surname.length() == 0) {
                    request.setAttribute("validation_error", "Please fill in first name or surname.");
                    logger.debug("form data invalid");
                    showContactsList(request, response);
                    return;
                }
                //form data processing - storing to database
                try {
                    Contact contact = new Contact.Builder()
                            .firstName(firstName)
                            .surname(surname)
                            .primaryEmail(primaryEmail)
                            .birthday(birthday)
                            .build();

                    getContactManager().createContact(contact);

                    //redirect-after-POST protects from multiple submission
                    logger.debug("redirecting after POST");
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (ServiceFailureException ex) {
                    logger.error("Cannot create contact. Please try again.", ex);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                    return;
                }
            case "/delete":
                try {
                    Long ID = Long.valueOf(request.getParameter("id"));
                    Contact contact = getContactManager().getContact(ID);
                    getContactManager().deleteContact(contact);
                    logger.debug("redirecting after POST");
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (ServiceFailureException ex) {
                    logger.error("Cannot delete book", ex);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                    return;
                }
            case "/update":
                //TODO
                return;
            default:
                logger.error("Unknown action " + action);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown action " + action);
        }
    }

    /**
     * Gets ContactManager from ServletContext, where it was stored by {@link StartListener}.
     *
     * @return BookManager instance
     */
    private ContactManager getContactManager() {
        return (ContactManager) getServletContext().getAttribute("contactManager");
    }

    /**
     * Stores the list of contacts to request attribute "contacts" and forwards to the JSP to display it.
     */
    private void showContactsList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            logger.debug("showing table of contacts");
            request.setAttribute("contacts", getContactManager().findAllContacts());
            request.getRequestDispatcher(LIST_JSP).forward(request, response);
        } catch (ServiceFailureException ex) {
            logger.error("Cannot display contacts", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

}