package com.timetable;

/* ============================================================
FILE: Classroom.java
PURPOSE: Model class representing a classroom / lab room.
============================================================ */
public class Classroom {
    private String roomId;
    private String roomName;
    private int capacity;

    public Classroom(String roomId, String roomName, int capacity) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.capacity = capacity;
    }

    public String getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public int getCapacity() { return capacity; }

    @Override
    public String toString() {
        return roomId + " - " + roomName + " (Capacity: " + capacity + ")";
    }
}
package com.timetable;

/* ============================================================
FILE: ClassroomManager.java
PURPOSE: Handles all CRUD operations for Classroom objects.
============================================================ */
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class ClassroomManager {
    private Map<String, Classroom> roomMap = new HashMap<>();

    public void addClassroom(Classroom c) throws IllegalArgumentException {
        if (roomMap.containsKey(c.getRoomId())) {
            throw new IllegalArgumentException("Room ID already exists: " +
                c.getRoomId());
        }
        roomMap.put(c.getRoomId(), c);
    }

    public void removeClassroom(String id) {
        roomMap.remove(id);
    }

    public Classroom getClassroom(String id) {
        return roomMap.get(id);
    }

    public Collection<Classroom> getAllClassrooms() {
        return roomMap.values();
    }

    public boolean isEmpty() {
        return roomMap.isEmpty();
    }
}
package com.timetable;

/* ============================================================
FILE: ConflictChecker.java
PURPOSE: Verifies that a proposed TimetableEntry does not
         clash with entries already placed in the timetable.
         A conflict occurs when, for the same TimeSlot, the
         same Teacher or the same Classroom is reused.
============================================================ */
import java.util.List;

public class ConflictChecker {
    public boolean hasConflict(List<TimetableEntry> existingEntries,
                                Teacher teacher, Classroom room, TimeSlot slot) {
        for (TimetableEntry entry : existingEntries) {
            boolean sameSlot = entry.getTimeSlot().getDay().equals(slot.getDay())
                && entry.getTimeSlot().getPeriod().equals(slot.getPeriod());
            if (sameSlot) {
                boolean teacherClash =
                    entry.getTeacher().getTeacherId().equals(teacher.getTeacherId());
                boolean roomClash =
                    entry.getClassroom().getRoomId().equals(room.getRoomId());
                if (teacherClash || roomClash) {
                    return true;
                }
            }
        }
        return false;
    }
}
package com.timetable;

/* ============================================================
FILE: ConflictException.java
PURPOSE: Custom checked exception thrown whenever the
         TimetableGenerator detects a scheduling conflict.
============================================================ */
public class ConflictException extends Exception {
    public ConflictException(String message) {
        super(message);
    }
}
package com.timetable;

