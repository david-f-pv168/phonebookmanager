package web;

import contactmanager.ContactManagerImpl;
import contactmanager.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.time.Clock;

@WebListener
public class StartListener implements ServletContextListener {

    private final static Logger logger = LoggerFactory.getLogger(StartListener.class);

    @Override
    public void contextInitialized(ServletContextEvent ev) {
        logger.info("Web app initialised");
        ServletContext servletContext = ev.getServletContext();
        ContactManagerImpl contactManager = new ContactManagerImpl(Clock.systemDefaultZone());

        contactManager.setDataSource(DBUtils.createDatabaseWithTables(true));

        servletContext.setAttribute("contactManager", contactManager);
        logger.info("Contact manager created and stored to servletContext.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent ev) {
        logger.info("The application closes.");
    }
}
