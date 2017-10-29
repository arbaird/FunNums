package com.funnums.funnums.maingame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.DialogInterface;


public class MainMenuActivity extends AppCompatActivity {
    private static final String TAG = "Main Menu";

    String logTag = "MainMenu";

    // This is the entry point to our game
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Here we set our UI layout as the view
        setContentView(com.funnums.funnums.R.layout.activity_main_menu);

        //get the stored data on this phone
        final SharedPreferences prefs = getSharedPreferences("HighScore", MODE_PRIVATE);
        //get the editor so we can update stored data, if needed
        final SharedPreferences.Editor editor = prefs.edit();
        String userName = prefs.getString("user_name", null);
        //if there is no username, prompt player to enter one
        if (userName == null) {
            //edit text is the text field that the user will enter their name into
            EditText input = new EditText(this);
            input.setId(1000);
            //alert dialog is popup that asks for username, needs the following boiler plate code
            //to store username when it is entered
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(input).setTitle("Enter your username!")
                    .setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    EditText theInput = (EditText) ((AlertDialog) dialog)
                                            .findViewById(1000);
                                    String enteredText = theInput.getText()
                                            .toString();
                                    //if the player entered a name, store it so we don't ask again later
                                    if (!enteredText.equals("")) {
                                        editor.putString("user_name",
                                                enteredText);
                                        editor.commit();
                                    }
                                }


                            })
                            .setCancelable(false)
                            .create();
            dialog.show();
        }

        // Prepare to highest Score. We don't need this yet, we can keep it for later when we implement scoring

        // Load fastest time
        // if not available our high score = 1000000
        long highScore = prefs.getLong("HighScore", 1000000);

        // Get a refference to the TextView in our layout
        final TextView textFastestTime = (TextView)findViewById(com.funnums.funnums.R.id.textHiScore);
        // Put the high score in our TextView
        textFastestTime.setText("Your High Score:" + highScore);


    }

    public void onPressedSelect(View v) {
        Log.d(TAG, "[SELECT GAME] pressed");
        // Create a new Intent object
        Intent i = new Intent(this, SelectGameActivity.class);
        // Start our GameActivity class via the Intent
        startActivity(i);
    }

    public void onPressedScores(View v) {
        Log.d(TAG, "[SEE LEADEBOARD] pressed");
        // Create a new Intent object
        Intent i = new Intent(this, LeaderboardGameActivity.class);
        // Start our GameActivity class via the Intent
        startActivity(i);
    }

    /*Empty for now*/
    public void onPressedSettings(View v) {
        Log.d(TAG, "[SETTINGS] pressed");
    }


}