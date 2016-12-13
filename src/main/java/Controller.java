//import junit.framework.Assert;

/**
 * Created by Marie on 11/29/2016.
 * This program can an read excel file to gather search items with which to fetch data from OMDB API.
 * The program will then store this data in a database structure containing two tables to
 * differentiate between active tv shows and shows that are no longer on the air.
 * This program can add items to the data base by obtaining tv show names from an
 * excel spreadsheet or searching by name or IMDB ID, it can delete multiple
 * rows of data at once and move items from the active table to the archive table.
 */
public class Controller {
    //main method to open GUI Form and fetch database data
    public static void main(String[] agrs)throws Exception{
        //initialize database connection and retrieves stored dataset if any
        MTVSdb tvdb = new MTVSdb();
        //
        TableModel tm = new TableModel(tvdb.getResultSet("active"));
        FormMyTVShows frm = new FormMyTVShows(tm);
    }

    public static void shutdown(){

        System.exit(0);
    }

}