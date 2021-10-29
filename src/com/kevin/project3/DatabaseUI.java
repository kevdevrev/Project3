package com.kevin.project3;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.Vector;

public class DatabaseUI extends JFrame{
    private static Connection connection;
    private static Connection operationsLogConnection;
    private static Statement operationsLogStatement;
    private static Statement statement;
    private JLabel JDBCDriverLabel;
    private JPanel Window;
    private JLabel DatabaseURLLabel;
    private JLabel UsernameLabel;
    private JLabel PasswordLabel;
    private JLabel EnterSQLLabel;
    private JButton ClearSQLButton;
    private JButton ExecuteSQLButton;
    private JTextField UsernameField;
    private JComboBox DatabaseURLComboBox;
    private JComboBox JDBCDriverComboBox;
    private JTextField PasswordField;
    private JButton ConnectToDBButton;
    private JLabel StatusLabel;
    private JLabel SQLExeLabel;
    private JTextField SQLCommandTextField;
    private JTable SQLJTableOutput;
    private JButton ClearResultWindow;
    private JTextArea SQLTextArea;
    private boolean connectedToDatabase = false;
    FileInputStream filein = null;
    Properties properties = new Properties();
    MysqlDataSource operationsLog = null;
    private boolean connectedToOperationsLog = false;

//    Connection connection;

