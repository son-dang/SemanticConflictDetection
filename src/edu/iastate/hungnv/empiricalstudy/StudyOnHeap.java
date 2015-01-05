package edu.iastate.hungnv.empiricalstudy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.caucho.quercus.env.ArrayValueImpl;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.ObjectExtValue;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.env.ObjectExtValue.EntrySet;
import com.caucho.quercus.statement.Statement;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.empiricalstudy.Utils.NameValuePair;
import edu.iastate.hungnv.util.Logging;
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
public class StudyOnHeap {
	
	public static final String heapTxtFile = "C:\\Users\\HUNG\\Desktop\\Varex Evaluation\\eval-heap.txt";

	private static final int snapshotsInterval = 100; // Take a snapshot for every snapshotsInterval statements
	private int statementCount;
	
	private static final int maxSize = 1000000; // Max size to compute for an array/object
	
	// Map a variable's name to its value
	private HashMap<String, Value> variableStore; 
	
	/**
	 * Called when Env is started.
	 */
    public void envStarted() {
    	statementCount = 0;
    	variableStore = new HashMap<String, Value>();
    }
    
    /**
     * Called when Env is closed.
     */
    public void envClosed(Env env) {
    	takeHeapSnapshot(env);
    	
    	ArrayList<String> variableList = getVariableList(variableStore);
    	
    	Utils.writeListToFile(variableList, heapTxtFile);
    }
    
    /**
     * Called when a statement is executed.
     */
    public void statementExecuted(Statement statement, Env env) {
    	statementCount++;
    	if (statementCount % snapshotsInterval == 1)
    		takeHeapSnapshot(env);
    }
    
    /**
     * Takes a snapshot of the heap.
     */
    private void takeHeapSnapshot(Env env) {
    	Logging.LOGGER.info("Taking memory snapshot at statement " + statementCount + "...");
    	updateVariableStore(env);
    }
    
	/**
	 * Updates the variableStore from the variables in Env
	 */
	private void updateVariableStore(Env env) {
		for (NameValuePair pair : Utils.getNameValuePairsFromEnv(env)) {
			String name = pair.getName();
			Value value = pair.getValue();
			
			if (!variableStore.containsKey(name)) {
				variableStore.put(name, value);
			}
			else {
				Value oldValue = variableStore.get(name);
				if (getComplexity(value) >= getComplexity(oldValue))
					variableStore.put(name, value);
			}
		}
	}
    
	/**
	 * Returns the complexity of a value.
	 */
	private int getComplexity(Value value) {
		if (value instanceof Concat) {
			return ((Concat) value).getChildNodes().size();
		}
		
		else if (value instanceof MultiValue) {
			return Utils.flattenValue(value).getCases().size();
		}
	    
		else if (value instanceof ArrayValueImpl) {
	    	return ((ArrayValueImpl) value).size();
		}
	    
		else if (value instanceof ObjectExtValue) {
	    	return ((ObjectExtValue) value).getSize();
		}
	    
		else { // PrimitiveValue
			return 1;
		}
	}
    
    /**
     * Returns a list of variables together with their properties
     */
    private ArrayList<String> getVariableList(HashMap<String, Value> variableStore) {
    	ArrayList<String> variableList = new ArrayList<String>();
    	
		ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
		for (String name : variableStore.keySet()) {
			Value value = variableStore.get(name);
			pairs.add(new NameValuePair(name, value));
		}
		Collections.sort(pairs, Utils.SortNameValuePairByName.inst);
		
		for (NameValuePair pair : pairs) {
			String name = pair.getName();
			Value value = pair.getValue();
			
	    	variableList.addAll(getVariableList(name, value, 0, Constraint.TRUE));
		}
		
		return variableList;
    }
    
    /**
     * Returns a list of variables together with their properties at deeper levels
     */
    private ArrayList<String> getVariableList(String name, Value value, int depth, Constraint constraint) {
    	// Must be simplified
    	value = MultiValue.simplify(value, constraint);
    	
    	// Ignore Undefined values
    	if (value instanceof Undefined)
    		return new ArrayList<String>();
    	
    	ArrayList<String> variableList = new ArrayList<String>();
    	
    	String valueStr = Utils.getAbbreviatedString(value);
    	String valueType = value.getClass().getSimpleName();
    	
    	HashSet<String> featureSet = getFeatureSet(value);
    	String featureSetStr = getFeatureSetString(featureSet);
    	int featureCount = featureSet.size();
    	
    	int size = -1;
    	int sizeWithoutCompact = -1;
    	if (depth == 0 && (value instanceof ArrayValueImpl || value instanceof ObjectExtValue)) {
    		size = computeSize(value);
    		
    		HashMap<Constraint, Integer> sizeMap = computeSizeWithoutCompact(value); 
    		sizeWithoutCompact = (sizeMap != null) ? getSize(sizeMap) : -maxSize;
    	}
    	
    	variableList.add(name + " # " 
    			+ valueStr + " # "
    			+ valueType + " # "
    			+ depth + " # "
    			+ featureSetStr + " # " 
    			+ featureCount + " # "
    			+ (size == -1 ? "-" : String.valueOf(size)) + " # "
    			+ (sizeWithoutCompact == -1 ? "-" : String.valueOf(sizeWithoutCompact))
    	);
    	
    	variableList.addAll(getVariableList(value, depth, constraint));
    	
    	return variableList;
    }
    
