/**
 * Created by Marie on 11/29/2016.
 */
import java.sql.*;
import java.util.InputMismatchException;

public class MTVSdb {
    //identify driver
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    //set up connection url
    static final String DB_CONNECTION_URL = "jdbc:mysql://localhost:3306/MyTVShowsDB";
    //identify user information for DB connection
    static final String USER = "Marie";
    static final String PASSWORD = "tryapassphrase";
    //generate connection to DB
    Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
    Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    MTVSdb()throws Exception {
        //constructor for database adds data if none is present
        Class.forName(JDBC_DRIVER);
        //create table if it doesn't already exist
        statement.execute("Create table if not EXISTS Active (ID varchar(10), ShowName VARCHAR (150))");
        statement.execute("Create table if not EXISTS Archive (ID varchar(10), ShowName VARCHAR (150))");


        connection.close();
    }
    public static ResultSet getActive()throws Exception{
        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
        //generate prepared statement for filling in DB
        String getAcResultsQuery = "Select * from active";
        PreparedStatement getAcResultsStatement = connection.prepareStatement(getAcResultsQuery);
        //get result set from table
        ResultSet acrs = getAcResultsStatement.executeQuery();
        return acrs;
    }
    public ResultSet getArchive()throws Exception{
        String getArResultsQuery = "Select * from archive";
        PreparedStatement getArResultsStatement = connection.prepareStatement(getArResultsQuery);
        //get result set from table
        ResultSet arrs = getArResultsStatement.executeQuery();
        return arrs;
    }
    //update method for database object
    public static ResultSet updateResultSet( int column, String newNalue, String oldValue)throws Exception{
        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);

        //Connection connection = Controller.connection;
        if (column==0){
            //isCellEditable=false;
        }
        if (column==1){
            //different sql updates depending on which column is edited using prepared statements
            String prepStatUpdate = "update records set holder = ? where holder = ?";
            PreparedStatement psUpdate = connection.prepareStatement(prepStatUpdate);
            psUpdate.setString(1,newNalue);
            psUpdate.setString(2,oldValue);
            try {
                psUpdate.executeUpdate();
            }
            catch (SQLException sq){
                System.out.println("here2");
            }
            //return new result set to gui form
            ResultSet rs = getActive();
            return rs;
        }
        else{
            String prepStatUpdate = "update records set record = ?  where holder = ?";
            PreparedStatement psUpdate = connection.prepareStatement(prepStatUpdate);
            System.out.println(newNalue);
            Double dNew = Double.parseDouble(newNalue);


            psUpdate.setDouble(1,dNew);
            psUpdate.setString(2,oldValue);
            try {
                psUpdate.executeUpdate();
            }
            catch (SQLException sq){
                System.out.println("here3");
                sq.printStackTrace();
            }
            ResultSet rs = getActive();
            return rs;
        }
    }

    //delete method using prepared statements returns the new resultSet
    public static ResultSet deleteRow(String primary) throws Exception{
        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
        String prepStatUpdate = "delete from records where holder = ?";
        PreparedStatement psUpdate = connection.prepareStatement(prepStatUpdate);
        psUpdate.setString(1,primary);
        try {
            psUpdate.executeUpdate();
        }
        catch (SQLException sq){
            System.out.println("here4");
        }
        ResultSet rs = getActive();
        return rs;


    }
    //add new record using prepared statements returns the new resultSet
    public static ResultSet addRow(String primary, String amount) throws Exception{
        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
        String prepStatUpdate = "insert into records values (?,?)";
        PreparedStatement psUpdate = connection.prepareStatement(prepStatUpdate);
        psUpdate.setString(1,primary);
        psUpdate.setDouble(2,Double.parseDouble(amount));
        try {
            psUpdate.executeUpdate();
        }
        catch (SQLException sq){
            System.out.println("here4");
        }
        ResultSet rs = getActive();
        return rs;

    }
}
