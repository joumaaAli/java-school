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
import model.user.Student;
import model.user.User;
import model.user.UserStorage;
import utils.IDGenerator;
import utils.SerializationUtil;
import utils.decorator.OrderedMultipleChoiceDecorator;
import utils.interpreter.BasicGradingRuleExpression;
import utils.interpreter.GradingRuleExpression;

public class StudentDashboard extends JFrame {
    private Student student;

    // Data lists
    private ArrayList<Subject> subjects;
    private ArrayList<Chapter> chapters;
    private ArrayList<Group> groups;
    private ArrayList<Material> materials;
    private ArrayList<Test> tests;
    private ArrayList<Session> sessions;
    private ArrayList<Teacher> teachers;

    // Test Results
    private ArrayList<TestResult> testResults;

    // GUI Components
    private JPanel viewMaterialsPanel;
    private JTable materialsTable;
    private DefaultTableModel materialsTableModel;
    private JButton refreshMaterialsButton;

    // Merged Tests Tab (combining "View Tests" and "Take Test")
    private JPanel testsPanel;
    private JTable testsTable;
    private DefaultTableModel testsTableModel;
    private JButton takeTestButton;

    private JPanel viewAllGroupsPanel;
    private JTable allGroupsTable;
    private DefaultTableModel allGroupsTableModel;
    private JButton refreshAllGroupsButton, enrollGroupButton, leaveGroupButton;

    private JPanel myGroupsPanel;
    private JTable myGroupsTable;
    private DefaultTableModel myGroupsTableModel;
    private JButton refreshMyGroupsButton;

    private JPanel payDuePanel;
    private JLabel currentBalanceLabel;
    private JTextField paymentAmountField;
    private JButton payButton;

    private JPanel viewTestResultsPanel;
    private JTable testResultsTable;
    private DefaultTableModel testResultsTableModel;
    private JButton refreshTestResultsButton;

    private JPanel sessionsPanel;
    private JTable sessionsTable;
    private DefaultTableModel sessionsTableModel;
    private JButton refreshSessionsButton, joinSessionButton;

    private Timer notificationRefreshTimer; // used only in student sessions refresh

    public StudentDashboard(Student student) {
        this.student = student;
        loadData();
        initComponents();
    }

    private void loadData() {
        subjects = SerializationUtil.readFromFile("subjects.txt");
        if (subjects == null) {
            subjects = new ArrayList<>();
        }
        chapters = SerializationUtil.readFromFile("chapters.txt");
        if (chapters == null) {
            chapters = new ArrayList<>();
        }
        groups = SerializationUtil.readFromFile("groups.txt");
        if (groups == null) {
            groups = new ArrayList<>();
        }
        materials = SerializationUtil.readFromFile("materials.txt");
        if (materials == null) {
            materials = new ArrayList<>();
        }
        tests = SerializationUtil.readFromFile("tests.txt");
        if (tests == null) {
            tests = new ArrayList<>();
        }
        sessions = SerializationUtil.readFromFile("sessions.txt");
        if (sessions == null) {
            sessions = new ArrayList<>();
        }
        List<User> allUsers = UserStorage.getUsers();
        teachers = allUsers.stream()
                .filter(u -> u instanceof Teacher)
                .map(u -> (Teacher) u)
                .collect(Collectors.toCollection(ArrayList::new));
        // Refresh the student instance from storage so that new notifications are
        // loaded.
        Student updatedStudent = (Student) UserStorage.getUserById(student.getId());
        if (updatedStudent != null) {
            student = updatedStudent;
        }
        testResults = (ArrayList<TestResult>) student.getTestResults();
    }

