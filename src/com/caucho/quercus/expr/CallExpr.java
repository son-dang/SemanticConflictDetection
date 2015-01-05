/*
 * Copyright (c) 1998-2012 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.quercus.expr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.caucho.quercus.Location;
import com.caucho.quercus.env.ArrayValue;
import com.caucho.quercus.env.ArrayValueImpl;
import com.caucho.quercus.env.BooleanValue;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.LongValue;
import com.caucho.quercus.env.NullValue;
import com.caucho.quercus.env.ObjectValue;
import com.caucho.quercus.env.QuercusClass;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.env.Var;
import com.caucho.quercus.function.AbstractFunction;
import com.caucho.util.L10N;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.debug.TraceViewer;
import edu.iastate.hungnv.shadow.Env_;
import edu.iastate.hungnv.shadow.Functions;
import edu.iastate.hungnv.value.Case;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.Switch;
import edu.iastate.hungnv.value.Undefined;

/**
 * A "foo(...)" function call.
 */
public class CallExpr extends Expr {
  private static final L10N L = new L10N(CallExpr.class);
  
  protected final String _name;
  protected final String _nsName;
  protected final Expr []_args;
  
  public Expr[] getArgs(){
      return _args;
  }
  
  private int _funId;
  
  protected boolean _isRef;

  public CallExpr(Location location, String name, ArrayList<Expr> args)
  {
    // quercus/120o
    super(location);
    _name = name.intern();
    
    int ns = _name.lastIndexOf('\\');
    
    if (ns > 0) {
      _nsName = _name.substring(ns + 1);
    }
    else
      _nsName = null;

    _args = new Expr[args.size()];
    args.toArray(_args);
  }

  public CallExpr(Location location, String name, Expr []args)
  {
    // quercus/120o
    super(location);
    _name = name.intern();
    
    int ns = _name.lastIndexOf('\\');
    
    if (ns > 0) {
      _nsName = _name.substring(ns + 1);
    }
    else
      _nsName = null;

    _args = args;
  }

  public CallExpr(String name, ArrayList<Expr> args)
  {
    this(Location.UNKNOWN, name, args);
  }

  public CallExpr(String name, Expr []args)
  {
    this(Location.UNKNOWN, name, args);
  }

  /**
   * Returns the name.
   */
  public String getName()
  {
    return _name;
  }
  
  /**
   * Returns the location if known.
   */
  public String getFunctionLocation()
  {
    return " [" + _name + "]";
  }

  /**
   * Returns the reference of the value.
   * @param location
   */
  /*
  @Override
  public Expr createRef(QuercusParser parser)
  {
    return parser.getExprFactory().createCallRef(this);
  }
  */

  /**
   * Returns the copy of the value.
   * @param location
   */
  @Override
  public Expr createCopy(ExprFactory factory)
  {
    return this;
  }
  
  /**
   * Evaluates the expression.
   *
   * @param env the calling environment.
   *
   * @return the expression value.
   */
  @Override
  public Value eval(Env env)
  {
    return evalImpl(env, false, false);
  }
  
  /**
   * Evaluates the expression.
   *
   * @param env the calling environment.
   *
   * @return the expression value.
   */
  @Override
  public Value evalCopy(Env env)
  {
	  // INST ADDED BY HUNG
	  if (Env_.INSTRUMENT) {
		  // ADHOC Never let _wp_filter_build_unique_id in wp-includes/plugin.php return a MultiValue
		  if (_name.equals("_wp_filter_build_unique_id")) {
			  Value retValue = evalImpl(env, false, true);
			  
			  if (retValue instanceof MultiValue) {
				  Value max = null;
				  for (Case case_ : ((MultiValue) retValue).flatten()) {
					  if (max == null || case_.getValue().gt(max))
						  max = case_.getValue();
				  }
				  return max;
			  }
			  else 
				  return retValue;
		  }
	  }
	  // END OF ADDED CODE
	  
    return evalImpl(env, false, true);
  }
  
