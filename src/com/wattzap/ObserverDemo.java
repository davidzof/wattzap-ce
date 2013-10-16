package com.wattzap;

import java.util.Observable;
import java.util.Observer;

/**
 * A simple demo of Observable->Observer
 * 
 * @author Ian Darwin
 * @version $Id: ObservDemo.java,v 1.3 2003/12/29 19:52:22 ian Exp $
 */
public class ObserverDemo extends Object {
	MyView view;

	MyModel model;

	public ObserverDemo() {

		view = new MyView();

		model = new MyModel();
		// view observes model
		model.addObserver(view);

	}

	public static void main(String[] av) {
		ObserverDemo me = new ObserverDemo();
		me.demo();
	}

	public void demo() {
		for (int i = 0; i < 100; i++) {
			model.changeSomething();
		}
	}

	/** The Observer normally maintains a view on the data */
	class MyView implements Observer {
		/** For now, we just print the fact that we got notified. */
		public void update(Observable obs, Object x) {
			MyModel m = (MyModel) x;
			System.out.println("updated(" + obs + "," + m.getSpeed() + ");");
		}
	}

	/** The Observable normally maintains the data */
	class MyModel extends Observable {
		private int speed = 0;;

		public void changeSomething() {
			// Notify observers of change
			speed++;
			setChanged();
			this.notifyObservers(this);

		}

		public int getSpeed() {
			return speed;
		}
	}
}