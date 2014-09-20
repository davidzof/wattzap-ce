/* This file is part of Wattzap Community Edition.
 *
 * Wattzap Community Edtion is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wattzap Community Edition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Wattzap.  If not, see <http://www.gnu.org/licenses/>.
*/
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