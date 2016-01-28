package ca.spollock.morphing;

public class Line {
    public Point start, end;
    public Line(float startX, float startY, float endX, float endY) {
        start = new Point(startX, startY);
        end = new Point(endX, endY);
    }
    public Line(float startX, float startY) { // for convenience
        this(startX, startY, startX, startY);
    }
}
