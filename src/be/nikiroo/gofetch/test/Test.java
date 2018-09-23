package be.nikiroo.gofetch.test;

import be.nikiroo.utils.test.TestLauncher;

/**
 * Tests for GoFetch.
 * 
 * @author niki
 */
public class Test extends TestLauncher {
	public Test(String[] args) {
		super("GoFetch", args);
		addSeries(new TestLWN(args));
	}

	public static void main(String[] args) {
		System.exit(new Test(args).launch());
	}
}