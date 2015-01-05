package edu.iastate.hungnv.empiricalstudy;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.statement.Statement;

import edu.iastate.hungnv.value.Switch;

/**
 * 
 * @author HUNG
 *
 */
public class EmpiricalStudy {
	
	/**
	 * Static instance of EmpiricalStudy
	 */
	public static EmpiricalStudy inst = new EmpiricalStudy();
    
    private StudyOnOutput studyOnOutput = new StudyOnOutput();
    private StudyOnTrace studyOnTrace = new StudyOnTrace();
    private StudyOnHeap studyOnHeap = new StudyOnHeap();
    private StudyOnTime studyOnTime = new StudyOnTime();
    
	/**
	 * Called when Env is started.
	 */
    public void envStarted() {
    	//studyOnOutput.envStarted();
    	//studyOnTrace.envStarted();
    	//studyOnHeap.envStarted();
    	//studyOnTime.envStarted();
    }
    
    /**
     * Called when Env is closed.
     */
    public void envClosed(Env env) {
    	//studyOnOutput.envClosed(env);
    	//studyOnTrace.envClosed(env);
    	//studyOnHeap.envClosed(env);
    	//studyOnTime.envClosed(env);
    }
    
    /**
     * Called when a statement is executed.
     */
    public void statementExecuted(Statement statement, Env env) {
    	//studyOnTrace.statementExecuted(statement, env);
    	//studyOnHeap.statementExecuted(statement, env);
    }
    
    /**
     * Called when a context is split.
     */
    public void contextSplit(Switch switch_) {
    	//studyOnTrace.contextSplit(switch_);
    }
 
}
