package edu.iastate.hungnv.empiricalstudy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.caucho.quercus.env.ArrayValueImpl;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.EnvVar;
import com.caucho.quercus.env.ObjectExtValue;
import com.caucho.quercus.env.StringValue;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.env.ObjectExtValue.EntrySet;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.util.FileIO;
import edu.iastate.hungnv.value.Case;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.Switch;

/**
 * 
 * @author HUNG
 *
 */
public class Utils {

	// Cache the flattenedValues to increase performance
    private static HashMap<Value, Switch> flattenedValues = new HashMap<Value, Switch>();
    
    public static Switch flattenValue(Value value) {
    	if (!flattenedValues.containsKey(value)) {
    		Switch originalSwitch = MultiValue.flatten(value);
    		
    		/*
    		 * Simplify the Switch
    		 */
    		HashMap<Value, Constraint> map = new HashMap<Value, Constraint>();
    		for (Case case_ : originalSwitch) {
    			if (map.containsKey(case_.getValue()))
    				map.put(case_.getValue(), Constraint.createOrConstraint(map.get(case_.getValue()), case_.getConstraint()));
    			else
    				map.put(case_.getValue(), case_.getConstraint());
    		}
    		
    		Switch simplifiedSwitch = new Switch();
    		for (Value value_ : map.keySet()) {
    			simplifiedSwitch.addCase(new Case(map.get(value_), value_));
    		}
    		
    		flattenedValues.put(value, simplifiedSwitch);
    	}
    	
    	return flattenedValues.get(value);
    }
    
    public static ArrayList<NameValuePair> getNameValuePairsFromEnv(Env env) {
		ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
		
		for (StringValue name : env.getEnv().keySet()) {
			EnvVar envVar = env.getEnv().get(name);
			
			// Identify a variable with a name and id (to distinguish variables with the same name but in different scopes)
			String namePart = name.toString();
			String idPart = String.valueOf(envVar.getVar().hashCode());
			Value value = env.getEnv().get(name).get();
			
			pairs.add(new NameValuePair(namePart + "(" + idPart + ")", value));
		}
		Collections.sort(pairs, Utils.SortNameValuePairByName.inst);
		
		return pairs;
    }
    
    public static ArrayList<NameValuePair> getNameValuePairsFromArray(ArrayValueImpl array) {
		ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
		
		for (Iterator<Map.Entry<Value, Value>> iter = array.getIterator(); iter.hasNext(); ) {
			Map.Entry<Value, Value> pair = iter.next();
			pairs.add(new NameValuePair(pair.getKey().toString(), pair.getValue()));
		}
		Collections.sort(pairs, Utils.SortNameValuePairByName.inst);
		
		return pairs;
    }
    
    public static ArrayList<NameValuePair> getNameValuePairsFromObject(ObjectExtValue object) {
		ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
		
		for (Iterator<Map.Entry<Value,Value>> iter = ((EntrySet) object.entrySet()).iterator(); iter.hasNext(); ) {
			Map.Entry<Value, Value> pair = iter.next();
			pairs.add(new NameValuePair(pair.getKey().toString(), pair.getValue()));
		}
		Collections.sort(pairs, Utils.SortNameValuePairByName.inst);
		
		return pairs;
    }
    
    /**
     * Class NameValuePair
     */
    public static class NameValuePair {
    	
    	private String name;
    	private Value value;
    	
    	public NameValuePair(String name, Value value) {
    		this.name = name;
    		this.value = value;
    	}
    	
    	public String getName() {
    		return name;
    	}
    	
    	public Value getValue() {
    		return value;
    	}
    	
    }
    
	/**
	 * Helper class to support sorting of NameValuePairs
	 */
	public static class SortNameValuePairByName implements Comparator<NameValuePair> {
	
		public static SortNameValuePairByName inst = new SortNameValuePairByName();
		
		@Override
		public int compare(NameValuePair pair1, NameValuePair pair2) {
			return pair1.getName().compareTo(pair2.getName());
		}
	
	}

	/**
	 * Returns a short string describing a value.
	 */
	public static String getAbbreviatedString(Value value) {
		String valueStr = value.toString();
		
		if (valueStr.length() > 20)
			valueStr = valueStr.substring(0, 10) + "..." + valueStr.substring(valueStr.length() - 10);
		
		valueStr = valueStr.replace("\r\n", " ").replace("\n", " ").replace("\t", " "). replace("#", " ");
		
		return valueStr;
	}

	/**
	 * Writes a list of strings to file.
	 */
	public static void writeListToFile(ArrayList<String> list, String file) {
		StringBuilder str = new StringBuilder();
		for (String line : list) {
			str.append(line + System.lineSeparator());
		}
		FileIO.writeStringToFile(str.toString(), file);
	}

}