  /**
   * Evaluates the expression.
   *
   * @param env the calling environment.
   *
   * @return the expression value.
   */
  @Override
  public Value evalRef(Env env)
  {
    return evalImpl(env, true, true);
  }
  
  
  /**
   * Evaluates the expression.
   *
   * @param env the calling environment.
   *
   * @return the expression value.
   */
  private Value evalImpl(Env env, boolean isRef, boolean isCopy)
  {
	  // INST ADDED BY HUNG
	  // NOTE: This code is not guarded by if (Env_.INSTRUMENT)
	  try {
		  TraceViewer.inst.enterFunction(_name, getLocation(), env.getEnv_().getScope().getConstraint());
	  // END OF ADDED CODE
	  
    if (_funId <= 0) {
      _funId = env.findFunctionId(_name);
    
      if (_funId <= 0) {
        if (_nsName != null)
          _funId = env.findFunctionId(_nsName);
      
        if (_funId <= 0) {
        	
        	// INST ADDED BY HUNG
        	// NOTE: This code is not guarded by if (Env_.INSTRUMENT)
        		
    		if (_name.equals(Functions.__CHOICE__.class.getSimpleName()))
    			return Functions.__CHOICE__.evalImpl(evalArgs(env, _args));
      
    		else if (_name.equals(Functions.__ASSERT__.class.getSimpleName()))
    			return Functions.__ASSERT__.evalImpl(env, _args, this.getLocation());
    		
    		else if (_name.equals(Functions.__DEBUG__.class.getSimpleName()))
    			return Functions.__DEBUG__.evalImpl(evalArgs(env, _args), this.getLocation());
    		
    		else if (_name.equals(Functions.__PLUGINS__.class.getSimpleName()))
    			return Functions.__PLUGINS__.evalImpl(evalArgs(env, _args));
                
    			// CODE ADDED BY SON
    		
                else if (_name.equals(Functions.__VERSION__.class.getSimpleName()))
                        return Functions.__VERSION__.evalImpl(evalArgs(env, _args));
                
                else if (_name.equals(Functions.__TEST__.class.getSimpleName()))
                        return Functions.__TEST__.evalImpl(evalArgs(env, _args));
                
                else if (_name.equals(Functions.__RUN__.class.getSimpleName()))
                        return Functions.__RUN__.evalImpl(env, evalArgs(env, _args));
    		
                else if (_name.equals(Functions.__ExpectOutputString__.class.getSimpleName()))
                    return Functions.__ExpectOutputString__.evalImpl(env, evalArgs(env, _args), this.getLocation());
    		
                else if (_name.equals(Functions.__ExpectOutputRegex__.class.getSimpleName()))
                    return Functions.__ExpectOutputRegex__.evalImpl(env, evalArgs(env, _args), this.getLocation());
                  
                else if (_name.equals(Functions.__CHECK__.class.getSimpleName()))
                	return Functions.__CHECK__.evalImpl(env, evalArgs(env, _args));
    			// END OF ADDED CODE
    		
    		// EMPI For Empirical Study only
    		else if (_name.equals(Functions.__RANDOM_SET__.class.getSimpleName()))
    			return Functions.__RANDOM_SET__.evalImpl(evalArgs(env, _args));
        	
        	// END OF ADDED CODE
    		
    		// INST ADDED BY HUNG
    		if (Env_.INSTRUMENT) {
    			// ADHOC Handle a conflict between Contact Form 7 and Reall Simple CAPTCHA
    			// @see wp-content/plugins/really-simple-captcha.php:Line236
    			// @see http://wordpress.org/support/topic/call-to-undefined-function-win_is_writable-on-line-236
    			if (_name.endsWith("win_is_writable"))
    				return NullValue.NULL;
    		}
    		// END OF ADDED CODE
        	
          env.error(getLocationLine(),
                    L.l("'{0}' is an unknown function.", _name));

          return NullValue.NULL;
        }
      }
    }
    
    AbstractFunction fun = env.findFunction(_funId);
    
    if (fun == null) {
      env.error(getLocationLine(), L.l("'{0}' is an unknown function.", _name));

      return NullValue.NULL;
    }

    Value []args = evalArgs(env, _args);

	  // INST ADDED BY HUNG
	  // NOTE: This code is not guarded by if (Env_.INSTRUMENT)
	  if (_name.equals("add_action") || _name.equals("do_action")) {
		  TraceViewer.inst.modifyLastEnteredFunctionName(_name, _name + " (" + args[0] + ")");
	  }
	  else if (_name.equals("call_user_func_array")) {
		  Value firstArg = args[0];
		  if (firstArg instanceof Var)
			  firstArg = ((Var) firstArg).getRawValue();
		  
		  String desc;
		  if (firstArg instanceof ArrayValue) {
			  if (((ArrayValue) firstArg).get(LongValue.ZERO) instanceof ObjectValue)
				  desc = ((ObjectValue) ((ArrayValue) firstArg).get(LongValue.ZERO)).getClassName() + "." + ((ArrayValue) firstArg).get(LongValue.ONE);
			  else
				  desc = ((ArrayValue) firstArg).get(LongValue.ZERO) + "." + ((ArrayValue) firstArg).get(LongValue.ONE);
		  }
		  else
			  desc = firstArg.toString();
		  
		  TraceViewer.inst.modifyLastEnteredFunctionName(_name, _name + " (" + desc + ")");
	  }
	  // END OF ADDED CODE
	  
	// INST ADDED BY HUNG
	if (Env_.INSTRUMENT) {
		// ADHOC Handle a conflict between Contact Form 7 and Reall Simple CAPTCHA. Rewrite function is_callable for specifically one case.
		// @see wp-content/plugins/contact-form-7/modules/captcha.php:Line417
		// @see http://wordpress.org/support/topic/call-to-undefined-function-win_is_writable-on-line-236
		if (_name.equals("is_callable")
				&& getLocation().getFileName().endsWith("wp-content\\plugins\\contact-form-7\\modules\\captcha.php")
				&& getLocation().getLineNumber() == 417)
			return MultiValue.createChoiceValue(Constraint.createConstraint("REA"), BooleanValue.TRUE, BooleanValue.FALSE);
                
                // CODE ADDED BY SON
				// modify php function count()
                if (_name.equals("count") && args.length == 1){
                    Value arrayAgr = args[0].getValue();
                    if (arrayAgr instanceof ArrayValue){
                        ArrayValue array = (ArrayValue)arrayAgr;
                        
                        List<Value> keysSrc = Arrays.asList(array.keysToArray());
                        List<Value> valuesSrc = Arrays.asList(array.valuesToArray());
                        
                        List<Value> keysDest = new ArrayList<Value>();
                        List<Value> valuesDest = new ArrayList<Value>();
                        for(int i=0; i<valuesSrc.size(); i++){
                            Value temp = MultiValue.simplify(valuesSrc.get(i), env.getEnv_().getScope().getConstraint());
                            if (temp == null || temp instanceof Undefined){
                            } else {
                                keysDest.add(keysSrc.get(i));
                                valuesDest.add(temp);
                            }
                        }
                            args[0] = new ArrayValueImpl(keysDest.toArray(new Value[keysDest.size()])
                                    , valuesDest.toArray(new Value[valuesDest.size()]));
                    }
                }
                // modify php function preg_match()
                else if (_name.equals("preg_match")){
                	if (args[1].isMultiValue()){
                		Switch argSwitch = MultiValue.flatten(args[1]);
                		Switch retSwitch = new Switch();
                		for(Case _case : argSwitch.getCases()){
                			Value[] tempArgs = new Value[args.length];
                			System.arraycopy(args, 0, tempArgs, 0, args.length);
                			Value caseValue = fun.call(env, tempArgs).toValue();
                			retSwitch.addCase(new Case(_case.getConstraint(), caseValue));
                		}
                		return MultiValue.createSwitchValue(retSwitch);
                	}
                }
                // END OF ADDED CODE BY SON
	}
	// END OF ADDED CODE

    env.pushCall(this, NullValue.NULL, args);
    
    // php/0249
    QuercusClass oldCallingClass = env.setCallingClass(null);
    
    // XXX: qa/1d14 Value oldThis = env.setThis(UnsetValue.NULL);
    try {
      env.checkTimeout();

      /*
      if (isRef)
        return fun.callRef(env, args);
      else if (isCopy)
        return fun.callCopy(env, args);
      else
        return fun.call(env, args);
        */
      
      if (isRef)
        return fun.callRef(env, args);
      else if (isCopy)
        return fun.call(env, args).copyReturn();
      else {
        return fun.call(env, args).toValue();
      }
    //} catch (Exception e) {
    //  throw QuercusException.create(e, env.getStackTrace());
    } finally {
      env.popCall();
      env.setCallingClass(oldCallingClass);
      // XXX: qa/1d14 env.setThis(oldThis);
    }
    
	  // INST ADDED BY HUNG
      // NOTE: This code is not guarded by if (Env_.INSTRUMENT)
	  
	  } finally {
		  TraceViewer.inst.exitFunction(_name, getLocation());
	  }
	  // END OF ADDED CODE
  }

  // Return an array containing the Values to be
  // passed in to this function.

  public Value []evalArguments(Env env)
  {
    AbstractFunction fun = env.findFunction(_name);

    if (fun == null) {
      return null;
    }

    return fun.evalArguments(env, this, _args);
  }

  @Override
  public String toString()
  {
    String funcName = _name.substring(_name.lastIndexOf("\\") + 1);
    String retValue = funcName + "(";
    for(int i=0; i<_args.length; i++){
        retValue += _args[i].toString();
        if (i < _args.length-1)
            retValue += ",";
    }
    retValue += ")";
  //  return _name + "()";
    return retValue;
  }
}

