package edu.iastate.hungnv.debug;

import java.util.ArrayList;

import com.caucho.quercus.env.ArrayValueImpl;
import com.caucho.quercus.env.CompiledConstStringValue;
import com.caucho.quercus.env.ConstStringValue;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.ObjectExtValue;
import com.caucho.quercus.env.StringBuilderValue;
import com.caucho.quercus.env.StringValue;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.env.Var;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.util.FileIO;
import edu.iastate.hungnv.value.Case;
import edu.iastate.hungnv.value.Choice;
import edu.iastate.hungnv.value.Concat;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.Switch;
import edu.iastate.hungnv.value.Undefined;

/**
 * 
 * @author HUNG
 *
 */
public class OutputIfDefViewer {
	
	public static final String ifdefFileAll		= "D:\\output-all.ifdef";
	
	// Used to simulate the expression $GLOBAL["__OUTPUT__"]
	private static final StringValue GLOBALS 	= new CompiledConstStringValue("GLOBALS");
	private static final StringValue __OUTPUT__ = new ConstStringValue("__OUTPUT__");
	
	/**
	 * Static instance of OutputIfDefViewer
	 */
	public static OutputIfDefViewer inst = new OutputIfDefViewer();
	
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
	public void writeToIfDefFile(String xmlFile) {
		writeToIfDefFile(xmlFile, null);
	}
	
	/**
	 * Writes the output to an XML file given a constraint
	 * @param xmlFile
	 * @param constraint (can be null)
	 */
	public void writeToIfDefFile(String xmlFile, Constraint constraint) {
		Value outputValue = OutputViewer.inst.getFinalOutputValue();
		
		String string = valueToString(outputValue);
		
		FileIO.writeStringToFile(string, xmlFile);
	}
	
	private String valueToString(Value outputValue) {
	    if (outputValue instanceof Concat) {
	    	StringBuilder str = new StringBuilder();
    		
	    	for (Value child : (Concat) outputValue) {
    			str.append(valueToString(child));
    		}
    		
    		return str.toString();
		}
	    
		else if (outputValue instanceof Choice) {
			Switch switch_ = ((Choice) outputValue).flatten();
			return valueToString(switch_);
		}
	    
		else if (outputValue instanceof Switch) {
			StringBuilder str = new StringBuilder();
			str.append(System.lineSeparator());
			for (Case case_ : (Switch) outputValue) {
				String caseConstraint = case_.getConstraint().toString();
				String caseString = valueToString(case_.getValue());
				
				str.append("#IF " + caseConstraint + System.lineSeparator());
				str.append(caseString + System.lineSeparator());
				str.append("#ENDIF" + System.lineSeparator());
			}
			
			return str.toString();
		}
	    
		else if (outputValue instanceof Undefined) {
   			System.err.println("In StudyOnOutput.java: value cannot be Undefined.");
   			System.exit(0); // This can't happen
		}
	    
		else if (outputValue instanceof ArrayValueImpl) {
   			System.err.println("In StudyOnOutput.java: value cannot be ArrayValueImpl.");
   			System.exit(0); // This can't happen
		}
	    
		else if (outputValue instanceof ObjectExtValue) {
   			System.err.println("In StudyOnOutput.java: value cannot be ObjectExtValue.");
   			System.exit(0); // This can't happen
		}
	    
		else { // PrimitiveValue
	    	return outputValue.toString();
		}
	    
	    return null; // Should not reach here
    }
		
}