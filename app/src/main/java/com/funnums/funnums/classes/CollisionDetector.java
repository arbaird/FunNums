package com.funnums.funnums.classes;

/**
 * Created by austinbaird on 10/9/17.
 */

public class CollisionDetector {

    public static boolean isCollision(TouchableNumber num1, TouchableNumber num2) {
        boolean collision = false;

        float distanceX = num1.getX() - num2.getX();

        // Get the distance of the two objects from
        // the centre of the circles on the y axis
        float distanceY = num1.getY() - num2.getY();

        // Calculate the distance between the center of each circle
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        //if the distance is contained in the radius of each cicle, the circles overlap!
        if (distance < num1.getRadius() + num2.getRadius())
            collision = true;

        return collision;
    }

    //overload method so we don't have to create otherwise unused TouchableNumber objects on every
    //iteration of the game loop, instead this overloaded method will just take an x, y, and radius
    //instead of a while new object
    public static boolean isCollision(TouchableNumber num1, int x, int y, int radius) {

        boolean collision = false;

        float distanceX = num1.getX() - x;

        // Get the distance of the two objects from
        // the centre of the circles on the y axis
        float distanceY = num1.getY() - y;

        // Calculate the distance between the center of each circle
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        //if the distance is contained in the radius of each cicle, the circles overlap!
        if (distance < num1.getRadius() + radius)
            collision = true;

        return collision;
    }
}
