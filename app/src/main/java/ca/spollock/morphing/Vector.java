package ca.spollock.morphing;

/**
 * Vector class for the lines
 */
public class Vector {
    /**
     * Point of the vector
     */
    private Point p;

    /**
     * Normal of the vector
     */
    private Vector normal;

    /**
     * Constructor for the vector
     */
    Vector(float x, float y){
        p = new Point(x, y);
    }

    /**
     * Constructor for the vector taking in a start and an end to calculate the vector
     */
    Vector(Point pp, Point q) { p = new Point((q.x - pp.x), (q.y - pp.y)); } // p--->q

    /**
     * Gets the X of the vector
     */
    public float getX(){ return p.getX(); }

    /**
     * Gets the Y of the vector
     */
    public float getY(){
        return p.getY();
    }

    /**
     * Sets the X of the vector
     */
    public void setX(float x){
        p.setX(x);
    }

    /**
     * Sets the Y of the vector
     */
    public void setY(float y){
        p.setY(y);
    }

    /**
     * Calculates the normal of the vector
     */
    private void calculateNormal(){
        float invY = p.getY() * -1;
        float tempX = p.getX();
        normal = new Vector(invY, tempX);
    }

    /**
     * Returns the normal of the vector
     */
    public Vector getNormal(){
        calculateNormal();
        return normal;
    }
}
