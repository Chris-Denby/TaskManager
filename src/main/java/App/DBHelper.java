package App;

import org.apache.derby.shared.common.error.DerbySQLIntegrityConstraintViolationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBHelper {
    /* the default framework is embedded */
    private static String dbName = "TASKSDB";
    private static String framework = "embedded";
    private static String protocol = "jdbc:derby:";
    final static String DATABASE_URL = "jdbc:derby:" + dbName + ";create=true;user=user;password=pass";
    private static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private static Connection connection;
    private static String tasksTableName = "TASKS";
    private static String notesTableName = "NOTES";
    private static String usersTableName = "USERS";

    public static void connectDB() {
        PreparedStatement psInsert;
        PreparedStatement psUpdate;
        ResultSet rs = null;
        ArrayList<Statement> statements = new ArrayList<Statement>(); // list of Statements, PreparedStatements
        Statement s;
        try {
            /*
             * This connection specifies create=true in the connection URL to
             * cause the database to be created when connecting for the first
             * time. To remove the database, remove the directory derbyDB (the
             * same as the database name) and its contents.
             *
             * The directory derbyDB will be created under the directory that
             * the system property derby.system.home points to, or the current
             * directory (user.dir) if derby.system.home is not set.
             */
            connection = DriverManager.getConnection(protocol + dbName
                    + ";create=true");
            // We want to control transactions manually. Autocommit is on by default in JBDC
            connection.setAutoCommit(false);
            System.out.println("Connected to database: " + dbName);

            //check if table exists - if not create it
            DatabaseMetaData dbm = connection.getMetaData();

            rs = dbm.getTables(null, null, tasksTableName, null);
            if (!rs.next())
            {
                //Creating a statement object that we can use for running various SQL statements commands against the database
                s = connection.createStatement();
                statements.add(s);
                // We create a table...
                s.execute("create table " + tasksTableName + "(" +
                        "primaryKey int GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                        "internalReference varchar(40)," +
                        "customerReference varchar(40)," +
                        "messageId varchar(40)," +
                        "receivedDate varchar(40)," +
                        "respondedDate varchar(40)," +
                        "customer varchar(40)," +
                        "revenue decimal(9,2)," +
                        "state varchar(40)," +
                        "preparedBy varchar(40)," +
                        "priority int," +
                        "orderNum int," +
                        "PRIMARY KEY(primaryKey)" +
                        ")");
                connection.commit();
                System.out.println("TASKS table doesnt exist - Created...");
            }
            else
            {
                System.out.println("TASKS table already exists");
                connection.commit();
            }

            rs = dbm.getTables(null, null, notesTableName, null);
            if(!rs.next())
            {
                s = connection.createStatement();
                statements.add(s);
                //We create a table
                s.execute("create table " + notesTableName + "(" +
                        "primaryKey int GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                        "taskId varchar(40)," +
                        "noteBody varchar(500)," +
                        "author varchar(40)," +
                        "createdDate varchar(40)," +
                        "PRIMARY KEY(primaryKey)" +
                        ")");
                connection.commit();
                System.out.println("NOTES table doesnt exist - Created...");
            }
            else
            {
                System.out.println("NOTES table already exists");
                connection.commit();
            }

            rs = dbm.getTables(null, null, usersTableName, null);
            if(!rs.next())
            {
                s = connection.createStatement();
                statements.add(s);
                //We create a table
                s.execute("create table " + usersTableName + "(" +
                        "primaryKey int GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                        "firstName varchar(40)," +
                        "lastName varchar(40)," +
                        "username varchar(40)," +
                        "emailAddress varchar(100)," +
                        "PRIMARY KEY(primaryKey)" +
                        ")");
                connection.commit();
                System.out.println("USERS table doesnt exist - Created...");
            }
            else
            {
                System.out.println("USERS table already exists");
                connection.commit();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void closeDB() throws SQLException {
        connection.close();
    }

    /**
     * Reports a data verification failure to System.err with the given message.
     *
     * @param message A message describing what failed.
     */
    private void reportFailure(String message) {
        System.err.println("\nData verification failed:");
        System.err.println('\t' + message);
    }

    /**
     * Prints details of an SQLException chain to <code>System.err</code>.
     * Details included are SQL State, Error code, Exception message.
     *
     * @param e the SQLException from which to print details.
     */
    public static void printSQLException(SQLException e) {
        // Unwraps the entire exception chain to unveil the real cause of the
        // Exception.
        while (e != null) {
            System.err.println("\n----- SQLException -----");
            System.err.println("  SQL State:  " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("  Message:    " + e.getMessage());
            // for stack traces, refer to derby.log or uncomment this:
            //e.printStackTrace(System.err);
            e = e.getNextException();
        }
    }

    public static String insertTask(Task task) throws SQLException, DerbySQLIntegrityConstraintViolationException
    {
        //TASK FIELDS
        /**
         1-  String internalReference = "none";
         2-  String customerReference = "none";
         3-  String messageId;
         4-  Date receivedDate = null;
         5-  Date respondedDate = null;
         6-  String customer;
         7-  double revenue;
         8-  String state;
         9-  String preparedBy;
         10- int priority = 0; //0 = BAU, 1 = HIGH, 2 = IMMEDIATE
         **/

        String key = null;
        //DB connection object
        connection = DriverManager.getConnection(protocol + dbName + ";create=false");
        //List of Statements, PreparedStatements
        ArrayList<Statement> statements = new ArrayList<Statement>();
        PreparedStatement psInsert;
        //Collect statements to close later
        Statement s;
        s = connection.createStatement();
        statements.add(s);
        psInsert = connection.prepareStatement("insert into " + tasksTableName + " (internalReference, customerReference, messageId, receivedDate, respondedDate, customer, revenue, state, preparedBy, priority, orderNum) values (?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
        statements.add(psInsert);
        psInsert.setString(1, task.getInternalReference());
        psInsert.setString(2, task.getCustomerReference());
        psInsert.setString(3, task.getMessageId());
        psInsert.setString(4, task.getReceivedDate());
        psInsert.setString(5, task.getRespondedDate());
        psInsert.setString(6, task.getCustomer());
        psInsert.setDouble(7, task.getRevenue());
        psInsert.setString(8, task.getState());
        psInsert.setString(9, task.getPreparedBy());
        psInsert.setInt(10, task.getPriority());
        psInsert.setInt(11, task.getOrder());

        //EXECUTE STATEMENT
        psInsert.executeUpdate();
        connection.commit();
        //System.out.println("Inserted task: " + task.getInternalReference());
        //GET ATUO GENERATED PRIMARY KEY

        try (ResultSet keys = psInsert.getGeneratedKeys()) {
            if(keys.next())
            {
                key = "" + keys.getLong(1);
            }
        }

        //CLOSE ALL STATEMENTS
        int i = 0;
        while (!statements.isEmpty()) {
            // PreparedStatement extend Statement
            Statement st = (Statement) statements.remove(i);
            try {
                if (st != null) {
                    st.close();
                    st = null;
                }
            } catch (SQLException sqle) {
                printSQLException(sqle);
            }
        }

        //CLOSE CONNECTION
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
        return key;
    }

    public static Boolean insertUser(User user) throws SQLException, DerbySQLIntegrityConstraintViolationException
    {
        //TASK FIELDS
        /**
         "firstName varchar(40)," +
         "lastName varchar(40)," +
         "username varchar(40)," +
         "emailAddress varchar(100)," +
         "PRIMARY KEY(primaryKey)" +
         **/

        Boolean result = false;
        //DB connection object
        connection = DriverManager.getConnection(protocol + dbName + ";create=false");
        //List of Statements, PreparedStatements
        ArrayList<Statement> statements = new ArrayList<Statement>();
        PreparedStatement psInsert;
        //Collect statements to close later
        Statement s;
        s = connection.createStatement();
        statements.add(s);
        psInsert = connection.prepareStatement("insert into " + usersTableName + " (firstName, lastName, username, emailAddress) values (?,?,?,?)");
        statements.add(psInsert);
        psInsert.setString(1, user.getFirstName());
        psInsert.setString(2, user.getLastName());
        psInsert.setString(3, user.getUsername());
        psInsert.setString(4, user.getEmailAddress());

        //EXECUTE STATEMENT
        psInsert.executeUpdate();
        connection.commit();
        result = true;


        //CLOSE ALL STATEMENTS
        int i = 0;
        while (!statements.isEmpty()) {
            // PreparedStatement extend Statement
            Statement st = (Statement) statements.remove(i);
            try {
                if (st != null) {
                    st.close();
                    st = null;
                }
            } catch (SQLException sqle) {
                printSQLException(sqle);
            }
        }

        //CLOSE CONNECTION
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
        return result;
    }

    public static List<Task> getAllTasks() throws SQLException {
        ArrayList tasks = new ArrayList<Task>();
        ResultSet results = null;
        ResultSet rs = null;
        connection = DriverManager.getConnection(protocol + dbName + ";create=false");
        connection.setAutoCommit(false);
        Statement s = connection.createStatement();
        Statement st = connection.createStatement();

        //EXECUTE STATEMENT
        results = s.executeQuery("SELECT * FROM " + tasksTableName);
        //COLLECT RESULTS
        if (!results.next()) {
            System.out.println("database empty");
        } else {
            do {
                Task task = new Task();
                task.setPrimaryKey(results.getString("primaryKey"));
                task.setInternalReference(results.getString("internalReference"));
                task.setCustomerReference(results.getString("customerReference"));
                task.setMessageId(results.getString("messageId"));
                task.setReceivedDate(results.getString("receivedDate"));
                task.setRespondedDate(results.getString("respondedDate"));
                task.setCustomer(results.getString("customer"));
                task.setRevenue(results.getDouble("revenue"));
                task.setState(results.getString("state"));
                task.setPreparedBy(results.getString("preparedBy"));
                task.setPriority(results.getInt("priority"));
                task.setOrder(results.getInt("orderNum"));

                rs = st.executeQuery("SELECT * FROM " + notesTableName + " WHERE taskId='" + task.getPrimaryKey() + "'");
                if(rs.next())
                {
                    do
                    {
                        TaskNote note = new TaskNote();
                        note.setPrimaryKey(rs.getString("primaryKey"));
                        note.setTaskId(rs.getString("taskId"));
                        note.setAuthor(rs.getString("author"));
                        note.setCreatedDate(rs.getString("createdDate"));
                        note.setNoteBody(rs.getString("noteBody"));
                        task.addTaskNote(note);
                    }
                    while(rs.next());
                }

                tasks.add(task);
                System.out.println("DBHelper - retrieved task by " + task.getPreparedBy());
            }
            while (results.next());
        }
        //CLOSE STATEMENTS
        s.close();
        st.close();
        results.close();
        //rs.close();
        //CLOSE CONNECTION
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
        return tasks;
    }

    public static List<User> getAllUsers() throws SQLException {
        ArrayList users = new ArrayList<User>();
        ResultSet results = null;
        ResultSet rs = null;
        connection = DriverManager.getConnection(protocol + dbName + ";create=false");
        connection.setAutoCommit(false);
        Statement s = connection.createStatement();
        Statement st = connection.createStatement();

        //EXECUTE STATEMENT
        results = s.executeQuery("SELECT * FROM " + usersTableName);
        //COLLECT RESULTS
        if (!results.next()) {
            System.out.println("database empty");
        } else {
            do {
                User user = new User();
                user.setFirstName(results.getString("firstName"));
                user.setLastName(results.getString("lastName"));
                user.setUsername(results.getString("username"));
                user.setEmailAddress(results.getString("emailAddress"));
                users.add(user);
                System.out.println("DBHelper - retrieved user " + user.getUsername());
            }
            while (results.next());
        }
        //CLOSE STATEMENTS
        s.close();
        st.close();
        results.close();
        //rs.close();
        //CLOSE CONNECTION
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
        return users;
    }

    public static Task getTask(String taskId) throws SQLException {
        Task task = null;
        ResultSet results = null;
        connection = DriverManager.getConnection(protocol + dbName + ";create=false");
        Statement s = connection.createStatement();
        PreparedStatement psSelect = connection.prepareStatement("SELECT * FROM" + tasksTableName + " WHERE internalReference=" + taskId);
        //EXECUTE STATEMENT
        results = psSelect.executeQuery("SELECT * FROM" + tasksTableName + " WHERE internalReference=" + taskId);
        if (!results.next()) {
            System.out.println("task not in DB");
        } else {
            task = new Task();
            task.setPrimaryKey(results.getString("primaryKey"));
            task.setInternalReference(results.getString("internalReference"));
            task.setCustomerReference(results.getString("customerReference"));
            task.setMessageId(results.getString("messageId"));
            task.setReceivedDate(results.getString("receivedDate"));
            task.setRespondedDate(results.getString("respondedDate"));
            task.setCustomer(results.getString("customer"));
            task.setRevenue(results.getDouble("revenue"));
            task.setState(results.getString("state"));
            task.setPreparedBy(results.getString("preparedBy"));
            task.setPriority(results.getInt("priority"));
            task.setOrder(results.getInt("orderNum"));

        }
        //CLOSE STATEMENTS
        s.close();
        results.close();
        //CLOSE CONNECTION
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
        return task;
    }

    public static void deleteTask(String primaryKey) throws SQLException {
        connection = DriverManager.getConnection(protocol + dbName + ";create=false");
        //EXECUTE STATEMENT
        PreparedStatement psSelect = connection.prepareStatement("DELETE FROM " + tasksTableName + " WHERE primaryKey=" + primaryKey);
        psSelect.executeUpdate();
        //CLOSE STATEMENTS
        psSelect.close();
        //CLOSE CONNECTION
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    public static void deleteUser(String username) throws SQLException {
        connection = DriverManager.getConnection(protocol + dbName + ";create=false");
        //EXECUTE STATEMENT
        PreparedStatement psSelect = connection.prepareStatement("DELETE FROM " + usersTableName + " WHERE username= '" + username + "'");
        psSelect.executeUpdate();
        //CLOSE STATEMENTS
        psSelect.close();
        //CLOSE CONNECTION
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    public static void overwriteTask(String oldTaskRef, Task task) throws SQLException {
        connection = DriverManager.getConnection(protocol + dbName + ";create=false");

        PreparedStatement psReplace = connection.prepareStatement("UPDATE " + tasksTableName + " SET internalReference = ?, customerReference = ?, messageId = ?, receivedDate = ?, respondedDate = ?, customer = ?, revenue = ?, state = ?, preparedBy = ?, priority = ?, orderNum = ? WHERE internalReference = ?");
        //PreparedStatement psReplace = connection.prepareStatement("UPDATE " + tableName + " SET internalReference = ? WHERE internalReference = ?");
        psReplace.setString(1, task.getInternalReference());
        psReplace.setString(2, task.getCustomerReference());
        psReplace.setString(3, task.getMessageId());
        psReplace.setString(4, task.getReceivedDate());
        psReplace.setString(5, task.getRespondedDate());
        psReplace.setString(6, task.getCustomer());
        psReplace.setDouble(7, task.getRevenue());
        psReplace.setString(8, task.getState());
        psReplace.setString(9, task.getPreparedBy());
        psReplace.setInt(10, task.getPriority());
        psReplace.setString(11, oldTaskRef);
        psReplace.setInt(12, task.getOrder());
        //EXECUTE
        psReplace.executeUpdate();
        //CLOSE
        psReplace.close();
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    public static void persistTaskArray(List<Task> tasksArray) throws SQLException {
        connection = DriverManager.getConnection(protocol + dbName + ";create=false");
        PreparedStatement ps = null;

        for(Task task:tasksArray)
        {
            ps = connection.prepareStatement("UPDATE " + tasksTableName + " SET internalReference = ?, customerReference = ?, messageId = ?, receivedDate = ?, respondedDate = ?, customer = ?, revenue = ?, state = ?, preparedBy = ?, priority = ?, orderNum= ? WHERE primaryKey = ?");
            ps.setString(1, task.getInternalReference());
            ps.setString(2, task.getCustomerReference());
            ps.setString(3, task.getMessageId());
            ps.setString(4, task.getReceivedDate());
            ps.setString(5, task.getRespondedDate());
            ps.setString(6, task.getCustomer());
            ps.setDouble(7, task.getRevenue());
            ps.setString(8, task.getState());
            ps.setString(9, task.getPreparedBy());
            ps.setInt(10, task.getPriority());
            ps.setInt(11, task.getOrder());
            ps.setString(12, task.getPrimaryKey());
            //EXECUTE
            ps.executeUpdate();
            System.out.println("DBHelper - persisted task owned by " + task.getPreparedBy() + " order: " + task.getOrder());

            if(!task.getAllTaskNotes().isEmpty())
            {
                for(Object o:task.getAllTaskNotes())
                {
                    TaskNote note = (TaskNote) o;
                    if(note.getPersist())
                    {
                        //persist to DB
                        ps = connection.prepareStatement("INSERT INTO " + notesTableName + " (taskId, noteBody, author, createdDate) VALUES (?,?,?,?)");
                        ps.setString(1, note.getTaskId());
                        ps.setString(2, note.getNoteBody());
                        ps.setString(3, note. getAuthor().toString());
                        ps.setString(4, note.getCreatedDate().toString());
                        //EXECUTE
                        try{
                            ps.executeUpdate();
                            connection.commit();
                            note.setPersist(false);
                            System.out.println("DBHelper - persisted note for task");
                        }
                        catch(DerbySQLIntegrityConstraintViolationException e)
                        {
                            System.out.println("Error inserting note - duplicate primary key found");
                        }
                    }
                }
            }
        }
        //CLOSE
        ps.close();
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    public static void dropTables() throws SQLException {
        //DB connection object
        connection = DriverManager.getConnection(protocol + dbName + ";create=false");
        //Collect statements to close later
        Statement s;
        s = connection.createStatement();

        //EXECUTE STATEMENT
        s.execute("drop table tasks");
        connection.commit();
        System.out.println("Task table dropped");

        s.execute("drop table notes");
        connection.commit();
        System.out.println("Notes table dropped");

        //CLOSE ALL STATEMENTS
        s.close();

        //CLOSE CONNECTION
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }


}
