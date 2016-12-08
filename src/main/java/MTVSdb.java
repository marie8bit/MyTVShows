/**
 * Created by Marie on 11/29/2016.
 */
import java.sql.*;
import java.util.InputMismatchException;

public class MTVSdb {
    //identify driver
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    //set up connection url
    static final String DB_CONNECTION_URL = "jdbc:mysql://localhost:3306/mytvshowsdb";
    //identify user information for DB connection
    static final String USER = "Marie";
    static final String PASSWORD = "tryapassphrase";
    //generate connection to DB

    MTVSdb() throws Exception {
        //constructor for database adds data if none is present
        Class.forName(JDBC_DRIVER);
        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

//        create table if it doesn't already exist
        statement.execute("Create table if not EXISTS Active (ID varchar(10), ShowName VARCHAR (150), Year varchar(9), Plot varchar(250))");
        statement.execute("Create table if not EXISTS Archive (ID varchar(10), ShowName VARCHAR (150), Year VARCHAR (9), Plot varchar(250))");


        connection.close();
    }

    public static ResultSet getResultSet(String table) throws Exception {
        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
        //generate prepared statement for filling in DB
        String getResultsQuery = "Select * from "+table;
        PreparedStatement getAcResultsStatement = connection.prepareStatement(getResultsQuery);
        //get result set from table
        ResultSet acrs = getAcResultsStatement.executeQuery();
        //connection.close();
        return acrs;
    }

    //update method for database object
    public static void getPrepStatement( int column,String newItem, String primary, String table )throws Exception{
        String prepStatUpdate;
        if (column==2) {
            //different sql updates depending on which column is edited using prepared statements
            prepStatUpdate = "update "+table+" set year = ? where ID = ?";
        }
        else if (column == 3){
            prepStatUpdate = "update "+table+" set plot = ? where ID = ?";
        }
        else{return;}
        updateResultSet(prepStatUpdate, newItem, primary);
    }
    public static void updateResultSet(String prepStatUpdate, String newItem, String primary) throws Exception {
        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);

        //Connection connection = Controller.connection;


        PreparedStatement psUpdate = connection.prepareStatement(prepStatUpdate);
        psUpdate.setString(1, newItem);
        psUpdate.setString(2, primary);
        try {
            psUpdate.executeUpdate();
        } catch (SQLException sq) {
            System.out.println("here2");
        }
        //return new result set to gui form
        //ResultSet rs = getActive();
        //connection.close();
        //return rs;
//        }
//        else{
//            String prepStatUpdate = "update active set ID = ?  where ID = ?";
//            PreparedStatement psUpdate = connection.prepareStatement(prepStatUpdate);
//            System.out.println(newNalue);
//            Double dNew = Double.parseDouble(newNalue);
//
//
//            psUpdate.setDouble(1,dNew);
//            psUpdate.setString(2,oldValue);
//            try {
//                psUpdate.executeUpdate();
//            }
//            catch (SQLException sq){
//                System.out.println("here3");
//                sq.printStackTrace();
//            }
//            ResultSet rs = getActive();
//            return rs;
//        }
    }

    //delete method using prepared statements returns the new resultSet
    public static void deleteRow(String primary, String table) throws Exception {
        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
        String prepStatUpdate = "active".equals(table)?"delete from active where ID = ?":"delete from archive where ID = ?";
        PreparedStatement psUpdate = connection.prepareStatement(prepStatUpdate);
        psUpdate.setString(1, primary);
        try {
            psUpdate.executeUpdate();
        } catch (SQLException sq) {
            System.out.println("here4");
        }

        connection.close();



    }

    //add new record using prepared statements returns the new resultSet
    public static void addRow(String primary, String title) throws Exception {
        String year=null;
        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
        String info = "Title not found, research the IMDB ID from the internet and insert it in the firt column";
        String prepStatUpdate = "insert into active values (?,?, ?, ?)";
        PreparedStatement psUpdate = connection.prepareStatement(prepStatUpdate);
        psUpdate.setString(1, primary);
        psUpdate.setString(2, title);
        psUpdate.setString(3, year);
        psUpdate.setString(4, info);
        try {
            psUpdate.executeUpdate();
        } catch (SQLException sq) {
            sq.printStackTrace();
            System.out.println("here5");
        }
        //ResultSet rs = getActive();
        connection.close();
        //return rs;

    }

    //add new record using prepared statements returns the new resultSet
    public static void addRowbyIDActive(String primary, String title, String year, String plot) throws Exception {
        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
        String prepStatUpdate = "insert into active values (?,?,?,?)";
        PreparedStatement psUpdate = connection.prepareStatement(prepStatUpdate);
        psUpdate.setString(1, primary);
        psUpdate.setString(2, title);
        psUpdate.setString(3, year);
        psUpdate.setString(4, plot);
        try {
            psUpdate.executeUpdate();
        } catch (SQLException sq) {

            System.out.println("here6");
            sq.printStackTrace();
        }
        //ResultSet rs = getActive();
        connection.close();
        //return rs;

    }

    public static void addRowbyIDArchive(String primary, String title, String year, String plot) throws Exception {
        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
        String prepStatUpdate = "insert into archive values (?,?,?,?)";
        PreparedStatement psUpdate = connection.prepareStatement(prepStatUpdate);
        psUpdate.setString(1, primary);
        psUpdate.setString(2, title);
        psUpdate.setString(3, year);
        psUpdate.setString(4, plot);
        try {
            psUpdate.executeUpdate();
        } catch (SQLException sq) {
            System.out.println("here7");
            sq.printStackTrace();
        }
        //ResultSet rs = getArchive();
        connection.close();
        //return rs;

    }
}
