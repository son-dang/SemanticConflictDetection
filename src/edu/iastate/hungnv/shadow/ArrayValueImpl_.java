package edu.iastate.hungnv.shadow;

import com.caucho.quercus.env.ArrayValue;
import com.caucho.quercus.env.ArrayValueImpl;
import com.caucho.quercus.env.BooleanValue;
import com.caucho.quercus.env.NullValue;
import com.caucho.quercus.env.Value;
import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.value.Case;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.Undefined;

/**
 * 
 * @author HUNG
 *
 */
public class ArrayValueImpl_ {
	
	/**
	 * @see com.caucho.quercus.env.ArrayValueImpl.append(Value, Value)
	 */
	public static ArrayValue append(Value key, Value value, final ArrayValueImpl _this, boolean withScoping) {
		if (key instanceof MultiValue) {
			// Convert Array(CHOICE(Cond, x, y) => z) into Array(x => CHOICE(Cond, z, UNDEFINED), y => CHOICE(!Cond, z, UNDEFINED))
			for (Case case_ : ((MultiValue) key).flatten()) {
				Value flattenedKey = case_.getValue();
				Constraint constraint = case_.getConstraint();
				
				Value oldValue = _this.get(flattenedKey);
				
				if (oldValue instanceof NullValue)	// Use UNDEFINED instead of NULL because if Array[i] == CHOICE(value, NULL), flatten(Array[i]) returns two values and it will cause a NULL exception if Array[i] is used
					oldValue = Undefined.UNDEFINED;	// On the other hand, if Array[i] == CHOICE(value, UNDEFINED), flatten(Array[i]) will return only one value.
													// @see com.caucho.quercus.env.ArrayValue.Entry.set(Value)
				
				Value modifiedValue = MultiValue.createChoiceValue(constraint, value, oldValue);
				
				// Eval basic case
				_this.append_basic(flattenedKey, modifiedValue, withScoping);
			}
			
			return _this;
		}
		else
			return _this.append_basic(key, value, withScoping);
	}
	
	/**
	 * Returns a CHOICE(C, TRUE, FALSE) if the array elements are undefined in some cases.
	 * For example, array = (0 => CHOICE(C, UNDEFINED, x), 1 => CHOICE(C, UNDEFINED, y))
	 */
	public static Value isNull(ArrayValueImpl array) {
		Constraint undefinedCases = null;
		for (Value value : array.values()) {
			Constraint undefined = MultiValue.whenUndefined(value);
			
			if (undefinedCases == null)
				undefinedCases = undefined;
			else if (!undefinedCases.equivalentTo(undefined))
				return BooleanValue.FALSE;
		}
		
		return (undefinedCases == null ? BooleanValue.TRUE : MultiValue.createChoiceValue(undefinedCases, BooleanValue.TRUE, BooleanValue.FALSE));
	}
	
	/*
	 * TODO PENDING CHANGES
	 */
	
	/*
	public static Value isCallable(ArrayValueImpl array, Env env) {
		Value result = array.isCallable(env) ? BooleanValue.TRUE : BooleanValue.FALSE;
		
		ArrayValueImpl array1 = new ArrayValueImpl();
		Constraint constraint = null;
		
		for (Value key : array.keySet()) {
			Value value = array.get(key);
			if (!(value instanceof Choice))
				return result;
			
			if (constraint == null)
				constraint = ((Choice) value).getConstraint();
			else if (!constraint.equivalentTo(((Choice) value).getConstraint()))
				return result;

			Value value1 = ((Choice) value).getValue1();
			Value value2 = ((Choice) value).getValue2();
			
			if (!(value2 instanceof Undefined))
				return result;
			
			array1.append(key, value1);
		}
		
		if (array1.isCallable(env))
			return MultiValue.createChoiceValue(constraint, BooleanValue.TRUE, BooleanValue.FALSE);
		else
			return BooleanValue.FALSE;
	}
	*/
	
	/**
	 * Flattens an array in case the array's elements are MultiValues.
	 * @see edu.iastate.hungnv.value.MultiValue.flatten(Value)
	 */
	/*
	public static Switch flatten(ArrayValueImpl array) {
		//TODO Revise
		Set<Value> keys = array.keySet();
		Value[] values = new Value[keys.size()];
		
		int idx = 0;
		for (Value key : keys) {
			values[idx++] = array.get(key);
		}
		
		Switch newSwitch = new Switch();
		
		// TODO: Optional. If this check is used, deep-flattening is not possible.
		// For example, all the array elements themselves are not instanceof MultiValues
		// but some of them may be arrays of MultiValues
		boolean alreadyFlattened = true;
		for (Value value : values) {
			if (value instanceof MultiValue) {
				alreadyFlattened = false;
				break;
			}
		}
		if (alreadyFlattened) {
			newSwitch.addCase(new Case(Constraint.TRUE, array));
			return newSwitch;
		}
		
		Switch flattened = JavaInvoker_.flattenArray(values);
		
		for (Case case_ : flattened) {
			Value[] flattenedValues = (Value[]) ((WrappedObject) case_.getValue()).getObject();
			Constraint constraint = case_.getConstraint();
			
			ArrayValueImpl newArray = (ArrayValueImpl) array.copy();
			idx = 0;
			for (Value key : keys) {
				newArray.append(key, flattenedValues[idx]);
				idx++;
			}
			
			Case newCase = new Case(constraint, newArray);
			newSwitch.addCase(newCase);
		}
		
		return newSwitch;
	}
	*/

}