    String[] JDBCArray = {"", "com.mysql.cj.jdbc.Driver"};
    String[] URLArray = {"", "jdbc:mysql://localhost:3306/project3?useTimezone=true&serverTimezone=UTC"};
    String incrementNumQueries = "update operationscount set num_queries = num_queries + 1;";
    String incrementNumUpdates = "update operationscount set num_updates = num_updates + 1;";
    public DatabaseUI(){
        //Driver Combo Box
        JDBCDriverComboBox.addItem(JDBCArray[0]);
        JDBCDriverComboBox.addItem(JDBCArray[1]);
        JDBCDriverComboBox.setSelectedIndex(0);
        //End Driver Combo Box
        //URL Combo Box
        DatabaseURLComboBox.addItem(URLArray[0]);
        DatabaseURLComboBox.addItem(URLArray[1]);
        DatabaseURLComboBox.setSelectedIndex(0);
        //End URL Combo bOX
        //Connection Data Source
        MysqlDataSource dataSource = new MysqlDataSource();
        JDBCDriverComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //todo try to connect to JDBC driver now.
                JComboBox cb = (JComboBox) e.getSource();
                String className = (String) cb.getSelectedItem();
                if(!className.equals("")) {
                    try {
                        Class.forName(className);
                    } catch (ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }
                    System.out.println("Driver loaded");
                }

            }
        });
        DatabaseURLComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                //assign url for datasource
                String urlString = (String) cb.getSelectedItem();
                if(!urlString.equals("")) {
                    dataSource.setURL(urlString);
                }
                System.out.println("Set URL to " + dataSource.getURL());
            }
        });
        ConnectToDBButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnectFromDatabase();
                dataSource.setUser(UsernameField.getText());
                dataSource.setPassword(PasswordField.getText());

                //project3 database connection
                try {
                    connection = dataSource.getConnection();
                    StatusLabel.setText("Connected to " + dataSource.getURL());
                    statement = connection.createStatement();
                    connectedToDatabase = true;
                } catch (SQLException ex) {
                    String errorMsg = "Message: " + ((SQLException)ex).getMessage();
                    JOptionPane.showMessageDialog(null,
                            errorMsg,
                            "Database error",
                            JOptionPane.WARNING_MESSAGE);
                }

                //operations log connection
                try {
                    filein = new FileInputStream("db.properties");
                    properties.load(filein);
                    operationsLog = new MysqlDataSource();
                    operationsLog.setURL(properties.getProperty("MYSQL_DB_URL"));
                    operationsLog.setUser(properties.getProperty("MYSQL_DB_USERNAME"));
                    operationsLog.setPassword(properties.getProperty("MYSQL_DB_PASSWORD"));
                    operationsLogConnection = operationsLog.getConnection();
                    connectedToOperationsLog = true;

                } catch (IOException | SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        ExecuteSQLButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                String SQLCommandStrings = SQLCommandTextField.getText();
                String SQLCommandStrings = SQLTextArea.getText();
                try {
                    String[] statements = createStatementArray(SQLCommandStrings);
                    for(int i = 0; i < statements.length; i++) {
                        boolean queryOrNot = determineStatementType(statements[i]);
                        ResultSet results;
                        if (queryOrNot) {
                            results = QueryIt(statements[i]);
                            //should only do the belong if it does not fail :)
                            AddToOperationsLog(incrementNumQueries);
                            //update table
                            SQLJTableOutput.setModel(createTableModel(results));
                        } else {

                            ExecuteIt(statements[i]);
                            AddToOperationsLog(incrementNumUpdates);
                            //clear table
                            SQLJTableOutput.setModel(new DefaultTableModel());
                        }
                    }
                } catch (SQLException ex) {
                    String errorMsg = "Message: " + ((SQLException)ex).getMessage();
                    JOptionPane.showMessageDialog(null,
                            errorMsg,
                            "Database error",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        ClearSQLButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SQLTextArea.setText("");
            }
        });
        ClearResultWindow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SQLJTableOutput.setModel(new DefaultTableModel());
            }
        });
    }

    private String[] createStatementArray(String statements) {
        String[] result = statements.split("; ");
        return result;
    }

    private boolean determineStatementType(String statement) {
        if(statement.stripLeading().toLowerCase().startsWith("select")){
            return true;
        }else{
            return false;
        }
    }

    public static DefaultTableModel createTableModel(ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsmd = resultSet.getMetaData();
        //get name of columns
        Vector<String> columnNames = new Vector<String>();
        int numCol = rsmd.getColumnCount();
        for(int i = 1; i <= numCol; i++){
            columnNames.add(rsmd.getColumnName(i));
        }

        //get data
        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        while (resultSet.next()){
            Vector<Object> dataPoint = new Vector<Object>();
            for(int j = 1; j <= numCol; j++){
                dataPoint.add(resultSet.getObject(j));
            }
            data.add(dataPoint);
        }
        return new DefaultTableModel(data, columnNames);

    }

    public static ResultSet QueryIt(String queryString) throws SQLException {
        //create statement object
        //execute a query using the statement object and get result from the DB
        ResultSet resultSet = statement.executeQuery(queryString);



        return resultSet;
    }

    public static int ExecuteIt(String queryString) throws SQLException {
        //create statement object
        int result = statement.executeUpdate(queryString);



        return result;
    }

    public static int AddToOperationsLog(String queryString) throws SQLException {
        //create statement object
        operationsLogStatement = operationsLogConnection.createStatement();

        //execute a query using the statement object and get result from the DB
        int safeOff = operationsLogStatement.executeUpdate("SET SQL_SAFE_UPDATES = 0");
        int result = operationsLogStatement.executeUpdate(queryString);
        int safeOn = operationsLogStatement.executeUpdate("SET SQL_SAFE_UPDATES = 1");

        return result;
    }

    public void disconnectFromDatabase()
    {
        if ( !connectedToDatabase )
            return;
            // close Statement and Connection
        else try
        {
            operationsLogStatement.close();
            statement.close();
            operationsLogConnection.close();
            connection.close();
        } // end try
        catch ( SQLException sqlException )
        {
            sqlException.printStackTrace();
        } // end catch
        finally  // update database connection status
        {
            connectedToDatabase = false;
            connectedToOperationsLog = false;
        } // end finally
    } // end method disconnectFromDatabase

    public static void main(String[] args){
        //JDBCDriverComboBox
        //chose JDBC Driver
        JFrame frame = new JFrame();
        frame.setTitle("Database Ui");
        frame.getContentPane().add(new DatabaseUI().Window);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
