package com.funnums.funnums.classes;

//Object of type fraction defined. No actual fraction math supported, only used for representation
public class Fraction{

    int numerator;
    int denominator;

    public Fraction(int n, int d){
        this.numerator = n;
        this.denominator = d;
    }

    //Returns decimal value of the fraction as a Double object
    public Double get_key(){
        return (double) numerator / denominator;
    }

    //Denominator is returned, used to differentiate one equivalent fraction from one another
    public int get_denom(){
        return denominator;
    }

    @Override
    public String toString(){
        return numerator + "/" + denominator;
    }

}
