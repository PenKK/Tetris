import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Arrays;
import java.util.LinkedList;

public class Grid {
    private boolean gameOver = false;
    private int score = 0;
    private int level;
    private final int startingLevel;
    // private int combo = 0; 
    private int lines;

    // Gravity constants scale by level https://harddrop.com/wiki/Tetris_Worlds
    private final double[] GRAVITY = new double[] { 0.01667, 0.021017, 0.026977, 0.035256, 0.04693, 0.06361, 0.0879,
            0.1236, 0.1775, 0.2598, 0.388, 0.59, 0.92, 1.46, 2.36 };
    private int dropRate;
    private int ticksTillDrop = dropRate;

    private final int COLUMNS;
    private final int ROWS;
    private final int PITY_ROWS = 2;
    private final double SCALE;

    private final int TILE_SIZE;
    private final int UI_TILE_SIZE;
    private final int GRID_WIDTH;
    private final int GRID_HEIGHT;

    private char[][] grid;
    private LinkedList<Tetromino> queue;

    private boolean canHold;
    private Tetromino currentTetromino;
    private Tetromino heldTetromino;

    Grid(int rows, int startingLevel, double scale) {
        this.SCALE = scale;
        this.ROWS = rows;
        this.COLUMNS = rows / 2;
        this.startingLevel = startingLevel;
        this.level = startingLevel;

        this.GRID_WIDTH = (int) (400 * scale);
        this.GRID_HEIGHT = GRID_WIDTH * 2;
        this.TILE_SIZE = GRID_WIDTH / COLUMNS;
        this.UI_TILE_SIZE = GRID_WIDTH / 10;

        init();
    }

    public void init() {
        grid = new char[ROWS + PITY_ROWS][COLUMNS];
        updateDropRate();
        canHold = true;

        queue = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            queue.push(getNextTetromino());
        }

