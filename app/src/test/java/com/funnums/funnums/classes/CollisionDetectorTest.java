package com.funnums.funnums.classes;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test the functionality of collision detector
 */
public class CollisionDetectorTest {
    @Test
    public void isCollisionWithOneBubble() throws Exception {
        int x = 5;
        int y = 5;
        int radius = 50;

        TouchableBubble num = new TouchableBubble(x, y, radius);
        //two bubbles overlap, should be collision
        assertEquals(true, CollisionDetector.isCollision(num, 10, 10, 50));
    }

    @Test
    public void isCollisionWithTwoBubbles() throws Exception {
        int x = 5;
        int y = 5;
        int radius = 50;
        TouchableBubble num = new TouchableBubble(x, y, radius);

        int x2 = 10;
        int y2 = 10;
        int radius2 = 50;
        TouchableBubble num2 = new TouchableBubble(x2, y2, radius2);

        //two bubbles overlap, should be collision
        assertEquals(true, CollisionDetector.isCollision(num, num2));
    }

    @Test
    public void isNotCollisionWithOneBubble() throws Exception {
        int x = 5;
        int y = 5;
        int radius = 50;

        TouchableBubble num = new TouchableBubble(x, y, radius);
        //two bubbles  do not overlap, should not be collision
        assertNotEquals(true, CollisionDetector.isCollision(num, 100, 100, 50));
    }

    @Test
    public void isNotCollisionWithTwoBubbles() throws Exception {
        int x = 5;
        int y = 5;
        int radius = 50;
        TouchableBubble num = new TouchableBubble(x, y, radius);

        int x2 = 100;
        int y2 = 100;
        int radius2 = 50;
        //two bubbles  do not overlap, should not be collision
        TouchableBubble num2 = new TouchableBubble(x2, y2, radius2);

        assertNotEquals(true, CollisionDetector.isCollision(num, num2));
    }

}