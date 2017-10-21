/*
* Class GameCountdownTimer inherits from abstract class CountDownTimer. Methods are defined for creating a small timer
* as well returning String representation of the CountDown
*
* */


package com.funnums.funnums;

import java.util.concurrent.TimeUnit;
import android.os.CountDownTimer;
import android.content.Context;


public class GameCountdownTimer extends CountDownTimer {
    private Context context;
    private String displayTime;                         /*String representation of the Countdown*/

    //Constructor
    public GameCountdownTimer(Context context,long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        this.context = context;
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
