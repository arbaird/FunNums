package com.funnums.funnums.minigames;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import java.util.ArrayList;
import java.util.Random;
import android.graphics.Bitmap;

import com.funnums.funnums.classes.CollisionDetector;
import com.funnums.funnums.classes.TouchableBalloon;
import com.funnums.funnums.classes.GameCountdownTimer;
import com.funnums.funnums.classes.FractionNumberGenerator;
import com.funnums.funnums.classes.Fraction;
import com.funnums.funnums.uihelpers.TextAnimator;
import com.funnums.funnums.uihelpers.UIButton;


public class BalloonGame extends MiniGame {
    public String VIEW_LOG_TAG = "Game"; //for debugging

    public final static int NANOS_TO_SECONDS = 1000000000; //conversion from nanosecs to seconds


    // Used to hold touch events so that drawing thread and onTouch thread don't result in concurrent access
    // not likely that these threads would interact, but if they do the game will crash!! which is why
    //we keep events in a separate list to be processed in the game loop
    private ArrayList<MotionEvent> events = new ArrayList<>();

    //dimensions of the sc
    private int screenX;
    private int screenY;

    //TODO make this vary based on phone size
    //this is the amount of space at the top of the screen used for the current sum, target, timer, and pause button
    private int topBuffer = 200;

    //running time, used to generate new numbers every few seconds
    private long runningMilis = 0;


    private int maxNumsOnScreen = 10;

    //player's current sum
    private int sum;
    //target player is trying to sum to
    private int target;

    //speed of the balloons
    private int speed=10;


    //list of all the touchable numbers on screen
    ArrayList<TouchableBalloon> numberList = new ArrayList<>();

    // For drawing
    //private Paint paint;
    //private Canvas canvas;
    //private SurfaceHolder ourHolder;

    //generates random numbers for us
    private Random r;

    //generates random fractions for us
    private FractionNumberGenerator rFrac= new FractionNumberGenerator(0);

    //used to animate text, i.e show +3 when a 3 is touched
    ArrayList<TextAnimator> scoreAnimations = new ArrayList<>();

    private int maxVal = 4; //one less than the maximum value to appear on a bubble

    //Optimal bubble radius
    private int bRadius;

    //Timer object
    private GameCountdownTimer gameTimer;

    public void init() {

        //initalize random generator and make the first target between 5 and 8
        r = new Random();
        target = r.nextInt(3)+5;

        screenX = com.funnums.funnums.maingame.GameActivity.screenX;
        screenY = com.funnums.funnums.maingame.GameActivity.screenY;

        bRadius = (int) (screenX * .1);

        for(int i = 0; i < maxNumsOnScreen; i++)
            generateNumber();

        //Initialize timer to 61 seconds, update after 1 sec interval
        gameTimer = new GameCountdownTimer(61000, 1000);
        gameTimer.start();

        //set up the pause button
        int offset = 100;
        Bitmap pauseImgDown = com.funnums.funnums.maingame.GameActivity.gameView.loadBitmap("pause_down.png", true);
        Bitmap pauseImg = com.funnums.funnums.maingame.GameActivity.gameView.loadBitmap("pause.png", true);
        pauseButton = new UIButton(screenX *3/4, 0, screenX, offset, pauseImg, pauseImgDown);
    }



    public void update(long delta){
        if(isPaused)
            return;

        //detect and handle collisions
        findCollisions();

        for(TouchableBalloon num : numberList) {
            //update the number
            num.update();


            if((num.getX() > screenX - num.getRadius() && num.getXVelocity() > 0)
                    || (num.getX() < 0 && num.getXVelocity() < 0) )
                num.setXVelocity(-num.getXVelocity()); //bounced off vertical edge
        }

        runningMilis += delta;
        //generate a new number every 1/2 second if there are less than the max amount of numbers on the screen
        if (runningMilis > 0.5 * NANOS_TO_SECONDS && numberList.size() < maxNumsOnScreen) {
            generateNumber();
            runningMilis = 0;
        }

        //Remove and checks the balloons when they left the screen
        offScreenCheck();

        //process all touch events
        processEvents();

        //create a list that will hold textAnimations that have completed so we can remove them
        //we can't remove them while iterating through numberList without a ConcurrentModificationError,
        //google "ConcurrentModificationError ArrayList" to get some helpful StackOverflow explanations
        ArrayList<TextAnimator> scoresToRemove = new ArrayList<>();
        for(TextAnimator score : scoreAnimations) {
            score.update(delta);
            if (score.alpha <= 0)
                scoresToRemove.add(score);
        }

        for(TextAnimator faded : scoresToRemove)
            scoreAnimations.remove(faded);

    }



    /*
    Generates a touchable number on screen
     */
    private void generateNumber() {
        int x, y;
        do {
            //Setting coordinates x and y
            x = r.nextInt(screenX);
            y = screenY+r.nextInt(3*screenY/4);
        }
        while(findCollisions(x,y));
        //while this new coordinate causes collisions, keep generating a new coordinates until
        //it finds coordinates in a place without collisions

        //angle is direction number travels, max and min are the max and min angles for a number
        //determined by which quadrant the number spawns in. i.e if it spawns in bottom right corner,
        //we want it to travel up and to the left (min = 90 max = 180)
        int angle, max, min;
        //determine the quadrant the number will spawn in to plan the angle
        if (x >= screenX/2) {
            max = 120;
            min = 91;
        }
        else {
            max = 90;
            min = 60;
        }

        angle = r.nextInt(max - min) + min; //get random angle between max and min angles

        Fraction value = rFrac.getNewBalloon();

        TouchableBalloon num = new TouchableBalloon(x, y, angle, bRadius,speed, value);
        numberList.add(num);
    }

