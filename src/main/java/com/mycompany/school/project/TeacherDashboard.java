package com.mycompany.school.project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import model.subject.*;
import model.user.Teacher;
import model.user.Student;
import model.user.User;
import model.user.UserStorage;
import utils.IDGenerator;
import utils.DataInitializer;
import utils.SerializationUtil;

public class TeacherDashboard extends JFrame {
    private Teacher teacher;

    // Data lists
    private List<Subject> subjects;
    private List<Chapter> chapters;
    // Use ArrayList for groups so we can serialize
    private ArrayList<Group> groups;
    private ArrayList<Material> materials;
    private ArrayList<Test> tests;
    private ArrayList<Session> sessions;
    private List<Teacher> teachers;
    private List<Student> students;

    // Test Results
    private List<TestResult> allTestResults;

    // GUI Components
    private JPanel manageGroupsPanel;
    private JTable groupsTable;
    private DefaultTableModel groupsTableModel;
    private JButton refreshGroupsButton;
    private JButton deleteGroupButton;
    private JButton assignTeacherButton;
    private JButton removeTeacherButton; // to remove teacher from group

    private JPanel myGroupsPanel;
    private JTable myGroupsTable;
    private DefaultTableModel myGroupsTableModel;
    private JButton refreshMyGroupsButton;

    private JPanel createMaterialPanel;
    private JTextField materialTitleField;
    private JComboBox<String> materialTypeComboBox;
    private JTextField materialContentField;
    private JButton addMaterialButton;
    private JComboBox<Chapter> chapterComboBox;

    private JPanel createTestPanel;
    private JButton addTestButton;

    private JPanel createSessionPanel;
    private JTextField sessionTitleField;
    private JTextField sessionDateTimeField;
    private JComboBox<Group> sessionGroupComboBox;
    private JButton addSessionButton;

    private JPanel viewTestsPanel;
    private JComboBox<Chapter> viewTestsChapterComboBox;
    private JTable viewTestsTable;
    private DefaultTableModel viewTestsTableModel;
    private JButton refreshViewTestsButton;

    // New Panels for Viewing Test Results
    private JPanel viewTestResultsPanel;
    private JTable testResultsTable;
    private DefaultTableModel testResultsTableModel;
    private JComboBox<Test> testSelectionComboBox;
    private JButton viewTestResultsButton;

    // List of chapters where the teacher is assigned to at least one group
    private List<Chapter> teacherChapters;

    public TeacherDashboard(Teacher teacher) {
        this.teacher = teacher;
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
        } else {
            System.out.println("Loaded " + subjects.size() + " subjects.");
        }

        // Load chapters
        chapters = SerializationUtil.readFromFile("chapters.ser");
        if (chapters == null) {
            chapters = new ArrayList<>();
            System.out.println("No existing chapters found.");
        } else {
            System.out.println("Loaded " + chapters.size() + " chapters.");
        }

        // Load groups (use ArrayList)
        ArrayList<Group> loadedGroups = SerializationUtil.readFromFile("groups.ser");
        if (loadedGroups == null) {
            groups = new ArrayList<>();
            System.out.println("No existing groups found.");
        } else {
            groups = loadedGroups;
            System.out.println("Loaded " + groups.size() + " groups.");
        }

        // Load materials
        materials = SerializationUtil.readFromFile("materials.ser");
        if (materials == null) {
            materials = new ArrayList<>();
            System.out.println("No existing materials found.");
        } else {
            System.out.println("Loaded " + materials.size() + " materials.");
        }

        // Load tests
        tests = SerializationUtil.readFromFile("tests.ser");
        if (tests == null) {
            tests = new ArrayList<>();
            System.out.println("No existing tests found.");
        } else {
            System.out.println("Loaded " + tests.size() + " tests from tests.ser");
        }

        // Load sessions
        sessions = SerializationUtil.readFromFile("sessions.ser");
        if (sessions == null) {
            sessions = new ArrayList<>();
            System.out.println("No existing sessions found.");
        } else {
            System.out.println("Loaded " + sessions.size() + " sessions.");
        }

