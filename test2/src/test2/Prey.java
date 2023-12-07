package test2;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;
import java.util.Random;
import java.util.List;
import java.awt.Color;

public class Prey extends JPanel implements Drawable, PreyInterface {
    private int age;  // 0 for baby, 1 for young, 2 for adult
    private int originalSpeed;  // Store the original speed for reference
    private Image rabbitImage;
    private int x, y;
    private int speed;
    private int directionX, directionY;
    private Dimension screenSize; // Store the screen size
    private Timer timer;
    private boolean isEating;
    private Timer eatTimer;
    private boolean isStopped = false;
    private int id; // Unique ID for each rabbit
    private boolean isEaten;
    private boolean isMale; // true for male, false for female

    public Prey(int startX, int startY, int initialSpeed, int initialDirectionX, int initialDirectionY, boolean isMale) {
        x = startX;
        y = startY;
        this.rabbitImage = Constants.loadImage(Constants.PREY_IMAGE_PATH);
        timer = new Timer();
        screenSize = Constants.getScreenSize(); // Get the current screen size
        scheduleTransition(1, Constants.TRANSITION_DELAY); // Schedule transition to young after 30 seconds
        speed = initialSpeed;
        directionX = initialDirectionX;
        directionY = initialDirectionY;
        age = 0;  // Initially set as baby
        originalSpeed = initialSpeed;
        eatTimer = new Timer();
        setDoubleBuffered(true);
        System.setProperty("sun.java2d.opengl", "true");
        this.id = ++Constants.lastRabbitId; // Assign and increment the ID
        this.isMale = isMale;
    }
    
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // for setting x and y coordinates
    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
    
    public int getAge() {
        return age;
    }
    
    public boolean isEaten() {
        return isEaten;
    }

    public void setEaten(boolean eaten) {
        isEaten = eaten;
    }
    
    public int getId() {
        return id;
    }
    
    public boolean isMale() {
        return isMale;
    }

    public String getStatus() {
    	String sex = isMale ? "Male" : "Female";
        return "Rabbit ID: " + id + ", Age: " + age + ", Size: " + getSizeByAge() + ", Sex: " + sex;
    }
    
    // GROWTH
    public void transitionAge(int targetAge) {
        age = targetAge;  // Update the age
        switch (targetAge) {
            case 1:
                speed = originalSpeed; // Speed for young foxes
                scheduleTransition(2, Constants.TRANSITION_DELAY); // Schedule transition to adult after 30 seconds
                break;
            case 2:
                speed = originalSpeed; // Speed for adult foxes
                scheduleDeath(Constants.TRANSITION_DELAY); // Schedule death after 30 seconds as an adult
                break;
            case 3:
                handleDeath(); // Die when reaching adult age
                break;
        }
    }
    
    private int getSizeByAge() {
        switch (age) {
            case 0:
                return Constants.BABY_SIZE;
            case 1:
                return Constants.YOUNG_SIZE;
            case 2:
                return Constants.ADULT_SIZE;
            default:
                return Constants.ADULT_SIZE; // Default to adult size
        }
    }
    
    private class AgeTransitionTask extends TimerTask {
        private Prey prey;
        private int targetAge;

        public AgeTransitionTask(Prey prey, int targetAge) {
            this.prey = prey;
            this.targetAge = targetAge;
        }

        @Override
        public void run() {
            prey.transitionAge(targetAge);
        }
    }
    
    private void scheduleTransition(int targetAge, int delay) {
        timer.schedule(new AgeTransitionTask(this, targetAge), delay);
    }

    // DEATH  
    private class DeathTask extends TimerTask {
        private Prey prey;

        public DeathTask(Prey prey) {
            this.prey = prey;
        }

        @Override
        public void run() {
            prey.handleDeath();
        }
    }

    private void scheduleDeath(int delay) {
        timer.schedule(new DeathTask(this), delay);
    }

