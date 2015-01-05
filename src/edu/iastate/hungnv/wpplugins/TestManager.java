package edu.iastate.hungnv.wpplugins;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import edu.iastate.hungnv.regressiontest.RegressionTest;
import edu.iastate.hungnv.util.FileIO;

/**
 * 
 * @author HUNG
 *
 */
public class TestManager {
	
	private ArrayList<TestConfig> testConfigs;
	
	/**
	 * Static instance of TestManager
	 */
	public static TestManager inst = new TestManager();
	
	/**
	 * Constructor
	 */
	public TestManager() {
		this.testConfigs = readTestConfigs();
	}
	
	private ArrayList<TestConfig> readTestConfigs() {
		ResourceBundle bundle = ResourceBundle.getBundle(RegressionTest.pathsPropertiesFile);
		String testsFile = bundle.getString("testsFile");
		
		ArrayList<TestConfig> testConfigs = new ArrayList<TestConfig>();
		
		for (String binaryString : FileIO.readLinesFromFile(testsFile)) {
			TestConfig testConfig = new TestConfig(binaryString);
			testConfigs.add(testConfig);
		}
		
		return testConfigs;
	}
	
	/*
	 * Getters & setters
	 */
	
	public List<TestConfig> getTestConfigs() {
		return new ArrayList<TestConfig>(testConfigs);
	}

}
