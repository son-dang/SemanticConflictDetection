package edu.iastate.hungnv.callable;

import com.caucho.quercus.env.ArrayValue;
import com.caucho.quercus.env.Callable;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;

import edu.iastate.hungnv.shadow.ShadowInterpreter;
import edu.iastate.hungnv.shadow.WrappedObject;
import edu.iastate.hungnv.util.Logging;
import edu.iastate.hungnv.value.MultiValue;

/**
 * 
 * @author HUNG
 *
 */
public class MultiCallable implements Callable {
	
	private MultiValue multiCallableValue;
	
	/**
	 * Constructor
	 * @param multiCallableValue a MultiValue whose single value is a WrappedObject of Callable
	 */
	public MultiCallable(MultiValue multiCallableValue) {
		this.multiCallableValue = multiCallableValue;
	}
	
	@Override
	public String getCallbackName() {
		Logging.LOGGER.fine("Unsupported operation for a MultiCallable.");
		
		return null;
	}

	@Override
	public boolean isValid(Env env) {
		// TODO Revise
		
		return true;
	}

	@Override
	public Value call(Env env) {
		return ShadowInterpreter.eval(multiCallableValue, new ShadowInterpreter.IBasicCaseHandler() {
			@Override
			public Value evalBasicCase(Value value, Env env) {
				return ((Callable) ((WrappedObject) value).getObject()).call(env);
			}
		}, env);
	}

	@Override
	public Value call(Env env, final Value a1) {
		return ShadowInterpreter.eval(multiCallableValue, new ShadowInterpreter.IBasicCaseHandler() {
			@Override
			public Value evalBasicCase(Value value, Env env) {
				return ((Callable) ((WrappedObject) value).getObject()).call(env, a1);
			}
		}, env);
	}

	@Override
	public Value call(Env env, final Value a1, final Value a2) {
		return ShadowInterpreter.eval(multiCallableValue, new ShadowInterpreter.IBasicCaseHandler() {
			@Override
			public Value evalBasicCase(Value value, Env env) {
				return ((Callable) ((WrappedObject) value).getObject()).call(env, a1, a2);
			}
		}, env);
	}

	@Override
	public Value call(Env env, final Value a1, final Value a2, final Value a3) {
		return ShadowInterpreter.eval(multiCallableValue, new ShadowInterpreter.IBasicCaseHandler() {
			@Override
			public Value evalBasicCase(Value value, Env env) {
				return ((Callable) ((WrappedObject) value).getObject()).call(env, a1, a2, a3);
			}
		}, env);
	}

	@Override
	public Value call(Env env, final Value a1, final Value a2, final Value a3, final Value a4) {
		return ShadowInterpreter.eval(multiCallableValue, new ShadowInterpreter.IBasicCaseHandler() {
			@Override
			public Value evalBasicCase(Value value, Env env) {
				return ((Callable) ((WrappedObject) value).getObject()).call(env, a1, a2, a3, a4);
			}
		}, env);
	}

	@Override
	public Value call(Env env, final Value a1, final Value a2, final Value a3, final Value a4, final Value a5) {
		return ShadowInterpreter.eval(multiCallableValue, new ShadowInterpreter.IBasicCaseHandler() {
			@Override
			public Value evalBasicCase(Value value, Env env) {
				return ((Callable) ((WrappedObject) value).getObject()).call(env, a1, a2, a3, a4, a5);
			}
		}, env);
	}

	@Override
	public Value call(Env env, final Value[] args) {
		return ShadowInterpreter.eval(multiCallableValue, new ShadowInterpreter.IBasicCaseHandler() {
			@Override
			public Value evalBasicCase(Value value, Env env) {
				return ((Callable) ((WrappedObject) value).getObject()).call(env, args);
			}
		}, env);
	}

	@Override
	public Value callArray(Env env, final ArrayValue array, final Value key, final Value a1) {
		return ShadowInterpreter.eval(multiCallableValue, new ShadowInterpreter.IBasicCaseHandler() {
			@Override
			public Value evalBasicCase(Value value, Env env) {
				return ((Callable) ((WrappedObject) value).getObject()).call(env, array, key, a1);
			}
		}, env);
	}

	@Override
	public Value callArray(Env env, final ArrayValue array, final Value key, final Value a1,
			final Value a2) {
		return ShadowInterpreter.eval(multiCallableValue, new ShadowInterpreter.IBasicCaseHandler() {
			@Override
			public Value evalBasicCase(Value value, Env env) {
				return ((Callable) ((WrappedObject) value).getObject()).call(env, array, key, a1, a2);
			}
		}, env);
	}

	@Override
	public Value callArray(Env env, final ArrayValue array, final Value key, final Value a1,
			final Value a2, final Value a3) {
		return ShadowInterpreter.eval(multiCallableValue, new ShadowInterpreter.IBasicCaseHandler() {
			@Override
			public Value evalBasicCase(Value value, Env env) {
				return ((Callable) ((WrappedObject) value).getObject()).call(env, array, key, a1, a2, a3);
			}
		}, env);
	}

}
