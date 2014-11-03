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

/**
 * Manages training data. Training data is is a list of Training Items.
 * 
 * @author David George (c) Copyright 2013
 * @date 19 August 2013
 */
public class TrainingData {
	private ArrayList<TrainingItem> training;
	private boolean hr = false;
	private boolean pwr = false;
	private boolean cdc = false;
	private String name;
	private int currentPoint;

	public TrainingData() {
		currentPoint = 0;
		training = new ArrayList<TrainingItem>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Does this Training Data include heart rate targets?
	 * 
	 * @return - true heart rate training
	 */
	public boolean isHr() {
		return hr;
	}

	public void setHr(boolean hr) {
		this.hr = hr;
	}

	/**
	 * Does this Training Data include power targets?
	 * 
	 * @return - true power training
	 */
	public boolean isPwr() {
		return pwr;
	}

	public void setPwr(boolean pwr) {
		this.pwr = pwr;
	}

	/**
	 * Does this Training Data include cadence targets?
	 * 
	 * @return true - cadence training
	 */
	public boolean isCdc() {
		return cdc;
	}

	public void setCdc(boolean cdc) {
		this.cdc = cdc;
	}

	public void addItem(TrainingItem item) {
		training.add(item);
	}

	/**
	 * Get the trainings as a collection
	 * 
	 * @return
	 */
	public Collection<TrainingItem> getTraining() {
		return training;
	}

	/**
	 * Sees if we need to fetch the next training item. The distance covered by
	 * the rider is greater than the next training item.
	 * 
	 * @param distance
	 *            Current distance covered
	 * @return true - ready to fetch next Training Item
	 */
	public boolean isNext(double distanceMeters) {
		if (currentPoint + 1 < training.size()
				&& distanceMeters > training.get(currentPoint + 1).getDistanceMeters()) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the next training item relative to distance. The current training
	 * item is the one immediately prior to the current rider distance
	 */
	public TrainingItem getNext(double distanceMeters) {

		TrainingItem current = training.get(currentPoint);
		while (distanceMeters > current.getDistanceMeters()
				&& (currentPoint + 1) < training.size()) {
			// we have more training items
			TrainingItem next = training.get(currentPoint + 1);
			if (distanceMeters < next.getDistanceMeters()) {
				break;
			}
			current = next;
			currentPoint++;
		} // while

		// set the item description
		if ((currentPoint + 1) < training.size()) {
			current.setDescription("To "
					+ String.format("%.2f", training.get(currentPoint + 1)
							.getDistanceMeters()/1000) + " km");
		} else {
			current.setDescription("Final segment ");
		}

		return current;
	}
}