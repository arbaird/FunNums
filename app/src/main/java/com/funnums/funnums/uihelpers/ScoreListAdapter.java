package com.funnums.funnums.uihelpers;

/**
 * This class is used to adapt the data in our code to the listview on the screen to display player scores.
 * Essentially, an adapter is the middle man between our arraylist holding PlayerScores and the
 * ListView that is on the screen, defined in xml file
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

import com.funnums.funnums.classes.PlayerScore;


public class ScoreListAdapter extends ArrayAdapter<PlayerScore> {

    //resource is the listview id we want to attach this adapter to
    int resource;
    Context context;

    public ScoreListAdapter(Context context, int resource, List<PlayerScore> items) {
        super(context, resource, items);
        this.resource = resource;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //the updated view we will return
        LinearLayout newView;

        //get items in reverse order, since firebase only returns data in ascending order and
        //we want descending to get top 10 scores.
        //getItem(index) returns item at given index in an arraylist
        PlayerScore score = getItem(getCount() - 1 - position);

        // Inflate a new view if necessary.
        // Not super certain why this is needed, but most adapters have this if else statment.
        if (convertView == null) {
            newView = new LinearLayout(getContext());
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi.inflate(resource,  newView, true);
        }
        else {
            newView = (LinearLayout) convertView;
        }

        //Get references to the Text for both the username and the user score
        TextView userNameText = (TextView) newView.findViewById(com.funnums.funnums.R.id.playerName);
        TextView userScore = (TextView) newView.findViewById(com.funnums.funnums.R.id.playerScore);
        //set the user name and score TextViews on screen to the values stored in the PlayerScore object
        userNameText.setText(score.name);
        userScore.setText(String.valueOf(score.hiScore));

        //need to return a view for getView to be ovveridden
        return newView;
    }
}
