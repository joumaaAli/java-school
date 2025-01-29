package com.mycompany.school.project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import model.subject.*;
import model.user.Teacher;
import model.user.Student;
import model.user.User;
import model.user.UserStorage;
import utils.SerializationUtil;
import utils.IDGenerator;

public class StudentDashboard extends JFrame {
    private Student student;

    // Data lists
    private ArrayList<Subject> subjects;
    private ArrayList<Chapter> chapters;
    private ArrayList<TestResult> testResults;

    private ArrayList<Group> groups;
    private ArrayList<Material> materials;
    private ArrayList<Test> tests;
    private ArrayList<Session> sessions;
    private ArrayList<Teacher> teachers; // store teacher data

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

    // New Panels for Taking Tests and Viewing Results
    private JPanel takeTestPanel;
    private JTable availableTestsTable;
    private DefaultTableModel availableTestsTableModel;
    private JButton takeTestButton;

    private JPanel viewTestResultsPanel;
    private JTable testResultsTable;
    private DefaultTableModel testResultsTableModel;
    private JButton refreshTestResultsButton;

    public StudentDashboard(Student student) {
        this.student = student;
        loadData();
        initComponents();
    }

    /**
     * Loads all necessary data from serialized files.
     */
    private void loadData() {
        // Load subjects
        subjects = SerializationUtil.readFromFile("subjects.ser");
        if (subjects == null) {
            subjects = new ArrayList<>();
            System.out.println("No existing subjects found.");
        }

        // Load chapters
        chapters = SerializationUtil.readFromFile("chapters.ser");
        if (chapters == null) {
            chapters = new ArrayList<>();
            System.out.println("No existing chapters found.");
        }

        // Load groups
        groups = SerializationUtil.readFromFile("groups.ser");
        if (groups == null) {
            groups = new ArrayList<>();
            System.out.println("No existing groups found.");
        }

        // Load materials
        materials = SerializationUtil.readFromFile("materials.ser");
        if (materials == null) {
            materials = new ArrayList<>();
            System.out.println("No existing materials found.");
        }

        // Load tests
        tests = SerializationUtil.readFromFile("tests.ser");
        if (tests == null) {
            tests = new ArrayList<>();
            System.out.println("No existing tests found.");
        }

        // Load sessions
        sessions = SerializationUtil.readFromFile("sessions.ser");
        if (sessions == null) {
            sessions = new ArrayList<>();
            System.out.println("No existing sessions found.");
        }

        // Load teachers from UserStorage
        ArrayList<User> allUsers = UserStorage.getUsers();
        teachers = allUsers.stream()
                .filter(u -> u instanceof Teacher)
                .map(u -> (Teacher) u)
                .collect(Collectors.toCollection(ArrayList::new));

        // Load test results
        testResults = SerializationUtil.readFromFile("testResults.ser");
        if (testResults == null) {
            testResults = new ArrayList<>();
            System.out.println("No existing test results found.");
        }

        // Link test results to the student
        testResults = testResults.stream()
                .filter(tr -> tr.getStudentId().equals(student.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
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
        initTakeTestTab();
        initViewTestResultsTab();

        // Add Tabs to TabbedPane
        tabbedPane.addTab("View Materials", viewMaterialsPanel);
        tabbedPane.addTab("View Tests", viewTestsPanel);
        tabbedPane.addTab("Take Test", takeTestPanel);
        tabbedPane.addTab("View Test Results", viewTestResultsPanel);
        tabbedPane.addTab("View All Groups", viewAllGroupsPanel);
        tabbedPane.addTab("My Groups", myGroupsPanel);
        tabbedPane.addTab("Pay Due Payment", payDuePanel);

        // Add Tabs to Frame
        add(tabbedPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
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
        ArrayList<Group> studentGroups = groups.stream()
                .filter(g -> g.getStudentIds().contains(student.getId()))
                .collect(Collectors.toCollection(ArrayList::new));

        // Find chapters associated with these groups
        ArrayList<String> chapterIds = studentGroups.stream()
                .map(Group::getChapterId)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));

        // Find materials assigned to these chapters
        ArrayList<Material> relevantMaterials = materials.stream()
                .filter(m -> chapterIds.contains(m.getChapterId()))
                .collect(Collectors.toCollection(ArrayList::new));

        for (Material m : relevantMaterials) {
            String chapterName = getChapterNameById(m.getChapterId());
            materialsTableModel
                    .addRow(new Object[] { m.getId(), m.getTitle(), m.getType(), m.getPathOrContent(),
                            chapterName });
        }
    }

    /**
     * Initializes the "View Tests" tab where students can view available tests.
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
        ArrayList<Group> studentGroups = groups.stream()
                .filter(g -> g.getStudentIds().contains(student.getId()))
                .collect(Collectors.toCollection(ArrayList::new));

        // Find chapters associated with these groups
        ArrayList<String> chapterIds = studentGroups.stream()
                .map(Group::getChapterId)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));

        // Find tests assigned to these chapters
        ArrayList<Test> relevantTests = tests.stream()
                .filter(t -> chapterIds.contains(t.getChapterId()))
                .collect(Collectors.toCollection(ArrayList::new));

        for (Test t : relevantTests) {
            String chapterName = getChapterNameById(t.getChapterId());
            testsTableModel
                    .addRow(new Object[] { t.getId(), t.getTitle(), t.getStartTime(), t.getDuration(),
                            chapterName });
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

            // Update groups.ser using SerializationUtil
            SerializationUtil.saveDataToDisk(groups, "groups.ser");

            // Refresh tables
            populateAllGroupsTable();
            populateMyGroupsTable();

            JOptionPane.showMessageDialog(this, "You have left the group successfully.", "Left Group",
                    JOptionPane.INFORMATION_MESSAGE);
        }
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

        // Update groups.ser using SerializationUtil
        SerializationUtil.saveDataToDisk(groups, "groups.ser");

        // Refresh tables
        populateAllGroupsTable();
        populateMyGroupsTable();

        JOptionPane.showMessageDialog(this, "Successfully enrolled in the group.", "Enrollment Successful",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Leaves the selected group.
     */
    private void leaveGroup() {
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

            // Update groups.ser using SerializationUtil
            SerializationUtil.saveDataToDisk(groups, "groups.ser");

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

        myGroupsTableModel = new DefaultTableModel();
        myGroupsTableModel.addColumn("Group ID");
        myGroupsTableModel.addColumn("Group Name");
        myGroupsTableModel.addColumn("Chapter");
        myGroupsTableModel.addColumn("Teachers"); // show multiple teachers

        populateMyGroupsTable();

        myGroupsTable = new JTable(myGroupsTableModel);
        JScrollPane scrollPane = new JScrollPane(myGroupsTable);
        myGroupsPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshMyGroupsButton = new JButton("Refresh");
        leaveSelectedGroupButton = new JButton("Leave Selected Group");
        buttonsPanel.add(refreshMyGroupsButton);
        buttonsPanel.add(leaveSelectedGroupButton);
        myGroupsPanel.add(buttonsPanel, BorderLayout.SOUTH);

        refreshMyGroupsButton.addActionListener(e -> populateMyGroupsTable());
        leaveSelectedGroupButton.addActionListener(e -> leaveSelectedGroupFromMyGroups());
    }

    private void populateMyGroupsTable() {
        myGroupsTableModel.setRowCount(0);
        ArrayList<Group> myGroups = groups.stream()
                .filter(g -> g.getStudentIds().contains(student.getId()))
                .collect(Collectors.toCollection(ArrayList::new));

        for (Group g : myGroups) {
            String chapterName = getChapterNameById(g.getChapterId());
            int enrollees = g.getStudentIds().size();
            String teachersNames = getTeachersNames(g.getTeacherIds());
            myGroupsTableModel.addRow(new Object[] {
                    g.getId(), g.getGroupName(), chapterName, teachersNames
            });
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

            // Update groups.ser using SerializationUtil
            SerializationUtil.saveDataToDisk(groups, "groups.ser");

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

        // Update users.ser with the new balance
        updateUserBalanceInStorage();

        JOptionPane.showMessageDialog(this,
                "Payment successful. New balance: $" + String.format("%.2f", newBalance),
                "Payment Successful",
                JOptionPane.INFORMATION_MESSAGE);

        // Clear payment field
        paymentAmountField.setText("");
    }

    /**
     * Updates the student's balance in the UserStorage and saves it to disk.
     */
    private void updateUserBalanceInStorage() {
        ArrayList<User> allUsers = UserStorage.getUsers();
        for (User u : allUsers) {
            if (u instanceof Student && u.getId().equals(student.getId())) {
                ((Student) u).setBalance(student.getBalance());
                break;
            }
        }
        UserStorage.updateUsers(allUsers);
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
    private String getTeachersNames(ArrayList<String> teacherIds) {
        ArrayList<String> names = new ArrayList<>();
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
     * Initializes the "Take Test" tab where students can take available tests.
     */
    private void initTakeTestTab() {
        takeTestPanel = new JPanel(new BorderLayout());

        // Table Model for Available Tests
        availableTestsTableModel = new DefaultTableModel();
        availableTestsTableModel.addColumn("Test ID");
        availableTestsTableModel.addColumn("Title");
        availableTestsTableModel.addColumn("Start Time");
        availableTestsTableModel.addColumn("Duration (mins)");
        availableTestsTableModel.addColumn("Chapter");

        // Populate Available Tests Table
        populateAvailableTestsTable();

        // Available Tests Table
        availableTestsTable = new JTable(availableTestsTableModel);
        JScrollPane scrollPane = new JScrollPane(availableTestsTable);
        takeTestPanel.add(scrollPane, BorderLayout.CENTER);

        // Take Test Button
        takeTestButton = new JButton("Take Selected Test");
        takeTestButton.addActionListener(e -> takeSelectedTest());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(takeTestButton);
        takeTestPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Populates the "Take Test" table with available tests related to the student's
     * groups.
     */
    private void populateAvailableTestsTable() {
        // Clear existing rows
        availableTestsTableModel.setRowCount(0);

        // Find groups the student is enrolled in
        ArrayList<Group> studentGroups = groups.stream()
                .filter(g -> g.getStudentIds().contains(student.getId()))
                .collect(Collectors.toCollection(ArrayList::new));

        // Find chapters associated with these groups
        ArrayList<String> chapterIds = studentGroups.stream()
                .map(Group::getChapterId)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));

        // Find tests assigned to these chapters
        ArrayList<Test> relevantTests = tests.stream()
                .filter(t -> chapterIds.contains(t.getChapterId()))
                .collect(Collectors.toCollection(ArrayList::new));

        // Exclude tests already taken by the student
        ArrayList<String> takenTestIds = testResults.stream()
                .map(TestResult::getTestId)
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Test> availableTests = relevantTests.stream()
                .filter(t -> !takenTestIds.contains(t.getId()))
                .collect(Collectors.toCollection(ArrayList::new));

        for (Test t : availableTests) {
            String chapterName = getChapterNameById(t.getChapterId());
            availableTestsTableModel
                    .addRow(new Object[] { t.getId(), t.getTitle(), t.getStartTime(), t.getDuration(),
                            chapterName });
        }
    }

    /**
     * Allows the student to take the selected test.
     */
    private void takeSelectedTest() {
        int selectedRow = availableTestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a test to take.", "No Test Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String testId = (String) availableTestsTableModel.getValueAt(selectedRow, 0);
        Test selectedTest = tests.stream()
                .filter(t -> t.getId().equals(testId))
                .findFirst()
                .orElse(null);

        if (selectedTest == null) {
            JOptionPane.showMessageDialog(this, "Selected test not found.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Open Test Taking Dialog
        TestTakingDialog testDialog = new TestTakingDialog(this, selectedTest);
        testDialog.setVisible(true);

        // After test is taken, refresh available tests and test results
        populateAvailableTestsTable();
        populateTestResultsTable();
    }

    /**
     * Initializes the "View Test Results" tab where students can view their
     * test results.
     */
    private void initViewTestResultsTab() {
        viewTestResultsPanel = new JPanel(new BorderLayout());

        // Table Model for Test Results
        testResultsTableModel = new DefaultTableModel();
        testResultsTableModel.addColumn("Test ID");
        testResultsTableModel.addColumn("Title");
        testResultsTableModel.addColumn("Score");
        testResultsTableModel.addColumn("Submission Time");

        // Populate Test Results Table
        populateTestResultsTable();

        // Test Results Table
        testResultsTable = new JTable(testResultsTableModel);
        JScrollPane scrollPane = new JScrollPane(testResultsTable);
        viewTestResultsPanel.add(scrollPane, BorderLayout.CENTER);

        // Refresh Button
        refreshTestResultsButton = new JButton("Refresh");
        refreshTestResultsButton.addActionListener(e -> populateTestResultsTable());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshTestResultsButton);
        viewTestResultsPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Populates the "View Test Results" table with the student's test results.
     */
    private void populateTestResultsTable() {
        // Clear existing rows
        testResultsTableModel.setRowCount(0);

        for (TestResult tr : testResults) {
            Test test = tests.stream()
                    .filter(t -> t.getId().equals(tr.getTestId()))
                    .findFirst()
                    .orElse(null);
            String testTitle = (test != null) ? test.getTitle() : "Unknown";
            testResultsTableModel.addRow(new Object[] {
                    tr.getTestId(), testTitle, String.format("%.2f", tr.getScore()), tr.getSubmissionTime()
            });
        }
    }

    /**
     * Main method to launch the Student Dashboard for testing purposes.
     */
    public static void main(String[] args) {
        // For testing: Create a sample student and launch the dashboard
        Student sampleStudent = new Student("s1a2b3c4-d5e6-f7g8-h9i0-0987654321ba", "Bob Smith", "bobPass", 50.0);
        new StudentDashboard(sampleStudent);
    }

    /**
     * Inner class representing the Test Taking Dialog.
     */
    private class TestTakingDialog extends JDialog {
        private Test test;
        private ArrayList<Question> questions;
        private Map<String, Integer> answers; // Map<QuestionID, SelectedOption>
        private int currentQuestionIndex = 0;

        private JLabel questionLabel;
        private JRadioButton[] optionButtons;
        private ButtonGroup optionsGroup;
        private JButton nextButton;
        private JButton submitButton;

        public TestTakingDialog(JFrame parent, Test test) {
            super(parent, "Taking Test: " + test.getTitle(), true);
            this.test = test;
            this.questions = test.getQuestions();
            this.answers = new HashMap<>();
            initTestComponents();
        }

        private void initTestComponents() {
            setSize(700, 500);
            setLocationRelativeTo(getParent());
            setLayout(new BorderLayout());

            JPanel questionPanel = new JPanel(new GridLayout(0, 1));
            questionLabel = new JLabel();
            questionPanel.add(questionLabel);

            optionsGroup = new ButtonGroup();
            optionButtons = new JRadioButton[4];
            for (int i = 0; i < 4; i++) {
                optionButtons[i] = new JRadioButton();
                optionsGroup.add(optionButtons[i]);
                questionPanel.add(optionButtons[i]);
            }

            add(questionPanel, BorderLayout.CENTER);

            JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            nextButton = new JButton("Next");
            submitButton = new JButton("Submit");
            submitButton.setEnabled(false);
            navigationPanel.add(nextButton);
            navigationPanel.add(submitButton);
            add(navigationPanel, BorderLayout.SOUTH);

            // Display first question
            displayQuestion(currentQuestionIndex);

            // Action Listeners
            nextButton.addActionListener(e -> goToNextQuestion());
            submitButton.addActionListener(e -> submitTest());
        }

        private void displayQuestion(int index) {
            if (index >= 0 && index < questions.size()) {
                Question q = questions.get(index);
                questionLabel.setText("Q" + (index + 1) + ": " + q.getText());
                String[] opts = q.getOptions();
                for (int i = 0; i < opts.length; i++) {
                    optionButtons[i].setText(opts[i]);
                    optionButtons[i].setSelected(false);
                }

                // If answer already exists, set it
                if (answers.containsKey(q.getId())) {
                    int selectedOption = answers.get(q.getId());
                    if (selectedOption >= 0 && selectedOption < optionButtons.length) {
                        optionButtons[selectedOption].setSelected(true);
                    }
                }

                // Update button states
                if (index == questions.size() - 1) {
                    nextButton.setEnabled(false);
                    submitButton.setEnabled(true);
                } else {
                    nextButton.setEnabled(true);
                    submitButton.setEnabled(false);
                }
            }
        }

        private void goToNextQuestion() {
            // Save current answer
            saveCurrentAnswer();

            if (currentQuestionIndex < questions.size() - 1) {
                currentQuestionIndex++;
                displayQuestion(currentQuestionIndex);
            }
        }

        private void saveCurrentAnswer() {
            Question q = questions.get(currentQuestionIndex);
            int selectedOption = -1;
            for (int i = 0; i < optionButtons.length; i++) {
                if (optionButtons[i].isSelected()) {
                    selectedOption = i;
                    break;
                }
            }
            if (selectedOption != -1) {
                answers.put(q.getId(), selectedOption);
            }
        }

        private void submitTest() {
            // Save last answer
            saveCurrentAnswer();

            // Check if all questions are answered
            if (answers.size() < questions.size()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "You have unanswered questions. Do you still want to submit?",
                        "Unanswered Questions",
                        JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // Calculate score
            double score = 0.0;
            for (Question q : questions) {
                if (answers.containsKey(q.getId())) {
                    int selected = answers.get(q.getId());
                    if (selected == q.getCorrectOption()) {
                        score += 1.0; // Each correct answer gives 1 point
                    }
                }
            }
            score = (score / questions.size()) * 100.0; // Percentage

            // Create TestResult
            String submissionTime = java.time.LocalDateTime.now().toString();
            TestResult tr = new TestResult(test.getId(), student.getId(), answers, score, submissionTime);
            testResults.add(tr);

            // Save TestResults to disk
            saveTestResults();

            // Show score to student
            JOptionPane.showMessageDialog(this,
                    "Test submitted successfully!\nYour Score: " + String.format("%.2f", score) + "%",
                    "Test Submitted",
                    JOptionPane.INFORMATION_MESSAGE);

            // Close dialog
            dispose();
        }

        private void saveTestResults() {
            // Load existing test results
            ArrayList<TestResult> allTestResults = SerializationUtil.readFromFile("testResults.ser");
            if (allTestResults == null) {
                allTestResults = new ArrayList<>();
            }

            // Add the new result
            allTestResults.addAll(testResults);

            // Save back to disk
            SerializationUtil.saveDataToDisk(allTestResults, "testResults.ser");
        }
    }
}
