package com.funnums.funnums.minigames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Random;
import android.graphics.Bitmap;

import com.funnums.funnums.R;
import com.funnums.funnums.classes.CollisionDetector;
import com.funnums.funnums.classes.TouchableBalloon;
import com.funnums.funnums.classes.FloatingObject;
import com.funnums.funnums.classes.FractionNumberGenerator;
import com.funnums.funnums.classes.Fraction;
import com.funnums.funnums.classes.HotAirBalloon;
import com.funnums.funnums.maingame.GameActivity;
import com.funnums.funnums.uihelpers.HUDSquare;
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

    String TAG = "ballonGame";

    //TODO make this vary based on phone size
    //this is the amount of space at the top of the screen used for the current sum, target, timer, and pause button
    private int topBuffer = 200;

    //running time, used to generate new numbers every few seconds
    private long runningMilis = 0;


    private int maxNumsOnScreen = 4;

    private int exactType = 0;

    //target player is trying to sum to
    private Fraction target;

    //speed of the balloons
    private int speed=4;


    //list of all the touchable numbers on screen
    ArrayList<TouchableBalloon> numberList = new ArrayList<>();

    // For drawing
    //private Paint paint;
    //private Canvas canvas;
    //private SurfaceHolder ourHolder;

    //generates random numbers for us
    private Random r;

    //generates random fractions for us
    private FractionNumberGenerator rFrac;

    //used to animate text, i.e show +3 when a 3 is touched
    ArrayList<TextAnimator> scoreAnimations = new ArrayList<>();

    //Optimal bubble radius
    private int xRadius;
    private int yRadius;

    //for implementing sound effects
    private int balloonDeflateId;
    private int balloonPopId;
    private int balloonInflateId;
    private int wooshId;

    private int balloonsProcessed;

    private int balloonsTilBuffer = 3;

    private String inequality;

    private boolean inBalloonGenBuffer;

    private Bitmap HUDBoard;
    private Bitmap bg;

    HUDSquare inequalityHUD;
    HUDSquare scoreHUD;
    HUDSquare targetHUD;
    HUDSquare timerHUD;
    int offset = 50;

    private float x1,x2;
    static final int MIN_DISTANCE = 150;

    HotAirBalloon hotAir1;
    HotAirBalloon hotAir2;
    FloatingObject directionBoard;


    public synchronized void init() {
        //game only finished when timer is done
        isFinished = false;

        //initializes soundPool
        soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC,0);
        balloonDeflateId = soundPool.load(context, R.raw.balloondeflate,1);
        balloonInflateId = soundPool.load(context,R.raw.ballooninflate,1);
        balloonPopId = soundPool.load(context,R.raw.balloonpop,1);
        wooshId=soundPool.load(context,R.raw.woosh,1);
        gameOverSoundId=soundPool.load(context,R.raw.timesup,1);

        //initalize random generator and make the first target between 5 and 8
        r = new Random();
        int mode = r.nextInt(5);
        rFrac= new FractionNumberGenerator(mode);

        setInequalityString(mode);

        target = rFrac.getTarget();

        screenX = com.funnums.funnums.maingame.GameActivity.screenX;
        screenY = com.funnums.funnums.maingame.GameActivity.screenY;

        speed = (int)Math.round(screenY * 0.003378);
        Log.d("SPEED", screenY + "");
        Log.d("SPEED", speed + "");

        xRadius = (int) (screenX * .13);
        yRadius = (int) (screenX * .15);


        generateNumber();

        //Initialize timer to 60 seconds, update after 1 sec interval
        initTimer(60000);

        //set up the pause button
        int offset = 100;
        Bitmap pauseImgDown = com.funnums.funnums.maingame.GameActivity.gameView.loadBitmap("pause_down.png", true);
        Bitmap pauseImg = com.funnums.funnums.maingame.GameActivity.gameView.loadBitmap("pause.png", true);
        pauseButton = new UIButton(screenX - pauseImg.getWidth(), 0, screenX, offset, pauseImg, pauseImgDown);

        balloonsProcessed = 0;
        inBalloonGenBuffer = false;


        HUDBoard = com.funnums.funnums.maingame.GameView.loadBitmap("HudBoard.png", false);
        HUDBoard = Bitmap.createScaledBitmap(HUDBoard, screenX, topBuffer,false);

        bg = com.funnums.funnums.maingame.GameView.loadBitmap("BalloonGame/BalloonBG.png", false);
        bg = Bitmap.createScaledBitmap(bg, screenX, screenY - 0/*topBuffer*/,false);

        Bitmap backdrop = com.funnums.funnums.maingame.GameView.loadBitmap("MenuBoard.png", true);

        GameActivity.gameView.pauseScreen.setBackDrop(backdrop);
        GameActivity.gameView.gameFinishedMenu.setBackDrop(backdrop);

        initHud();

        Bitmap hotAirImg1 = com.funnums.funnums.maingame.GameView.loadBitmap("BalloonGame/HotAir1.png", false);
        hotAir1 = new HotAirBalloon(hotAirImg1.getWidth()/2, screenY/2, hotAirImg1);
        Bitmap hotAirImg2 = com.funnums.funnums.maingame.GameView.loadBitmap("BalloonGame/HotAir2.png", false);
        hotAir2 = new HotAirBalloon(screenX - hotAirImg2.getWidth()*3/2, screenY * 3/8, hotAirImg2);

        Bitmap directionBoardImg = com.funnums.funnums.maingame.GameView.loadBitmap("BalloonGame/DirectionBoard.png", false);
        directionBoard = new FloatingObject(screenX *1/2 - directionBoardImg.getWidth()/2, topBuffer, directionBoardImg);
    }



    public synchronized void update(long delta){
        if(isPaused)
            return;

        //detect and handle collisions
        findCollisions();

        ArrayList<TouchableBalloon> toRemove = new ArrayList<>();
        for(TouchableBalloon num : numberList) {
            //update the number
            num.update(delta);

            if(isPopped(num))
                toRemove.add(num);

            if((num.getX() > screenX - num.getRadius() && num.getXVelocity() > 0)
                    || (num.getX()  - num.getRadius() < 0 && num.getXVelocity() < 0) ) {
                num.x = num.x - num.getXVelocity(); //fix balloon's position so it is not offscreen
                num.setXVelocity(0); //stop the balloon from going off screen
            }
        }
        //remove balloons that have finished pooping animation
        for(TouchableBalloon popped : toRemove) {
            numberList.remove(popped);
            //System.gc();
        }

        runningMilis += delta;
        //generate a new balloon every 1 1/2 second if there are less than the max amount of numbers on the screen
        if (runningMilis > 2 * NANOS_TO_SECONDS) {
            runningMilis = 0;
            //if there is room on screen and we are not in buffer zone, generate a new balloon
            if(numberList.size() < maxNumsOnScreen && !inBalloonGenBuffer)
                generateNumber();
            //else, if we are in buffer zone and player has cleared all balloons on screen, exit buffer zone
            //and create new target
            else if(inBalloonGenBuffer && numberList.size() == 0){
                inBalloonGenBuffer = false;
                makeNewTarget();
                balloonsProcessed = 0;
            }
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

        hotAir1.update(delta);
        hotAir2.update(delta);
        directionBoard.update(delta);
    }



    /*
    Generates a touchable number on screen
     */
    private synchronized void generateNumber() {
        int x, y;
        do {
            //Setting coordinates x and y
            x = r.nextInt(screenX - 2*xRadius) + xRadius;
            y = screenY;
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
            max = 93;
            min = 91;
        }
        else {
            max = 90;
            min = 88;
        }

        angle = r.nextInt(max - min) + min; //get random angle between max and min angles

        Fraction value = rFrac.getNewBalloon();

        TouchableBalloon num = new TouchableBalloon(x, y, angle, xRadius,yRadius,speed, value);
        numberList.add(num);
    }

    /*
    Process the touch events
     */
    private synchronized void processEvents() {
        try {
            for (MotionEvent e : events) {


                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = e.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = e.getX();
                        float deltaX = x2 - x1;
                        if (Math.abs(deltaX) > MIN_DISTANCE) {
                            int x = (int) e.getX();
                            int y = (int) e.getY();
                            if (x2 > x1) {
                                Log.d("SWIPE", "LEft to RIGHT");
                                checkSwipeX(y, true);
                            }

                            // Right to left swipe action
                            else {
                                Log.d("SWIPE", "RIGHT to LEFT");
                                checkSwipeX(y, false);
                            }
                        } else {
                            // consider as something else - a screen tap for example
                        }
                        break;
                }
                /*if(e.getActionMasked()==MotionEvent.ACTION_DOWN) {
                int x = (int) e.getX();
                int y = (int) e.getY();

                if (checkTouchRadius(x, y)) {
                    //removedNum = true;
                    break;
                }
            }*/
            }
        }
        catch(ConcurrentModificationException ex){
            Log.e("ERROR", ex.toString());
        }
        events.clear();
    }

    /*
   Check if where the player touched the screen is on a touchable number and, if it is, call
   processScore() to update the number/score/etc
    */
    private synchronized boolean checkTouchRadius(int x, int y) {
        for(TouchableBalloon num : numberList) {
            //Trig! //(x−h)2r2x+(y−k)2r2y≤1
            if(  (Math.pow(x - num.getX(), 2)/Math.pow(num.xRadius, 2)) + (Math.pow(y - num.getY(), 2)/(Math.pow(num.yRadius, 2))) <= 1){
            //if(Math.pow(x - num.getX(), 2) + Math.pow(y - num.getY(), 2) < Math.pow(num.getRadius(), 2) && !num.popping) {
                int value = 5;
                processScore(num, value);

                num.pop();

                return true;
                //break after removing to avoid concurrent memory modification error, shouldn't be possible to touch two at once anyway
                //we could have a list of numbers to remove like in the update() function, but let's keep it simple for now
            }
        }
        return false;

    }

    private synchronized void processCorrect(TouchableBalloon num, int value){
        boolean isCorrect = satisfiesInequality(num, inequality);
        processScore(isCorrect, value);
    }

    /*
       When a balloon is touched, call this function. It rewards the player a given amount of points
       if the balloon popped satisfies the given inequality, and deducts points otherwise
    */
    private synchronized void processScore(TouchableBalloon num, int value) {
        if(num.getX() <= screenX/2)
            processCorrect(num, value);
        else
            processIncorrect(num, value);


        //check if it is time to enter buffer zone where we wait before making new target
        checkBalloonCount();

    }


    //When a number is leaves the screen, call this function. We check if the opposite is true
    //since users only pop balloons satisfying inequality, then they are rewarded if unpopped
    //balloond do NOT satisfy inequality
    private synchronized void processIncorrect(TouchableBalloon num, int value) {
        //score player on opposite of inequality truth value
        boolean isCorrect = !satisfiesInequality(num, inequality);
        processScore(isCorrect, value);
    }


    private synchronized void processScore(boolean correct, int value){

        TextAnimator textAnimator;
        if (correct) {
            soundPool.play(balloonPopId,volume,volume,1,0,1);
            textAnimator = new TextAnimator("+" + String.valueOf(value), screenX * 1/8, offset*2*4/5, 0, 255, 0);
        } else {
            soundPool.play(balloonDeflateId,volume,volume,1,0,1);
            textAnimator = new TextAnimator("-" + String.valueOf(value), screenX * 1/8, offset*2*4/5, 0, 255, 0);
            value = -value;
        }
        scoreAnimations.add(textAnimator);
        score += value;
    }

    /*
        checks if game should enter buffer zone before changing target in which no additional
        balloons are generated, to avoid changing inequality right before player is about to touch
        a balloon
     */
    private synchronized void checkBalloonCount(){
        balloonsProcessed++;
        Log.d("BUFFER", "Increment balloons: " + balloonsProcessed);
        if(!inBalloonGenBuffer && balloonsProcessed >= balloonsTilBuffer){
            inBalloonGenBuffer = true;
            Log.d("BUFFER", "Enter the buffer balloonsProcessed: " + balloonsProcessed);
        }
    }

    /*
        Sets the string displayed for current inequality based on the int value for inequality types
        defined in FractionNumberGenerator
     */
    private void setInequalityString(int type){
        switch(type){
            case(FractionNumberGenerator.GEQ_game):
                inequality = ">=";
                break;
            case(FractionNumberGenerator.LEQ_game):
                inequality = "<=";
                break;
            case(FractionNumberGenerator.LT_game):
                inequality = "<";
                break;
            case(FractionNumberGenerator.GT_game):
                inequality = ">";
                break;
            case(FractionNumberGenerator.EQ_game):
                inequality = "=";
                break;

        }
    }

    /*
        create a new fraction target using FractionNumberGenerator and changes current inequality
     */
    private void makeNewTarget(){
        //there are 5 different inequalities in the game, 0 indexed, so we get a random int between 0-4, inclusive
        int inequalityType = r.nextInt(5);
        //set new game to re initialize fraction generator
        rFrac.new_game(inequalityType);
        //reset current inequality being displayed
        setInequalityString(inequalityType);
        //reset current fraction we are comparing to
        target=rFrac.getTarget();

        //add text animation
        TextAnimator textAnimator = new TextAnimator("New Target!", screenX/2, screenY/2, 44, 185, 185, 1.25, 50);
        scoreAnimations.add(textAnimator);

        //play balloon inflating sound effect
        soundPool.play(balloonInflateId,volume,volume,2,0,1);
    }

    //Checks if y coordinate of ballons is greater than -diameter of the ballons. If yes, process/remove balloon.
    private synchronized  void offScreenCheck() {
        for(TouchableBalloon num : numberList) {
            if(num.getY()<topBuffer+yRadius&& !num.popping) {
                processScore(num, 5);
                num.pop();
                break;
                //break after removing to avoid concurrent memory modification error, shouldn't be possible to touch two at once anyway
                //we could have a list of numbers to remove like in the update() function, but let's keep it simple for now
            }
        }
    }

    /*
        Detect collisions for all our numbers on screen and bounce numbers that have collided
     */
    private synchronized  void findCollisions() {
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
    private synchronized boolean findCollisions(int x, int y) {
        //this double for loop set up is so we don't check 0 1 and then 1 0 later, since they would have the same result
        //a bit of a micro optimization, but can be useful if there are a lot of numbers on screen

        //allow a little extra space for new appearing numbers
        int buffer = xRadius;
        for(int i = 0; i < numberList.size(); i++)
            if(CollisionDetector.isCollision(numberList.get(i), x, y, xRadius + buffer))
                return true;

        return false;
    }

    public synchronized void draw(SurfaceHolder ourHolder, Canvas canvas, Paint paint) {

        if (ourHolder.getSurface().isValid()) {
            //First we lock the area of memory we will be drawing to
            canvas = ourHolder.lockCanvas();

            // Rub out the last frame
            //canvas.drawColor(Color.argb(255, 0, 0, 0));

            canvas.drawBitmap(bg, 0, 0, paint);

            hotAir1.draw(canvas, paint);
            hotAir2.draw(canvas, paint);
            directionBoard.draw(canvas, paint);

            canvas.drawBitmap(HUDBoard, 0 , 0 , paint);

            //draw all the numbers
            for(TouchableBalloon num : numberList)
                num.draw(canvas, paint);


            //Draw Inequality
            targetHUD.drawBetter(canvas, paint, String.valueOf(target));
            scoreHUD.drawBetter(canvas, paint, String.valueOf(score));
            inequalityHUD.drawBetterNoLabel(canvas, paint, inequality);
            timerHUD.drawBetterNoLabel(canvas, paint, gameTimer.toString());
            //draw all text animations
            for(TextAnimator score : scoreAnimations)
                score.render(canvas, paint);

            //Draw pause button
            if(pauseButton != null)
                pauseButton.render(canvas, paint);
            //draw pause menu, if paused
            if(isPaused)
                com.funnums.funnums.maingame.GameActivity.gameView.pauseScreen.draw(canvas, paint);
            //draw game finished screen, if game is finished
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


    //return true if the number satisfies the current inequality, false otherwise
    private synchronized boolean satisfiesInequality(TouchableBalloon num, String inequality){
        switch (inequality){
            case ">":
                return num.getValue().get_key() > target.get_key();
            case "<":
                return num.getValue().get_key() < target.get_key();
            case ">=":
                return num.getValue().get_key() >= target.get_key();
            case "<=":
                return num.getValue().get_key() <= target.get_key();
            case "=":
                return num.getValue().get_key() <= target.get_key();
            default:
                Log.e("ERROR", "Invalide inequality " + inequality);
                return false;
        }
    }
    
    public synchronized boolean isPopped(TouchableBalloon num){
        if(num.popping && !num.anim.playing) {
            Log.d("pop", "remove it");
            return true;
        }
        return false;

    }

    private synchronized void initHud(){

        Paint paint = GameActivity.gameView.paint;
        //HUDSquare(float x, float y, float width, float height, String msg, String value, Paint paint)
        inequalityHUD = new HUDSquare(screenX * 7/16, topBuffer - offset*2, screenX/8, offset*2, "<=", inequality, paint);
        //curHUD = new HUDSquare(screenX * 1/4, topBuffer - offset, "Current", String.valueOf(sum), paint);
        targetHUD = new HUDSquare(screenX * 5/8, topBuffer - offset*2, screenX *4/16, offset*2, "Target", target.toString(), paint);
        //targetHUD = new HUDSquare(screenX * 3/4, topBuffer - offset, "Target", String.valueOf(target), paint);
        //timerHUD = new HUDSquare(screenX * 1/2, offset, "0:00", gameTimer.toString(), paint);
        timerHUD = new HUDSquare(screenX * 1/2 - screenX*5/64, offset/5, screenX * 5/32, offset*2, "0:00", gameTimer.toString(), paint);
        scoreHUD = new HUDSquare(screenX * 1/8, offset/5,  screenX * 5/32, offset*2*4/5, "Score", String.valueOf(score), paint);
    }

    /*
   Check if where the player touched the screen is on a touchable number and, if it is, call
   processScore() to update the number/score/etc
    */
    private synchronized boolean checkSwipeX(int y, boolean isSwipeRight) {
        for(TouchableBalloon num : numberList) {
            //Trig! //(x−h)2r2x+(y−k)2r2y≤1
            if  (Math.abs(num.getY() - y) <= 150 ){
                //if(Math.pow(x - num.getX(), 2) + Math.pow(y - num.getY(), 2) < Math.pow(num.getRadius(), 2) && !num.popping) {
                Log.d("SWIPE", "MOVE " + num.getValue().toString());
                if(isSwipeRight)
                    num.setXVelocity(10);
                else
                    num.setXVelocity(-10);
                soundPool.play(wooshId,volume,volume,1,0,1);
                return true;
                //break after removing to avoid concurrent memory modification error, shouldn't be possible to touch two at once anyway
                //we could have a list of numbers to remove like in the update() function, but let's keep it simple for now
            }
        }
        return false;

    }


}
