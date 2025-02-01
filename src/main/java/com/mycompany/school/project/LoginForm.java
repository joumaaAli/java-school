package com.mycompany.school.project;

import javax.swing.*;
import model.user.Student;
import model.user.Teacher;
import model.user.User;
import model.user.UserStorage;

public class LoginForm extends javax.swing.JFrame {

    public LoginForm() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        titleLabel = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        loginButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Login Form");

        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        titleLabel.setText("Login");

        nameLabel.setText("Name:");

        passwordLabel.setText("Password:");

        nameField.setText("");

        passwordField.setText("");

        loginButton.setText("Login");
        loginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });

        // Layout configuration
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(titleLabel, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(nameLabel)
                                        .addComponent(nameField)
                                        .addComponent(passwordLabel)
                                        .addComponent(passwordField)
                                        .addComponent(loginButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(50, Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addComponent(titleLabel)
                                .addGap(20, 20, 20)
                                .addComponent(nameLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(passwordLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(loginButton)
                                .addContainerGap(30, Short.MAX_VALUE)));

        pack();
    }

    private void loginButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String name = nameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (name.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Password cannot be empty.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = UserStorage.findUserByNameAndPassword(name, password);

        if (user != null) {
            JOptionPane.showMessageDialog(this, "Welcome, " + user.getRole() + " " + user.getName(), "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Open appropriate dashboard based on role
            if (user instanceof Teacher) {
                new TeacherDashboard((Teacher) user).setVisible(true);
            } else if (user instanceof Student) {
                new StudentDashboard((Student) user).setVisible(true);
            }

            // Close login form
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LoginForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LoginForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LoginForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoginForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        // </editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LoginForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify
    private javax.swing.JButton loginButton;
    private javax.swing.JComboBox<String> roleComboBox; // Removed if not needed
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel roleLabel; // Removed if not needed
    private javax.swing.JLabel titleLabel;
    // End of variables declaration
}
