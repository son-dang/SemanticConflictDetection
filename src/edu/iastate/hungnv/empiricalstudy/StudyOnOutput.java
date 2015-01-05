package edu.iastate.hungnv.empiricalstudy;

import java.util.ArrayList;
import java.util.LinkedList;

import com.caucho.quercus.env.ArrayValueImpl;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.ObjectExtValue;
import com.caucho.quercus.env.StringBuilderValue;
import com.caucho.quercus.env.StringValue;
import com.caucho.quercus.env.Value;

import de.fosd.typechef.featureexpr.FeatureExpr;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.debug.OutputViewer;
import edu.iastate.hungnv.value.Case;
import edu.iastate.hungnv.value.Choice;
import edu.iastate.hungnv.value.Concat;
import edu.iastate.hungnv.value.Switch;
import edu.iastate.hungnv.value.Undefined;

/**
 * 
 * @author HUNG
 *
 */
public class StudyOnOutput {
	
	public static final String outputTxtFile = "C:\\Users\\HUNG\\Desktop\\Varex Evaluation\\eval-output.txt";

	/**
	 * Called when Env is started.
	 */
    public void envStarted() {
    }
    
    /**
     * Called when Env is closed.
     */
    public void envClosed(Env env) {
		Value outputValue = compactOutputValue(OutputViewer.inst.getFinalOutputValue(), Constraint.TRUE);
		
		ArrayList<String> outputFragments = getOutputFragments(outputValue, Constraint.TRUE);
		
		Utils.writeListToFile(outputFragments, outputTxtFile);
    }
    
    /**
     * Compacts output value
     */
    private Value compactOutputValue(Value outputValue, Constraint constraint) {
	    if (outputValue instanceof Concat) {
	    	ArrayList<Value> childValues = new ArrayList<Value>();
    		
	    	for (Value child : (Concat) outputValue) {
    			Value currentChild = compactOutputValue(child, constraint);
    			
    			if (childValues.isEmpty())
    				childValues.add(currentChild);
    			
    			Value lastChild = childValues.get(childValues.size() - 1);
    			if (lastChild instanceof StringValue && currentChild instanceof StringValue) {
    				StringValue newChild = new StringBuilderValue(lastChild.toString() + currentChild.toString());
    				childValues.set(childValues.size() - 1, newChild);
    			}
    			else
    				childValues.add(currentChild);
    		}
    		
    		return new Concat(childValues);
		}
	    
		else if (outputValue instanceof Choice) {
			// TODO Revise
			Switch switch_ = ((Choice) outputValue).flatten();
			return compactOutputValue(switch_, constraint);
		}
	    
		else if (outputValue instanceof Switch) {
			return compactSwitchValue((Switch) outputValue, constraint);
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
	    	return outputValue;
		}
	    
	    return null; // Should not reach here
    }
    