    /**
     * Returns a list of variables together with their properties at deeper levels
     */
    private ArrayList<String> getVariableList(Value value, int depth, Constraint constraint) {
    	ArrayList<String> variableList = new ArrayList<String>();
    	
	    if (value instanceof Concat) {
			// Do nothing
		}
	    
		else if (value instanceof Choice) {
			Constraint trueConstraint = Constraint.createAndConstraint(constraint, ((Choice) value).getConstraint());
			if (trueConstraint.isSatisfiable())
				variableList.addAll(getVariableList(((Choice) value).getValue1(), depth, trueConstraint));
			
			Constraint falseConstraint = Constraint.createAndConstraint(constraint, Constraint.createNotConstraint(((Choice) value).getConstraint()));
			if (falseConstraint.isSatisfiable())
				variableList.addAll(getVariableList(((Choice) value).getValue2(), depth, falseConstraint));
		}
	    
		else if (value instanceof Switch) {
			for (Case case_ : (Switch) value) {
				Constraint caseConstraint =  Constraint.createAndConstraint(constraint, case_.getConstraint());
				if (caseConstraint.isSatisfiable())
					variableList.addAll(getVariableList(case_.getValue(), depth, caseConstraint));
			}
		}
	    
		else if (value instanceof Undefined) {
			// Do nothing
		}
	    
		else if (value instanceof ArrayValueImpl) {
	    	for (Utils.NameValuePair pair : Utils.getNameValuePairsFromArray((ArrayValueImpl) value)) {
	    		variableList.addAll(getVariableList(pair.getName(), pair.getValue(), depth + 1, constraint));
			}
		}
	    
		else if (value instanceof ObjectExtValue) {
	    	for (Utils.NameValuePair pair : Utils.getNameValuePairsFromObject((ObjectExtValue) value)) {
				variableList.addAll(getVariableList(pair.getName(), pair.getValue(), depth + 1, constraint));
			}
		}
	    
		else { // PrimitiveValue
			// Do nothing
		}
	    
	    return variableList;
    }
    
    /**
     * Returns the features that a given value depends on.
     */
    private HashSet<String> getFeatureSet(Value value) {
    	HashSet<String> features = new HashSet<String>();
		
		if (value instanceof Concat) {
			for (Value child : ((Concat) value).getChildNodes()) {
				features.addAll(getFeatureSet(child));
			}
		}
		else {    	
			for (Case case_ : Utils.flattenValue(value)) {
				FeatureExpr featureExpr = case_.getConstraint().getFeatureExpr();
				for (scala.collection.Iterator<String> iter = featureExpr.collectDistinctFeatures().iterator(); iter.hasNext(); )
					features.add(iter.next());
			}
		}
    	
    	return features;
    }
    
    /**
     * Returns a string that describes a feature set.
     */
    private String getFeatureSetString(HashSet<String> featureSet) {
    	StringBuilder str = new StringBuilder();
    	for (String s : featureSet) {
    		str.append("[" + s + "]");
    	}
    	return str.toString();
    }
    
    /*
     * Compute size of a value
     */
    
    private int computeSize(Value value) {
    	int size = 0;
    	
    	for (Case case_ : Utils.flattenValue(value)) {
    		Value flattenedValue = case_.getValue();
    		
    		if (flattenedValue instanceof ArrayValueImpl) {
    			size += computeArraySize((ArrayValueImpl) flattenedValue);
    		}
    		else if (flattenedValue instanceof ObjectExtValue) {
    			size += computeObjectSize((ObjectExtValue) flattenedValue);
    		}
    		else { // PrimitiveValue
    			size++;
    		}
    	}
    	
    	return size;
    }
    
    private int computeArraySize(ArrayValueImpl array) {
    	int size = 1;
		
    	for (Iterator<Map.Entry<Value, Value>> iter = array.getIterator(); iter.hasNext(); ) {
			Map.Entry<Value, Value> pair = iter.next();
			size += computeSize(pair.getValue());
		}
		
		return size;
    }
    
    private int computeObjectSize(ObjectExtValue object) {
    	int size = 1;
    	
		for (Iterator<Map.Entry<Value,Value>> iter = ((EntrySet) object.entrySet()).iterator(); iter.hasNext(); ) {
			Map.Entry<Value, Value> pair = iter.next();
			size += computeSize(pair.getValue());
		}
		
		return size;
    }
    
