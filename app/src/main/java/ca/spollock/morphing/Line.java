package ca.spollock.morphing;

public class Line {
    public Point start, end;
    private Vector vector;
    public Line(float startX, float startY, float endX, float endY) {
        start = new Point(startX, startY);
        end = new Point(endX, endY);
    }
    public Line(float startX, float startY) { // for convenience
        start = new Point(startX, startY);
        end = new Point(startX, startY);
    }

    public Vector getLineVector(){
        vector = new Vector((start.getX() - end.getX()), (start.getY() - end.getY()));
        return vector;
    }
}
