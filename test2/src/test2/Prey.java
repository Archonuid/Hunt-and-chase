package test2;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;

public class Prey extends JPanel {
    private int id;  // Unique identifier for the prey
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
    private Main mainInstance; // Reference to Main

    public Prey(int id, int startX, int startY, int initialSpeed, int initialDirectionX, int initialDirectionY, Main main) {
        this.id = id;
        this.mainInstance = main;
        this.x = startX;
        this.y = startY;
        this.rabbitImage = Constants.loadImage(Constants.PREY_IMAGE_PATH);
        this.timer = new Timer();
        this.screenSize = Constants.getScreenSize();
        this.speed = initialSpeed;
        this.directionX = initialDirectionX;
        this.directionY = initialDirectionY;
        this.age = 0;  // Initially set as baby
        this.originalSpeed = initialSpeed;
        this.eatTimer = new Timer();
        setDoubleBuffered(true);
        System.setProperty("sun.java2d.opengl", "true");
        scheduleTransition(1, Constants.TRANSITION_DELAY);
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
    
    private void scheduleTransition(int targetAge, int delay) {
        timer.schedule(new AgeTransitionTask(this, targetAge), delay);
    }

    private void scheduleDeath(int delay) {
        timer.schedule(new DeathTask(this), delay);
    }

    public void die() {
        // Delay the image removal by 2 seconds
        timer.schedule(new RemoveImageTask(this), 2000);
    }
    
    public int getAge() {
        return age;
    }
    
    public int getId() {
        return id;
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
            // Delay the image removal by 2 seconds
            Timer removeImageTimer = new Timer();
            removeImageTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    removeImage(); // Remove the image
                }
            }, 2000);
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (rabbitImage != null) {
            g.drawImage(rabbitImage, x, y, this); // x and y are the coordinates
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // Additional methods for setting x and y coordinates
    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
    
    public void moveBabyRabbit() {
        // Baby rabbit may move more randomly or slowly
        double speedFactor = Constants.BABY_SPEED_FACTOR;
        if (!isEating) {
            x += speed * directionX * speedFactor; // Move at half the speed
            y += speed * directionY * speedFactor;
        }
        handleScreenEdges();
        maybeStop();
    }

    public void moveYoungRabbit() {
        // Young rabbit may move faster or exhibit playful behavior
        double speedFactor = Constants.YOUNG_SPEED_FACTOR;
        if (!isEating) {
            x += speed * directionX * speedFactor; // Move at 25% faster than baby rabbit
            y += speed * directionY * speedFactor;
        }
        handleScreenEdges();
        maybeStop();
    }

    public void move() {
        // Update the position based on the current direction and speed
    	if (!isEating && isFoxNearby() && attemptEscape()) {
            escape();
        } else {
            x += speed * directionX;
            y += speed * directionY;
        }
        handleScreenEdges();
        maybeStop();
    }
    
    private void normalMovement() {
        double speedFactor = (age == 0) ? Constants.BABY_SPEED_FACTOR : (age == 1) ? Constants.YOUNG_SPEED_FACTOR : 1.0;
        x += speed * directionX * speedFactor;
        y += speed * directionY * speedFactor;
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
    
    private void setRandomDirection() {
        Random random = new Random();
        directionX = random.nextInt(3) - 1; // Random value between -1 and 1
        directionY = random.nextInt(3) - 1; // Random value between -1 and 1
    }
    
    private boolean isFoxNearby() {
        for (AnimalRecord record : mainInstance.getAnimalRecords()) {
            if (record.getType().equals("Fox")) {
                Point foxPosition = record.getPosition();
                if (new Point(x, y).distance(foxPosition) < 35) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean attemptEscape() {
        double escapeChance = age == 2 ? 0.1 : 0.05; // 10% for adult, 5% for young
        return Math.random() < escapeChance;
    }

    private void escape() {
        // Change direction randomly to simulate erratic escape behavior
        Random random = new Random();
        directionX = random.nextInt(3) - 1; // Random value between -1 and 1
        directionY = random.nextInt(3) - 1;

        // Optionally, increase the speed temporarily for the escape
        double escapeSpeedFactor = 1.5; // Increase speed by 50% during escape
        x += speed * escapeSpeedFactor * directionX;
        y += speed * escapeSpeedFactor * directionY;

        // You may want to limit the number of movements in escape mode,
        // after which the rabbit goes back to normal movement.
        // This can be achieved by using a counter or a timer.
    }

}