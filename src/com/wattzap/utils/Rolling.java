package com.wattzap.utils;

/**
 * Rolling Average Calculator
 * 
 * @author David George
 * @date 11 June 2013
 */
public class Rolling {

    private int size;
    private double total = 0d;
    private int index = 0;
    private int count = 0;
    private double samples[];

    public Rolling(int size) {
        this.size = size;
        samples = new double[size];
        for (int i = 0; i < size; i++) samples[i] = 0d;
    }

    public double add(double x) {
        total -= samples[index];
        samples[index] = x;
        total += x;
        if (++index == size) {
        	index = 0; // cheaper than modulus
        }
        count++;
        
        return getAverage();
    }

    public double getAverage() {
    	if (count < size) {
    		return total / count;	// while it is filling up initially
    	} else {
    		return total / size;
    	}
    }   
}