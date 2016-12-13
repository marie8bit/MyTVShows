import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.JSONObject;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Created by Marie on 12/8/2016.
 */
//class that provides information to the user about what the program is doing and alerts
    //them when they the program has finished it's action
    //it is an abstract class to initialize common properties and methods for subclasses
public abstract class APIworker extends SwingWorker<Boolean, Void> {
    protected static FormMyTVShows resultListener;
    protected String path;
    protected int sheetNum;
    protected int col;
    protected String table;
    protected String search;
    //method to read spreadsheet data
    //FormMyTVShows resultListener,
    public static void getSpreadsheet( String path, int sheetNum, int col) {
        try {
            //source reference http://poi.apache.org/spreadsheet/index.html
            InputStream inp = new FileInputStream(path);
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(sheetNum);
            //itterate throught the rows of the data in the spreadsheet
            for (Row row : sheet) {
                String rawTitle = row.getCell(col).getStringCellValue();
                //call method to get JSON object from OMDB API
                String str = getOMDBentry(rawTitle, "title");
                // process the returned JSON object
                JSONObject obj = new JSONObject(str);
                if (!obj.getString("Response").equals("True")) {
                    //method to add a row of data to the database when the search doesn't return any results
                    MTVSdb.addRow("PK0", rawTitle);
                } else {
                    //calls method to handle search results
                    getAPISearch(obj);
                }
            }
        }
        catch(NullPointerException npe){
            npe.printStackTrace();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
    //method to generate URL depending on the search type
     //source reference https://www.omdbapi.com/
    public static String getOMDBentry(String identifier, String searchType) throws Exception{
        String s ="";
        if ("title".equals(searchType)){
            // format string to be used in an API search by removing and trailing spaces and encoding search string
            identifier.trim();
            identifier = identifier.replace(" ", "%20");
            s = "http://www.omdbapi.com/?s=" + identifier + "&type=series";}
        else {s = "http://www.omdbapi.com/?i=" + identifier;}
        URL url = new URL(s);
        // read from the URL, search omdb
        Scanner scan = new Scanner(url.openStream());
        String str = new String();
        //makes sure all data is added to the string
        while (scan.hasNext())
            str += scan.nextLine();
        scan.close();
        return str;
    }
    //process JSON object to use search results to generate more complete data
    //source reference http://theoryapp.com/parse-json-in-java/
    public static void getAPISearch(JSONObject obj)throws Exception{
        try {
            for (int x = 0; x < obj.getJSONArray("Search").length(); x++) {
                JSONObject res = obj.getJSONArray("Search").getJSONObject(x);
                //decide if data should be added to active table or archive table
                //a show is considered active if it doesn't have an end date
                String PKID = res.getString("imdbID");
                //search using ID gets more specific/additional results
                String str = getOMDBentry(PKID, "ID");
                JSONObject dbo = new JSONObject(str);
                //add row of data from JSON object using key,value pairs
                if (res.getString("Year").length() < 6) {
                    MTVSdb.addRowFull("active",PKID, dbo.getString("Title"),
                            dbo.getString("Year"), dbo.getString("Plot"));
                } else {
                    //process archive data
                    MTVSdb.addRowFull("archive", PKID, dbo.getString("Title"),
                            dbo.getString("Year"), dbo.getString("Plot"));
                }
            }
        }
        //calls method to provide information to user about failure to retrieve results
        catch (ExecutionException ee){
            resultListener.fail();
        }
    }
}
//subclass for working with excel spreadsheet data fetch
    class XAPIworker  extends APIworker {

        public XAPIworker(FormMyTVShows resultListener, String path, int sheetNum, int col) {
            //initializes SwingWorker, passes arguments as parameters
            this.resultListener = resultListener;
            this.path = path;
            this.sheetNum = sheetNum;
            this.col = col;
        }
        //result listener listens for the signal to execute SwingWorker behavior
        @Override
        protected Boolean doInBackground() throws Exception {
            //resultListener,
            getSpreadsheet(path, sheetNum, col);
            return true;
        }
        //result listener receive the signal that its process is complete
        @Override
        protected void done() {
            try {
                boolean finished = get();
                if (finished == true) {
                    resultListener.finish();
                }
            } catch (ExecutionException ee) {
                ee.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
//APIWorker subclass for working with user data input to fetch data
class AddAPIworker  extends APIworker {
    //
        public AddAPIworker(FormMyTVShows resultListener, String search,String table) {
            this.resultListener = resultListener;
            this.search= search;
            this.table = table;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            String str = getOMDBentry(search, table);
            JSONObject obj = new JSONObject(str);
            getAPISearch(obj);
            return true;
        }

        @Override
        protected void done() {
            try {
                boolean finished = get();
                if (finished == true) {
                    resultListener.finish();
                }


            } catch (ExecutionException ee) {
                resultListener.fail();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }