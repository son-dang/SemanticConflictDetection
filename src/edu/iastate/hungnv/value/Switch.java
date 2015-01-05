package edu.iastate.hungnv.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.caucho.quercus.env.Value;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.constraint.Constraint.Result;

/**
 * 
 * @author HUNG
 *
 */
@SuppressWarnings("serial")
public class Switch extends MultiValue implements Iterable<Case> {
	
	private List<Case> cases = new ArrayList<Case>();
	
	/*
	 * Getters and setters
	 */
	
	public void addCase(Case case_) {
		cases.add(case_);
	}
	
	public void addCases(Switch switch_) {
		cases.addAll(switch_.cases);
	}
	
	public List<Case> getCases() {
		return new ArrayList<Case>(cases);
	}
	
	/*
	 * Methods
	 */
	
	@Override
	public Switch flatten() {
		return this;
	}
	
	@Override
	public Value simplify(Constraint constraint) {
		Switch switch_ = new Switch();
		for (Case case_ : cases) {
			Constraint.Result result = constraint.tryAddingConstraint(case_.getConstraint());
			
			if (result == Result.THE_SAME)
				return case_.getValue();
			
			if (result == Result.UNDETERMINED)
				switch_.addCase(case_);
			
			// Do nothing if result == ResultType.ALWAYS_FALSE
		}
		
		return switch_;
	}

	@Override
	public Iterator<Case> iterator() {
		return cases.iterator();
	}
	
	/*
	 * Shadowed methods of the Value class
	 */
	
	@Override
	public String toString() {
		StringBuilder string = new StringBuilder();
		string.append("SWITCH(");
		
		for (Case case_ : cases) {
			if (cases.indexOf(case_) > 0)
				string.append(", ");
			
			string.append(case_.toString());
		}
		string.append(")");
		
		return string.toString();
	}

}
