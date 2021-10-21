package com.kevin.project3;

import javax.swing.*;

public class DatabaseUI extends JFrame{
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
    private JTextArea SQLExecutionResultWindowTextArea;
    private JLabel SQLExeLabel;
    private JTextField SQLCommandTextField;

    public DatabaseUI(){



    }
    public static void main(String[] args){
        //JDBCDriverComboBox
        //chose JDBC Driver




        JFrame frame = new JFrame("DatabaseUI");
        frame.setContentPane(new DatabaseUI().Window);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
