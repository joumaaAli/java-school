package model.user;

import utils.SerializationUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserStorage implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String FILE_PATH = "users.ser";
    private static ArrayList<User> users = loadUsers();

    /**
     * Loads users from the serialized file.
     *
     * @return A list of users.
     */
    private static ArrayList<User> loadUsers() {
        ArrayList<User> loadedUsers = SerializationUtil.readFromFile(FILE_PATH);
        if (loadedUsers == null) {
            loadedUsers = new ArrayList<>();
            System.out.println("No existing users found. Starting with an empty user list.");
        }
        return loadedUsers;
    }

    /**
     * Adds a new user and saves the updated list to disk.
     *
     * @param user The user to add.
     */
    public static void addUser(User user) {
        users.add(user);
        saveUsers();
    }

    /**
     * Saves the current list of users to disk.
     */
    private static void saveUsers() {
        SerializationUtil.saveDataToDisk(users, FILE_PATH);
    }

    /**
     * Finds a user by name and password.
     *
     * @param name     The user's name.
     * @param password The user's password.
     * @return The matching user or null if not found.
     */
    public static User findUserByNameAndPassword(String name, String password) {
        return users.stream()
                .filter(user -> user.getName().equals(name) && user.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves all users.
     *
     * @return A list of all users.
     */
    public static ArrayList<User> getUsers() {
        return users;
    }

    /**
     * Updates the users list and saves it to disk.
     *
     * @param updatedUsers The updated list of users.
     */
    public static void updateUsers(ArrayList<User> updatedUsers) {
        users = updatedUsers;
        saveUsers();
    }
}
