package edu.nmsu.cs.circles;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Implements a base abstract class for a circle. Everything except a intersects() method is
 * implemented here. Subclass must implement the intersects() method.
 **/
public class Test2Runner{
	
	public static void main(String[] args){
		Result result = JUnitCore.runClasses(Circle2Test.class);
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.toString());
		}
		System.out.println(result.wasSuccessful());
	}

}
