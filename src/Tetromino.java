
import java.awt.Color;
import java.awt.Point;

public class Tetromino {

    private Point[] points;
    private TetrominoType type;
    private Color color;
    private char character;
    private int[] offsetFromOrigin; // Tracks displacement from 0, 0 so that the rotation transformations can be applied

    Tetromino(TetrominoType type) {
        this.type = type;
        this.points = type.getShape();
        this.color = type.getColor();
        this.character = type.getCharacter();
        this.offsetFromOrigin = type.getRotateOrigin().clone();
    }

    Tetromino(TetrominoType type, int column, int row) {
        this.type = type;
        this.points = type.getShape();
        this.color = type.getColor();
        this.character = type.getCharacter();
        this.offsetFromOrigin = type.getRotateOrigin().clone();

        for (int i = 0; i < column; i++) {
            this.moveSide(true);
        }

        for (int i = 0; i < row; i++) {
            this.moveDown();
        }
    }

    Tetromino(int typeIndex) {
        TetrominoType[] types = TetrominoType.values();

        this.type = types[typeIndex];
        this.points = type.getShape();
        this.color = type.getColor();
        this.character = type.getCharacter();
        this.offsetFromOrigin = type.getRotateOrigin().clone();
    }

    Tetromino(int typeIndex, int column) {
        TetrominoType[] types = TetrominoType.values();

        this.type = types[typeIndex];
        this.points = type.getShape().clone();
        this.color = type.getColor();
        this.character = type.getCharacter();
        this.offsetFromOrigin = type.getRotateOrigin().clone();

        for (int i = 0; i < column; i++) {
            this.moveSide(true);
        }
    }

    Tetromino(int typeIndex, int column, int row) {
        TetrominoType[] types = TetrominoType.values();

        this.type = types[typeIndex];
        this.points = type.getShape().clone();
        this.color = type.getColor();
        this.character = type.getCharacter();
        this.offsetFromOrigin = type.getRotateOrigin().clone();

        for (int i = 0; i < column; i++) {
            this.moveSide(true);
        }

        for (int i = 0; i < row; i++) {
            this.moveDown();
        }

    }

    public void moveDown() {
        Point[] newShape = new Point[points.length];
        for (int i = 0; i < points.length; i++) {
            newShape[i] = new Point(points[i].x + 1, points[i].y);
        }
        offsetFromOrigin[1]++;
        points = newShape;
    }

    public void moveUp() {
        Point[] newShape = new Point[points.length];
        for (int i = 0; i < points.length; i++) {
            newShape[i] = new Point(points[i].x - 1, points[i].y);
        }
        offsetFromOrigin[1]--;
        points = newShape;
    }

    public boolean canMoveDown(char[][] board) {
        for (int i = 0; i < points.length; i++) {
            try {
                if (board[points[i].x + 1][points[i].y] != 0) {
                    return false;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                return false;
            }

        }

        return true;
    }

    public void moveSide(boolean rightSide) {
        // Didn't want to duplicate code for smth simple so both sides
        // are handled in here with a ternary with rightSide boolean
        Point[] newShape = new Point[points.length];
        for (int i = 0; i < points.length; i++) {
            newShape[i] = new Point(points[i].x, points[i].y + (rightSide ? 1 : -1));
        }

        offsetFromOrigin[0] = offsetFromOrigin[0] + (rightSide ? 1 : -1);
        points = newShape;
    }

    public boolean canMoveSide(char[][] board, boolean rightSide) {
        for (int i = 0; i < points.length; i++) {
            try {
                int pieceX = points[i].x;
                int pieceY = points[i].y;
                if (board[pieceX][pieceY + (rightSide ? 1 : -1)] != 0) {
                    return false;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                return false;
            }
        }

        return true;
    }

    public void rotate(boolean counterClockwise) {
        points = removeOffset(points); // Bring pieces relative to 0,0
        points = rotatePoints(points, counterClockwise); // Apply CW 90 degrees transformation
        points = returnOffset(points); // Bring pieces back to where they were
    }

    public void drop(char[][] board) {
        while (canMoveDown(board)) {
            moveDown();
        }
    }

    public boolean canRotate(char[][] board, boolean counterClockwise) {
        if (!getType().isRotatable())
            return false;

        Point[] tempPoints = new Point[points.length];

        for (int i = 0; i < points.length; i++) {
            tempPoints[i] = (Point) this.getPoints()[i].clone();
        }

        tempPoints = removeOffset(tempPoints);
        tempPoints = rotatePoints(tempPoints, counterClockwise);
        tempPoints = returnOffset(tempPoints);

        try {
            for (int i = 0; i < tempPoints.length; i++) {
                if (board[tempPoints[i].x][tempPoints[i].y] != 0) {
                    return false;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }

    private Point[] removeOffset(Point[] pts) {
        Point[] newPoints = pts.clone();
        for (int i = 0; i < newPoints.length; i++) {
            newPoints[i].translate(-offsetFromOrigin[1], -offsetFromOrigin[0]);
        }
        return newPoints;
    }

    private Point[] rotatePoints(Point[] pts, boolean counterClockwise) {
        Point[] newPoints = new Point[points.length];
        for (int i = 0; i < pts.length; i++) {
            newPoints[i] = new Point(pts[i].y * (counterClockwise ? -1 : 1), pts[i].x * (counterClockwise ? 1 : -1));
        }
        return newPoints;
    }

    private Point[] returnOffset(Point[] pts) {
        Point[] newPoints = pts.clone();
        for (int i = 0; i < newPoints.length; i++) {
            newPoints[i].translate(offsetFromOrigin[1], offsetFromOrigin[0]);
        }
        return newPoints;
    }

    public Point[] getPoints() {
        return points;
    }

    public void setPoints(Point[] shape) {
        this.points = shape;
    }

    public TetrominoType getType() {
        return type;
    }

    public void setType(TetrominoType type) {
        this.type = type;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public char getCharacter() {
        return character;
    }

    public void setCharacter(char character) {
        this.character = character;
    }

    public Tetromino getCopy() {
        Tetromino tetromino = new Tetromino(this.type);
        tetromino.points = this.points.clone();
        tetromino.offsetFromOrigin = this.offsetFromOrigin.clone();
        return tetromino;
    }
}
