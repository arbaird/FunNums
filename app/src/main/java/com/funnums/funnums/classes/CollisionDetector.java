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
        //iteration of the game loop when checking spawn points, instead this overloaded method will
        // just take an x, y, and radius instead of a whole new object
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

    /*
        Given two touchable numbers (circles), this function corrects their positions so they are no longer overlapping
        using the midpoint formula for distance between circles. Super helpful link below
        http://ericleong.me/research/circle-circle/
     */
    public static void correctCircleOverlap(TouchableNumber num1, TouchableNumber num2)
    {
        //get midpoint for x and y directions
        float midpointx = (num1.x + num2.x) / 2;
        float midpointy = (num1.y + num2.y) / 2;

        //calculate distance between the circles using distance formula
        float dist = (float)Math.hypot(num1.x-num2.x, num1.y-num2.y);

        //since we only call this method AFTER a collision has been detected, this should never be
        //true. However, best to have this just in case so our game won't crash if trying to divide
        //by 0 at runtime
        if (dist == 0)
            return;

        //reset both bubbles' coordinates so they are no longer overlapping
        num1.x = midpointx + num1.radius * (num1.x - num2.x) / dist;
        num1.y = midpointy + num1.radius * (num1.y - num2.y) / dist;
        num2.x = midpointx + num2.radius * (num2.x - num1.x) / dist;
        num2.y = midpointy + num2.radius * (num2.y - num1.y) / dist;
    }

}
