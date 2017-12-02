package com.funnums.funnums.classes;

/*
* Class GameCountdownTimer inherits from abstract class CountDownTimer. Methods are defined for creating a small timer
* as well returning String representation of the CountDown
*
* */




import java.util.concurrent.TimeUnit;
import android.os.CountDownTimer;
import android.content.Context;

public class GameCountdownTimer extends CountDownTimer {

    private String displayTime;/*String representation of the Countdown*/
    //time til the timer is complete
    private long timeLeft;

    public boolean isPaused;
    //context used to call android functions from surface view
    public Context context;

    //Constructor
    public GameCountdownTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        //initially not paused
        isPaused = false;
        //perform first tick
        onTick(millisInFuture);
    }

    //Updates displayTime after every  "tick" accordingly
    @Override
    public void onTick(long millisUntilFinished) {
        timeLeft = millisUntilFinished;
        displayTime = String.format("%01d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeLeft) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeLeft)),
                TimeUnit.MILLISECONDS.toSeconds(timeLeft) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeLeft)));
    }

    //perform end game logic and reset timer values
    @Override
    public void onFinish() {
        completeGame();

        timeLeft = 0;
        displayTime = "0:00";
    }

    /*
        OnFinish, set flag to display game finished menu and set the score to be displayed. Also
        update high score, if applicable
     */
    public void completeGame(){
        com.funnums.funnums.maingame.GameActivity.gameView.currentGame.onFinish();
    }

    public long getTime() {
        return timeLeft;
    }

    //String representation of countdown
    @Override
    public String toString() {
        return displayTime;
    }

}
