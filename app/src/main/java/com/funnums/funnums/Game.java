package com.funnums.funnums;

/**
 * Created by austinbaird on 10/6/17.
 */



import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Iterator;




public class Game extends SurfaceView implements Runnable
{

    public State currentState;

    public String logTag = "Game"; //for debugging

    public final static int NANOS_TO_SECONDS = 1000000000; //conversion from nanosecs to seconds


    // Used to hold touch events so that drawing thread and onTouch thread don't result in concurrent access
    // not likely that these threads would interact, but if they do the game will crash!! which is why
    //we keep events in a separate list to be processed in the game loop
    private ArrayList<MotionEvent> events = new ArrayList<>();

    private boolean gameEnded;

    //for drawing
    private Context context;

    //dimensions of the sc
    private int screenX;
    private int screenY;

    //Optimal bubble radius
    private int bRadius;

    private int topBuffer = 200;

    //while playing is true, we keep updating game loop
    public boolean playing;

    //thread for the game
    Thread gameThread = null;


    //running time, used to generate new numbers every few seconds
    private long runningMilis = 0;


    private int maxNumsOnScreen = 6;




    //player's current sum
    private int sum;
    //target player is trying to sum to
    private int target;

    //list of all the touchable numbers on screen
    ArrayList<TouchableNumber> numberList = new ArrayList<>();

    // For drawing
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder ourHolder;

    //generates random numbers for us
    private Random r;

    //used to animate text, i.e show +3 when a 3 is touched
    ArrayList<TextAnimator> scoreAnimations = new ArrayList<>();

    private int maxVal = 4; //one less than the maximum value to appear on a bubble


    Game(Context context, int x, int y)
    {
        //set up view properly
        super(context);
        this.context  = context;

        //initalize random generator and make the first target between 5 and 8
        r = new Random();
        target = r.nextInt(3)+5;

        screenX = x;
        screenY = y;

        bRadius = (int) (screenX * .13);

        Log.d(VIEW_LOG_TAG, String.valueOf(x) + ", " + String.valueOf(y));

        // Initialize our drawing objects
        ourHolder = getHolder();
        paint = new Paint();

        startGame();
    }

    /*
    Used to round a number to 0 if it is less than the cutoff or to max if it is greater than the
    cutoff
     */
    private int bin(int cutoff, int max, int min, int num)
    {
        if (num > cutoff)
            return max;
        else
            return min;
    }

    /*
    Generates a touchable number on screen
     */
    private void generateNumber()
    {
        int x, y;
        int radius = 50;
        do
        {
            //random coordinates
            x = r.nextInt(screenX);
            y = r.nextInt(screenY - topBuffer - radius) + topBuffer + radius;

            //randomly decide if next number appears along top/bottom of screen or far left/right of screen
            if (r.nextBoolean())
                x = bin(screenX / 2, screenX, 0, x);
            else
                y = bin(screenY/2, screenY, topBuffer + radius, y);
        }
        while(findCollisions(x,y,0));
        //while this new coordinate causes collisions, keep generating a new coordinates until
        //it finds coordinates in a place without collisions

        //angle is direction number travels, max and min are the max and min angles for a number
        //determined by which quadrant the number spawns in. i.e if it spawns in bottom right corner,
        //we want it to travel up and to the left (min = 90 max = 180)
        int angle, max, min;
        //determine the quadrant the number will spawn in to plan the angle
        if (x >= screenX/2)
        {
            if(y >= screenY / 2) //lower right quadrant
            {
                max = 180;
                min = 91;
            }
            else //upper right quadrant
            {
                max = 270;
                min = 181;
            }
        }
        else
        {
            if(y >= screenY / 2) //lower left quadranr
            {
                max = 90;
                min = 1;
            }
            else //upper left qudrant
            {
                max = 360;
                min = 270;
            }
        }

        //make angles more diagonal
        max -= 25;
        min += 25;

        angle = r.nextInt(max - min) + min; //get random angle between max and min angles

        int value;
        int iterations = 0;
        do
        {
            value = r.nextInt(maxVal) + 1;
            iterations++;
        }while(valueAlreadyOnScreen(value) && iterations < maxVal * 2);
        //get a random number until we find one thats not already on the screen.
        //iterations < maxVal * 2 lets us break out of this loop if there are not enough unique numbers
        //left to generate a number that is not already on the screen.

        TouchableNumber num = new TouchableNumber(context, x, y, angle, value, bRadius);
        numberList.add(num);
    }

    private void startGame()
    {
        for(int i = 0; i < maxNumsOnScreen; i++)
            generateNumber();
        gameEnded = false;
    }

