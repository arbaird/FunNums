package com.funnums.funnums.maingame;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.funnums.funnums.R;

/**
 * Created by alanking on 11/27/17.
 */

public class SettingsActivity extends AppCompatActivity {
    public static final String TAG = "Settings";
    private static RadioGroup rgroup;
    private static RadioButton high,high_mid,mid,mid_low,noSound;
    static SharedPreferences prefs;
    private SoundPool soundPool;
    private int bubblePopId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Beginning of onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        Context context = GameActivity.gameView.context;
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
        bubblePopId = soundPool.load(context,R.raw.bubble,1);

        //prefs = getSharedPreferences("HighScore", MODE_PRIVATE);
        //final SharedPreferences.Editor editor = prefs.edit();

        rgroup = (RadioGroup)findViewById(R.id.volumeGroup);
        rgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                RadioButton rb = (RadioButton) findViewById(i);
                switch (i) {
                    case R.id.high:
                        soundPool.play(bubblePopId, 1, 1, 1, 0, 1);
                        break;
                    case R.id.mid_high:
                        soundPool.play(bubblePopId, 0.75f, 0.75f, 1, 0, 1);
                        break;
                    case R.id.mid:
                        soundPool.play(bubblePopId, 0.5f, 0.5f, 1, 0, 1);
                        break;
                    case R.id.mid_low:
                        soundPool.play(bubblePopId, 0.25f, 0.25f, 1, 0, 1);
                        break;
                    case R.id.noSound:
                        soundPool.play(bubblePopId, 0, 0, 1, 0, 1);
                        break;
                }
            }
        });
        Log.d(TAG, "End of onCreate");
    }

}