    private void initComponents() {
        setTitle("Student Dashboard - " + student.getName());
        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();

        initViewMaterialsTab();
        initTestsTab(); // Merged tab for viewing/taking tests
        initViewAllGroupsTab();
        initMyGroupsTab();
        initPayDuePanel();
        initViewTestResultsTab();
        initSessionsTab();

        tabbedPane.addTab("View Materials", viewMaterialsPanel);
        tabbedPane.addTab("Tests", testsPanel);
        tabbedPane.addTab("View Test Results", viewTestResultsPanel);
        tabbedPane.addTab("View All Groups", viewAllGroupsPanel);
        tabbedPane.addTab("My Groups", myGroupsPanel);
        tabbedPane.addTab("Pay Due Payment", payDuePanel);
        tabbedPane.addTab("Sessions", sessionsPanel);

        add(tabbedPane, BorderLayout.CENTER);
        // Removed initNotificationRefreshTimer() since notifications tab is removed.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setVisible(true);
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
        // Show only materials for chapters associated with groups the student is
        // enrolled in.
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

    // -------------------- Merged Tests Tab --------------------
    private void initTestsTab() {
        testsPanel = new JPanel(new BorderLayout());
        testsTableModel = new DefaultTableModel();
        testsTableModel.addColumn("Test ID");
        testsTableModel.addColumn("Title");
        testsTableModel.addColumn("Start Time");
        testsTableModel.addColumn("Duration (mins)");
        testsTableModel.addColumn("Chapter");
        populateTestsTable();
        testsTable = new JTable(testsTableModel);
        JScrollPane scrollPane = new JScrollPane(testsTable);
        testsPanel.add(scrollPane, BorderLayout.CENTER);
        takeTestButton = new JButton("Take Selected Test");
        takeTestButton.addActionListener(e -> takeSelectedTest());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(takeTestButton);
        testsPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    // This method populates only tests that the student has not taken.
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
        // Exclude tests that have already been taken
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

    private void takeSelectedTest() {
        int selectedRow = testsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a test to take.", "No Test Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String testId = (String) testsTableModel.getValueAt(selectedRow, 0);
        Test selectedTest = tests.stream()
                .filter(t -> t.getId().equals(testId))
                .findFirst().orElse(null);
        if (selectedTest == null) {
            JOptionPane.showMessageDialog(this, "Selected test not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        TestTakingDialog testDialog = new TestTakingDialog(this, selectedTest);
        testDialog.setVisible(true);
        populateTestsTable(); // refresh after test submission
        populateTestResultsTable();
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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshAllGroupsButton = new JButton("Refresh");
        enrollGroupButton = new JButton("Enroll in Selected Group");
        leaveGroupButton = new JButton("Leave Selected Group");
        buttonPanel.add(refreshAllGroupsButton);
        buttonPanel.add(enrollGroupButton);
        buttonPanel.add(leaveGroupButton);
        viewAllGroupsPanel.add(buttonPanel, BorderLayout.SOUTH);
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
                .findFirst().orElse(null);
        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Selected group not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        selectedGroup.addStudent(student.getId());
        student.enrollGroup(groupId);
        SerializationUtil.saveDataToDisk(groups, "groups.txt");
        SerializationUtil.saveDataToDisk(UserStorage.getUsers(), "users.txt");
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
                .findFirst().orElse(null);
        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Selected group not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to leave Group ID: " + groupId + "?",
                "Confirm Leave", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            selectedGroup.removeStudent(student.getId());
            student.leaveGroup(groupId);
            SerializationUtil.saveDataToDisk(groups, "groups.txt");
            SerializationUtil.saveDataToDisk(UserStorage.getUsers(), "users.txt");
            populateAllGroupsTable();
            populateMyGroupsTable();
            JOptionPane.showMessageDialog(this, "You have left the group successfully.", "Left Group",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshMyGroupsButton = new JButton("Refresh");
        buttonPanel.add(refreshMyGroupsButton);
        refreshMyGroupsButton.addActionListener(e -> populateMyGroupsTable());
        myGroupsPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void populateMyGroupsTable() {
        myGroupsTableModel.setRowCount(0);
        ArrayList<Group> myGroups = groups.stream()
                .filter(g -> g.getStudentIds().contains(student.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
        for (Group g : myGroups) {
            String chapterName = getChapterNameById(g.getChapterId());
            String teachersNames = g.getTeacherIds().stream()
                    .map(id -> getTeacherNameById(id))
                    .collect(Collectors.joining("; "));
            myGroupsTableModel.addRow(new Object[] { g.getId(), g.getGroupName(), chapterName, teachersNames });
        }
    }

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
                    .findFirst().orElse(null);
            String testTitle = (test != null) ? test.getTitle() : "Unknown";
            testResultsTableModel.addRow(new Object[] { tr.getTestId(), testTitle, String.format("%.2f", tr.getScore()),
                    tr.getSubmissionTime() });
        }
    }

    private void initSessionsTab() {
        sessionsPanel = new JPanel(new BorderLayout());
        sessionsTableModel = new DefaultTableModel(
                new Object[] { "Session ID", "Title", "DateTime", "Group", "Participants" }, 0);
        sessionsTable = new JTable(sessionsTableModel);
        JScrollPane tableScroll = new JScrollPane(sessionsTable);
        sessionsPanel.add(tableScroll, BorderLayout.CENTER);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshSessionsButton = new JButton("Refresh Sessions");
        joinSessionButton = new JButton("Join Session");
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
                .findFirst().orElse(null);
        if (session == null) {
            JOptionPane.showMessageDialog(this, "Selected session not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!session.getStudentIds().contains(student.getId())) {
            session.addStudent(student.getId());
            SerializationUtil.saveDataToDisk(sessions, "sessions.txt");
        }
        openSessionRoom(session);
    }

    private void openSessionRoom(Session session) {
        final Session[] currentSession = new Session[] { session };
        JDialog sessionDialog = new JDialog(this, "Session: " + currentSession[0].getTitle(), true);
        sessionDialog.setSize(800, 600);
        sessionDialog.setLayout(new BorderLayout());
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        sessionDialog.add(chatScroll, BorderLayout.CENTER);
        new Timer().schedule(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    ArrayList<Session> sessionsList = SerializationUtil.readFromFile("sessions.txt");
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
        private JButton nextButton, submitButton;

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
            GradingRuleExpression basicRule = new BasicGradingRuleExpression();
            GradingRuleExpression ruleWithDecorator = new OrderedMultipleChoiceDecorator(basicRule);
            for (Question q : questions) {
                if (answers.containsKey(q.getId())) {
                    int selected = answers.get(q.getId());
                    score += ruleWithDecorator.evaluate(q, selected);
                }
            }
            score = (score / questions.size()) * 100.0;
            String submissionTime = java.time.LocalDateTime.now().toString();
            TestResult tr = new TestResult(test.getId(), student.getId(), answers, score, submissionTime);
            test.addTestResult(tr);
            student.addTestResult(tr);
            saveTestResult(tr);
            JOptionPane.showMessageDialog(this,
                    "Test submitted successfully!\nYour Score: " + String.format("%.2f", score) + "%",
                    "Test Submitted", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }

        private void saveTestResult(TestResult tr) {
            ArrayList<TestResult> allTestResults = SerializationUtil.readFromFile("testResults.txt");
            if (allTestResults == null) {
                allTestResults = new ArrayList<>();
            }
            allTestResults.add(tr);
            SerializationUtil.saveDataToDisk(allTestResults, "testResults.txt");
        }
    }

    // Utility methods to look up chapter, group, and teacher names.
    private String getChapterNameById(String chapterId) {
        for (Chapter c : chapters) {
            if (c.getId().equals(chapterId)) {
                return c.getName();
            }
        }
        return "Unknown";
    }

    private String getGroupNameById(String groupId) {
        for (Group g : groups) {
            if (g.getId().equals(groupId)) {
                return g.getGroupName();
            }
        }
        return "Unknown";
    }

    private String getTeacherNameById(String teacherId) {
        for (Teacher t : teachers) {
            if (t.getId().equals(teacherId)) {
                return t.getName();
            }
        }
        return "Unknown";
    }

    public static void main(String[] args) {
        Student sampleStudent = new Student("s1a2b3c4-d5e6-f7g8-h9i0-0987654321", "Bob Smith", "bobPass", 100.0);
        List<User> allUsers = UserStorage.getUsers();
        boolean exists = allUsers.stream().anyMatch(u -> u.getId().equals(sampleStudent.getId()));
        if (!exists) {
            UserStorage.addUser(sampleStudent);
            SerializationUtil.saveDataToDisk(UserStorage.getUsers(), "users.txt");
        }
        new StudentDashboard(sampleStudent);
    }
}
