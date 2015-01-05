package edu.iastate.hungnv.shadow;

import java.util.ArrayList;

import son.hcmus.edu.VarexException;
import son.hcmus.edu.VarexException.VarexExceptionItem;

import com.caucho.quercus.QuercusDieException;
import com.caucho.quercus.QuercusException;
import com.caucho.quercus.QuercusExitException;
import com.caucho.quercus.QuercusRuntimeException;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.NullValue;
import com.caucho.quercus.env.QuercusLanguageException;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.statement.Statement;
import com.caucho.quercus.statement.TryStatement.Catch;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.scope.ScopedValue;
import edu.iastate.hungnv.value.Case;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.Switch;

public class TryStatement_ {
	public static Value execute(Env env, Statement _block,ArrayList<Catch> _catchList) {
		try {
			return _block.execute(env);
		} catch (VarexException ex) {
			Switch retSwitch = new Switch();
			//retSwitch.addCases(ex.getValue());
			Constraint exCombineContraint = Constraint.FALSE;
			for (VarexExceptionItem exItem : ex.getExceptionList()) {
				exCombineContraint = Constraint.createOrConstraint(exCombineContraint, exItem.getConstraint());
				if (exItem.getException() instanceof QuercusLanguageException) {
					QuercusLanguageException e = (QuercusLanguageException) exItem.getException();
					Value value = null;
					try {
						value = e.toValue(env);
					} catch (Throwable e1) {
						throw new QuercusRuntimeException(e1);
					}
					
					boolean exceptionCatched = false;
					for (int i = 0; i < _catchList.size(); i++) {
						Catch item = _catchList.get(i);
						env.getEnv_().enterNewScope(exItem.getConstraint());
						if (value != null && value.isA(item.getId().substring(item.getId().lastIndexOf("\\") + 1))
								|| item.getId().substring(item.getId().lastIndexOf("\\") + 1).equals("Exception")) {
							exceptionCatched = true;
							if (value != null)
								item.getExpr().evalAssignValue(env, value);
							else
								item.getExpr().evalAssignValue(env, NullValue.NULL);

							retSwitch.addCase(new Case(exItem.getConstraint(), item.getBlock().execute(env)));
							env.getEnv_().exitScope();
						}
					}
					
					if (!exceptionCatched)
						throw e;
				}
				else if (exItem.getException() instanceof QuercusDieException){
					QuercusDieException e = (QuercusDieException) exItem.getException();
					boolean exceptionCatched = false;
					for (int i = 0; i < _catchList.size(); i++) {
						Catch item = _catchList.get(i);

						if (item.getId().substring(item.getId().lastIndexOf("\\") + 1).equals("QuercusDieException")) {
							exceptionCatched = true;
							item.getExpr().evalAssignValue(env, env.createException(e));

							retSwitch.addCase(new Case(exItem.getConstraint(), item.getBlock().execute(env)));
						}
					}

					if (!exceptionCatched)
						throw e;
				}
				else if (exItem.getException() instanceof QuercusExitException){
					QuercusExitException e = (QuercusExitException) exItem.getException();
					boolean exceptionCatched = false;
					for (int i = 0; i < _catchList.size(); i++) {
						Catch item = _catchList.get(i);

						if (item.getId().substring(item.getId().lastIndexOf("\\") + 1).equals("QuercusExitException")) {
							exceptionCatched = true;
							item.getExpr().evalAssignValue(env, env.createException(e));

							retSwitch.addCase(new Case(exItem.getConstraint(), item.getBlock().execute(env)));
						}
					}

					if (!exceptionCatched)
						throw e;
				}
				else {
					Exception e = exItem.getException();
					for (int i = 0; i < _catchList.size(); i++) {
						Catch item = _catchList.get(i);

						if (item.getId().equals("Exception")) {
							Throwable cause = e;

							// if (e instanceof QuercusException && e.getCause() !=
							// null)
							// cause = e.getCause();

							item.getExpr().evalAssignValue(env,
									env.createException(cause));

							retSwitch.addCase(new Case(exItem.getConstraint(), item.getBlock().execute(env)));
						}
					}

					if (e instanceof QuercusException)
						throw (QuercusException) e;
					else
						throw new QuercusException(e);
				}
			}
			
			retSwitch.addCase(new Case(Constraint.createNotConstraint(exCombineContraint), ex.getValue()));
			return MultiValue.createSwitchValue(retSwitch);
		}
	}
}
