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

public class HowToPopUp extends Activity {
    static final public String TAG = "PopUp";

    final private double POPUP_XSCREEN_PERCENT_USED = 0.80;   /*80%*/
    final private double POPUP_YSCREEN_PERCENT_USED = 0.80;   /*80%*/

    //Screen variables
    private int screenX;
    private int screenY;

    //Image
    private int currentImageIndex;
    private String game_type;
    private int [] imagesUsed;

    //PopUp View Resources
    //private int numberOfTips;
    private Button nextTipButton;
    private Button previousTipButton;
    private RelativeLayout rl;

    //Image Resources
    //TODO set proper images
    private final int [] bubbleImages  = {R.drawable.owl_popup, R.drawable.owl_popup2, R.drawable.owl_popup3};
    private final int [] balloonImages  = {R.drawable.owl_popup2, R.drawable.owl_popup3, R.drawable.owl_popup};
    private final int [] owlImages  = {R.drawable.owl_popup3, R.drawable.owl_popup, R.drawable.owl_popup2};

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Set Pop-Up layout
        setContentView(R.layout.pop_up_window);

        //Save View Elements
        nextTipButton = (Button) findViewById(R.id.pop_up_button_n);
        previousTipButton = (Button) findViewById(R.id.pop_up_button_p);
        rl = (RelativeLayout) findViewById(R.id.pop_up_window);;

        //Get Proper Game Type
        Bundle extras = getIntent().getExtras();
        game_type = extras.getString("minigame");

        //Set Proper Image
        currentImageIndex = 0;
        imagesUsed = getImages();
        rl.setBackgroundResource(imagesUsed[currentImageIndex]);

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

        //New image if another one is available
        if ( currentImageIndex < imagesUsed.length - 1){
            rl.setBackgroundResource(imagesUsed[++currentImageIndex]);
        }

        //Disable next button at the last image
        if ( currentImageIndex == imagesUsed.length - 1){
            nextTipButton.setVisibility(View.INVISIBLE);
        }

        //Set previous button to visible if it not currently visible
        if (previousTipButton.getVisibility() == View.INVISIBLE){
            previousTipButton.setVisibility(View.VISIBLE);
        }

    }

    public void onPressedPreviousTip(View v) {
        Log.d(TAG, "Previous Tip Pressed");

        //New image if another one is available
        if ( currentImageIndex > 0){
            rl.setBackgroundResource(imagesUsed[--currentImageIndex]);
        }

        //Enable nextButton if it is not currently visible
        if (nextTipButton.getVisibility() == View.INVISIBLE){
            nextTipButton.setVisibility(View.VISIBLE);
        }

        //Disable previous button if at first image
        if ( currentImageIndex == 0){
            previousTipButton.setVisibility(View.INVISIBLE);
        }
    }

    //Return Proper Array of Images
    private int [] getImages() {
        int [] imageArr = null;

        switch (game_type) {
            case ("bubble"):
                imageArr = bubbleImages;
                break;
            case ("balloon"):
                imageArr = balloonImages;
                break;
            case ("owl"):
                imageArr = owlImages;
                break;
        }

        return imageArr;
    }


}
