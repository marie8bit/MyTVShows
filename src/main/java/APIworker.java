import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.JSONObject;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Created by Marie on 12/8/2016.
 */
public abstract class APIworker extends SwingWorker<Boolean, Void> {
    protected static FormMyTVShows resultListener;
    protected String path;
    protected int sheetNum;
    protected int col;
    protected String table;
    protected String search;

    public static void getSpreadsheet(FormMyTVShows resultListener, String path, int sheetNum, int col) {

        //for(Row row:wb)
        try {
            //path.replace("\\", "\\" + "\\");
            InputStream inp = new FileInputStream(path);
            //InputStream inp = new FileInputStream("workbook.xlsx");

            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(sheetNum);
            for (Row row : sheet) {
                String rawTitle = row.getCell(col).getStringCellValue();
                // build a URL
                String rawTitle1 = rawTitle.trim();
                String title = rawTitle1.replace(" ", "%20");
                String str = getOMDBentry(title, "title");

                // build a JSON object
                JSONObject obj = new JSONObject(str);
                MTVSdb tvdb = new MTVSdb();
                if (!obj.getString("Response").equals("True")) {

                    tvdb.addRow("PK0", rawTitle);

                } else {
                    getAPISearch(obj);
                }

            }

        }
        catch(NullPointerException npe){

        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static String getOMDBentry(String identifier, String searchType) throws Exception{
        String s ="";
        if ("title".equals(searchType)){
            identifier.trim();
            identifier = identifier.replace(" ", "%20");
            s = "http://www.omdbapi.com/?s=" + identifier + "&type=series";}
        else {s = "http://www.omdbapi.com/?i=" + identifier;}
        //s += URLEncoder.encode(addr, "UTF-8");
        URL url = new URL(s);
        // read from the URL, search omdb
        Scanner scan = new Scanner(url.openStream());
        String str = new String();
        while (scan.hasNext())
            str += scan.nextLine();
        scan.close();
        return str;
    }
    public static void getAPISearch(JSONObject obj)throws Exception{
        // get the first result
        MTVSdb tvdb = new MTVSdb();

//            if (obj.getJSONArray("Search").length() > 0) {
        try {
            for (int x = 0; x < obj.getJSONArray("Search").length(); x++) {
                JSONObject res = obj.getJSONArray("Search").getJSONObject(x);
                //System.out.println(res.getString("Title") + res.getString("Year"));
                if (res.getString("Year").length() < 6) {
                    String PKID = res.getString("imdbID");
                    //list.add or new object or SQL insert

                    String str = getOMDBentry(PKID, "ID");
                    JSONObject dbo = new JSONObject(str);
                    tvdb.addRowbyIDActive(PKID, dbo.getString("Title"),
                            dbo.getString("Year"), dbo.getString("Plot"));
                } else {
                    getIDAPISearch(res);
                }
            }
        }
        catch (ExecutionException ee){
            resultListener.fail();
        }
    }
    public static void getIDAPISearch(JSONObject res)throws Exception{
        String PKID = res.getString("imdbID");
        //list.add or new object or SQL insert
        String str = getOMDBentry(PKID, "ID");
        JSONObject dbo = new JSONObject(str);
        MTVSdb.addRowbyIDArchive(PKID, dbo.getString("Title"),
                dbo.getString("Year"), dbo.getString("Plot"));
    }
}
    class XAPIworker  extends APIworker {

        public XAPIworker(FormMyTVShows resultListener, String path, int sheetNum, int col) {

            this.resultListener = resultListener;
            this.path = path;
            this.sheetNum = sheetNum;
            this.col = col;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            getSpreadsheet(resultListener, path, sheetNum, col);
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
                ee.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
class AddAPIworker  extends APIworker {
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