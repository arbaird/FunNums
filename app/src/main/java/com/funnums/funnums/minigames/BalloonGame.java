package com.funnums.funnums.minigames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
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
import com.funnums.funnums.uihelpers.HUDSquareNoLabel;
import com.funnums.funnums.uihelpers.TextAnimator;
import com.funnums.funnums.uihelpers.UIButton;

/*
    The balloon game to practice solving fraction inequalities
 */
public class BalloonGame extends MiniGame {
    //for debugging
    public String VIEW_LOG_TAG = "BallloonGame";

    //conversion from nanosecs to seconds
    public final static int NANOS_TO_SECONDS = 1000000000;

    // Used to hold touch events so that drawing thread and onTouch thread don't result in concurrent access
    // not likely that these threads would interact, but if they do the game will crash!! which is why
    //we keep events in a separate list to be processed in the game loop
    private ArrayList<MotionEvent> events = new ArrayList<>();

    //dimensions of the screen
    private int screenX;
    private int screenY;

    //this is the amount of space at the top of the screen used for the current sum, target, timer, and pause button
    private int topBuffer;// = 200;

    //running time, used to generate new numbers every few seconds
    private long runningMilis = 0;

    //target the balloons values will be compared against
    private Fraction target;

    //speed of the balloons
    private int speed;//=4;


    //list of all the balloons on screen
    ArrayList<TouchableBalloon> numberList = new ArrayList<>();

    //generates random numbers for us
    private Random r;

    //generates random fractions for us
    private FractionNumberGenerator rFrac;

    //used to animate text, i.e show +3 when a 3 is touched
    ArrayList<TextAnimator> scoreAnimations = new ArrayList<>();

    //Radius for x and y, for ellipses
    private int xRadius;
    private int yRadius;


    //for implementing sound effects
    private int balloonDeflateId;
    private int balloonPopId;
    private int balloonInflateId;
    private int wooshId;

    //balloons that have been processed this round
    private int balloonsProcessed;

    //balloons until we generate before entering buffer, in which no more balloons are generated
    //until all balloons have been popped(processed)
    private int balloonsTilBuffer = 3;

    //current inequality used to compare balloons against the target fraction
    private String inequality;

    //flag if we are in balloon gneration buffer
    private boolean inBalloonGenBuffer;

    //the board for th HUD
    private Bitmap HUDBoard;
    //background
    private Bitmap bg;

    //squares holding info displayed in HUD
    HUDSquare inequalityHUD;
    HUDSquare scoreHUD;
    HUDSquare targetHUD;
    HUDSquare timerHUD;
    //space between HUD squared
    int offset;

    //used to determine swipes on the screen
    private float x1,x2;
    static final int MIN_DISTANCE = 50;

    //list of all floating objects, i.e the hot air balloons and the arrows
    ArrayList<FloatingObject> floatingObjects = new ArrayList<>();

    //flag to see if the player got all inequalities correct this round
    boolean allCorrect;

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
        //set current inequality
        setInequalityString(mode);
        //get starting target
        target = rFrac.getTarget();

        screenX = com.funnums.funnums.maingame.GameActivity.screenX;
        screenY = com.funnums.funnums.maingame.GameActivity.screenY;
        //scale speed based on phone size
        speed = (int)Math.floor(screenY * 0.003378);
        //get radius for x and y based on phone size, to draw elliptical balloons
        xRadius = (int) (screenX * .13);
        yRadius = (int) (screenX * .15);

        //generated first balloon
        generateNumber();

        //Initialize timer to 60 seconds, update after 1 sec interval
        initTimer(60000);
        //get offset for drawing HUD elements
        offset = pauseButton.getImg().getHeight()/2;
        //initialize flags for balloon generation and if all answers are correct this round
        balloonsProcessed = 0;
        inBalloonGenBuffer = false;
        allCorrect = true;


        //initialize the background
        bg = com.funnums.funnums.maingame.GameView.loadBitmap("BalloonGame/BalloonBG.png", false);
        bg = Bitmap.createScaledBitmap(bg, screenX, screenY - 0/*topBuffer*/,false);

        //set the backdrop for the menu and pause screen
        com.funnums.funnums.maingame.GameActivity.gameView.setMenuBackdrop("BalloonGame/BalloonMenuBoard.png");

        initHud();

