package edu.iastate.hungnv.shadow;

import son.hcmus.edu.VarexException;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.NullValue;
import com.caucho.quercus.env.QuercusLanguageException;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.statement.BlockStatement;
import com.caucho.quercus.statement.Statement;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.constraint.Constraint.Result;
import edu.iastate.hungnv.debug.Debugger;
import edu.iastate.hungnv.util.Logging;
import edu.iastate.hungnv.value.Case;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.Switch;

/**
 * 
 * @author HUNG
 *
 */
public class BlockStatement_ {
	
	/**
	 * @see com.caucho.quercus.statement.BlockStatement.execute(Env)
	 */
	public static Value execute(Env env, BlockStatement _this, Statement[] _statements) {
		
		/*
		 * These are used to handle continue, break, return.
		 */
		Switch combinedReturnValue = null; // The combined return value
		Constraint combinedConstraint = Constraint.TRUE; // The combined constraint each time the execution enters a new scope
		int timesEnteringScope = 0; // The number of times the execution enters a new scope

		VarexException varexEx = new VarexException();
		for (int i = 0; i < _statements.length; i++) {
			Statement statement = _statements[i];
	
	//        Logging.LOGGER.info("Executing " + statement.getLocation().prettyPrint() + " (Constraint = " + env.getEnv_().getScope().getConstraint() + ")");
	        
	        Debugger.inst.checkBreakpoint(statement.getLocation());
	        
	        //Value retValue = statement.execute(env);
	        // CODE MODIFIED BY SON
	        	Value retValue = null;
	        	Constraint beforeExecuteConstraint = env.getEnv_().getScope().getConstraint();
	        	try{
	        		retValue = statement.execute(env);
	        	}
	        	catch(Exception ex){
	        		varexEx.addItem(env.getEnv_().getScope().getConstraint(), ex);
	        		while(!env.getEnv_().getScope().getConstraint().toString().equals(beforeExecuteConstraint.toString()))
	        			env.getEnv_().exitScope();
	        		
	        	}
	        // END OF MODIFIED CODE
	        
	        if (retValue != null) {
	        	/*
	        	 * Handle the trivial case
	        	 */
	        	if (combinedReturnValue == null && !(retValue instanceof MultiValue))
	        		return retValue;
	        	
	        	/*
	        	 * Handle more complex cases
	        	 */
	        	if (combinedReturnValue == null)
	        		combinedReturnValue = new Switch();
	        	
	        	for (Case case_ : MultiValue.flatten(retValue)) {
	        		Constraint newConstraint = Constraint.createAndConstraint(combinedConstraint, case_.getConstraint());
	        		Value newValue = case_.getValue();
	        		if (newValue != null && newConstraint.isSatisfiable()) // This check is required
	        			combinedReturnValue.addCase(new Case(newConstraint, newValue));
	        	}
	        	
	        	Constraint whenNull = MultiValue.whenNull(retValue);
				combinedConstraint = Constraint.createAndConstraint(combinedConstraint, whenNull);
	        	
				Constraint aggregatedConstraint = env.getEnv_().getScope().getConstraint();
				Constraint.Result result = aggregatedConstraint.tryAddingConstraint(whenNull);
				boolean constraintAlwaysTrue = (result == Result.THE_SAME);
				boolean constraintAlwaysFalse = (result == Result.ALWAYS_FALSE);
	        		
				if (constraintAlwaysFalse)
					break;
				
				if (!constraintAlwaysTrue) {
					timesEnteringScope++;
					env.getEnv_().enterNewScope(whenNull);
				}
	        }
		}
		
		// CODE ADDED BY SON
		// handle exception case
		if (varexEx.getExceptionList().size() > 0){
			if (combinedReturnValue == null)
				varexEx.setValue(combinedReturnValue);
			else{
				for (int i = 1; i <= timesEnteringScope; i++)
					env.getEnv_().exitScope();
				if (combinedConstraint.isSatisfiable()){
					combinedReturnValue.addCase(new Case(combinedConstraint, null));
					varexEx.setValue(MultiValue.createSwitchValue(combinedReturnValue));
				}
			}
			throw varexEx;
		}
		// END OF ADDED CODE
		
    	/*
    	 * Handle the trivial case
    	 */
		if (combinedReturnValue == null)
			return null;
		
    	/*
    	 * Handle more complex cases
    	 */
		for (int i = 1; i <= timesEnteringScope; i++)
			env.getEnv_().exitScope();
		
		if (combinedConstraint.isSatisfiable()) // This check is required
			combinedReturnValue.addCase(new Case(combinedConstraint, null));
		
		return MultiValue.createSwitchValue(combinedReturnValue);
	}

}
