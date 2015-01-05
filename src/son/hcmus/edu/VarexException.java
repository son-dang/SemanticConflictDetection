package son.hcmus.edu;

import java.util.ArrayList;

import com.caucho.quercus.env.Value;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.value.Switch;

public class VarexException extends RuntimeException{
	protected ArrayList<VarexExceptionItem> _exList = new ArrayList<VarexExceptionItem>();
	protected Value _blockExeValue = null;
	
	public void addItem(Constraint constraint, Exception ex){
		_exList.add(new VarexExceptionItem(constraint, ex));
	}
	
	public ArrayList<VarexExceptionItem> getExceptionList(){
		return this._exList;
	}
	
	public Value getValue(){
		return this._blockExeValue;
	}
	
	public void setValue(Value sw){
		this._blockExeValue = sw;
	}
	
	public class VarexExceptionItem{
		protected Constraint constraint = Constraint.TRUE;
		protected Exception ex = null;
		
		public Constraint getConstraint(){
			return this.constraint;
		}
		
		public Exception getException(){
			return this.ex;
		}
		
		public VarexExceptionItem(Constraint constraint, Exception ex){
			this.constraint = constraint;
			this.ex = ex;
		}
	}
}
