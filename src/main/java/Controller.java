import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Scanner;

import java.net.URLEncoder;
import java.util.StringJoiner;
//import junit.framework.Assert;

import org.apache.poi.ss.usermodel.*;

/**
 * Created by Marie on 11/29/2016.
 * program to read ecxel file and get JSON objects from OMDB API
 */
public class Controller {
public static int gen = 0;
public static String PrimaryKeyGen = "PK"+gen;
    public static void main(String[] agrs)throws Exception{
        MTVSdb tvdb = new MTVSdb();
        TableModel tm = new TableModel(tvdb.getActive());
        FormMyTVShows frm = new FormMyTVShows(tm);
    }

    public static void getSpreadsheet(String path, String name, String ext) {

        //for(Row row:wb)
        try {
            path.replace("\\", "\\" + "\\");
            InputStream inp = new FileInputStream(path + name + ext);
            //InputStream inp = new FileInputStream("workbook.xlsx");

            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(2);
            for (Row row : sheet) {
                String rawTitle = row.getCell(1).getStringCellValue();
                // build a URL
                String title = rawTitle.replace(" ", "%20");
                String str = getOMDBentry(title, "title");

                // build a JSON object
                JSONObject obj = new JSONObject(str);
                MTVSdb tvdb = new MTVSdb();
                if (!obj.getString("Response").equals("True")) {
                    //
                    tvdb.addRow(Controller.PrimaryKeyGen, rawTitle);
                    Controller.gen++;
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
        if ("title".equals(searchType)){s = "http://www.omdbapi.com/?s=" + identifier + "&type=series";}
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
            for (int x = 0; x < obj.getJSONArray("Search").length(); x++) {
                JSONObject res = obj.getJSONArray("Search").getJSONObject(x);
                //System.out.println(res.getString("Title") + res.getString("Year"));
                if (res.getString("Year").length()<6) {
                    String PKID = res.getString("imdbID");
                    //list.add or new object or SQL insert

                    String str = getOMDBentry(PKID, "ID");
                    JSONObject dbo = new JSONObject(str);
                    tvdb.addRowbyIDActive(PKID, dbo.getString("Title"),
                            dbo.getString("Year"), dbo.getString("Plot"));
                }
                else {
                    String PKID = res.getString("imdbID");
                    //list.add or new object or SQL insert
                    String str = getOMDBentry(PKID, "ID");
                    JSONObject dbo = new JSONObject(str);
                    tvdb.addRowbyIDArchive(PKID, dbo.getString("Title"),
                            dbo.getString("Year"), dbo.getString("Plot"));
                }
            }
        }
    public static void shutdown(){

        //Close resources - ResultSet, statement, connection - and tidy up whether this code worked or not.

        //Close ResultSet...
//        try {
//            if (rs != null) {
//                rs.close();
//                System.out.println("Result set closed");
//            }
//        } catch (SQLException se) {
//            se.printStackTrace();
//        }
//
//        //And then the statement....
//        try {
//            if (statement != null) {
//                statement.close();
//                System.out.println("Statement closed");
//            }
//        } catch (SQLException se) {
//            se.printStackTrace();
//
//        }
//
//        //And then the connection
//        try {
//            if (connection != null) {
//                connection.close();
//                System.out.println("Database connection closed");
//            }
//
//        } catch (SQLException se) {
//            se.printStackTrace();
//        }

        //And quit the program
        System.exit(0);
    }

}