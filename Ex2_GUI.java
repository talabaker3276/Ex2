package assignments.Ex2;

import classes.week4.StdDraw;

import java.awt.Color;

public class Ex2_GUI {

    // Pixel grid size (increase for more pixels / more "blocky" look)
    private static final int W = 80;
    private static final int H = 50;

    // Middle block size (in grid cells)
    private static final int BLOCK_W = 18;
    private static final int BLOCK_H = 12;

    // Colors
    private static final Color BG = new Color(14, 14, 20);
    private static final Color GRID = new Color(30, 30, 40);
    private static final Color BLOCK = new Color(75, 75, 90);
    private static final Color HOT_PINK = new Color(255, 70, 180);

    // “Paint” buffer (stores intensity 0..255)
    private static int[][] paint = new int[W][H];

    public static void main(String[] args) {
        pixelMouseWorld();
    }

    /**
     * Pixelized world that follows the mouse with a pink glowing cursor and trail,
     * plus a solid block in the center.
     */
    public static void pixelMouseWorld() {
        StdDraw.setCanvasSize(1000, 650);
        StdDraw.setXscale(0, W);
        StdDraw.setYscale(0, H);
        StdDraw.enableDoubleBuffering();

        // Middle block bounds in grid coords
        int bx0 = (W - BLOCK_W) / 2;
        int by0 = (H - BLOCK_H) / 2;
        int bx1 = bx0 + BLOCK_W - 1;
        int by1 = by0 + BLOCK_H - 1;

        // Trail (stores previous mouse positions in grid cells)
        final int TRAIL = 30;
        int[] tx = new int[TRAIL];
        int[] ty = new int[TRAIL];
        for (int i = 0; i < TRAIL; i++) { tx[i] = W / 2; ty[i] = H / 2; }

        double t = 0;

        while (true) {
            // Mouse position in "grid space"
            int mx = clamp((int)Math.floor(StdDraw.mouseX()), 0, W - 1);
            int my = clamp((int)Math.floor(StdDraw.mouseY()), 0, H - 1);

            // Shift trail
            for (int i = TRAIL - 1; i > 0; i--) {
                tx[i] = tx[i - 1];
                ty[i] = ty[i - 1];
            }
            tx[0] = mx;
            ty[0] = my;

            // Optional painting: hold mouse pressed to paint pixels
            boolean pressed = StdDraw.isMousePressed();
            if (pressed && !inBlock(mx, my, bx0, by0, bx1, by1)) {
                paint[mx][my] = 255;
                // paint a tiny 3x3 brush
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int x = mx + dx, y = my + dy;
                        if (inside(x, y) && !inBlock(x, y, bx0, by0, bx1, by1)) {
                            paint[x][y] = Math.max(paint[x][y], 200);
                        }
                    }
                }
            }

            // Fade paint over time (cool pixel fade effect)
            for (int x = 0; x < W; x++) {
                for (int y = 0; y < H; y++) {
                    if (paint[x][y] > 0) paint[x][y] -= 6;  // fade speed
                    if (paint[x][y] < 0) paint[x][y] = 0;
                }
            }

            // Also add trail “glow” into paint buffer
            for (int i = 0; i < TRAIL; i++) {
                int x = tx[i], y = ty[i];
                if (inBlock(x, y, bx0, by0, bx1, by1)) continue;

                // intensity decreases with age
                int val = (int)(220 * (1.0 - (double)i / (TRAIL - 1)));
                val = clamp(val, 0, 220);
                paint[x][y] = Math.max(paint[x][y], val);
            }

            // Draw background
            StdDraw.clear(BG);

            // Draw pixel grid
            // Each cell is drawn as a filled square. We'll draw:
            // 1) base grid (dark)
            // 2) paint overlay (pink)
            // 3) middle block (solid)
            for (int x = 0; x < W; x++) {
                for (int y = 0; y < H; y++) {

                    // Base cell color
                    StdDraw.setPenColor(GRID);
                    drawCell(x, y);

                    // If in middle block, override with block color
                    if (inBlock(x, y, bx0, by0, bx1, by1)) {
                        StdDraw.setPenColor(BLOCK);
                        drawCell(x, y);
                        continue;
                    }

                    // Paint overlay
                    int a = paint[x][y]; // 0..255
                    if (a > 0) {
                        // Make it look more "neon" using alpha-ish blending manually
                        // (StdDraw doesn't support alpha directly usually)
                        // We'll approximate by brightening with intensity.
                        int r = clamp((HOT_PINK.getRed()   * a + GRID.getRed()   * (255 - a)) / 255, 0, 255);
                        int g = clamp((HOT_PINK.getGreen() * a + GRID.getGreen() * (255 - a)) / 255, 0, 255);
                        int b = clamp((HOT_PINK.getBlue()  * a + GRID.getBlue()  * (255 - a)) / 255, 0, 255);
                        StdDraw.setPenColor(new Color(r, g, b));
                        drawCell(x, y);
                    }
                }
            }

            // Draw cursor highlight (a little animated ring)
            if (!inBlock(mx, my, bx0, by0, bx1, by1)) {
                double pulse = 0.35 + 0.10 * Math.sin(t);
                StdDraw.setPenColor(HOT_PINK);
                StdDraw.setPenRadius();
                StdDraw.circle(mx + 0.5, my + 0.5, pulse);
            }

            // Draw the middle block outline (makes it pop)
            StdDraw.setPenColor(new Color(140, 140, 165));
            StdDraw.setPenRadius(0.003);
            StdDraw.rectangle((bx0 + bx1 + 1) / 2.0, (by0 + by1 + 1) / 2.0,
                    BLOCK_W / 2.0, BLOCK_H / 2.0);

            StdDraw.show();
            StdDraw.pause(16);
            t += 0.15;
        }
    }

    // Draw one cell as a square centered at (x+0.5, y+0.5)
    private static void drawCell(int x, int y) {
        StdDraw.filledSquare(x + 0.5, y + 0.5, 0.48);
    }

    private static boolean inside(int x, int y) {
        return x >= 0 && y >= 0 && x < W && y < H;
    }

    private static boolean inBlock(int x, int y, int bx0, int by0, int bx1, int by1) {
        return x >= bx0 && x <= bx1 && y >= by0 && y <= by1;
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