/* ============================================================
FILE: Dashboard.java
PURPOSE: Main GUI window with buttons for every module:
         teacher/subject/classroom/time-slot entry,
         timetable generation, display, save and exit.
============================================================ */
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Dashboard extends JFrame {
    private TeacherManager teacherManager = new TeacherManager();
    private SubjectManager subjectManager = new SubjectManager();
    private ClassroomManager classroomManager = new ClassroomManager();
    private List<TimeSlot> timeSlots = new ArrayList<>();
    private TimetableGenerator generator = new TimetableGenerator();
    private ReportManager reportManager = new ReportManager();

    public Dashboard() {
        setTitle("AI Timetable Generator - Dashboard");
        setSize(420, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(9, 1, 5, 5));
        JButton addTeacherBtn = new JButton("Add Teacher");
        JButton addSubjectBtn = new JButton("Add Subject");
        JButton addRoomBtn = new JButton("Add Classroom");
        JButton addSlotBtn = new JButton("Add Time Slot");
        JButton generateBtn = new JButton("Generate Timetable");
        JButton viewBtn = new JButton("View Timetable");
        JButton saveFileBtn = new JButton("Save Timetable (File)");
        JButton saveDbBtn = new JButton("Save Timetable (Database)");
        JButton exitBtn = new JButton("Exit");

        addTeacherBtn.addActionListener(e -> addTeacher());
        addSubjectBtn.addActionListener(e -> addSubject());
        addRoomBtn.addActionListener(e -> addClassroom());
        addSlotBtn.addActionListener(e -> addTimeSlot());
        generateBtn.addActionListener(e -> generateTimetable());
        viewBtn.addActionListener(e -> viewTimetable());
        saveFileBtn.addActionListener(e ->
            reportManager.saveToFile(generator.getTimetable(), "timetable_output.txt"));
        saveDbBtn.addActionListener(e ->
            reportManager.saveToDatabase(generator.getTimetable()));
        exitBtn.addActionListener(e -> System.exit(0));

        panel.add(addTeacherBtn);
        panel.add(addSubjectBtn);
        panel.add(addRoomBtn);
        panel.add(addSlotBtn);
        panel.add(generateBtn);
        panel.add(viewBtn);
        panel.add(saveFileBtn);
        panel.add(saveDbBtn);
        panel.add(exitBtn);

        add(panel);
    }

    private void addTeacher() {
        String id = JOptionPane.showInputDialog(this, "Teacher ID:");
        String name = JOptionPane.showInputDialog(this, "Teacher Name:");
        String spec = JOptionPane.showInputDialog(this, "Specialization:");
        try {
            teacherManager.addTeacher(new Teacher(id, name, spec));
            JOptionPane.showMessageDialog(this, "Teacher added successfully.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addSubject() {
        String code = JOptionPane.showInputDialog(this, "Subject Code:");
        String name = JOptionPane.showInputDialog(this, "Subject Name:");
        String tid = JOptionPane.showInputDialog(this, "Teacher ID for this subject:");
        try {
            subjectManager.addSubject(new Subject(code, name, tid));
            JOptionPane.showMessageDialog(this, "Subject added successfully.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addClassroom() {
        String id = JOptionPane.showInputDialog(this, "Room ID:");
        String name = JOptionPane.showInputDialog(this, "Room Name:");
        int capacity = 0;
        try {
            capacity = Integer.parseInt(JOptionPane.showInputDialog(this, "Capacity:"));
            classroomManager.addClassroom(new Classroom(id, name, capacity));
            JOptionPane.showMessageDialog(this, "Classroom added successfully.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Capacity must be a number.",
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addTimeSlot() {
        String day = JOptionPane.showInputDialog(this, "Day (e.g. Monday):");
        String period = JOptionPane.showInputDialog(this, "Period (e.g. 09:00-10:00):");
        timeSlots.add(new TimeSlot(day, period));
        JOptionPane.showMessageDialog(this, "Time slot added successfully.");
    }

    private void generateTimetable() {
        if (teacherManager.isEmpty() || subjectManager.isEmpty()
                || classroomManager.isEmpty() || timeSlots.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please add teachers, subjects, classrooms and time slots first.",
                "Incomplete Data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            generator.generateTimetable(subjectManager.getAllSubjects(), teacherManager,
                classroomManager.getAllClassrooms(), timeSlots);
            JOptionPane.showMessageDialog(this,
                "Conflict-free timetable generated successfully!");
        } catch (ConflictException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Conflict Detected",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewTimetable() {
        List<TimetableEntry> table = generator.getTimetable();
        if (table.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No timetable generated yet.");
            return;
        }
        String[] columns = {"Day", "Period", "Subject", "Teacher", "Room"};
        String[][] data = new String[table.size()][5];
        for (int i = 0; i < table.size(); i++) {
            TimetableEntry entry = table.get(i);
            data[i][0] = entry.getTimeSlot().getDay();
            data[i][1] = entry.getTimeSlot().getPeriod();
            data[i][2] = entry.getSubject().getSubjectName();
            data[i][3] = entry.getTeacher().getName();
            data[i][4] = entry.getClassroom().getRoomName();
        }
        JTable jTable = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(jTable);
        JOptionPane.showMessageDialog(this, scrollPane, "Generated Timetable",
            JOptionPane.PLAIN_MESSAGE);
        reportManager.printTimetable(table);
    }
}
package com.timetable;

/* ============================================================
FILE: DatabaseConnection.java
PURPOSE: Provides a single shared JDBC connection to the
         MySQL database "timetable_db" using the Singleton
         design pattern.
============================================================ */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection connection = null;
    private static final String URL =
        "jdbc:mysql://localhost:3306/timetable_db";
    private static final String USER = "root";
    private static final String PASS = "root123";

    // Private constructor prevents direct instantiation
    private DatabaseConnection() { }

    // Returns the single shared connection instance
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("Database connected successfully.");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found: " +
                e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database connection failed: " +
                e.getMessage());
        }
        return connection;
    }

    // Closes the connection gracefully
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("Error closing connection: " +
                e.getMessage());
        }
    }
}
package com.timetable;

/* ============================================================
FILE: Login.java
PURPOSE: Swing GUI login screen that authenticates the user
         before granting access to the Dashboard.
============================================================ */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Login extends JFrame {
    private JTextField userField;
    private JPasswordField passField;

    public Login() {
        setTitle("AI Timetable Generator - Login");
        setSize(350, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Username:"));
        userField = new JTextField();
        panel.add(userField);
        panel.add(new JLabel("Password:"));
        passField = new JPasswordField();
        panel.add(passField);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(this::handleLogin);
        panel.add(new JLabel());
        panel.add(loginBtn);

        add(panel);
    }

    // Validates the entered credentials
    private void handleLogin(ActionEvent e) {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());
        if (user.equals("admin") && pass.equals("admin123")) {
            JOptionPane.showMessageDialog(this, "Login successful!");
            new Dashboard().setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.",
                "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
package com.timetable;

/* ============================================================
FILE: Main.java
PURPOSE: Entry point of the application. Launches the Login
         screen on the Swing Event Dispatch Thread.
============================================================ */
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DatabaseConnection.getConnection(); // establish DB connection at startup
            new Login().setVisible(true);
        });
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::closeConnection));
    }
}
package com.timetable;

