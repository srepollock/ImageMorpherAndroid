package ca.spollock.morphing;

public class Vector {
    private Point p;
    private Vector normal;
    Vector(float x, float y){
        p.setX(x);
        p.setY(y);
    }

    public float getX(){
        return p.getX();
    }

    public float getY(){
        return p.getY();
    }

    public void setX(float x){
        p.setX(x);
    }

    public void setY(float y){
        p.setY(y);
    }

    public void calculateNormal(){
        float invY = p.getY() * -1;
        normal = new Vector(invY, p.getX());
    }

    public Vector getNormal(){
        calculateNormal();
        return normal;
    }
}
