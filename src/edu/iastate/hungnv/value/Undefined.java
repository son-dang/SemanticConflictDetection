package edu.iastate.hungnv.value;

import com.caucho.quercus.env.Value;

import edu.iastate.hungnv.constraint.Constraint;

/**
 * 
 * @author HUNG
 *
 */
@SuppressWarnings("serial")
public class Undefined extends MultiValue {

	public static final Undefined UNDEFINED = new Undefined();

	/**
	 * Private constructor
	 */
	private Undefined() {
	}
	
	/*
	 * Methods
	 */
	
	@Override
	public Switch flatten() {
		// Return an empty Switch instead of a Switch of Case(Constraint.TRUE, this)
		// because Case can only take a Quercus value. 
		
		return new Switch();
	}

	@Override
	public Value simplify(Constraint constraint) {
		return this;
	}
	
	/*
	 * Shadowed methods of the Value class
	 */
	
	@Override
	public String toString() {
		return "UNDEFINED";
	}
	
	@Override
	public Value get(Value index) {
	    return UNDEFINED;
	}
	
}
