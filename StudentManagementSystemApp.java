/* 
 * Student Management System App
 * Name: Guilherme Duarte da Silva
 * ID: 25662
 * 
 * Description: This is the main class of the application. It calls the GUI class.
 *              It is also responsible for the execution of the application.
*/

import javax.swing.*;

public class StudentManagementSystemApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudentManagementSystemGUI::new);
    }
    
}