/* ============================================================
FILE: ReportManager.java
PURPOSE: Displays the generated timetable on the console /
         GUI table and saves it to a text file and to the
         MySQL database using JDBC.
============================================================ */
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ReportManager {
    public void printTimetable(List<TimetableEntry> timetable) {
        System.out.println("\n===== GENERATED TIMETABLE =====");
        for (TimetableEntry entry : timetable) {
            System.out.println(entry.toString());
        }
        System.out.println("================================\n");
    }

    // Saves the timetable to a local text file
    public void saveToFile(List<TimetableEntry> timetable, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (TimetableEntry entry : timetable) {
                writer.write(entry.toString());
                writer.newLine();
            }
            System.out.println("Timetable saved to file: " + fileName);
        } catch (IOException e) {
            System.out.println("Error saving timetable to file: " +
                e.getMessage());
        }
    }

    // Saves the timetable into the MySQL database
    public void saveToDatabase(List<TimetableEntry> timetable) {
        String sql = "INSERT INTO timetable (day, period, subject, teacher, room) "
            + "VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps =
                DatabaseConnection.getConnection().prepareStatement(sql);
            for (TimetableEntry entry : timetable) {
                ps.setString(1, entry.getTimeSlot().getDay());
                ps.setString(2, entry.getTimeSlot().getPeriod());
                ps.setString(3, entry.getSubject().getSubjectName());
                ps.setString(4, entry.getTeacher().getName());
                ps.setString(5, entry.getClassroom().getRoomName());
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("Timetable saved to database successfully.");
        } catch (SQLException e) {
            System.out.println("Error saving timetable to database: " +
                e.getMessage());
        }
    }
}
package com.timetable;

/* ============================================================
FILE: Subject.java
PURPOSE: Model class representing a subject and the teacher
         responsible for teaching it.
============================================================ */
public class Subject {
    private String subjectCode;
    private String subjectName;
    private String teacherId;

    public Subject(String subjectCode, String subjectName, String teacherId) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.teacherId = teacherId;
    }

    public String getSubjectCode() { return subjectCode; }
    public String getSubjectName() { return subjectName; }
    public String getTeacherId() { return teacherId; }

    @Override
    public String toString() {
        return subjectCode + " - " + subjectName;
    }
}
package com.timetable;

/* ============================================================
FILE: SubjectManager.java
PURPOSE: Handles all CRUD operations for Subject objects.
============================================================ */
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class SubjectManager {
    private Map<String, Subject> subjectMap = new HashMap<>();

    public void addSubject(Subject s) throws IllegalArgumentException {
        if (subjectMap.containsKey(s.getSubjectCode())) {
            throw new IllegalArgumentException("Subject code already exists: "
                + s.getSubjectCode());
        }
        subjectMap.put(s.getSubjectCode(), s);
    }

    public void removeSubject(String code) {
        subjectMap.remove(code);
    }

    public Subject getSubject(String code) {
        return subjectMap.get(code);
    }

    public Collection<Subject> getAllSubjects() {
        return subjectMap.values();
    }

    public boolean isEmpty() {
        return subjectMap.isEmpty();
    }
}
package com.timetable;

/* ============================================================
FILE: Teacher.java
PURPOSE: Model (POJO) class representing a teacher.
============================================================ */
public class Teacher {
    private String teacherId;
    private String name;
    private String specialization;

