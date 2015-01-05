package edu.iastate.hungnv.shadow;

import java.util.Arrays;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.JavaInvoker;
import com.caucho.quercus.env.QuercusClass;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.env.Var;
import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.scope.ScopedValue;
import edu.iastate.hungnv.value.Case;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.Switch;

/**
 * 
 * @author HUNG
 *
 */
public class JavaInvoker_ {

	/**
	 * @see com.caucho.quercus.env.JavaInvoker.callMethod(Env, QuercusClass, Value, Value[])
	 */
	public static Value callMethod(Env env,
									final QuercusClass qClass,
									final Value qThis,
									Value []args,
									final JavaInvoker _this)
	{
		if (!argsFlattenable(args))
			return _this.callMethod_orig(env, qClass, qThis, args);
		
		Value flattenedArgs = flattenArgs(args);
		
		return ShadowInterpreter.eval(flattenedArgs, new ShadowInterpreter.IBasicCaseHandler() {
			@Override
			public Value evalBasicCase(Value flattendedArgs_, Env env) {
				Value[] args_ = (Value[]) ((WrappedObject) flattendedArgs_).getObject();
				return _this.callMethod_orig(env, qClass, qThis, args_);
			}
		}, env);
	}
	
	/**
	 * Returns true if the arguments should be flattened.
	 * The implementation of this method must be consistent with the flattenArgs method.
	 * @see edu.iastate.hungnv.shadow.JavaInvoker_.flattenArgs(Value[])
	 */
	private static boolean argsFlattenable(Value[] args) {
		int len = args.length;
		Value[] argValues = new Value[len];
		
		for (int i = 0; i < len; i++) {
			if (args[i] instanceof Var)
				argValues[i] = ((Var) args[i]).getRawValue();
			else
				argValues[i] = args[i];
			
			// TODO Revise
			if (argValues[i] instanceof ScopedValue)
				argValues[i] = ((ScopedValue) argValues[i]).getValue();
			
			if (argValues[i] instanceof MultiValue)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Flattens an array of arguments.
	 * Since an argument can be of type Var, wrapping around a Value, this method proceeds in 3 steps:
	 * 	 1. Pre-processing: Unwrap the Values from Var arguments
	 *   2. Flattening: Flatten the array of unwrapped values
	 *   3. Post-processing: Wrap the flattened Values back into Var arguments
	 */
	private static Switch flattenArgs(Value[] args) {
		int len = args.length;
		Switch arraySwitch = new Switch();
		
		// 1. Pre-processing
		Value[] argValues = new Value[len];
		for (int i = 0; i < len; i++) {
			if (args[i] instanceof Var)
				argValues[i] = ((Var) args[i]).getRawValue();
			else
				argValues[i] = args[i];
			
			// TODO Revise
			if (argValues[i] instanceof ScopedValue)
				argValues[i] = ((ScopedValue) argValues[i]).getValue();
		}
		
		// 2. The real flattening operation takes place here
		Switch flattenedArgSet_ = flattenArray(argValues);
		
		// 3. Post-processing
		for (Case case_ : flattenedArgSet_) {
			Value[] flattenedArgValues = (Value[]) ((WrappedObject) case_.getValue()).getObject();
			Constraint constraint = case_.getConstraint();
			
			for (int i = 0; i < len; i++) {
				if (args[i] instanceof Var)
					argValues[i] = new Var(flattenedArgValues[i]);
				else
					argValues[i] = flattenedArgValues[i];
			}
			
			Case flattenedArgs = new Case(constraint, new WrappedObject(Arrays.copyOf(argValues, argValues.length)));
			arraySwitch.addCase(flattenedArgs);
		}
		
		return arraySwitch;
	}
	
	/**
	 * Flattens an array of values
	 */
	private static Switch flattenArray(Value[] values) {
		int len = values.length;
		Switch arraySwitch = new Switch();
		
		// Get all possible values of array elements
		Case[][] cases = new Case[len][];
		for (int i = 0; i < len; i++) {
			Switch switch_ = MultiValue.flatten(values[i]);
			cases[i] = switch_.getCases().toArray(new Case[0]);
		}
		
		// selection is an array indicating what value is being selected at a given position
		int[] selection = new int[len];
		for (int i = 0; i < len; i++)
			selection[i] = 0;
		
		// max is an array indicating the maximum number that can be assigned to a given position (selection[i] assumes values from 0 to max[i] - 1)
		int[] max = new int[len];
		for (int i = 0; i < len; i++)
			max[i] = cases[i].length;
		
		// flattenedValues will contain flattened values of the array elements
		Value[] flattenedValues = new Value[len];
		
		while (true) {
			Constraint constraint = Constraint.TRUE;
			
			// Get the current selected values
			int i;
			for (i = 0; i < len; i++) {
				Case case_ = cases[i][selection[i]];
				
				flattenedValues[i] = case_.getValue();
				constraint = Constraint.createAndConstraint(constraint, case_.getConstraint());
				
				// If the constraint is a contradiction, stop exploring all the combinations after that 
				if (constraint.isContradiction())
					break;
			}
			
			int focusPosition = i; // focusPosition is the position used to generate the next selection
			
			if (constraint.isSatisfiable()) { // This check is required
				Case newCase = new Case(constraint, new WrappedObject(Arrays.copyOf(flattenedValues, flattenedValues.length)));
				arraySwitch.addCase(newCase);
				
				focusPosition = len - 1;
			}
			
			// Generate new selection, exit if all combinations have been visited
			if (!generateNextSelection(selection, focusPosition, max))
				break;
		}
		
		return arraySwitch;
	}
	
	/**
	 * Generates the next selection based on the current selection, used in flattenArray method.
	 * In general, the value at the focusPosition will increase by 1, while the values after the focusPosition will be reset to 0's.
	 * @param selection			An array indicating what value is being selected at a given position
	 * @param focusPosition		The position used to generate the next selection
	 * @param max				An array indicating the maximum number that can be assigned to a given position (selection[i] assumes values from 0 to max[i] - 1)
	 * @return	true if there exists a next selection, false otherwise
	 */
	private static boolean generateNextSelection(int[] selection, int focusPosition, int[] max) {
		int i = focusPosition;
		while (i >= 0 && selection[i] == max[i] - 1)
			i--;
		
		if (i == -1)
			return false;
		
		selection[i]++;
		for (int j = i + 1; j < selection.length; j++) {
			selection[j] = 0;
		}
		
		return true;
	}
	  
}
