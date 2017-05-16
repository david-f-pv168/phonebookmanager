package web;

import contactmanager.ContactManagerImpl;
import contactmanager.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.SQLException;
import java.time.Clock;

@WebListener
public class StartListener implements ServletContextListener {

    private final static Logger log = LoggerFactory.getLogger(StartListener.class);

    @Override
    public void contextInitialized(ServletContextEvent ev) {
        log.info("Web app initialised");
        ServletContext servletContext = ev.getServletContext();
        ContactManagerImpl contactManager = new ContactManagerImpl(Clock.systemDefaultZone());

        contactManager.setDataSource(DBUtils.createMemoryDatabaseWithTables(true));

        servletContext.setAttribute("contactManager", contactManager);
        log.info("Contact manager created and stored to servletContext.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent ev) {
        log.info("The application closes.");
    }
}
