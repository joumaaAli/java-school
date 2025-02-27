package com.mycompany.school.project;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.Timer;

import model.subject.*;
import model.user.Teacher;
import model.user.Student;
import model.user.User;
import model.user.UserStorage;
import utils.IDGenerator;
import utils.SerializationUtil;
import utils.memento.TestCaretaker;
import utils.memento.TestMemento;
import utils.memento.TestOriginator;
import utils.template.StandardExamProcessor;
import utils.observer.SessionObserver;

public class TeacherDashboard extends JFrame implements SessionObserver {
    private Teacher teacher;

    // Data Lists
    private List<Subject> subjects;
    private List<Chapter> chapters;
    private ArrayList<Group> groups;
    private ArrayList<Material> materials;
    private ArrayList<Test> tests;
    private ArrayList<Session> sessions;
    private List<Teacher> teachers;
    private List<Student> students;
    // Test Results
    private List<TestResult> allTestResults;

    // -------------------- GUI Components for Various Tabs --------------------
    // Manage Groups Tab
    private JPanel manageGroupsPanel;
    private JTable groupsTable;
    private DefaultTableModel groupsTableModel;
    private JButton refreshGroupsButton;
    private JButton deleteGroupButton;
    private JButton assignTeacherButton;
    private JButton removeTeacherButton;

    // My Groups Tab
    private JPanel myGroupsPanel;
    private JTable myGroupsTable;
    private DefaultTableModel myGroupsTableModel;
    private JButton refreshMyGroupsButton;

    // Create Material Tab
    private JPanel createMaterialPanel;
    private JTextField materialTitleField;
    private JComboBox<String> materialTypeComboBox;
    private JTextField materialContentField;
    private JButton addMaterialButton;
    private JComboBox<Chapter> chapterComboBox;

    // Create Test Tab
    private JPanel createTestPanel;
    private JButton addTestButton;

    // Create Session Tab
    private JPanel createSessionPanel;
    private JTextField sessionTitleField;
    private JTextField sessionDateTimeField;
    private JComboBox<Group> sessionGroupComboBox;
    private JButton addSessionButton;

    // View Test Results Tab
    private JPanel viewTestResultsPanel;
    private JTable testResultsTable;
    private DefaultTableModel testResultsTableModel;
    private JComboBox<Test> testSelectionComboBox;
    private JButton viewTestResultsButton;

    // Sessions Tab
    private JPanel sessionsPanel;
    private JTable sessionsTable;
    private DefaultTableModel sessionsTableModel;
    private JButton refreshSessionsButton;
    private JButton joinSessionButton;
    private JButton endSessionButton;
    private JTextField sessionMessageField;
    private JTextArea sessionChatArea;

    // Process Exam Tab
    private JPanel processExamPanel;
    private JComboBox<Test> processExamComboBox;
    private JButton processExamButton;
    // NEW: Table to display processed exam results
    private JTable processExamTable;
    private DefaultTableModel processExamTableModel;

    // Notifications Tab
    private JPanel notificationsPanel;
    private DefaultListModel<String> notificationsListModel;
    private JList<String> notificationsList;

    // List of chapters to which the teacher is assigned
    private List<Chapter> teacherChapters;

    // -------------------- NEW: Edit Test Tab (with Undo/Redo using Memento)
    private JPanel editTestPanel;
    private JComboBox<Test> editTestComboBox;
    private JTextField editTestTitleField;
    private JTextField editTestStartTimeField;
    private JTextField editTestDurationField;
    private JButton saveTestChangesButton;
    private JButton undoButton;
    private JButton redoButton;

    // Memento fields for test editing
    private TestOriginator testOriginator;
    private TestCaretaker testCaretaker;

    public TeacherDashboard(Teacher teacher) {
        this.teacher = teacher;
        loadData();
        // Initialize memento support for test editing.
        testOriginator = new TestOriginator();
        testCaretaker = new TestCaretaker();
        initComponents();
    }

