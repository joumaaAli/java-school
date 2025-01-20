package model.subject;

public class Subject {
    private String id;
    private String name;
    private String domain;
    private double cost;

    public Subject(String id, String name, String domain, double cost) {
        this.id = id;
        this.name = name;
        this.domain = domain;
        this.cost = cost;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDomain() {
        return domain;
    }

    public double getCost() {
        return cost;
    }

    public String toString() {
        // For CSV writing
        // Format: "id,name,domain,cost"
        return id + "," + name + "," + domain + "," + cost;
    }

    public String toStringForDisplay() {
        return name + " (" + domain + ") - Cost: $" + cost;
    }

}
