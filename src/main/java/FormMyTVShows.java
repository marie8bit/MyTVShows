import org.json.JSONObject;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
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
    private JLabel lblInst;
    private JButton deleteButton;
    private JRadioButton addWithSpreadsheetRadioButton;
    private JRadioButton addBySearchRadioButton;
    private JLabel lblFirst;
    private JLabel lblSecond;
    private JLabel lblExt;
    private JButton archiveButton;
    private JLabel lblName1;
    private JButton selectSpreadsheetButton;
    private JComboBox cbxSheet;
    private JComboBox cbxCol;
    private JLabel lblX;
    private JScrollPane jScrollPane;
    private JLabel lblFetch;
    private JTextArea taName;
    //private JScrollPane mtsJScroll;
    private Object newV;
    private Object oldV;

protected FormMyTVShows(TableModel tm){
    setContentPane(rootPanel);
    pack();
    addWindowListener(this);
    setVisible(true);
    setSize(new Dimension(1000, 700));

    mtsTable.setModel(tm);
    setTableColumns();

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    mtsTable.addPropertyChangeListener(new PropertyChangeListener() {
        //method to allow for editing
        @Override
        public void propertyChange(PropertyChangeEvent evt){
            //action listener to allow for changes to the database when changes are made in the jtable
            String table =activeRadioButton.isSelected()?"active":"archive";
            if ("tableCellEditor".equals(evt.getPropertyName())) {
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
                        int rw = mtsTable.getSelectedRow();
                        //String oldValue = mtsTable.getValueAt(rw, col).toString();//used for testing
                        try {
                            //update database
                        MTVSdb.getPrepStatement(col, newValue, primary, table);
                        updateForm(table, mtsTable);
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
                    //code to identify which search parameter has been selected
                    if (!txtPath.getText().equals("")) {
                        //preform IMDB ID search
                        String str = APIworker.getOMDBentry(txtPath.getText(), "ID");
                        JSONObject obj = new JSONObject(str);
                        String PKID = obj.getString("imdbID");
                        MTVSdb.addRowFull("active",PKID, obj.getString("Title"),
                                obj.getString("Year"), obj.getString("Plot"));
                        updateForm("active",mtsTable);
                        txtPath.setText("");
                        lblInst.setText("Added entry");
                    } else {
                        //preform Title search
                        lblInst.setText("Fetching data...");
                        lblInst.setForeground(Color.red);
                        AddAPIworker aaw = new AddAPIworker(FormMyTVShows.this ,txtName.getText(), "title" );
                        aaw.execute();

                    }
            }
            catch(SQLException sqle){
                sqle.printStackTrace();
                System.out.println("HereAdd");
                sqle.printStackTrace();
            }
            catch (NullPointerException npe){
                npe.printStackTrace();
            }
            catch (java.lang.Exception j){
                //provides information to the user and focuses back on the text field that generated the error
                j.printStackTrace();
                txtPath.setText("");
                txtName.setText("");
                txtPath.requestFocus();
                lblInst.setText("Entry not found");
                lblInst.setForeground(Color.red);
            }
        }
    });
    //deletes row from database
    deleteButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //gets primary key for safe editing
            //allows for the selection of mutiple rows
            int[] selection = mtsTable.getSelectedRows();
            //deletes from the highest row index number to allow for index changes
            for (int x =selection.length-1; x>=0;x--) {
                String primary = mtsTable.getValueAt(selection[x],0).toString();
                String table = activeRadioButton.isSelected() ? "active" : "archive";
                try {
                    MTVSdb.deleteRow(primary, table);
                }
                catch (java.lang.Exception j) {
                    j.printStackTrace();
                    System.out.println("whaa?");
                }
                updateForm(table, mtsTable);
            }
        }
    });
    activeRadioButton.addActionListener(new ActionListener() {
        @Override
        //populates Jtable with appropriate table from the database
        public void actionPerformed(ActionEvent e) {
            updateForm("active", mtsTable);
            archiveButton.setVisible(true);
        }
    });
    archiveRadioButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            updateForm("archive" , mtsTable );
            archiveButton.setVisible(false);
        }
    });
    //handles the button click event by moving row from active to archive
    archiveButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selection = mtsTable.getSelectedRows();
            for (int x =selection.length-1; x>=0;x--) {
                int row = selection[x];
                String primary = mtsTable.getValueAt(row, 0).toString();
                String name = mtsTable.getValueAt(row, 1).toString();
                String year = mtsTable.getValueAt(row, 2).toString();
                String plot = mtsTable.getValueAt(row, 3).toString();
                try {
                    //add row to archive, delete row from active
                    MTVSdb.addRowFull("archive",primary, name, year, plot);
                    MTVSdb.deleteRow(primary, "active");
                    updateForm("active", mtsTable);
                } catch (Exception ex) {
                    System.out.println("archiveButton");
                }
            }
        }
    });
    //event that triggers the reading of the excel file
    selectSpreadsheetButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //source Fred from class and website accessed 12/7/16
            //http://www.codejava.net/java-se/swing/show-simple-open-file-dialog-using-jfilechooser
            if(cbxCol.getSelectedIndex()==0||cbxSheet.getSelectedIndex()==0) {
                lblX.setText("Select sheet and column number");
                }
                else{

                    JFileChooser chooser = new JFileChooser();
                    Component parent = chooser.getParent();
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel", "xls", "xlsx");
                    chooser.setFileFilter(filter);
                    int returnVal = chooser.showOpenDialog(parent);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        chooser.setCurrentDirectory(new java.io.File("user.home"));
                        File f = chooser.getSelectedFile();
                        String file = f.getAbsolutePath();
                        //lets the user choose the sheet and column in an excel file
                        int col = (Integer.parseInt(cbxCol.getSelectedItem().toString()))-1;
                        int sheet = (Integer.parseInt(cbxSheet.getSelectedItem().toString()))-1;
                        lblInst.setText("Fetching data...");
                        lblInst.setForeground(Color.red);
                        //instantiate Swing worker to track activity
                        XAPIworker worker = new XAPIworker(FormMyTVShows.this, file, sheet, col);
                        worker.execute();
                }
            }
            updateForm("active", mtsTable);
        }
    });
}
    //informs the user that the program process finished
    public void finish(){
            String table = activeRadioButton.isSelected()?"active": "archive";
            lblInst.setText("Items Fetched");
            txtPath.setText("");
            txtName.setText("");
            lblInst.setForeground(Color.green);
            updateForm(table, mtsTable);
    }
    //sets the preferred width of each table column
    private void setTableColumns() {
        mtsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        mtsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        mtsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        mtsTable.getColumnModel().getColumn(3).setPreferredWidth(350);
    }
    //method to update the displayed result set
    public void updateForm(String table, JTable jt){
        try {
            //calls update method sends column number, new data to enter and
            // the primary key for the sql table
            ResultSet updateResultSet = MTVSdb.getResultSet("active".equals(table)? table: "archive" );
            //refreshes the data in the jtable after update
            TableModel tM = new TableModel(updateResultSet);
            jt.setModel(tM);
            this.setTableColumns();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("here");
        }
    }
    public void fail() {
    //reset component defaults and provide information to user
    txtPath.setText("");
    txtName.setText("");
    txtPath.requestFocus();
    lblInst.setText("Entry not found");
    lblInst.setForeground(Color.red);
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
