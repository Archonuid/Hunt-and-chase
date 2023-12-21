package test2;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;

public class Constants {
    // sizes for Predator and Prey
    public static final int BABY_SIZE = 10;
    public static final int YOUNG_SIZE = 15;
    public static final int ADULT_SIZE = 20;
    public static final int DEAD_IMAGE_SIZE = 20;
    public static final String PREDATOR_IMAGE_PATH = "C:\\Users\\hp\\eclipse-workspace\\test2\\images\\IMG_20231109_141730.jpg";
    public static final String PREY_IMAGE_PATH = "C:\\Users\\hp\\eclipse-workspace\\test2\\images\\IMG_20231109_141750.jpg";
    public static final String DEAD_PREDATOR_IMAGE_PATH = "C:\\Users\\hp\\eclipse-workspace\\test2\\images\\IMG_20231109_141730(dead).jpg";
    public static final String DEAD_PREY_IMAGE_PATH = "C:\\Users\\hp\\eclipse-workspace\\test2\\images\\IMG_20231109_141750(dead).jpg";

    // image loading method
    public static Image loadImage(String filePath) {
        return Toolkit.getDefaultToolkit().getImage(filePath);
    }

    // timer-related variables
    public static final int FOX_TRANSITION_DELAY = 20000; //40 seconds
    public static final int RABBIT_TRANSITION_DELAY = 20000; //10 seconds
    public static final int FOX_DEATH_DELAY = 120000; //120 seconds
    public static final int RABBIT_DEATH_DELAY = 60000; //60 seconds

    // screen-related variables
    public static Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    // movement-related variables
    public static final double BABY_SPEED_FACTOR = 0.5;
    public static final double YOUNG_SPEED_FACTOR = 0.55;
    public static final double ADULT_SPEED_FACTOR = 0.7;
    public static final double chaseSpeedFactor = 1.65;
    public static final double escapeSpeedFactor = 1.6;

    // image removal delay
    public static final int IMAGE_REMOVAL_DELAY = 1000;
    
    // ID tracking
    public static int lastRabbitId = 0;
    public static int lastFoxId = 0;
    
    public static final int FOX_HUNGER_CYCLE = 30000; // Fox gets hungry every 20 seconds
    public static final int HUNTING_RANGE = 300; // Foxes hunt within 100px range
    public static final int ADULT_FOX_SUCCESSFUL_HUNT = 99; // 70% success rate for adult foxes
    public static final int YOUNG_FOX_SUCCESSFUL_HUNT = 98; // 50% success rate for young foxes
    public static final int FOX_DEATH_BY_HUNGER = 100000; // Fox dies if hungry for more than 40 seconds
    public static final int ESCAPE_RANGE = 80; // Rabbits can detect foxes within 50px
    public static final int FOX_HUNT_TIMEOUT_RANGE = 80; // Fox stops chasing if rabbit is more than 80px away

    public static final int REPRODUCTION_DISTANCE = 500; //Distance between mates to mingle
    public static final int FOX_MATING_CYCLE = 15000;
    //public static final int UPPER_CAP_FOXES = 40;
    public static final int RABBIT_MATING_CYCLE = 20000;
    //public static final int UPPER_CAP_RABBITS = 70;
    public static final int MATE_PROXIMITY = 10;
    
    public static double calculateDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}