    /*
    Process the touch events
     */
    private void processEvents() {
        for(MotionEvent e : events)
        {
            int x = (int) e.getX();
            int y = (int) e.getY();

            checkTouchRadius(x, y);
        }
        events.clear();
    }

    private boolean valueAlreadyOnScreen(int value) {
        for(TouchableBalloon  num : numberList)
        {
            if(num.getValue() == value)
                return true;
        }
        return false;
    }

    /*
   Check if where the player touched the screen is on a touchable number and, if it is, call
   processScore() to update the number/score/etc
    */
    private void checkTouchRadius(int x, int y) {
        for(TouchableBalloon num : numberList) {
            //Trig! (x,y) is in a circle if (x - center_x)^2 + (y - center_y)^2 < radius^2
            if(Math.pow(x - num.getX(), 2) + Math.pow(y - num.getY(), 2) < Math.pow(num.getRadius(), 2)) {
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
    private void processScore(TouchableBalloon num) {

        sum += num.getValue();
        TextAnimator textAnimator = new TextAnimator("+" + String.valueOf(num.getValue()), num.getX(), num.getY(), 0, 255, 0);
        scoreAnimations.add(textAnimator);
        if(sum == target)
            makeNewTarget();
        else if(sum > target) {
            resetGame();
        }
    }

    /*
       Create a new target
    */
    private void makeNewTarget() {
        //text, x, y, r, g, b, interval, size
        TextAnimator textAnimator = new TextAnimator("New Target!", screenX/2, screenY/2, 44, 185, 185, 1.25, 50);
        scoreAnimations.add(textAnimator);

        target += r.nextInt(3)+5;
    }

    /*
        For now, tell player they missed the target and reset the target and current sum
     */
    private void resetGame() {
        //text, x, y, r, g, b, interval, size
        TextAnimator textAnimator = new TextAnimator("Target Missed\nResetting...!", screenX/2, screenY/2, 185, 44, 44, 1.25, 50);
        scoreAnimations.add(textAnimator);

        target = r.nextInt(3)+5;
        sum = 0;

        //if we want game to stop, make playing false here
        //   playing = false;
    }

    /*
    Used to round a number to min if it is less than the cutoff or to max if it is greater than the
    cutoff
     */
    private int bin(int cutoff, int max, int min, int num) {
        if (num > cutoff)
            return max;
        else
            return min;
    }

    //Checks if y coordinate of ballons is greater than -diameter of the ballons. If yes, process/remve balloon.
    private void offScreenCheck() {
        for(TouchableBalloon num : numberList) {
            if(num.getY()<topBuffer-bRadius) {
                processScore(num);
                numberList.remove(num);
                break;
                //break after removing to avoid concurrent memory modification error, shouldn't be possible to touch two at once anyway
                //we could have a list of numbers to remove like in the update() function, but let's keep it simple for now
            }
        }
    }

    /*
        Detect collisions for all our numbers on screen and bouce numbers that have collided
     */
    private void findCollisions() {
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
        Overloaded to take an x and y coordinate as arguments.
        Return true if a given coordinate will cause a collision with numbers on screen, false otherwise
     */
    private boolean findCollisions(int x, int y) {
        //this double for loop set up is so we don't check 0 1 and then 1 0 later, since they would have the same result
        //a bit of a micro optimization, but can be useful if there are a lot of numbers on screen

        //allow a little extra space for new appearing numbers
        int buffer = bRadius / 2;
        for(int i = 0; i < numberList.size(); i++)
            if(CollisionDetector.isCollision(numberList.get(i), x, y, bRadius + buffer))
                return true;

        return false;
    }

    public void draw(SurfaceHolder ourHolder, Canvas canvas, Paint paint) {

        if (ourHolder.getSurface().isValid()) {
            //First we lock the area of memory we will be drawing to
            canvas = ourHolder.lockCanvas();

            // Rub out the last frame
            canvas.drawColor(Color.argb(255, 0, 0, 0));

            //draw all the numbers
            for(TouchableBalloon num : numberList)
                num.draw(canvas, paint);
            //draw all text animations
            for(TextAnimator score : scoreAnimations)
                score.render(canvas, paint);

            // Draw the Current Sum and Target Score at top of screen
            int offset = 50;

            //Draw Current
            paint.setColor(Color.argb(255, 0, 0, 255));
            paint.setTextSize(45);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Current", screenX * 1/4, topBuffer - offset, paint);
            canvas.drawText(String.valueOf(sum),  screenX * 1/4, topBuffer, paint);
            //Draw Target
            canvas.drawText("Target", screenX * 3/4, topBuffer - offset, paint);
            canvas.drawText(String.valueOf(target),  screenX * 3/4, topBuffer, paint);
            //draw timer
            canvas.drawText("Timer", screenX * 1/2, offset, paint);
            canvas.drawText(String.valueOf(gameTimer.toString()),  screenX *  1/2, offset*2, paint);
            //Draw pause button
            pauseButton.render(canvas, paint);
            //draw pause menu, if paused
            if(isPaused)
                com.funnums.funnums.maingame.GameActivity.gameView.pauseScreen.draw(canvas, paint);

            ourHolder.unlockCanvasAndPost(canvas);
        }


    }


    public boolean onTouch(MotionEvent e) {
        //add touch event to eventsQueue rather than processing it immediately. This is because
        //onTouchEvent is run in a separate thread by Android and if we touch and delete a number
        //in this touch UI thread while our game thread is accessing that same number, the game crashes
        //because two threads are accessing same memory being removed. We could do mutex but this setup
        //is pretty standard I believe.

        events.add(e);
        return true;
    }


}