        nextTetromino();
    }

    public void reset() {
        score = 0;
        level = startingLevel;
        lines = 0;

        init();
        gameOver = false;
    }

    public void tick() {
        if (gameOver)
            return;

        ticksTillDrop--;

        if (ticksTillDrop < 0) {
            moveTetrominoDown();
            ticksTillDrop = dropRate;

            if (!currentTetromino.canMoveDown(grid)) {
                ticksTillDrop = 200;
            }
        }

        if (currentTetromino.getLockTimer() == 0) {
            addToGrid(currentTetromino, false);
        } else if (currentTetromino.getLockTimer() != -1) {
            currentTetromino.setLockTimer(currentTetromino.getLockTimer() - 1);
        }
    }

    // GRID LOGIC

    public void addToGrid(Tetromino tetromino, boolean doublePlaceScore) {
        Point[] shape = tetromino.getPoints().clone();

        for (Point p : shape) {
            grid[p.x][p.y] = tetromino.getCharacter();
            score += doublePlaceScore ? 2 : 1;
        }

        canHold = true;
        lineClearCheck();
        nextTetromino();
    }

    public void lineClearCheck() {
        int linesCleared = 0;
        int scoreGained = 0;
        boolean isFull;

        for (int r = 0; r < PITY_ROWS + ROWS; r++) {
            isFull = true;
            for (int c = 0; c < COLUMNS; c++) {
                if (grid[r][c] == 0) {
                    isFull = false;
                    break;
                }
            }
            if (isFull) {
                linesCleared++;
                deleteRow(r);
            }
        }

        switch (linesCleared) {
            case 1:
                scoreGained = 100;
                break;
            case 2:
                scoreGained = 300;
                break;
            case 3:
                scoreGained = 500;
                break;
            case 4:
                scoreGained = 800;
                break;
            default:
                break;
        }

        score += scoreGained * level;
        lines += linesCleared;
        level = lines / 10 + startingLevel;

        updateDropRate();
    }

    public void updateDropRate() {
        dropRate = (int) (1 / GRAVITY[level > GRAVITY.length ? GRAVITY.length - 1 : level - 1] * 3.33);
    }

    private void deleteRow(int rowToDelete) {
        // Shifts the rows above down
        Arrays.fill(grid[rowToDelete], '\u0000');
        for (int r = rowToDelete; r > 0; r--) {
            grid[r] = Arrays.copyOf(grid[r - 1], COLUMNS);
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void drawSideContainers(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        g.drawRect((int) (UI_TILE_SIZE * 15.5), (int) (UI_TILE_SIZE * 1.5), UI_TILE_SIZE * 4, UI_TILE_SIZE * 15); // Queue - right side
        final int holdBoxHeight = 4;

        g.drawRect((int) (UI_TILE_SIZE * .5), (int) (UI_TILE_SIZE * 1.5), UI_TILE_SIZE * 4,
                UI_TILE_SIZE * holdBoxHeight); // Hold - left side
    }

    public void drawGridLines(Graphics2D g) {
        g.setColor(Color.GRAY);
        // Vertical grid
        for (int c = 0; c < COLUMNS + 1; c++) {
            g.drawLine(c * TILE_SIZE + GRID_WIDTH / 2, 0, c * TILE_SIZE + GRID_WIDTH / 2, GRID_HEIGHT);
        }
        // Horizontal grid
        for (int r = 0; r < ROWS + 1; r++) {
            g.drawLine(GRID_WIDTH / 2, r * TILE_SIZE, COLUMNS * TILE_SIZE + GRID_WIDTH / 2, r * TILE_SIZE);
        }
    }

    // TETROMINO LOGIC

    public void hold() {
        if (!canHold)
            return;

        canHold = false;
        Tetromino tempTetromino = heldTetromino == null ? null
                : new Tetromino(heldTetromino.getType(), COLUMNS / 2, PITY_ROWS);

        heldTetromino = new Tetromino(currentTetromino.getType());
        if (tempTetromino == null) {
            nextTetromino();
        } else {
            currentTetromino = tempTetromino;
        }
    }

    public void drawLocked(Graphics2D g) {
        g.setColor(Color.WHITE);
        for (int r = 0; r < ROWS + PITY_ROWS; r++) {
            for (int c = 0; c < COLUMNS; c++) {
                if (grid[r][c] != 0) {
                    g.setColor(TetrominoType.getColor(grid[r][c]));
                    g.fillRect(c * TILE_SIZE + GRID_WIDTH / 2, r * TILE_SIZE - TILE_SIZE * PITY_ROWS, TILE_SIZE,
                            TILE_SIZE);
                }
            }
        }
    }

    public void drawHold(Graphics g) {
        GraphicsPanel.drawCenteredString(g, "HOLD", Color.WHITE, (int) (100 * SCALE), (int) (4 * SCALE),
                (int) (30 * SCALE));

        if (heldTetromino == null)
            return;

        g.setColor(heldTetromino.getColor());
        Point[] tetrominoPoints = heldTetromino.getPoints().clone();

        for (Point p : tetrominoPoints) {
            int x = UI_TILE_SIZE + p.y * UI_TILE_SIZE;
            int y = UI_TILE_SIZE * 2 + p.x * UI_TILE_SIZE;

            switch (heldTetromino.getType()) {
                case O:
                    x += UI_TILE_SIZE / 2;
                    y += UI_TILE_SIZE / 2;
                    break;
                case I:
                    x -= UI_TILE_SIZE / 2;
                    y += UI_TILE_SIZE;
                    break;
                default:
                    y += UI_TILE_SIZE / 2;
                    break;
            }

            g.fillRect(x, y, UI_TILE_SIZE, UI_TILE_SIZE);
        }

    }

    public void drawScore(Graphics g) {
        g.drawRect((int) (UI_TILE_SIZE * .5),  (UI_TILE_SIZE * 7), UI_TILE_SIZE * 4, UI_TILE_SIZE * 6); // Border

        // Headers
        GraphicsPanel.drawCenteredString(g, "SCORE", Color.WHITE, (int) (100 * SCALE),  (UI_TILE_SIZE * 7),
                (int) (30 * SCALE));
        GraphicsPanel.drawCenteredString(g, "LEVEL", Color.WHITE, (int) (100 * SCALE),  (UI_TILE_SIZE * 9),
                (int) (30 * SCALE));
        GraphicsPanel.drawCenteredString(g, "LINES", Color.WHITE, (int) (100 * SCALE),  (UI_TILE_SIZE * 11),
                (int) (30 * SCALE));

        // Labels
        GraphicsPanel.drawCenteredString(g, score + "", Color.GREEN, (int) (100 * SCALE),  (UI_TILE_SIZE * 8),
                (int) (30 * SCALE));
        GraphicsPanel.drawCenteredString(g, level + "", Color.GREEN, (int) (100 * SCALE),  (UI_TILE_SIZE * 10),
                (int) (30 * SCALE));
        GraphicsPanel.drawCenteredString(g, lines + "", Color.GREEN, (int) (100 * SCALE),  (UI_TILE_SIZE * 12),
                (int) (30 * SCALE));
    }

    public void nextTetromino() {
        currentTetromino = queue.pop();
        queue.add(getNextTetromino());

        int i = 0;
        while (!checkGridEmpty(currentTetromino.getPoints())) { // If the default spawn location is occupied, move up
            if (i >= PITY_ROWS) { // If there are no free spots end the game
                gameOver = true;
                return;
            }
            currentTetromino.moveUp();
            i++;
        }
    }

    public boolean checkGridEmpty(Point[] points) {
        for (Point p : points) {
            if (grid[p.x][p.y] != 0)
                return false;
        }
        return true;
    }

    public void drawTempTetromino(Graphics g, Tetromino tetromino) {
        Point[] shape = tetromino.getPoints();
        for (Point point : shape) {
            g.setColor(tetromino.getColor());
            g.fillRect(point.y * TILE_SIZE + GRID_WIDTH / 2, point.x * TILE_SIZE - PITY_ROWS * TILE_SIZE,
                    TILE_SIZE, TILE_SIZE);
        }
    }

    public void drawQueueTetrominos(Graphics g) {
        for (int i = 0; i < queue.size(); i++) {
            Tetromino tetromino = new Tetromino(queue.get(i).getType());
            Point[] points = tetromino.getPoints();
            g.setColor(tetromino.getColor());

            for (Point p : points) {
                int x = p.y * UI_TILE_SIZE + GRID_WIDTH / 2 + UI_TILE_SIZE * 11;
                int y = p.x * UI_TILE_SIZE + UI_TILE_SIZE * i * 5 + UI_TILE_SIZE * 2;

                switch (tetromino.getType()) {
                    case O:
                        x += UI_TILE_SIZE / 2;
                        break;
                    case I:
                        x -= UI_TILE_SIZE / 2;
                        break;
                    default:
                        break;
                }
                g.fillRect(x, y, UI_TILE_SIZE, UI_TILE_SIZE);
            }
        }

        GraphicsPanel.drawCenteredString(g, "NEXT", Color.WHITE, (int) (700 * SCALE), (int) (4 * SCALE),
                (int) (30 * SCALE));
    }

    public Tetromino getNextTetromino() {
        return new Tetromino((int) (Math.random() * TetrominoType.values().length), COLUMNS / 2 - 1, PITY_ROWS);
    }

    public void moveTetrominoDown() {
        if (currentTetromino.canMoveDown(grid)) {
            currentTetromino.moveDown();
            ticksTillDrop = dropRate;
        } else if (currentTetromino.getLockTimer() == -1) {
            currentTetromino.setLockTimer(500);
        }
    }

    public void moveTetrominoRight() {
        if (currentTetromino.canMoveSide(grid, true)) {
            currentTetromino.moveSide(true);
        }
    }

    public void moveTetrominoLeft() {
        if (currentTetromino.canMoveSide(grid, false)) {
            currentTetromino.moveSide(false);
        }
    }

    public void dropTetromino() {
        currentTetromino.drop(grid);
        addToGrid(currentTetromino, true);
        ticksTillDrop = dropRate;
    }

    public void rotate(boolean counterClockwise) {
        if (currentTetromino.canRotate(grid, counterClockwise))
            currentTetromino.rotate(counterClockwise);
    }

    public Tetromino getGhostTetromino() {
        Tetromino ghostTetromino = currentTetromino.getCopy();

        int R = ghostTetromino.getColor().getRed() + 200;
        int G = ghostTetromino.getColor().getGreen() + 200;
        int B = ghostTetromino.getColor().getBlue() + 200;
        ghostTetromino.setColor(new Color(Math.min(R, 255), Math.min(G, 255), Math.min(B, 255)));

        while (ghostTetromino.canMoveDown(grid)) {
            ghostTetromino.moveDown();
        }
        return ghostTetromino;
    }

    public Tetromino getCurrentTetromino() {
        return currentTetromino;
    }

    public static void print2DArray(char[][] Matrix) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < Matrix.length; i++) {
            for (int j = 0; j < Matrix[0].length; j++) {
                if (Matrix[i][j] != 0) {
                    str.append(Matrix[i][j]);
                } else {
                    str.append('#');
                }

            }
            str.append("\n");
        }
        System.out.println(str);
    }

    public void setCurrentTetromino(Tetromino currentTetromino) {
        this.currentTetromino = currentTetromino;
    }

    public int getTileSize() {
        return TILE_SIZE;
    }

    public int getGridWidth() {
        return GRID_WIDTH;
    }

    public int getGridHeight() {
        return GRID_HEIGHT;
    }

    public int getColumns() {
        return COLUMNS;
    }

    public int getRows() {
        return ROWS;
    }

    public char[][] getGrid() {
        return grid;
    }

    public double getScale() {
        return SCALE;
    }

    public int getScore() {
        return score;
    }

    public int getUITileSize() {
        return UI_TILE_SIZE;
    }
}
