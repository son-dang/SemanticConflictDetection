package edu.iastate.hungnv.shadow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import son.hcmus.edu.Vaseco;

import com.caucho.quercus.Location;
import com.caucho.quercus.QuercusContext;
import com.caucho.quercus.env.ArrayValue;
import com.caucho.quercus.env.ArrayValue.Entry;
import com.caucho.quercus.env.ArrayValueImpl;
import com.caucho.quercus.env.BooleanValue;
import com.caucho.quercus.env.ConstStringValue;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.LongValue;
import com.caucho.quercus.env.NullValue;
import com.caucho.quercus.env.ObjectExtValue;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.env.Var;
import com.caucho.quercus.expr.Expr;
import com.caucho.quercus.expr.ThisMethodExpr;
import com.caucho.quercus.function.AbstractFunction;
import com.caucho.quercus.page.QuercusPage;
import com.caucho.quercus.program.Function;
import com.caucho.quercus.statement.ClassDefStatement;
import com.caucho.quercus.statement.ExprStatement;
import com.caucho.quercus.statement.Statement;
import com.caucho.vfs.Path;
import com.caucho.vfs.Vfs;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.debug.OutputViewer;
import edu.iastate.hungnv.regressiontest.RegressionTest;
import edu.iastate.hungnv.util.Logging;
import edu.iastate.hungnv.value.Choice;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.MultiValue.IOperation;

/**
 * 
 * @author HUNG
 *
 */
public class Functions {
	
