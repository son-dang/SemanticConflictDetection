package edu.iastate.hungnv.debug;

import com.caucho.quercus.env.CompiledConstStringValue;
import com.caucho.quercus.env.ConstStringValue;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.StringValue;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.env.Var;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.util.FileIO;
import edu.iastate.hungnv.value.MultiValue;

/**
 * 
 * @author HUNG
 *
 */
public class OutputViewer {
	
	public static final String txtFile 			= "D:\\output.txt";
	public static final String xmlFileAll		= "D:\\output-all.xml";
	public static final String txtFileDerived 	= "D:\\output-derived.txt";
	
	// Used to simulate the expression $GLOBAL["__OUTPUT__"]
	private static final StringValue GLOBALS 	= new CompiledConstStringValue("GLOBALS");
	public static final StringValue __OUTPUT__ = new ConstStringValue("__OUTPUT__");
	
	/**
	 * Static instance of OutputViewer
	 */
	public static OutputViewer inst = new OutputViewer();
	
	/**
	 * The final output value after execution.
	 * This value should be set one time only right after the execution.
	 * @see edu.iastate.hungnv.shadow.Env_.closing(Env)
	 */
	private Value finalOutputValue = null;
	
	/**
	 * This variable is used to allow and disallow the tracking of output,
	 * preventing duplication in the tracking of high-level and low-level output printing.
	 */
	private boolean enabled = true;
	
	/*
	 * Getters and setters
	 */
	
	public void setFinalOutputValue(Value value) {
		finalOutputValue = value;
	}
	
	public Value getFinalOutputValue() {
		return finalOutputValue;
	}
	
	public boolean getEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/*
	 * Methods
	 */
	
	/**
	 * Returns the current output value
	 * @return The current output value
	 */
	public Value getOutputValue() {
		Env env =  Env.getInstance();
		
		// Simulate the expression $GLOBAL["__OUTPUT__"]
		// @see com.caucho.quercus.expr.ArrayGetExpr.eval(Env), com.caucho.quercus.expr.VarExpr.eval(Env)
		Value array = env.getValue(GLOBALS, false, true);
		Value index = __OUTPUT__;
						
		return array.get(index);
	}
	
	/**
	 * Sets the current output value 
	 * @param value
	 */
	public void setOutputValue(Value value) {
		Env env =  Env.getInstance();
		
		// Simulate the expression $GLOBAL["__OUTPUT__"] = value
		// @see com.caucho.quercus.expr.ArrayGetExpr.evalAssignValue(Env, Value), com.caucho.quercus.expr.VarExpr.evalArray(Env)
		
		Var array = env.getVar(GLOBALS);
		array.put(__OUTPUT__, value);
	}
	
	/**
	 * Tracks the printing of a string
	 * @param string
	 */
	public void print(String string) {
		print(new ConstStringValue(string));
	}
	
	/**
	 * Tracks the printing of a value
	 * @param value
	 */
	public void print(Value value) {
		if (!enabled)
			return;
		
		Value oldOutput = getOutputValue();
		Value newOutput = MultiValue.createConcatValue(oldOutput, value);
		
		setOutputValue(newOutput);
	}
	
	/**
	 * Writes the output to a TXT file.
	 * @param txtFile
	 */
	public void writeToTxtFile(String txtFile) {
		writeToTxtFile(txtFile, null);
	}
	
	/**
	 * Writes the output to a TXT file given a constraint
	 * @param txtFile
	 * @param constraint (can be null)
	 */
	public void writeToTxtFile(String txtFile, Constraint constraint) {
		Value outputValue = getFinalOutputValue();
				
		if (constraint != null)
			outputValue = MultiValue.simplify(outputValue, constraint);
		else
			outputValue = MultiValue.simplify(outputValue);
		
		FileIO.writeStringToFile(outputValue.toString(), txtFile);
	}
	
	/**
	 * Writes the output to an XML file.
	 * @param xmlFile
	 */
	public void writeToXmlFile(String xmlFile) {
		writeToXmlFile(xmlFile, null);
	}
	
	/**
	 * Writes the output to an XML file given a constraint
	 * @param xmlFile
	 * @param constraint (can be null)
	 */
	public void writeToXmlFile(String xmlFile, Constraint constraint) {
		Value outputValue = getFinalOutputValue();
		
		ValueViewer valueViewer = new ValueViewer();
		valueViewer.add(__OUTPUT__, outputValue);
		
		valueViewer.writeToXmlFile(xmlFile, constraint);
	}
	
}