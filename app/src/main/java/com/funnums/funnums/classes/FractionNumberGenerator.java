package com.funnums.funnums.classes;

import android.util.Log;
import java.util.*;
import java.util.Random;

/**
 *  FractionNumberGenerator class defines module for Fraction Balloon game that contains methods to
 *  generate a new target fraction balloon, as well as fraction balloons to compare the target with.
 *
 *  When creating an object of type FractionNumberGenerator, integer argument 'n' must be passed to constructor such that:
 *      - n = 0 for game involving LT or GT
 *      - n = 1 for game involving LEQ or GEQ
 *      - n = 2 for game involving EQ
 */

public class FractionNumberGenerator {
    static final public String TAG_F = "Fraction";

    Fraction target;                            /*Current fraction target*/
    int gType;                                  /*Current game type, must be one of the following: {0, 1, 2} */
    Random rd;                                  /*Random Object*/

    //Hash table containing ArrayLists with pre defined equivalent fractions, decimal value of fraction used as hash key
    Hashtable <Double, ArrayList<Fraction>> eqivFracs;
    //Array containing the different hash keys
    private final double fracKeys [] = {1.0/2, 1.0/4, 1.0/3, 1.0/5, 4.0/5, 2.0/3, 2.0/5, 3.0/4, 3.0/5};


    //Constructor takes value type (0,1,2) to specify current game mode.
    public FractionNumberGenerator(int type){
        this.gType = type;
        eqivFracs = new Hashtable<>();
        rd = new Random();

        //Define arrays of known equivalent fractions
        Fraction pointFive [] =  {new Fraction(1,2), new Fraction(2,4), new Fraction(3,6), new Fraction(4,8),
                                    new Fraction(5, 10), new Fraction(6,12) };
        Fraction pointTwoFive [] =  {new Fraction(1,4), new Fraction(2,8), new Fraction(3,12), new Fraction(4,16)};
        Fraction pointThree [] =  {new Fraction(1,3), new Fraction(2,6), new Fraction(3,9), new Fraction(4,12),
                                    new Fraction(5,15) };
        Fraction pointTwo [] =  {new Fraction(1,5), new Fraction(2,10), new Fraction(3,15)};
        Fraction pointEight [] =  {new Fraction(4,5), new Fraction(8,10), new Fraction(12,15) };
        Fraction pointSixSix [] =  {new Fraction(2,3), new Fraction(4,6), new Fraction(6,9), new Fraction(8,12)};
        Fraction pointFour [] =  {new Fraction(2,5), new Fraction(4,10), new Fraction(6,15)};
        Fraction pointSevenFive [] =  {new Fraction(3,4), new Fraction(6,8), new Fraction(9,12)};
        Fraction pointSix [] =  {new Fraction(3,5), new Fraction(6,10), new Fraction(9,15)};

        //Create ArrayLists from arrays
        ArrayList<Fraction> pointFiveL = new ArrayList<>(Arrays.asList(pointFive));
        ArrayList<Fraction> pointTwoFiveL = new ArrayList<>(Arrays.asList(pointTwoFive));
        ArrayList<Fraction> pointThreeL = new ArrayList<>(Arrays.asList(pointThree));
        ArrayList<Fraction> pointTwoL = new ArrayList<>(Arrays.asList(pointTwo));
        ArrayList<Fraction> pointEightL = new ArrayList<>(Arrays.asList(pointEight));
        ArrayList<Fraction> pointSixSixL = new ArrayList<>(Arrays.asList(pointSixSix));
        ArrayList<Fraction> pointFourL = new ArrayList<>(Arrays.asList(pointFour));
        ArrayList<Fraction> pointSevenFiveL = new ArrayList<>(Arrays.asList(pointSevenFive));
        ArrayList<Fraction> pointSixL = new ArrayList<>(Arrays.asList(pointSix));

        //Add arrays lists to hash table with their corresponding key.
        eqivFracs.put(new Double(1.0/2),pointFiveL);
        eqivFracs.put(new Double(1.0/4),pointTwoFiveL);
        eqivFracs.put(new Double(1.0/3),pointThreeL);
        eqivFracs.put(new Double(1.0/5),pointTwoL);
        eqivFracs.put(new Double(4.0/5),pointEightL);
        eqivFracs.put(new Double(2.0/3),pointSixSixL);
        eqivFracs.put(new Double(2.0/5),pointFourL);
        eqivFracs.put(new Double(3.0/4),pointSevenFiveL);
        eqivFracs.put(new Double(3.0/5),pointSixL);

        //Call to generate appropriate target
        initGenerator();
    }

