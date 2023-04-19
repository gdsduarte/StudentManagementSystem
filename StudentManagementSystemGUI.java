import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StudentManagementSystemGUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudentManagementSystemGUI::new);
    }

    private final StudentManagementSystem sms;
    private final JFrame frame;
    private final JTable dataDisplayDashboard;
    private final JTable dataDisplayStudent;
    private final JTable dataDisplayModule;
    private final JTable dataDisplayGrade;
    private Student currentStudent;

    // StudentManagementSystemGUI constructor method
    public StudentManagementSystemGUI() {
        sms = new StudentManagementSystem();
        frame = createMainFrame();

        // Create table models
        DefaultTableModel dashboardTableModel = createDashboardTableModel();
        DefaultTableModel studentTableModel = createStudentTableModel();
        DefaultTableModel moduleTableModel = createModuleTableModel();
        DefaultTableModel gradeTableModel = createGradeTableModel();

        // Create tables
        dataDisplayDashboard = createTable(dashboardTableModel);
        dataDisplayStudent = createTable(studentTableModel);
        dataDisplayModule = createTable(moduleTableModel);
        dataDisplayGrade = createTable(gradeTableModel);

        // Set the custom GradeColorRenderer for the dashboard table
        dataDisplayDashboard.setDefaultRenderer(Object.class, new GradeColorRenderer(3));
        setupGUI();

        // Load data from file when the program starts up
        sms.loadFromFile("Java/StudentManagementSystem_CA3/database.csv");
        updateDataDisplays();
    }

    // Create a table models
    private DefaultTableModel createDashboardTableModel() {
        String[] columnNames = { "Student", "Module", "Grade", "Status", "Enrolled" };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        // Loop through all students and modules to find the status of each module for
        // each student
        for (Student student : sms.getStudents()) {
            for (Module module : sms.getModules()) {
                String status;
                boolean enrolled = student.getEnrolledModules().contains(module);
                Optional<Grade> gradeOpt = sms.findGrade(student, module);

                if (enrolled) {
                    if (gradeOpt.isPresent()) {
                        double gradeValue = gradeOpt.get().getGrade();
                        if (gradeValue >= 40) {
                            status = "Pass";
                        } else {
                            status = "Fail";
                        }
                    } else {
                        status = "In Progress";
                    }
                } else {
                    if (gradeOpt.isPresent()) {
                        double gradeValue = gradeOpt.get().getGrade();
                        if (gradeValue >= 40) {
                            status = "Completed";
                        } else {
                            status = "To Repeat";
                        }
                    } else {
                        continue;
                    }
                }

                Object[] rowData = { student.getName(), module.getName(), gradeOpt.map(Grade::getGrade).orElse(null),
                        status, enrolled ? "Yes" : "No" };
                model.addRow(rowData);
            }
        }
        return model;
    }

    private DefaultTableModel createStudentTableModel() {
        String[] columnNames = { "Student ID", "Name", "Email" };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        sms.getStudents().forEach(
                student -> model.addRow(new Object[] { student.getId(), student.getName(), student.getEmail() }));
        return model;
    }

    private DefaultTableModel createModuleTableModel() {
        String[] columnNames = { "Module ID", "Name", "Teacher", "Semester" };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        sms.getModules().forEach(
                module -> model.addRow(
                        new Object[] { module.getId(), module.getName(), module.getTeacher(), module.getSemester() }));
        return model;
    }

    private DefaultTableModel createGradeTableModel() {
        String[] columnNames = { "Student ID", "Module ID", "Grade" };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        sms.getGrades().forEach(grade -> model
                .addRow(new Object[] { grade.getStudent().getId(), grade.getModule().getId(), grade.getGrade() }));
        return model;
    }

    // Create main frame for GUI
    private JFrame createMainFrame() {
        JFrame mainFrame = new JFrame("Student Management System");
        mainFrame.setSize(900, 500);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Alert user to save data
                int result = JOptionPane.showConfirmDialog(mainFrame, "Do you want to save data before exiting?",
                        "Save data?", JOptionPane.YES_NO_CANCEL_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    sms.saveToFile("Java/StudentManagementSystem_CA3/database.csv");
                    JOptionPane.showMessageDialog(mainFrame, "Data saved successfully.");
                    System.exit(0);
                } else if (result == JOptionPane.NO_OPTION) {
                    System.exit(0);
                } else {
                    mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                }
            }
        });
        mainFrame.setLocationRelativeTo(null);
        return mainFrame;
    }

    // Setup GUI components and add them to the frame
    private void setupGUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Dashboard
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new BoxLayout(dashboardPanel, BoxLayout.Y_AXIS));
        dashboardPanel.add(new JScrollPane(dataDisplayDashboard));

        JPanel dashboardControlsPanel = createDashboardPanel();
        dashboardPanel.add(dashboardControlsPanel);
        tabbedPane.addTab("Dashboard", dashboardPanel);

        // Students
        JPanel studentPanel = createStudentPanel();
        tabbedPane.addTab("Students", studentPanel);

        // Modules
        JPanel modulePanel = createModulePanel();
        tabbedPane.addTab("Modules", modulePanel);

        // Grades
        JPanel gradePanel = createGradePanel();
        tabbedPane.addTab("Grades", gradePanel);

        // Enrollment
        JPanel enrollmentPanel = createEnrollmentPanel();
        tabbedPane.addTab("Enrollment", enrollmentPanel);

        frame.add(tabbedPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Create a table model for the dashboard
    private JTable createTable(DefaultTableModel tableModel) {
        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setDefaultEditor(Object.class, null);
        table.setAutoCreateRowSorter(true);

        // Apply the custom GradeColorRenderer to the table
        GradeColorRenderer gradeColorRenderer = new GradeColorRenderer(0);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(gradeColorRenderer);
        }

        return table;
    }

    // Create the panels for the GUI components
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JScrollPane(dataDisplayGrade));

        // Create a JPanel with BorderLayout for the button
        JPanel buttonPanel = new JPanel(new BorderLayout());

        // Add filter and unfilter buttons to the left side
        JPanel filterButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterButtonPanel.add(createButton("Filter", this::filterDashboard));
        filterButtonPanel.add(createButton("Unfilter", this::unfilterDashboard));
        buttonPanel.add(filterButtonPanel, BorderLayout.WEST);

        // Create a Save button and add an action listener that displays a confirmation
        // dialog
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            int dialogResult = JOptionPane.showConfirmDialog(frame, "Do you want to save the changes?", "Save",
                    JOptionPane.YES_NO_OPTION);

            if (dialogResult == JOptionPane.YES_OPTION) {
                saveToFile();
            }
        });

        // Add the Save button to the button panel on the right side
        JPanel saveButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButtonPanel.add(saveButton);
        buttonPanel.add(saveButtonPanel, BorderLayout.EAST);

        // Add the button panel to the main panel
        panel.add(buttonPanel);

        return panel;
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(dataDisplayStudent));

        // Create a JPanel for the filter and unfilter buttons
        JPanel filterButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterButtonPanel.add(createButton("Filter", this::filterStudents));
        filterButtonPanel.add(createButton("Unfilter", this::unfilterStudents));

        // Create a JPanel for the Add and Remove buttons
        JPanel addButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButtonPanel.add(createButton("Add", this::addStudent));
        addButtonPanel.add(createButton("Remove", this::removeStudent));

        // Create a JPanel with BorderLayout for the button panel
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(filterButtonPanel, BorderLayout.WEST);
        buttonPanel.add(addButtonPanel, BorderLayout.EAST);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        dataDisplayStudent.setDefaultRenderer(Object.class, new CustomTableCellRenderer(new Color(219, 235, 241)));
        return panel;
    }

    private JPanel createModulePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(dataDisplayModule));

        // Create a JPanel for the filter and unfilter buttons
        JPanel filterButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterButtonPanel.add(createButton("Filter", this::filterModules));
        filterButtonPanel.add(createButton("Unfilter", this::unfilterModules));

        // Create a JPanel for the Add and Remove buttons
        JPanel addButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButtonPanel.add(createButton("Add", this::addModule));
        addButtonPanel.add(createButton("Remove", this::removeModule));

        // Create a JPanel with BorderLayout for the button panel
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(filterButtonPanel, BorderLayout.WEST);
        buttonPanel.add(addButtonPanel, BorderLayout.EAST);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        dataDisplayModule.setDefaultRenderer(Object.class, new CustomTableCellRenderer(new Color(219, 235, 241)));
        return panel;
    }

    private JPanel createGradePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(dataDisplayGrade));

        // Create a JPanel for the filter and unfilter buttons
        JPanel filterButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterButtonPanel.add(createButton("Filter", this::filterGrades));
        filterButtonPanel.add(createButton("Unfilter", this::unfilterGrades));

        // Create a JPanel for the Add and Remove buttons
        JPanel addButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButtonPanel.add(createButton("Add", this::addGrade));
        addButtonPanel.add(createButton("Remove", this::removeGrade));

        // Create a JPanel with BorderLayout for the button panel
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(filterButtonPanel, BorderLayout.WEST);
        buttonPanel.add(addButtonPanel, BorderLayout.EAST);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        dataDisplayGrade.setDefaultRenderer(Object.class, new CustomTableCellRenderer(new Color(219, 235, 241)));
        return panel;
    }

    private JPanel createEnrollmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Search Panel
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));

        JPanel inputPanel = new JPanel(new FlowLayout());
        JTextField studentIdSearchField = new JTextField(20);
        inputPanel.add(studentIdSearchField);

        JButton searchButton = new JButton("Search");
        inputPanel.add(searchButton);

        searchPanel.add(inputPanel);

        JLabel studentInfoLabel = new JLabel();
        searchPanel.add(studentInfoLabel);

        // Modules Panel
        JPanel modulesPanel = new JPanel(new BorderLayout());
        JPanel radioButtonPanel = new JPanel();
        JRadioButton firstYearButton = new JRadioButton("First Year");
        JRadioButton secondYearButton = new JRadioButton("Second Year");
        JRadioButton thirdYearButton = new JRadioButton("Third Year");
        radioButtonPanel.add(firstYearButton);
        radioButtonPanel.add(secondYearButton);
        radioButtonPanel.add(thirdYearButton);

        ButtonGroup yearButtons = new ButtonGroup();
        yearButtons.add(firstYearButton);
        yearButtons.add(secondYearButton);
        yearButtons.add(thirdYearButton);

        modulesPanel.add(radioButtonPanel, BorderLayout.NORTH);

        JPanel semestersPanel = new JPanel(new GridLayout(1, 2));
        JScrollPane sem1ScrollPane = new JScrollPane();
        JScrollPane sem2ScrollPane = new JScrollPane();
        semestersPanel.add(sem1ScrollPane);
        semestersPanel.add(sem2ScrollPane);

        TitledBorder sem1Border = new TitledBorder("First Semester");
        sem1ScrollPane.setBorder(sem1Border);
        TitledBorder sem2Border = new TitledBorder("Second Semester");
        sem2ScrollPane.setBorder(sem2Border);

        modulesPanel.add(semestersPanel, BorderLayout.CENTER);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, searchPanel, modulesPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.1);
        panel.add(splitPane, BorderLayout.CENTER);

        // Search button action
        searchButton.addActionListener(e -> {
            String studentId = studentIdSearchField.getText();
            Optional<Student> studentOpt = sms.getStudentById(studentId);
            if (studentOpt.isPresent()) {
                currentStudent = studentOpt.get();
                long completedModulesCount = currentStudent.getEnrolledModules().stream()
                        .filter(Module::isCompleted)
                        .filter(Module::isPassed)
                        .count();
                studentInfoLabel
                        .setText(currentStudent.getName() + " - " + currentStudent.getEmail()
                                + " - Completed and Passed Modules: "
                                + completedModulesCount);
            } else {
                studentInfoLabel.setText("Student not found.");
                currentStudent = null;
            }
            String[] semIdentifiers = getCurrentSemesterIdentifier(firstYearButton, secondYearButton, thirdYearButton);
            updateModulesTable(sem1ScrollPane, sem2ScrollPane, semIdentifiers[0], semIdentifiers[1]);
        });

        // Update button
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            String studentId = studentIdSearchField.getText();
            Optional<Student> studentOpt = sms.getStudentById(studentId);
            if (studentOpt.isPresent()) {
                updateEnrollment(sem1ScrollPane, studentId);
                updateEnrollment(sem2ScrollPane, studentId);
                updateDataDisplays();
            }
        });

        panel.add(updateButton, BorderLayout.SOUTH);

        firstYearButton.addActionListener(e -> {
            String[] semIdentifiers = getCurrentSemesterIdentifier(firstYearButton, secondYearButton, thirdYearButton);
            updateModulesTable(sem1ScrollPane, sem2ScrollPane, semIdentifiers[0], semIdentifiers[1]);
        });

        secondYearButton.addActionListener(e -> {
            String[] semIdentifiers = getCurrentSemesterIdentifier(firstYearButton, secondYearButton, thirdYearButton);
            updateModulesTable(sem1ScrollPane, sem2ScrollPane, semIdentifiers[0], semIdentifiers[1]);
        });

        thirdYearButton.addActionListener(e -> {
            String[] semIdentifiers = getCurrentSemesterIdentifier(firstYearButton, secondYearButton, thirdYearButton);
            updateModulesTable(sem1ScrollPane, sem2ScrollPane, semIdentifiers[0], semIdentifiers[1]);
        });

        firstYearButton.setSelected(true);
        SwingUtilities.invokeLater(() -> {
            updateModulesTable(sem1ScrollPane, sem2ScrollPane, "SEM1", "SEM2");
        });

        return panel;
    }

    private String[] getCurrentSemesterIdentifier(JRadioButton firstYearButton, JRadioButton secondYearButton,
            JRadioButton thirdYearButton) {
        if (firstYearButton.isSelected()) {
            return new String[] { "SEM1", "SEM2" };
        } else if (secondYearButton.isSelected()) {
            return new String[] { "SEM3", "SEM4" };
        } else {
            return new String[] { "SEM5", "SEM6" };
        }
    }

    private JTable createModuleTable(List<Module> modules, Student student) {
        String[] columnNames = { "Module ID", "Module Name", "Enrolled" };
        Object[][] data = new Object[modules.size()][3];
        int i = 0;
        for (Module module : modules) {
            data[i][0] = module.getId();
            data[i][1] = module.getName();
            data[i][2] = student != null && student.getEnrolledModules().stream()
                    .anyMatch(enrolledModule -> enrolledModule.getId().equalsIgnoreCase(module.getId()));
            i++;
        }

        JTable table = new JTable(data, columnNames);
        table.getColumn("Enrolled").setCellEditor(new DisabledCheckboxCellEditor(new JCheckBox()));
        table.getColumn("Enrolled").setCellRenderer(new DisabledCheckboxCellRenderer());
        table.setRowHeight(25);

        return table;
    }

    private List<Module> getSemesterModules(String semesterIdentifier) {
        return sms.getModules().stream()
                .filter(module -> module.getSemester().toUpperCase().contains(semesterIdentifier.toUpperCase()))
                .collect(Collectors.toList());
    }

    private void updateModulesTable(JScrollPane sem1ScrollPane, JScrollPane sem2ScrollPane,
            String sem1Identifier, String sem2Identifier) {
        List<Module> sem1Modules = getSemesterModules(sem1Identifier);
        List<Module> sem2Modules = getSemesterModules(sem2Identifier);

        sem1ScrollPane.setViewportView(createModuleTable(sem1Modules, currentStudent));
        sem2ScrollPane.setViewportView(createModuleTable(sem2Modules, currentStudent));
    }

    class DisabledCheckboxCellEditor extends DefaultCellEditor {
        private JCheckBox checkBox;
    
        public DisabledCheckboxCellEditor(JCheckBox checkBox) {
            super(checkBox);
            this.checkBox = checkBox;
        }
    
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
            String moduleId = ((String) table.getValueAt(row, 0));
            if (currentStudent != null) {
                checkBox.setEnabled(!currentStudent.hasPassedModule(moduleId));
            } else {
                checkBox.setEnabled(false);
            }
            return c;
        }
    }
    
    class DisabledCheckboxCellRenderer extends JCheckBox implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            if (value instanceof Boolean) {
                setSelected((Boolean) value);
            }
            String moduleId = ((String) table.getValueAt(row, 0));
            if (currentStudent != null) {
                setEnabled(!currentStudent.hasPassedModule(moduleId));
            } else {
                setEnabled(false);
            }
            return this;
        }
    }
    

    // Create Button for GUI actions
    private JButton createButton(String buttonText, Runnable action) {
        JButton button = new JButton(buttonText);
        button.addActionListener(e -> action.run());
        return button;
    }

    // Filter the dashboard table
    private void filterDashboard() {
        String filter = JOptionPane.showInputDialog("Type to filter:");
        if (filter != null) {
            TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(dataDisplayDashboard.getModel());
            dataDisplayDashboard.setRowSorter(rowSorter);
            rowSorter.setRowFilter(RowFilter.regexFilter(filter));
        }
    }

    private void unfilterDashboard() {
        dataDisplayDashboard.setRowSorter(null);
    }

    // Add, remove, filter students
    private void addStudent() {
        while (true) {
            JPanel inputPanel = new JPanel(new GridLayout(3, 2));
            JTextField nameField = new JTextField();
            JTextField idField = new JTextField();
            JTextField emailField = new JTextField();

            inputPanel.add(new JLabel("Name:"));
            inputPanel.add(nameField);
            inputPanel.add(new JLabel("ID:"));
            inputPanel.add(idField);
            inputPanel.add(new JLabel("Email:"));
            inputPanel.add(emailField);

            int result = JOptionPane.showOptionDialog(frame, inputPanel, "Add Student", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, null, null);

            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                break;
            }

            String name = nameField.getText().trim();
            String id = idField.getText().trim();
            String email = emailField.getText().trim();

            if (name.isEmpty() || id.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields must be filled out.");
            } else {
                Student student = new Student(name, id, email);
                sms.addStudent(student);
                updateDataDisplays();
                break;
            }
        }
    }

    private void removeStudent() {
        while (true) {
            JPanel inputPanel = new JPanel(new GridLayout(1, 2));
            JTextField studentIdField = new JTextField();

            inputPanel.add(new JLabel("Student ID:"));
            inputPanel.add(studentIdField);

            int result = JOptionPane.showOptionDialog(frame, inputPanel, "Remove Student", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, null, null);

            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                break;
            }

            String id = studentIdField.getText().trim();

            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields must be filled out.");
                continue;
            }

            Optional<Student> student = sms.getStudents().stream()
                    .filter(s -> s.getId().equals(id))
                    .findFirst();

            if (student.isPresent()) {
                sms.removeStudent(student.get());
                JOptionPane.showMessageDialog(frame, "Student removed.");
                break;
            } else {
                JOptionPane.showMessageDialog(frame, "Student not found.");
            }
        }

        updateDataDisplays();
    }

    private void filterStudents() {
        String filter = JOptionPane.showInputDialog("Type to filter:");
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(dataDisplayStudent.getModel());
        dataDisplayStudent.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter(filter));
    }

    private void unfilterStudents() {
        dataDisplayStudent.setRowSorter(null);
    }

    // Add, remove, filter grades
    private void addModule() {
        while (true) {
            JPanel inputPanel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(2, 2, 2, 2);

            JTextField nameField = new JTextField(15);
            JTextField idField = new JTextField(15);
            JTextField teacherField = new JTextField(15);

            JRadioButton firstYearButton = new JRadioButton("First Year");
            JRadioButton secondYearButton = new JRadioButton("Second Year");
            JRadioButton thirdYearButton = new JRadioButton("Third Year");
            ButtonGroup yearButtonGroup = new ButtonGroup();
            yearButtonGroup.add(firstYearButton);
            yearButtonGroup.add(secondYearButton);
            yearButtonGroup.add(thirdYearButton);

            JPanel yearPanel = new JPanel();
            yearPanel.add(firstYearButton);
            yearPanel.add(secondYearButton);
            yearPanel.add(thirdYearButton);

            JCheckBox sem1CheckBox = new JCheckBox("SEM1");
            JCheckBox sem2CheckBox = new JCheckBox("SEM2");

            JPanel semesterPanel = new JPanel();
            semesterPanel.add(sem1CheckBox);
            semesterPanel.add(sem2CheckBox);

            c.gridx = 0;
            c.gridy = 0;
            inputPanel.add(new JLabel("Name:"), c);
            c.gridx = 1;
            inputPanel.add(nameField, c);

            c.gridx = 0;
            c.gridy = 1;
            inputPanel.add(new JLabel("ID:"), c);
            c.gridx = 1;
            inputPanel.add(idField, c);

            c.gridx = 0;
            c.gridy = 2;
            inputPanel.add(new JLabel("Teacher:"), c);
            c.gridx = 1;
            inputPanel.add(teacherField, c);

            c.gridx = 0;
            c.gridy = 3;
            inputPanel.add(new JLabel("Year:"), c);
            c.gridx = 1;
            inputPanel.add(yearPanel, c);

            c.gridx = 0;
            c.gridy = 4;
            inputPanel.add(new JLabel("Semester:"), c);
            c.gridx = 1;
            inputPanel.add(semesterPanel, c);

            int result = JOptionPane.showOptionDialog(frame, inputPanel, "Add Module", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, null, null);

            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                break;
            }

            String name = nameField.getText().trim();
            String id = idField.getText().trim();
            String teacher = teacherField.getText().trim();

            if (name.isEmpty() || id.isEmpty() || teacher.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields must be filled out.");
                continue;
            }

            int year = firstYearButton.isSelected() ? 1 : secondYearButton.isSelected() ? 2 : 3;
            List<String> semesters = new ArrayList<>();
            if (sem1CheckBox.isSelected() && sem2CheckBox.isSelected()) {
                semesters.add("SEM" + (2 * year - 1) + " & SEM" + (2 * year));
            } else {
                if (sem1CheckBox.isSelected()) {
                    semesters.add("SEM" + (2 * year - 1));
                }
                if (sem2CheckBox.isSelected()) {
                    semesters.add("SEM" + (2 * year));
                }
            }

            for (String semester : semesters) {
                Module module = new Module(name, id, teacher, semester);
                sms.addModule(module);
            }

            updateDataDisplays();
            break;
        }
    }

    private void removeModule() {
        while (true) {
            JPanel inputPanel = new JPanel(new GridLayout(1, 2));
            JTextField moduleIdField = new JTextField();

            inputPanel.add(new JLabel("Module ID:"));
            inputPanel.add(moduleIdField);

            int result = JOptionPane.showOptionDialog(frame, inputPanel, "Remove Module", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, null, null);

            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                break;
            }

            String id = moduleIdField.getText().trim();

            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields must be filled out.");
                continue;
            }

            Optional<Module> module = sms.getModules().stream()
                    .filter(m -> m.getId().equals(id))
                    .findFirst();

            if (module.isPresent()) {
                sms.removeModule(module.get());
                JOptionPane.showMessageDialog(frame, "Module removed.");
                break;
            } else {
                JOptionPane.showMessageDialog(frame, "Module not found.");
            }
        }

        updateDataDisplays();
    }

    private void filterModules() {
        String filter = JOptionPane.showInputDialog("Type to filter:");
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(dataDisplayModule.getModel());
        dataDisplayModule.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter(filter));
    }

    private void unfilterModules() {
        dataDisplayModule.setRowSorter(null);
    }

    // Add, remove, filter grades
    private void addGrade() {
        while (true) {
            JPanel inputPanel = new JPanel(new GridLayout(3, 2));
            JTextField studentIdField = new JTextField();
            JTextField moduleIdField = new JTextField();
            JTextField gradeField = new JTextField();

            inputPanel.add(new JLabel("Student ID:"));
            inputPanel.add(studentIdField);
            inputPanel.add(new JLabel("Module ID:"));
            inputPanel.add(moduleIdField);
            inputPanel.add(new JLabel("Grade Value:"));
            inputPanel.add(gradeField);

            int result = JOptionPane.showOptionDialog(frame, inputPanel, "Add Grade", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, null, null);

            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                break;
            }

            String studentId = studentIdField.getText().trim();
            String moduleId = moduleIdField.getText().trim();
            String gradeStr = gradeField.getText().trim();

            if (studentId.isEmpty() || moduleId.isEmpty() || gradeStr.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields must be filled out.");
                continue;
            }

            Optional<Student> student = sms.getStudents().stream()
                    .filter(s -> s.getId().equals(studentId))
                    .findFirst();
            Optional<Module> module = sms.getModules().stream()
                    .filter(m -> m.getId().equals(moduleId))
                    .findFirst();

            if (student.isPresent() && module.isPresent()) {
                double gradeValue = Double.parseDouble(gradeStr);
                sms.addGrade(student.get(), module.get(), gradeValue);
                JOptionPane.showMessageDialog(frame, "Grade added.");
                break;
            } else {
                JOptionPane.showMessageDialog(frame, "Student or module not found.");
            }
        }

        updateDataDisplays();
    }

    private void removeGrade() {
        while (true) {
            JPanel inputPanel = new JPanel(new GridLayout(2, 2));
            JTextField studentIdField = new JTextField();
            JTextField moduleIdField = new JTextField();

            inputPanel.add(new JLabel("Student ID:"));
            inputPanel.add(studentIdField);
            inputPanel.add(new JLabel("Module ID:"));
            inputPanel.add(moduleIdField);

            int result = JOptionPane.showOptionDialog(frame, inputPanel, "Remove Grade", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, null, null);

            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                break;
            }

            String studentId = studentIdField.getText().trim();
            String moduleId = moduleIdField.getText().trim();

            if (studentId.isEmpty() || moduleId.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields must be filled out.");
                continue;
            }

            Optional<Student> student = sms.getStudents().stream()
                    .filter(s -> s.getId().equals(studentId))
                    .findFirst();
            Optional<Module> module = sms.getModules().stream()
                    .filter(m -> m.getId().equals(moduleId))
                    .findFirst();

            if (student.isPresent() && module.isPresent()) {
                Optional<Grade> grade = sms.findGrade(student.get(), module.get());
                if (grade.isPresent()) {
                    sms.removeGrade(grade.get());
                    JOptionPane.showMessageDialog(frame, "Grade removed.");
                    break;
                } else {
                    JOptionPane.showMessageDialog(frame, "Grade not found.");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Student or module not found.");
            }
        }

        updateDataDisplays();
    }

    private void filterGrades() {
        String filter = JOptionPane.showInputDialog("Type to filter:");
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(dataDisplayGrade.getModel());
        dataDisplayGrade.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter(filter));
    }

    private void unfilterGrades() {
        dataDisplayGrade.setRowSorter(null);
    }

    // Save to file
    private void saveToFile() {
        String fileName = "Java/StudentManagementSystem_CA3/database.csv";
        sms.saveToFile(fileName);
        JOptionPane.showMessageDialog(frame, "Data saved successfully.");
    }

    // Ennroll/unenroll student in module
    private void enrollStudent(String studentId, String moduleId) {
        Optional<Student> student = sms.getStudentById(studentId);
        Optional<Module> module = sms.getModuleById(moduleId);

        if (student.isPresent() && module.isPresent()) {
            sms.enrollStudentInModule(student.get(), module.get());
            JOptionPane.showMessageDialog(frame, "Student enrolled in module " + module.get().getName());
        } else {
            JOptionPane.showMessageDialog(frame, "Student or module not found.");
        }
        updateDataDisplays();
    }

    private void unenrollStudent(String studentId, String moduleId) {
        Optional<Student> student = sms.getStudentById(studentId);
        Optional<Module> module = sms.getModuleById(moduleId);

        if (student.isPresent() && module.isPresent()) {
            sms.unenrollStudentFromModule(student.get(), module.get());
            JOptionPane.showMessageDialog(frame, "Student unenrolled from module " + module.get().getName());
        } else {
            JOptionPane.showMessageDialog(frame, "Student or module not found.");
        }
        updateDataDisplays();
    }

    private void updateEnrollment(JScrollPane scrollPane, String studentId) {
        JTable table = (JTable) scrollPane.getViewport().getView();
        for (int i = 0; i < table.getRowCount(); i++) {
            boolean enrolled = (boolean) table.getValueAt(i, 2);
            String moduleId = (String) table.getValueAt(i, 0);
            if (enrolled) {
                enrollStudent(studentId, moduleId);
            } else {
                unenrollStudent(studentId, moduleId);
            }
        }
    }

    // Update data displays after changes
    private void updateDataDisplays() {
        updateDataDisplayDashboard();
        updateDataDisplayStudent();
        updateDataDisplayModule();
        updateDataDisplayGrade();
    }

    private void updateDataDisplayDashboard() {
        dataDisplayDashboard.setModel(createDashboardTableModel());
    }

    private void updateDataDisplayStudent() {
        dataDisplayStudent.setModel(createStudentTableModel());
    }

    private void updateDataDisplayModule() {
        dataDisplayModule.setModel(createModuleTableModel());
    }

    private void updateDataDisplayGrade() {
        dataDisplayGrade.setModel(createGradeTableModel());
    }

}