	public static class __CHECK__{
		public static Value evalImpl(Env env, Value[] args){
			if (args.length == 1){
				ArrayValue ret = new ArrayValueImpl();
				ArrayValue executableFiles = new ArrayValueImpl();
				ArrayValue unexecutableFiles = new ArrayValueImpl();
				
				// directory to project base branch
				String projectDir = args[0].toString();
				
				// reset test case map
				Env_.testCaseMap = new HashMap<String, ClassDefStatement>();
				
				// get all php files in project
				File projectFile = new File(projectDir);
				Collection<File> files = FileUtils.listFiles(projectFile, new String[]{"php"}, true);
				
				// parse all php files to get test cases.
				QuercusContext quercusContext = env.getQuercus();
				Path pwd = Vfs.lookup();
				ArrayList<QuercusPage> pages = new ArrayList<QuercusPage>();
				for(File file : files){
					try {
						QuercusPage page = quercusContext.parse(pwd.lookup(file.getAbsolutePath()));
						pages.add(page);
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				// check if test case is executable
				int nExecutable = 0;
				for (Map.Entry<String, ClassDefStatement> entry : Env_.testCaseMap.entrySet()){
					ClassDefStatement classDefStm = entry.getValue();
					ArrayList<Statement> exprStms = classDefStm.findStatements(ExprStatement.class, true);
					boolean executable = true;
					
					for(Statement stm : exprStms){
						ExprStatement exprStm = (ExprStatement)stm;
						if (!(exprStm.getExpr() instanceof ThisMethodExpr))
							continue;
						ThisMethodExpr thisMethodExpr = (ThisMethodExpr)exprStm.getExpr();
						if (thisMethodExpr.toString().contains("this")){
							executable = false;
							for(AbstractFunction abFunc : classDefStm.getClassDef()._functionMap.values()){
								if (!(abFunc instanceof Function))
									continue;
								
								Function func = (Function)abFunc;
								if (func.getName().equals(thisMethodExpr.getName())){
									executable = true;
									break;
								}
							}
						}
					}
					
					String fileName = classDefStm.getLocation().getFileName();
					fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
					if (executable){
						executableFiles.append(new ConstStringValue(fileName));
						nExecutable++;
					}
					else{
						unexecutableFiles.append(new ConstStringValue(fileName));
					}
				}
				
				ret.put("nCase", Env_.testCaseMap.size());
				ret.put("nExecutable", nExecutable);
				ret.put(new ConstStringValue("unexecutableFiles"), unexecutableFiles);
				ret.put(new ConstStringValue("executableFiles"), executableFiles);
				return ret;
				//return new LongValue(nExecutable);
			}
			return NullValue.NULL;
		}
	}
	
    public static class __RUN__{
            public static Value evalImpl(Env env, Value[] args){
                if (args.length == 2){
                    Vaseco.run(env, args[0].toString(), args[1].toString());
                }
                return NullValue.NULL;
            }
        }
    
    public static class __TEST__{
            public static Value evalImpl(Value[] args){
                if (args.length == 1){
                    Env_.testClass = args[0].toString();
                }
                return NullValue.NULL;
            }
        }
    
    public static class __VERSION__{
            public static Value evalImpl(Value[] args){
                if (args.length == 2){
                    String branchName = args[0].toString();
                    String branchDir = args[1].toString();
                    Env_.BranchDirectoryMap.put(branchName, branchDir);
                }
                return NullValue.NULL;
            }
        }
    
    public static class __ExpectOutputString__{
    	public static Value evalImpl(Env env, Value[] args, Location location){
    		Env_.expectOutput = args[0].toString();
    		Env_.expectOutputLoc = location;
    		OutputViewer.inst.setOutputValue(new ConstStringValue(""));
    		return NullValue.NULL;
        }
    }
    
    public static class __ExpectOutputRegex__{
    	public static Value evalImpl(Env env, Value[] args, Location location){
    		Env_.expectOutputRegex = args[0].toString();
    		Env_.expectOutputRegexLoc = location;
    		OutputViewer.inst.setOutputValue(new ConstStringValue(""));
    		return NullValue.NULL;
        }
    }
    
	public static class __CHOICE__ {
		
		public static Value evalImpl(Value[] args) {
			if (args.length == 3) {
                            if (args[0].isMultiValue()){
                                Constraint trueConstraint = MultiValue.whenTrue(args[0]);
                                return MultiValue.createChoiceValue(trueConstraint, args[1], args[2]);
                            }
                            
                            Choice retValue = (Choice) MultiValue.createChoiceValue(Constraint.createConstraint(args[0].toString()), args[1], args[2]);
                            return retValue;
                            //return MultiValue.createChoiceValue(Constraint.createConstraint(args[0].toString()), args[1], args[2]);
			}
			else if (args.length == 1) {
                            Choice retValue = (Choice) MultiValue.createChoiceValue(Constraint.createConstraint(args[0].toString()), BooleanValue.TRUE, BooleanValue.FALSE);
                            return retValue;
                            //return MultiValue.createChoiceValue(Constraint.createConstraint(args[0].toString()), BooleanValue.TRUE, BooleanValue.FALSE);
			}
			else
                            return NullValue.NULL; // Should not reach here
		}
		
	}
	
	public static class __ASSERT__ {
		
		public static Value evalImpl(Env env, Expr[] args, Location location) {
			if (args.length != 1 && args.length != 2)
				return NullValue.NULL; // Should not reach here
			
			Value assertedValue = Expr.evalArgs(env, args)[0];
			Constraint falseConstraint = MultiValue.whenFalse(assertedValue);
			if (falseConstraint.isSatisfiable()){
                            //    Logging.LOGGER.severe("Assert value " + args[0] + " in constraint " + env.getEnv_().getScope().getConstraint() + " is false when " + falseConstraint);
                            if (args.length == 2)
                                Env_.errorLog.append(args[1].toString());
                            else
                                Env_.errorLog.append("Assert value ").append(args[0].toString())
                                .append(" in " + location.getFileName())
                                .append(" at line ").append(location.getLineNumber())
                                .append(" is false");
                            Env_.errorLog.append(" when ").append(falseConstraint.toString())
                                    .append("\r\n");
                        }
			// Optional:
			Constraint undefinedConstraint = MultiValue.whenUndefined(assertedValue);
                     /*   if (undefinedConstraint.isSatisfiable()){
                                Env_.errorLog.append("Assert value ").append(args[0].toString())
                                    .append(" is undifined when ").append(undefinedConstraint.toString())
                                    .append("\r\n");
				//Logging.LOGGER.severe("Assertion Error: " + location.prettyPrint() + ". Asserted value is undefined when " + undefinedConstraint.toString());
                        }*/
			return NullValue.NULL;
		}
		
	}
	
	public static class __DEBUG__ {
		
		public static Value evalImpl(Value[] args, Location location) {
			Logging.LOGGER.info("Breakpoint: " + location.prettyPrint());
			
			return NullValue.NULL;
		}
		
	}
	
	public static class __PLUGINS__ {
		
		public static Value evalImpl(Value[] args) {
			return RegressionTest.inst.loadPlugins();
		}
		
	}
	
	// EMPI For Empirical Study only
	public static class __RANDOM_SET__ {
		
		public static Value evalImpl(Value[] args) {
			int targetSize = Integer.valueOf(args[0].toString());
			ArrayValueImpl plugins = (ArrayValueImpl) ((Var) args[1]).getRawValue();
			
			int entriesToRemove = plugins.size() - targetSize;
			for (int i = 0; i < entriesToRemove; i++) {
				Value[] keys = plugins.keysToArray();

				int idx = new Random().nextInt(keys.length);
				plugins.remove(keys[idx]);
			}
			
			return plugins;
		}
		
	}
	
	/**
	 * @see com.caucho.quercus.lib.VariableModule.is_null(Value)
	 */
	public static class is_null {
		
		public static Value eval(Value arg) {
			if (arg instanceof Var)
				arg = ((Var) arg).getRawValue();
			
			if (arg instanceof MultiValue) {
				Constraint undefinedCases = MultiValue.whenUndefined(arg);
				return MultiValue.createChoiceValue(undefinedCases, BooleanValue.TRUE, BooleanValue.FALSE);
			}
			
			else if (arg instanceof ArrayValueImpl)
				return ArrayValueImpl_.isNull((ArrayValueImpl) arg);
			
			else
				return arg.isNull() ? BooleanValue.TRUE : BooleanValue.FALSE;
		}
		
	}
	
	/**
	 * @see com.caucho.quercus.lib.ArrayModule.in_array(Value, ArrayValue, boolean)
	 */
	public static class in_array {
		
		public static Value eval(Value[] args) {
			Value arg0 = args[0] instanceof Var ? ((Var) args[0]).getRawValue() : args[0];
			Value arg1 = args[1] instanceof Var ? ((Var) args[1]).getRawValue() : args[1];
			Value arg2 = args.length > 2 ? (args[2] instanceof Var ? ((Var) args[2]).getRawValue() : args[2]) : null;
			
			final Value needle = arg0;
			final ArrayValue stack = arg1 instanceof ArrayValue ? (ArrayValue) arg1 : null;
			final boolean strict = arg2 != null ? (arg2 == BooleanValue.TRUE) : false; 
			
			if (stack == null)
				return BooleanValue.FALSE;
			
			Constraint existCond = Constraint.FALSE;
		    for (Entry entry = stack.getHead(); entry != null; entry = entry.getNext()) {
		    	
	    		Value result = MultiValue.operateOnValue(entry.getValue(), new IOperation() {
					@Override
					public Value operate(Value flattenedValue) {
						if (strict)
							return flattenedValue.eql(needle) ? BooleanValue.TRUE : BooleanValue.FALSE;
						else
							return flattenedValue.eq(needle) ? BooleanValue.TRUE : BooleanValue.FALSE;
					}
	    		});
	    		
	    		existCond = Constraint.createOrConstraint(existCond, MultiValue.whenTrue(result));
		    }

		    return MultiValue.createChoiceValue(existCond, BooleanValue.TRUE, BooleanValue.FALSE);
		}
		
	}	
	
	/*
	 * TODO PENDING CHANGES
	 */
	
	/*
	public static class is_callable {
		
		public static Value eval(Value arg, final Env env) {
			if (arg instanceof Var)
				arg = ((Var) arg).getRawValue();
			
			Value retValue = MultiValue.operateOnValue(arg, new IOperation()  {
					@Override
					public Value operate(Value value) {
						if (value instanceof ArrayValueImpl)
							return ArrayValueImpl_.isCallable((ArrayValueImpl) value, env);
						
						return value.isCallable(env) ? BooleanValue.TRUE : BooleanValue.FALSE;
					}
			});

			// TODO Revise
			Constraint undefinedCases = MultiValue.whenUndefined(retValue);
			retValue = MultiValue.createChoiceValue(undefinedCases, BooleanValue.FALSE, retValue);
			
			return retValue;
		}
		
	}
	*/

}