        // Load teachers and students from UserStorage
        List<User> allUsers = UserStorage.getUsers();
        teachers = allUsers.stream()
                .filter(u -> u instanceof Teacher)
                .map(u -> (Teacher) u)
                .collect(Collectors.toList());

        students = allUsers.stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .collect(Collectors.toList());

        System.out.println("Loaded " + teachers.size() + " teachers and " + students.size() + " students.");

        // Load all test results
        allTestResults = SerializationUtil.readFromFile("testResults.ser");
        if (allTestResults == null) {
            allTestResults = new ArrayList<>();
            System.out.println("No existing test results found.");
        } else {
            System.out.println("Loaded " + allTestResults.size() + " test results from testResults.ser");
        }

        // Determine chapters where the teacher is assigned to at least one group
        teacherChapters = groups.stream()
                .filter(g -> g.getTeacherIds().contains(teacher.getId()))
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

    /**
     * Initializes all GUI components and layouts.
     */
    private void initComponents() {
        setTitle("Teacher Dashboard - " + teacher.getName());
        setLayout(new BorderLayout());

        // Create Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Initialize Tabs
        initManageGroupsTab();
        initMyGroupsTab();
        initCreateMaterialTab();
        initCreateTestTab();
        initCreateSessionTab();
        initViewTestsTab();
        initViewTestResultsTab();

        // Add Tabs
        tabbedPane.addTab("Manage Groups", manageGroupsPanel);
        tabbedPane.addTab("My Groups", myGroupsPanel);
        tabbedPane.addTab("Create Material", createMaterialPanel);
        tabbedPane.addTab("Create Test", createTestPanel);
        tabbedPane.addTab("Create Session", createSessionPanel);
        tabbedPane.addTab("View Tests", viewTestsPanel);
        tabbedPane.addTab("View Test Results", viewTestResultsPanel);

        // Disable creation tabs if no assigned chapters
        if (teacherChapters.isEmpty()) {
            tabbedPane.setEnabledAt(tabbedPane.indexOfTab("Create Material"), false);
            tabbedPane.setEnabledAt(tabbedPane.indexOfTab("Create Test"), false);
            tabbedPane.setEnabledAt(tabbedPane.indexOfTab("Create Session"), false);
            JOptionPane.showMessageDialog(this,
                    "You are not assigned to any chapters. Material, Test, and Session creation are disabled.",
                    "No Assigned Chapters",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        add(tabbedPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 1000);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * "Manage Groups" tab.
     */
    private void initManageGroupsTab() {
        manageGroupsPanel = new JPanel(new BorderLayout());

        // Table Model
        groupsTableModel = new DefaultTableModel();
        groupsTableModel.addColumn("Group ID");
        groupsTableModel.addColumn("Group Name");
        groupsTableModel.addColumn("Chapter");
        groupsTableModel.addColumn("Teachers");
        groupsTableModel.addColumn("Enrollees");
        groupsTableModel.addColumn("Assigned to Me");

        // Populate
        populateGroupsTable();

        groupsTable = new JTable(groupsTableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
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

        // Buttons
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

        // Listeners
        refreshGroupsButton.addActionListener(e -> populateGroupsTable());
        deleteGroupButton.addActionListener(e -> deleteSelectedGroup());
        assignTeacherButton.addActionListener(e -> assignTeacherToGroup());
        removeTeacherButton.addActionListener(e -> removeTeacherFromGroup());
    }

    private void populateGroupsTable() {
        groupsTableModel.setRowCount(0);
        for (Group g : groups) {
            String chapterName = getChapterNameById(g.getChapterId());
            String teachersNames = g.getTeacherIds().stream()
                    .map(this::getTeacherNameById)
                    .collect(Collectors.joining("; "));
            int enrollees = g.getStudentIds().size();
            boolean assignedToMe = g.getTeacherIds().contains(teacher.getId());
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
                    "Deletion Not Allowed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Group ID: " + groupId + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            groups.removeIf(g -> g.getId().equals(groupId));
            SerializationUtil.saveDataToDisk(groups, "groups.ser");
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
        Group selectedGroup = groups.stream()
                .filter(g -> g.getId().equals(groupId))
                .findFirst()
                .orElse(null);

        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Selected group not found.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean alreadyAssigned = selectedGroup.getTeacherIds().contains(teacher.getId());
        if (alreadyAssigned) {
            JOptionPane.showMessageDialog(this, "You are already assigned to this group.", "Already Assigned",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        selectedGroup.addTeacher(teacher.getId());
        SerializationUtil.saveDataToDisk(groups, "groups.ser");
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
        Group selectedGroup = groups.stream()
                .filter(g -> g.getId().equals(groupId))
                .findFirst()
                .orElse(null);

        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Selected group not found.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean assignedToMe = selectedGroup.getTeacherIds().contains(teacher.getId());
        if (!assignedToMe) {
            JOptionPane.showMessageDialog(this, "You are not assigned to this group.", "Not Assigned",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove yourself from Group ID: " + groupId + "?",
                "Confirm Removal", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            selectedGroup.removeTeacher(teacher.getId());
            SerializationUtil.saveDataToDisk(groups, "groups.ser");
            populateGroupsTable();
            populateMyGroupsTable();
            JOptionPane.showMessageDialog(this, "You have been removed from the group.", "Removal Successful",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * "My Groups" tab.
     */
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
                .filter(g -> g.getTeacherIds().contains(teacher.getId()))
                .collect(Collectors.toList());

        for (Group g : myGroups) {
            String chapterName = getChapterNameById(g.getChapterId());
            int enrollees = g.getStudentIds().size();
            String teachersNames = g.getTeacherIds().stream()
                    .map(this::getTeacherNameById)
                    .collect(Collectors.joining("; "));
            myGroupsTableModel.addRow(new Object[] {
                    g.getId(), g.getGroupName(), chapterName, enrollees, teachersNames
            });
        }
    }

    /**
     * "Create Material" tab.
     */
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
            JOptionPane.showMessageDialog(this,
                    "Please fill in all fields and ensure you have assigned chapters.",
                    "Incomplete Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Material material = new Material(IDGenerator.generateID(), title, type, content, selectedChapter.getId());
        materials.add(material);

        SerializationUtil.saveDataToDisk(materials, "materials.ser");
        System.out.println("Material saved. Total materials now: " + materials.size());
        JOptionPane.showMessageDialog(this, "Material added successfully.", "Success",
                JOptionPane.INFORMATION_MESSAGE);

        materialTitleField.setText("");
        materialContentField.setText("");
    }

    /**
     * "Create Test" tab.
     */
    private void initCreateTestTab() {
        createTestPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addTestButton = new JButton("Create New Test");
        topPanel.add(addTestButton);
        createTestPanel.add(topPanel, BorderLayout.NORTH);

        // Table Model for displaying tests
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

        // Refresh Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshTestButton = new JButton("Refresh");
        bottomPanel.add(refreshTestButton);
        createTestPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Populate the test table
        populateTestTable(testTableModel);

        // Listeners
        addTestButton.addActionListener(e -> openCreateTestDialog(testTableModel));
        refreshTestButton.addActionListener(e -> populateTestTable(testTableModel));
    }

    private void populateTestTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Test t : tests) {
            String chapterName = getChapterNameById(t.getChapterId());
            int numQuestions = t.getQuestions().size();
            model.addRow(new Object[] {
                    t.getId(), t.getTitle(), chapterName, t.getStartTime(), t.getDuration(), numQuestions
            });
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

        // Questions Panel
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

        // Save Test Button
        JButton saveTestButton = new JButton("Save Test");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        formPanel.add(saveTestButton, gbc);

        dialog.add(formPanel, BorderLayout.CENTER);

        // Listeners for adding and removing questions
        addQuestionButton.addActionListener(e -> {
            JPanel questionDialogPanel = new JPanel(new GridLayout(6, 1, 5, 5));
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
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String[] options = { opt1, opt2, opt3, opt4 };
                Question newQuestion = new Question(IDGenerator.generateID(), "temp-test-id", questionText, options,
                        correctOption);
                questionListModel.addElement(newQuestion);
                System.out.println("Added question: " + newQuestion.getText());
            }
        });

        removeQuestionButton.addActionListener(e -> {
            int selectedIndex = questionJList.getSelectedIndex();
            if (selectedIndex != -1) {
                Question removedQuestion = questionListModel.getElementAt(selectedIndex);
                questionListModel.remove(selectedIndex);
                System.out.println("Removed question: " + removedQuestion.getText());
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a question to remove.", "No Question Selected",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Listener for saving the test
        saveTestButton.addActionListener(e -> {
            String title = testTitleField.getText().trim();
            String startTime = testStartTimeField.getText().trim();
            String durationStr = testDurationField.getText().trim();
            Chapter selectedChapter = (Chapter) testChapterComboBox.getSelectedItem();

            if (title.isEmpty() || startTime.isEmpty() || durationStr.isEmpty() || selectedChapter == null
                    || selectedChapter.getId().equals("N/A")) {
                JOptionPane.showMessageDialog(dialog,
                        "Please fill in all fields and ensure you have assigned chapters.",
                        "Incomplete Information",
                        JOptionPane.WARNING_MESSAGE);
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

            // Create the test
            Test newTest = new Test(IDGenerator.generateID(), selectedChapter.getId(), title, startTime, duration);

            // Add questions to the test
            for (int i = 0; i < questionListModel.size(); i++) {
                Question q = questionListModel.getElementAt(i);
                q.setTestId(newTest.getId()); // Set the correct testId
                newTest.addQuestion(q);
                System.out.println("Added question to test: " + q.getText());
            }

            // Add the test to the list and serialize
            tests.add(newTest);
            SerializationUtil.saveDataToDisk(tests, "tests.ser");
            System.out.println("Test saved. Total tests now: " + tests.size());

            // Update the test table
            populateTestTable(testTableModel);

            JOptionPane.showMessageDialog(dialog, "Test and questions added successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            dialog.dispose();
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    /**
     * "Create Session" tab.
     */
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

        JLabel groupLabel = new JLabel("Select Group:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        createSessionPanel.add(groupLabel, gbc);

        sessionGroupComboBox = new JComboBox<>(groups.toArray(new Group[0]));
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

        Session session = new Session(IDGenerator.generateID(), title, dateTime,
                selectedGroup.getChapterId(), selectedGroup.getId());
        sessions.add(session);

        SerializationUtil.saveDataToDisk(sessions, "sessions.ser");
        System.out.println("Session saved. Total sessions now: " + sessions.size());
        JOptionPane.showMessageDialog(this, "Session added successfully.", "Success",
                JOptionPane.INFORMATION_MESSAGE);

        // "Notify" students (dummy)
        List<String> studentIds = selectedGroup.getStudentIds();
        List<Student> enrolledStudents = students.stream()
                .filter(s -> studentIds.contains(s.getId()))
                .collect(Collectors.toList());
        session.notifyStudents(enrolledStudents);

        sessionTitleField.setText("");
        sessionDateTimeField.setText("");
    }

    /**
     * "View Tests" tab.
     */
    private void initViewTestsTab() {
        viewTestsPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel selectChapterLabel = new JLabel("Select Chapter:");
        viewTestsChapterComboBox = new JComboBox<>(teacherChapters.toArray(new Chapter[0]));
        refreshViewTestsButton = new JButton("Refresh");
        topPanel.add(selectChapterLabel);
        topPanel.add(viewTestsChapterComboBox);
        topPanel.add(refreshViewTestsButton);
        viewTestsPanel.add(topPanel, BorderLayout.NORTH);

        // Table Model
        viewTestsTableModel = new DefaultTableModel();
        viewTestsTableModel.addColumn("Test ID");
        viewTestsTableModel.addColumn("Title");
        viewTestsTableModel.addColumn("Start Time");
        viewTestsTableModel.addColumn("Duration (mins)");
        viewTestsTableModel.addColumn("Number of Questions");

        JTable viewTestsTable = new JTable(viewTestsTableModel);
        JScrollPane scrollPane = new JScrollPane(viewTestsTable);
        viewTestsPanel.add(scrollPane, BorderLayout.CENTER);

        // Populate tests based on selected chapter
        populateViewTestsTable(viewTestsTableModel);

        // Listener for refresh button
        refreshViewTestsButton.addActionListener(e -> populateViewTestsTable(viewTestsTableModel));

        // Listener for chapter selection change
        viewTestsChapterComboBox.addActionListener(e -> populateViewTestsTable(viewTestsTableModel));
    }

    /**
     * Populates the View Tests table based on the selected chapter.
     *
     * @param model The table model to populate.
     */
    private void populateViewTestsTable(DefaultTableModel model) {
        model.setRowCount(0); // Clear existing rows

        Chapter selectedChapter = (Chapter) viewTestsChapterComboBox.getSelectedItem();
        if (selectedChapter == null || selectedChapter.getId().equals("N/A")) {
            return; // No chapter selected or invalid chapter
        }

        // Filter tests assigned to the selected chapter
        List<Test> chapterTests = tests.stream()
                .filter(t -> t.getChapterId().equals(selectedChapter.getId()))
                .collect(Collectors.toList());

        // Populate the table model with the filtered tests
        for (Test t : chapterTests) {
            int numQuestions = t.getQuestions().size();
            model.addRow(new Object[] {
                    t.getId(),
                    t.getTitle(),
                    t.getStartTime(),
                    t.getDuration(),
                    numQuestions
            });
        }

        // Optional: Notify if no tests are found
        if (chapterTests.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tests found for the selected chapter.", "No Tests",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * "View Test Results" tab.
     */
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

        // Table Model for Test Results
        testResultsTableModel = new DefaultTableModel();
        testResultsTableModel.addColumn("Student ID");
        testResultsTableModel.addColumn("Student Name");
        testResultsTableModel.addColumn("Score");
        testResultsTableModel.addColumn("Submission Time");

        // Test Results Table
        testResultsTable = new JTable(testResultsTableModel);
        JScrollPane scrollPane = new JScrollPane(testResultsTable);
        viewTestResultsPanel.add(scrollPane, BorderLayout.CENTER);

        // Listener for view results button
        viewTestResultsButton.addActionListener(e -> viewSelectedTestResults());
    }

    private void viewSelectedTestResults() {
        Test selectedTest = (Test) testSelectionComboBox.getSelectedItem();
        if (selectedTest == null) {
            JOptionPane.showMessageDialog(this, "Please select a test to view results.", "No Test Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Clear existing rows
        testResultsTableModel.setRowCount(0);

        // Filter test results for the selected test
        List<TestResult> selectedTestResults = allTestResults.stream()
                .filter(tr -> tr.getTestId().equals(selectedTest.getId()))
                .collect(Collectors.toList());

        for (TestResult tr : selectedTestResults) {
            Student student = students.stream()
                    .filter(s -> s.getId().equals(tr.getStudentId()))
                    .findFirst()
                    .orElse(null);
            String studentName = (student != null) ? student.getName() : "Unknown";
            testResultsTableModel.addRow(new Object[] {
                    tr.getStudentId(), studentName, String.format("%.2f", tr.getScore()), tr.getSubmissionTime()
            });
        }

        if (selectedTestResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results found for the selected test.", "No Results",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            System.out.println(
                    "Displayed " + selectedTestResults.size() + " results for test ID: " + selectedTest.getId());
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

    private String getTeacherNameById(String teacherId) {
        for (Teacher t : teachers) {
            if (t.getId().equals(teacherId)) {
                return t.getName();
            }
        }
        return "Unknown";
    }

    /**
     * Main method to launch the Teacher Dashboard for testing purposes.
     */
    public static void main(String[] args) {
        // For testing: Create a sample teacher and launch the dashboard
        Teacher sampleTeacher = new Teacher("t1a2b3c4-d5e6-f7g8-h9i0-1234567890ab", "Alice Johnson", "alicePass");
        new TeacherDashboard(sampleTeacher);
    }
}
