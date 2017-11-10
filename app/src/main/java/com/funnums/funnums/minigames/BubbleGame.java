package com.funnums.funnums.minigames;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import java.util.ArrayList;
import java.util.Random;
import android.graphics.Bitmap;

import com.funnums.funnums.classes.ExpressionEvaluator;
import com.funnums.funnums.classes.BubbleTargetGenerator;
import com.funnums.funnums.classes.BubbleNumberGenerator;
import com.funnums.funnums.classes.CollisionDetector;
import com.funnums.funnums.classes.TouchableNumber;
import com.funnums.funnums.classes.TouchableBubble;
import com.funnums.funnums.classes.GameCountdownTimer;
import com.funnums.funnums.uihelpers.TextAnimator;
import com.funnums.funnums.uihelpers.UIButton;
import com.funnums.funnums.uihelpers.GameFinishedMenu;


public class BubbleGame extends MiniGame {

    public String TAG = "Game"; //for debugging

    public final static int NANOS_TO_SECONDS = 1000000000; //conversion from nanosecs to seconds


    // Used to hold touch events so that drawing thread and onTouch thread don't result in concurrent access
    // not likely that these threads would interact, but if they do the game will crash!! which is why
    //we keep events in a separate list to be processed in the game loop
    private ArrayList<MotionEvent> events = new ArrayList<>();

    //dimensions of the sc
    private int screenX;
    private int screenY;

    //TODO make this vary based on phone size
    //this is the amount of space at the top of the sceen used for the current sum, target, timer, and pause button
    private int topBuffer = 200;

    //running time, used to generate new numbers every few seconds
    private long runningMilis = 0;


    private int maxNumsOnScreen = 7;

    //player's current sum
    private int sum;
    //target player is trying to sum to
    private int target;
    private int previousTarget = 0;
    //The target generator
    BubbleTargetGenerator targetGen = new BubbleTargetGenerator();
    //The number generator
    BubbleNumberGenerator numGen = new BubbleNumberGenerator();

    //speed of the bubbles
    private int speed=5;

    //list of all the touchable numbers on screen
    ArrayList<TouchableBubble> numberList = new ArrayList<>();

    // For drawing
    //private Paint paint;
    //private Canvas canvas;
    //private SurfaceHolder ourHolder;

    //generates random numbers for us
    private Random r;

    //used to animate text, i.e show +3 when a 3 is touched
    ArrayList<TextAnimator> scoreAnimations = new ArrayList<>();

    private int maxVal = 4; //one less than the maximum value to appear on a bubble

    //Optimal bubble radius
    private int bRadius;


    //game over menu
    private GameFinishedMenu gameFinishedMenu;

    public void init() {

        //game only finished when timer is done
        isFinished = false;

        //initalize random generator
        r = new Random();
        //get a target from the target generator
        target = targetGen.nextTarget();

        screenX = com.funnums.funnums.maingame.GameActivity.screenX;
        screenY = com.funnums.funnums.maingame.GameActivity.screenY;

        bRadius = (int) (screenX * .13);

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


        Log.d(TAG, "init pauseButton: " + pauseButton);
        /**!!This will be removed is just a test*/
        /*Log.d("Fraction", "Test LT or GT");
        FractionNumberGenerator lol = new FractionNumberGenerator(0);
        lol.runTest();
        /*!!*/
        /*Log.d("Fraction", "Test LEQ or GEQ ");
        lol.new_game(1);
        lol.runTest();
        /*!!*/
        /*Log.d("Fraction", "Test EQ");
        lol.new_game(2);
        lol.runTest();*/
        /****************************************************/

        Bitmap resumeDown = com.funnums.funnums.maingame.GameView.loadBitmap("button_resume_down.png", true);
        Bitmap resume = com.funnums.funnums.maingame.GameView.loadBitmap("button_resume.png", true);
        UIButton resumeButton = new UIButton(0,0,0,0, resume, resumeDown);

        Bitmap menuDown = com.funnums.funnums.maingame.GameView.loadBitmap("button_quit_down.png", true);
        Bitmap menu = com.funnums.funnums.maingame.GameView.loadBitmap("button_quit.png", true);
        UIButton menuButton = new UIButton(0,0,0,0, menu, menuDown);

        gameFinishedMenu = new GameFinishedMenu(screenX * 1/8,
                offset,
                screenX * 7/8,
                screenY - offset,
                resumeButton,
                menuButton, sum);
    }



    public void update(long delta) {
        if(isPaused)
            return;

        //detect and handle collisions
        findCollisions();

        for(TouchableNumber num : numberList) {
            //update the number
            num.update();


            if((num.getX() > screenX - num.getRadius() && num.getXVelocity() > 0)
                    || (num.getX() < 0 && num.getXVelocity() < 0) )
                num.setXVelocity(-num.getXVelocity()); //bounced off vertical edge

            if ((num.getY() > screenY - num.getRadius() && num.getYVelocity() > 0)
                    || (num.getY() < topBuffer + num.getRadius() && num.getYVelocity() < 0))
                num.setYVelocity(-num.getYVelocity()); //bounce off horizontal edge

        }

        runningMilis += delta;
        //generate a new number every 1/2 second if there are less than the max amount of numbers on the screen
        if (runningMilis > 0.5 * NANOS_TO_SECONDS && numberList.size() < maxNumsOnScreen) {
            generateNumber();
            runningMilis = 0;

        }

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
        for(TextAnimator faded : scoresToRemove) {
            scoreAnimations.remove(faded);
        }
    }



