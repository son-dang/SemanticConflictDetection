package edu.iastate.hungnv.value;

import com.caucho.quercus.env.Value;
import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.constraint.Constraint.Result;

/**
 * 
 * @author HUNG
 *
 */
@SuppressWarnings("serial")
public class Choice extends MultiValue {

	private Constraint constraint;
	private Value value1;	// A regular value, not null
	private Value value2;	// A regular value, not null
	
	/**
	 * Constructor
	 * @param constraint
	 * @param value1	A regular value, not null
	 * @param value2	A regular value, not null
	 */
	public Choice(Constraint constraint, Value value1, Value value2) {
		this.constraint = constraint;
		this.value1 = value1;
		this.value2 = value2;
	}
	
	/*
	 * Getters and setters
	 */
	
	public Constraint getConstraint() {
		return constraint;
	}
	
	public Value getValue1() {
		return value1;
	}
	
	public Value getValue2() {
		return value2;
	}
        
        public void setValue1(Value value){
            this.value1 = value;
        }
        
        public void setValue2(Value value){
            this.value2 = value;
        }
	
	/*
	 * Methods
	 */
	
	@Override
	public Switch flatten() {
		Switch switch_ = new Switch();
		
		Switch cases1 = MultiValue.flatten(value1);
		Switch cases2 = MultiValue.flatten(value2);
		
		for (Case case_ : cases1) {
			Value value = case_.getValue();
			Constraint constraint = Constraint.createAndConstraint(this.constraint, case_.getConstraint());
			
			if (constraint.isSatisfiable()) // This check is required
				switch_.addCase(new Case(constraint, value));
		}
		
		Constraint notConstraint = Constraint.createNotConstraint(this.constraint);
		for (Case case_ : cases2) {
			Value value = case_.getValue();
			Constraint constraint = Constraint.createAndConstraint(notConstraint, case_.getConstraint());
			
			if (constraint.isSatisfiable()) // This check is required
				switch_.addCase(new Case(constraint, value));
		}
		
		return switch_;
	}
	
	@Override
	public Value simplify(Constraint constraint) {
		Constraint.Result result = constraint.tryAddingConstraint(this.constraint);
		
		if (result == Result.THE_SAME) {
			return MultiValue.simplify(value1, constraint);
		}
		else if (result == Result.ALWAYS_FALSE)
			return MultiValue.simplify(value2, constraint);
		
		Value trueBranchValue = MultiValue.simplify(value1, Constraint.createAndConstraint(constraint, this.constraint));
		Value falseBranchValue = MultiValue.simplify(value2,  Constraint.createAndConstraint(constraint, Constraint.createNotConstraint(this.constraint)));
		
		return MultiValue.createChoiceValue(this.constraint, trueBranchValue, falseBranchValue);
	}
	
	/**
	 * Returns the inverse of this Choice value.
	 * For example, CHOICE(A, x, y) => CHOICE(!A, y, x)
	 */
	public Choice getInverse() {
		return new Choice(Constraint.createNotConstraint(constraint), value2, value1);
	}
	
	/*
	 * Shadowed methods of the Value class
	 */
	
	@Override
	public String toString() {
		// TODO Produce a warning here
		
		return "CHOICE(" + constraint.toString() + ", " + value1.toString() + ", " + value2.toString() + ")";
	}
	
	@Override
	public Value get(Value index) {
		return MultiValue.createChoiceValue(constraint, value1.get(index), value2.get(index));
	}
	
}
