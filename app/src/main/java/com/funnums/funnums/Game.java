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
import android.os.CountDownTimer;



public class Game extends SurfaceView implements Runnable {

    public String logTag = "Game"; //for debugging

    private final static int NANOS_TO_SECONDS = 1000000000;

    // Countdown timer.
    private CountDownTimer newNumTimer;

    // Used to hold touch events so that drawing thread and onTouch thread don't result in concurrent access
    // not likely that these threads would interact, but if they do the game will crash!! which is why
    //we keep events in a separate list to be processed in the game loop
    private ArrayList<MotionEvent> events = new ArrayList<>();

    //For the FX
    private SoundPool soundPool;
    int start = -1;
    int bump = -1;
    int destroyed = -1;
    int win = -1;

    private boolean gameEnded;

    private Context context;

    private int screenX;
    private int screenY;

    private float distanceRemaining;
    private long timeTaken;
    private long timeStarted;
    private long fastestTime;

    volatile boolean playing;
    Thread gameThread = null;

    // Game objects
    private TouchableNumber num;

    //running time, used to generate new numbers every few seconds
    private long runningMilis = 0;
    /*public EnemyShip enemy1;
    public EnemyShip enemy2;
    public EnemyShip enemy3;
    public EnemyShip enemy4;
    public EnemyShip enemy5;


    // Make some random space dust
    ArrayList<SpaceDust> dustList = new ArrayList<SpaceDust>();*/

    private int score;
    private int target;

    ArrayList<TouchableNumber> numberList = new ArrayList<>();
    // For drawing
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder ourHolder;

    // For saving and loading the high score
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private Random r;


    Game(Context context, int x, int y) {
        super(context);
        this.context  = context;

        r = new Random();
        target = r.nextInt(10)+5;

        // This SoundPool is deprecated but don't worry
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        try{
            //Create objects of the 2 required classes
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            //create our three fx in memory ready for use
            descriptor = assetManager.openFd("start.ogg");
            start = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("win.ogg");
            win = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("bump.ogg");
            bump = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("crash.ogg");
            destroyed = soundPool.load(descriptor, 0);


        }catch(IOException e){
            //Print an error message to the console
            Log.e("error", "failed to load sound files");
        }

        screenX = x;
        screenY = y;

        // Initialize our drawing objects
        ourHolder = getHolder();
        paint = new Paint();



        // Initialise our player ship
        //player = new PlayerShip(context, x, y);
        //enemy1 = new EnemyShip(context, x, y);
        //enemy2 = new EnemyShip(context, x, y);
        //enemy3 = new EnemyShip(context, x, y);

        //int numSpecs = 40;

        //for (int i = 0; i < numSpecs; i++) {
        // Where will the dust spawn?
        //SpaceDust spec = new SpaceDust(x, y);
        //dustList.add(spec);
        //}

        // Load fastest time
        prefs = context.getSharedPreferences("HiScores", context.MODE_PRIVATE);
        // Initialize the editor ready
        editor = prefs.edit();
        // Load fastest time
        // if not available our highscore = 1000000
        fastestTime = prefs.getLong("fastestTime", 1000000);

        startGame();
    }


    private void generateNumber()
    {
        int x = r.nextInt(screenX - 100)+50;
        TouchableNumber firstNum = new TouchableNumber(context, x, 200);
        numberList.add(firstNum);
    }

    private void startGame(){
        // Play the start sound
        soundPool.play(start,1, 1, 0, 0, 1);

        //Initialise game objects

        int x = r.nextInt(screenX - 100)+50;
        TouchableNumber firstNum = new TouchableNumber(context, x, 0);
        numberList.add(firstNum);

        //initialize timer for making new numbers




        /*enemy1 = new EnemyShip(context, screenX, screenY);
        enemy2 = new EnemyShip(context, screenX, screenY);
        enemy3 = new EnemyShip(context, screenX, screenY);
        if(screenX > 1000){
            enemy4 = new EnemyShip(context, screenX, screenY);
        }
        if(screenX > 1200){
            enemy5 = new EnemyShip(context, screenX, screenY);
        }*/




        int numSpecs = 400;

        /*for (int i = 0; i < numSpecs; i++) {
            // Where will the dust spawn?
            SpaceDust spec = new SpaceDust(screenX, screenY);
            dustList.add(spec);
        }*/


        // Reset time and distance
        distanceRemaining = 10000;// 10 km
        timeTaken = 0;

        // Get start time
        timeStarted = System.currentTimeMillis();

        gameEnded = false;
        soundPool.play(start, 1, 1, 0, 0, 1);
    }

