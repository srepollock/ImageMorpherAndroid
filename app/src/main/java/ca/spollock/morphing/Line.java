package ca.spollock.morphing;

/**
 * Created by Spencer on 2016-01-07.
 */
public class Line {
    float startX, startY, endX, endY;
    public Line(float startX, float startY, float endX, float endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }
    public Line(float startX, float startY) { // for convenience
        this(startX, startY, startX, startY);
    }
}
