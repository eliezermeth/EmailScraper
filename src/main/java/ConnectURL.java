import java.sql.*;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Eliezer Meth
 * @version 1
 * Start Date: 05.26.2020
 *
 * see https://docs.microsoft.com/en-us/sql/connect/jdbc/connection-url-sample?view=sql-server-ver15
 */

public class ConnectURL {

    public static void addEmails(Collection col)
    {
        // see https://docs.microsoft.com/en-us/sql/connect/jdbc/connection-url-sample?view=sql-server-ver15
        // Create a variable for the connection string.
        String url = "database-1.cbjmpwcdjfmq.us-east-1.rds.amazonaws.com:1433"; // should pull from AWS Secrets Manager, environment variable, Properties class (key, value pairs)
        String connectionUrl =
                String.format("jdbc:sqlserver://%s;databaseName=meth;user=admin;password=mco368Touro", url);

        try (Connection con = DriverManager.getConnection(connectionUrl); // Autoclosable
                        Statement stmt = con.createStatement();) {
            String insert = "INSERT INTO Emails VALUES ('%s')";
            String query;

            Iterator iter = col.iterator();

            while (iter.hasNext()) // would be better doing a bulk insert
            {
                query = String.format(insert, iter.next());
                stmt.executeUpdate(query);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}