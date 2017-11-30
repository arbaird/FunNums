package com.funnums.funnums.classes;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.funnums.funnums.R;

/**
 * Created by cesar on 11/28/2017.
 *
 * Popup Class used to display how to play instructions for each game.
 */

public class PopUp extends Activity {
    static final public String TAG = "PopUp";

    private int screenX;
    private int screenY;

    private double POPUP_XSCREEN_PERCENT_USED = 0.80;   /*80%*/
    private double POPUP_YSCREEN_PERCENT_USED = 0.90;   /*75%*/

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Set Pop-Up layout
        setContentView(R.layout.pop_up_window);

        //Set proper image
        Bundle extras = getIntent().getExtras();
        String game_type = extras.getString("minigame");
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.pop_up_window);

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
    }
}
