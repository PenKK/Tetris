import javax.swing.JFrame;

public class Frame extends JFrame {
    public static void main(String[] args) {

        double scale;
        int rows;
        int startingLevel;

        // Attempt to grab values from args otherwise default to 20 rows scale 1.0

        try {
            scale = Double.parseDouble(args[0]);
        } catch (Exception e) {
            scale = 1; // Window size multiplier
        }

        try {
            rows = Integer.parseInt(args[1]); 

            if (rows % 2 == 1) // even only
                rows++;
            
            if (rows < 10) // minimum 10
                rows = 10;
            
            if (rows > 400) // max 400, UI breaks after 401
                rows = 400;
            
        } catch (Exception e) {
            rows = 20;
        }

        try {
            startingLevel = Integer.parseInt(args[2]);
        } catch (Exception e) {
            startingLevel = 1;
        }

        // The first parameter is the size/scale of the window and its components
        // A good default is 1 for the game width to be 400x800 pixels

        // The 2nd parameter is the # of rows, # of Columns will always be half of rows.
        // A normal tetris game typically has 20 rows and 10 columns (should be even)

        System.out.printf("Launch parameters: %d rows, scale %.2f%n", rows, scale);
        new Frame(scale, rows, startingLevel); // Use even number for rows thanks
    }

    GraphicsPanel graphicsPanel;

    Frame(double scale, int rows, int startingLevel) {
        graphicsPanel = new GraphicsPanel(scale, rows, startingLevel);
        this.add(graphicsPanel);

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        graphicsPanel.start();
    }
}