    @Override
    public void run()
    {

        //keep track of delta time, that is, how much time has passed in between each iteration of
        //the game loop
        long updateDurationMillis = 0;
        while(playing)
        {
            long beforeUpdateRender = System.nanoTime();

            //three main functions of game loop
            update(updateDurationMillis);
            draw();
            /*currentState.update(updateDurationMillis);
            currentState.draw();*/
            control();

            //update delta time
            updateDurationMillis = (System.nanoTime() - beforeUpdateRender);
            runningMilis += updateDurationMillis;
        }
    }





    private void update(long delta)
    {
        //detect and handle collisions
        findCollisions();

        //create a list that will hold numbers that have drifted offscreen so we can remove them
        //we can't remove them while iterating through numberList without a ConcurrentModificationError,
        //google "ConcurrentModificationError ArrayList" to get some helpful StackOverflow explanations
        ArrayList<TouchableNumber> toRemove = new ArrayList<>();
        for(TouchableNumber num : numberList)
        {
            //update the number
            num.update();

            //Check for numbers off screen and add them to list of numbers to remove
            /*if (num.getY() > screenY + num.getRadius() || num.getY() < 0 - num.getRadius()
                    || num.getX() > screenX + num.getRadius() || num.getX() < 0 - num.getRadius())
            {
                toRemove.add(num);
                Log.d(VIEW_LOG_TAG, "Remove Off screen!");
            }*/

            if((num.getX() > screenX - num.getRadius() && num.getXVelocity() > 0)
                    || (num.getX() < 0 && num.getXVelocity() < 0) )
            {
                num.setXVelocity(-num.getXVelocity());// num.setAngle(180 - num.angle)
                //num.fixAngle();
                Log.d(VIEW_LOG_TAG, String.valueOf(num.getX()) + ", " + String.valueOf(num.getY()) );
            }
            if ((num.getY() > screenY - num.getRadius() && num.getYVelocity() > 0)
                    || (num.getY() < topBuffer + num.getRadius() && num.getYVelocity() < 0))
            {
                num.setYVelocity(-num.getYVelocity()); //num.setAngle(num.angle - 180);
                //num.fixAngle();
                Log.d(VIEW_LOG_TAG, String.valueOf(num.getX()) + ", " + String.valueOf(num.getY()) );
            }

        }

        //remove offscreen numbers
        for(TouchableNumber offScreen : toRemove)
            numberList.remove(offScreen);

        //generate a new number every 1 second if there are less than the max amount of numbers on the screen
        if (runningMilis > 0.5 * NANOS_TO_SECONDS && numberList.size() < maxNumsOnScreen)
        {
            generateNumber();
            runningMilis = 0;
        }

        //process all touch events
        processEvents();

        //create a list that will hold textAnimations that have completed so we can remove them
        //we can't remove them while iterating through numberList without a ConcurrentModificationError,
        //google "ConcurrentModificationError ArrayList" to get some helpful StackOverflow explanations
        ArrayList<TextAnimator> scoresToRemove = new ArrayList<>();
        for(TextAnimator score : scoreAnimations)
        {
            score.update(delta);
            if (score.alpha <= 0)
                scoresToRemove.add(score);
        }
        for(TextAnimator faded : scoresToRemove)
            scoreAnimations.remove(faded);

    }

    private void draw()
    {

        if (ourHolder.getSurface().isValid())
        {
            //First we lock the area of memory we will be drawing to
            canvas = ourHolder.lockCanvas();

            // Rub out the last frame
            canvas.drawColor(Color.argb(255, 0, 0, 0));


            //draw all the numbers
            for(TouchableNumber num : numberList)
                num.draw(canvas, paint);
            //draw all text animations
            for(TextAnimator score : scoreAnimations)
                score.render(canvas, paint);


            if(!gameEnded)
            {
                // Draw the Current Sum and Target Score at top of screen
                int offset = 50;

                paint.setColor(Color.argb(255, 0, 0, 255));
                paint.setTextSize(45);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("Current", screenX * 1/4, topBuffer - offset, paint);
                canvas.drawText(String.valueOf(sum),  screenX * 1/4, topBuffer, paint);

                canvas.drawText("Target", screenX * 3/4, topBuffer - offset, paint);
                canvas.drawText(String.valueOf(target),  screenX * 3/4, topBuffer, paint);

                canvas.drawText("Pause", screenX * 1/2, offset, paint);
                //canvas.drawText(String.valueOf(target),  screenX * 3/4, 100, paint);

            }
            else
            {
                //TODO, what do we want to happen? we can end game when time runs out, but a running timer
                //is not implemented currently

                /*if(userScore < highScore) {
                    // Save high score
                    editor.putLong("highScore", userScore);
                    editor.commit();
                    highScore = userScore;
                }*/
            }

            // Unlock and draw the scene
            ourHolder.unlockCanvasAndPost(canvas);
        }


    }

