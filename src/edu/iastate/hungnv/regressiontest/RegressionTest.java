package edu.iastate.hungnv.regressiontest;

import java.util.ResourceBundle;

import com.caucho.quercus.env.ArrayValue;
import com.caucho.quercus.env.ArrayValueImpl;
import com.caucho.quercus.env.ConstStringValue;
import com.caucho.quercus.env.Value;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.debug.OutputViewer;
import edu.iastate.hungnv.debug.ValueViewer;
import edu.iastate.hungnv.shadow.Env_;
import edu.iastate.hungnv.util.FileIO;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.wpplugins.Plugin;
import edu.iastate.hungnv.wpplugins.PluginManager;
import edu.iastate.hungnv.wpplugins.TestConfig;
import edu.iastate.hungnv.wpplugins.TestManager;

/**
 * 
 * @author HUNG
 *
 */
public class RegressionTest {
	
	/**
	 * This properties file in src/resources contains input/output file paths for the regression test. 
	 */
	public static final String pathsPropertiesFile = "paths-10plugins-22tests";
	
	/**
	 * Static instance of RegressionTest
	 */
	public static RegressionTest inst = new RegressionTest();
	
	/**
	 * ADHOC This file is used as a means of communication with the client browser
	 * to identify what test config is being executed
	 */
	public static final String testConfigFile 	= "C:\\Users\\10123_000\\Documents\\NetBeansProjects\\TestVarex\\src\\java\\resources\\temp\\testconfig";
	
	/**
	 * This variable must be set before the concrete plugins are loaded.
	 * @see edu.iastate.hungnv.regressiontest.RegressionTest.start()
	 */
	private TestConfig testConfig = null;
	
	/**
	 * This method is called when the Env starts.
	 * Used to set the testConfig.
	 */
	public void start() {
		if (Env_.INSTRUMENT) {
			testConfig = null;
		}
		else {
			String binaryString = FileIO.readStringFromFile(testConfigFile);
			testConfig = new TestConfig(binaryString);
		}
	}
	
	/*
	 * Load plugins for the following PHP code:
	 *    $active_plugins = __PLUGINS__();
	 */

	public Value loadPlugins() {
		if (Env_.INSTRUMENT)
			return loadChoicePlugins();
		else
			return loadConcretePlugins();
	}
	
	public Value loadChoicePlugins() {
		ArrayValue array = new ArrayValueImpl();
		
		for (Plugin plugin : PluginManager.inst.getPlugins()) {
			Constraint pluginConstraint = plugin.getConstraint();
			String pluginPath = plugin.getPath();
			
			Value choicePlugin = MultiValue.createChoiceValue(pluginConstraint, new ConstStringValue(pluginPath), new ConstStringValue("")); 
			array.put(choicePlugin);
		}
		
		return array;
	}
	
	public Value loadConcretePlugins() {
		ArrayValue array = new ArrayValueImpl();
	
		for (Plugin activePlugin : testConfig.getActivePlugins()) {
			String pluginPath = activePlugin.getPath();
			array.put(new ConstStringValue(pluginPath));
		}
		
		return array;
	}
	
	/*
	 * Generate outputs & heaps
	 */
	
	public void generateOutputs(OutputViewer outputViewer) {
		if (Env_.INSTRUMENT)
			generateDerivedOutputs(outputViewer);
		else
			generateExpectedOutput(outputViewer);
	}
	
	public void generateHeaps(ValueViewer valueViewer) {
		if (Env_.INSTRUMENT)
			generateDerivedHeaps(valueViewer);
		else
			generateExpectedHeap(valueViewer);
	}

	public void generateDerivedOutputs(OutputViewer outputViewer) {
		for (TestConfig testConfig : TestManager.inst.getTestConfigs()) {
			Constraint constraint = testConfig.getConstraint();
			outputViewer.writeToTxtFile(computeDerivedOutputFileName(testConfig), constraint);
		}
	}
	
	public void generateExpectedOutput(OutputViewer outputViewer) {
		outputViewer.writeToTxtFile(computeExpectedOutputFileName(testConfig));
	}
	
	public void generateDerivedHeaps(ValueViewer valueViewer) {
		for (TestConfig testConfig : TestManager.inst.getTestConfigs()) {
			Constraint constraint = testConfig.getConstraint();
			valueViewer.writeToXmlFile(computeDerivedHeapFileName(testConfig), constraint);
		}
	}
	
	public void generateExpectedHeap(ValueViewer valueViewer) {
		valueViewer.writeToXmlFile(computeExpectedHeapFileName(testConfig));
	}
	
	/*
	 * Compute file names
	 */
	
	public static String computeDerivedOutputFileName(TestConfig testConfig) {
		return getPath("derivedOutputFile").replace("{BINARY-STRING}", testConfig.getBinaryString()).replace("{ACTIVE-PLUGINS-STRING}", testConfig.getActivePluginsString());
	}
	
	public static String computeExpectedOutputFileName(TestConfig testConfig) {
		return getPath("expectedOutputFile").replace("{BINARY-STRING}", testConfig.getBinaryString()).replace("{ACTIVE-PLUGINS-STRING}", testConfig.getActivePluginsString());
	}
	
	public static String computeDerivedHeapFileName(TestConfig testConfig) {
		return getPath("derivedHeapFile").replace("{BINARY-STRING}", testConfig.getBinaryString()).replace("{ACTIVE-PLUGINS-STRING}", testConfig.getActivePluginsString());
	}
	
	public static String computeExpectedHeapFileName(TestConfig testConfig) {
		return getPath("expectedHeapFile").replace("{BINARY-STRING}", testConfig.getBinaryString()).replace("{ACTIVE-PLUGINS-STRING}", testConfig.getActivePluginsString());
	}
	
	private static String getPath(String key) {
		ResourceBundle bundle = ResourceBundle.getBundle(pathsPropertiesFile);
		return bundle.getString(key);
	}
	
}
