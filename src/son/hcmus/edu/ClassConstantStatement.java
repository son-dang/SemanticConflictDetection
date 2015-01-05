package son.hcmus.edu;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.expr.Expr;
import com.caucho.quercus.statement.Statement;

public class ClassConstantStatement extends Statement{
	protected Expr _expr = null;
	protected String _key = "";
	
	public ClassConstantStatement(String key, Expr expr){
		this._key = key;
		this._expr = expr;
	}
	
	@Override
	public Value execute(Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder("const ");
		ret.append(_key + " = " + _expr.toString());
		return "$__Fields__ .='" + ret.toString().replace("'", "\\'") + ";';\r\n";
	}
	
	@Override
	public boolean equal(Statement stm) {
		if (!(stm instanceof ClassConstantStatement))
			return false;
		
		ClassConstantStatement ccStm = (ClassConstantStatement)stm;
		
		if (!ccStm._key.equals(this._key))
			return false;
		
		return (ccStm._expr.toString().equals(this._expr.toString()));
	}
}
