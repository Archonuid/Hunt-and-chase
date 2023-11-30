package test2;

import java.awt.Point;

public class AnimalRecord {
    private int id;
    private int age;
    private String type; // "Rabbit" or "Fox"
    private Point position;

    public AnimalRecord(int id, int age, String type, Point position) {
        this.id = id;
        this.age = age;
        this.type = type;
        this.position = position;
    }

    // Getters and setters
    public int getId() { return id; }
    public int getAge() { return age; }
    public String getType() { return type; }
    public Point getPosition() { return position; }

    public void setAge(int age) { this.age = age; }
    public void setPosition(Point position) { this.position = position; }
}
