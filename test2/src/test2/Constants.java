package test2;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;

public class Constants {
    // Common sizes for both Predator and Prey
    public static final int BABY_SIZE = 10;
    public static final int YOUNG_SIZE = 15;
    public static final int ADULT_SIZE = 20;
    public static final int DEAD_IMAGE_SIZE = 20;
    public static final String PREDATOR_IMAGE_PATH = "C:\\Users\\hp\\eclipse-workspace\\test2\\images\\IMG_20231109_141730.jpg";
    public static final String PREY_IMAGE_PATH = "C:\\Users\\hp\\eclipse-workspace\\test2\\images\\IMG_20231109_141750.jpg";
    public static final String DEAD_PREDATOR_IMAGE_PATH = "C:\\Users\\hp\\eclipse-workspace\\test2\\images\\IMG_20231109_141730(dead).jpg";
    public static final String DEAD_PREY_IMAGE_PATH = "C:\\Users\\hp\\eclipse-workspace\\test2\\images\\IMG_20231109_141750(dead).jpg";

    // Common image loading method
    public static Image loadImage(String filePath) {
        return Toolkit.getDefaultToolkit().getImage(filePath);
    }

    // Common timer-related variables
    public static final int TRANSITION_DELAY = 30000; // 30 seconds
    public static final int DEATH_DELAY = 30000;

    // Common screen-related variables
    public static Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    // Common movement-related variables
    public static final double BABY_SPEED_FACTOR = 0.35;
    public static final double YOUNG_SPEED_FACTOR = 0.35;
    public static final double ADULT_SPEED_FACTOR = 0.5;

    // Common image removal delay
    public static final int IMAGE_REMOVAL_DELAY = 2000;
    
    // ID tracking
    public static int lastRabbitId = 0;
    public static int lastFoxId = 0;
    
    public static final int FOX_HUNGER_CYCLE = 5000; // Fox gets hungry every 20 seconds
    public static final int HUNTING_RANGE = 100; // Foxes hunt within 30px range
    public static final int ADULT_FOX_SUCCESSFUL_HUNT = 80; // 95% success rate for adult foxes
    public static final int YOUNG_FOX_SUCCESSFUL_HUNT = 70; // 90% success rate for young foxes
    public static final int FOX_DEATH_BY_HUNGER = 50000; // Fox dies if hungry for more than 45 seconds
    public static final int ESCAPE_RANGE = 50; // Rabbits can detect foxes within 35px
    public static final int FOX_HUNT_TIMEOUT_RANGE = 80; // Fox stops chasing if rabbit is more than 60px away

    public static double calculateDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}
