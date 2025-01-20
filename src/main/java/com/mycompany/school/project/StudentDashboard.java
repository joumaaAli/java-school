package com.mycompany.school.project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.ArrayList;

import model.subject.*;
import model.user.Teacher;
import model.user.Student;
import model.user.User;
import utils.CSVUtil;
import utils.IDGenerator;

public class StudentDashboard extends JFrame {
    private Student student;

    // Data lists
    private List<Subject> subjects;
    private List<Chapter> chapters;
    private List<Group> groups;
    private List<Material> materials;
    private List<Test> tests;
    private List<Session> sessions;
    private List<Teacher> teachers; // Added to store teacher data

    // GUI Components
    private JPanel viewMaterialsPanel;
    private JTable materialsTable;
    private DefaultTableModel materialsTableModel;
    private JButton refreshMaterialsButton;

    private JPanel viewTestsPanel;
    private JTable testsTable;
    private DefaultTableModel testsTableModel;
    private JButton refreshTestsButton;

    private JPanel viewAllGroupsPanel;
    private JTable allGroupsTable;
    private DefaultTableModel allGroupsTableModel;
    private JButton refreshAllGroupsButton;
    private JButton enrollGroupButton;
    private JButton leaveGroupButton;

    private JPanel myGroupsPanel;
    private JTable myGroupsTable;
    private DefaultTableModel myGroupsTableModel;
    private JButton refreshMyGroupsButton;
    private JButton leaveSelectedGroupButton;

    private JPanel payDuePanel;
    private JLabel currentBalanceLabel;
    private JTextField paymentAmountField;
    private JButton payButton;

    public StudentDashboard(Student student) {
        this.student = student;
        loadData();
        initComponents();
    }

    /**
     * Loads all necessary data from CSV files.
     */
    private void loadData() {
        // Load subjects
        subjects = CSVUtil.readFromCSV("subjects.csv", fields -> {
            if (fields.length < 4) {
                System.err.println("Malformed subject entry: " + Arrays.toString(fields));
                return null;
            }
            try {
                return new Subject(
                        fields[0],
                        fields[1],
                        fields[2],
                        Double.parseDouble(fields[3]));
            } catch (NumberFormatException e) {
                System.err.println("Invalid price in subject entry: " + Arrays.toString(fields));
                return null;
            }
        }).stream().filter(s -> s != null).collect(Collectors.toList());

        // Load chapters
        chapters = CSVUtil.readFromCSV("chapters.csv", fields -> {
            if (fields.length < 4) {
                System.err.println("Malformed chapter entry: " + Arrays.toString(fields));
                return null;
            }
            return new Chapter(
                    fields[0],
                    fields[1],
                    fields[2],
                    fields[3]);
        }).stream().filter(c -> c != null).collect(Collectors.toList());

        // Load groups with multiple teacherIds
        groups = CSVUtil.readFromCSV("groups.csv", fields -> {
            if (fields.length < 5) {
                System.err.println("Malformed group entry: " + Arrays.toString(fields));
                return null;
            }
            // Parse teacherIds separated by semicolons
            String[] teacherIdsArray = fields[3].isEmpty() ? new String[0] : fields[3].split(";");
            List<String> teacherIds = Arrays.asList(teacherIdsArray);
            Group group = new Group(
                    fields[0],
                    fields[1],
                    fields[2],
                    teacherIds);
            if (!fields[4].isEmpty()) {
                String[] studentIds = fields[4].split(";");
                for (String sid : studentIds) {
                    group.addStudent(sid);
                }
            }
            return group;
        }).stream().filter(g -> g != null).collect(Collectors.toList());

        // Load materials
        materials = CSVUtil.readFromCSV("materials.csv", fields -> {
            if (fields.length < 5) {
                System.err.println("Malformed material entry: " + Arrays.toString(fields));
                return null;
            }
            return new Material(
                    fields[0],
                    fields[1],
                    fields[2],
                    fields[3],
                    fields[4]);
        }).stream().filter(m -> m != null).collect(Collectors.toList());

        // Load tests
        tests = CSVUtil.readFromCSV("tests.csv", fields -> {
            if (fields.length < 6) {
                System.err.println("Malformed test entry: " + Arrays.toString(fields));
                return null;
            }
            Test test = new Test(
                    fields[0],
                    fields[1],
                    fields[2],
                    fields[3],
                    Integer.parseInt(fields[4]));
            if (!fields[5].isEmpty()) {
                String[] questionStrs = fields[5].split(";");
                for (String qStr : questionStrs) {
                    String[] qFields = qStr.split(",");
                    if (qFields.length < 5) {
                        System.err.println("Malformed question entry: " + Arrays.toString(qFields));
                        continue;
                    }
                    Question q = new Question(
                            qFields[0],
                            qFields[1],
                            qFields[2],
                            qFields[3].split("\\|"),
                            Integer.parseInt(qFields[4]));
                    test.addQuestion(q);
                }
            }
            return test;
        }).stream().filter(t -> t != null).collect(Collectors.toList());

        // Load sessions
        sessions = CSVUtil.readFromCSV("sessions.csv", fields -> {
            if (fields.length < 5) {
                System.err.println("Malformed session entry: " + Arrays.toString(fields));
                return null;
            }
            return new Session(
                    fields[0],
                    fields[1],
                    fields[2],
                    fields[3],
                    fields[4]);
        }).stream().filter(s -> s != null).collect(Collectors.toList());

        // Load teachers from users.csv
        loadTeachers();
    }

