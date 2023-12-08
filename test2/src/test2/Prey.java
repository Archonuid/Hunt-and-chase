package test2;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Color;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.util.Random;
import java.util.List;

public class Prey extends JPanel implements Drawable, PreyInterface {
    private int age;  
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
    private boolean hungry;
    private long lastSuccessfulMatingTime = System.currentTimeMillis();
    private Timer matingTimer = new Timer();
    private boolean isMating = false;
    private static Main mainInstance;

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
    
    public boolean isFemale() {
        return !isMale();
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
    
    // ESCAPE
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

        // Increase the speed for escape
        x += Constants.escapeSpeedFactor * speed * directionX;
        y += Constants.escapeSpeedFactor * speed * directionY;

        // Ensure the rabbit stays within the screen edges
        handleScreenEdges();
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
    
    //REPRODUCING, NEW SPAWNS
    public boolean isHungry() {
        return hungry;  // Replace 'hungry' with your actual hunger condition
    }
    
    public long getLastSuccessfulMatingTime() {
        return lastSuccessfulMatingTime;
    }
    
    public boolean isMating() {
        return isMating;
    }

    public void setMating(boolean mating) {
        isMating = mating;
    }
    
    public int getSpeed() {
        return Constants.ADULT_SIZE;
    }
    
    public static Main getMainInstance() {
        return mainInstance;
    }
    
    private void startMatingCountdown() {
        matingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Prey rabbit : Main.getRabbits()) {
                    if (rabbit != null && !rabbit.isHungry() && !rabbit.isEaten()) {
                        long currentTime = System.currentTimeMillis();
                        long timeSinceLastMating = currentTime - rabbit.getLastSuccessfulMatingTime();

                        if (timeSinceLastMating >= Constants.MATING_CYCLE) {
                            rabbit.mates();
                        }
                    }
                }
            }
        }, 0, Constants.MATING_CYCLE);
    }
    
    private void renewMatingCycle() {
        matingTimer.cancel();
        matingTimer = new Timer();
        startMatingCountdown();
    }
    
    private Prey findMate() {
        List<Prey> rabbits = Main.getRabbits();  // method to get all rabbits
        Prey nearestMate = null;
        double nearestDistance = Double.MAX_VALUE; // Start with the largest value possible

        for (Prey rabbit : rabbits) {
            // Check basic conditions first
            if (rabbit != this && rabbit.isMale() != this.isMale() && !rabbit.isEaten() && rabbit.getAge() == 2 && !rabbit.isMating()) {
                double distance = Constants.calculateDistance(getX(), getY(), rabbit.getX(), rabbit.getY());
                
                // Check if the rabbit is within the reproduction distance and is the nearest so far
                if (distance <= Constants.REPRODUCTION_DISTANCE && distance < nearestDistance) {
                    nearestMate = rabbit;
                    nearestDistance = distance;
                }
            }
        }

        return nearestMate;
    }
    
    private void moveTowardsMate(Prey mate) {
        // Check if the current rabbit is male and the mate is female
        if (this.isMale() && mate.isFemale()) {
            // Calculate the angle between the two rabbits
            double angle = Math.atan2(mate.getY() - getY(), mate.getX() - getX());

            // Calculate the new direction based on the angle
            directionX = (int) Math.round(Math.cos(angle));
            directionY = (int) Math.round(Math.sin(angle));

            // Move the rabbit towards the mate
            x += speed * directionX;
            y += speed * directionY;

            // Check if the rabbit is close enough to the mate
            double distance = Constants.calculateDistance(getX(), getY(), mate.getX(), mate.getY());
            if (distance <= Constants.MATE_PROXIMITY) {
                // If close enough, stop moving
                x = mate.getX();  // Set exact position to avoid overshooting
                y = mate.getY();
                directionX = 0;
                directionY = 0;
            }

            // Handle screen edges to prevent going off-screen
            handleScreenEdges();
        }
    }
    
    public void mates() {
        // Find a suitable mate
        Prey mate = findMate();

        if (mate != null) {
            // Move both rabbits towards each other
            moveTowardsMate(mate);
            mate.moveTowardsMate(this);

            // Both rabbits halt movements
            stopMovements();
            mate.stopMovements();

            // Use SwingWorker to handle the waiting and post-waiting actions
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // Waiting for 3 seconds in the background thread
                    Thread.sleep(3000);
                    return null;
                }

                @Override
                protected void done() {
                    // Executed on the EDT after the background task is completed
                    // Resuming movements for both rabbits
                    setRandomDirection();
                    mate.setRandomDirection();

                    // Spawn a new rabbit (create a newborn)
                    Prey newborn = createNewborn(Prey.this);

                    // Update the last successful mating time for both rabbits
                    lastSuccessfulMatingTime = System.currentTimeMillis();
                    mate.lastSuccessfulMatingTime = System.currentTimeMillis();

                    // Renew the seasonal mating cycle for both rabbits
                    renewMatingCycle();
                    mate.renewMatingCycle();

                    // Set mating status to false after successful reproduction
                    setMating(false);
                    mate.setMating(false);
                }
            };

            // Start the SwingWorker
            worker.execute();
        }
    }
    
    private void createNewborns(Prey parent) {
        // Generate a random number of newborns between 1 and 8
        int numberOfNewborns = (int) (Math.random() * 8) + 1;

        // Create and add each newborn to the existing array
        for (int i = 0; i < numberOfNewborns; i++) {
            // Generate random properties for each newborn (you may adjust this based on your requirements)
            int startX = parent.getX();  // Use the same X position as the parent
            int startY = parent.getY();  // Use the same Y position as the parent
            int initialSpeed;
            if (parent.getAge() == 0) {
                initialSpeed = (int) (Constants.ADULT_SIZE * Constants.BABY_SPEED_FACTOR);
            } else {
                initialSpeed = parent.getSpeed();  // Use the same initial speed as the parent
            }
            int initialDirectionX = 0;  // Initialize directionX for the newborn (you may adjust this based on your requirements)
            int initialDirectionY = 0;  // Initialize directionY for the newborn (you may adjust this based on your requirements)
            boolean isMale = Math.random() < 0.5;  // 50% chance of being male

            // Create a new Prey object for the newborn
            Prey newborn = new Prey(startX, startY, initialSpeed, initialDirectionX, initialDirectionY, isMale);

            // Set age to 0 for baby rabbit
            newborn.transitionAge(0);
            Main.addRabbit(newborn);  // Access the addRabbit method in a static way using the class name
            newborn.scheduleTransition(1, Constants.TRANSITION_DELAY);  // Adjust this method based on your implementation
        }
    }
    
    private Prey createNewborn(Prey parent) {
        createNewborns(parent);
        return Main.getRabbits().get(Main.getRabbits().size() - 1);  // Access the getRabbits method in a static way
    }
    
    @Override
    public void draw(Graphics g) {
        int size = getSizeByAge();

        if (isEaten) {
            // Draw the image for a dead prey
            g.drawImage(Constants.loadImage(Constants.DEAD_PREY_IMAGE_PATH), x, y, size, size, this);
        } else {
            // Draw the prey image based on age and gender
            if (isMale) {
                // Draw blue border for male rabbits
                g.setColor(Color.BLUE);
                g.drawRect(x, y, size, size);
                g.setColor(Color.BLACK); // Reset color for the image
                g.drawImage(rabbitImage, x + 1, y + 1, size - 2, size - 2, this);
            } else {
                // Draw pink color for female rabbits
                g.setColor(Color.PINK);
                g.fillRect(x, y, size, size);
                g.setColor(Color.BLACK); // Reset color for the image
                g.drawImage(rabbitImage, x + 1, y + 1, size - 2, size - 2, this);
            }
        }
    }
}