package com.funnums.funnums.classes;

/*
* Class GameCountdownTimer inherits from abstract class CountDownTimer. Methods are defined for creating a small timer
* as well returning String representation of the CountDown
*
* */




import java.util.concurrent.TimeUnit;
import android.os.CountDownTimer;
import android.content.SharedPreferences;

public class GameCountdownTimer extends CountDownTimer {

    private String displayTime;/*String representation of the Countdown*/

    private long millisLeft;

    public boolean isPaused;

    //Constructor
    public GameCountdownTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        isPaused = false;
    }

    //Updates displayTime after every  "tick" accordingly
    @Override
    public void onTick(long millisUntilFinished) {
        millisLeft = millisUntilFinished;
        displayTime = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millisLeft) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisLeft)),
                TimeUnit.MILLISECONDS.toSeconds(millisLeft) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisLeft)));
    }

    @Override
    public void onFinish() {
        com.funnums.funnums.maingame.GameActivity.gameView.currentGame.isFinished = true;
        int score = com.funnums.funnums.maingame.GameActivity.gameView.currentGame.score;
        com.funnums.funnums.maingame.GameActivity.gameView.gameFinishedMenu.setScore(score);

        com.funnums.funnums.maingame.LeaderboardGameActivity.storeHighScore(score);
    }

    public long getMillisLeft()
    {
        return millisLeft;
    }

    //String representation of countdown
    @Override
    public String toString() {
        return displayTime;
    }

}
