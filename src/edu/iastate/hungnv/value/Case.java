package edu.iastate.hungnv.value;

import com.caucho.quercus.env.Value;

import edu.iastate.hungnv.constraint.Constraint;

/**
 * 
 * @author HUNG
 *
 */
public class Case {
	
	private Constraint constraint;	// The constraint must be satisfiable
	private Value value;			// A Quercus value, not null
	
	/**
	 * Constructor
	 * @param constraint	The constraint must be satisfiable
	 * @param value			A Quercus value, not null
	 */
	public Case(Constraint constraint, Value value) {
		this.constraint = constraint;
		this.value = value;
	}

	/*
	 * Getters and setters
	 */
	
	/**
	 * @return The constraint (must be satisfiable)
	 */
	public Constraint getConstraint() {
		return constraint;
	}

	/**
	 * @return The Quercus value (not null)
	 */
	public Value getValue() {
		return value;
	}
	
	/*
	 * Methods
	 */

	@Override
	public String toString() {
		return "CASE(" + constraint.toString() + ", " + value.toString() + ")";
	}
	
}