    /**
     * Loads teacher data from users.csv and populates the teachers list.
     */
    private void loadTeachers() {
        teachers = CSVUtil.readFromCSV("users.csv", fields -> {
            if (fields.length < 5) {
                System.err.println("Malformed user entry: " + Arrays.toString(fields));
                return null;
            }
            if (fields[3].equalsIgnoreCase("Teacher")) {
                return new Teacher(fields[0], fields[1], fields[2]);
            } else {
                return null; // Only load teachers
            }
        }).stream().filter(u -> u != null).map(u -> (Teacher) u).collect(Collectors.toList());
    }

    /**
     * Initializes all GUI components and layouts.
     */
    private void initComponents() {
        setTitle("Student Dashboard - " + student.getName());
        setLayout(new BorderLayout());

        // Create Tabbed Pane to organize sections
        JTabbedPane tabbedPane = new JTabbedPane();

        // Initialize Tabs
        initViewMaterialsTab();
        initViewTestsTab();
        initViewAllGroupsTab();
        initMyGroupsTab();
        initPayDuePanel();

        // Add Tabs to TabbedPane
        tabbedPane.addTab("View Materials", viewMaterialsPanel);
        tabbedPane.addTab("View Tests", viewTestsPanel);
        tabbedPane.addTab("View All Groups", viewAllGroupsPanel);
        tabbedPane.addTab("My Groups", myGroupsPanel);
        tabbedPane.addTab("Pay Due Payment", payDuePanel);

        // Add Tabs to Frame
        add(tabbedPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }

    /**
     * Initializes the "View Materials" tab where students can view materials.
     */
    private void initViewMaterialsTab() {
        viewMaterialsPanel = new JPanel(new BorderLayout());

        // Table Model for Materials
        materialsTableModel = new DefaultTableModel();
        materialsTableModel.addColumn("Material ID");
        materialsTableModel.addColumn("Title");
        materialsTableModel.addColumn("Type");
        materialsTableModel.addColumn("Path/URL");
        materialsTableModel.addColumn("Chapter");

        // Populate Materials Table
        populateMaterialsTable();

        // Materials Table
        materialsTable = new JTable(materialsTableModel);
        JScrollPane scrollPane = new JScrollPane(materialsTable);
        viewMaterialsPanel.add(scrollPane, BorderLayout.CENTER);

        // Refresh Button
        refreshMaterialsButton = new JButton("Refresh");
        refreshMaterialsButton.addActionListener(e -> populateMaterialsTable());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshMaterialsButton);
        viewMaterialsPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Populates the "View Materials" table with materials related to the student's
     * groups.
     */
    private void populateMaterialsTable() {
        // Clear existing rows
        materialsTableModel.setRowCount(0);

        // Find groups the student is enrolled in
        List<Group> studentGroups = groups.stream()
                .filter(g -> g.getStudentIds().contains(student.getId()))
                .collect(Collectors.toList());

        // Find chapters associated with these groups
        List<String> chapterIds = studentGroups.stream()
                .map(Group::getChapterId)
                .distinct()
                .collect(Collectors.toList());

        // Find materials assigned to these chapters
        List<Material> relevantMaterials = materials.stream()
                .filter(m -> chapterIds.contains(m.getChapterId()))
                .collect(Collectors.toList());

        for (Material m : relevantMaterials) {
            String chapterName = getChapterNameById(m.getChapterId());
            materialsTableModel
                    .addRow(new Object[] { m.getId(), m.getTitle(), m.getType(), m.getPathOrContent(), chapterName });
        }
    }

    /**
     * Initializes the "View Tests" tab where students can view tests.
     */
    private void initViewTestsTab() {
        viewTestsPanel = new JPanel(new BorderLayout());

        // Table Model for Tests
        testsTableModel = new DefaultTableModel();
        testsTableModel.addColumn("Test ID");
        testsTableModel.addColumn("Title");
        testsTableModel.addColumn("Start Time");
        testsTableModel.addColumn("Duration (mins)");
        testsTableModel.addColumn("Chapter");

        // Populate Tests Table
        populateTestsTable();

        // Tests Table
        testsTable = new JTable(testsTableModel);
        JScrollPane scrollPane = new JScrollPane(testsTable);
        viewTestsPanel.add(scrollPane, BorderLayout.CENTER);

        // Refresh Button
        refreshTestsButton = new JButton("Refresh");
        refreshTestsButton.addActionListener(e -> populateTestsTable());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshTestsButton);
        viewTestsPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Populates the "View Tests" table with tests related to the student's
     * groups.
     */
    private void populateTestsTable() {
        // Clear existing rows
        testsTableModel.setRowCount(0);

        // Find groups the student is enrolled in
        List<Group> studentGroups = groups.stream()
                .filter(g -> g.getStudentIds().contains(student.getId()))
                .collect(Collectors.toList());

        // Find chapters associated with these groups
        List<String> chapterIds = studentGroups.stream()
                .map(Group::getChapterId)
                .distinct()
                .collect(Collectors.toList());

        // Find tests assigned to these chapters
        List<Test> relevantTests = tests.stream()
                .filter(t -> chapterIds.contains(t.getChapterId()))
                .collect(Collectors.toList());

        for (Test t : relevantTests) {
            String chapterName = getChapterNameById(t.getChapterId());
            testsTableModel
                    .addRow(new Object[] { t.getId(), t.getTitle(), t.getStartTime(), t.getDuration(), chapterName });
        }
    }

    /**
     * Initializes the "View All Groups" tab where students can view all groups
     * and see their enrollments.
     */
    private void initViewAllGroupsTab() {
        viewAllGroupsPanel = new JPanel(new BorderLayout());

        // Table Model for All Groups
        allGroupsTableModel = new DefaultTableModel();
        allGroupsTableModel.addColumn("Group ID");
        allGroupsTableModel.addColumn("Group Name");
        allGroupsTableModel.addColumn("Chapter");
        allGroupsTableModel.addColumn("Enrolled");

        // Populate All Groups Table
        populateAllGroupsTable();

        // All Groups Table
        allGroupsTable = new JTable(allGroupsTableModel) {
            // To visually distinguish groups the student is enrolled in
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                Boolean enrolled = (Boolean) getValueAt(row, 3);
                if (enrolled != null && enrolled) {
                    comp.setBackground(new Color(204, 255, 204)); // Light green
                } else {
                    comp.setBackground(new Color(255, 204, 255)); // Light pink
                }
                return comp;
            }
        };
        JScrollPane scrollPane = new JScrollPane(allGroupsTable);
        viewAllGroupsPanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshAllGroupsButton = new JButton("Refresh");
        enrollGroupButton = new JButton("Enroll in Selected Group");
        leaveGroupButton = new JButton("Leave Selected Group");
        buttonsPanel.add(refreshAllGroupsButton);
        buttonsPanel.add(enrollGroupButton);
        buttonsPanel.add(leaveGroupButton);
        viewAllGroupsPanel.add(buttonsPanel, BorderLayout.SOUTH);

        // Action Listeners
        refreshAllGroupsButton.addActionListener(e -> populateAllGroupsTable());
        enrollGroupButton.addActionListener(e -> enrollInSelectedGroup());
        leaveGroupButton.addActionListener(e -> leaveSelectedGroup());
    }

