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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.view.View;
/**
 * Created by alanking on 11/27/17.
 */

public class SettingsActivity extends AppCompatActivity implements OnSeekBarChangeListener {
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
        volume=prefs.getFloat("volume", 1)*100;

        //initialize the seek bar
        SeekBar sb = (SeekBar)findViewById(R.id.slider);
        //max value is 100, somehow that's not the default
        sb.setMax(100);
        //set progress to the value the user last stored
        sb.setProgress((int)volume);
        //display the precentage
        TextView tv = (TextView)findViewById(R.id.percent);
        tv.setText(Integer.toString((int)volume)+"%");
        //allow this class to repsond when the player changes the seek bar
        sb.setOnSeekBarChangeListener(this);
        //set the username string that appears on the screen
        setUserNameString();
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

    /*
        Display the volume percentage as the player scrolls the seek bar
     */
    @Override
    public void onProgressChanged(SeekBar v, int progress, boolean isUser) {

        TextView tv = (TextView)findViewById(R.id.percent);
        tv.setText(Integer.toString(progress)+"%");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    //Auto-generated method stub, needed to implement OnSeekBarChangeListener

    }

    /*
        Play the owl sound effect at the volume level the user selected so they can sample the volume
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        float volume = seekBar.getProgress() / 100f;
        //get the editor so we can update stored data
        SharedPreferences.Editor editor = prefs.edit();
        playAndStoreSound(editor, volume);
    }

    /*
        Prepare the alert dialog from the main menu that prompts the user for a new username
     */
    public void setUserName(View v){
        MainMenuActivity m = new MainMenuActivity();
        m.setContext(this);
        m.makeAlert("Enter a user name");
    }

    /*
        set the username that is displayed on the Settings menu
     */
    public void setUserNameString(){
        TextView userTV = (TextView)findViewById(R.id.username);
        userTV.setText(prefs.getString("user_name", ""));
    }

}
