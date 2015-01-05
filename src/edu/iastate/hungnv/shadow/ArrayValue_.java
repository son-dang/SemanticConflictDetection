package edu.iastate.hungnv.shadow;

import com.caucho.quercus.env.ArrayValue;
import com.caucho.quercus.env.Callable;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;

import edu.iastate.hungnv.callable.MultiCallable;
import edu.iastate.hungnv.value.MultiValue;

/**
 * 
 * @author HUNG
 *
 */
public class ArrayValue_ {

	/**
	 * @see com.caucho.quercus.env.ArrayValue.toCallable(Env)
	 */
	public static Callable toCallable(Env env, Value obj, final Value nameV, final ArrayValue this_) {
    	if (obj instanceof MultiValue || nameV instanceof MultiValue) {
    		// Eval basic case
    		ShadowInterpreter.IBasicCaseHandler
    			handler = new ShadowInterpreter.IBasicCaseHandler() {
    			
    			@Override
    			public Value evalBasicCase(Value value, Env env) {
    				return ArrayValue_.toCallable2(env, value, nameV, this_);
    			}
    		};
    		
    		// Eval all cases
    		Value retValue = ShadowInterpreter.eval(obj, handler, env);
    		
    		if (!(retValue instanceof MultiValue))
    			return ((Callable) ((WrappedObject) retValue).getObject());
    		else
    			return new MultiCallable((MultiValue) retValue);
    	}
    	else {
    		return this_.toCallable_basic(env, obj, nameV);
    	}
	}
	
	/**
	 * @param obj  must not be a MultiValue
	 */
	private static Value toCallable2(Env env, final Value obj, Value nameV, final ArrayValue this_) {
    	if (nameV instanceof MultiValue) {
    		// Eval basic case
    		ShadowInterpreter.IBasicCaseHandler
    			handler = new ShadowInterpreter.IBasicCaseHandler() {
    			
    			@Override
    			public Value evalBasicCase(Value value, Env env) {
    				return new WrappedObject(this_.toCallable_basic(env, obj, value));
    			}
    		};
    		
    		// Eval all cases
    		Value retValue = ShadowInterpreter.eval(nameV, handler, env);
    		
    		return retValue;
        	}
    	else {
    		return new WrappedObject(this_.toCallable_basic(env, obj, nameV));
    	}
	}
	
}
