package model.subject;

import model.user.Student;
import model.user.Teacher;
import model.user.User;
import model.user.UserStorage;
import utils.SerializationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SessionRoom extends JDialog {
    private Session session;
    private User currentUser;

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private DefaultTableModel participantsTableModel;
    private JTable participantsTable;
    private JButton removeStudentButton;
    private Timer refreshTimer;

    public SessionRoom(JFrame parent, Session session, User user) {
        super(parent, "Session: " + session.getTitle(), true);
        this.session = session;
        this.currentUser = user;
        initComponents();
        startRefreshTimer();
    }

    private void initComponents() {
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Chat Panel
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatPanel.add(chatScroll, BorderLayout.CENTER);

        // In a teacher session room the teacher can send messages.
        // (For students, you might even remove the inputPanel altogether.)
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Send");
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        add(chatPanel, BorderLayout.CENTER);

        // Participants Panel
        JPanel participantsPanel = new JPanel(new BorderLayout());
        participantsTableModel = new DefaultTableModel(new String[] { "ID", "Name" }, 0);
        participantsTable = new JTable(participantsTableModel);
        JScrollPane partScroll = new JScrollPane(participantsTable);
        participantsPanel.add(partScroll, BorderLayout.CENTER);

        // Only add removal button if the current user is a teacher.
        if (currentUser instanceof Teacher) {
            removeStudentButton = new JButton("Remove Student");
            removeStudentButton.addActionListener(e -> removeSelectedStudent());
            participantsPanel.add(removeStudentButton, BorderLayout.SOUTH);
        }

        add(participantsPanel, BorderLayout.EAST);

        // Listeners for sending messages
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        loadMessages();
        loadParticipants();
    }

    private void startRefreshTimer() {
        refreshTimer = new Timer();
        refreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    // Reload the session from disk
                    ArrayList<Session> sessionsList = SerializationUtil.readFromFile("sessions.ser");
                    if (sessionsList != null) {
                        for (Session s : sessionsList) {
                            if (s.getId().equals(session.getId())) {
                                session = s;
                                break;
                            }
                        }
                    }
                    // --- NEW CODE: Check if a student has been removed ---
                    if (currentUser instanceof Student) {
                        if (!session.getStudentIds().contains(currentUser.getId())) {
                            // Notify the student and close the window.
                            JOptionPane.showMessageDialog(thisDialog(),
                                    "You have been removed from this session by the teacher.",
                                    "Session Ended", JOptionPane.INFORMATION_MESSAGE);
                            dispose();
                            return; // Stop further updates.
                        }
                    }
                    // ----------------------------------------------------------
                    loadMessages();
                    loadParticipants();
                });
            }
        }, 0, 5000);
    }

    // Utility method to return a reference to the current dialog (for use in
    // JOptionPane)
    private Component thisDialog() {
        return this;
    }

    private void loadMessages() {
        chatArea.setText("");
        session.getMessages().forEach(msg -> chatArea.append(msg.getSenderName() + ": " + msg.getContent() + "\n"));
    }

    private void loadParticipants() {
        participantsTableModel.setRowCount(0);
        session.getTeacherIds().forEach(tId -> {
            User u = UserStorage.getUserById(tId);
            if (u != null)
                participantsTableModel.addRow(new Object[] { u.getId(), u.getName() });
        });
        session.getStudentIds().forEach(sId -> {
            User u = UserStorage.getUserById(sId);
            if (u != null)
                participantsTableModel.addRow(new Object[] { u.getId(), u.getName() });
        });
    }

    private void sendMessage() {
        String content = messageField.getText().trim();
        if (content.isEmpty())
            return;

        Message msg = new Message(currentUser.getId(), currentUser.getName(), content);
        session.addMessage(msg);
        saveSession();
        messageField.setText("");
        loadMessages();
    }

    private void removeSelectedStudent() {
        int row = participantsTable.getSelectedRow();
        if (row == -1)
            return;

        String id = (String) participantsTableModel.getValueAt(row, 0);
        if (session.getStudentIds().contains(id)) {
            session.removeStudent(id);
            saveSession();
            loadParticipants();
        }
    }

    private void saveSession() {
        ArrayList<Session> sessions = SerializationUtil.readFromFile("sessions.ser");
        sessions.replaceAll(s -> s.getId().equals(session.getId()) ? session : s);
        SerializationUtil.saveDataToDisk(sessions, "sessions.ser");
    }

    @Override
    public void dispose() {
        super.dispose();
        if (refreshTimer != null)
            refreshTimer.cancel();
    }
}
