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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

/**
 * (c) 2013 David George / Wattzap.com
 * 
 * @author David George
 * @date 12 November 2013
 */
public class MenuItem extends JMenuItem implements ActionListener {
	private static final long serialVersionUID = -9184031206677276673L;
	Messages message;
	
	public  MenuItem(Messages message, String s) {
		super(s);
		this.message = message;
		addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		MessageBus.INSTANCE.send(message, null);
	}
}
