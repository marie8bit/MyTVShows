import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import java.net.URLEncoder;
//import junit.framework.Assert;
import org.apache.poi.ss.usermodel.*;

/**
 * Created by Marie on 11/29/2016.
 */
public class Controller {


    public static void main(String[] args) throws Exception {

        //for(Row row:wb)
        InputStream inp = new FileInputStream("D:\\oldharddrive2015\\Documents and Settings\\My Documents\\Entertainment.xlsx");
        //InputStream inp = new FileInputStream("workbook.xlsx");

        Workbook wb = WorkbookFactory.create(inp);
        Sheet sheet = wb.getSheetAt(2);
        String rawTitle = sheet.getRow(1).getCell(1).getStringCellValue();

        // build a URL
        String title = rawTitle.replace(" ","%20");
        String s = "http://www.omdbapi.com/?s="+title+"&type=series";
        //s += URLEncoder.encode(addr, "UTF-8");
        URL url = new URL(s);

        // read from the URL
        Scanner scan = new Scanner(url.openStream());
        String str = new String();
        while (scan.hasNext())
            str += scan.nextLine();
        scan.close();

        // build a JSON object
        JSONObject obj = new JSONObject(str);
        if (!obj.getString("Response").equals("True"))
            //search omdb
            return;

        // get the first result
        for(int x=0; x<obj.getJSONArray("Search").length(); x++) {
            JSONObject res = obj.getJSONArray("Search").getJSONObject(x);
            System.out.println(res.getString("Title")+res.getString("Year"));
            //list.add or new object or SQL insert
        }

//        JSONObject loc =
//                res.getJSONObject("geometry").getJSONObject("location");
//        System.out.println("lat: " + loc.getDouble("lat") +
//                ", lng: " + loc.getDouble("lng"));
    }
}