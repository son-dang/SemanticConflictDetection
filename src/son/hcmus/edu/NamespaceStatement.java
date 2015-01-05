package son.hcmus.edu;

import java.util.ArrayList;
import java.util.List;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.statement.Statement;

public class NamespaceStatement extends Statement{

	protected String _namespace = "";
	public NamespaceStatement(String namespace) {
		_namespace = namespace;
	}
	@Override
	public Value execute(Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return "namespace " + _namespace + ";";
	}
	
	@Override
	public boolean equal(Statement stm) {
		if (!(stm instanceof NamespaceStatement))
			return false;
		
		NamespaceStatement nsStm = (NamespaceStatement)stm;
		
		return this._namespace.equals(nsStm._namespace);
	}
	
	@Override
	public String buildModel(ArrayList<Statement> statements,
			List<String> branches) {
		return statements.get(0).toString();
	}
}
