/* 
 * Student Management System App
 * Name: Guilherme Duarte da Silva
 * ID: 25662
 * 
 * Description: This class contains the classes Student and Module, which are used
 *              to create the objects that will be used in the application. It also contains
 *              the class StudentManagementSystemGUI, which is responsible for the GUI of the
 *              application. It also contains the class StudentManagementSystemApp, which is
 *              responsible for the execution of the application.
 *
*/

import java.io.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.table.DefaultTableCellRenderer;

// Color the table rows based on the status of the module
class GradeColorRenderer extends DefaultTableCellRenderer {
    private int statusColumn;

    public GradeColorRenderer(int statusColumn) {
        this.statusColumn = statusColumn;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (isSelected) {
            cellComponent.setBackground(table.getSelectionBackground());
            cellComponent.setForeground(table.getSelectionForeground());
        } else {
            cellComponent.setForeground(table.getForeground());
            String status = (String) table.getValueAt(row, statusColumn);
            if (status != null) {
                switch (status) {
                    case "Pass":
                        cellComponent.setBackground(new Color(204, 255, 204)); // Light green
                        break;
                    case "Fail":
                        cellComponent.setBackground(new Color(255, 255, 153)); // Light yellow
                        break;
                    case "To Repeat":
                        cellComponent.setBackground(new Color(255, 204, 204)); // Light red
                        break;
                    case "Completed":
                        cellComponent.setBackground(new Color(204, 255, 255)); // Light blue
                        break;
                    case "In Progress":
                        cellComponent.setBackground(Color.WHITE); // White
                        break;
                    default:
                        cellComponent.setBackground(table.getBackground());
                }
            } else {
                cellComponent.setBackground(table.getBackground());
            }
        }

        return cellComponent;
    }
}

// Color the rows of the table
class CustomTableCellRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1L;
    private final Color rowColor;

    public CustomTableCellRenderer(Color rowColor) {
        this.rowColor = rowColor;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (!isSelected) {
            c.setBackground(row % 2 == 0 ? rowColor : table.getBackground());
        }
        return c;
    }
}

// Student class
class Student implements Serializable {
    private String name;
    private String id;
    private String email;
    private Set<Module> enrolledModules;

    public Student(String name, String id, String email) {
        this.name = name;
        this.id = id;
        this.email = email;
        this.enrolledModules = new HashSet<>();
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Module> getEnrolledModules() {
        return enrolledModules;
    }

    public void setEnrolledModules(Set<Module> enrolledModules) {
        this.enrolledModules = enrolledModules;
    }

    public Object getValue() {
        return null;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", ID: " + id + ", Email: " + email;
    }

    public Set<Module> getEnrolledStudents() {
        return null;
    }

    public List<Module> getCompletedModules() {
        return enrolledModules.stream()
                .filter(Module::isCompleted)
                .collect(Collectors.toList());
    }

    public boolean hasCompletedModule(String moduleId) {
        return getCompletedModules().stream()
                .anyMatch(module -> module.getId().equals(moduleId));
    }

    public boolean hasPassedModule(String moduleId) {
        return enrolledModules.stream()
                .anyMatch(module -> module.getId().equalsIgnoreCase(moduleId) && module.isCompleted() && module.isPassed());
    }
    
    public List<Module> getCompletedAndPassedModules() {
        return enrolledModules.stream()
                .filter(module -> module.isCompleted() && module.isPassed())
                .collect(Collectors.toList());
    }
    
}

// Module class
class Module implements Serializable {
    private String name;
    private String id;
    private String teacher;
    private String semester;
    private boolean completed;
    private boolean passed;
    private Set<Student> enrolledStudents;