    @Override
    public void run()
    {

        while (playing)
        {
            long beforeUpdateRender = System.nanoTime();


            update();
            draw();
            control();

            long updateDurationMillis = (System.nanoTime() - beforeUpdateRender); // / 1000000L;
            //Log.d(VIEW_LOG_TAG, String.valueOf(updateDurationMillis));
            runningMilis += updateDurationMillis;
        }
    }





    private void update() {
        // Collision detection on new positions
        // Before move because we are testing last frames
        // position which has just been drawn
        boolean hitDetected = false;
        /*if(Rect.intersects(player.getHitbox(), enemy1.getHitbox())){
            hitDetected = true;
            enemy1.setX(-100);//this will cause mine to respawn in 1 frame
        }
        if(Rect.intersects(player.getHitbox(), enemy2.getHitbox())){
            hitDetected = true;
            enemy2.setX(-100);//this will cause mine to respawn in 1 frame
        }
        if(Rect.intersects(player.getHitbox(), enemy3.getHitbox())){
            hitDetected = true;
            enemy3.setX(-100);//this will cause mine to respawn in 1 frame
        }
        if(screenX > 1000){
            if(Rect.intersects(player.getHitbox(), enemy4.getHitbox())){
                hitDetected = true;
                enemy4.setX(-100);//this will cause mine to respawn in 1 frame
            }
        }
        if(screenX > 1200){
            if(Rect.intersects(player.getHitbox(), enemy3.getHitbox())){
                hitDetected = true;
                enemy5.setX(-100);//this will cause mine to respawn in 1 frame
            }
        }

        if(hitDetected) {
            soundPool.play(bump, 1, 1, 0, 0, 1);
            player.reduceShieldStrength();
            if (player.getShieldStrength() < 0) {
                soundPool.play(destroyed, 1, 1, 0, 0, 1);
                gameEnded = true;
            }
        }*/


        ArrayList<TouchableNumber> toRemove = new ArrayList<>();
        // Update the numbers
        for(TouchableNumber num : numberList)
        {
            num.update();
            if (num.getY() > screenY + num.getRadius())
            {
                toRemove.add(num);
            }
        }

        for(TouchableNumber offScreen : toRemove)
            numberList.remove(offScreen);

        // Update the enemies
        /*enemy1.update(player.getSpeed());
        enemy2.update(player.getSpeed());
        enemy3.update(player.getSpeed());
        if(screenX > 1000) {
            enemy4.update(player.getSpeed());
        }
        if(screenX > 1200) {
            enemy5.update(player.getSpeed());
        }
        for (SpaceDust sd : dustList) {
            sd.update(player.getSpeed());
        }

        if(!gameEnded) {
            //subtract distance to home planet based on current speed
            distanceRemaining -= player.getSpeed();

            //How long has the player been flying
            timeTaken = System.currentTimeMillis() - timeStarted;
        }*/


        if (runningMilis > 2 * NANOS_TO_SECONDS)
        {
            generateNumber();
            runningMilis = 0;
        }
        processEvents();


    }

