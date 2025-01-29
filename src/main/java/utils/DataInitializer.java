package utils;

import model.user.*;
import model.subject.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors; // Import for Collectors

public class DataInitializer {

        /**
         * Initializes and serializes dummy data for teachers, students, groups,
         * subjects, chapters, materials, tests, and sessions.
         *
         * @param teacher The primary teacher to assign to some groups.
         */
        public static void initializeData(Teacher teacher) {
                initializeUsers(teacher);
                initializeSubjects();
                initializeChapters();
                initializeGroups(teacher);
                initializeMaterials();
                initializeTests();
                initializeSessions();
        }

        /**
         * Overloaded method to initialize data with a default teacher.
         */
        public static void initializeData() {
                // Create a default teacher
                Teacher defaultTeacher = new Teacher("default-teacher-id", "Default Teacher", "defaultPass");
                initializeData(defaultTeacher);
        }

        private static void initializeUsers(Teacher teacher) {
                List<User> existingUsers = UserStorage.getUsers();
                if (!existingUsers.isEmpty()) {
                        System.out.println("Users already initialized. Skipping...");
                        return;
                }

                // Create sample teachers
                Teacher teacher1 = teacher;
                Teacher teacher2 = new Teacher("t2b3c4d5-e6f7-g8h9-i0j1-234567890abc", "David Lee", "davidPass");

                // Create sample students
                Student student1 = new Student("s1a2b3c4-d5e6-f7g8-h9i0-0987654321ba", "Bob Smith", "bobPass", 50.0);
                Student student2 = new Student("s2c3d4e5-f6g7-h8i9-j0k1-234567890abc", "Carol White", "carolPass",
                                30.0);
                Student student3 = new Student("s3f6g7h8-i0j1-k2l3-m4n5-67890abcdefg", "Eve Black", "evePass", 20.0);
                Student student4 = new Student("s4g7h8i9-j1k2-l3m4-n5o6-7890abcdefgh", "Frank Green", "frankPass",
                                40.0);

                // Add users to UserStorage
                UserStorage.addUser(teacher1);
                UserStorage.addUser(teacher2);
                UserStorage.addUser(student1);
                UserStorage.addUser(student2);
                UserStorage.addUser(student3);
                UserStorage.addUser(student4);

                System.out.println("Users initialized and serialized.");
        }

        private static void initializeSubjects() {
                String filePath = "subjects.ser";
                List<Subject> existingSubjects = SerializationUtil.readFromFile(filePath);
                if (existingSubjects != null && !existingSubjects.isEmpty()) {
                        System.out.println("Subjects already initialized. Skipping...");
                        return;
                }

                // Initialize Subjects
                Subject math = new Subject("sub1", "Mathematics", "Core", 100.0);
                Subject physics = new Subject("sub2", "Physics", "Core", 120.0);
                Subject history = new Subject("sub3", "History", "Elective", 80.0);

                ArrayList<Subject> subjects = new ArrayList<>(Arrays.asList(math, physics, history));
                SerializationUtil.saveDataToDisk(subjects, filePath);

                System.out.println("Subjects initialized and serialized.");
        }

        private static void initializeChapters() {
                String filePath = "chapters.ser";
                List<Chapter> existingChapters = SerializationUtil.readFromFile(filePath);
                if (existingChapters != null && !existingChapters.isEmpty()) {
                        System.out.println("Chapters already initialized. Skipping...");
                        return;
                }

                // Initialize Chapters
                Chapter chap1 = new Chapter("chap1", "Algebra", "sub1", "Core Concepts");
                Chapter chap2 = new Chapter("chap2", "Mechanics", "sub2", "Newtonian Physics");
                Chapter chap3 = new Chapter("chap3", "World War II", "sub3", "Historical Events");

                ArrayList<Chapter> chapters = new ArrayList<>(Arrays.asList(chap1, chap2, chap3));
                SerializationUtil.saveDataToDisk(chapters, filePath);

                System.out.println("Chapters initialized and serialized.");
        }

