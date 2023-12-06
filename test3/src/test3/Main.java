package test3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

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
        rabbits = new ArrayList<>();
        foxes = new ArrayList<>();

     // Spawn 12 rabbits
        for (int i = 0; i < 25; i++) {
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
        for (int i = 0; i < 15; i++) {
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
        List<Prey> newRabbits = new ArrayList<>();

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

                Prey offspring = rabbit.getOffspring();
                if (offspring != null) {
                    newRabbits.add(offspring);
                }
            }
        }

        // Adding new offspring to the simulation
        rabbits.addAll(newRabbits);
    }

    private void moveFoxes() {
        List<Predator> newFoxes = new ArrayList<>();

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

                Predator offspring = fox.getOffspring();
                if (offspring != null) {
                    newFoxes.add(offspring);
                }
            }
        }

        // Adding new offspring to the simulation
        foxes.addAll(newFoxes);
    }
    
    private void handleMatingSeason() {
        handleFoxMating();
        handleRabbitMating();
        handleOffspring();
    }
    
    private void handleFoxMating() {
        for (Predator fox : foxes) {
            if (fox != null) {
                fox.startMatingSeason();
            }
        }
    }
    
    private void handleRabbitMating() {
        for (Prey rabbit : rabbits) {
            if (rabbit != null) {
                rabbit.startMatingSeason();
            }
        }
    }
    
    private void handleOffspring() {
        List<Prey> newRabbits = new ArrayList<>();
        List<Predator> newFoxes = new ArrayList<>();

        // Handling rabbit offspring
        for (Prey rabbit : rabbits) {
            if (rabbit != null) {
                Prey offspring = rabbit.getOffspring();
                if (offspring != null) {
                    newRabbits.add(offspring);
                }
            }
        }

        // Handling fox offspring
        for (Predator fox : foxes) {
            if (fox != null) {
                Predator offspring = fox.getOffspring();
                if (offspring != null) {
                    newFoxes.add(offspring);
                }
            }
        }

        // Adding new offspring to the simulation
        rabbits.addAll(newRabbits);
        foxes.addAll(newFoxes);
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
        SwingUtilities.invokeLater(() -> {
            Main main = new Main();
            new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    main.moveRabbits();
                    main.moveFoxes();
                    main.handleMatingSeason();
                    main.repaint();
                }
            }).start();
            main.setVisible(true);
        });
    }
    
    // Method to remove a rabbit from the list
    public static void removeRabbit(Prey rabbit) {
        rabbits.remove(rabbit);
    }

    // Method to remove a rabbit from the list
    public static void removeFox(Predator fox) {
        foxes.remove(fox);
    }
}