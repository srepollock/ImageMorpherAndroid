package ca.spollock.morphing;

/**
 * Point class for the lines and the vector classes
 */
public class Point {
    /**
     * X and Y position of the point
     */
    float x, y;

    /**
     * Contsructor for the point
     */
    public Point(float x, float y){
        this.x = x;
        this.y = y;
    }
    /**
     * Gets the X of the point
     */
    public float getX(){
        return this.x;
    }
    /**
     * Gets the Y of the point
     */
    public float getY(){ return this.y; }
    /**
     * Sets the X of the point
     */
    public void setX(float x){
        this.x = x;
    }
    /**
     * Sets the Y of the point
     */
    public void setY(float y){
        this.y = y;
    }
}