    private void loadData() {
        subjects = SerializationUtil.readFromFile("subjects.txt");
        if (subjects == null) {
            subjects = new ArrayList<>();
            System.out.println("No existing subjects found.");
        }
        chapters = SerializationUtil.readFromFile("chapters.txt");
        if (chapters == null) {
            chapters = new ArrayList<>();
            System.out.println("No existing chapters found.");
        }
        ArrayList<Group> loadedGroups = SerializationUtil.readFromFile("groups.txt");
        if (loadedGroups == null) {
            groups = new ArrayList<>();
            System.out.println("No existing groups found.");
        } else {
            groups = loadedGroups;
            System.out.println("Loaded " + groups.size() + " groups.");
        }
        materials = SerializationUtil.readFromFile("materials.txt");
        if (materials == null) {
            materials = new ArrayList<>();
            System.out.println("No existing materials found.");
        }
        tests = SerializationUtil.readFromFile("tests.txt");
        if (tests == null) {
            tests = new ArrayList<>();
            System.out.println("No existing tests found.");
        } else {
            System.out.println("Loaded " + tests.size() + " tests from tests.txt");
        }
        sessions = SerializationUtil.readFromFile("sessions.txt");
        if (sessions == null) {
            sessions = new ArrayList<>();
            System.out.println("No existing sessions found.");
        }
        List<User> allUsers = UserStorage.getUsers();
        teachers = allUsers.stream()
                .filter(u -> u instanceof Teacher)
                .map(u -> (Teacher) u)
                .collect(Collectors.toList());
        students = allUsers.stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .collect(Collectors.toList());
        allTestResults = SerializationUtil.readFromFile("testResults.txt");
        if (allTestResults == null) {
            allTestResults = new ArrayList<>();
            System.out.println("No existing test results found.");
        }
        teacherChapters = groups.stream()
                .filter(g -> g.getTeacherIds() != null && g.getTeacherIds().contains(teacher.getId()))
                .map(Group::getChapterId)
                .distinct()
                .map(chapterId -> chapters.stream()
                        .filter(c -> c.getId().equals(chapterId))
                        .findFirst()
                        .orElse(null))
                .filter(c -> c != null)
                .collect(Collectors.toList());
        System.out.println("Chapters assigned to Teacher (" + teacher.getName() + "): " +
                teacherChapters.stream().map(Chapter::getName).collect(Collectors.joining(", ")));
    }

