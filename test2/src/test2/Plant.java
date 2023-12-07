package test2;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.Timer;

public class Plant {
    private static final Color GRASS_GREEN = new Color(34, 139, 34); // Dark green
    private static final int PATCH_SIZE = 20;
    private static final int PATCH_APPEARANCE_PROBABILITY = 5; // Adjust as needed
    // private static final int PATCH_DISAPPEAR_RATE = 2; // Adjust as needed
    private static final int FADE_OUT_DELAY = 5000000; // Milliseconds to wait before fading out (increased delay)
    private static final int FADE_IN_DELAY = 5000000; // Milliseconds to wait before fading in (increased delay)

    private boolean fadingOut = false;

    public void draw(Graphics g, int width, int height) {
        // Draw the green grass background
        g.setColor(GRASS_GREEN);
        g.fillRect(0, 0, width, height);

        if (!fadingOut) {
            // Draw the patches
            Random random = new Random();
            for (int x = 0; x < width; x += PATCH_SIZE) {
                for (int y = 0; y < height; y += PATCH_SIZE) {
                    if (random.nextInt(50000) < PATCH_APPEARANCE_PROBABILITY) {
                        // Draw a patch of a different shade of green
                        g.setColor(generateRandomGreenShade());
                        g.fillRect(x, y, PATCH_SIZE, PATCH_SIZE);
                    }
                }
            }
        } else {
            // If fading out, draw patches with a brown color
            g.setColor(new Color(139, 69, 19)); // Brown color
            for (int x = 0; x < width; x += PATCH_SIZE) {
                for (int y = 0; y < height; y += PATCH_SIZE) {
                    g.fillRect(x, y, PATCH_SIZE, PATCH_SIZE);
                }
            }
        }
    }

    private Color generateRandomGreenShade() {
        Random random = new Random();
        int red = 34; // Dark green base
        int green = random.nextInt(100) + 100; // Random shade of green
        int blue = 34; // Dark green base
        return new Color(red, green, blue);
    }

    public void startFadeOut() {
        fadingOut = true;
        Timer fadeOutTimer = new Timer(FADE_OUT_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fadingOut = false;
            }
        });
        fadeOutTimer.setRepeats(false);
        fadeOutTimer.start();
    }

    public void startFadeIn() {
        Timer fadeInTimer = new Timer(FADE_IN_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startFadeOut();
            }
        });
        fadeInTimer.setRepeats(false);
        fadeInTimer.start();
    }
}
