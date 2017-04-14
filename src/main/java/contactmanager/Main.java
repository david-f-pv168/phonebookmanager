package contactmanager;

import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import java.sql.SQLException;

public class Main {

    public static DataSource createMemoryDatabaseWithTables(boolean withData) throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:contactsDB;create=true");
        DBUtils.executeSqlScript(ds, Main.class.getResource("/createTables.sql"));
        
        if (withData) {
            DBUtils.executeSqlScript(ds, Main.class.getResource("/populateTables.sql"));    
        }
        
        return ds;
    }
}
