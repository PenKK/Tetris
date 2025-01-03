import java.awt.Color;
import java.awt.Point;

public enum TetrominoType {
    // Points are (row, column) with 0,0 being in the top left corner
    I(new Point[] { new Point(0, 0), new Point(0, 1), new Point(0, 2), new Point(0, 3) }, new Color(0, 240, 240), 'I', true, new int[] { 1, 0 }),
    J(new Point[] { new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(1, 2) }, new Color(0, 0, 240), 'J', true),
    L(new Point[] { new Point(0, 2), new Point(1, 0), new Point(1, 1), new Point(1, 2) }, new Color(240, 160, 0), 'L', true),
    O(new Point[] { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) }, new Color(240, 240, 0), 'O', false),
    S(new Point[] { new Point(0, 1), new Point(0, 2), new Point(1, 0), new Point(1, 1) }, new Color(0, 240, 0), 'S', true),
    T(new Point[] { new Point(0, 1), new Point(1, 0), new Point(1, 1), new Point(1, 2) }, new Color(160, 0, 240), 'T', true),
    Z(new Point[] { new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) }, new Color(240, 0, 0), 'Z', true);

    private final Point[] shape;
    private final Color color;
    private final char character;
    private final int[] rotateOrigin; // The rotate origin is the point which the tetrominos will rotate around
    private final boolean rotatable;

    TetrominoType(Point[] shape, Color color, char character, boolean canRotate, int[] rotateOrigin) {
        this.shape = shape;
        this.color = color;
        this.character = character;
        this.rotateOrigin = rotateOrigin;
        this.rotatable = canRotate;
    }

    TetrominoType(Point[] shape, Color color, char character, boolean canRotate) {
        this.shape = shape;
        this.color = color;
        this.character = character;
        this.rotateOrigin = new int[] { 1, 1 }; // If no offset the constructor will default to 1, 1
        this.rotatable = canRotate;
    }

    public static Color getColor(char typeChar) {
        for (int i = 0; i < TetrominoType.values().length; i++) {
            if (TetrominoType.values()[i].character == typeChar) {
                return TetrominoType.values()[i].color;
            }
        }
        System.out.println("COLOR NOT FOUND");
        return Color.WHITE;
    }

    public Point[] getShape() {
        return shape;
    }

    public Color getColor() {
        return color;
    }

    public char getCharacter() {
        return character;
    }

    public int[] getRotateOrigin() {
        return rotateOrigin;
    }

    public boolean isRotatable() {
        return rotatable;
    }

}