    //Initializes the target for the appropriate game mode
    private void initGenerator(){

        if (gType == 0){             //Condition involving equality
            target = getRandomFrac();
        } else if (gType == 1 || gType == 2){
            target = getKnownFrac();
        } else {
            throw new IllegalArgumentException("Argument type must be a 0, 1 or a 2");
        }
    }

    //Return current target
    public Fraction getTarget(){
        return target;
    }

    //Start a new game and define a new target, game type must be specified (0,1,2)
    public void new_game(int type){
        gType = type;
        initGenerator();
    }

    //Get a new balloon fraction to compare target with
    public Fraction getNewBalloon(){

        //By default generate a random fraction (Might change based on game type and "chance"
        Fraction balloon = getRandomFrac();
        int chance = -1;

        //Current game condition involving equality
        if (gType != 0) {

            //Current game is of type LEQ or GEQ get a 1 in 10 chance to throw a known equivalent function
            if (gType == 1){
                chance = rd.nextInt(10);
            }

            //Current game is of type EQget a 1 in 5 chance to throw a known equivalent function
            if (gType == 2){
                chance = rd.nextInt(5);
            }

            if (chance == 0){

                //Get a reference to list of equivalent fractions
                //Log.d(TEST_LOG_TAG, "Target key: " + String.valueOf(target.get_key()));
                ArrayList<Fraction> fractions = eqivFracs.get(target.get_key());

                //Get a different equivalent fraction than that of the target
                do {
                    //Get a random equivalent Fraction
                    balloon = fractions.get( rd.nextInt(fractions.size()) ) ;

                } while ( balloon.get_denom() == target.get_denom() );

            }

        }

        return balloon;
    }

    //*********************************PRIVATE METHODS****************

    //Return a new random Fraction object within the specified range
    private Fraction getRandomFrac(){
        int denom = rd.nextInt(11)+ 2;          //Between 2 and 12
        int nume = rd.nextInt(denom-1)+ 1;      //Between 1 and denominator - 1

        return new Fraction(nume, denom);
    }

    //Return a new known Fraction object from pre defined Fractions
    private Fraction getKnownFrac(){

        //Get the a random index of fracKeys array, and get that key
        int index = rd.nextInt( fracKeys.length );
        //Log.d(TEST_LOG_TAG, "Index: " + String.valueOf(index));
        Double key = new Double(fracKeys[index]);
        //Log.d(TEST_LOG_TAG, "Double value: " + String.valueOf(key));

        //Get reference to corresponding array of fractions matching the key
        ArrayList<Fraction> fractions = eqivFracs.get(key);
        //Log.d(TEST_LOG_TAG, "Is it Frac Array: " + String.valueOf(fractions instanceof Fraction[]));

        //Randomly get index to get a known expression from chosen ArrayList
        index = rd.nextInt( fractions.size() );

        return fractions.get(index);
    }

    //Test generates 10 balloons
    public void runTest(){

        Log.d(TAG_F, "Test of type: " + String.valueOf(gType));

        Log.d(TAG_F, "Target: " + String.valueOf(target));
        for (int i = 0; i < 10; i++) {
            Log.d(TAG_F, "Balloon: " + String.valueOf(getNewBalloon()));
        }
    }


}
