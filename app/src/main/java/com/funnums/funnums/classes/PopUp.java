package com.funnums.funnums.classes;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.funnums.funnums.R;

/**
 * Created by cesar on 11/28/2017.
 *
 * Popup Class used to display how to play instructions for each game.
 */

public class PopUp extends Activity {
    static final public String TAG = "PopUp";

    final private double POPUP_XSCREEN_PERCENT_USED = 0.80;   /*80%*/
    final private double POPUP_YSCREEN_PERCENT_USED = 0.80;   /*80%*/

    private int screenX;
    private int screenY;
    private String game_type;
    private RelativeLayout rl;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Set Pop-Up layout
        setContentView(R.layout.pop_up_window);

        //Set proper image
        Bundle extras = getIntent().getExtras();
        game_type = extras.getString("minigame");
        rl = (RelativeLayout) findViewById(R.id.pop_up_window);

        switch (game_type){
            case("bubble"):
                rl.setBackgroundResource(R.drawable.owl_popup);
                break;
            case("balloon"):
                rl.setBackgroundResource(R.drawable.owl_popup2);
                break;
            case("owl"):
                rl.setBackgroundResource(R.drawable.owl_popup3);
                break;
        }

        //Set proper size of screen based on Screen Resolution
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        screenX = dm.widthPixels;
        screenY = dm.heightPixels;

        getWindow().setLayout(
                (int) (screenX* POPUP_XSCREEN_PERCENT_USED),
                (int) (screenY * POPUP_YSCREEN_PERCENT_USED)
        );

        Log.d(TAG, "PopUp window created");
    }

    public void onPressedNextTip(View v) {
        Log.d(TAG, "Next Tip Pressed");

        //TODO change to proper image and keep record if its the last image
        rl.setBackgroundResource(R.drawable.owl_popup2);

        //Set previous button to visible if it not currently visible
        Button previous_button = (Button) findViewById(R.id.pop_up_button_p);
        if (previous_button.getVisibility() == View.INVISIBLE){
            previous_button.setVisibility(View.VISIBLE);
        }

    }

    public void onPressedPreviousTip(View v) {
        Log.d(TAG, "Previous Tip Pressed");
        rl.setBackgroundResource(R.drawable.owl_popup);
        Button b = (Button) findViewById(R.id.pop_up_button_p);
        b.setVisibility(View.INVISIBLE);
    }


}
