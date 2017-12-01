package com.funnums.funnums.classes;

/*
* Class GameCountdownTimer inherits from abstract class CountDownTimer. Methods are defined for creating a small timer
* as well returning String representation of the CountDown
*
* */




import java.util.concurrent.TimeUnit;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.CountDownTimer;
import android.content.Context;

import com.funnums.funnums.R;
import com.funnums.funnums.maingame.GameActivity;

public class GameCountdownTimer extends CountDownTimer {

    private String displayTime;/*String representation of the Countdown*/
    //GameCountdownTimer gameTimer;


    private long timeLeft;

    public boolean isPaused;

    public Context context;

    //Constructor
    public GameCountdownTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        isPaused = false;

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

    /*
        OnFinish, set flag to display game finished menu and set the score to be displayed. Also
        update high score, if applicable
     */
    @Override
    public void onFinish() {
        com.funnums.funnums.minigames.MiniGame.playGameOverSound();
        completeGame();

        timeLeft = 0;
        displayTime = "0:00";
    }

    public static void completeGame(){
        com.funnums.funnums.maingame.GameActivity.gameView.currentGame.isFinished = true;
        int score = com.funnums.funnums.maingame.GameActivity.gameView.currentGame.score;
        com.funnums.funnums.maingame.GameActivity.gameView.gameFinishedMenu.setScore(score);
        com.funnums.funnums.maingame.LeaderboardGameActivity.storeHighScore(score);
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
