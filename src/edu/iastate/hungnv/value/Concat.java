package edu.iastate.hungnv.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.caucho.quercus.env.StringBuilderValue;
import com.caucho.quercus.env.Value;
import edu.iastate.hungnv.constraint.Constraint;

/**
 * 
 * @author HUNG
 *
 */
@SuppressWarnings("serial")
public class Concat extends MultiValue implements Iterable<Value> {
	
	private ArrayList<Value> childNodes = new ArrayList<Value>(); // A size-2+ list of not-null regular values
	
	/*
	 * Constructors
	 */
	
	/**
	 * Constructor
	 * @param values	A size-2+ list of not-null regular values
	 */
	public Concat(List<Value> values) {
		childNodes.addAll(values);
	}

	/*
	 * Getters and setters
	 */
	
	public List<Value> getChildNodes() {
		return new ArrayList<Value>(childNodes);
	}
	
	/*
	 * Methods
	 */
	
	@Override
	public Switch flatten() {
		// Get all possible values of child nodes
		Switch[] cases = new Switch[childNodes.size()];
		for (int i = 0; i < childNodes.size(); i++) {
			cases[i] = MultiValue.flatten(childNodes.get(i));
		}
		
		return flatten(0, cases);
	}
	
	/**
	 * Flattens the child nodes of a Concat node starting from the idxToFlatten.
	 * @param idxToFlatten		
	 * @param cases				All possible values of the child nodes
	 */
	private Switch flatten(int idxToFlatten, Switch[] cases) {
		Switch finalResult = new Switch();
		
		if (idxToFlatten < childNodes.size()) {
			Switch result1 = cases[idxToFlatten];
			
			/*
			 * Handle the case where some child node is undefined (e.g., C = CONCAT(CHOICE(A, X, UNDEFINED), Y))
			 * In such cases, turn UNDEFINED into an empty string, so that C.flatten() = CHOICE(A, XY, Y).
			 * Without this handling, C.flatten() would be CHOICE(A, XY, UNDEFINED), which we don't want.
			 */
			Constraint undefinedCases = MultiValue.whenUndefined(result1);
			if (undefinedCases.isSatisfiable()) // This check is required
				result1.addCase(new Case(undefinedCases, new StringBuilderValue("")));
			
			for (Case case1 : result1) {
				Value value1 = case1.getValue();
				Constraint constraint1 = case1.getConstraint();
				
				Switch result2 = flatten(idxToFlatten + 1, cases);
				
				for (Case case2 : result2) {
					Value value2 = case2.getValue();
					Constraint constraint2 = case2.getConstraint();
					
					Constraint constraint = Constraint.createAndConstraint(constraint1, constraint2);
					
					if (constraint.isSatisfiable()) // This check is required
						finalResult.addCase(new Case(constraint, new StringBuilderValue(value1.toString() + value2.toString())));
				}
			}
		}
		else {
			finalResult.addCase(new Case(Constraint.TRUE, new StringBuilderValue("")));
		}
		
		return finalResult;
	}
	
	@Override
	public Value simplify(Constraint constraint) {
		Value simplifiedValue = MultiValue.simplify(childNodes.get(0), constraint);
		
		for (int i = 1; i < childNodes.size(); i++) {
			Value nextValue = MultiValue.simplify(childNodes.get(i), constraint);
			simplifiedValue = MultiValue.createConcatValue(simplifiedValue, nextValue, true);
		}

		return simplifiedValue;
	}
	
	@Override
	public Iterator<Value> iterator() {
		return childNodes.iterator();
	}
	
	/*
	 * Shadowed methods of the Value class
	 */
	
	@Override
	public String toString() {
		// TODO Produce a warning here
		
		StringBuilder result = new StringBuilder();
		for (Value childNode : childNodes)
			result.append(childNode.toString());
		
		return result.toString();
	}
	
	@Override
	public boolean isset() {
		return true;
	}
	
}