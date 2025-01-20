package utils;

import java.io.*;
import java.util.*;
import java.util.function.Function;

public class CSVUtil {

    /**
     * Reads data from a CSV file and maps each row to an object using the provided
     * mapper.
     *
     * @param <T>      The type of object to map to.
     * @param fileName The name of the CSV file.
     * @param mapper   Function to map a String array to an object.
     * @return A list of objects.
     */
    public static <T> List<T> readFromCSV(String fileName, Function<String[], T> mapper) {
        List<T> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                // Skip header
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] fields = splitCSVLine(line);
                T obj = mapper.apply(fields);
                if (obj != null) {
                    list.add(obj);
                } else {
                    System.err.println("Skipping malformed line " + lineNumber + " in " + fileName + ": " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading " + fileName + ": " + e.getMessage());
            // e.printStackTrace();
        }
        return list;
    }

    /**
     * Writes data to a CSV file from a list of objects using the provided mapper.
     *
     * @param <T>      The type of objects in the list.
     * @param fileName The name of the CSV file.
     * @param data     The list of objects to write.
     * @param mapper   Function to map an object to a CSV string.
     * @param header   The header line of the CSV.
     */
    public static <T> void writeToCSV(String fileName, List<T> data, Function<T, String> mapper, String header) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            // Write header
            bw.write(header);
            bw.newLine();
            // Write data
            for (T obj : data) {
                bw.write(mapper.apply(obj));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to " + fileName + ": " + e.getMessage());
            // e.printStackTrace();
        }
    }

    /**
     * Splits a CSV line into fields, handling quoted commas.
     *
     * @param line The CSV line.
     * @return An array of fields.
     */
    private static String[] splitCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes; // Toggle state
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField.setLength(0); // Reset the buffer
            } else {
                currentField.append(c);
            }
        }
        // Add the last field
        fields.add(currentField.toString().trim());
        return fields.toArray(new String[0]);
    }
}