    /*
    Generates a touchable number on screen
     */
    private void generateNumber() {
        int x, y;
        int radius = bRadius;
        do {
            //random coordinates
            x = r.nextInt(screenX);
            y = r.nextInt(screenY - topBuffer - radius) + topBuffer + radius;

            //randomly decide if next number appears along top/bottom of screen or far left/right of screen
            if (r.nextBoolean())
                x = bin(screenX / 2, screenX, 0, x);
            else
                y = bin(screenY/2, screenY, topBuffer + radius, y);

        } while(findCollisions(x,y));
        //while this new coordinate causes collisions, keep generating a new coordinates until
        //it finds coordinates in a place without collisions

        //angle is direction number travels, max and min are the max and min angles for a number
        //determined by which quadrant the number spawns in. i.e if it spawns in bottom right corner,
        //we want it to travel up and to the left (min = 90 max = 180)
        int angle, max, min;
        //determine the quadrant the number will spawn in to plan the angle
        if (x >= screenX/2) {
            if (y >= screenY / 2) {
                //lower right quadrant
                max = 180;
                min = 91;
            }
            else {
                //upper right quadrant
                max = 270;
                min = 181;
            }
        }
        else {
            if (y >= screenY / 2) {
                //lower left quadrant
                max = 90;
                min = 1;
            }
            else {
                //upper left quadrant
                max = 360;
                min = 270;
            }
        }

        //make angles more diagonal
        max -= 25;
        min += 25;

        angle = r.nextInt(max - min) + min; //get random angle between max and min angles

        int newNumber = numGen.nextNum(); // get generated number from our num gen
        TouchableBubble num = new TouchableBubble(x, y, angle, bRadius, speed, newNumber);
        numberList.add(num);

        // Todo remove this after done testing 
        ExpressionEvaluator solutionTest = new ExpressionEvaluator();
        Log.d(TAG, Integer.toString(solutionTest.evalExpr("1 + 4 * 3 / 2 - 3")));
    }

    /*
    Process the touch events
     */
    private void processEvents() {

        for(MotionEvent e : events) {
            int x = (int) e.getX();
            int y = (int) e.getY();

            checkTouchRadius(x, y);
        }
        events.clear();
    }

    private boolean valueAlreadyOnScreen(int value) {

        for(TouchableBubble num : numberList) {
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

        for(TouchableBubble num : numberList) {
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

       Also if the target is reached add 5 seconds or if the target is exceeded take away 5 seconds
    */
    private void processScore(TouchableBubble num) {

        sum += num.getValue();
        score = sum;
        TextAnimator textAnimator = new TextAnimator("+" + String.valueOf(num.getValue()), num.getX(), num.getY(), 0, 255, 0);
        scoreAnimations.add(textAnimator);
        if (sum == target) {
            makeNewTarget();
            long newTime = 1000;
            com.funnums.funnums.maingame.GameActivity.gameView.updateGameTimer(newTime);

        } else if (sum > target) {
            resetGame();

            long newTime = -1000;
            com.funnums.funnums.maingame.GameActivity.gameView.updateGameTimer(newTime);
        }
    }

    /*
       Create a new target
    */
    private void makeNewTarget() {
        //text, x, y, r, g, b, interval, size
        TextAnimator textAnimator = new TextAnimator("New Target!", screenX/2, screenY/2, 44, 185, 185, 1.25, 50);
        scoreAnimations.add(textAnimator);

        previousTarget = target;
        target = targetGen.nextTarget();
        numGen.setAbsoluteTarget(target - previousTarget); //used for scaling the numbers generated
    }

    /*
        For now, tell player they missed the target and reset the target and current sum
     */
    private void resetGame() {
        //text, x, y, r, g, b, interval, size
        TextAnimator message1 = new TextAnimator("Target missed!", screenX/2, screenY/2, 185, 44, 44, 1.25, 60);
        TextAnimator message2 = new TextAnimator("Current reset", screenX/2, screenY/2 + 60, 185, 44, 44, 1.25, 50);
        scoreAnimations.add(message1);
        scoreAnimations.add(message2);

        /*target = r.nextInt(3)+5;
        sum = 0;
        score = 0;*/

        sum = previousTarget; //reset the current sum to the previous target


        //if we want game to stop, make playing false here
        //   playing = false;
    }

    /*
    Used to round a number to 0 if it is less than the cutoff or to max if it is greater than the
    cutoff
     */
    private int bin(int cutoff, int max, int min, int num) {
        if (num > cutoff)
            return max;
        else
            return min;
    }


    /*
        Detect collisions for all our numbers on screen and bouce numbers that have collided
     */
    private void findCollisions() {
        //this double for loop set up is so we don't check 0 1 and then 1 0 later, since they would have the same result
        //a bit of a micro optimization, but can be useful if there are a lot of numbers on screen
        for(int i = 0; i < numberList.size(); i++)
            for(int j = i+1; j < numberList.size(); j++)
                if(CollisionDetector.isCollision(numberList.get(i), numberList.get(j))) {
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
            for(TouchableNumber num : numberList)
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
            if(pauseButton != null)
                pauseButton.render(canvas, paint);

            //draw pause menu, if paused
            if(isPaused)
                com.funnums.funnums.maingame.GameActivity.gameView.pauseScreen.draw(canvas, paint);
            //game finished stuff
            if(isFinished)
                com.funnums.funnums.maingame.GameActivity.gameView.gameFinishedMenu.draw(canvas, paint);

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
