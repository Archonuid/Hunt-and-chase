package test2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Main extends JFrame {
    private Plant grassField;
    private static List<Prey> rabbits = new ArrayList<>();
    private static List<Predator> foxes = new ArrayList<>();
    private int screenHeight = 600;
    private int screenWidth = 800; 
    public static List<Prey> getRabbits() {
        return rabbits;
    }
    public static List<Predator> getFoxes() {
        return foxes;
    }
    private static XYSeries rabbitSeries = new XYSeries("Rabbits");
    private static XYSeries foxSeries = new XYSeries("Foxes");
    private static ChartPanel chartPanel;
    private long simulationStartTime = System.currentTimeMillis();

    public Main() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Planet Life Simulation");
        setSize(screenWidth, screenHeight);
        rabbitSeries = new XYSeries("Rabbits");
        foxSeries = new XYSeries("Foxes");
        createChart();

        // actual screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();

        grassField = new Plant();
        rabbits = Collections.synchronizedList(new ArrayList<>());
        foxes = Collections.synchronizedList(new ArrayList<>());

     // Spawn 10 rabbits
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
                age = 2; // set age to adult
            }

            int startX = (int) (Math.random() * screenWidth); // Random starting position
            int startY = (int) (Math.random() * screenHeight);
            int directionX = Math.random() < 0.5 ? -1 : 1; // Random direction
            int directionY = Math.random() < 0.5 ? -1 : 1;
            
            // Randomly assign sex (true for male, false for female)
            boolean isMale = Math.random() < 0.5;
            
            Prey rabbit = new Prey(startX, startY, speed, directionX, directionY, isMale);
            rabbit.transitionAge(age); // set age
            rabbits.add(rabbit);
        }

        // Spawn 10 foxes
        List<Predator> newFoxes = new ArrayList<>();
        int numberOfAdultMales = 2;
        int numberOfAdultFemales = 2;
        int numberOfRemainingFoxes = 6;

        for (int j = 0; j < numberOfAdultMales; j++) {
            // For adult males
            int age = 2; // Set age to adult
            int size = Constants.ADULT_SIZE;
            int speed = Constants.ADULT_SIZE;
            int startX = (int) (Math.random() * screenWidth);
            int startY = (int) (Math.random() * screenHeight);
            int directionX = Math.random() < 0.5 ? -1 : 1;
            int directionY = Math.random() < 0.5 ? -1 : 1;

            Predator fox = new Predator(startX, startY, speed, directionX, directionY, true); // true for male
            fox.transitionAge(age); // Set age
            newFoxes.add(fox);
        }

        for (int j = 0; j < numberOfAdultFemales; j++) {
            // For adult females
            int age = 2; // Set age to adult
            int size = Constants.ADULT_SIZE;
            int speed = Constants.ADULT_SIZE;
            int startX = (int) (Math.random() * screenWidth);
            int startY = (int) (Math.random() * screenHeight);
            int directionX = Math.random() < 0.5 ? -1 : 1;
            int directionY = Math.random() < 0.5 ? -1 : 1;

            Predator fox = new Predator(startX, startY, speed, directionX, directionY, false); // false for female
            fox.transitionAge(age); // Set age
            newFoxes.add(fox);
        }

        for (int j = 0; j < numberOfRemainingFoxes; j++) {
            // For the remaining random foxes
            int age = (int) (Math.random() * 3); // Randomly assign age
            int size;
            int speed;

            if (age == 0) { // Baby
                size = Constants.BABY_SIZE;
                speed = (int) (Constants.ADULT_SIZE * Constants.BABY_SPEED_FACTOR);
            } else if (age == 1) { // Young
                size = Constants.YOUNG_SIZE;
                speed = (int) (Constants.ADULT_SIZE * Constants.YOUNG_SPEED_FACTOR);
            } else { // Adult
                size = Constants.ADULT_SIZE;
                speed = Constants.ADULT_SIZE;
                age = 2; // Set age to adult
            }

            int startX = (int) (Math.random() * screenWidth);
            int startY = (int) (Math.random() * screenHeight);
            int directionX = Math.random() < 0.5 ? -1 : 1;
            int directionY = Math.random() < 0.5 ? -1 : 1;

            boolean isMale = Math.random() < 0.5;

            Predator fox = new Predator(startX, startY, speed, directionX, directionY, isMale);
            fox.transitionAge(age); // Set age
            newFoxes.add(fox);
        }

        foxes.clear();
        foxes.addAll(newFoxes);

        // Start the simulation loop
        startSimulationLoop();

        // Start the graph update thread
        startGraphUpdateThread();
    }
    
    private void createChart() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(rabbitSeries);
        dataset.addSeries(foxSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Population Growth",
                "Time",
                "Population",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        chartPanel = new ChartPanel(chart);
    }

    private void startSimulationLoop() {
        // Start the simulation loop
        new Timer(150, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveRabbits();
                moveFoxes();
                repaint();
            }
        }).start();
    }

    private void startGraphUpdateThread() {
    	// Start a daemon thread for the graph updates
        Thread graphThread = new Thread(() -> {
            JFrame chartFrame = new JFrame("Population Growth Chart");
            chartFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            chartFrame.getContentPane().add(chartPanel);
            chartFrame.pack();
            chartFrame.setLocationRelativeTo(null);
            chartFrame.setVisible(true);

            while (true) {
                try {
                    Thread.sleep(150); // Adjust the sleep time as needed
                    updateGraph();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        graphThread.setDaemon(true);
        graphThread.start();
    }

    private void updateGraph() {
        long currentTime = System.currentTimeMillis();
        long elapsedTimeInSeconds = (currentTime - simulationStartTime) / 1000; // Convert to seconds

        int rabbitCount = rabbits.size();
        int foxCount = foxes.size();

        rabbitSeries.add(elapsedTimeInSeconds, rabbitCount);
        foxSeries.add(elapsedTimeInSeconds, foxCount);
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

        if (timeSinceLastMating >= Constants.RABBIT_MATING_CYCLE) {
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
        if (!fox.isAlive()) {
            return; // Don't reproduce if hungry or dead
        }
        long currentTime = System.currentTimeMillis();
        long timeSinceLastMating = currentTime - fox.getLastSuccessfulMatingTime();
        if (timeSinceLastMating >= Constants.FOX_MATING_CYCLE) {
            fox.setMating(true);
            fox.mates();
        }
    }

    private void huntAndEat(Predator fox) {
        List<Prey> nearbyRabbits = findNearbyPrey(fox, rabbits);

        if (!nearbyRabbits.isEmpty()) {
            fox.chase(nearbyRabbits); // chaseTarget method in the Predator class handles the hunting logic
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
        SwingUtilities.invokeLater(() -> {
            Main main = new Main();
            main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            main.setTitle("Planet Life Simulation");
            main.setSize(800, 600);
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
    
    public static void addRabbit(Prey rabbit) {
        rabbits.add(rabbit);
    }
    
    public static void addFox(Predator fox) {
        foxes.add(fox);
    }
}