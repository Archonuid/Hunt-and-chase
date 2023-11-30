package test2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {
	private static int nextId = 1;
	private List<AnimalRecord> animalRecords;
    private Plant grassField;
    private List<Prey> rabbits;
    private List<Predator> foxes;
    private int screenHeight = 600; // Replace 600 with your actual screen height
    private int screenWidth = 800; // Replace 800 with your actual screen width

    public Main() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Planet Life Simulation");
        setSize(screenWidth, screenHeight);

        animalRecords = new ArrayList<>(); // Initialize the animal records list
        grassField = new Plant();
        rabbits = new ArrayList<>();
        foxes = new ArrayList<>();

        // Spawn Rabbits
        spawnRabbits();

        // Spawn Foxes
        spawnFoxes();

        // Start the simulation loop
        new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveRabbits();
                moveFoxes();
                repaint();
            }
        }).start();
    }
    
    private void spawnRabbits() {
    	for (int i = 0; i < 12; i++) {
            int age = (int) (Math.random() * 3); // Randomly assign age (excluding adult for initial spawn)
            int size;
            int speed;

            if (age == 0) { // Baby
                size = Constants.BABY_SIZE;
                speed = (int) (Constants.ADULT_SIZE * Constants.BABY_SPEED_FACTOR); // Half the speed of adults
            } else if (age == 1) { // Young
                size = Constants.YOUNG_SIZE;
                speed = (int) (Constants.ADULT_SIZE * Constants.YOUNG_SPEED_FACTOR); // 75% the speed of adults
            } else { // Adult
                size = Constants.ADULT_SIZE;
                speed = Constants.ADULT_SIZE;
                age = 2; // Set age to adult
            }

            int startX = (int) (Math.random() * screenWidth); // Random starting position
            int startY = (int) (Math.random() * screenHeight);
            int directionX = Math.random() < 0.5 ? -1 : 1; // Random direction
            int directionY = Math.random() < 0.5 ? -1 : 1;

            // Create a new rabbit and add it to the list
            Prey rabbit = new Prey(startX, startY, speed, directionX, directionY);
            rabbit.transitionAge(age); // Set the age
            rabbits.add(rabbit);
            animalRecords.add(new AnimalRecord(nextId++, age, "Rabbit", new Point(startX, startY)));
        }
    }
    
    private void spawnFoxes() {
        for (int i = 0; i < 4; i++) {
            int age = (int) (Math.random() * 3); // Randomly assign age (excluding adult for initial spawn)
            int size;
            int speed;

            if (age == 0) { // Baby
                size = Constants.BABY_SIZE;
                speed = (int) (Constants.ADULT_SIZE * Constants.BABY_SPEED_FACTOR); // Half the speed of adults
            } else if (age == 1) { // Young
                size = Constants.YOUNG_SIZE;
                speed = (int) (Constants.ADULT_SIZE * Constants.YOUNG_SPEED_FACTOR); // 75% the speed of adults
            } else { // Adult
                size = Constants.ADULT_SIZE;
                speed = Constants.ADULT_SIZE;
                age = 2; // Set age to adult
            }

            int startX = (int) (Math.random() * screenWidth); // Random starting position
            int startY = (int) (Math.random() * screenHeight);
            int directionX = Math.random() < 0.5 ? -1 : 1; // Random direction
            int directionY = Math.random() < 0.5 ? -1 : 1;

            // Pass 'this' as an argument to the Predator constructor
            Predator fox = new Predator(startX, startY, speed, directionX, directionY, this);
            fox.transitionAge(age); // Set the age
            foxes.add(fox);
            animalRecords.add(new AnimalRecord(nextId++, age, "Fox", new Point(startX, startY)));
        }
    }


    private void moveRabbits() {
        for (Prey rabbit : rabbits) {
            if (rabbit != null) {
                switch (rabbit.getAge()) {
                    case 0:
                        rabbit.moveBabyRabbit();
                        break;
                    case 1:
                        rabbit.moveYoungRabbit();
                        break;
                    case 2:
                        rabbit.move();
                        break;
                }
            }
        }
    }

    private void moveFoxes() {
        for (Predator fox : foxes) {
            if (fox != null) {
                switch (fox.getAge()) {
                    case 0:
                        fox.moveBabyFox();
                        break;
                    case 1:
                        fox.moveYoungFox();
                        break;
                    case 2:
                        fox.move();
                        break;
                }
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        grassField.draw(g, getWidth(), getHeight());
        for (Prey rabbit : rabbits) {
            rabbit.draw(g);
        }
        for (Predator fox : foxes) {
            fox.draw(g);
        }
    }

    public List<AnimalRecord> getAnimalRecords() {
        return animalRecords;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
    
    public void calculateDistances() {
        for (AnimalRecord a : animalRecords) {
            if (a.getType().equals("Rabbit")) {
                for (AnimalRecord b : animalRecords) {
                    if (b.getType().equals("Fox")) {
                        double distance = a.getPosition().distance(b.getPosition());
                        // Use this distance as needed for logic
                    }
                }
            }
        }
    }
}