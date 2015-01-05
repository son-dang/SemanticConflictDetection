package edu.iastate.hungnv.shadow;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.constraint.Constraint.Result;
import edu.iastate.hungnv.empiricalstudy.EmpiricalStudy;
import edu.iastate.hungnv.value.Case;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.Switch;

/**
 * 
 * @author HUNG
 *
 */
public class ShadowInterpreter {
	
	public interface IBasicCaseHandler {
		
		/**
		 * Execute the code with a given Quercus value.
		 * @param value	A Quercus value, not null
		 * @return 		The result of the execution
		 */
		public Value evalBasicCase(Value value, Env env);
	}
	
	/**
	 * Execute the code with a given regular Value.
	 * @param value		A regular value, not null
	 * @param handler	The handler for a *Quercus* value
	 */
	public static Value eval(Value value, IBasicCaseHandler handler, Env env) {
		Switch combinedReturnValue = new Switch();
		
		for (Case case_ : MultiValue.flatten(value)) {
			Value flattenedValue = case_.getValue();
			Constraint constraint = case_.getConstraint();
			
			Constraint aggregatedConstraint = env.getEnv_().getScope().getConstraint();
                        
               //         if (constraint.toString().contains("!"))
               //             continue;
                        
			Constraint.Result result = aggregatedConstraint.tryAddingConstraint(constraint);
			boolean constraintAlwaysTrue = (result == Result.THE_SAME);
			boolean constraintAlwaysFalse = (result == Result.ALWAYS_FALSE);
			
			if (constraintAlwaysFalse)
				continue;
			
			if (!constraintAlwaysTrue)
				env.getEnv_().enterNewScope(constraint);
			
			// EMPI
			//EmpiricalStudy.inst.recordEvalBasicCase(env);
						
			//----- EVAL BASIC CASE -----
			Value retValue = handler.evalBasicCase(flattenedValue, env);    
		    //---------------------------
			
			if (!constraintAlwaysTrue)
			   	env.getEnv_().exitScope();
			
			if (constraintAlwaysTrue)
				return retValue;

			/*
			 * Handle retValue
			 */
			if (retValue instanceof MultiValue) {
				for (Case c : ((MultiValue) retValue).flatten()) {
					Constraint con = Constraint.createAndConstraint(constraint, c.getConstraint());
					Value val = c.getValue();
					
					if (con.isSatisfiable()) // This check is required
						combinedReturnValue.addCase(new Case(con, val));
				}
			}
			else
				combinedReturnValue.addCase(new Case(constraint, retValue));
		}
		
		// TODO Check if combinedReturnValue is an empty Switch and debug why this happens. 
		
		// EMPI
		EmpiricalStudy.inst.contextSplit(combinedReturnValue);

		return MultiValue.createSwitchValue(combinedReturnValue);
	}

}
