package test3;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;
import java.util.Random;
import java.util.List;
import java.awt.Color;

public class Prey extends JPanel implements Drawable {
    private int age;  // 0 for baby, 1 for young, 2 for adult
    private int originalSpeed;  // Store the original speed for reference
    private Image rabbitImage;
    private int x, y;
    private int speed;
    private int directionX, directionY;
    private Dimension screenSize; // Store the screen size
    private Timer timer;
    private boolean isEating;
    private int id; // Unique ID for each rabbit
    private boolean isEaten;
    private boolean isMale; // true for male, false for female
    private boolean isMating = false;
    private int matingCooldown = 0;

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

    public void setEaten(boolean eaten) {
        isEaten = eaten;
    }
    
    public int getId() {
        return id;
    }
    
    public boolean isMale() {
        return isMale;
    }
    
    public boolean isMating() {
        return isMating;
    }

    public String getStatus() {
    	String sex = isMale ? "Male" : "Female";
        return "Rabbit ID: " + id + ", Age: " + age + ", Size: " + getSizeByAge() + ", Sex: " + sex;
    }
    
    public boolean isEating() {
        // Implement the logic to determine if the prey is currently eating
        // For example, you might have a boolean field like isEating and return its value.
        return isEating;
    }
    
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
                die(); // Die when reaching adult age
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
    
    private class DeathTask extends TimerTask {
        private Prey prey;

        public DeathTask(Prey prey) {
            this.prey = prey;
        }

        @Override
        public void run() {
            prey.die();
        }
    }
    
    private void scheduleTransition(int targetAge, int delay) {
        timer.schedule(new AgeTransitionTask(this, targetAge), delay);
    }

    private void scheduleDeath(int delay) {
        timer.schedule(new DeathTask(this), delay);
    }

    public void die() {
        Main.removeRabbit(this); // Remove rabbit from the simulation
        timer.schedule(new RemoveImageTask(this), Constants.IMAGE_REMOVAL_DELAY);
    }
    
    private class RemoveImageTask extends TimerTask {
        private Prey prey;

        public RemoveImageTask(Prey prey) {
            this.prey = prey;
        }

        @Override
        public void run() {
            replaceWithDeadImage(); // Replace the image with the dead image
            stopMovements(); // Stop any ongoing movements or behaviors
            delayAndRemoveImage(); // Delay the image removal
        }

        private void replaceWithDeadImage() {
            prey.rabbitImage = Constants.loadImage(Constants.DEAD_PREY_IMAGE_PATH);
        }

        private void delayAndRemoveImage() {
            // Delay the image removal by 1 second
            Timer removeImageTimer = new Timer();
            removeImageTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    removeImage(); // Remove the image
                }
            }, 1000);
        }

        private void removeImage() {
            prey.rabbitImage = null;  // Set the image to null
        }

        private void stopMovements() {
            // Cancel any remaining tasks in the timer
            prey.directionX = 0;
            prey.directionY = 0;
            // Implement additional logic to stop movements or behaviors
        }
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
    
    public void startMatingSeason() {
        if (!isEating() && matingCooldown <= 0) {
            matingCooldown = Constants.MATING_SEASON_DURATION;
            mate();
        }
    }

    private void mate() {
        if (!isMale && !isMating && age == 2) { // Check if the prey is an adult and female
            Prey mate = findMate();
            if (mate != null && mate.getAge() == 2) { // Check if the mate is an adult
                isMating = true;
                moveTowards(mate.getX(), mate.getY());

                Timer matingTimer = new Timer();
                matingTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        spawnNewRabbit(mate);
                        isMating = false;
                    }
                }, Constants.MATING_DELAY);
            }
        }
    }

    private Prey findMate() {
        for (Prey prey : Main.getRabbits()) {
            if (prey.isMale() != this.isMale() && !prey.isMating()) {
                return prey;
            }
        }
        return null;
    }
    
    public Prey getOffspring() {
        // probability or conditions for reproduction
        if (isMating() && age == 2 && Math.random() < Constants.PREY_REPRODUCTION) {
            // Create a new baby rabbit with similar characteristics
            return new Prey(x, y, originalSpeed, directionX, directionY, Math.random() < 0.5);
        }
        return null; // No offspring
    }

    private void moveTowards(int targetX, int targetY) {
        // Calculate the direction to move towards the target
        int deltaX = targetX - getX();
        int deltaY = targetY - getY();

        // Calculate the distance between the prey and the target
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // Normalize the direction vector
        double normalizedDeltaX = deltaX / distance;
        double normalizedDeltaY = deltaY / distance;

        // Adjust the prey's direction based on the normalized vector
        directionX = (int) Math.round(normalizedDeltaX);
        directionY = (int) Math.round(normalizedDeltaY);
    }

    private void spawnNewRabbit(Prey mate) {
        // Implement logic to spawn a new baby rabbit at the female's position
        int babyX = mate.getX();
        int babyY = mate.getY();

        // Randomly decide the number of baby rabbits to spawn (between 1 and 8)
        int numberOfBabyRabbits = (int) (Math.random() * 8) + 1;
        
        for (int i = 0; i < numberOfBabyRabbits; i++) {
        	// Randomly decide the sex of the baby rabbit
        	boolean isBabyMale = Math.random() < 0.5;

        	// Create a new baby rabbit and add it to the list
        	Prey babyRabbit = new Prey(babyX, babyY, originalSpeed, directionX, directionY, isBabyMale);
        	babyRabbit.transitionAge(0); // Set the age to baby
        	Main.getRabbits().add(babyRabbit);
        }
    }
    
    @Override
    public void draw(Graphics g) {
        int size = getSizeByAge();

        // Draw the border based on gender
        if (isMale()) {
            g.setColor(Color.BLUE);
        } else {
            g.setColor(Color.PINK);
        }
        g.drawRect(x - 1, y - 1, size + 1, size + 1);

        // Draw the rabbit image
        g.drawImage(rabbitImage, x, y, size, size, this);
    }
}