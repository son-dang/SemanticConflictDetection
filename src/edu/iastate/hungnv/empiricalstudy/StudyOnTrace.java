package edu.iastate.hungnv.empiricalstudy;

import java.util.ArrayList;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.statement.Statement;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.Switch;

/**
 * 
 * @author HUNG
 *
 */
public class StudyOnTrace {
	
	public static final String traceTxtFile 	= "C:\\Users\\HUNG\\Desktop\\Varex Evaluation\\eval-trace.txt";
	public static final String splitsTxtFile 	= "C:\\Users\\HUNG\\Desktop\\Varex Evaluation\\eval-splits.txt";

	private ArrayList<String> statements;
	private ArrayList<String> contextSplits;
	
	/**
	 * Called when Env is started.
	 */
    public void envStarted() {
    	statements = new ArrayList<String>();
    	contextSplits = new ArrayList<String>();
    }
    
    /**
     * Called when Env is closed.
     */
    public void envClosed(Env env) {
    	Utils.writeListToFile(statements, traceTxtFile);
    	Utils.writeListToFile(contextSplits, splitsTxtFile);
    }
    
    /**
     * Called when a statement is executed.
     */
    public void statementExecuted(Statement statement, Env env) {
    	String location = statement.getLocation().prettyPrint();
    	FeatureExpr featureExpr = env.getEnv_().getScope().getConstraint().getFeatureExpr(); 
    	int featureCount = featureExpr.collectDistinctFeatures().size();
    	
    	statements.add(location + " # " + featureExpr + " # " + featureCount);
    }
    
    /**
     * Called when a context is split.
     */
    public void contextSplit(Switch switch_) {
    	int splits;
    	if (MultiValue.whenUndefined(switch_).isSatisfiable())
    		splits = switch_.getCases().size() + 1;
    	else
    		splits = switch_.getCases().size();
    	
    	contextSplits.add(String.valueOf(splits));
    }
    
}