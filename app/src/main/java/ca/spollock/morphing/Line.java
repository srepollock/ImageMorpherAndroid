package ca.spollock.morphing;

/**
 * Line class for all lines drawn on the editview and saved in the linecontroller
 */
public class Line {
    /**
     * Start and end points of the line
     */
    public Point start, end;
    /**
     * Constructor for the line taking in the start (x,y) and end (x,y)
     */
    public Line(float startX, float startY, float endX, float endY) {
        start = new Point(startX, startY);
        end = new Point(endX, endY);
    }
    /**
     * Constructor for the line taking in the start (x,y)
     */
    public Line(float startX, float startY) { // for convenience
        this(startX, startY, startX, startY);
    }
}