        initFloatingObjects();
        //set typeface specific to balloon game
        Typeface tf =Typeface.createFromAsset(GameActivity.assets,"fonts/FunCartoon2.ttf");
        GameActivity.gameView.paint.setTypeface(tf);

    }


    /*
        Update the game logic
     */
    public synchronized void update(long delta){
        //detect and handle collisions
        findCollisions();

        //create list of balloons to remove, can't remove inside following loop without causing Concurrent
        //memory modification error
        ArrayList<TouchableBalloon> toRemove = new ArrayList<>();
        for(TouchableBalloon num : numberList) {
            //update the number
            num.update(delta);
            //add popped numbers in list of balloons to be removed
            if(isPopped(num))
                toRemove.add(num);
            //check if number is drifting offscreen
            if((num.getX() > screenX - num.getRadius() && num.getXVelocity() > 0)
                    || (num.getX()  - num.getRadius() < 0 && num.getXVelocity() < 0) ) {
                num.x = num.x - num.getXVelocity(); //fix balloon's position so it is not offscreen
                num.setXVelocity(0); //stop the balloon from going off screen
            }
        }
        //remove balloons that have finished popping animation
        for(TouchableBalloon popped : toRemove) {
            numberList.remove(popped);
        }

        runningMilis += delta;
        //generate a new balloon every 2 seconds AND there is enough room, in case speed is low
        if (runningMilis > 2 * NANOS_TO_SECONDS && isRoomForNewBalloon()) {
            runningMilis = 0;
            //if  we are not in buffer zone, generate a new balloon
            if(!inBalloonGenBuffer)
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
        //remove completely faded texts
        for(TextAnimator faded : scoresToRemove)
            scoreAnimations.remove(faded);
        //update the floating objects
        for(FloatingObject f : floatingObjects)
            f.update(delta);
    }



    /*
        Generates a balloon on screen
     */
    private synchronized void generateNumber() {
        int x, y;
        //do {
            //Setting coordinates x and y
            x = r.nextInt(screenX - 2*xRadius) + xRadius;
            y = screenY;
        //}
        //while(findCollisions(x,y));
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

        //get new fraction from fraction generator
        Fraction value = rFrac.getNewBalloon();
        //add this balloon to the list of balloons
        TouchableBalloon num = new TouchableBalloon(x, y, angle, xRadius,yRadius,speed, value);
        numberList.add(num);
    }

    /*
    Process the touch events
     */
    private synchronized void processEvents() {
        try {
            for (MotionEvent e : events) {

                //determine if user has swiped the screen horizontally
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        //store starting x coordinate of potential swipe
                        x1 = e.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        //get ending x coordinate of potential swipe
                        x2 = e.getX();
                        float deltaX = x2 - x1;
                        //determine if swipe was wide enough to be considered an intentional swipe
                        if (Math.abs(deltaX) > MIN_DISTANCE) {
                            //get "altitude" of swipe to see if it is at same altitude of any balloons
                            int y = (int) e.getY();
                            //left to right swipe action
                            if (x2 > x1) {
                                checkSwipeX(y, true);
                            }
                            // Right to left swipe action
                            else {
                                Log.d("SWIPE", "RIGHT to LEFT");
                                checkSwipeX(y, false);
                            }
                        }
                        break;
                }
            }
        }
        //don't let multiple threads working on touch events crash the app
        catch(ConcurrentModificationException ex){
            Log.e("ERROR", ex.toString());
        }
        //empty events, so we don't keep processing the same events every iteration
        events.clear();
    }

    /*
        Processs the balloon and see if it correctly satsified the inequality
     */
    private synchronized void processCorrect(TouchableBalloon num, int value){
        boolean isCorrect = satisfiesInequality(num, inequality);
        processScore(isCorrect, value);
    }

    /*
       When a balloon is touched, call this function. It rewards the player a given amount of points
       if the balloon popped satisfies the given inequality, and deducts points otherwise
    */
    private synchronized void processScore(TouchableBalloon num, int value) {
        if(isFinished)
            return;
        if(num.getX() <= screenX/2)
            processCorrect(num, value);
        else
            processIncorrect(num, value);


        //check if it is time to enter buffer zone where we wait before making new target
        checkBalloonCount();

    }


    /*
        Processs the balloon and see if it correctly did not satsify the inequality
     */
    private synchronized void processIncorrect(TouchableBalloon num, int value) {
        //score player on opposite of inequality truth value
        boolean isCorrect = !satisfiesInequality(num, inequality);
        processScore(isCorrect, value);
    }

    /*
        Process the score, given a boolean indicating if the balloon being processed correctly
        satisfies the current inequality or not
     */
    private synchronized void processScore(boolean correct, int value){
        //play sounds and update score appropriately based on if answer is correct
        TextAnimator textAnimator;
        if (correct) {
            soundPool.play(balloonPopId,volume,volume,1,0,1);
            textAnimator = new TextAnimator("+" + String.valueOf(value), screenX * 1/8, offset*2*4/5, 0, 255, 0);
        } else {
            soundPool.play(balloonDeflateId,volume,volume,1,0,1);
            textAnimator = new TextAnimator("-" + String.valueOf(value), screenX * 1/8, offset*2*4/5, 0, 255, 0);
            value = -value;
            allCorrect = false;
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
        if(!inBalloonGenBuffer && balloonsProcessed >= balloonsTilBuffer){
            inBalloonGenBuffer = true;
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
        TextAnimator textAnimator = new TextAnimator("New Target!", screenX/2, screenY/2, 44, 220, 185, 1.25, 50);
        scoreAnimations.add(textAnimator);

        //play balloon inflating sound effect
        soundPool.play(balloonInflateId,volume,volume,2,0,1);
        //if all answers were correct for previous round, reward the player with more time and display message
        if(allCorrect){
            TextAnimator addTimetextAnimator = new TextAnimator("+15", screenX * 1/2, timerHUD.bottom-timerHUD.MARGIN, 0, 255, 0);
            scoreAnimations.add(addTimetextAnimator);

            TextAnimator bonusTextAnimator = new TextAnimator("All Correct Bonus!", screenX * 1/2 + (int)timerHUD.width/2, (int)timerHUD.bottom+timerHUD.MARGIN, 0, 255, 0, 1.25, 50);
            scoreAnimations.add(addTimetextAnimator);
            scoreAnimations.add(bonusTextAnimator);
            long newTime = 15000;
            com.funnums.funnums.maingame.GameActivity.gameView.updateGameTimer(newTime);
        }
        allCorrect = true;
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

            for(FloatingObject f : floatingObjects)
                f.draw(canvas, paint);

            canvas.drawBitmap(HUDBoard, 0 , 0 , paint);

            //draw all the numbers
            for(TouchableBalloon num : numberList)
                num.draw(canvas, paint);


            //Draw Inequality
            targetHUD.draw(canvas, paint, String.valueOf(target));
            scoreHUD.draw(canvas, paint, String.valueOf(score));
            inequalityHUD.draw(canvas, paint, inequality);
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
            //draw game finished screen, if game is finished
            if(isFinished)
                com.funnums.funnums.maingame.GameActivity.gameView.gameFinishedMenu.draw(canvas, paint);

            ourHolder.unlockCanvasAndPost(canvas);
        }


    }


    /*
        handle touch events
     */
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
        if(num.getValue().get_key() <= 0 ||num.getValue().get_key() >= 1) {
            Log.e(VIEW_LOG_TAG, "Invalid fraction value: " + num.getValue().toString());
            return false;
        }
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
                return num.getValue().get_key().equals(target.get_key());
            default:
                Log.e("ERROR", "Invalid inequality " + inequality);
                return false;
        }
    }
    /*
        Determine if a balloon is done with its popping animation
     */
    public synchronized boolean isPopped(TouchableBalloon num){
        if(num.popping && !num.anim.playing) {
            return true;
        }
        return false;

    }
    /*
        initialize the HUD
     */
    private synchronized void initHud(){
        topBuffer = offset*4;

        HUDBoard = com.funnums.funnums.maingame.GameView.loadBitmap("Shared/HudBoard.png", false);
        HUDBoard = Bitmap.createScaledBitmap(HUDBoard, screenX, topBuffer,false);

        Paint paint = GameActivity.gameView.paint;

        //set up HUDSquares and place them based on size of phone
        inequalityHUD = new HUDSquareNoLabel(screenX * 7/16, topBuffer - offset*2, screenX/8, offset*2, "<=", paint);
        targetHUD = new HUDSquare(screenX * 5/8, topBuffer - offset*2, screenX *4/16, offset*2, "Target", target.toString(), paint);
        timerHUD = new HUDSquareNoLabel(screenX * 1/2 - screenX*5/64, offset/5, screenX * 5/32, offset*2, "0:00",  paint);
        scoreHUD = new HUDSquare(screenX * 1/8, offset/5,  screenX * 5/32, offset*2*4/5, "Score", String.valueOf(score), paint);
    }

    /*
        Check if where the player touched the screen is on a touchable number and, if it is, call
        processScore() to update the number/score/etc
    */
    private synchronized boolean checkSwipeX(int y, boolean isSwipeRight) {
        for(TouchableBalloon num : numberList) {
            if  (Math.abs(num.getY() - y) <= 150 ){
                //set velocity based on direction of swipe
                if(isSwipeRight)
                    num.setXVelocity(10);
                else
                    num.setXVelocity(-10);
                soundPool.play(wooshId,volume,volume,1,0,1);
                return true;

            }
        }
        return false;

    }

    /*
        Initialize the floating objects
     */
    private synchronized void initFloatingObjects() {
        Bitmap hotAirImg1 = com.funnums.funnums.maingame.GameView.loadBitmap("BalloonGame/HotAir1.png", false);
        HotAirBalloon hotAir1 = new HotAirBalloon(hotAirImg1.getWidth() / 16, topBuffer + hotAirImg1.getHeight() / 4, hotAirImg1);
        Bitmap hotAirImg2 = com.funnums.funnums.maingame.GameView.loadBitmap("BalloonGame/HotAir2.png", false);
        HotAirBalloon hotAir2 = new HotAirBalloon(screenX - hotAirImg2.getWidth(), topBuffer, hotAirImg2);


        Bitmap directionBoardImg = com.funnums.funnums.maingame.GameView.loadBitmap("BalloonGame/DirectionBoard.png", false);
        FloatingObject directionBoard = new FloatingObject(screenX * 1 / 2 - directionBoardImg.getWidth() / 2, topBuffer, directionBoardImg);
        floatingObjects.add(hotAir1);
        floatingObjects.add(hotAir2);
        floatingObjects.add(directionBoard);
    }

    private boolean isRoomForNewBalloon(){
        for(TouchableBalloon balloon : numberList){
            if(balloon.getY() + balloon.yRadius*3 > screenY) {
                return false;
            }
        }
        return true;
    }
}
