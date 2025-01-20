package com.mycompany.school.project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

import model.subject.*;
import model.user.Teacher;
import model.user.Student;
import model.user.User;
import utils.CSVUtil;
import utils.IDGenerator;

public class TeacherDashboard extends JFrame {
    private Teacher teacher;

    // Data lists
    private List<Subject> subjects;
    private List<Chapter> chapters;
    private List<Group> groups;
    private List<Material> materials;
    private List<Test> tests;
    private List<Session> sessions;
    private List<Teacher> teachers;
    private List<Student> students;

    // GUI Components
    private JPanel manageGroupsPanel;
    private JTable groupsTable;
    private DefaultTableModel groupsTableModel;
    private JButton refreshGroupsButton;
    private JButton deleteGroupButton;
    private JButton assignTeacherButton;
    private JButton removeTeacherButton; // New button to remove teacher from group

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
    private JTextField testTitleField;
    private JTextField testStartTimeField;
    private JTextField testDurationField;
    private JButton addTestButton;
    private JButton addQuestionButton;

    private JPanel createSessionPanel;
    private JTextField sessionTitleField;
    private JTextField sessionDateTimeField;
    private JComboBox<Group> sessionGroupComboBox;
    private JButton addSessionButton;

    // List of chapters where the teacher is assigned to at least one group
    private List<Chapter> teacherChapters;