    /**
     * Populates the "View All Groups" table with all groups and marks those
     * where the student is enrolled.
     */
    private void populateAllGroupsTable() {
        // Clear existing rows
        allGroupsTableModel.setRowCount(0);

        for (Group g : groups) {
            String chapterName = getChapterNameById(g.getChapterId());
            boolean enrolled = g.getStudentIds().contains(student.getId());
            allGroupsTableModel.addRow(
                    new Object[] { g.getId(), g.getGroupName(), chapterName, enrolled });
        }
    }

    /**
     * Enrolls the student in the selected group.
     */
    private void enrollInSelectedGroup() {
        int selectedRow = allGroupsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a group to enroll.", "No Group Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean enrolled = (Boolean) allGroupsTableModel.getValueAt(selectedRow, 3);
        if (enrolled) {
            JOptionPane.showMessageDialog(this, "You are already enrolled in this group.", "Already Enrolled",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String groupId = (String) allGroupsTableModel.getValueAt(selectedRow, 0);
        Group selectedGroup = groups.stream()
                .filter(g -> g.getId().equals(groupId))
                .findFirst()
                .orElse(null);

        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Selected group not found.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Enroll the student
        selectedGroup.addStudent(student.getId());

        // Update groups.csv using toStringCSV()
        CSVUtil.writeToCSV("groups.csv", groups, g -> g.toStringCSV(),
                "id,groupName,chapterId,teacherIds,studentIds"); // Updated header to 'teacherIds'

        // Refresh tables
        populateAllGroupsTable();
        populateMyGroupsTable();

        JOptionPane.showMessageDialog(this, "Successfully enrolled in the group.", "Enrollment Successful",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Leaves the selected group.
     */
    private void leaveSelectedGroup() {
        int selectedRow = allGroupsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a group to leave.", "No Group Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean enrolled = (Boolean) allGroupsTableModel.getValueAt(selectedRow, 3);
        if (!enrolled) {
            JOptionPane.showMessageDialog(this, "You are not enrolled in this group.", "Not Enrolled",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String groupId = (String) allGroupsTableModel.getValueAt(selectedRow, 0);
        Group selectedGroup = groups.stream()
                .filter(g -> g.getId().equals(groupId))
                .findFirst()
                .orElse(null);

        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Selected group not found.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirm leaving the group
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to leave Group ID: " + groupId + "?",
                "Confirm Leave", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Remove the student from the group
            selectedGroup.removeStudent(student.getId());

            // Update groups.csv using toStringCSV()
            CSVUtil.writeToCSV("groups.csv", groups, g -> g.toStringCSV(),
                    "id,groupName,chapterId,teacherIds,studentIds"); // Updated header to 'teacherIds'

            // Refresh tables
            populateAllGroupsTable();
            populateMyGroupsTable();

            JOptionPane.showMessageDialog(this, "You have left the group successfully.", "Left Group",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Initializes the "My Groups" tab where students can view their enrolled
     * groups.
     */
    private void initMyGroupsTab() {
        myGroupsPanel = new JPanel(new BorderLayout());

        // Table Model for My Groups
        myGroupsTableModel = new DefaultTableModel();
        myGroupsTableModel.addColumn("Group ID");
        myGroupsTableModel.addColumn("Group Name");
        myGroupsTableModel.addColumn("Chapter");
        myGroupsTableModel.addColumn("Teachers"); // Updated to show multiple teachers

        // Populate My Groups Table
        populateMyGroupsTable();

        // My Groups Table
        myGroupsTable = new JTable(myGroupsTableModel);
        JScrollPane scrollPane = new JScrollPane(myGroupsTable);
        myGroupsPanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshMyGroupsButton = new JButton("Refresh");
        leaveSelectedGroupButton = new JButton("Leave Selected Group");
        buttonsPanel.add(refreshMyGroupsButton);
        buttonsPanel.add(leaveSelectedGroupButton);
        myGroupsPanel.add(buttonsPanel, BorderLayout.SOUTH);

        // Action Listeners
        refreshMyGroupsButton.addActionListener(e -> populateMyGroupsTable());
        leaveSelectedGroupButton.addActionListener(e -> leaveSelectedGroupFromMyGroups());
    }

    /**
     * Populates the "My Groups" table with groups the student is enrolled in.
     */
    private void populateMyGroupsTable() {
        // Clear existing rows
        myGroupsTableModel.setRowCount(0);

        // Find groups the student is enrolled in
        List<Group> studentGroups = groups.stream()
                .filter(g -> g.getStudentIds().contains(student.getId()))
                .collect(Collectors.toList());

        for (Group g : studentGroups) {
            String chapterName = getChapterNameById(g.getChapterId());
            List<String> teacherIds = g.getTeacherIds();
            String teachersNames = getTeachersNames(teacherIds); // Get concatenated teacher names
            myGroupsTableModel.addRow(new Object[] { g.getId(), g.getGroupName(), chapterName, teachersNames });
        }
    }

    /**
     * Leaves the selected group from the "My Groups" tab.
     */
    private void leaveSelectedGroupFromMyGroups() {
        int selectedRow = myGroupsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a group to leave.", "No Group Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String groupId = (String) myGroupsTableModel.getValueAt(selectedRow, 0);
        Group selectedGroup = groups.stream()
                .filter(g -> g.getId().equals(groupId))
                .findFirst()
                .orElse(null);

        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Selected group not found.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirm leaving the group
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to leave Group ID: " + groupId + "?",
                "Confirm Leave", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Remove the student from the group
            selectedGroup.removeStudent(student.getId());

            // Update groups.csv using toStringCSV()
            CSVUtil.writeToCSV("groups.csv", groups, g -> g.toStringCSV(),
                    "id,groupName,chapterId,teacherIds,studentIds"); // Updated header to 'teacherIds'

            // Refresh tables
            populateAllGroupsTable();
            populateMyGroupsTable();

            JOptionPane.showMessageDialog(this, "You have left the group successfully.", "Left Group",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Initializes the "Pay Due Payment" tab where students can view and pay
     * their balance.
     */
    private void initPayDuePanel() {
        payDuePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Current Balance Label
        JLabel balanceLabel = new JLabel("Current Balance:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        payDuePanel.add(balanceLabel, gbc);

        currentBalanceLabel = new JLabel(String.format("$%.2f", student.getBalance()));
        gbc.gridx = 1;
        gbc.gridy = 0;
        payDuePanel.add(currentBalanceLabel, gbc);

        // Payment Amount Label
        JLabel paymentLabel = new JLabel("Payment Amount:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        payDuePanel.add(paymentLabel, gbc);

        paymentAmountField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        payDuePanel.add(paymentAmountField, gbc);

        // Pay Button
        payButton = new JButton("Pay");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        payDuePanel.add(payButton, gbc);

        // Action Listener for Pay Button
        payButton.addActionListener(e -> processPayment());
    }

    /**
     * Processes the payment made by the student.
     */
    private void processPayment() {
        String paymentStr = paymentAmountField.getText().trim();
        if (paymentStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a payment amount.", "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        double paymentAmount;
        try {
            paymentAmount = Double.parseDouble(paymentStr);
            if (paymentAmount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive amount.", "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (paymentAmount > student.getBalance()) {
            JOptionPane.showMessageDialog(this, "Payment exceeds current balance.", "Payment Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update student's balance
        double newBalance = student.getBalance() - paymentAmount;
        student.setBalance(newBalance);
        currentBalanceLabel.setText(String.format("$%.2f", newBalance));

        // Update users.csv with the new balance
        updateUserBalanceInCSV();

        JOptionPane.showMessageDialog(this, "Payment successful. New balance: $" + String.format("%.2f", newBalance),
                "Payment Successful", JOptionPane.INFORMATION_MESSAGE);

        // Clear payment field
        paymentAmountField.setText("");
    }

    /**
     * Updates the student's balance in the users.csv file.
     */
    private void updateUserBalanceInCSV() {
        String filePath = "users.csv";
        File file = new File(filePath);
        File tempFile = new File("users_temp.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                String[] fields = currentLine.split(",");
                if (fields.length < 5) {
                    writer.write(currentLine + System.lineSeparator());
                    continue;
                }

                if (fields[0].equals(student.getId())) {
                    // Update balance
                    fields[4] = String.valueOf(student.getBalance());
                    String updatedLine = String.join(",", fields);
                    writer.write(updatedLine + System.lineSeparator());
                } else {
                    writer.write(currentLine + System.lineSeparator());
                }
            }

            writer.flush();
            reader.close();
            writer.close();

            // Replace original file with updated file
            if (!file.delete()) {
                System.err.println("Could not delete original users.csv file.");
                return;
            }
            if (!tempFile.renameTo(file)) {
                System.err.println("Could not rename temp file to users.csv.");
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error updating balance in users.csv.", "File Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the chapter name by its ID.
     *
     * @param chapterId The ID of the chapter.
     * @return The name of the chapter or "Unknown" if not found.
     */
    private String getChapterNameById(String chapterId) {
        for (Chapter c : chapters) {
            if (c.getId().equals(chapterId)) {
                return c.getName();
            }
        }
        return "Unknown";
    }

    /**
     * Retrieves the teachers' names by their IDs.
     *
     * @param teacherIds The list of teacher IDs.
     * @return A semicolon-separated string of teacher names or "Unknown" if not
     *         found.
     */
    private String getTeachersNames(List<String> teacherIds) {
        List<String> names = new ArrayList<>();
        for (String id : teacherIds) {
            String name = "Unknown";
            for (Teacher t : teachers) {
                if (t.getId().equals(id)) {
                    name = t.getName();
                    break;
                }
            }
            names.add(name);
        }
        return String.join("; ", names);
    }

    /**
     * Retrieves the teacher's name by their ID.
     * (Not used directly since we handle multiple teachers)
     *
     * @param teacherId The ID of the teacher.
     * @return The name of the teacher or "Unknown" if not found.
     */
    private String getTeacherNameById(String teacherId) {
        for (Teacher t : teachers) {
            if (t.getId().equals(teacherId)) {
                return t.getName();
            }
        }
        return "Unknown";
    }

    /**
     * Main method to launch the Student Dashboard for testing purposes.
     */
    public static void main(String[] args) {
        // For testing: Create a sample student and launch the dashboard
        Student sampleStudent = new Student("s1a2b3c4-d5e6-f7g8-h9i0-0987654321ba", "Bob Smith", "bobPass", 50.0);
        new StudentDashboard(sampleStudent);
    }
}
