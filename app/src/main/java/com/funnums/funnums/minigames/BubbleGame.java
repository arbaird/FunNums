package com.funnums.funnums.minigames;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;

import android.graphics.Rect;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Random;
import android.graphics.Bitmap;

import com.funnums.funnums.maingame.GameActivity;
import com.funnums.funnums.uihelpers.HUDSquare;
import com.funnums.funnums.uihelpers.HUDSquareNoLabel;

import com.funnums.funnums.R;
import com.funnums.funnums.classes.ExpressionEvaluator;

import com.funnums.funnums.classes.BubbleTargetGenerator;
import com.funnums.funnums.classes.BubbleNumberGenerator;
import com.funnums.funnums.classes.CollisionDetector;
import com.funnums.funnums.classes.TouchableNumber;
import com.funnums.funnums.classes.TouchableBubble;

import com.funnums.funnums.animation.*;

import com.funnums.funnums.classes.GameCountdownTimer;
import com.funnums.funnums.maingame.GameActivity;
import com.funnums.funnums.maingame.MainMenuActivity;

import com.funnums.funnums.uihelpers.PauseMenu;
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
    private int target = 0;
    private int previousTarget = 0;
    //The target generator
    BubbleTargetGenerator targetGen = new BubbleTargetGenerator();
    //The number generator
    BubbleNumberGenerator numGen = new BubbleNumberGenerator();

    //speed of the bubbles
    private int speed=5;

    //list of all the touchable numbers on screen
    ArrayList<TouchableBubble> numberList;

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

    //used to implement sound
    private int bubblePopId;
    private int correctId;
    private int splashId;
    private int wrongId;


    //game over menu
    private GameFinishedMenu gameFinishedMenu;

    private Bitmap HUDBoard;
    private Bitmap bg;


    HUDSquare curHUD;
    HUDSquare targetHUD;
    HUDSquare timerHUD;


    public synchronized void init() {

        numberList = new ArrayList<>();

        //game only finished when timer is done
        isFinished = false;

        //initializes soundPool
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
        bubblePopId = soundPool.load(context,R.raw.bubble,1);
        correctId = soundPool.load(context,R.raw.correct,1);
        splashId = soundPool.load(context,R.raw.splash,1);
        wrongId = soundPool.load(context,R.raw.wrong,1);
        gameOverSoundId=soundPool.load(context,R.raw.timesup,1);



        //initalize random generator
        r = new Random();
        //get a target from the target generator
        target = targetGen.nextTarget();
        numGen.setAbsoluteTarget(target, previousTarget);

        screenX = com.funnums.funnums.maingame.GameActivity.screenX;
        screenY = com.funnums.funnums.maingame.GameActivity.screenY;

        //found factor (0.00694) that scales the speed appropriately across different phones
        speed = (int)Math.round(screenX  * 0.00694) - 1;


        bRadius = (int) (screenX * .13);

        //generated nbubbles to fill the screen on start
        for(int i = 0; i < maxNumsOnScreen; i++)
            generateNumber();

        //Initialize timer to 60 seconds, update after 1 sec interval
        initTimer(60000);

        //set the backdrop for the menu and pause screen
        bg = com.funnums.funnums.maingame.GameView.loadBitmap("BubbleGame/Bubblescape test 1mdpi.png", false);
        bg = Bitmap.createScaledBitmap(bg, screenX, screenY - 0/*topBuffer*/,false);
        com.funnums.funnums.maingame.GameActivity.gameView.setMenuBackdrop("BubbleGame/BubbleMenuBoard.png");

        initHud();
    }


    /*
        update the game logic
     */
    public synchronized void update(long delta) {
        //detect and handle collisions
        findCollisions();
        //ist of bubbles to remove, can't remove inside for loop without causing Concurrent Memory Modification Error
        ArrayList<TouchableBubble> toRemove = new ArrayList<>();

        for(TouchableBubble num : numberList) {
            //update the number
            num.update(delta);
            //add numers to remove
            if (isPopped(num))
                toRemove.add(num);
            //check if bubble is drifting off the screen
            if((num.getX() > screenX - num.getRadius() && num.getXVelocity() > 0)
                    || (num.getX() < 0 && num.getXVelocity() < 0) )
                num.setXVelocity(-num.getXVelocity()); //bounced off vertical edge

            if ((num.getY() > screenY - num.getRadius() && num.getYVelocity() > 0)
                    || (num.getY() < topBuffer + num.getRadius() && num.getYVelocity() < 0))
                num.setYVelocity(-num.getYVelocity()); //bounce off horizontal edge

        }
        //remove pooped bubbles
        for(TouchableBubble popped : toRemove) {
            numberList.remove(popped);
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
    private synchronized void generateNumber() {
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
        numGen.increment(newNumber);      // maintain the count of each number on the screen
        TouchableBubble num = new TouchableBubble(x, y, angle, bRadius, speed, newNumber);
        numberList.add(num);
    }

    /*
        Process the touch events
     */
    private synchronized void processEvents() {
        try {
            for (MotionEvent e : events) {
                if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    int x = (int) e.getX();
                    int y = (int) e.getY();

                    if (checkTouchRadius(x, y)) {
                        break;
                    }
                }
            }
        }
        //don't let multiple threads working on touch events crash the app
        catch(ConcurrentModificationException ex){
            Log.e("ERROR", ex.toString());
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
    private synchronized boolean checkTouchRadius(int x, int y) {

        for(TouchableBubble num : numberList) {
            //Trig! (x,y) is in a circle if (x - center_x)^2 + (y - center_y)^2 < radius^2
            if(Math.pow(x - num.getX(), 2) + Math.pow(y - num.getY(), 2) < Math.pow(num.getRadius(), 2) && !num.popping) {
                processScore(num);

                soundPool.play(bubblePopId,volume,volume,1,0,1);
                numberList.remove(num);

                numGen.decrement(num.getValue()); //maintain the count of the number on the screen
                num.pop();

                return true;

                //break after removing to avoid concurrent memory modification error, shouldn't be possible to touch two at once anyway
                //we could have a list of numbers to remove like in the update() function, but let's keep it simple for now
            }else{
                soundPool.play(splashId,volume,volume,0,0,1);
            }
        }
        return false;
    }

    /*
       When a number is touched, call this function. It will update the current Sum and check it
       player has reached the target, in which case we make a new target. Else, if the target is
       exceeded, for now we tell the player they exceeded the target and reset the game

       Also if the target is reached add 1 second
    */
    private synchronized void processScore(TouchableBubble num) {

        sum += num.getValue();
        score = sum;
        TextAnimator textAnimator = new TextAnimator("+" + String.valueOf(num.getValue()), screenX * 1/4, topBuffer, 0, 255, 0);
        scoreAnimations.add(textAnimator);

        if (sum == target) {
            soundPool.play(correctId,volume,volume,2,0,1);
            makeNewTarget();
            long newTime = 1000;
            com.funnums.funnums.maingame.GameActivity.gameView.updateGameTimer(newTime);

        } else if (sum > target) {
            soundPool.play(wrongId,volume,volume,2,0,1);
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
        TextAnimator textAnimator = new TextAnimator("New Target!", screenX/2, screenY/2, 44, 220, 185, 1.25, 50);
        scoreAnimations.add(textAnimator);
        TextAnimator addTimetextAnimator = new TextAnimator("+1", screenX * 1/2, timerHUD.bottom-timerHUD.MARGIN, 0, 255, 0);
        scoreAnimations.add(addTimetextAnimator);

        previousTarget = target;
        target = targetGen.nextTarget();
        numGen.setAbsoluteTarget(target , previousTarget); //used for scaling the numbers generated
    }

    /*
        Tell player they missed the target and reset the target and current sum
     */
    private void resetGame() {
        //text, x, y, r, g, b, interval, size
        TextAnimator message1 = new TextAnimator("Target missed!", screenX/2, screenY/2, 185, 44, 44, 1.25, 60);
        TextAnimator message2 = new TextAnimator("Current reset", screenX/2, screenY/2 + 60, 185, 44, 44, 1.25, 50);
        scoreAnimations.add(message1);
        scoreAnimations.add(message2);

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
    private synchronized void findCollisions() {
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
    private synchronized boolean findCollisions(int x, int y) {
        //this double for loop set up is so we don't check 0 1 and then 1 0 later, since they would have the same result
        //a bit of a micro optimization, but can be useful if there are a lot of numbers on screen

        //allow a little extra space for new appearing numbers
        int buffer = bRadius / 2;
        for(int i = 0; i < numberList.size(); i++)
            if(CollisionDetector.isCollision(numberList.get(i), x, y, bRadius + buffer))
                return true;

        return false;
    }

    public synchronized void draw(SurfaceHolder ourHolder, Canvas canvas, Paint paint) {

        if (ourHolder.getSurface().isValid()) {
            //First we lock the area of memory we will be drawing to
            canvas = ourHolder.lockCanvas();

            // Rub out the last frame
            //canvas.drawColor(Color.argb(255, 70, 103, 234));
            paint.setColor(Color.argb(255, 70, 103, 234));
            //canvas.drawRect(0, topBuffer, screenX, screenY, paint);

            canvas.drawBitmap(bg, 0, 0, paint);

            canvas.drawBitmap(HUDBoard, 0 , 0 , paint);
            // border
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(1);
            canvas.drawRect(0, 0, screenX, topBuffer, paint);

            paint.setStyle(Paint.Style.FILL);



            //draw all the numbers
            for(TouchableNumber num : numberList)
                num.draw(canvas, paint);

            //draw HUD Squares
            curHUD.draw(canvas, paint, String.valueOf(sum));
            targetHUD.draw(canvas, paint, String.valueOf(target));
            timerHUD.draw(canvas, paint, gameTimer.toString());

            //draw all text animations
            for(TextAnimator score : scoreAnimations)
                score.render(canvas, paint);

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


    public  boolean onTouch(MotionEvent e) {
        //add touch event to eventsQueue rather than processing it immediately. This is because
        //onTouchEvent is run in a separate thread by Android and if we touch and delete a number
        //in this touch UI thread while our game thread is accessing that same number, the game crashes
        //because two threads are accessing same memory being removed. We could do mutex but this setup
        //is pretty standard I believe.
        events.add(e);

        return true;
    }
    /*
        Tell if a bubble is done with popping animation
     */
    public synchronized boolean isPopped(TouchableBubble num){
        if(num.popping && !num.anim.playing) {
            return true;
        }
        return false;

    }

    /*
        Initialize HUD elements
     */
    private synchronized void initHud(){


        HUDBoard = com.funnums.funnums.maingame.GameView.loadBitmap("Shared/HudBoard.png", false);
        HUDBoard = Bitmap.createScaledBitmap(HUDBoard, screenX, topBuffer,false);

        int offset = pauseButton.getImg().getHeight()/2;
        Paint paint = GameActivity.gameView.paint;
        //HUDSquare(float x, float y, float width, float height, String msg, String value, Paint paint)
        curHUD = new HUDSquare(screenX * 1/8, topBuffer - offset*2, screenX/4, offset*2, "Current", String.valueOf(sum), paint);
        //curHUD = new HUDSquare(screenX * 1/4, topBuffer - offset, "Current", String.valueOf(sum), paint);
        targetHUD = new HUDSquare(screenX * 5/8, topBuffer - offset*2, screenX *4/16, offset*2, "Target", String.valueOf(sum), paint);
        //targetHUD = new HUDSquare(screenX * 3/4, topBuffer - offset, "Target", String.valueOf(target), paint);
        //timerHUD = new HUDSquare(screenX * 1/2, offset, "0:00", gameTimer.toString(), paint);
        timerHUD = new HUDSquareNoLabel(screenX * 1/2 - screenX*5/64, offset/5, screenX * 5/32, offset*2, "0:00",  paint);
    }


}
