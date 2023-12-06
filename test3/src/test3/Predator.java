package test3;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;
import java.util.Random;
import java.awt.Color;
import java.util.List;

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
    private boolean isMating = false;
    private int matingCooldown = 0;

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
        timer = new Timer();
        setDoubleBuffered(true);
        System.setProperty("sun.java2d.opengl", "true");
        this.id = ++Constants.lastFoxId; // Assign and increment the ID
        hungerTimer = new Timer();
        this.isMale = isMale;
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
    
    public boolean isMating() {
        return isMating;
    }

    public String getStatus() {
        String sex = isMale ? "Male" : "Female";
        return "Fox ID: " + id + ", Age: " + age + ", Size: " + getSizeByAge() + ", Sex: " + sex;
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
    
    private void scheduleTransition(int targetAge, int delay) {
        timer.schedule(new AgeTransitionTask(this, targetAge), delay);
    }

    private void scheduleDeath(int delay) {
        timer.schedule(new DeathTask(this), delay);
    }

    public void die() {
        Main.removeFox(this); // Remove fox from the simulation
        timer.schedule(new RemoveImageTask(this), Constants.IMAGE_REMOVAL_DELAY);
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
            predator.foxImage = null;  // Set the image to null
        }

        private void stopMovements() {
            // Cancel any remaining tasks in the timer
            predator.directionX = 0;
            predator.directionY = 0;
            // Implement additional logic to stop movements or behaviors
        }
    }
    
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
    
    // Add a method to start the hunger countdown
    private void startHungerCountdown() {
        hungerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (hungry) {
                    die(); // Fox dies if hungry for more than FOX_DEATH_BY_HUNGER milliseconds
                } else {
                    hungry = true;
                    // Schedule death after 40 seconds of continuous hunger
                    Timer deathTimer = new Timer();
                    deathTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            die(); // Fox dies after 40 seconds of continuous hunger
                        }
                    }, Constants.FOX_DEATH_BY_HUNGER);
                }
            }
        }, Constants.FOX_HUNGER_TIME);
    }

    
    // Add a method to stop the hunger countdown
    private void stopHungerCountdown() {
        hungerTimer.cancel();
        hungerTimer = new Timer();
    }
    
    private void chase(List<Prey> rabbits) {
        Prey targetRabbit = findNearestRabbit(rabbits);

        if (targetRabbit != null) {
            double distance = Constants.calculateDistance(getX(), getY(), targetRabbit.getX(), targetRabbit.getY());

            if (distance <= Constants.HUNTING_RANGE) {
                // Fox catches the rabbit based on success rate
                int successRate = (getAge() == 2) ? Constants.ADULT_FOX_SUCCESSFUL_HUNT : Constants.YOUNG_FOX_SUCCESSFUL_HUNT;
                if (Math.random() * 100 < successRate) {
                    // Replace the image with a dead rabbit image
                    targetRabbit.setEaten(true);
                    // Schedule the removal after a delay
                    Timer removeRabbitTimer = new Timer();
                    removeRabbitTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            removeEatenRabbit(targetRabbit);
                        }
                    }, 1000); // Delay of 1 second
                    hungry = false; // Reset hunger state
                    stopHungerCountdown(); // Reset hunger countdown
                    // Continue movement or perform other actions after a successful hunt
                } else {
                    // Rabbit escaped, stop chasing
                    directionX = 0;
                    directionY = 0;

                    // Add code to go back to default movement based on fox's age
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

                    // Replace the immediate die call with setting the fox as hungry
                    hungry = true; // Set the fox as hungry
                    stopHungerCountdown(); // Stop the hunger countdown
                }
            }
        }
    }


    // Method to remove a rabbit from the list
    private void removeEatenRabbit(Prey rabbit) {
        Main.removeRabbit(rabbit); // Remove rabbit from the simulation
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
    
    public void startMatingSeason() {
        if (matingCooldown <= 0) {
            matingCooldown = Constants.MATING_SEASON_DURATION;
            mate();
        }
    }
    
    private void mate() {
        if (isMale && !isMating && age == 2) { // Check if the predator is an adult
            Predator mate = findMate();
            if (mate != null && mate.getAge() == 2) { // Check if the mate is an adult
                isMating = true;
                moveTowards(mate.getX(), mate.getY());

                Timer matingTimer = new Timer();
                matingTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        spawnNewFox(mate);
                        isMating = false;
                    }
                }, Constants.MATING_DELAY);
            }
        }
    }
    
    private Predator findMate() {
        for (Predator predator : Main.getFoxes()) {
            if (predator.isMale() != this.isMale() && !predator.isMating()) {
                return predator;
            }
        }
        return null;
    }
    
    public Predator getOffspring() {
    	// probability or conditions for reproduction
        if (isMating() && age == 2 && Math.random() < Constants.PREDATOR_REPRODUCTION) {
            // Create a new baby rabbit with similar characteristics
            return new Predator(x, y, originalSpeed, directionX, directionY, Math.random() < 0.5);
        }
        return null; // No offspring
    }
    
    private void moveTowards(int targetX, int targetY) {
        // Calculate the direction to move towards the target
        int deltaX = targetX - getX();
        int deltaY = targetY - getY();

        // Calculate the distance between the predator and the target
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // Normalize the direction vector
        double normalizedDeltaX = deltaX / distance;
        double normalizedDeltaY = deltaY / distance;

        // Adjust the predator's direction based on the normalized vector
        directionX = (int) Math.round(normalizedDeltaX);
        directionY = (int) Math.round(normalizedDeltaY);
    }
    
    private void spawnNewFox(Predator mate) {
        // Implement logic to spawn a new baby fox at the female's position
        int babyX = mate.getX();
        int babyY = mate.getY();

        // Randomly decide the number of baby foxes to spawn (between 1 and 5)
        int numberOfBabyFoxes = (int) (Math.random() * 5) + 1;

        for (int i = 0; i < numberOfBabyFoxes; i++) {
            // Randomly decide the sex of the baby fox
            boolean isBabyMale = Math.random() < 0.5;

            // Create a new baby fox and add it to the list
            Predator babyFox = new Predator(babyX, babyY, originalSpeed, directionX, directionY, isBabyMale);
            babyFox.transitionAge(0); // Set the age to baby
            Main.getFoxes().add(babyFox);
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

        // Draw the fox image
        if (hungry) {
            // Draw a red border around the fox if hungry
            g.setColor(Color.RED);
            g.drawRect(x - 1, y - 1, size + 1, size + 1);
        }
        g.drawImage(foxImage, x, y, size, size, this);
    }
}