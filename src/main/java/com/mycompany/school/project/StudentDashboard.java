package com.mycompany.school.project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import model.subject.*;
import model.user.Teacher;
import model.user.Notification;
import model.user.Student;
import model.user.User;
import model.user.UserStorage;
import utils.IDGenerator;
import utils.SerializationUtil;

public class StudentDashboard extends JFrame {
    private Student student;

    // Data lists
    private ArrayList<Subject> subjects;
    private ArrayList<Chapter> chapters;
    private ArrayList<Group> groups;
    private ArrayList<Material> materials;
    private ArrayList<Test> tests;
    private ArrayList<Session> sessions;
    private ArrayList<Teacher> teachers; // store teacher data

    // Test Results
    private ArrayList<TestResult> testResults;

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

    // Panels for Taking Tests and Viewing Results
    private JPanel takeTestPanel;
    private JTable availableTestsTable;
    private DefaultTableModel availableTestsTableModel;
    private JButton takeTestButton;

    private JPanel viewTestResultsPanel;
    private JTable testResultsTable;
    private DefaultTableModel testResultsTableModel;
    private JButton refreshTestResultsButton;

    // Notifications Panel
    private JPanel notificationsPanel;
    private JTable notificationsTable;
    private DefaultTableModel notificationsTableModel;
    private JButton refreshNotificationsButton;

    // Sessions Tab (for sessions the student can join)
    private JPanel sessionsPanel;
    private JTable sessionsTable;
    private DefaultTableModel sessionsTableModel;
    private JButton refreshSessionsButton;
    private JButton joinSessionButton; // NEW: Button to join selected session

    private Timer notificationRefreshTimer;

    public StudentDashboard(Student student) {
        // Store the initially passed student
        this.student = student;
        loadData();
        initComponents();
    }

    /**
     * Reload data from disk and refresh the student object from storage.
     */
    private void loadData() {
        subjects = SerializationUtil.readFromFile("subjects.ser");
        if (subjects == null) {
            subjects = new ArrayList<>();
            System.out.println("No existing subjects found.");
        }
        chapters = SerializationUtil.readFromFile("chapters.ser");
        if (chapters == null) {
            chapters = new ArrayList<>();
            System.out.println("No existing chapters found.");
        }
        groups = SerializationUtil.readFromFile("groups.ser");
        if (groups == null) {
            groups = new ArrayList<>();
            System.out.println("No existing groups found.");
        }
        materials = SerializationUtil.readFromFile("materials.ser");
        if (materials == null) {
            materials = new ArrayList<>();
            System.out.println("No existing materials found.");
        }
        tests = SerializationUtil.readFromFile("tests.ser");
        if (tests == null) {
            tests = new ArrayList<>();
            System.out.println("No existing tests found.");
        }
        sessions = SerializationUtil.readFromFile("sessions.ser");
        if (sessions == null) {
            sessions = new ArrayList<>();
            System.out.println("No existing sessions found.");
        }
        List<User> allUsers = UserStorage.getUsers();
        teachers = allUsers.stream()
                .filter(u -> u instanceof Teacher)
                .map(u -> (Teacher) u)
                .collect(Collectors.toCollection(ArrayList::new));
        // IMPORTANT: Refresh the student instance from storage so that new
        // notifications are loaded.
        Student updatedStudent = (Student) UserStorage.getUserById(student.getId());
        if (updatedStudent != null) {
            this.student = updatedStudent;
        }
        testResults = (ArrayList<TestResult>) student.getTestResults();
        System.out.println("Loaded " + testResults.size() + " test results for student: " + student.getName());
    }

