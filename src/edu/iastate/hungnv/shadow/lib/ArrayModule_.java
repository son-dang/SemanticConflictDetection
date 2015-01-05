package edu.iastate.hungnv.shadow.lib;

import com.caucho.quercus.annotation.Optional;
import com.caucho.quercus.env.ArrayValue;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.LongValue;
import com.caucho.quercus.env.NullValue;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.lib.ArrayModule;

import edu.iastate.hungnv.shadow.ShadowInterpreter;
import edu.iastate.hungnv.value.MultiValue;

/**
 * 
 * @author HUNG
 *
 */
public class ArrayModule_ {
	
	/**
	 * @see com.caucho.quercus.lib.ArrayModule.array_filter(Env, ArrayValue, Value)
	 */
	public static Value array_filter(final Env env,
	                                   final ArrayValue array,
	                                   final @Optional Value callbackName) {
    	if (callbackName instanceof ArrayValue 
    		&& ((ArrayValue) callbackName).get(LongValue.ZERO) instanceof MultiValue) {
    		
    		// Pre-processing
        	Value obj = ((ArrayValue) callbackName).get(LongValue.ZERO);
    		
    		// Eval basic case
    		ShadowInterpreter.IBasicCaseHandler
    			handler = new ShadowInterpreter.IBasicCaseHandler() {
    			
    			@Override
    			public Value evalBasicCase(Value value, Env env) {
    				if (value instanceof NullValue) // TODO Revise what to do when this happens (probably due to unimplemented MultiValue.isset?)
    					return NullValue.NULL;
    				
    				((ArrayValue) callbackName).put(LongValue.ZERO, value);
    				
    				return ArrayModule.array_filter_orig(env, array, callbackName);
    			}
    		};
    		
    		// Eval all cases
    		Value retValue = ShadowInterpreter.eval(obj, handler, env);
    		
    		// Post-processing
    		((ArrayValue) callbackName).put(LongValue.ZERO, obj);
    		
    		return retValue;
    	}
    	else {
    		return ArrayModule.array_filter_orig(env, array, callbackName);
    	}
	}
	
}