    /**
     * Compacts a Switch value
     */
    private Value compactSwitchValue(Switch switch_, Constraint constraint) {
    	LinkedList<String> fragments = new LinkedList<String>();
    	LinkedList<Constraint> constraints = new LinkedList<Constraint>();
    	
    	/*
    	 * Step 1: Align sequences of string fragments. Each fragment has an associated constraint.
    	 * For example, sequence 1: [Foo, AB][Foo, C], sequence 2: [!Foo, AB][!Foo, D]. After alignment: [True, AB][Foo, C][!Foo, D].
    	 */
    	for (Case case_ : switch_) {
    		Constraint caseConstraint = Constraint.createAndConstraint(constraint, case_.getConstraint());
    		String caseString = case_.getValue().toString();
    		
    		if (caseConstraint.isContradiction())
    			continue;
    		
    		LinkedList<String> caseFragments = getStringFragments(caseString);
    		if (fragments.isEmpty()) {
    			for (String fragment : caseFragments) {
    				fragments.add(fragment);
    				constraints.add(caseConstraint);
    			}
    			continue;
    		}

    		int i = 0;
			int j = 0;
			while (j < caseFragments.size()) {
				if (i == fragments.size()) {
					fragments.add(caseFragments.get(j));
					constraints.add(caseConstraint);
					i++;
					j++;
					continue;
				}
				
				String iFragment = fragments.get(i);
				String jFragment = caseFragments.get(j);
				
				Constraint iConstraint = constraints.get(i);
				Constraint jConstraint = caseConstraint;
				
				if (iFragment.equals(jFragment)) {
					constraints.set(i, Constraint.createOrConstraint(iConstraint, jConstraint));
					i++;
					j++;
				}
				else if (indexOf(fragments, jFragment, i) != -1) {
					i = indexOf(fragments, jFragment, i);
				}
				else { // if (indexOf(caseFragments, iFragment, j) != -1)
					fragments.add(i, jFragment);
					constraints.add(i, jConstraint);
					i++;
					j++;
				}
    		}
    	}
    	
    	/*
    	 * Step 2: Turn the fragments and their associated constraints into a Concat of Switches.
    	 * For example, [True, AB][Foo, C][!Foo, D] => Concat(AB, Switch(Case(Foo, C)), Switch(Case(!Foo, D))).
    	 */
    	ArrayList<Value> childValues = new ArrayList<Value>();		
    	
    	int i = 0;
    	while (i < fragments.size()) {
    		Constraint iConstraint = constraints.get(i);
    		int j;
    		for (j = i; j < fragments.size(); j++) {
    			if (!constraints.get(j).equivalentTo(iConstraint))
    				break;
    		}
    		
    		String combinedFragment = "";
    		for (int k = i; k < j; k++)
    			combinedFragment += fragments.get(k);
    		
    		i = j;
    		
    		Value fragmentValue;
    		if (iConstraint.isTautology())
    			fragmentValue = new StringBuilderValue(combinedFragment);
    		else {
    			Switch newSwitch = new Switch();
    			newSwitch.addCase(new Case(iConstraint, new StringBuilderValue(combinedFragment)));
    			fragmentValue = newSwitch;
    		}
    		
    		childValues.add(fragmentValue);
    	}
		
    	if (childValues.size() == 0)
    		return switch_;
    	else if (childValues.size() == 1)
    		return childValues.get(0);
    	else
    		return new Concat(childValues);
    }
    
    /**
     * Splits a string into string fragments.
     */
    private LinkedList<String> getStringFragments(String string) {
    	LinkedList<String> fragments = new LinkedList<String>();
    	
    	int i = 0;
    	while (i < string.length()) {
    		int iType = Character.isWhitespace(string.charAt(i)) ? 1 : 2;
    		int j;
    		for (j = i; j < string.length(); j++) {
    			int jType = Character.isWhitespace(string.charAt(j)) ? 1 : 2;
    			
    			if (jType != iType)
    				break;
    		}
    		fragments.add(string.substring(i, j));
    		i = j;
		}
    	
    	return fragments;
    }
    
    /**
     * Returns the index of a fragment in a list of fragments.
     */
    private int indexOf(LinkedList<String> fragments, String fragment, int fromIndex) {
    	for (int i = fromIndex; i < fragments.size(); i++)
    		if (fragments.get(i).equals(fragment))
    			return i;
    	
    	return -1;
    }
    
    /**
     * Gets output fragments from an outputValue
     */
    private ArrayList<String> getOutputFragments(Value outputValue, Constraint constraint) {
    	ArrayList<String> outputFragments = new ArrayList<String>();
    	
	    if (outputValue instanceof Concat) {
    		for (Value child : (Concat) outputValue) {
    			outputFragments.addAll(getOutputFragments(child, constraint));
    		}
		}
	    
		else if (outputValue instanceof Choice) {
   			System.err.println("In StudyOnOutput.java: value cannot be Choice.");
   			System.exit(0); // This can't happen
		}
	    
		else if (outputValue instanceof Switch) {
			for (Case case_ : (Switch) outputValue) {
				outputFragments.addAll(getOutputFragments(case_.getValue(), Constraint.createAndConstraint(constraint, case_.getConstraint())));
			}
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
	    	String valueStr = Utils.getAbbreviatedString(outputValue);
	    	String valueType = outputValue.getClass().getSimpleName();
	    	
	    	FeatureExpr featureExpr = constraint.getFeatureExpr();
	    	String featureExprStr = featureExpr.toString();
	    	int featureCount = featureExpr.collectDistinctFeatures().size();
	    	
	    	int stringSize = outputValue.toString().length();
	    	
	    	outputFragments.add(valueStr + " # "
	    			+ valueType + " # "
	    			+ featureExprStr + " # " 
	    			+ featureCount + " # " 
	    			+ String.valueOf(stringSize)
	    	);
		}
	    
	    return outputFragments;
    }
    
}
