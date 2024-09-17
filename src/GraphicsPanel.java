import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.awt.Color;
import javax.swing.JPanel;

public class GraphicsPanel extends JPanel implements KeyListener {
    private Grid grid;

    private final int TICK_DELAY = 5;
    private final int FPS = 60;
    private boolean running = false;

    private static Font mainFont;

    GraphicsPanel(double scale, int rows, int startingLevel) {
        // Most of the game logic is handled in the grid
        grid = new Grid(rows, startingLevel, scale);
        addKeyListener(this);
        this.setFocusable(true);
        // Dimension is calculated by the tile size and rows/columns
        this.setPreferredSize(new Dimension((int) (grid.getTileSize() * grid.getColumns() * 2),
                grid.getTileSize() * grid.getRows() + 1));
        this.setBackground(Color.BLACK);

        try {
            mainFont = Font.createFont(Font.TRUETYPE_FONT, new File("lib\\fonts\\ChakraPetch-Medium.ttf"));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(mainFont);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
    }

    public void start() { // Game loop
        running = true;
        long deltaT = 0;
        long accumulatorT = 0;
        long lastUpdateT = System.currentTimeMillis();
        updateObjects();

        while (true) {
            long now = System.currentTimeMillis();
            deltaT = System.currentTimeMillis() - lastUpdateT;
            lastUpdateT += deltaT;
            accumulatorT += deltaT;

            while (accumulatorT >= TICK_DELAY) {
                updateObjects(); // Process one tick.
                accumulatorT -= TICK_DELAY;
            }

            if (!running) {
                break;
            }

            repaint(); // Draw frame

            // Limit renders to a certain fps
            long sleepTime = 1000 / FPS - (System.currentTimeMillis() - now);

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        repaint();
    }

    private void updateObjects() {
        grid.tick();
    }

    public void resetGame() {
        System.out.println("Restarting");
        grid.reset();
    }

    @Override
    public void paint(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;

        // Drawing main tetris grid and its components
        grid.drawGridLines(g);
        grid.drawLocked(g);
        grid.drawSideContainers(g);
        grid.drawScore(g);

        if (!grid.isGameOver()) {
            grid.drawTempTetromino(g, grid.getGhostTetromino());
            grid.drawTempTetromino(g, grid.getCurrentTetromino());
            grid.drawQueueTetrominos(g);
            grid.drawHold(g);
        } else {
            drawGameEnd(g);
        }
    }

    public static void drawCenteredString(Graphics g, String text, Color color, int x, int y, int size) {
        g.setColor(color);
        g.setFont(mainFont.deriveFont(Font.PLAIN, size));
        int textWidth = g.getFontMetrics().stringWidth(text);
        int xPos = x - textWidth / 2;
        g.drawString(text, xPos, y + size);
    }

    public void drawGameEnd(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(grid.getGridWidth() / 2, 0, grid.getGridWidth() + 1, grid.getGridHeight() + 1);
        drawCenteredString(g, "You lose!", Color.WHITE, grid.getGridWidth(), (int) (50 * grid.getScale()),
                grid.getUITileSize());
        drawCenteredString(g, "Press R to play again", Color.WHITE, grid.getGridWidth(),
                (int) (50 * grid.getScale()) * 2, grid.getUITileSize());
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_R && grid.isGameOver()) {
            resetGame();
        }
        if (grid.isGameOver())
            return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                grid.moveTetrominoRight();
                break;
            case KeyEvent.VK_LEFT:
                grid.moveTetrominoLeft();
                break;
            case KeyEvent.VK_UP:
                grid.rotate(false);
                break;
            case KeyEvent.VK_SPACE:
                grid.dropTetromino();
                break;
            case KeyEvent.VK_DOWN:
                grid.moveTetrominoDown();
                break;
            case KeyEvent.VK_CONTROL:
                grid.rotate(true);
                break;
            case KeyEvent.VK_C:
                grid.hold();
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public static Font getMainFont() {
        return mainFont;
    }
}