    private void draw() {

        if (ourHolder.getSurface().isValid()) {
            //First we lock the area of memory we will be drawing to
            canvas = ourHolder.lockCanvas();

            // Rub out the last frame
            canvas.drawColor(Color.argb(255, 0, 0, 0));

            // For debugging
            // Switch to white pixels
            paint.setColor(Color.argb(255, 255, 255, 255));
            // Draw Hit boxes
            //canvas.drawRect(player.getHitbox().left, player.getHitbox().top, player.getHitbox().right, player.getHitbox().bottom, paint);
            //canvas.drawRect(enemy1.getHitbox().left, enemy1.getHitbox().top, enemy1.getHitbox().right, enemy1.getHitbox().bottom, paint);
            //canvas.drawRect(enemy2.getHitbox().left, enemy2.getHitbox().top, enemy2.getHitbox().right, enemy2.getHitbox().bottom, paint);
            //canvas.drawRect(enemy3.getHitbox().left, enemy3.getHitbox().top, enemy3.getHitbox().right, enemy3.getHitbox().bottom, paint);


            // White specs of dust
            paint.setColor(Color.argb(255, 255, 255, 255));
            //Draw the dust from our arrayList
            /*for (SpaceDust sd : dustList) {
                canvas.drawPoint(sd.getX(), sd.getY(), paint);
            }*/

            // Draw the player
            /*
            canvas.drawBitmap(player.getBitmap(), player.getX(), player.getY(), paint);
            canvas.drawBitmap(enemy1.getBitmap(), enemy1.getX(), enemy1.getY(), paint);
            canvas.drawBitmap(enemy2.getBitmap(), enemy2.getX(), enemy2.getY(), paint);
            canvas.drawBitmap(enemy3.getBitmap(), enemy3.getX(), enemy3.getY(), paint);
            */
            for(TouchableNumber num : numberList)
                num.draw(canvas, paint); //canvas.drawCircle(num.getX(), num.getY(), num.getRadius(), paint);


            if(!gameEnded) {
                // Draw the hud
                paint.setColor(Color.argb(255, 0, 0, 255));
                paint.setTextSize(45);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("Current", screenX/4, 50, paint);
                canvas.drawText(String.valueOf(score),  screenX/4, 100, paint);


                canvas.drawText("Target", screenX * 3/4, 50, paint);
                canvas.drawText(String.valueOf(target),  screenX * 3/4, 100, paint);
                //canvas.drawText("Fastest:" + fastestTime + "s", 10, 20, paint);
                /*canvas.drawText("Fastest:" + formatTime(fastestTime) + "s", 10, 20, paint);
                //canvas.drawText("Time:" + timeTaken + "s", screenX / 2, 20, paint);
                canvas.drawText("Time:" + formatTime(timeTaken) + "s", screenX / 2, 20, paint);
                canvas.drawText("Distance:" + distanceRemaining / 1000 + " KM", screenX / 3, screenY - 20, paint);*/
                //canvas.drawText("Shield:" + player.getShieldStrength(), 10, screenY - 20, paint);
                //canvas.drawText("Speed:" + player.getSpeed() * 60 + " MPS", (screenX / 3) * 2, screenY - 20, paint);
            }else{
                // Show pause screen
                paint.setTextSize(80);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("Game Over", screenX/2, 100, paint);
                paint.setTextSize(25);
                //canvas.drawText("Fastest:"+ fastestTime + "s", screenX/2, 160, paint);
                canvas.drawText("Fastest:"+ formatTime(fastestTime) + "s", screenX/2, 160, paint);
                //canvas.drawText("Time:" + timeTaken + "s", screenX / 2, 200, paint);
                canvas.drawText("Time:" + formatTime(timeTaken) + "s", screenX / 2, 200, paint);
                canvas.drawText("Distance remaining:" + distanceRemaining/1000 + " KM",screenX/2, 240, paint);
                paint.setTextSize(80);
                canvas.drawText("Tap to replay!", screenX/2, 350, paint);
            }
            // Unlock and draw the scene
            ourHolder.unlockCanvasAndPost(canvas);
        }

    }

    private void control() {
        try
        {
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
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {

        }

        if (newNumTimer != null) {
            newNumTimer.cancel();
            newNumTimer = null;
        }
    }

    // Make a new thread and start it
    // Execution moves to our R
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();

        /*if (newNumTimer == null) {
            initNewNumTimer();
            newNumTimer.start();
        }*/
    }

    private String formatTime(long time){
        long seconds = (time) / 1000;
        long thousandths = (time) - (seconds * 1000);
        String strThousandths = "" + thousandths;
        if (thousandths < 100){strThousandths = "0" + thousandths;}
        if (thousandths < 10){strThousandths = "0" + strThousandths;}
        String stringTime = "" + seconds + "." + strThousandths;
        return stringTime;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent)
    {
        events.add(motionEvent);
        //checkTouchRadius((int)motionEvent.getX(), (int)motionEvent.getY());
        return true;
    }

    private void processEvents()
    {
        for(MotionEvent e : events)
            checkTouchRadius((int) e.getX(), (int) e.getY());
        events.clear();
    }

    private void checkTouchRadius(int x, int y)
    {
        //(x - center_x)^2 + (y - center_y)^2 < radius^2;
        //Math.pow(x - x, 2) + Math.pow(y - y, 2) < Math.pow(radius, 2);

        for(TouchableNumber num : numberList)
        {
            //Trig! (x,y) is in a circle if (x - center_x)^2 + (y - center_y)^2 < radius^2
            if(Math.pow(x - num.getX(), 2) + Math.pow(y - num.getY(), 2) < Math.pow(num.getRadius(), 2))
            {
                Log.d(VIEW_LOG_TAG, "Circle touched!");
                score += num.getValue();
                numberList.remove(num);
                break;
                //break after removing to avoid concurrent memory modification error, shouldn't be possible to touch two at once anyway
            }
        }

    }
}

