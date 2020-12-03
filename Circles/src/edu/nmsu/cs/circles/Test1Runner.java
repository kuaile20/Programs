package edu.nmsu.cs.circles;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import edu.nmsu.cs.circles.Circle1Test;

/**
 * Implements a base abstract class for a circle. Everything except a intersects() method is
 * implemented here. Subclass must implement the intersects() method.
 **/
public class Test1Runner{
	
	public static void main(String[] args){
		Result result = JUnitCore.runClasses(Circle1Test.class);
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.toString());
		}
		System.out.println(result.wasSuccessful());
	}

}
