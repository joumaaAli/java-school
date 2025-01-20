package model.user;

import utils.CSVUtil;

import java.util.ArrayList;
import java.util.List;

public class UserStorage {
    private static final String FILE_PATH = "users.csv";
    private static List<User> users = loadUsers();

    // Load users from CSV
    private static List<User> loadUsers() {
        List<User> loadedUsers = CSVUtil.readFromCSV(FILE_PATH, fields -> {
            try {
                String id = fields[0];
                String name = fields[1];
                String password = fields[2];
                String role = fields[3];
                if (role.equalsIgnoreCase("Teacher")) {
                    return new Teacher(id, name, password);
                } else if (role.equalsIgnoreCase("Student")) {
                    double balanceDue = fields.length >= 5 ? Double.parseDouble(fields[4]) : 0.0;
                    return new Student(id, name, password, balanceDue);
                }
                throw new IllegalArgumentException("Invalid role: " + role);
            } catch (Exception e) {
                System.err.println("Error parsing user: " + String.join(",", fields));
                return null; // Skip invalid rows
            }
        });

        // Filter out null entries (invalid users)
        List<User> validUsers = new ArrayList<>();
        for (User user : loadedUsers) {
            if (user != null) {
                validUsers.add(user);
            }
        }
        return validUsers;
    }

    // Add a user and save to CSV
    public static void addUser(User user) {
        users.add(user);
        saveUsers();
    }

    // Save all users to CSV
    private static void saveUsers() {
        CSVUtil.writeToCSV(FILE_PATH, users,
                u -> {
                    if (u instanceof Teacher) {
                        return u.toString(); // Teacher's toString includes a trailing comma for balanceDue
                    } else if (u instanceof Student) {
                        return u.toString(); // Student's toString includes balanceDue
                    }
                    return "";
                },
                "ID,Name,Password,Role,BalanceDue");
    }

    // Find a user by name and password
    public static User findUserByNameAndPassword(String name, String password) {
        return users.stream()
                .filter(user -> user.getName().equals(name) && user.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    // Get all users
    public static List<User> getUsers() {
        return users;
    }
}
