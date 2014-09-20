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
package com.wattzap.model.dto;

import java.util.ArrayList;
import java.util.Collection;
/* 
* @author David George (c) Copyright 2013
* @date 19 August 2013
*/
public class TrainingData {
	private ArrayList<TrainingItem> training = new ArrayList<TrainingItem>();
	private boolean hr = false;
	private boolean pwr  = false;
	private boolean cdc = false;
	private String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isHr() {
		return hr;
	}

	public void setHr(boolean hr) {
		this.hr = hr;
	}

	public boolean isPwr() {
		return pwr;
	}

	public void setPwr(boolean pwr) {
		this.pwr = pwr;
	}

	public boolean isCdc() {
		return cdc;
	}

	public void setCdc(boolean cdc) {
		this.cdc = cdc;
	}

	public void addItem(TrainingItem item) {
		training.add(item);
	}

	public Collection<TrainingItem> getTraining() {
		return training;
	}
	
	public TrainingItem getPreviousItem() {
		return training.get(training.size()-1);
	}

}