    public Module(String name, String id, String teacher, String semester) {
        this.name = name;
        this.id = id;
        this.teacher = teacher;
        this.semester = semester;
        this.enrolledStudents = new HashSet<>();
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<Student> getEnrolledStudents() {
        return enrolledStudents;
    }

    public void setEnrolledStudents(Set<Student> enrolledStudents) {
        this.enrolledStudents = enrolledStudents;
    }

    public Object getValue() {
        return null;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    @Override
    public String toString() {
        return "Module: " + name + ", ID: " + id + ", Teacher: " + teacher + ", Semesters: " + semester;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

}

// Grade class
class Grade implements Serializable {
    private Student student;
    private Module module;
    private double grade;

    public Grade(Student student, Module module, double grade) {
        this.student = student;
        this.module = module;
        this.grade = grade;
    }

    // Getters and setters
    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public double getGrade() {
        return grade;
    }

    public void setGrade(double grade) {
        this.grade = grade;
    }

    public Object getValue() {
        return null;
    }

    @Override
    public String toString() {
        return "Student ID: " + student.getId() + ", Module ID: " + module.getId() + ", Grade: " + grade;
    }
}

// Student management system class
class StudentManagementSystem {
    private Set<Student> students;
    private Set<Module> modules;
    private Set<Grade> grades;

    // Getters for students, modules and grades
    public Set<Student> getStudents() {
        return students;
    }

    public Set<Module> getModules() {
        return modules;
    }

    public Set<Grade> getGrades() {
        return grades;
    }

    // Get student or module by ID
    public Optional<Student> getStudentById(String studentId) {
        return students.stream().filter(s -> s.getId().equals(studentId)).findFirst();
    }

    public Optional<Module> getModuleById(String moduleId) {
        return modules.stream().filter(m -> m.getId().equals(moduleId)).findFirst();
    }

    // HashSets for students, modules and grades
    public StudentManagementSystem() {
        students = new HashSet<>();
        modules = new HashSet<>();
        grades = new HashSet<>();
    }

    // Add, remove, update student
    public void addStudent(Student student) {
        students.add(student);
    }

    public void removeStudent(Student student) {
        students.remove(student);
        grades.removeIf(grade -> grade.getStudent().equals(student));
    }

    public void updateStudent(Student student, String name, String id, String email) {
        student.setName(name);
        student.setId(id);
        student.setEmail(email);
    }

    // Add, remove, update module
    public void addModule(Module module) {
        modules.add(module);
    }

    public void removeModule(Module module) {
        modules.remove(module);
        grades.removeIf(grade -> grade.getModule().equals(module));
    }

    public void updateModule(Module module, String name, String id) {
        module.setName(name);
        module.setId(id);
    }

    // Enroll, unenroll student from module
    public void enrollStudentInModule(Student student, Module module) {
        student.getEnrolledModules().add(module);
        module.getEnrolledStudents().add(student);
    }

    public void unenrollStudentFromModule(Student student, Module module) {
        student.getEnrolledModules().remove(module);
        module.getEnrolledStudents().remove(student);
    }

    // Add, remove, update grade
    public void addGrade(Student student, Module module, double gradeValue) {
        Grade grade = new Grade(student, module, gradeValue);
        grades.add(grade);
    }

    public void removeGrade(Grade grade) {
        grades.remove(grade);
    }

    public void updateGrade(Grade grade, double newGradeValue) {
        grade.setGrade(newGradeValue);
    }

    public Optional<Grade> findGrade(Student student, Module module) {
        return grades.stream()
                .filter(grade -> grade.getStudent().equals(student) && grade.getModule().equals(module))
                .findFirst();
    }

    // Save/Load data to file
    public void saveToFile(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("Students\n");
            for (Student student : students) {
                writer.write(student.getId() + ", " + student.getName() + ", " + student.getEmail() + "\n");
            }

            writer.write("Modules\n");
            for (Module module : modules) {
                writer.write(module.getId() + ", " + module.getName() + ", " + module.getTeacher() + ", "
                        + module.getSemester() + "\n");
            }

            writer.write("Grades\n");
            for (Grade grade : grades) {
                writer.write(
                        grade.getStudent().getId() + ", " + grade.getModule().getId() + ", " + grade.getGrade() + "\n");
            }

            writer.write("Enrollments\n");
            for (Student student : students) {
                for (Module module : student.getEnrolledModules()) {
                    writer.write(student.getId() + ", " + module.getId() + "\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            String section = "";
            int lineNumber = 0;
    
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.equals("Students") || line.equals("Modules") || line.equals("Grades")
                        || line.equals("Enrollments")) {
                    section = line;
                    continue;
                }
    
                String[] parts = line.split(", ");
                try {
                    switch (section) {
                        case "Students":
                            addStudent(new Student(parts[1], parts[0], parts[2]));
                            break;
                        case "Modules":
                            addModule(new Module(parts[1], parts[0], parts[2], parts[3]));
                            break;
                        case "Enrollments":
                            Optional<Student> student = students.stream().filter(s -> s.getId().equals(parts[0]))
                                    .findFirst();
                            Optional<Module> module = modules.stream().filter(m -> m.getId().equals(parts[1])).findFirst();
                            if (student.isPresent() && module.isPresent()) {
                                enrollStudentInModule(student.get(), module.get());
                            }
                            break;
                        case "Grades":
                            student = students.stream().filter(s -> s.getId().equals(parts[0])).findFirst();
                            module = modules.stream().filter(m -> m.getId().equals(parts[1])).findFirst();
                            if (student.isPresent() && module.isPresent()) {
                                addGrade(student.get(), module.get(), Double.parseDouble(parts[2]));
                            }
                            break;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println("Error processing line " + lineNumber + ": " + line);
                    throw e;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