    private void control() {
        try
        {
            //TODO don't hard code 17 in sleep, should be variable based on milis,
            //this acheives approximately 60FPS,
            // 17 milliseconds =  (1000(milliseconds)/60(FPS))
            gameThread.sleep(17);
        }
        catch (InterruptedException e)
        {
            Log.e(logTag, "Error causing thread to sleep\n" + e.getStackTrace());
        }
    }



    // Clean up our thread if the game is interrupted or the player quits
    public void pause()
    {
        playing = false;
        try
        {
            gameThread.join();
        }
        catch (InterruptedException e)
        {
            Log.e(logTag, "Error joining gameThread\n" + e.getStackTrace());
        }
    }

    // Make a new thread and start it
    public void resume()
    {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();

    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent)
    {
        //add touch event to eventsQueue rather than processing it immediately. This is because
        //onTouchEvent is run in a separate thread by Android and if we touch and delete a number
        //in this touch UI thread while our game thread is accessing that same number, the game crashes
        //because two threads are accessing same memory being removed. We could do mutex but this setup
        //is pretty standard I believe.
        events.add(motionEvent);
        return true;
    }

    /*
    Process the touch events
     */
    private void processEvents()
    {
        for(MotionEvent e : events)
            checkTouchRadius((int) e.getX(), (int) e.getY());
        events.clear();
    }

    /*
    Check if where the player touched the screen is on a touchable number and, if it is, call
    processScore() to update the number/score/etc
     */
    private void checkTouchRadius(int x, int y)
    {
        for(TouchableNumber num : numberList)
        {
            //Trig! (x,y) is in a circle if (x - center_x)^2 + (y - center_y)^2 < radius^2
            if(Math.pow(x - num.getX(), 2) + Math.pow(y - num.getY(), 2) < Math.pow(num.getRadius(), 2))
            {
                Log.d(VIEW_LOG_TAG, "Circle touched!");
                processScore(num);
                numberList.remove(num);
                break;
                //break after removing to avoid concurrent memory modification error, shouldn't be possible to touch two at once anyway
                //we could have a list of numbers to remove like in the update() function, but let's keep it simple for now
            }
        }

    }

    /*
        When a number is touched, call this function. It will update the current Sum and check it
        player has reached the target, in which case we make a new target. Else, if the target is
        exceeded, for now we tell the player they exceeded the target and reset the game
     */
    private void processScore(TouchableNumber num)
    {

        sum += num.getValue();
        TextAnimator textAnimator = new TextAnimator("+" + String.valueOf(num.getValue()), num.getX(), num.getY(), 0, 255, 0);
        scoreAnimations.add(textAnimator);
        if(sum == target)
            makeNewTarget();
        else if(sum > target)
        {
            resetGame();
        }
    }

    /*
        Create a new target
     */
    private void makeNewTarget()
    {
        //text, x, y, r, g, b, interval, size
        TextAnimator textAnimator = new TextAnimator("New Target!", screenX/2, screenY/2, 44, 185, 185, 1.25, 50);
        scoreAnimations.add(textAnimator);

        target += r.nextInt(3)+5;
    }

    /*
        For now, tell player they missed the target and reset the target and current sum
     */
    private void resetGame()
    {
        //text, x, y, r, g, b, interval, size
        TextAnimator textAnimator = new TextAnimator("Target Missed\nResetting...!", screenX/2, screenY/2, 185, 44, 44, 1.25, 50);
        scoreAnimations.add(textAnimator);

        target = r.nextInt(3)+5;
        sum = 0;

        //if we want game to stop, make playing false here
        //   playing = false;
    }

    /*
        Detect collisions for all our numbers on screen and bouce numbers that have collided
     */
    private void findCollisions()
    {
        //this double for loop set up is so we don't check 0 1 and then 1 0 later, since they would have the same result
        //a bit of a micro optimization, but can be useful if there are a lot of numbers on screen
        for(int i = 0; i < numberList.size(); i++)
            for(int j = i+1; j < numberList.size(); j++)
                if(CollisionDetector.isCollision(numberList.get(i), numberList.get(j)))
                {
                    numberList.get(i).bounceWith(numberList.get(j));
                }

    }


    /*
        Return true if a given coordinate will cause a collision with numbers on screen, false otherwise
     */
    private boolean findCollisions(int x, int y, int radius)
    {
        //this double for loop set up is so we don't check 0 1 and then 1 0 later, since they would have the same result
        //a bit of a micro optimization, but can be useful if there are a lot of numbers on screen
        TouchableNumber num = new TouchableNumber(context, x, y, 0, 0, bRadius);
        num.setRadius(num.getRadius() + 25);
        for(int i = 0; i < numberList.size(); i++)
            if(CollisionDetector.isCollision(numberList.get(i), num))
                return true;

        return false;
    }

    private boolean valueAlreadyOnScreen(int value)
    {
        for(TouchableNumber num : numberList)
        {
            if(num.getValue() == value)
                return true;
        }
        return false;
    }
}