        private static void initializeGroups(Teacher teacher) {
                String filePath = "groups.ser";
                List<Group> existingGroups = SerializationUtil.readFromFile(filePath);
                if (existingGroups != null && !existingGroups.isEmpty()) {
                        System.out.println("Groups already initialized. Skipping...");
                        return;
                }

                // Load existing users to assign to groups
                List<User> allUsers = UserStorage.getUsers();
                List<Teacher> teachers = allUsers.stream()
                                .filter(u -> u instanceof Teacher)
                                .map(u -> (Teacher) u)
                                .collect(Collectors.toList());
                List<Student> students = allUsers.stream()
                                .filter(u -> u instanceof Student)
                                .map(u -> (Student) u)
                                .collect(Collectors.toList());

                // Initialize Groups
                Group grp1 = new Group("grp1", "Algebra Group 1", "chap1",
                                Arrays.asList(teachers.get(0).getId(), teachers.get(1).getId()));
                grp1.addStudent(students.get(0).getId());
                grp1.addStudent(students.get(1).getId());
                // Add a duplicate to test uniqueness
                grp1.addStudent(students.get(0).getId());

                Group grp2 = new Group("grp2", "Mechanics Group 1", "chap2",
                                Arrays.asList(teachers.get(1).getId()));
                grp2.addStudent(students.get(2).getId());
                grp2.addStudent(students.get(3).getId());

                Group grp3 = new Group("grp3", "World War II Group 1", "chap3",
                                Arrays.asList(teachers.get(0).getId()));
                grp3.addStudent(students.get(0).getId());
                grp3.addStudent(students.get(2).getId());

                ArrayList<Group> groups = new ArrayList<>(Arrays.asList(grp1, grp2, grp3));
                SerializationUtil.saveDataToDisk(groups, filePath);

                System.out.println("Groups initialized and serialized.");
        }

        private static void initializeMaterials() {
                String filePath = "materials.ser";
                List<Material> existingMaterials = SerializationUtil.readFromFile(filePath);
                if (existingMaterials != null && !existingMaterials.isEmpty()) {
                        System.out.println("Materials already initialized. Skipping...");
                        return;
                }

                // Initialize Materials
                Material mat1 = new Material("mat1", "Algebra Basics", "pdf", "/materials/algebra_basics.pdf", "chap1");
                Material mat2 = new Material("mat2", "Mechanics Video", "video", "http://videos.com/mechanics",
                                "chap2");
                Material mat3 = new Material("mat3", "WWII Image Collection", "image", "/materials/wwii_images/",
                                "chap3");

                ArrayList<Material> materials = new ArrayList<>(Arrays.asList(mat1, mat2, mat3));
                SerializationUtil.saveDataToDisk(materials, filePath);

                System.out.println("Materials initialized and serialized.");
        }

        private static void initializeTests() {
                String filePath = "tests.ser";
                List<Test> existingTests = SerializationUtil.readFromFile(filePath);
                if (existingTests != null && !existingTests.isEmpty()) {
                        System.out.println("Tests already initialized. Skipping...");
                        return;
                }

                // Initialize Tests
                Test test1 = new Test("test1", "chap1", "Algebra Midterm", "2025-03-01 09:00", 90);
                test1.addQuestion(new Question("q1", "test1", "What is 2+2?",
                                new String[] { "3", "4", "5", "6" }, 1));
                test1.addQuestion(new Question("q2", "test1", "Solve for x: 2x = 10.",
                                new String[] { "3", "4", "5", "6" }, 2));

                Test test2 = new Test("test2", "chap2", "Mechanics Final", "2025-04-15 14:00", 120);
                test2.addQuestion(new Question("q3", "test2", "What is Newton's Second Law?",
                                new String[] { "F=ma", "E=mc^2", "V=IR", "P=IV" }, 0));
                test2.addQuestion(new Question("q4", "test2", "Define acceleration.",
                                new String[] { "Change in velocity over time", "Change in position over time",
                                                "Change in mass over time", "Change in force over time" },
                                0));

                ArrayList<Test> tests = new ArrayList<>(Arrays.asList(test1, test2));
                SerializationUtil.saveDataToDisk(tests, filePath);

                System.out.println("Tests initialized and serialized.");
        }

        private static void initializeSessions() {
                String filePath = "sessions.ser";
                List<Session> existingSessions = SerializationUtil.readFromFile(filePath);
                if (existingSessions != null && !existingSessions.isEmpty()) {
                        System.out.println("Sessions already initialized. Skipping...");
                        return;
                }

                // Initialize Sessions
                Session session1 = new Session("sess1", "Algebra Workshop", "2025-02-20 10:00", "chap1", "grp1");
                Session session2 = new Session("sess2", "Mechanics Lab", "2025-03-10 13:00", "chap2", "grp2");

                ArrayList<Session> sessions = new ArrayList<>(Arrays.asList(session1, session2));
                SerializationUtil.saveDataToDisk(sessions, filePath);

                System.out.println("Sessions initialized and serialized.");
        }

        /**
         * Main method to initialize all data.
         */
        public static void main(String[] args) {
                // Initialize data using the default teacher
                initializeData();

                System.out.println("All data initialization processes completed successfully.");
        }
}
