package edu.iastate.hungnv.debug;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.caucho.quercus.env.BooleanValue;
import com.caucho.quercus.env.Value;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.MultiValue.IOperation;

/**
 * 
 * @author HUNG
 *
 */
public class Tester {
	
	/**
	 * Static instance of Tester
	 */
	public static Tester inst = new Tester();
	
	/**
	 * The output to test
	 */
	private static Value output;
	
	/**
	 * Tests the output 
	 */
	public void test(Value output) {
		Tester.output = output;
		
		JUnitCore junit = new JUnitCore();
		junit.addListener(new TextListener(System.out));
		
		Result result = junit.run(Tester.class);
		
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.toString());
		}
	}
	
	/*
	 * setUp and tearDown methods
	 */

	@BeforeClass
	public static void oneTimeSetUp() {
	}

	@AfterClass
	public static void oneTimeTearDown() {
	}

	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}
	
	/*
	 * Test framework
	 */
	
	public void assertContains(Value output, final String searchStr) {
		Value result = MultiValue.operateOnValue(output, new IOperation() {
			@Override
			public Value operate(Value value) {
				return value.toString().contains(searchStr) ? BooleanValue.TRUE : BooleanValue.FALSE;
			}
		});
		
		Constraint failedCondition = MultiValue.whenFalse(result);
		
		if (failedCondition.isSatisfiable())
			Assert.fail("Search string " + searchStr + " not found in configuration: " + failedCondition.toString());
	}
	
	/*
	 * Test methods
	 */

	@Test
	public void test_CALwea() {
		Value output_CALwea = MultiValue.simplify(output, Constraint.createAndConstraint(Constraint.createConstraint("CAL"), Constraint.createNotConstraint(Constraint.createConstraint("WEA"))));
		assertContains(output_CALwea, "June 2013");
	}
	
	@Test
	public void test_xxxWEA() {
		Value output_xxxWEA = MultiValue.simplify(output, Constraint.createConstraint("WEA"));
		assertContains(output_xxxWEA, "June 2013");
	}
	
	@Test
	public void test_xxxxxx() {
		Value output_xxxxxx = MultiValue.simplify(output);
		assertContains(output_xxxxxx, "June 2013");
	}
	
} 