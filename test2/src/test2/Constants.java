package test2;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;

public class Constants {
    // Common sizes for both Predator and Prey
    public static final int BABY_SIZE = 5;
    public static final int YOUNG_SIZE = 10;
    public static final int ADULT_SIZE = 15;
    public static final int DEAD_IMAGE_SIZE = 15;
    public static final String PREDATOR_IMAGE_PATH = "C:\\Users\\hp\\eclipse-workspace\\test2\\images\\IMG_20231109_141730.jpg";
    public static final String PREY_IMAGE_PATH = "C:\\Users\\hp\\eclipse-workspace\\test2\\images\\IMG_20231109_141750.jpg";
    public static final String DEAD_PREDATOR_IMAGE_PATH = "C:\\Users\\hp\\eclipse-workspace\\test2\\images\\IMG_20231109_141730(dead).jpg";
    public static final String DEAD_PREY_IMAGE_PATH = "C:\\Users\\hp\\eclipse-workspace\\test2\\images\\IMG_20231109_141750(dead).jpg";

    // Common image loading method
    public static Image loadImage(String filePath) {
        return Toolkit.getDefaultToolkit().getImage(filePath);
    }

    // Common timer-related variables
    public static final int TRANSITION_DELAY = 30000;
    public static final int DEATH_DELAY = 30000;

    // Common screen-related variables
    public static Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    // Common movement-related variables
    public static final double BABY_SPEED_FACTOR = 0.5;
    public static final double YOUNG_SPEED_FACTOR = 0.75;

    // Common image removal delay
    public static final int IMAGE_REMOVAL_DELAY = 2000;
}
