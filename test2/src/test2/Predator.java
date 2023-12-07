package test2;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;
import java.util.Random;
import java.awt.Color;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Predator extends JPanel implements Drawable {
    private int age;  // 0 for baby, 1 for young, 2 for adult
    private int originalSpeed;  // Store the original speed for reference
    private Image foxImage;
    private int x, y;
    private int speed;
    private int directionX, directionY;
    private Dimension screenSize; // Store the screen size
    private Timer timer;
    private int id; // Unique ID for each fox
    private boolean hungry = false;
    private Timer hungerTimer;
    private boolean isMale; // true for male, false for female

    public Predator(int startX, int startY, int initialSpeed, int initialDirectionX, int initialDirectionY, boolean isMale) {
        x = startX;
        y = startY;
        this.foxImage = Constants.loadImage(Constants.PREDATOR_IMAGE_PATH);
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
        this.id = ++Constants.lastFoxId; // Assign and increment the ID
        hungerTimer = new Timer();
        this.isMale = isMale;
        this.lastSuccessfulHuntTime = System.currentTimeMillis();
        Main.getFoxes().add(this);
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
    
    public int getAge() {
        return age;
    }
    
    public int getId() {
        return id;
    }
    
    public boolean isMale() {
        return isMale;
    }

    public String getStatus() {
        String sex = isMale ? "Male" : "Female";
        return "Fox ID: " + id + ", Age: " + age + ", Size: " + getSizeByAge() + ", Sex: " + sex;
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
    
    private void scheduleTransition(int targetAge, int delay) {
        timer.schedule(new AgeTransitionTask(this, targetAge), delay);
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
    
    // DEATH
    private class DeathTask extends TimerTask {
        private Predator predator;

        public DeathTask(Predator predator) {
            this.predator = predator;
        }

        @Override
        public void run() {
            predator.handleDeath();
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
                foxImage = Constants.loadImage(Constants.DEAD_PREDATOR_IMAGE_PATH);

                // Delay the image removal by 1 second
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // Remove the image
                        foxImage = null;
                        // Remove fox from the simulation and array
                        Main.removeFox(Predator.this);
                    }
                }, 1000);
            }
        }, Constants.IMAGE_REMOVAL_DELAY);
    }
       
    // MOVEMENT
    private void setRandomDirection() {
        Random random = new Random();
        directionX = random.nextInt(3) - 1; // Random value between -1 and 1
        directionY = random.nextInt(3) - 1; // Random value between -1 and 1
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
    
    private void moveFox(double speedFactor) {
        x += speed * directionX * speedFactor;
        y += speed * directionY * speedFactor;
        handleScreenEdges();
        maybeStop();
        startHungerCountdown();
        chase(Main.getRabbits());
    }

    public void moveBabyFox() {
        moveFox(Constants.BABY_SPEED_FACTOR);
    }

    public void moveYoungFox() {
        moveFox(Constants.YOUNG_SPEED_FACTOR);
    }

    public void move() {
        moveFox(Constants.ADULT_SPEED_FACTOR);
    }
    
    // CHASE, HUNT, EAT
    public boolean isHungry() {
        return hungry;
    }

    public long getLastSuccessfulHuntTime() {
        return lastSuccessfulHuntTime;
    }

    public void setHungry(boolean hungry) {
        this.hungry = hungry;
    }

    private boolean isWithinHuntingRange(Prey target) {
        double distance = Constants.calculateDistance(getX(), getY(), target.getX(), target.getY());
        return distance <= Constants.HUNTING_RANGE;
    }

    private long lastSuccessfulHuntTime = System.currentTimeMillis();

    private void startHungerCountdown() {
        hungerTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Predator fox : Main.getFoxes()) {
                    if (fox != null && !fox.isHungry()) {
                        long currentTime = System.currentTimeMillis();
                        long timeSinceLastHunt = currentTime - fox.getLastSuccessfulHuntTime();

                        if (timeSinceLastHunt >= Constants.FOX_HUNGER_CYCLE) {
                            fox.setHungry(true);
                            fox.chase(Main.getRabbits());
                        }
                    }
                }
            }
        }, 0, Constants.FOX_HUNGER_CYCLE);
    }

    // Add a method to renew the hunger countdown
    private void renewHungerCountdown() {
        hungerTimer.cancel();
        hungerTimer = new Timer();
        startHungerCountdown();
    }

    public void chase(List<Prey> rabbits) {
        if (!hungry) {
            // If not hungry, don't chase
            return;
        }

        Prey targetRabbit = findNearestRabbit(rabbits);

        if (targetRabbit != null && isWithinHuntingRange(targetRabbit)) {
            chaseTarget(targetRabbit);
        }
    }

    private void chaseTarget(Prey targetRabbit) {
        // Calculate the angle between the fox and the rabbit
        double chaseSpeedFactor = 2.0; // Adjust the value as needed
        double angle = Math.atan2(targetRabbit.getY() - getY(), targetRabbit.getX() - getX());

        // Calculate the new direction based on the angle
        directionX = (int) Math.round(Math.cos(angle));
        directionY = (int) Math.round(Math.sin(angle));

        // Move the fox to close the distance at an increased speed
        x += chaseSpeedFactor * speed * directionX;
        y += chaseSpeedFactor * speed * directionY;

        // Handle screen edges to prevent the fox from going off-screen
        handleScreenEdges();

        // Check if the fox caught the rabbit
        double distance = Constants.calculateDistance(getX(), getY(), targetRabbit.getX(), targetRabbit.getY());
        if (distance <= 5) {
            // Fox catches the rabbit
            eat(targetRabbit);
        }
    }

    private void eat(Prey rabbit) {
        // Stop all movements of the eaten rabbit
        rabbit.stopMovements();

        // Remove the eaten rabbit from the simulation
        Main.removeRabbit(rabbit);

        // Resume normal movement for the fox
        switch (getAge()) {
            case 0:
                moveBabyFox(); // Implement moveBabyFox method for baby fox movement
                break;
            case 1:
                moveYoungFox(); // Implement moveYoungFox method for young fox movement
                break;
            case 2:
                move(); // Implement move method for adult fox movement
                break;
            // Add additional cases if needed for other age values
        }

        // Continue movement or perform other actions after a successful hunt
        hungry = false; // Reset hunger state
        lastSuccessfulHuntTime = System.currentTimeMillis(); // Update the last successful hunt time
        renewHungerCountdown(); // Reset hunger countdown
    }


	// Add a method to find the nearest rabbit within the hunting range
    private Prey findNearestRabbit(List<Prey> rabbits) {
        Prey nearestRabbit = null;
        double minDistance = Double.MAX_VALUE;

        for (Prey rabbit : rabbits) {
            double distance = Constants.calculateDistance(getX(), getY(), rabbit.getX(), rabbit.getY());
            if (distance < minDistance) {
                minDistance = distance;
                nearestRabbit = rabbit;
            }
        }

        return nearestRabbit;
    }
    
    @Override
    public void draw(Graphics g) {
        int size = getSizeByAge();
        if (hungry) {
            // Draw a red border around the fox only when it is currently hungry
            g.setColor(Color.RED);
            g.drawRect(x - 1, y - 1, size + 1, size + 1);
        }

        // Draw the fox image
        g.drawImage(foxImage, x, y, size, size, this);
    }
}