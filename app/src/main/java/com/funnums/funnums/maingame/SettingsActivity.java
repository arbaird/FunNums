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
    //for volume radio group
    private static RadioGroup volumeGroup;
    //for storing volume data that persists even after app is closed
    private SharedPreferences prefs;
    //for playing sample sound
    private SoundPool soundPool;
    private int owlHootId;
    private float volume;
    private RadioButton currentVolume;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Beginning of onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        Context context = getApplicationContext();
        //initialize and load sample sound file
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
        owlHootId = soundPool.load(context,R.raw.owlhoot,1);

        //get the stored data on this phone
        prefs = getSharedPreferences("HighScore", MODE_PRIVATE);
        //get the current volume data
        volume=prefs.getFloat("volume", 1);

        //check the radio button that corresponds to the current volume settings
        if(volume==1){
            currentVolume=(RadioButton)findViewById(R.id.high);
            currentVolume.setChecked(true);
        }else if(volume==0.75f){
            currentVolume=(RadioButton)findViewById(R.id.mid_high);
            currentVolume.setChecked(true);
        }else if(volume==0.5f){
            currentVolume=(RadioButton)findViewById(R.id.mid);
            currentVolume.setChecked(true);
        }else if(volume==0.25f){
            currentVolume=(RadioButton)findViewById(R.id.mid_low);
            currentVolume.setChecked(true);
        }else if(volume==0){
            currentVolume=(RadioButton)findViewById(R.id.noSound);
            currentVolume.setChecked(true);
        }

        //get the editor so we can update stored data
        final SharedPreferences.Editor editor = prefs.edit();

        //gets the radio group for volume settings
        volumeGroup = (RadioGroup)findViewById(R.id.volumeGroup);
        //updates whenever a new radio button is checked
        volumeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                RadioButton rButton = (RadioButton) findViewById(checkedId);
                switch (checkedId) {
                    //cases for each radio button selection
                    case R.id.high:
                        playAndStoreSound(editor,1);
                        break;
                    case R.id.mid_high:
                        playAndStoreSound(editor,0.75f);
                        break;
                    case R.id.mid:
                        playAndStoreSound(editor,0.5f);
                        break;
                    case R.id.mid_low:
                        playAndStoreSound(editor,0.25f);
                        break;
                    case R.id.noSound:
                        playAndStoreSound(editor,0);
                        break;
                }
            }
        });
        Log.d(TAG, "End of onCreate");
    }

    /*
        play a sample sound with corresponding volume and store volume data into sharedPreferences
        volume is in float and ranges from 0(min) to 1(max)
     */
    public void playAndStoreSound(SharedPreferences.Editor editor,float volume){
        soundPool.play(owlHootId, volume, volume, 1, 0, 1);
        editor.putFloat("volume", volume);
        editor.apply();
    }

}