    private void initComponents() {
        setTitle("Student Dashboard - " + student.getName());
        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();

        initViewMaterialsTab();
        initViewTestsTab();
        initViewAllGroupsTab();
        initMyGroupsTab();
        initPayDuePanel();
        initTakeTestTab();
        initViewTestResultsTab();
        initNotificationsTab();
        initSessionsTab();

        tabbedPane.addTab("View Materials", viewMaterialsPanel);
        tabbedPane.addTab("View Tests", viewTestsPanel);
        tabbedPane.addTab("Take Test", takeTestPanel);
        tabbedPane.addTab("View Test Results", viewTestResultsPanel);
        tabbedPane.addTab("View All Groups", viewAllGroupsPanel);
        tabbedPane.addTab("My Groups", myGroupsPanel);
        tabbedPane.addTab("Pay Due Payment", payDuePanel);
        tabbedPane.addTab("Notifications", notificationsPanel);
        tabbedPane.addTab("Sessions", sessionsPanel);

        add(tabbedPane, BorderLayout.CENTER);
        initNotificationRefreshTimer();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Initializes and starts the notification refresh timer.
     */
    private void initNotificationRefreshTimer() {
        notificationRefreshTimer = new Timer();
        notificationRefreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> populateNotifications());
            }
        }, 0, 10000); // Refresh every 10 seconds
    }

    // -------------------- View Materials Tab --------------------
    private void initViewMaterialsTab() {
        viewMaterialsPanel = new JPanel(new BorderLayout());
        materialsTableModel = new DefaultTableModel();
        materialsTableModel.addColumn("Material ID");
        materialsTableModel.addColumn("Title");
        materialsTableModel.addColumn("Type");
        materialsTableModel.addColumn("Path/URL");
        materialsTableModel.addColumn("Chapter");
        populateMaterialsTable();
        materialsTable = new JTable(materialsTableModel);
        JScrollPane scrollPane = new JScrollPane(materialsTable);
        viewMaterialsPanel.add(scrollPane, BorderLayout.CENTER);
        refreshMaterialsButton = new JButton("Refresh");
        refreshMaterialsButton.addActionListener(e -> populateMaterialsTable());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshMaterialsButton);
        viewMaterialsPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void populateMaterialsTable() {
        materialsTableModel.setRowCount(0);
        ArrayList<Group> studentGroups = groups.stream()
                .filter(g -> g.getStudentIds().contains(student.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> chapterIds = studentGroups.stream()
                .map(Group::getChapterId)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Material> relevantMaterials = materials.stream()
                .filter(m -> chapterIds.contains(m.getChapterId()))
                .collect(Collectors.toCollection(ArrayList::new));
        for (Material m : relevantMaterials) {
            String chapterName = getChapterNameById(m.getChapterId());
            materialsTableModel
                    .addRow(new Object[] { m.getId(), m.getTitle(), m.getType(), m.getPathOrContent(), chapterName });
        }
    }

    // -------------------- View Tests Tab --------------------
    private void initViewTestsTab() {
        viewTestsPanel = new JPanel(new BorderLayout());
        testsTableModel = new DefaultTableModel();
        testsTableModel.addColumn("Test ID");
        testsTableModel.addColumn("Title");
        testsTableModel.addColumn("Start Time");
        testsTableModel.addColumn("Duration (mins)");
        testsTableModel.addColumn("Chapter");
        populateTestsTable();
        testsTable = new JTable(testsTableModel);
        JScrollPane scrollPane = new JScrollPane(testsTable);
        viewTestsPanel.add(scrollPane, BorderLayout.CENTER);
        refreshTestsButton = new JButton("Refresh");
        refreshTestsButton.addActionListener(e -> populateTestsTable());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshTestsButton);
        viewTestsPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void populateTestsTable() {
        testsTableModel.setRowCount(0);
        ArrayList<Group> studentGroups = groups.stream()
                .filter(g -> g.getStudentIds().contains(student.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> chapterIds = studentGroups.stream()
                .map(Group::getChapterId)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Test> relevantTests = tests.stream()
                .filter(t -> chapterIds.contains(t.getChapterId()))
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> takenTestIds = testResults.stream()
                .map(TestResult::getTestId)
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Test> availableTests = relevantTests.stream()
                .filter(t -> !takenTestIds.contains(t.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
        for (Test t : availableTests) {
            String chapterName = getChapterNameById(t.getChapterId());
            testsTableModel
                    .addRow(new Object[] { t.getId(), t.getTitle(), t.getStartTime(), t.getDuration(), chapterName });
        }
    }

    // -------------------- View All Groups Tab --------------------
    private void initViewAllGroupsTab() {
        viewAllGroupsPanel = new JPanel(new BorderLayout());
        allGroupsTableModel = new DefaultTableModel();
        allGroupsTableModel.addColumn("Group ID");
        allGroupsTableModel.addColumn("Group Name");
        allGroupsTableModel.addColumn("Chapter");
        allGroupsTableModel.addColumn("Enrolled");
        populateAllGroupsTable();
        allGroupsTable = new JTable(allGroupsTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                Boolean enrolled = (Boolean) getValueAt(row, 3);
                if (enrolled != null && enrolled) {
                    comp.setBackground(new Color(204, 255, 204));
                } else {
                    comp.setBackground(new Color(255, 204, 255));
                }
                return comp;
            }
        };
        JScrollPane scrollPane = new JScrollPane(allGroupsTable);
        viewAllGroupsPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshAllGroupsButton = new JButton("Refresh");
        enrollGroupButton = new JButton("Enroll in Selected Group");
        leaveGroupButton = new JButton("Leave Selected Group");
        buttonsPanel.add(refreshAllGroupsButton);
        buttonsPanel.add(enrollGroupButton);
        buttonsPanel.add(leaveGroupButton);
        viewAllGroupsPanel.add(buttonsPanel, BorderLayout.SOUTH);
        refreshAllGroupsButton.addActionListener(e -> populateAllGroupsTable());
        enrollGroupButton.addActionListener(e -> enrollInSelectedGroup());
        leaveGroupButton.addActionListener(e -> leaveSelectedGroup());
    }

    private void populateAllGroupsTable() {
        allGroupsTableModel.setRowCount(0);
        for (Group g : groups) {
            String chapterName = getChapterNameById(g.getChapterId());
            boolean enrolled = g.getStudentIds().contains(student.getId());
            allGroupsTableModel.addRow(new Object[] { g.getId(), g.getGroupName(), chapterName, enrolled });
        }
    }

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
            JOptionPane.showMessageDialog(this, "Selected group not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        selectedGroup.addStudent(student.getId());
        student.enrollGroup(groupId);
        SerializationUtil.saveDataToDisk(groups, "groups.ser");
        SerializationUtil.saveDataToDisk(UserStorage.getUsers(), "users.ser");
        populateAllGroupsTable();
        populateMyGroupsTable();
        JOptionPane.showMessageDialog(this, "Successfully enrolled in the group.", "Enrollment Successful",
                JOptionPane.INFORMATION_MESSAGE);
    }

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
            JOptionPane.showMessageDialog(this, "Selected group not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to leave Group ID: " + groupId + "?",
                "Confirm Leave", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            selectedGroup.removeStudent(student.getId());
            student.leaveGroup(groupId);
            SerializationUtil.saveDataToDisk(groups, "groups.ser");
            SerializationUtil.saveDataToDisk(UserStorage.getUsers(), "users.ser");
            populateAllGroupsTable();
            populateMyGroupsTable();
            JOptionPane.showMessageDialog(this, "You have left the group successfully.", "Left Group",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // -------------------- My Groups Tab --------------------
    private void initMyGroupsTab() {
        myGroupsPanel = new JPanel(new BorderLayout());
        myGroupsTableModel = new DefaultTableModel();
        myGroupsTableModel.addColumn("Group ID");
        myGroupsTableModel.addColumn("Group Name");
        myGroupsTableModel.addColumn("Chapter");
        myGroupsTableModel.addColumn("Teachers");
        populateMyGroupsTable();
        myGroupsTable = new JTable(myGroupsTableModel);
        JScrollPane scrollPane = new JScrollPane(myGroupsTable);
        myGroupsPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshMyGroupsButton = new JButton("Refresh");
        buttonsPanel.add(refreshMyGroupsButton);
        refreshMyGroupsButton.addActionListener(e -> populateMyGroupsTable());
    }

    private void populateMyGroupsTable() {
        myGroupsTableModel.setRowCount(0);
        ArrayList<Group> myGroups = groups.stream()
                .filter(g -> g.getStudentIds().contains(student.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
        for (Group g : myGroups) {
            String chapterName = getChapterNameById(g.getChapterId());
            String teachersNames = g.getTeacherIds().stream().map(this::getTeacherNameById)
                    .collect(Collectors.joining("; "));
            myGroupsTableModel.addRow(new Object[] { g.getId(), g.getGroupName(), chapterName, teachersNames });
        }
    }

    // -------------------- Pay Due Panel --------------------
    private void initPayDuePanel() {
        payDuePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel balanceLabel = new JLabel("Current Balance:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        payDuePanel.add(balanceLabel, gbc);
        currentBalanceLabel = new JLabel(String.format("$%.2f", student.getBalance()));
        gbc.gridx = 1;
        gbc.gridy = 0;
        payDuePanel.add(currentBalanceLabel, gbc);
        JLabel paymentLabel = new JLabel("Payment Amount:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        payDuePanel.add(paymentLabel, gbc);
        paymentAmountField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        payDuePanel.add(paymentAmountField, gbc);
        payButton = new JButton("Pay");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        payDuePanel.add(payButton, gbc);
        payButton.addActionListener(e -> processPayment());
    }

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
            if (paymentAmount <= 0)
                throw new NumberFormatException();
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
        double newBalance = student.getBalance() - paymentAmount;
        student.setBalance(newBalance);
        currentBalanceLabel.setText(String.format("$%.2f", newBalance));
        updateUserBalanceInStorage();
        JOptionPane.showMessageDialog(this, "Payment successful. New balance: $" + String.format("%.2f", newBalance),
                "Payment Successful", JOptionPane.INFORMATION_MESSAGE);
        paymentAmountField.setText("");
    }

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

    // -------------------- Take Test Tab --------------------
    private void initTakeTestTab() {
        takeTestPanel = new JPanel(new BorderLayout());
        availableTestsTableModel = new DefaultTableModel();
        availableTestsTableModel.addColumn("Test ID");
        availableTestsTableModel.addColumn("Title");
        availableTestsTableModel.addColumn("Start Time");
        availableTestsTableModel.addColumn("Duration (mins)");
        availableTestsTableModel.addColumn("Chapter");
        populateAvailableTestsTable();
        availableTestsTable = new JTable(availableTestsTableModel);
        JScrollPane scrollPane = new JScrollPane(availableTestsTable);
        takeTestPanel.add(scrollPane, BorderLayout.CENTER);
        takeTestButton = new JButton("Take Selected Test");
        takeTestButton.addActionListener(e -> takeSelectedTest());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(takeTestButton);
        takeTestPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void populateAvailableTestsTable() {
        availableTestsTableModel.setRowCount(0);
        ArrayList<Group> studentGroups = groups.stream()
                .filter(g -> g.getStudentIds().contains(student.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> chapterIds = studentGroups.stream()
                .map(Group::getChapterId)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Test> relevantTests = tests.stream()
                .filter(t -> chapterIds.contains(t.getChapterId()))
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> takenTestIds = testResults.stream()
                .map(TestResult::getTestId)
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Test> availableTests = relevantTests.stream()
                .filter(t -> !takenTestIds.contains(t.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
        for (Test t : availableTests) {
            String chapterName = getChapterNameById(t.getChapterId());
            availableTestsTableModel
                    .addRow(new Object[] { t.getId(), t.getTitle(), t.getStartTime(), t.getDuration(), chapterName });
        }
    }

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
            JOptionPane.showMessageDialog(this, "Selected test not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        TestTakingDialog testDialog = new TestTakingDialog(this, selectedTest);
        testDialog.setVisible(true);
        populateAvailableTestsTable();
        populateTestResultsTable();
    }

    // -------------------- View Test Results Tab --------------------
    private void initViewTestResultsTab() {
        viewTestResultsPanel = new JPanel(new BorderLayout());
        testResultsTableModel = new DefaultTableModel();
        testResultsTableModel.addColumn("Test ID");
        testResultsTableModel.addColumn("Title");
        testResultsTableModel.addColumn("Score");
        testResultsTableModel.addColumn("Submission Time");
        populateTestResultsTable();
        testResultsTable = new JTable(testResultsTableModel);
        JScrollPane scrollPane = new JScrollPane(testResultsTable);
        viewTestResultsPanel.add(scrollPane, BorderLayout.CENTER);
        refreshTestResultsButton = new JButton("Refresh");
        refreshTestResultsButton.addActionListener(e -> populateTestResultsTable());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshTestResultsButton);
        viewTestResultsPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void populateTestResultsTable() {
        testResultsTableModel.setRowCount(0);
        for (TestResult tr : testResults) {
            Test test = tests.stream()
                    .filter(t -> t.getId().equals(tr.getTestId()))
                    .findFirst()
                    .orElse(null);
            String testTitle = (test != null) ? test.getTitle() : "Unknown";
            testResultsTableModel.addRow(new Object[] { tr.getTestId(), testTitle, String.format("%.2f", tr.getScore()),
                    tr.getSubmissionTime() });
        }
    }

    // -------------------- Notifications Tab --------------------
    private void initNotificationsTab() {
        notificationsPanel = new JPanel(new BorderLayout());
        notificationsTableModel = new DefaultTableModel(new Object[] { "Session", "Date", "Status", "Action" }, 0);
        notificationsTable = new JTable(notificationsTableModel) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 3 ? JButton.class : String.class;
            }
        };
        notificationsTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        notificationsTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
        JScrollPane scroll = new JScrollPane(notificationsTable);
        JPanel buttonPanel = new JPanel();
        refreshNotificationsButton = new JButton("Refresh");
        // NEW: Add Remove Notification button
        JButton removeNotificationButton = new JButton("Remove Notification");
        buttonPanel.add(refreshNotificationsButton);
        buttonPanel.add(removeNotificationButton);
        notificationsPanel.add(scroll, BorderLayout.CENTER);
        notificationsPanel.add(buttonPanel, BorderLayout.SOUTH);
        refreshNotificationsButton.addActionListener(e -> populateNotifications());
        removeNotificationButton.addActionListener(e -> removeSelectedNotification());
        new Timer().schedule(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    // Refresh student object from storage to include new notifications.
                    Student updatedStudent = (Student) UserStorage.getUserById(student.getId());
                    if (updatedStudent != null) {
                        student = updatedStudent;
                    }
                    populateNotifications();
                });
            }
        }, 0, 10000);
    }

    private void populateNotifications() {
        // Refresh the sessions list so we pick up any new sessions
        sessions = SerializationUtil.readFromFile("sessions.ser");
        System.out.println(sessions);
        if (sessions == null) {
            sessions = new ArrayList<>();
        }
        System.out.println(student);
        notificationsTableModel.setRowCount(0);
        student.getNotifications().forEach(notif -> {
            Session session = sessions.stream()
                    .filter(s -> s.getId().equals(notif.getSessionId()))
                    .findFirst()
                    .orElse(null);
            if (session != null) {
                boolean isJoined = session.getStudentIds().contains(student.getId());
                notificationsTableModel.addRow(new Object[] { session.getTitle(), session.getDateTime(),
                        isJoined ? "Joined" : "Pending", isJoined ? "Open" : "Join" });
            }
        });
    }

    // NEW: Remove selected notification from student's notifications list.
    private void removeSelectedNotification() {
        int selectedRow = notificationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a notification to remove.", "No Notification Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sessionTitle = (String) notificationsTableModel.getValueAt(selectedRow, 0);
        String sessionDateTime = (String) notificationsTableModel.getValueAt(selectedRow, 1);
        // Find the matching notification based on the session details.
        Notification toRemove = null;
        for (Notification notif : student.getNotifications()) {
            Session session = sessions.stream()
                    .filter(s -> s.getId().equals(notif.getSessionId()))
                    .findFirst()
                    .orElse(null);
            if (session != null && session.getTitle().equals(sessionTitle)
                    && session.getDateTime().equals(sessionDateTime)) {
                toRemove = notif;
                break;
            }
        }
        if (toRemove != null) {
            student.getNotifications().remove(toRemove);
            UserStorage.updateUsers(UserStorage.getUsers());
            populateNotifications();
            JOptionPane.showMessageDialog(this, "Notification removed.", "Removal Successful",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Notification not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------- Sessions Tab (Student) --------------------
    private void initSessionsTab() {
        sessionsPanel = new JPanel(new BorderLayout());
        // Show only sessions for groups the student is enrolled in.
        sessionsTableModel = new DefaultTableModel(
                new Object[] { "Session ID", "Title", "DateTime", "Group", "Participants" }, 0);
        sessionsTable = new JTable(sessionsTableModel);
        JScrollPane tableScroll = new JScrollPane(sessionsTable);
        sessionsPanel.add(tableScroll, BorderLayout.CENTER);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshSessionsButton = new JButton("Refresh Sessions");
        joinSessionButton = new JButton("Join Session"); // NEW: Button to join a session
        topPanel.add(refreshSessionsButton);
        topPanel.add(joinSessionButton);
        sessionsPanel.add(topPanel, BorderLayout.NORTH);
        refreshSessionsButton.addActionListener(e -> populateSessionsTable());
        joinSessionButton.addActionListener(e -> joinSelectedSession());
        new Timer().schedule(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(() -> populateSessionsTable());
            }
        }, 0, 5000);
    }

    private void populateSessionsTable() {
        sessionsTableModel.setRowCount(0);
        // Only display sessions whose groupId is among those the student is enrolled
        // in.
        List<String> studentGroupIds = student.getGroupIds();
        for (Session s : sessions) {
            if (studentGroupIds.contains(s.getGroupId())) {
                String groupName = getGroupNameById(s.getGroupId());
                int teacherCount = (s.getTeacherIds() != null) ? s.getTeacherIds().size() : 0;
                int studentCount = (s.getStudentIds() != null) ? s.getStudentIds().size() : 0;
                String participants = "Teachers: " + teacherCount + ", Students: " + studentCount;
                sessionsTableModel
                        .addRow(new Object[] { s.getId(), s.getTitle(), s.getDateTime(), groupName, participants });
            }
        }
    }

    // NEW: Allow student to join the selected session from the Sessions page.
    private void joinSelectedSession() {
        int selectedRow = sessionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a session to join.", "No Session Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sessionId = (String) sessionsTableModel.getValueAt(selectedRow, 0);
        Session session = sessions.stream()
                .filter(s -> s.getId().equals(sessionId))
                .findFirst()
                .orElse(null);
        if (session == null) {
            JOptionPane.showMessageDialog(this, "Selected session not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!session.getStudentIds().contains(student.getId())) {
            session.addStudent(student.getId());
            SerializationUtil.saveDataToDisk(sessions, "sessions.ser");
        }
        openSessionRoom(session);
    }

    /**
     * When a student clicks the "Join" button in a notification, verify that they
     * are enrolled
     * and open the session room.
     */
    private void joinSession(int row) {
        String sessionTitle = (String) notificationsTableModel.getValueAt(row, 0);
        Session session = sessions.stream()
                .filter(s -> s.getTitle().equals(sessionTitle))
                .findFirst()
                .orElse(null);
        if (session != null) {
            Group sessionGroup = groups.stream()
                    .filter(g -> g.getId().equals(session.getGroupId()))
                    .findFirst()
                    .orElse(null);
            if (sessionGroup == null || !sessionGroup.getStudentIds().contains(student.getId())) {
                JOptionPane.showMessageDialog(this, "You are not enrolled in the group for this session.",
                        "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }
            session.addStudent(student.getId());
            SerializationUtil.saveDataToDisk(sessions, "sessions.ser");
            openSessionRoom(session);
            populateNotifications();
        }
    }

    // Modified: Students no longer can send messages.
    private void openSessionRoom(Session session) {
        // Wrap the session in an array so we can update it inside the inner class.
        final Session[] currentSession = new Session[] { session };

        JDialog sessionDialog = new JDialog(this, "Session: " + currentSession[0].getTitle(), true);
        sessionDialog.setSize(800, 600);
        sessionDialog.setLayout(new BorderLayout());

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        sessionDialog.add(chatScroll, BorderLayout.CENTER);

        // Timer to refresh the chat area every 3 seconds.
        new Timer().schedule(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    // Reload the session from disk
                    ArrayList<Session> sessionsList = SerializationUtil.readFromFile("sessions.ser");
                    if (sessionsList != null) {
                        for (Session s : sessionsList) {
                            if (s.getId().equals(currentSession[0].getId())) {
                                currentSession[0] = s;
                                break;
                            }
                        }
                    }
                    chatArea.setText("");
                    currentSession[0].getMessages()
                            .forEach(msg -> chatArea.append(msg.getSenderName() + ": " + msg.getContent() + "\n"));
                });
            }
        }, 0, 3000);

        sessionDialog.setVisible(true);
    }

    // -------------------- Test Taking Dialog (Inner Class) --------------------
    private class TestTakingDialog extends JDialog {
        private Test test;
        private ArrayList<Question> questions;
        private java.util.Map<String, Integer> answers;
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
            this.answers = new java.util.HashMap<>();
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
            displayQuestion(currentQuestionIndex);
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
                if (answers.containsKey(q.getId())) {
                    int selectedOption = answers.get(q.getId());
                    if (selectedOption >= 0 && selectedOption < optionButtons.length) {
                        optionButtons[selectedOption].setSelected(true);
                    }
                }
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
            saveCurrentAnswer();
            if (answers.size() < questions.size()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "You have unanswered questions. Do you still want to submit?", "Unanswered Questions",
                        JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            double score = 0.0;
            for (Question q : questions) {
                if (answers.containsKey(q.getId())) {
                    int selected = answers.get(q.getId());
                    if (selected == q.getCorrectOption()) {
                        score += 1.0;
                    }
                }
            }
            score = (score / questions.size()) * 100.0;
            String submissionTime = java.time.LocalDateTime.now().toString();
            TestResult tr = new TestResult(test.getId(), student.getId(), answers, score, submissionTime);
            testResults.add(tr);
            student.addTestResult(tr);
            saveTestResults();
            JOptionPane.showMessageDialog(this,
                    "Test submitted successfully!\nYour Score: " + String.format("%.2f", score) + "%", "Test Submitted",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }

        private void saveTestResults() {
            ArrayList<TestResult> allTestResults = SerializationUtil.readFromFile("testResults.ser");
            if (allTestResults == null) {
                allTestResults = new ArrayList<>();
            }
            allTestResults.addAll(testResults);
            SerializationUtil.saveDataToDisk(allTestResults, "testResults.ser");
            SerializationUtil.saveDataToDisk(UserStorage.getUsers(), "users.ser");
        }
    }

    // -------------------- Button Renderer & Editor for Notifications
    // --------------------
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setText("Join");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object obj, boolean selected, boolean focused,
                int row, int col) {
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.addActionListener(e -> {
                int row = notificationsTable.getSelectedRow();
                if (row >= 0) {
                    String action = (String) notificationsTable.getValueAt(row, 3);
                    if ("Join".equals(action)) {
                        joinSession(row);
                    } else {
                        String sessionTitle = (String) notificationsTable.getValueAt(row, 0);
                        Session session = sessions.stream()
                                .filter(s -> s.getTitle().equals(sessionTitle))
                                .findFirst()
                                .orElse(null);
                        if (session != null)
                            openSessionRoom(session);
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            button.setText((value == null) ? "" : value.toString());
            return button;
        }
    }

    // -------------------- Utility Methods --------------------
    private String getChapterNameById(String chapterId) {
        for (Chapter c : chapters) {
            if (c.getId().equals(chapterId))
                return c.getName();
        }
        return "Unknown";
    }

    private String getGroupNameById(String groupId) {
        for (Group g : groups) {
            if (g.getId().equals(groupId))
                return g.getGroupName();
        }
        return "Unknown";
    }

    private String getTeacherNameById(String teacherId) {
        for (Teacher t : teachers) {
            if (t.getId().equals(teacherId))
                return t.getName();
        }
        return "Unknown";
    }

    @Override
    public void dispose() {
        super.dispose();
        if (notificationRefreshTimer != null) {
            notificationRefreshTimer.cancel();
        }
    }

    public static void main(String[] args) {
        Student sampleStudent = new Student("s1a2b3c4-d5e6-f7g8-h9i0-0987654321", "Bob Smith", "bobPass", 100.0);
        List<User> allUsers = UserStorage.getUsers();
        boolean exists = allUsers.stream().anyMatch(u -> u.getId().equals(sampleStudent.getId()));
        if (!exists) {
            UserStorage.addUser(sampleStudent);
            SerializationUtil.saveDataToDisk(UserStorage.getUsers(), "users.ser");
        }
        new StudentDashboard(sampleStudent);
    }
}
