/**
 * Created by Marie on 11/29/2016.
 */
import java.sql.*;

public class MTVSdb {
    //identify driver
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    //set up connection url
    static final String DB_CONNECTION_URL = "jdbc:mysql://localhost:3306/mytvshowsdb";
    //identify user information for DB connection
    static final String USER = "Marie";
    static final String PASSWORD = "tryapassphrase";
    MTVSdb() throws Exception {
        //constructor for database adds data if none is present
        Class.forName(JDBC_DRIVER);
        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
//        create table if it doesn't already exist
        statement.execute("Create table if not EXISTS Active (ID varchar(10), ShowName VARCHAR (150), Year varchar(9), Plot varchar(250))");
        statement.execute("Create table if not EXISTS Archive (ID varchar(10), ShowName VARCHAR (150), Year VARCHAR (9), Plot varchar(250))");
        connection.close();
        statement.close();
    }
    //method for getting table data from database manager
    public static ResultSet getResultSet(String table) throws Exception {
        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
        //generate prepared statement for filling in DB
        String getResultsQuery = "Select * from "+table;
        PreparedStatement getAcResultsStatement = connection.prepareStatement(getResultsQuery);
        //get result set from table
        ResultSet acrs = getAcResultsStatement.executeQuery();
        //connection.close();
        //getAcResultsStatement.close();
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
        //call method to update a database object
        updateResultSet(prepStatUpdate, newItem, primary);
    }
    //method to update existing data
    public static void updateResultSet(String prepStatUpdate, String newItem, String primary)  {
        try {
            Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
            PreparedStatement psUpdate = connection.prepareStatement(prepStatUpdate);
            psUpdate.setString(1, newItem);
            psUpdate.setString(2, primary);
            psUpdate.executeUpdate();
            connection.close();
            psUpdate.close();
            } catch (SQLException sq) {
                sq.printStackTrace();
            }
            catch (Exception e){
                e.printStackTrace();
            }
    }
    //method to delete data from a database using prepared statements
    public static void deleteRow(String primary, String table){
        try {
            Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
            String prepStatUpdate = "active".equals(table)?"delete from active where ID = ?":"delete from archive where ID = ?";
            PreparedStatement psUpdate = connection.prepareStatement(prepStatUpdate);
            psUpdate.setString(1, primary);
            psUpdate.executeUpdate();
            connection.close();
            psUpdate.close();
        } catch (SQLException sq) {
            sq.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
    //generate user information for API searches that return null results
    public static void addRow(String primary, String title) {
        try {
            String year=null;
            String info = "Title not found, research the IMDB ID from the internet and insert it in the firt column";
            addRowFull("active",primary, title, year, info);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
   //add new record using prepared statements
    public static void addRowFull(String table, String primary, String title, String year, String plot) throws Exception {
        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
        String prepStatUpdate = "insert into "+table+" values (?,?,?,?)";
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
        connection.close();
        psUpdate.close();
    }
}
