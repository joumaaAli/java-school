package model.user;

import utils.IDGenerator;

public class UserFactory {
    // Factory method to create User based on role
    public static User createUser(String role, String name, String password) {
        String id = IDGenerator.generateID();
        if (role.equalsIgnoreCase("Teacher")) {
            return new Teacher(id, name, password);
        } else if (role.equalsIgnoreCase("Student")) {
            return new Student(id, name, password, 0.0); // Initial balanceDue is 0.0
        }
        throw new IllegalArgumentException("Invalid role: " + role);
    }
}
