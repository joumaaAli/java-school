package utils;

import java.io.*;

public class SerializationUtil {

    /**
     * Saves a serializable object to disk.
     *
     * @param <T>      The type of the object.
     * @param obj      The object to save.
     * @param filePath The file path to save the object.
     */
    public static <T extends Serializable> void saveDataToDisk(T obj, String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(obj);
            oos.flush();
            System.out.println("Data successfully saved to " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving data to " + filePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reads a serializable object from disk.
     *
     * @param <T>      The type of the object.
     * @param filePath The file path to read the object from.
     * @return The deserialized object, or null if an error occurred.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T readFromFile(String filePath) {
        T obj = null;
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("File " + filePath + " does not exist.");
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            obj = (T) ois.readObject();
            System.out.println("Data successfully loaded from " + filePath);
        } catch (IOException e) {
            System.err.println("Error reading from " + filePath + ": " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found while reading from " + filePath + ": " + e.getMessage());
            e.printStackTrace();
        }
        return obj;
    }
}
