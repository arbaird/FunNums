package com.funnums.funnums.classes;

import java.io.Serializable;

/**
 * Objects that will be stored by Firebase. Contains the username and score, as well as uniqueID,
 * in case we need to access a specific user by id. Implements Serializable so data can be sent
 * and received in JSON format. This is required by Firebase
 */

public class PlayerScore implements Serializable {
    public String name;
    public int hiScore;

    public String scoreID;

    PlayerScore() {
        this.name = "";
    }

    PlayerScore(String name, int hiScore) {
        this.name = name;
        this.hiScore = hiScore;
    }

    @Override
    public String toString() {
        return name + ": " + hiScore;
    }
}