    public void handleDeath() {
        // Stop movements
        directionX = 0;
        directionY = 0;

        // Replace the image with the dead image and schedule removal
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Replace the image with the dead image
                rabbitImage = Constants.loadImage(Constants.DEAD_PREY_IMAGE_PATH);

                // Delay the image removal by 1 second
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // Remove the image
                        rabbitImage = null;
                        // Remove fox from the simulation and array
                        Main.removeRabbit(Prey.this);
                    }
                }, 1000);
            }
        }, Constants.IMAGE_REMOVAL_DELAY);
    }
    
    @Override
    public void stopMovements() {
        // Implement logic to stop any ongoing movements or behaviors
        directionX = 0;
        directionY = 0;
        // Additional logic if needed
        handleDeath();
    }
    
    private void setRandomDirection() {
        Random random = new Random();
        directionX = random.nextInt(3) - 1; // Random value between -1 and 1
        directionY = random.nextInt(3) - 1; // Random value between -1 and 1
    }
    
    private void handleScreenEdges() {
    	int size = getSizeByAge();
        int titleBarHeight = 20;
        int taskbarHeight = 10;

        // Adjust screen height by subtracting the heights of title bar and taskbar
        int adjustedScreenHeight = screenSize.height - titleBarHeight - taskbarHeight;
        // Check if the prey has reached the screen edges
        if (x < 0) {
            x = 0;
            directionX *= -1;
        } else if (x > screenSize.width - size) {
            x = screenSize.width - size * 2;
            directionX *= -1;
        }

        if (y < titleBarHeight) {
            y = titleBarHeight;
            directionY *= -1;
        } else if (y > adjustedScreenHeight - size) {
            y = adjustedScreenHeight - size * 3;
            directionY *= -1;
        }
    }
    
    private void maybeStop() {
        Random random = new Random();
        int stopProbability = 1; // Adjust this probability as needed (e.g., 5% chance of stopping)

        if (random.nextInt(100) < stopProbability) {
            // Stop the movement
            directionX = 0;
            directionY = 0;
            
            // Schedule resuming movement after 2 seconds
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // Resume normal movement
                    setRandomDirection();
                }
            }, 1000);
        }
    }
    
    private void moveWithFactor(double speedFactor) {
        if (!isEating) {
            x += speed * directionX * speedFactor;
            y += speed * directionY * speedFactor;
        }
        handleScreenEdges();
        maybeStop();
        Predator nearestFox = findNearestFox(Main.getFoxes());
        if (nearestFox != null && Constants.calculateDistance(getX(), getY(), nearestFox.getX(), nearestFox.getY()) <= Constants.ESCAPE_RANGE) {
            outOfRange(nearestFox);
        }
    }

    public void moveBabyRabbit() {
        moveWithFactor(Constants.BABY_SPEED_FACTOR);
    }

    public void moveYoungRabbit() {
        moveWithFactor(Constants.YOUNG_SPEED_FACTOR);
    }

    public void move() {
        moveWithFactor(Constants.ADULT_SPEED_FACTOR);
    }
    
    // Add a method to detect nearby foxes within the escape range
    private Predator findNearestFox(List<Predator> foxes) {
        if (foxes == null || foxes.isEmpty()) {
            return null; // No foxes available, return null
        }

        Predator nearestFox = null;
        double minDistance = Double.MAX_VALUE;

        for (Predator fox : foxes) {
            if (fox != null) { // Add null check for the fox object
                double distance = Constants.calculateDistance(getX(), getY(), fox.getX(), fox.getY());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestFox = fox;
                }
            }
        }

        return nearestFox;
    }
    
    private void escapeFromFox(Predator fox) {
        // Calculate the escape distance based on the predator's hunting range
        int escapeDistance = Constants.ESCAPE_RANGE;

        // Calculate the angle between the rabbit and the fox
        double angle = Math.atan2(y - fox.getY(), x - fox.getX());

        // Calculate the new position to move away from the fox
        int newX = (int) (x + escapeDistance * Math.cos(angle));
        int newY = (int) (y + escapeDistance * Math.sin(angle));

        // Update the rabbit's position
        x = newX;
        y = newY;

        // Adjust the direction to move away from the fox
        directionX = (int) Math.cos(angle);
        directionY = (int) Math.sin(angle);
    }

    private void outOfRange(Predator fox) {
        // Move away from the nearest fox if within the escape range
        int escapeRange = Constants.ESCAPE_RANGE;
        double distanceToFox = Constants.calculateDistance(getX(), getY(), fox.getX(), fox.getY());

        if (distanceToFox <= escapeRange) {
            // If within escape range, actively escape from the fox
            escapeFromFox(fox);
        }
    }
    
    @Override
    public void draw(Graphics g) {
        int size = getSizeByAge();
        if (isEaten) {
            // Draw the image for a dead prey
            g.drawImage(Constants.loadImage(Constants.DEAD_PREY_IMAGE_PATH), x, y, size, size, this);
        } else {
            // Draw the regular prey image
            g.drawImage(rabbitImage, x, y, size, size, this);
        }
    }

}