    public Teacher(String teacherId, String name, String specialization) {
        this.teacherId = teacherId;
        this.name = name;
        this.specialization = specialization;
    }

    public String getTeacherId() { return teacherId; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }

    @Override
    public String toString() {
        return teacherId + " - " + name + " (" + specialization + ")";
    }
}
package com.timetable;

/* ============================================================
FILE: TeacherManager.java
PURPOSE: Handles all CRUD operations for Teacher objects
         using a HashMap as an in-memory Collection store.
============================================================ */
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class TeacherManager {
    private Map<String, Teacher> teacherMap = new HashMap<>();

    // Adds a new teacher; throws exception on duplicate id
    public void addTeacher(Teacher t) throws IllegalArgumentException {
        if (teacherMap.containsKey(t.getTeacherId())) {
            throw new IllegalArgumentException("Teacher ID already exists: "
                + t.getTeacherId());
        }
        teacherMap.put(t.getTeacherId(), t);
    }

    public void removeTeacher(String id) {
        teacherMap.remove(id);
    }

    public Teacher getTeacher(String id) {
        return teacherMap.get(id);
    }

    public Collection<Teacher> getAllTeachers() {
        return teacherMap.values();
    }

    public boolean isEmpty() {
        return teacherMap.isEmpty();
    }
}
package com.timetable;

/* ============================================================
FILE: TimeSlot.java
PURPOSE: Model class representing a single teaching period.
============================================================ */
public class TimeSlot {
    private String day;
    private String period; // e.g. "09:00-10:00"

    public TimeSlot(String day, String period) {
        this.day = day;
        this.period = period;
    }

    public String getDay() { return day; }
    public String getPeriod() { return period; }

    @Override
    public String toString() {
        return day + " [" + period + "]";
    }
}
package com.timetable;

/* ============================================================
FILE: TimetableEntry.java
PURPOSE: Represents one allocated cell of the final
         timetable (subject + teacher + room + slot).
============================================================ */
public class TimetableEntry {
    private Subject subject;
    private Teacher teacher;
    private Classroom classroom;
    private TimeSlot timeSlot;

    public TimetableEntry(Subject subject, Teacher teacher,
                           Classroom classroom, TimeSlot timeSlot) {
        this.subject = subject;
        this.teacher = teacher;
        this.classroom = classroom;
        this.timeSlot = timeSlot;
    }

    public Subject getSubject() { return subject; }
    public Teacher getTeacher() { return teacher; }
    public Classroom getClassroom() { return classroom; }
    public TimeSlot getTimeSlot() { return timeSlot; }

    @Override
    public String toString() {
        return timeSlot + " | " + subject.getSubjectName() + " | "
            + teacher.getName() + " | " + classroom.getRoomName();
    }
}
package com.timetable;

/* ============================================================
FILE: TimetableGenerator.java
PURPOSE: Core scheduling engine. Allocates every subject to
         a teacher, classroom and time slot such that no
         conflicts occur, using a greedy back-tracking style
         search over the available time slots.
============================================================ */
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TimetableGenerator {
    private ConflictChecker conflictChecker = new ConflictChecker();
    private List<TimetableEntry> timetable = new ArrayList<>();

    public List<TimetableEntry> generateTimetable(Collection<Subject> subjects,
                                                   TeacherManager teacherManager,
                                                   Collection<Classroom> classrooms,
                                                   List<TimeSlot> timeSlots)
            throws ConflictException {
        timetable.clear();
        for (Subject subject : subjects) {
            Teacher teacher = teacherManager.getTeacher(subject.getTeacherId());
            if (teacher == null) {
                throw new ConflictException("No teacher assigned for subject: "
                    + subject.getSubjectName());
            }
            boolean placed = false;
            // Try every combination of classroom and time slot
            outer:
            for (Classroom room : classrooms) {
                for (TimeSlot slot : timeSlots) {
                    if (!conflictChecker.hasConflict(timetable, teacher, room, slot)) {
                        timetable.add(new TimetableEntry(subject, teacher, room, slot));
                        placed = true;
                        break outer;
                    }
                }
            }
            if (!placed) {
                throw new ConflictException(
                    "Unable to schedule subject " + subject.getSubjectName()
                    + " - no free slot/room/teacher combination available.");
            }
        }
        return timetable;
    }

    public List<TimetableEntry> getTimetable() {
        return timetable;
    }
}
