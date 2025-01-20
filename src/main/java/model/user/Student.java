package model.user;

public class Student extends User {
    private double balanceDue;

    public Student(String id, String name, String password, double balanceDue) {
        super(id, name, password);
        this.balanceDue = balanceDue;
    }

    public double getBalance() {
        return balanceDue;
    }

    public void addBalance(double amount) {
        balanceDue += amount;
    }

    public void setBalance(double amount) {
        balanceDue = amount;
    }

    public void pay(double amount) {
        balanceDue -= amount;
    }

    public String getRole() {
        return "Student";
    }

    @Override
    public String toString() {
        return this.getId() + "," + this.getName() + "," + "Student" + "," + balanceDue;
    }
}
