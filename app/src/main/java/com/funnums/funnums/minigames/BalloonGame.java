package com.funnums.funnums.minigames;

import android.graphics.Canvas;
import android.graphics.Color;
import android.media.SoundPool;
import android.util.Log;
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
    private int speed=5;


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
    private int bRadius;

    //for implementing sound effects
    private SoundPool soundPool;
    private int volume;



    private int balloonsProcessed;

    private int balloonsTilBuffer = 3;

    private String inequality;

    private boolean inBalloonGenBuffer;


    public synchronized void init() {
        //game only finished when timer is done
        isFinished = false;
        //initalize random generator and make the first target between 5 and 8
        r = new Random();
        int mode = r.nextInt(5);
        rFrac= new FractionNumberGenerator(mode);

        setInequalityString(mode);

        target = rFrac.getTarget();

        screenX = com.funnums.funnums.maingame.GameActivity.screenX;
        screenY = com.funnums.funnums.maingame.GameActivity.screenY;

        bRadius = (int) (screenX * .15);


        generateNumber();

        //Initialize timer to 60 seconds, update after 1 sec interval
        initTimer(60000);

        //set up the pause button
        int offset = 100;
        Bitmap pauseImgDown = com.funnums.funnums.maingame.GameActivity.gameView.loadBitmap("pause_down.png", true);
        Bitmap pauseImg = com.funnums.funnums.maingame.GameActivity.gameView.loadBitmap("pause.png", true);
        pauseButton = new UIButton(screenX *3/4, 0, screenX, offset, pauseImg, pauseImgDown);

        balloonsProcessed = 0;
        inBalloonGenBuffer = false;
    }



    public synchronized void update(long delta){
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
        //generate a new balloon every 1 1/2 second if there are less than the max amount of numbers on the screen
        if (runningMilis > 1.5 * NANOS_TO_SECONDS) {
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
    }



    /*
    Generates a touchable number on screen
     */
    private synchronized void generateNumber() {
        int x, y;
        do {
            //Setting coordinates x and y
            x = r.nextInt(screenX);
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
    private synchronized void processEvents() {
        for(MotionEvent e : events)
        {
            int x = (int) e.getX();
            int y = (int) e.getY();

            checkTouchRadius(x, y);
        }
        events.clear();
    }

    /*
   Check if where the player touched the screen is on a touchable number and, if it is, call
   processScore() to update the number/score/etc
    */
    private synchronized void checkTouchRadius(int x, int y) {
        for(TouchableBalloon num : numberList) {
            //Trig! (x,y) is in a circle if (x - center_x)^2 + (y - center_y)^2 < radius^2
            if(Math.pow(x - num.getX(), 2) + Math.pow(y - num.getY(), 2) < Math.pow(num.getRadius(), 2)) {
                int value = 5;
                processScore(num, value);
                numberList.remove(num);
                break;
                //break after removing to avoid concurrent memory modification error, shouldn't be possible to touch two at once anyway
                //we could have a list of numbers to remove like in the update() function, but let's keep it simple for now
            }
        }

    }



    /*
       When a balloon is touched, call this function. It rewards the player a given amount of points
       if the balloon popped satisfies the given inequality, and deducts points otherwise
    */
    private synchronized void processScore(TouchableBalloon num, int value) {
        if (rFrac.gType == rFrac.GEQ_game) {
            scoreGEQ(num, value);
        }
        else if(rFrac.gType == rFrac.LEQ_game){
            scoreLEQ(num, value);
        }
        else if(rFrac.gType == rFrac.GT_game){
            scoreGT(num, value);
        }
        else if(rFrac.gType == rFrac.LT_game){
            scoreLT(num, value);
        }
        else if(rFrac.gType == rFrac.EQ_game){
            scoreEQ(num, value);
        }
        //check if it is time to enter buffer zone where we wait before making new target
        checkBalloonCount();

    }


    //When a number is leaves the screen, call this function. We check if the opposite is true
    //since users only pop balloons satisfying inequality, then they are rewarded if unpopped
    //balloond do NOT satisfy inequality
    private synchronized void processScoreOffScreen(TouchableBalloon num, int value) {
        //score player on opposite of inequality truth value
        if (rFrac.gType == rFrac.GEQ_game) {
            scoreLT(num, value);
        }
        else if(rFrac.gType == rFrac.LEQ_game){
            scoreGT(num, value);
        }
        else if(rFrac.gType == rFrac.GT_game){
            scoreLEQ(num, value);
        }
        else if(rFrac.gType == rFrac.LT_game){
            scoreGEQ(num, value);
        }
        else if(rFrac.gType == rFrac.EQ_game){
            scoreNEQ(num, value);
        }
        checkBalloonCount();
    }

    /*
        checks if game should enter buffer zone before changing target in which no additional
        balloons are generated, to avoid changing inequality right before player is about to touch
        a balloon
     */
    private void checkBalloonCount(){
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
        TextAnimator textAnimator = new TextAnimator("New Target!", screenX/2, screenY/2, 44, 185, 185, 1.25, 50);
        scoreAnimations.add(textAnimator);
    }

    //Checks if y coordinate of ballons is greater than -diameter of the ballons. If yes, process/remove balloon.
    private synchronized  void offScreenCheck() {
        for(TouchableBalloon num : numberList) {
            if(num.getY()<topBuffer+bRadius) {
                processScoreOffScreen(num, 5);
                numberList.remove(num);
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
            canvas.drawColor(Color.argb(255, 0, 0, 0));

            //draw all the numbers
            for(TouchableBalloon num : numberList)
                num.draw(canvas, paint);
            //draw all text animations
            for(TextAnimator score : scoreAnimations)
                score.render(canvas, paint);

            // Get offset to space out HUD
            int offset = 50;

            //Draw Inequality
            paint.setColor(Color.argb(255, 0, 0, 255));
            paint.setTextSize(45);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Inequality", screenX * 1/4, topBuffer - offset, paint);
            canvas.drawText(inequality, screenX * 1 / 4, topBuffer, paint);


            //Draw Target
            canvas.drawText("Target", screenX * 3/4, topBuffer - offset, paint);
            canvas.drawText(String.valueOf(target),  screenX * 3/4, topBuffer, paint);
            //draw timer
            canvas.drawText("Timer", screenX * 1/2, offset, paint);
            canvas.drawText(String.valueOf(gameTimer.toString()),  screenX *  1/2, offset*2, paint);
            //draw score
            canvas.drawText("Score", screenX * 1/4, offset, paint);
            canvas.drawText(String.valueOf(score),  screenX *  1/4, offset*2, paint);
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



    public synchronized boolean onTouch(MotionEvent e) {
        //add touch event to eventsQueue rather than processing it immediately. This is because
        //onTouchEvent is run in a separate thread by Android and if we touch and delete a number
        //in this touch UI thread while our game thread is accessing that same number, the game crashes
        //because two threads are accessing same memory being removed. We could do mutex but this setup
        //is pretty standard I believe.

        events.add(e);
        return true;
    }

    /*************inequality functions************/
    //all of these reward the player a given amount if the balloon passed as an argument satisfies
    //the current inequality, and deducts the given amount if the balloon does not satisfy the
    //given inequality

    private void scoreGEQ(TouchableBalloon num, int value){
        TextAnimator textAnimator;
        boolean correct;
        if (num.getValue().get_key() >= target.get_key()) {
            textAnimator = new TextAnimator("+" + String.valueOf(value), num.getX(), num.getY(), 0, 255, 0);
        } else {
            textAnimator = new TextAnimator("-" + String.valueOf(value), num.getX(), num.getY(), 0, 255, 0);
            value = -value;
        }
        scoreAnimations.add(textAnimator);
        score += value;
    }
    private void scoreLEQ(TouchableBalloon num, int value){
        TextAnimator textAnimator;
        if (num.getValue().get_key() <= target.get_key()) {
            textAnimator = new TextAnimator("+" + String.valueOf(value), num.getX(), num.getY(), 0, 255, 0);
        } else {
            textAnimator = new TextAnimator("-" + String.valueOf(value), num.getX(), num.getY(), 0, 255, 0);
            value = -value;
        }
        scoreAnimations.add(textAnimator);
        score += value;
    }
    private void scoreGT(TouchableBalloon num, int value){
        TextAnimator textAnimator;
        if (num.getValue().get_key() > target.get_key()) {
            textAnimator = new TextAnimator("+" + String.valueOf(value), num.getX(), num.getY(), 0, 255, 0);
        } else {
            textAnimator = new TextAnimator("-" + String.valueOf(value), num.getX(), num.getY(), 0, 255, 0);
            value = -value;
        }
        scoreAnimations.add(textAnimator);
        score += value;
    }
    private void scoreLT(TouchableBalloon num, int value){
        TextAnimator textAnimator;
        if (num.getValue().get_key() < target.get_key()) {
            textAnimator = new TextAnimator("+" + String.valueOf(value), num.getX(), num.getY(), 0, 255, 0);
        } else {
            textAnimator = new TextAnimator("-" + String.valueOf(value), num.getX(), num.getY(), 0, 255, 0);
            value = -value;
        }
        scoreAnimations.add(textAnimator);
        score += value;
    }
    private void scoreEQ(TouchableBalloon num, int value){
        TextAnimator textAnimator;
        if (num.getValue().get_key().equals(target.get_key())) {
            textAnimator = new TextAnimator("+" + String.valueOf(value), num.getX(), num.getY(), 0, 255, 0);
        } else {
            textAnimator = new TextAnimator("-" + String.valueOf(value), num.getX(), num.getY(), 0, 255, 0);
            value = -value;
        }
        scoreAnimations.add(textAnimator);
        score += value;
    }
    private void scoreNEQ(TouchableBalloon num, int value){
        TextAnimator textAnimator;
        if (!num.getValue().get_key().equals(target.get_key())) {
            textAnimator = new TextAnimator("+" + String.valueOf(value), num.getX(), num.getY(), 0, 255, 0);
        } else {
            textAnimator = new TextAnimator("-" + String.valueOf(value), num.getX(), num.getY(), 0, 255, 0);
            value = -value;
        }
        scoreAnimations.add(textAnimator);
        score += value;
    }




}
