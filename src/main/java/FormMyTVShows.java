import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Marie on 11/29/2016.
 */
public class FormMyTVShows extends JFrame implements WindowListener
{
    private JRadioButton activeRadioButton;
    private JRadioButton archiveRadioButton;
    private JTable mtsTable;
    private JTextField txtPath;
    private JTextField txtName;
    private JRadioButton xlsRadioButton;
    private JRadioButton xlsxRadioButton;
    private JButton btnAdd;
    private JLabel lblPlot;
    private JPanel rootPanel;
    //private JScrollPane mtsScrollPane;
    private JLabel lblInst;
    private JButton deleteButton;
    private JRadioButton addWithSpreadsheetRadioButton;
    private JRadioButton addBySearchRadioButton;
    private JLabel lblFirst;
    private JLabel lblSecond;
    private JLabel lblExt;
    private Object newV;
    private Object oldV;

protected FormMyTVShows(TableModel tm){
    setContentPane(rootPanel);
    pack();
    addWindowListener(this);
    setVisible(true);
    setSize(new Dimension(1000, 1000));
    mtsTable.setModel(tm);
    mtsTable.setGridColor(Color.BLACK);
    mtsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
    mtsTable.getColumnModel().getColumn(1).setPreferredWidth(300);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    //mtsScrollPane = new JScrollPane(mtsTable);
    //action listener that fires whenever editing starts or stops in a cell
    mtsTable.addPropertyChangeListener(new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            //action listener to allow for changes to the database when changes are made in the jtable
            if ( "tableCellEditor".equals(evt.getPropertyName())) {
                String primary = mtsTable.getValueAt(mtsTable.getSelectedRow(), 0).toString();
                newV = evt.getNewValue();//used for testing
                oldV = evt.getOldValue();
                //ignores property change editor when the cell begins editing
                if (oldV != null) {
                    //coverts event object into table cell editor object
                    TableCellEditor oldVal = (TableCellEditor) oldV;
                    //Object s = oldVal.getCellEditorValue();
                    //gets text value from object
                    String newValue = oldVal.getCellEditorValue().toString();
                    int col = mtsTable.getSelectedColumn();
                    int rw =mtsTable.getSelectedRow();

                    String oldValue = mtsTable.getValueAt(rw,col).toString();//used for testing
                    try {
                        //calls update method sends column number, new data to enter and
                        // the primary key for the sql table
                        ResultSet updateResultSet = MTVSdb.getActive();
                        //refreshes the data in the jtable after update
                        TableModel tM = new TableModel(updateResultSet);
                        mtsTable.setModel(tM);
                        mtsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
                        //mtsScrollPane = new JScrollPane(mtsTable);

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("here");
                    }
                }

            }
        }
    });
    //add button click event calls insert sql statement
    btnAdd.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try
            {
                if (addWithSpreadsheetRadioButton.isSelected()) {
                    Controller.getSpreadsheet(txtPath.getText(), txtName.getText(), xlsRadioButton.isSelected() ? ".xls" : ".xlsx");
                } else {
                    if (txtPath.getText() != null) {
                        String str = Controller.getOMDBentry(txtPath.getText(), "ID");
                        JSONObject obj = new JSONObject(str);
                        Controller.getAPISearch(obj);
                    } else {
                        String str = Controller.getOMDBentry(txtName.getText(), "Title");
                        JSONObject obj = new JSONObject(str);
                        Controller.getAPISearch(obj);
                    }
                    //sends data from txtfields to database class to run insert query
                    ResultSet updateResultSet = MTVSdb.getActive();
                    TableModel tM = new TableModel(updateResultSet);

                    mtsTable.setModel(tM);
                    mtsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
                    //mtsScrollPane = new JScrollPane(mtsTable);
                }

            }
            catch(SQLException sqle){
                sqle.printStackTrace();
                System.out.println("HereAdd");
                sqle.printStackTrace();
            }
            catch (java.lang.Exception j){
                //provides information to the user and focuses back on the text field that generated the error
                txtPath.setText("");
                txtName.setText("");
                txtPath.requestFocus();
                lblInst.setText("Entry not found");
            }

        }

    });
    //deletes row from database
    deleteButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //gets primary key for safe editing
            String primary = mtsTable.getValueAt(mtsTable.getSelectedRow(), 0).toString();
            try {
                //gets new resultset after row nas been deleted and formats the jtable column dimensions
                ResultSet updateResultSet = MTVSdb.deleteRow(primary);
                TableModel tM = new TableModel(updateResultSet);
                mtsTable.setModel(tM);
                mtsTable.getColumnModel().getColumn(0).setPreferredWidth(200);

            }
            catch(SQLException s){
                System.out.println("HereDelete");
            }
            catch (java.lang.Exception j){
                System.out.println("whaa?");
            }
        }
    });
    addWithSpreadsheetRadioButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            lblFirst.setText("Enter the full path to spreadsheet you want to import");
            lblSecond.setText("Enter the name of the spreadsheet you want to import");
            lblExt.setText("Choose a file extension");
        }
    });
    addBySearchRadioButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            lblFirst.setText("Enter IMDB ID from the internet");
            lblSecond.setText("Or Enter the name of the TV Show");
            lblExt.setText("");
            xlsRadioButton.setVisible(false);
            xlsxRadioButton.setVisible(false);
        }
    });
    addWithSpreadsheetRadioButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {

        }
    });
}
    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        System.out.println("Window closing");
        Controller.shutdown();
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
