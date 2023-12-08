package test2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Main extends JFrame {
    private Plant grassField;
    private static List<Prey> rabbits = new ArrayList<>();
    private static List<Predator> foxes = new ArrayList<>();
    private int screenHeight = 600; // Replace 600 with your actual screen height
    private int screenWidth = 800; // Replace 800 with your actual screen width
    public static List<Prey> getRabbits() {
        return rabbits;
    }
    public static List<Predator> getFoxes() {
        return foxes;
    }

    public Main() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Planet Life Simulation");
        setSize(screenWidth, screenHeight);

        // Get the actual screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();

        grassField = new Plant();
        rabbits = Collections.synchronizedList(new ArrayList<>());
        foxes = Collections.synchronizedList(new ArrayList<>());

     // Spawn 12 rabbits
        for (int i = 0; i < 20; i++) {
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
            
            // Randomly assign sex (true for male, false for female)
            boolean isMale = Math.random() < 0.5;
            
            Prey rabbit = new Prey(startX, startY, speed, directionX, directionY, isMale);
            rabbit.transitionAge(age); // Set the age
            rabbits.add(rabbit);
        }

     // Spawn 4 foxes
        for (int i = 0; i < 10; i++) {
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
            
            // Randomly assign sex (true for male, false for female)
            boolean isMale = Math.random() < 0.5;

            Predator fox = new Predator(startX, startY, speed, directionX, directionY, isMale);
            fox.transitionAge(age); // Set the age
            foxes.add(fox);
        }

        // Start the simulation loop
        new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveRabbits(); // Adjust rabbits' positions
                moveFoxes(); // Adjust foxes' positions
                repaint(); // Trigger repaint to update the graphics
            }
        }).start();
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
                        if (rabbit.getAge() == 2 && !rabbit.isMating()) {
                            initiateReproduction(rabbit);
                        }
                        break;
                }
            }
        }
    }

    private void initiateReproduction(Prey rabbit) {
        if (rabbit.isHungry() || rabbit.isEaten()) {
            return; // Don't reproduce if hungry or eaten
        }

        long currentTime = System.currentTimeMillis();
        long timeSinceLastMating = currentTime - rabbit.getLastSuccessfulMatingTime();

        if (timeSinceLastMating >= Constants.MATING_CYCLE) {
            rabbit.setMating(true);
            rabbit.mates();
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
                        if (fox.getAge() == 2 && !fox.isMating()) {
                            initiateFoxReproduction(fox);
                        }
                        break;
                }
                huntAndEat(fox);
            }
        }
    }

    private void initiateFoxReproduction(Predator fox) {
        if (fox.isHungry() || fox.isAlive()) {
            return; // Don't reproduce if hungry or eaten
        }
        long currentTime = System.currentTimeMillis();
        long timeSinceLastMating = currentTime - fox.getLastSuccessfulMatingTime();
        if (timeSinceLastMating >= Constants.MATING_CYCLE) {
            fox.setMating(true);
            fox.mates();
        }
    }

    private void huntAndEat(Predator fox) {
        List<Prey> nearbyRabbits = findNearbyPrey(fox, rabbits);

        if (!nearbyRabbits.isEmpty()) {
            fox.chase(nearbyRabbits); // The hunt method in the Predator class will handle the hunting logic
        }
    }

    private List<Prey> findNearbyPrey(Predator fox, List<Prey> potentialPrey) {
        List<Prey> nearbyPrey = new ArrayList<>();

        for (Prey prey : potentialPrey) {
            double distance = Constants.calculateDistance(fox.getX(), fox.getY(), prey.getX(), prey.getY());
            if (distance <= Constants.HUNTING_RANGE) {
                nearbyPrey.add(prey);
            }
        }

        return nearbyPrey;
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true)); // LAMBDA function, what does it do
    }
    
    // Method to remove a rabbit from the list
    public static void removeRabbit(Prey rabbit) {
        rabbits.remove(rabbit);
    }

    // Method to remove a rabbit from the list
    public static void removeFox(Predator fox) {
        foxes.remove(fox);
    }
    
    public static void addRabbit(Prey rabbit) {
        rabbits.add(rabbit);
    }
    
    public static void addFox(Predator fox) {
        foxes.add(fox);
    }
}