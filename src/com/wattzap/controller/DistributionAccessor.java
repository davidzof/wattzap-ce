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
package com.wattzap.controller;

import com.wattzap.model.dto.Telemetry;

/**
 * Used by distribution graphs to determine graph key
 * 
 * @author David George (c) Copyright 17 January 2014
 * @date 17 April 2014
 */
public abstract class DistributionAccessor {
	public int scale = 0;
	public boolean keepZeroes = true;

	/**
	 * 
	 * @param t
	 * @return value or -1 if it should be ignored
	 */
	public abstract int getKey(Telemetry t);
	
	/**
	 * Typical case
	 * 
	 * @param v
	 * @return
	 */
	protected int getKey(int v) {
		if (!keepZeroes && v < scale) {
			return -1;
		}
		return (int) v / scale;

	}
	public String getValueLabel(int v) {
		return "" + (v * scale) + " - " + ((v * scale) + scale);
	}

	public void setBucketSize(int scale) {
		this.scale = scale;
	}
	
	public void setKeepZeroes(boolean kz) {
		this.keepZeroes = kz;
	}
}