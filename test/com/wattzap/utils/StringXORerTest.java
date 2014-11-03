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

import org.junit.Test;

public class StringXORerTest {
	private static final String cryptKey = "CAFEBABE";
	
	@Test
	public void encodeString() {
		String s = StringXORer
				.encode("<html>WattzAp Turbo Trainer Software<br/><br/>Version: 2.2.1<br/>Date: 7th October<br/>(c) 2014, All rights reserved<br/>",
						cryptKey);
		System.out.println(s);
		s = StringXORer.decode(s, cryptKey);
		System.out.println(s);

	}

	@Test
	public void encodeRegistered() {
		String s = StringXORer.encode("Your software is registered",
				cryptKey);
		System.out.println(s);
		s = StringXORer.decode(s, cryptKey);
		System.out.println(s);

	}
	
	@Test
	public void encodeNotRegistered() {
		String s = StringXORer.encode("Your software is not registered",
				cryptKey);
		System.out.println(s);
		s = StringXORer.decode(s, cryptKey);
		System.out.println(s);

	}
	
	@Test
	public void encodeIncorrect() {
		String s = StringXORer.encode("Incorrect registration key",
				cryptKey);
		System.out.println(s);
		s = StringXORer.decode(s, cryptKey);
		System.out.println(s);

	}

	@Test
	public void error() {
		String s = StringXORer.encode("Error in registration key",
				cryptKey);
		System.out.println(s);
		s = StringXORer.decode(s, cryptKey);
		System.out.println(s);

	}
	
	@Test
	public void success() {
		String s = StringXORer.encode("Your software has been registered successfully",
				cryptKey);
		System.out.println(s);
		s = StringXORer.decode(s, cryptKey);
		System.out.println(s);

	}
	
	@Test
	public void eval() {
		String s = StringXORer.encode("You have %d minutes left to evaluate this software<br/>",
				cryptKey);
		System.out.println(s);
		s = StringXORer.decode(s, cryptKey);
		System.out.println(s);

	}

	@Test
	public void register() {
		String s = StringXORer.encode("Register",
				cryptKey);
		System.out.println(s);
		s = StringXORer.decode(s, cryptKey);
		System.out.println(s);

	}
	
	@Test
	public void about() {
		String s = StringXORer.encode("About WattzAp",
				cryptKey);
		System.out.println(s);
		s = StringXORer.decode(s, cryptKey);
		System.out.println(s);

	}
	
	@Test
	public void serial() {
		String s = StringXORer.encode("Serial Number: ",
				cryptKey);
		System.out.println(s);
		s = StringXORer.decode(s, cryptKey);
		System.out.println(s);

	}
	
	@Test
	public void debug() {
		String s = StringXORer.encode("Debug",
				cryptKey);
		System.out.println(s);
		s = StringXORer.decode(s, cryptKey);
		System.out.println(s);

	}

}
