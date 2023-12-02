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

public class Predator extends JPanel {
    private boolean isHungry = false;
    private Timer hungerTimer;
    private Timer deathTimer;
    private int age;  // 0 for baby, 1 for young, 2 for adult
    private int originalSpeed;  // Store the original speed for reference
    private Image foxImage;
    private int x, y;
    private int speed;
    private int directionX, directionY;
    private Dimension screenSize; // Store the screen size
    private Timer timer;
    private Point position;
    private Main mainInstance; // Reference to Main
    private PredatorActionListener actionListener;

    public Predator(int startX, int startY, int initialSpeed, int initialDirectionX, int initialDirectionY, Main main) {
        this.mainInstance = main;
        this.actionListener = actionListener;
        this.x = startX;
        this.y = startY;
        this.position = new Point(x, y);
        this.foxImage = Constants.loadImage(Constants.PREDATOR_IMAGE_PATH);
        this.screenSize = Constants.getScreenSize();
        this.speed = initialSpeed;
        this.directionX = initialDirectionX;
        this.directionY = initialDirectionY;
        this.age = 0;
        this.originalSpeed = initialSpeed;
        this.timer = new Timer();
        setDoubleBuffered(true);
        System.setProperty("sun.java2d.opengl", "true");
        setupHungerTimer();
        scheduleTransition(1, Constants.TRANSITION_DELAY);
        this.mainInstance = main;
        this.actionListener = main;
    }
    
    private void setupHungerTimer() {
        hungerTimer = new Timer();
        hungerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isHungry = true;
                setupDeathTimer();
            }
        }, 15000, 15000); // gets hungry every 15 seconds
    }
    
    private void setupDeathTimer() {
        if (deathTimer != null) {
            deathTimer.cancel();
        }
        deathTimer = new Timer();
        deathTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isHungry) {
                    die(); // Implement die method as per your existing logic
                }
            }
        }, 40000); // 40 seconds to catch a rabbit
    }

    private void chaseNearestRabbit() {
        AnimalRecord nearestRabbit = findNearestRabbit();
        if (nearestRabbit != null) {
            Point rabbitPosition = nearestRabbit.getPosition();
            // Basic logic to move towards the rabbit
            directionX = (rabbitPosition.x > x) ? 1 : -1;
            directionY = (rabbitPosition.y > y) ? 1 : -1;

            x += speed * directionX;
            y += speed * directionY;
            position.setLocation(x, y);

            // Check for successful catch
            if (position.distance(rabbitPosition) < 5 && successfulCatch(nearestRabbit)) { // 5 can be changed based on your catch criteria
                eatRabbit(nearestRabbit);
                isHungry = false;
                if (deathTimer != null) {
                    deathTimer.cancel();
                }
            }
        }
    }
    
    private AnimalRecord findNearestRabbit() {
        AnimalRecord nearestRabbit = null;
        double minDistance = Double.MAX_VALUE;
        for (AnimalRecord record : mainInstance.getAnimalRecords()) {
            if (record.getType().equals("Rabbit")) {
                double distance = this.position.distance(record.getPosition());
                if (distance < 30 && distance < minDistance) {
                    nearestRabbit = record;
                    minDistance = distance;
                }
            }
        }
        return nearestRabbit;
    }
    
    private boolean successfulCatch(AnimalRecord rabbit) {
        double catchProbability = age == 2 ? 0.95 : 0.85; // 95% for adult, 85% for young
        return Math.random() < catchProbability;
    }
    
    private void eatRabbit(AnimalRecord rabbit) {
        // Notify that the rabbit is caught
        if (actionListener != null) {
            actionListener.onRabbitCaught(rabbit.getId());
        }

        // Reset hunger state and any other necessary fox state updates
        isHungry = false;
        // Other state updates as needed
    }

    
    private class AgeTransitionTask extends TimerTask {
        private Predator predator;
        private int targetAge;

        public AgeTransitionTask(Predator predator, int targetAge) {
            this.predator = predator;
            this.targetAge = targetAge;
        }

        @Override
        public void run() {
            predator.transitionAge(targetAge);
        }
    }
    
    private class DeathTask extends TimerTask {
        private Predator predator;

        public DeathTask(Predator predator) {
            this.predator = predator;
        }

        @Override
        public void run() {
            predator.die();
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

    private class RemoveImageTask extends TimerTask {
        private Predator predator;

        public RemoveImageTask(Predator predator) {
            this.predator = predator;
        }

        @Override
        public void run() {
            replaceWithDeadImage(); // Replace the image with the dead image
            stopMovements(); // Stop any ongoing movements or behaviors
            delayAndRemoveImage(); // Delay the image removal
        }
        
        private void replaceWithDeadImage() {
            predator.foxImage = Constants.loadImage(Constants.DEAD_PREDATOR_IMAGE_PATH);
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
            foxImage = null;  // Set the image to null
        }

        private void stopMovements() {
            // Cancel any remaining tasks in the timer
        	predator.directionX = 0;
            predator.directionY = 0;
            // Implement additional logic to stop movements or behaviors
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (foxImage != null) {
            int size = getSizeByAge();
            g.drawImage(foxImage, x, y, size, size, this);
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
    
    public void moveBabyFox() {
        // Baby fox may move more randomly or slowly
    	double speedFactor = Constants.BABY_SPEED_FACTOR;
        x += speed * directionX * speedFactor; // Move at half the speed
        y += speed * directionY * speedFactor;
        handleScreenEdges();
        maybeStop();
    }
    
    public void moveYoungFox() {
    	if (isHungry && age > 0) { // Baby foxes can't hunt
            chaseNearestRabbit();
        } else {
        // Young fox may move faster or exhibit playful behavior
        	double speedFactor = Constants.YOUNG_SPEED_FACTOR;
        	x += speed * directionX * speedFactor; // Move at 25% faster than baby fox
        	y += speed * directionY * speedFactor;
        	handleScreenEdges();
        	maybeStop();
        	position.setLocation(x, y);
        	updatePosition();
        }
    }
    
    public void move() {
    	if (isHungry && age > 0) { // Baby foxes can't hunt
            chaseNearestRabbit();
        } else {
        // Update the position based on the current direction and speed
        	x += speed * directionX;
        	y += speed * directionY;
        	handleScreenEdges();
        	maybeStop();
        	position.setLocation(x, y);
        	updatePosition();
        }
    }
    
    private void updatePosition() {
        // Existing logic to update the fox's position
        position.setLocation(x, y); // Update the position field
        handleScreenEdges();
        maybeStop();
    }
    
    public int getAge() {
        return age;
    }
    
    private void handleScreenEdges() {
    	int size = getSizeByAge();
        int titleBarHeight = 10;
        int taskbarHeight = 10;

        // Adjust screen height by subtracting the heights of title bar and taskbar
        int adjustedScreenHeight = screenSize.height - titleBarHeight - taskbarHeight;
        // Check if the predator has reached the screen edges
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
}