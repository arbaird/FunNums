package com.funnums.funnums.classes;

/*
* Class GameCountdownTimer inherits from abstract class CountDownTimer. Methods are defined for creating a small timer
* as well returning String representation of the CountDown
*
* */




import java.util.concurrent.TimeUnit;
import android.os.CountDownTimer;

public class GameCountdownTimer extends CountDownTimer
{

    private String displayTime;/*String representation of the Countdown*/

    //Constructor
    public GameCountdownTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    //Updates displayTime after every  "tick" accordingly
    @Override
    public void onTick(long millisUntilFinished) {
        long millis = millisUntilFinished;
        displayTime = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    @Override
    public void onFinish() {
        /*Empty for now*/
    }

    //String representation of countdown
    @Override
    public String toString() {
        return displayTime;
    }

}
