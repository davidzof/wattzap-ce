package com.wattzap.model.dto;

import java.util.ArrayList;
import java.util.Collection;
/* 
* @author David George (c) Copyright 2013
* @date 19 August 2013
*/
public class TrainingData {
	ArrayList<TrainingItem> training = new ArrayList<TrainingItem>();
	boolean hr = false;
	boolean pwr  = false;
	boolean cdc = false;
	
	
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