    /*
     * Compute size of a value WITHOUT using the compact algorithm
     * Return null if the size is too big.
     */
    
    private HashMap<Constraint, Integer> computeSizeWithoutCompact(Value value) {
    	HashMap<Constraint, Integer> sizeMap = new HashMap<Constraint, Integer>();
    	
    	Switch switch_ = Utils.flattenValue(value);
    	for (Case case_ : switch_) {
    		Constraint caseConstraint = case_.getConstraint();
    		Value caseValue = case_.getValue();
    		
    		HashMap<Constraint, Integer> caseSizeMap;
    		
    		if (caseValue instanceof ArrayValueImpl) {
    			caseSizeMap = computeArraySizeWithoutCompact((ArrayValueImpl) caseValue);
    		}
    		else if (caseValue instanceof ObjectExtValue) {
    			caseSizeMap = computeObjectSizeWithoutCompact((ObjectExtValue) caseValue);
    		}
    		else { // PrimitiveValue
    			caseSizeMap = new HashMap<Constraint, Integer>();
    			caseSizeMap.put(Constraint.TRUE, 1);
    		}
    		
    		if (caseSizeMap == null)
    			return null;

			for (Constraint c : caseSizeMap.keySet()) {
    			int s = caseSizeMap.get(c);
    			
    			Constraint newConstraint = Constraint.createAndConstraint(caseConstraint, c);
    			if (newConstraint.isSatisfiable())
    				sizeMap.put(newConstraint, s);
    		}
			
			if (sizeTooBig(sizeMap))
    			return null;
    	}
    	
    	Constraint undefined = Switch.whenUndefined(switch_);
    	if (undefined.isSatisfiable())
    		sizeMap.put(undefined, 0);
    	
    	return sizeMap;
    }
    
    private HashMap<Constraint, Integer> computeArraySizeWithoutCompact(ArrayValueImpl array) {
    	HashMap<Constraint, Integer> sizeMap = new HashMap<Constraint, Integer>();
		
    	sizeMap.put(Constraint.TRUE, 1);
    	
    	for (Iterator<Map.Entry<Value, Value>> iter = array.getIterator(); iter.hasNext(); ) {
			Map.Entry<Value, Value> pair = iter.next();
			HashMap<Constraint, Integer> elementSizeMap = computeSizeWithoutCompact(pair.getValue());
    		
			if (elementSizeMap == null)
				return null;
			
			HashMap<Constraint, Integer> updatedSizeMap = new HashMap<Constraint, Integer>();
			for (Constraint c1 : sizeMap.keySet()) {
				int s1 = sizeMap.get(c1);
				
				for (Constraint c2 : elementSizeMap.keySet()) {
	    			int s2 = elementSizeMap.get(c2);
	    			
	    			Constraint newConstraint = Constraint.createAndConstraint(c1, c2);
	    			if (newConstraint.isSatisfiable())
	    				updatedSizeMap.put(newConstraint, s1 + s2);
	    		}
				
	    		if (sizeTooBig(updatedSizeMap))
	    			return null;
			}
			
			sizeMap = updatedSizeMap;
		}
		
		return sizeMap;
    }
    
    private HashMap<Constraint, Integer> computeObjectSizeWithoutCompact(ObjectExtValue object) {
    	HashMap<Constraint, Integer> sizeMap = new HashMap<Constraint, Integer>();
		
    	sizeMap.put(Constraint.TRUE, 1);
    	
    	for (Iterator<Map.Entry<Value,Value>> iter = ((EntrySet) object.entrySet()).iterator(); iter.hasNext(); ) {
			Map.Entry<Value, Value> pair = iter.next();
			HashMap<Constraint, Integer> elementSizeMap = computeSizeWithoutCompact(pair.getValue());
    		
			if (elementSizeMap == null)
				return null;
			
			HashMap<Constraint, Integer> updatedSizeMap = new HashMap<Constraint, Integer>();
			for (Constraint c1 : sizeMap.keySet()) {
				int s1 = sizeMap.get(c1);
				
				for (Constraint c2 : elementSizeMap.keySet()) {
	    			int s2 = elementSizeMap.get(c2);
	    			
	    			Constraint newConstraint = Constraint.createAndConstraint(c1, c2);
	    			if (newConstraint.isSatisfiable())
	    				updatedSizeMap.put(newConstraint, s1 + s2);
	    		}
	    		
	    		if (sizeTooBig(updatedSizeMap))
	    			return null;
			}
			
			sizeMap = updatedSizeMap;
		}
		
		return sizeMap;
    }
    
    private int getSize(HashMap<Constraint, Integer> sizeMap) {
    	int size = 0;
    	
    	for (int s : sizeMap.values())
    		size += s;
    			
    	return size;
    }
    
    private boolean sizeTooBig(HashMap<Constraint, Integer> sizeMap) {
    	int size = getSize(sizeMap);
    	return size > maxSize;
    }
    
}