    public TeacherDashboard(Teacher teacher) {
        this.teacher = teacher;
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
            return new Subject(
                    fields[0],
                    fields[1],
                    fields[2],
                    Double.parseDouble(fields[3]));
        });

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
        });

        // Load groups
        groups = CSVUtil.readFromCSV("groups.csv", fields -> {
            if (fields.length < 5) {
                System.err.println("Malformed group entry: " + Arrays.toString(fields));
                return null;
            }
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
        });

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
        });

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
        });

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
        });

        // Load teachers and students from users.csv
        List<User> allUsers = CSVUtil.readFromCSV("users.csv", fields -> {
            if (fields.length < 5) {
                System.err.println("Malformed user entry: " + Arrays.toString(fields));
                return null;
            }
            if (fields[3].equalsIgnoreCase("Teacher")) {
                return new Teacher(fields[0], fields[1], fields[2]);
            } else if (fields[3].equalsIgnoreCase("Student")) {
                try {
                    double balance = Double.parseDouble(fields[4]);
                    return new Student(fields[0], fields[1], fields[2], balance);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid balance for student: " + Arrays.toString(fields));
                    return null;
                }
            }
            return null; // For roles other than Teacher and Student
        }).stream().filter(u -> u != null).collect(Collectors.toList());

        teachers = allUsers.stream()
                .filter(u -> u instanceof Teacher)
                .map(u -> (Teacher) u)
                .collect(Collectors.toList());

        students = allUsers.stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .collect(Collectors.toList());

        // Determine chapters where the teacher is assigned to at least one group
        teacherChapters = groups.stream()
                .filter(g -> g.getTeacherIds().contains(teacher.getId()))
                .map(Group::getChapterId)
                .distinct()
                .map(chapterId -> chapters.stream().filter(c -> c.getId().equals(chapterId)).findFirst().orElse(null))
                .filter(c -> c != null)
                .collect(Collectors.toList());

        // Optionally, you can print the chapters assigned to the teacher for debugging
        System.out.println("Chapters assigned to Teacher (" + teacher.getName() + "): " +
                teacherChapters.stream().map(Chapter::getName).collect(Collectors.joining(", ")));
    }

    /**
     * Initializes all GUI components and layouts.
     */
    private void initComponents() {
        setTitle("Teacher Dashboard");
        setLayout(new BorderLayout());

        // Create Tabbed Pane to organize sections
        JTabbedPane tabbedPane = new JTabbedPane();

        // Initialize Tabs
        initManageGroupsTab();
        initMyGroupsTab();
        initCreateMaterialTab();
        initCreateTestTab();
        initCreateSessionTab();

        // Add Tabs to TabbedPane
        tabbedPane.addTab("Manage Groups", manageGroupsPanel);
        tabbedPane.addTab("My Groups", myGroupsPanel);
        tabbedPane.addTab("Create Material", createMaterialPanel);
        tabbedPane.addTab("Create Test", createTestPanel);
        tabbedPane.addTab("Create Session", createSessionPanel);

        // Disable Create Material and Create Test tabs if no assigned chapters
        if (teacherChapters.isEmpty()) {
            tabbedPane.setEnabledAt(tabbedPane.indexOfTab("Create Material"), false);
            tabbedPane.setEnabledAt(tabbedPane.indexOfTab("Create Test"), false);
            JOptionPane.showMessageDialog(this,
                    "You are not assigned to any chapters. Material and Test creation are disabled.",
                    "No Assigned Chapters",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        // Add Tabs to Frame
        add(tabbedPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }

    /**
     * Initializes the "Manage Groups" tab where teachers can view and manage
     * groups.
     */
    private void initManageGroupsTab() {
        manageGroupsPanel = new JPanel(new BorderLayout());

        // Table Model for Groups
        groupsTableModel = new DefaultTableModel();
        groupsTableModel.addColumn("Group ID");
        groupsTableModel.addColumn("Group Name");
        groupsTableModel.addColumn("Chapter");
        groupsTableModel.addColumn("Teachers");
        groupsTableModel.addColumn("Enrollees");
        groupsTableModel.addColumn("Assigned to Me");

        // Populate Groups Table
        populateGroupsTable();

        // Groups Table
        groupsTable = new JTable(groupsTableModel) {
            // To visually distinguish groups assigned to the current teacher
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                Boolean assigned = (Boolean) getValueAt(row, 5);
                if (assigned != null && assigned) {
                    comp.setBackground(new Color(204, 255, 204)); // Light green
                } else {
                    comp.setBackground(new Color(255, 204, 255)); // Light pink
                }
                return comp;
            }
        };
        JScrollPane scrollPane = new JScrollPane(groupsTable);
        manageGroupsPanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshGroupsButton = new JButton("Refresh");
        deleteGroupButton = new JButton("Delete Selected Group");
        assignTeacherButton = new JButton("Assign Myself to Selected Group");
        removeTeacherButton = new JButton("Remove Myself from Selected Group"); // New Button
        buttonsPanel.add(refreshGroupsButton);
        buttonsPanel.add(assignTeacherButton);
        buttonsPanel.add(removeTeacherButton); // Add to panel
        buttonsPanel.add(deleteGroupButton); // Moved delete button to the end
        manageGroupsPanel.add(buttonsPanel, BorderLayout.SOUTH);

        // Action Listeners
        refreshGroupsButton.addActionListener(e -> populateGroupsTable());
        deleteGroupButton.addActionListener(e -> deleteSelectedGroup());
        assignTeacherButton.addActionListener(e -> assignTeacherToGroup());
        removeTeacherButton.addActionListener(e -> removeTeacherFromGroup()); // New Action
    }

    /**
     * Populates the "Manage Groups" table with all groups and marks those
     * assigned to the current teacher.
     */
    private void populateGroupsTable() {
        // Clear existing rows
        groupsTableModel.setRowCount(0);

        for (Group g : groups) {
            String chapterName = getChapterNameById(g.getChapterId());
            String teachersNames = g.getTeacherIds().stream()
                    .map(this::getTeacherNameById)
                    .collect(Collectors.joining("; "));
            int enrollees = g.getStudentIds().size();
            boolean assignedToMe = g.getTeacherIds().contains(teacher.getId());
            groupsTableModel.addRow(
                    new Object[] { g.getId(), g.getGroupName(), chapterName, teachersNames, enrollees, assignedToMe });
        }
    }

    /**
     * Deletes the selected group from the table and updates the CSV file.
     */
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

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete Group ID: " + groupId + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Remove group from list
            groups.removeIf(g -> g.getId().equals(groupId));

            // Update groups.csv using toStringCSV()
            CSVUtil.writeToCSV("groups.csv", groups, g -> g.toStringCSV(),
                    "id,groupName,chapterId,teacherIds,studentIds");

            // Refresh table
            populateGroupsTable();

            JOptionPane.showMessageDialog(this, "Group deleted successfully.", "Deletion Successful",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Assigns the current teacher to the selected group, ensuring no duplicate
     * assignments.
     */
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

        // Assign the teacher to the group
        selectedGroup.addTeacher(teacher.getId());

        // Update groups.csv using toStringCSV()
        CSVUtil.writeToCSV("groups.csv", groups, g -> g.toStringCSV(),
                "id,groupName,chapterId,teacherIds,studentIds");

        // Refresh table
        populateGroupsTable();
        populateMyGroupsTable();

        JOptionPane.showMessageDialog(this, "You have been assigned to the selected group.", "Assignment Successful",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Removes the current teacher from the selected group.
     */
    private void removeTeacherFromGroup() {
        int selectedRow = groupsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a group to remove yourself from.", "No Group Selected",
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

        // Confirm removal
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove yourself from Group ID: " + groupId + "?",
                "Confirm Removal", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Remove the teacher from the group
            selectedGroup.removeTeacher(teacher.getId());

            // Update groups.csv using toStringCSV()
            CSVUtil.writeToCSV("groups.csv", groups, g -> g.toStringCSV(),
                    "id,groupName,chapterId,teacherIds,studentIds");

            // Refresh table
            populateGroupsTable();
            populateMyGroupsTable();

            JOptionPane.showMessageDialog(this, "You have been removed from the group successfully.",
                    "Removal Successful",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Initializes the "My Groups" tab where teachers can view their assigned
     * groups.
     */
    private void initMyGroupsTab() {
        myGroupsPanel = new JPanel(new BorderLayout());

        // Table Model for My Groups
        myGroupsTableModel = new DefaultTableModel();
        myGroupsTableModel.addColumn("Group ID");
        myGroupsTableModel.addColumn("Group Name");
        myGroupsTableModel.addColumn("Chapter");
        myGroupsTableModel.addColumn("Enrollees");
        myGroupsTableModel.addColumn("Teachers");

        // Populate My Groups Table
        populateMyGroupsTable();

        // My Groups Table
        myGroupsTable = new JTable(myGroupsTableModel);
        JScrollPane scrollPane = new JScrollPane(myGroupsTable);
        myGroupsPanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshMyGroupsButton = new JButton("Refresh");
        buttonsPanel.add(refreshMyGroupsButton);
        myGroupsPanel.add(buttonsPanel, BorderLayout.SOUTH);

        // Action Listeners
        refreshMyGroupsButton.addActionListener(e -> populateMyGroupsTable());
    }

    /**
     * Populates the "My Groups" table with groups assigned to the current teacher.
     */
    private void populateMyGroupsTable() {
        // Clear existing rows
        myGroupsTableModel.setRowCount(0);

        // Filter groups assigned to the teacher
        List<Group> myGroups = groups.stream()
                .filter(g -> g.getTeacherIds().contains(teacher.getId()))
                .collect(Collectors.toList());

        for (Group g : myGroups) {
            String chapterName = getChapterNameById(g.getChapterId());
            int enrollees = g.getStudentIds().size();
            String teachersNames = g.getTeacherIds().stream()
                    .map(this::getTeacherNameById)
                    .collect(Collectors.joining("; "));
            myGroupsTableModel
                    .addRow(new Object[] { g.getId(), g.getGroupName(), chapterName, enrollees, teachersNames });
        }
    }

    /**
     * Initializes the "Create Material" tab where teachers can add new materials.
     */
    private void initCreateMaterialTab() {
        createMaterialPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Material Title
        JLabel titleLabel = new JLabel("Material Title:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        createMaterialPanel.add(titleLabel, gbc);

        materialTitleField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        createMaterialPanel.add(materialTitleField, gbc);

        // Material Type
        JLabel typeLabel = new JLabel("Material Type:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        createMaterialPanel.add(typeLabel, gbc);

        String[] types = { "video", "image", "pdf" };
        materialTypeComboBox = new JComboBox<>(types);
        gbc.gridx = 1;
        gbc.gridy = 1;
        createMaterialPanel.add(materialTypeComboBox, gbc);

        // Material Content/Path
        JLabel contentLabel = new JLabel("Content Path/URL:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        createMaterialPanel.add(contentLabel, gbc);

        materialContentField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 2;
        createMaterialPanel.add(materialContentField, gbc);

        // Assign Chapter ComboBox (Filtered)
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

        // Add Material Button
        addMaterialButton = new JButton("Add Material");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        createMaterialPanel.add(addMaterialButton, gbc);

        // Action Listener
        addMaterialButton.addActionListener(e -> addMaterial());
    }

    /**
     * Adds a new material based on user input.
     */
    private void addMaterial() {
        String title = materialTitleField.getText().trim();
        String type = (String) materialTypeComboBox.getSelectedItem();
        String content = materialContentField.getText().trim();
        Chapter selectedChapter = (Chapter) chapterComboBox.getSelectedItem();

        if (title.isEmpty() || type.isEmpty() || content.isEmpty() || selectedChapter == null
                || selectedChapter.getId().equals("N/A")) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields and ensure you have assigned chapters.",
                    "Incomplete Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create Material
        Material material = teacher.createMaterial(title, type, content, selectedChapter.getId());
        materials.add(material);

        // Update materials.csv using toStringCSV()
        CSVUtil.writeToCSV("materials.csv", materials, m -> m.toStringCSV(),
                "id,title,type,pathOrContent,chapterId");

        JOptionPane.showMessageDialog(this, "Material added successfully.", "Success",
                JOptionPane.INFORMATION_MESSAGE);

        // Clear fields
        materialTitleField.setText("");
        materialContentField.setText("");
    }

    /**
     * Initializes the "Create Test" tab where teachers can add new tests and
     * questions.
     */
    private void initCreateTestTab() {
        createTestPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Test Title
        JLabel titleLabel = new JLabel("Test Title:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        createTestPanel.add(titleLabel, gbc);

        testTitleField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        createTestPanel.add(testTitleField, gbc);

        // Test Start Time
        JLabel startTimeLabel = new JLabel("Start Time (e.g., 2025-02-01 10:00):");
        gbc.gridx = 0;
        gbc.gridy = 1;
        createTestPanel.add(startTimeLabel, gbc);

        testStartTimeField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        createTestPanel.add(testStartTimeField, gbc);

        // Test Duration
        JLabel durationLabel = new JLabel("Duration (minutes):");
        gbc.gridx = 0;
        gbc.gridy = 2;
        createTestPanel.add(durationLabel, gbc);

        testDurationField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 2;
        createTestPanel.add(testDurationField, gbc);

        // Assign Chapter ComboBox (Filtered)
        JLabel chapterLabel = new JLabel("Assign to Chapter:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        createTestPanel.add(chapterLabel, gbc);

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
        createTestPanel.add(testChapterComboBox, gbc);

        // Add Test Button
        addTestButton = new JButton("Add Test");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        createTestPanel.add(addTestButton, gbc);

        // Add Action Listener
        addTestButton.addActionListener(e -> addTest(testChapterComboBox));

        // Add Question Button
        addQuestionButton = new JButton("Add Questions to Last Test");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        createTestPanel.add(addQuestionButton, gbc);

        // Action Listener for Adding Questions
        addQuestionButton.addActionListener(e -> addQuestionsToTest());
    }

    /**
     * Adds a new test based on user input.
     *
     * @param chapterComboBox The JComboBox containing chapters.
     */
    private void addTest(JComboBox<Chapter> chapterComboBox) {
        String title = testTitleField.getText().trim();
        String startTime = testStartTimeField.getText().trim();
        String durationStr = testDurationField.getText().trim();
        Chapter selectedChapter = (Chapter) chapterComboBox.getSelectedItem();

        if (title.isEmpty() || startTime.isEmpty() || durationStr.isEmpty() || selectedChapter == null
                || selectedChapter.getId().equals("N/A")) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields and ensure you have assigned chapters.",
                    "Incomplete Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationStr);
            if (duration <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid duration.", "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create Test
        Test test = teacher.createTest(selectedChapter.getId(), title, startTime, duration);
        tests.add(test);

        // Update tests.csv using toStringCSV()
        CSVUtil.writeToCSV("tests.csv", tests, t -> t.toStringCSV(),
                "id,chapterId,title,startTime,duration,questions");

        JOptionPane.showMessageDialog(this, "Test added successfully. Now add questions.", "Success",
                JOptionPane.INFORMATION_MESSAGE);

        // Clear fields
        testTitleField.setText("");
        testStartTimeField.setText("");
        testDurationField.setText("");
    }

    /**
     * Adds questions to the most recently added test.
     */
    private void addQuestionsToTest() {
        if (tests.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tests available to add questions.", "No Tests",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Test latestTest = tests.get(tests.size() - 1);

        while (true) {
            int option = JOptionPane.showConfirmDialog(this, "Add a question to the test?", "Add Question",
                    JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                String questionText = JOptionPane.showInputDialog(this, "Enter Question Text:");
                if (questionText == null || questionText.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Question text cannot be empty.", "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                String[] options = new String[4];
                for (int i = 0; i < 4; i++) {
                    String opt = JOptionPane.showInputDialog(this, "Enter option " + (i + 1) + ":");
                    if (opt == null || opt.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Option cannot be empty.", "Invalid Input",
                                JOptionPane.ERROR_MESSAGE);
                        i--;
                        continue;
                    }
                    options[i] = opt;
                }

                String correctOptionStr = JOptionPane.showInputDialog(this, "Enter correct option number (1-4):");
                int correctOption;
                try {
                    correctOption = Integer.parseInt(correctOptionStr) - 1;
                    if (correctOption < 0 || correctOption > 3)
                        throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid option number.", "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                Question question = teacher.createQuestion(latestTest.getId(), questionText, options, correctOption);
                latestTest.addQuestion(question);
            } else {
                break;
            }
        }

        // Update tests.csv with new questions using toStringCSV()
        CSVUtil.writeToCSV("tests.csv", tests, t -> t.toStringCSV(),
                "id,chapterId,title,startTime,duration,questions");

        JOptionPane.showMessageDialog(this, "Questions added successfully.", "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Initializes the "Create Session" tab where teachers can schedule sessions.
     */
    private void initCreateSessionTab() {
        createSessionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Session Title
        JLabel titleLabel = new JLabel("Session Title:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        createSessionPanel.add(titleLabel, gbc);

        sessionTitleField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        createSessionPanel.add(sessionTitleField, gbc);

        // Session Date and Time
        JLabel dateTimeLabel = new JLabel("Date and Time (e.g., 2025-02-01 10:00):");
        gbc.gridx = 0;
        gbc.gridy = 1;
        createSessionPanel.add(dateTimeLabel, gbc);

        sessionDateTimeField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        createSessionPanel.add(sessionDateTimeField, gbc);

        // Select Group
        JLabel groupLabel = new JLabel("Select Group:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        createSessionPanel.add(groupLabel, gbc);

        sessionGroupComboBox = new JComboBox<>(groups.toArray(new Group[0]));
        gbc.gridx = 1;
        gbc.gridy = 2;
        createSessionPanel.add(sessionGroupComboBox, gbc);

        // Add Session Button
        addSessionButton = new JButton("Add Session");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        createSessionPanel.add(addSessionButton, gbc);

        // Action Listener
        addSessionButton.addActionListener(e -> addSession());
    }

    /**
     * Adds a new session based on user input.
     */
    private void addSession() {
        String title = sessionTitleField.getText().trim();
        String dateTime = sessionDateTimeField.getText().trim();
        Group selectedGroup = (Group) sessionGroupComboBox.getSelectedItem();

        if (title.isEmpty() || dateTime.isEmpty() || selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Incomplete Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create Session
        Session session = teacher.createSession(title, dateTime, selectedGroup.getChapterId(), selectedGroup.getId());
        sessions.add(session);

        // Update sessions.csv using toStringCSV()
        CSVUtil.writeToCSV("sessions.csv", sessions, s -> s.toStringCSV(),
                "id,title,dateTime,chapterId,groupId");

        JOptionPane.showMessageDialog(this, "Session added successfully.", "Success",
                JOptionPane.INFORMATION_MESSAGE);

        // Notify Students
        List<String> studentIds = selectedGroup.getStudentIds();
        List<Student> enrolledStudents = students.stream()
                .filter(s -> studentIds.contains(s.getId()))
                .collect(Collectors.toList());
        session.notifyStudents(enrolledStudents);

        // Clear fields
        sessionTitleField.setText("");
        sessionDateTimeField.setText("");
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
     * Retrieves the teacher's name by their ID.
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
     * Main method to launch the Teacher Dashboard for testing purposes.
     */
    public static void main(String[] args) {
        // For testing: Create a sample teacher and launch the dashboard
        Teacher sampleTeacher = new Teacher("t1a2b3c4-d5e6-f7g8-h9i0-1234567890ab", "Alice Johnson", "alicePass");
        new TeacherDashboard(sampleTeacher);
    }
}
