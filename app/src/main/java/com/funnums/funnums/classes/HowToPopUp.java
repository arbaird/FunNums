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
    private Button nextTipButton;
    private Button previousTipButton;
    private RelativeLayout rl;

    //Image Resources
    private final int [] bubbleImages  = { R.drawable.bubble1, R.drawable.bubble2,
                        R.drawable.bubble3, R.drawable.bubble4, R.drawable.bubble5 };
    private final int [] balloonImages  = { R.drawable.balloon1, R.drawable.balloon2,
                        R.drawable.balloon3, R.drawable.balloon4, R.drawable.balloon5 };
    private final int [] owlImages  = { R.drawable.owl1, R.drawable.owl2, R.drawable.owl3,
                        R.drawable.owl4, R.drawable.owl5 };

    /*Generates a new pop up activity with the proper background images (How to play each game)*/
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

    }

    /* Next_button event handling updates the background picture if there are more pictures to show
    * Disables next button if last image is being shown, and reveals previous button if needed.*/
    public void onPressedNextTip(View v) {

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

    /* Previous_button event handling going back to previous pictures shown if there are any
    * Disables previous button if first image is being shown, and reveals next button if needed.*/
    public void onPressedPreviousTip(View v) {

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

    //Return Proper Array of Images based on the game type passed as extra info by the event handler
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