    private void initComponents() {
        setTitle("Teacher Dashboard - " + teacher.getName());
        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();

        // Initialize each tab (removed View Tests tab)
        initManageGroupsTab();
        initMyGroupsTab();
        initCreateMaterialTab();
        initCreateTestTab();
        initCreateSessionTab();
        initViewTestResultsTab();
        initSessionsTab();
        initProcessExamTab();
        initNotificationsTab();
        initEditTestTab(); // NEW Edit Test tab with undo/redo support

        tabbedPane.addTab("Manage Groups", manageGroupsPanel);
        tabbedPane.addTab("My Groups", myGroupsPanel);
        tabbedPane.addTab("Create Material", createMaterialPanel);
        tabbedPane.addTab("Create Test", createTestPanel);
        tabbedPane.addTab("Create Session", createSessionPanel);
        tabbedPane.addTab("View Test Results", viewTestResultsPanel);
        tabbedPane.addTab("Sessions", sessionsPanel);
        tabbedPane.addTab("Process Exam", processExamPanel);
        tabbedPane.addTab("Notifications", notificationsPanel);
        tabbedPane.addTab("Edit Test", editTestPanel);

        // Add a ChangeListener so that when "View Test Results" is selected, the tests
        // dropdown is refreshed.
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JTabbedPane source = (JTabbedPane) e.getSource();
                Component selected = source.getSelectedComponent();
                if (selected == viewTestResultsPanel) {
                    updateTestSelectionComboBox();
                }
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 1000);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // -------------------- Manage Groups Tab --------------------
    private void initManageGroupsTab() {
        manageGroupsPanel = new JPanel(new BorderLayout());
        groupsTableModel = new DefaultTableModel();
        groupsTableModel.addColumn("Group ID");
        groupsTableModel.addColumn("Group Name");
        groupsTableModel.addColumn("Chapter");
        groupsTableModel.addColumn("Teachers");
        groupsTableModel.addColumn("Enrollees");
        groupsTableModel.addColumn("Assigned to Me");
        populateGroupsTable();
        groupsTable = new JTable(groupsTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                Boolean assigned = (Boolean) getValueAt(row, 5);
                if (assigned != null && assigned) {
                    comp.setBackground(new Color(204, 255, 204));
                } else {
                    comp.setBackground(new Color(255, 204, 255));
                }
                return comp;
            }
        };
        JScrollPane scrollPane = new JScrollPane(groupsTable);
        manageGroupsPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshGroupsButton = new JButton("Refresh");
        deleteGroupButton = new JButton("Delete Selected Group");
        assignTeacherButton = new JButton("Assign Myself to Selected Group");
        removeTeacherButton = new JButton("Remove Myself from Selected Group");
        buttonsPanel.add(refreshGroupsButton);
        buttonsPanel.add(assignTeacherButton);
        buttonsPanel.add(removeTeacherButton);
        buttonsPanel.add(deleteGroupButton);
        manageGroupsPanel.add(buttonsPanel, BorderLayout.SOUTH);
        refreshGroupsButton.addActionListener(e -> populateGroupsTable());
        deleteGroupButton.addActionListener(e -> deleteSelectedGroup());
        assignTeacherButton.addActionListener(e -> assignTeacherToGroup());
        removeTeacherButton.addActionListener(e -> removeTeacherFromGroup());
    }

    private void populateGroupsTable() {
        groupsTableModel.setRowCount(0);
        for (Group g : groups) {
            String chapterName = getChapterNameById(g.getChapterId());
            String teachersNames = (g.getTeacherIds() != null)
                    ? g.getTeacherIds().stream().map(this::getTeacherNameById).collect(Collectors.joining("; "))
                    : "";
            int enrollees = (g.getStudentIds() != null) ? g.getStudentIds().size() : 0;
            boolean assignedToMe = (g.getTeacherIds() != null && g.getTeacherIds().contains(teacher.getId()));
            groupsTableModel.addRow(new Object[] {
                    g.getId(), g.getGroupName(), chapterName, teachersNames, enrollees, assignedToMe
            });
        }
    }

    private void deleteSelectedGroup() {
        int selectedRow = groupsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a group to delete.", "No Group Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String groupId = (String) groupsTableModel.getValueAt(selectedRow, 0);
        boolean assignedToMe = (Boolean) groupsTableModel.getValueAt(selectedRow, 5);
        if (assignedToMe) {
            JOptionPane.showMessageDialog(this, "You cannot delete a group you are assigned to.",
                    "Deletion Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete Group ID: " + groupId + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            groups.removeIf(g -> g.getId().equals(groupId));
            SerializationUtil.saveDataToDisk(groups, "groups.txt");
            populateGroupsTable();
            JOptionPane.showMessageDialog(this, "Group deleted successfully.", "Deletion Successful",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void assignTeacherToGroup() {
        int selectedRow = groupsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a group to assign yourself.", "No Group Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String groupId = (String) groupsTableModel.getValueAt(selectedRow, 0);
        Group selectedGroup = groups.stream().filter(g -> g.getId().equals(groupId)).findFirst().orElse(null);
        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Selected group not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (selectedGroup.getTeacherIds() != null && selectedGroup.getTeacherIds().contains(teacher.getId())) {
            JOptionPane.showMessageDialog(this, "You are already assigned to this group.", "Already Assigned",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        selectedGroup.addTeacher(teacher.getId());
        SerializationUtil.saveDataToDisk(groups, "groups.txt");
        populateGroupsTable();
        populateMyGroupsTable();
        JOptionPane.showMessageDialog(this, "You have been assigned to the selected group.", "Assignment Successful",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void removeTeacherFromGroup() {
        int selectedRow = groupsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a group.", "No Group Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String groupId = (String) groupsTableModel.getValueAt(selectedRow, 0);
        Group selectedGroup = groups.stream().filter(g -> g.getId().equals(groupId)).findFirst().orElse(null);
        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Selected group not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (selectedGroup.getTeacherIds() == null || !selectedGroup.getTeacherIds().contains(teacher.getId())) {
            JOptionPane.showMessageDialog(this, "You are not assigned to this group.", "Not Assigned",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Remove yourself from Group ID: " + groupId + "?",
                "Confirm Removal", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            selectedGroup.removeTeacher(teacher.getId());
            SerializationUtil.saveDataToDisk(groups, "groups.txt");
            populateGroupsTable();
            populateMyGroupsTable();
            JOptionPane.showMessageDialog(this, "You have been removed from the group.", "Removal Successful",
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
        myGroupsTableModel.addColumn("Enrollees");
        myGroupsTableModel.addColumn("Teachers");
        populateMyGroupsTable();
        myGroupsTable = new JTable(myGroupsTableModel);
        JScrollPane scrollPane = new JScrollPane(myGroupsTable);
        myGroupsPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshMyGroupsButton = new JButton("Refresh");
        buttonsPanel.add(refreshMyGroupsButton);
        myGroupsPanel.add(buttonsPanel, BorderLayout.SOUTH);
        refreshMyGroupsButton.addActionListener(e -> populateMyGroupsTable());
    }

    private void populateMyGroupsTable() {
        myGroupsTableModel.setRowCount(0);
        List<Group> myGroups = groups.stream()
                .filter(g -> g.getTeacherIds() != null && g.getTeacherIds().contains(teacher.getId()))
                .collect(Collectors.toList());
        for (Group g : myGroups) {
            String chapterName = getChapterNameById(g.getChapterId());
            int enrollees = (g.getStudentIds() != null) ? g.getStudentIds().size() : 0;
            String teachersNames = (g.getTeacherIds() != null)
                    ? g.getTeacherIds().stream().map(this::getTeacherNameById).collect(Collectors.joining("; "))
                    : "";
            myGroupsTableModel
                    .addRow(new Object[] { g.getId(), g.getGroupName(), chapterName, enrollees, teachersNames });
        }
    }

    // -------------------- Create Material Tab --------------------
    private void initCreateMaterialTab() {
        createMaterialPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel titleLabel = new JLabel("Material Title:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        createMaterialPanel.add(titleLabel, gbc);
        materialTitleField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        createMaterialPanel.add(materialTitleField, gbc);
        JLabel typeLabel = new JLabel("Material Type:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        createMaterialPanel.add(typeLabel, gbc);
        String[] types = { "video", "image", "pdf" };
        materialTypeComboBox = new JComboBox<>(types);
        gbc.gridx = 1;
        gbc.gridy = 1;
        createMaterialPanel.add(materialTypeComboBox, gbc);
        JLabel contentLabel = new JLabel("Content Path/URL:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        createMaterialPanel.add(contentLabel, gbc);
        materialContentField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 2;
        createMaterialPanel.add(materialContentField, gbc);
        JLabel chapterLabel = new JLabel("Assign to Chapter:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        createMaterialPanel.add(chapterLabel, gbc);
        if (!teacherChapters.isEmpty()) {
            chapterComboBox = new JComboBox<>(teacherChapters.toArray(new Chapter[0]));
        } else {
            chapterComboBox = new JComboBox<>();
            chapterComboBox.addItem(new Chapter("N/A", "N/A", "N/A", "No Assigned Chapters"));
            chapterComboBox.setEnabled(false);
        }
        gbc.gridx = 1;
        gbc.gridy = 3;
        createMaterialPanel.add(chapterComboBox, gbc);
        addMaterialButton = new JButton("Add Material");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        createMaterialPanel.add(addMaterialButton, gbc);
        addMaterialButton.addActionListener(e -> addMaterial());
    }

    private void addMaterial() {
        String title = materialTitleField.getText().trim();
        String type = (String) materialTypeComboBox.getSelectedItem();
        String content = materialContentField.getText().trim();
        Chapter selectedChapter = (Chapter) chapterComboBox.getSelectedItem();
        if (title.isEmpty() || type.isEmpty() || content.isEmpty() || selectedChapter == null
                || selectedChapter.getId().equals("N/A")) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields and ensure you have assigned chapters.",
                    "Incomplete Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Material material = new Material(IDGenerator.generateID(), title, type, content, selectedChapter.getId());
        materials.add(material);
        SerializationUtil.saveDataToDisk(materials, "materials.txt");
        JOptionPane.showMessageDialog(this, "Material added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        materialTitleField.setText("");
        materialContentField.setText("");
    }

    // -------------------- Create Test Tab --------------------
    private void initCreateTestTab() {
        createTestPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addTestButton = new JButton("Create New Test");
        topPanel.add(addTestButton);
        createTestPanel.add(topPanel, BorderLayout.NORTH);
        DefaultTableModel testTableModel = new DefaultTableModel();
        testTableModel.addColumn("Test ID");
        testTableModel.addColumn("Title");
        testTableModel.addColumn("Chapter");
        testTableModel.addColumn("Start Time");
        testTableModel.addColumn("Duration (mins)");
        testTableModel.addColumn("Number of Questions");
        JTable testTable = new JTable(testTableModel);
        JScrollPane scrollPane = new JScrollPane(testTable);
        createTestPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshTestButton = new JButton("Refresh");
        bottomPanel.add(refreshTestButton);
        createTestPanel.add(bottomPanel, BorderLayout.SOUTH);
        populateTestTable(testTableModel);
        addTestButton.addActionListener(e -> openCreateTestDialog(testTableModel));
        refreshTestButton.addActionListener(e -> populateTestTable(testTableModel));
    }

    private void populateTestTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Test t : tests) {
            String chapterName = getChapterNameById(t.getChapterId());
            int numQuestions = t.getQuestions().size();
            model.addRow(new Object[] { t.getId(), t.getTitle(), chapterName, t.getStartTime(), t.getDuration(),
                    numQuestions });
        }
    }

    private void openCreateTestDialog(DefaultTableModel testTableModel) {
        JDialog dialog = new JDialog(this, "Create New Test", true);
        dialog.setSize(600, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel titleLabel = new JLabel("Test Title:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(titleLabel, gbc);
        JTextField testTitleField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(testTitleField, gbc);
        JLabel startTimeLabel = new JLabel("Start Time (e.g., 2025-02-01 10:00):");
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(startTimeLabel, gbc);
        JTextField testStartTimeField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(testStartTimeField, gbc);
        JLabel durationLabel = new JLabel("Duration (minutes):");
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(durationLabel, gbc);
        JTextField testDurationField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(testDurationField, gbc);
        JLabel chapterLabel = new JLabel("Assign to Chapter:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(chapterLabel, gbc);
        JComboBox<Chapter> testChapterComboBox;
        if (!teacherChapters.isEmpty()) {
            testChapterComboBox = new JComboBox<>(teacherChapters.toArray(new Chapter[0]));
        } else {
            testChapterComboBox = new JComboBox<>();
            testChapterComboBox.addItem(new Chapter("N/A", "N/A", "N/A", "No Assigned Chapters"));
            testChapterComboBox.setEnabled(false);
        }
        gbc.gridx = 1;
        gbc.gridy = 3;
        formPanel.add(testChapterComboBox, gbc);
        JPanel questionsPanel = new JPanel(new BorderLayout());
        questionsPanel.setBorder(BorderFactory.createTitledBorder("Questions"));
        DefaultListModel<Question> questionListModel = new DefaultListModel<>();
        JList<Question> questionJList = new JList<>(questionListModel);
        questionJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane questionScrollPane = new JScrollPane(questionJList);
        questionsPanel.add(questionScrollPane, BorderLayout.CENTER);
        JButton addQuestionButton = new JButton("Add Question");
        JButton removeQuestionButton = new JButton("Remove Selected Question");
        JPanel questionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        questionButtonsPanel.add(addQuestionButton);
        questionButtonsPanel.add(removeQuestionButton);
        questionsPanel.add(questionButtonsPanel, BorderLayout.SOUTH);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(questionsPanel, gbc);
        JButton saveTestButton = new JButton("Save Test");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        formPanel.add(saveTestButton, gbc);
        dialog.add(formPanel, BorderLayout.CENTER);
        addQuestionButton.addActionListener(e -> {
            JPanel questionDialogPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            JTextField questionTextField = new JTextField();
            JTextField option1Field = new JTextField();
            JTextField option2Field = new JTextField();
            JTextField option3Field = new JTextField();
            JTextField option4Field = new JTextField();
            JTextField correctOptionField = new JTextField();
            questionDialogPanel.add(new JLabel("Question Text:"));
            questionDialogPanel.add(questionTextField);
            questionDialogPanel.add(new JLabel("Option 1:"));
            questionDialogPanel.add(option1Field);
            questionDialogPanel.add(new JLabel("Option 2:"));
            questionDialogPanel.add(option2Field);
            questionDialogPanel.add(new JLabel("Option 3:"));
            questionDialogPanel.add(option3Field);
            questionDialogPanel.add(new JLabel("Option 4:"));
            questionDialogPanel.add(option4Field);
            questionDialogPanel.add(new JLabel("Correct Option Number (1-4):"));
            questionDialogPanel.add(correctOptionField);
            int result = JOptionPane.showConfirmDialog(dialog, questionDialogPanel, "Add New Question",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String questionText = questionTextField.getText().trim();
                String opt1 = option1Field.getText().trim();
                String opt2 = option2Field.getText().trim();
                String opt3 = option3Field.getText().trim();
                String opt4 = option4Field.getText().trim();
                String correctOptStr = correctOptionField.getText().trim();
                if (questionText.isEmpty() || opt1.isEmpty() || opt2.isEmpty() || opt3.isEmpty() || opt4.isEmpty()
                        || correctOptStr.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "All fields must be filled.", "Incomplete Information",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int correctOption;
                try {
                    correctOption = Integer.parseInt(correctOptStr) - 1;
                    if (correctOption < 0 || correctOption > 3) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Correct option must be a number between 1 and 4.",
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String[] options = { opt1, opt2, opt3, opt4 };
                Question newQuestion = new Question(IDGenerator.generateID(), "temp-test-id", questionText, options,
                        correctOption);
                questionListModel.addElement(newQuestion);
            }
        });
        removeQuestionButton.addActionListener(e -> {
            int selectedIndex = questionJList.getSelectedIndex();
            if (selectedIndex != -1) {
                questionListModel.remove(selectedIndex);
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a question to remove.", "No Question Selected",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        saveTestButton.addActionListener(e -> {
            String title = testTitleField.getText().trim();
            String startTime = testStartTimeField.getText().trim();
            String durationStr = testDurationField.getText().trim();
            Chapter selectedChapter = (Chapter) testChapterComboBox.getSelectedItem();
            if (title.isEmpty() || startTime.isEmpty() || durationStr.isEmpty() || selectedChapter == null
                    || selectedChapter.getId().equals("N/A")) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields and ensure you have assigned chapters.",
                        "Incomplete Information", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int duration;
            try {
                duration = Integer.parseInt(durationStr);
                if (duration <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid duration.", "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (questionListModel.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please add at least one question.", "No Questions Added",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Test newTest = new Test(IDGenerator.generateID(), selectedChapter.getId(), title, startTime, duration);
            for (int i = 0; i < questionListModel.size(); i++) {
                Question q = questionListModel.getElementAt(i);
                q.setTestId(newTest.getId());
                newTest.addQuestion(q);
            }
            tests.add(newTest);
            SerializationUtil.saveDataToDisk(tests, "tests.txt");
            populateTestTable(testTableModel);
            JOptionPane.showMessageDialog(dialog, "Test and questions added successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        dialog.setVisible(true);
    }

    // -------------------- Create Session Tab --------------------
    private void initCreateSessionTab() {
        createSessionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel titleLabel = new JLabel("Session Title:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        createSessionPanel.add(titleLabel, gbc);
        sessionTitleField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        createSessionPanel.add(sessionTitleField, gbc);
        JLabel dateTimeLabel = new JLabel("Date and Time (e.g., 2025-02-01 10:00):");
        gbc.gridx = 0;
        gbc.gridy = 1;
        createSessionPanel.add(dateTimeLabel, gbc);
        sessionDateTimeField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        createSessionPanel.add(sessionDateTimeField, gbc);
        JLabel groupLabel = new JLabel("Select Group (only groups assigned to you):");
        gbc.gridx = 0;
        gbc.gridy = 2;
        createSessionPanel.add(groupLabel, gbc);
        List<Group> teacherGroups = groups.stream()
                .filter(g -> g.getTeacherIds() != null && g.getTeacherIds().contains(teacher.getId()))
                .collect(Collectors.toList());
        if (teacherGroups.isEmpty()) {
            sessionGroupComboBox = new JComboBox<>(new Group[] {});
            sessionGroupComboBox.setEnabled(false);
            addSessionButton = new JButton("Add Session");
            addSessionButton.setEnabled(false);
            JOptionPane.showMessageDialog(this, "You are not assigned to any groups. Session creation disabled.",
                    "No Groups", JOptionPane.INFORMATION_MESSAGE);
        } else {
            sessionGroupComboBox = new JComboBox<>(teacherGroups.toArray(new Group[0]));
        }
        gbc.gridx = 1;
        gbc.gridy = 2;
        createSessionPanel.add(sessionGroupComboBox, gbc);
        addSessionButton = new JButton("Add Session");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        createSessionPanel.add(addSessionButton, gbc);
        addSessionButton.addActionListener(e -> addSession());
    }

    private void addSession() {
        String title = sessionTitleField.getText().trim();
        String dateTime = sessionDateTimeField.getText().trim();
        Group selectedGroup = (Group) sessionGroupComboBox.getSelectedItem();
        if (title.isEmpty() || dateTime.isEmpty() || selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Incomplete Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!selectedGroup.getTeacherIds().contains(teacher.getId())) {
            JOptionPane.showMessageDialog(this, "You cannot create a session in a group you are not assigned to.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Session session = teacher.createSession(title, dateTime, selectedGroup.getChapterId(), selectedGroup.getId());
        session.addTeacher(teacher.getId());
        sessions.add(session);
        SerializationUtil.saveDataToDisk(sessions, "sessions.txt");
        JOptionPane.showMessageDialog(this, "Session added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        sessionTitleField.setText("");
        sessionDateTimeField.setText("");
    }

    // -------------------- View Test Results Tab --------------------
    private void initViewTestResultsTab() {
        viewTestResultsPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel selectTestLabel = new JLabel("Select Test:");
        testSelectionComboBox = new JComboBox<>(tests.toArray(new Test[0]));
        viewTestResultsButton = new JButton("View Results");
        topPanel.add(selectTestLabel);
        topPanel.add(testSelectionComboBox);
        topPanel.add(viewTestResultsButton);
        viewTestResultsPanel.add(topPanel, BorderLayout.NORTH);
        testResultsTableModel = new DefaultTableModel();
        testResultsTableModel.addColumn("Student ID");
        testResultsTableModel.addColumn("Student Name");
        testResultsTableModel.addColumn("Score");
        testResultsTableModel.addColumn("Submission Time");
        testResultsTable = new JTable(testResultsTableModel);
        JScrollPane scrollPane = new JScrollPane(testResultsTable);
        viewTestResultsPanel.add(scrollPane, BorderLayout.CENTER);
        viewTestResultsButton.addActionListener(e -> viewSelectedTestResults());
    }

    private void viewSelectedTestResults() {
        tests = SerializationUtil.readFromFile("tests.txt");
        allTestResults = SerializationUtil.readFromFile("testResults.txt");
        if (allTestResults == null) {
            allTestResults = new ArrayList<>();
        }
        Test selectedTest = (Test) testSelectionComboBox.getSelectedItem();
        if (selectedTest == null) {
            JOptionPane.showMessageDialog(this, "Please select a test to view results.", "No Test Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        testResultsTableModel.setRowCount(0);
        List<TestResult> selectedTestResults = allTestResults.stream()
                .filter(tr -> tr.getTestId().equals(selectedTest.getId()))
                .collect(Collectors.toList());
        for (TestResult tr : selectedTestResults) {
            Student stu = students.stream().filter(s -> s.getId().equals(tr.getStudentId())).findFirst().orElse(null);
            String studentName = (stu != null) ? stu.getName() : "Unknown";
            testResultsTableModel.addRow(new Object[] { tr.getStudentId(), studentName,
                    String.format("%.2f", tr.getScore()), tr.getSubmissionTime() });
        }
        if (selectedTestResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results found for the selected test.", "No Results",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // This method refreshes the tests in the dropdown.
    private void updateTestSelectionComboBox() {
        tests = SerializationUtil.readFromFile("tests.txt"); // re-read tests
        testSelectionComboBox.removeAllItems();
        for (Test t : tests) {
            testSelectionComboBox.addItem(t);
        }
    }

    // -------------------- Sessions Tab --------------------
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
        endSessionButton = new JButton("End Session");
        topPanel.add(refreshSessionsButton);
        topPanel.add(joinSessionButton);
        topPanel.add(endSessionButton);
        sessionsPanel.add(topPanel, BorderLayout.NORTH);
        refreshSessionsButton.addActionListener(e -> populateSessionsTable());
        joinSessionButton.addActionListener(e -> joinSelectedSession());
        endSessionButton.addActionListener(e -> endSelectedSession());
        sessionsTable.getSelectionModel().addListSelectionListener(e -> displaySessionDetails());
        new Timer().schedule(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    sessions = SerializationUtil.readFromFile("sessions.txt");
                    if (sessions == null) {
                        sessions = new ArrayList<>();
                    }
                    populateSessionsTable();
                    displaySessionDetails();
                });
            }
        }, 0, 5000);
    }

    private void populateSessionsTable() {
        int selectedRow = sessionsTable.getSelectedRow();
        String selectedSessionId = null;
        if (selectedRow != -1) {
            selectedSessionId = (String) sessionsTableModel.getValueAt(selectedRow, 0);
        }
        sessionsTableModel.setRowCount(0);
        for (Session s : sessions) {
            String groupName = getGroupNameById(s.getGroupId());
            int teacherCount = (s.getTeacherIds() != null) ? s.getTeacherIds().size() : 0;
            int studentCount = (s.getStudentIds() != null) ? s.getStudentIds().size() : 0;
            String participants = "Teachers: " + teacherCount + ", Students: " + studentCount;
            sessionsTableModel
                    .addRow(new Object[] { s.getId(), s.getTitle(), s.getDateTime(), groupName, participants });
        }
        if (selectedSessionId != null) {
            for (int i = 0; i < sessionsTableModel.getRowCount(); i++) {
                if (((String) sessionsTableModel.getValueAt(i, 0)).equals(selectedSessionId)) {
                    sessionsTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    private void displaySessionDetails() {
        int row = sessionsTable.getSelectedRow();
        if (row == -1)
            return;
        String sessionId = (String) sessionsTableModel.getValueAt(row, 0);
        Session session = sessions.stream().filter(s -> s.getId().equals(sessionId)).findFirst().orElse(null);
        if (session != null) {
            System.out.println("Session Details for " + session.getTitle() + ":");
            session.getMessages().forEach(msg -> System.out.println(msg.getSenderName() + ": " + msg.getContent()));
        }
    }

    private void endSelectedSession() {
        int selectedRow = sessionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a session to end.", "No Session Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sessionId = (String) sessionsTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to end the selected session?",
                "Confirm End Session", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            sessions.removeIf(s -> s.getId().equals(sessionId));
            SerializationUtil.saveDataToDisk(sessions, "sessions.txt");
            populateSessionsTable();
            JOptionPane.showMessageDialog(this, "Session ended successfully.", "Session Ended",
                    JOptionPane.INFORMATION_MESSAGE);
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
        Session session = sessions.stream().filter(s -> s.getId().equals(sessionId)).findFirst().orElse(null);
        if (session == null) {
            JOptionPane.showMessageDialog(this, "Selected session not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!session.getTeacherIds().contains(teacher.getId())) {
            session.addTeacher(teacher.getId());
            SerializationUtil.saveDataToDisk(sessions, "sessions.txt");
        }
        // Open the session room (assumed to be implemented elsewhere)
        SessionRoom room = new SessionRoom(this, session, teacher);
        room.setVisible(true);
    }

    // -------------------- Process Exam Tab --------------------
    private void initProcessExamTab() {
        processExamPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel selectTestLabel = new JLabel("Select Test to Process:");
        processExamComboBox = new JComboBox<>(tests.toArray(new Test[0]));
        processExamButton = new JButton("Process Exam");
        topPanel.add(selectTestLabel);
        topPanel.add(processExamComboBox);
        topPanel.add(processExamButton);
        processExamPanel.add(topPanel, BorderLayout.NORTH);

        // NEW: Create a table to display the exam results after processing
        processExamTableModel = new DefaultTableModel();
        processExamTableModel.addColumn("Student ID");
        processExamTableModel.addColumn("Student Name");
        processExamTableModel.addColumn("Score");
        processExamTableModel.addColumn("Submission Time");
        processExamTable = new JTable(processExamTableModel);
        JScrollPane processExamScrollPane = new JScrollPane(processExamTable);
        processExamPanel.add(processExamScrollPane, BorderLayout.CENTER);

        processExamButton.addActionListener(e -> processSelectedExam());
    }

    private void processSelectedExam() {
        Test selectedTest = (Test) processExamComboBox.getSelectedItem();
        if (selectedTest == null) {
            JOptionPane.showMessageDialog(this, "Please select a test to process.", "No Test Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        StandardExamProcessor processor = new StandardExamProcessor();
        try {
            processor.processExam(selectedTest);
            JOptionPane.showMessageDialog(this, "Exam '" + selectedTest.getTitle() + "' processed successfully.",
                    "Exam Processed", JOptionPane.INFORMATION_MESSAGE);
            // After processing, update the table with each student's result
            processExamTableModel.setRowCount(0);
            List<TestResult> results = selectedTest.getTestResults();
            for (TestResult tr : results) {
                Student stu = students.stream()
                        .filter(s -> s.getId().equals(tr.getStudentId()))
                        .findFirst().orElse(null);
                String studentName = (stu != null) ? stu.getName() : "Unknown";
                processExamTableModel.addRow(new Object[] {
                        tr.getStudentId(),
                        studentName,
                        String.format("%.2f", tr.getScore()),
                        tr.getSubmissionTime()
                });
            }
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No submissions found for the exam.", "No Results",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error processing exam: " + ex.getMessage(), "Processing Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------- Notifications Tab --------------------
    private void initNotificationsTab() {
        notificationsPanel = new JPanel(new BorderLayout());
        notificationsListModel = new DefaultListModel<>();
        notificationsList = new JList<>(notificationsListModel);
        JScrollPane scrollPane = new JScrollPane(notificationsList);
        notificationsPanel.add(scrollPane, BorderLayout.CENTER);
    }

    // -------------------- NEW: Edit Test Tab (with Undo/Redo using Memento)
    private void initEditTestTab() {
        editTestPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Test to Edit:"));
        editTestComboBox = new JComboBox<>(tests.toArray(new Test[0]));
        topPanel.add(editTestComboBox);
        JButton loadTestButton = new JButton("Load Test");
        topPanel.add(loadTestButton);
        editTestPanel.add(topPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel titleLabel = new JLabel("Test Title:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(titleLabel, gbc);
        editTestTitleField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(editTestTitleField, gbc);
        JLabel startTimeLabel = new JLabel("Start Time:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(startTimeLabel, gbc);
        editTestStartTimeField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(editTestStartTimeField, gbc);
        JLabel durationLabel = new JLabel("Duration (mins):");
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(durationLabel, gbc);
        editTestDurationField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(editTestDurationField, gbc);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        saveTestChangesButton = new JButton("Save Changes");
        undoButton = new JButton("Undo");
        redoButton = new JButton("Redo");
        buttonPanel.add(saveTestChangesButton);
        buttonPanel.add(undoButton);
        buttonPanel.add(redoButton);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        editTestPanel.add(formPanel, BorderLayout.CENTER);

        loadTestButton.addActionListener(e -> loadSelectedTestForEditing());
        saveTestChangesButton.addActionListener(e -> saveTestEdits());
        undoButton.addActionListener(e -> undoTestEdit());
        redoButton.addActionListener(e -> redoTestEdit());
    }

    private void loadSelectedTestForEditing() {
        Test selectedTest = (Test) editTestComboBox.getSelectedItem();
        if (selectedTest == null) {
            JOptionPane.showMessageDialog(this, "Please select a test to load.", "No Test Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        editTestTitleField.setText(selectedTest.getTitle());
        editTestStartTimeField.setText(selectedTest.getStartTime());
        editTestDurationField.setText(String.valueOf(selectedTest.getDuration()));
        // Create a deep copy and initialize memento history.
        Test testCopy = selectedTest.copy();
        testOriginator.setState(testCopy);
        testCaretaker.clear();
        testCaretaker.saveState(testOriginator.saveStateToMemento());
        JOptionPane.showMessageDialog(this, "Test loaded for editing.", "Load Successful",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveTestEdits() {
        Test selectedTest = (Test) editTestComboBox.getSelectedItem();
        if (selectedTest == null) {
            JOptionPane.showMessageDialog(this, "No test loaded for editing.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Clear the redo stack since new changes are made.
        testCaretaker.clearRedo();
        Test currentState = testOriginator.getState();
        currentState.setTitle(editTestTitleField.getText().trim());
        currentState.setStartTime(editTestStartTimeField.getText().trim());
        try {
            currentState.setDuration(Integer.parseInt(editTestDurationField.getText().trim()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid duration.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Update the originator state and save the new state to the undo stack.
        testOriginator.setState(currentState);
        testCaretaker.saveState(testOriginator.saveStateToMemento());
        // Update the global tests list.
        for (int i = 0; i < tests.size(); i++) {
            if (tests.get(i).getId().equals(currentState.getId())) {
                tests.set(i, currentState.copy());
                break;
            }
        }
        SerializationUtil.saveDataToDisk(tests, "tests.txt");
        JOptionPane.showMessageDialog(this, "Test changes saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
        updateEditTestComboBox();
    }

    private void undoTestEdit() {
        if (!testCaretaker.canUndo()) {
            JOptionPane.showMessageDialog(this, "No more undos available.", "Undo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        TestMemento memento = testCaretaker.undo();
        testOriginator.restoreState(memento);
        updateEditFieldsFromOriginator();
    }

    private void redoTestEdit() {
        if (!testCaretaker.canRedo()) {
            JOptionPane.showMessageDialog(this, "No more redos available.", "Redo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        TestMemento memento = testCaretaker.redo();
        testOriginator.restoreState(memento);
        updateEditFieldsFromOriginator();
    }

    private void updateEditFieldsFromOriginator() {
        Test current = testOriginator.getState();
        editTestTitleField.setText(current.getTitle());
        editTestStartTimeField.setText(current.getStartTime());
        editTestDurationField.setText(String.valueOf(current.getDuration()));
    }

    private void updateEditTestComboBox() {
        editTestComboBox.removeAllItems();
        for (Test t : tests) {
            editTestComboBox.addItem(t);
        }
    }

    // -------------------- Utility Methods --------------------
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

    // -------------------- SessionObserver Implementation --------------------
    @Override
    public void update(Session session, String message) {
        SwingUtilities.invokeLater(() -> {
            notificationsListModel.addElement("[" + session.getTitle() + "] " + message);
        });
    }

    public static void main(String[] args) {
        Teacher sampleTeacher = new Teacher("t1a2b3c4-d5e6-f7g8-h9i0-1234567890ab", "Alice Johnson", "alicePass");
        List<User> allUsers = UserStorage.getUsers();
        boolean exists = allUsers.stream().anyMatch(u -> u.getId().equals(sampleTeacher.getId()));
        if (!exists) {
            UserStorage.addUser(sampleTeacher);
            SerializationUtil.saveDataToDisk(UserStorage.getUsers(), "users.txt");
        }
        new TeacherDashboard(sampleTeacher);
    }
}
