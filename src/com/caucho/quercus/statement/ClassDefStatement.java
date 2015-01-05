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

package com.caucho.quercus.statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import son.hcmus.edu.ClassConstantStatement;
import son.hcmus.edu.ClassExtendStatement;
import son.hcmus.edu.ClassFieldStatement;
import son.hcmus.edu.ClassFuncStatement;
import son.hcmus.edu.GapStatement;
import son.hcmus.edu.ModelBuilder;
import son.hcmus.edu.StatementAlignment;

import com.caucho.quercus.Location;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.StringValue;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.expr.Expr;
import com.caucho.quercus.function.AbstractFunction;
import com.caucho.quercus.program.ClassDef.FieldEntry;
import com.caucho.quercus.program.ClassDef.StaticFieldEntry;
import com.caucho.quercus.program.Function;
import com.caucho.quercus.program.InterpretedClassDef;
import com.caucho.util.L10N;

import edu.iastate.hungnv.empiricalstudy.EmpiricalStudy;
import edu.iastate.hungnv.shadow.Env_;

/**
 * Represents a class definition
 */
public class ClassDefStatement extends Statement {
  private final static L10N L = new L10N(ClassDefStatement.class);
  
  protected /*final*/ InterpretedClassDef _cl;
  
  public InterpretedClassDef getClassDef(){
      return this._cl;
  }
  
  public ClassDefStatement(Location location, InterpretedClassDef cl)
  {
    super(location);

    _cl = cl;
  }

  @Override
  public Value execute(Env env)
  {
	  // EMPI ADDED BY HUNG
	  EmpiricalStudy.inst.statementExecuted(this, env);
	  // END OF ADDED CODE
    
	  // CODE MODIFIED BY SON
    //env.addClass(_cl.getName(), _cl);
    env.addClass(_cl.getName(), _cl, env.getEnv_().getScope().getConstraint());
    // END OF MODIFIED CODE

    return null;
  }

  @Override
  public String toString()
  {
	  StringBuilder retValue = new StringBuilder();
	  
	  //retValue.append("class ").append(_cl.getName().substring(_cl.getName().lastIndexOf("\\") + 1));
	  retValue.append("class ").append(_cl.getName());
	  if (_cl.getParentName() != null)
		  retValue.append(" extends ").append(_cl.getParentName());
	  
	  if (_cl.getInterfaces().length > 0)
		  retValue.append(" implements ");
	  for(String iface : _cl.getInterfaces()){
		  retValue.append(iface + " ");
	  }
	  
	  retValue.append("{\r\n");
	  
	  for(Entry<StringValue, FieldEntry> entry : _cl.fieldSet()){
		  retValue.append(entry.getValue().getVisibility().toString().toLowerCase() 
				  + " $" + entry.getKey() + " = " + entry.getValue().toString() + ";\r\n");
	  }
	  
	  for(Entry<String, StaticFieldEntry> entry : _cl.staticFieldSet()){
		  retValue.append("private static $" + entry.getKey() + " = " + entry.getValue().toString() + ";\r\n");
	  }
	  
	  for(Entry<String, AbstractFunction> entry : _cl.functionSet()){
		  if (entry.getValue() instanceof Function){
			  String comment = entry.getValue().getComment();
			  if (comment != null) retValue.append(comment);
			  retValue.append(entry.getValue().toString());
			  Function func = (Function)entry.getValue();
			  retValue.append(func._statement.createBlock().toString());
		  }
	  }
	  
	  retValue.append("}");
	  
	  return retValue.toString();
  }
  
  @Override
  public boolean equal(Statement stm){
      if (!(stm instanceof ClassDefStatement))
          return false;
      
      ClassDefStatement cdStm = (ClassDefStatement)stm;
      if(cdStm.getClassDef().getName().equals(this._cl.getName()))
          return true;
      
      return false;
  }
  
  @Override
  public ArrayList<Statement> alignStatement(ArrayList<Statement> statements){
	  // Do not align test class
	  ClassDefStatement classDefStm = (ClassDefStatement)statements.get(0);
	  if (classDefStm._cl.getName().equals(Env_.testClass)){
		  return statements;
	  }
	  if (classDefStm._cl.getParentName() != null){
		  if (classDefStm._cl.getParentName().equals(Env_.testClass)){
			  return statements;
		  }
	  }
	  
      // Align field, static field, constant, function
      HashMap<StringValue, FieldEntry> fieldMap = new LinkedHashMap<StringValue, FieldEntry>();
      HashMap<String, StaticFieldEntry> staticFieldMap = new LinkedHashMap<String, StaticFieldEntry>();
      HashMap<String, Expr> constMap = new HashMap<String, Expr>();
      HashMap<String, AbstractFunction> functionMap = new HashMap<String, AbstractFunction>();
      for(Statement stm : statements){
          ClassDefStatement cdStm = (ClassDefStatement)stm;
          fieldMap.putAll(cdStm._cl._fieldMap);
          staticFieldMap.putAll(cdStm._cl._staticFieldMap);
          constMap.putAll(cdStm._cl._constMap);
          functionMap.putAll(cdStm._cl._functionMap);
      }

      for(Statement stm : statements){
          ClassDefStatement cdStm = (ClassDefStatement)stm;
          StringValue[] keyFieldMap = fieldMap.keySet().toArray(new StringValue[fieldMap.size()]);
          for(StringValue key : keyFieldMap)
              if (!cdStm._cl._fieldMap.containsKey(key))
                  cdStm._cl._fieldMap.put(key, null);
          
          String[] keyStaticFieldMap = staticFieldMap.keySet().toArray(new String[staticFieldMap.size()]);
          for(String key : keyStaticFieldMap)
              if(!cdStm._cl._staticFieldMap.containsKey(key))
                  cdStm._cl._staticFieldMap.put(key, null);
          
          String[] keyConstMap = constMap.keySet().toArray(new String[constMap.size()]);
          for(String key : keyConstMap)
              if(!cdStm._cl._constMap.containsKey(key))
                  cdStm._cl._constMap.put(key, null);
          
          String[] keyFuncMap = functionMap.keySet().toArray(new String[functionMap.size()]);
          for(String key : keyFuncMap)
              if(!cdStm._cl._functionMap.containsKey(key))
                  cdStm._cl._functionMap.put(key, null);
      }
      
      // Align statements in each function  
      String[] keyFuncMap = functionMap.keySet().toArray(new String[functionMap.size()]);
      for(String key : keyFuncMap){
          ArrayList<ArrayList<Statement>> input = new ArrayList<ArrayList<Statement>>();
          for(Statement stm : statements){
              ArrayList<Statement> inputRow = new ArrayList<Statement>();
              ClassDefStatement cdStm = (ClassDefStatement)stm;
              AbstractFunction func = cdStm._cl._functionMap.get(key);
              if (func == null){
                  inputRow.add(new GapStatement().createBlock());
                  input.add(inputRow);
              }
              else if (func instanceof Function){
                  Statement funcStm = ((Function)func)._statement.createBlock();
                  inputRow.add(funcStm);
                  input.add(inputRow);
              }
              
          }
          input = StatementAlignment.alignImpl(input);
          for(int i=0; i<statements.size(); i++){
              ClassDefStatement cdStm = (ClassDefStatement)statements.get(i);
              AbstractFunction func = cdStm._cl._functionMap.get(key);
              if (func == null) continue;
              if (func instanceof Function){
                  ((Function)func)._statement = new BlockStatement(Location.UNKNOWN, input.get(i));
              }
          }
      }
      
      return statements;
  }
  
  @Override
  public String buildModel(ArrayList<Statement> statements, List<String> branches){
	  
	  // Do not build model from test class
	  ClassDefStatement classDefStm = (ClassDefStatement)statements.get(0);
	  if (classDefStm._cl.getName().equals(Env_.testClass)){
		  return classDefStm.toString();
	  }
	  if (classDefStm._cl.getParentName() != null){
		  if (classDefStm._cl.getParentName().equals(Env_.testClass)){
			  return classDefStm.toString();
		  }
	  }
	  
      StringBuilder retValue = new StringBuilder();
      //String className = _cl.getName().substring(_cl.getName().lastIndexOf("\\") + 1);
      String className = _cl.getName();
      retValue.append("{\r\n$__ClassName__ = '").append(className.replace("\\", "\\\\")).append("';\r\n");
      retValue.append("$__Extends__ = '';\r\n");
      retValue.append("$__Fields__='';\r\n");
      retValue.append("$__Functions__='';\r\n");
      
      // interface
      Set<String> implSet = new HashSet<String>();
      int nStm = statements.size();
      for(int i=0; i<nStm; i++){
    	  ClassDefStatement cdStm = (ClassDefStatement)statements.get(i);
    	  int nInterface = cdStm._cl.getInterfaces().length;
    	  for (int j=0; j<nInterface; j++){
    		  implSet.add(cdStm._cl.getInterfaces()[j]);
    	  }
      }
      
      String implStr = "";
      if (!implSet.isEmpty()){
    	  implStr = "implements ";
    	  for (String impl : implSet)
    		  implStr += impl + ", ";
    	  implStr = implStr.substring(0, implStr.length() - 2);
      }
      retValue.append("$__Implements__= '" + implStr.replace("\\", "\\\\") + "';\r\n");
      
      
      HashMap<StringValue, FieldEntry> fieldMap = _cl._fieldMap;
      HashMap<String, StaticFieldEntry> staticFieldMap = _cl._staticFieldMap;
      HashMap<String, Expr> constMap = _cl._constMap;
      HashMap<String, AbstractFunction> functionMap = _cl._functionMap;

      ArrayList<ArrayList<Statement>> extendStms = new ArrayList<ArrayList<Statement>>();
      for (int i = 0; i < statements.size(); i++) {
          ArrayList<Statement> temp = new ArrayList<Statement>();
          ClassDefStatement cdStm = (ClassDefStatement) statements.get(i);
          if (cdStm._cl.getParentName() == null || cdStm._cl.getParentName().isEmpty()) {
              temp.add(new ClassExtendStatement(""));
          }
          else
              temp.add(new ClassExtendStatement(cdStm._cl.getParentName()));
          extendStms.add(temp);
      }
      retValue.append(ModelBuilder.BuildModel(extendStms, branches));

      StringValue[] keyFieldMap = fieldMap.keySet().toArray(new StringValue[fieldMap.size()]);
      for(StringValue key : keyFieldMap){
          ArrayList<ArrayList<Statement>> fieldStms = new ArrayList<ArrayList<Statement>>();
          for(int i=0; i<statements.size(); i++){
              ClassDefStatement cdStm = (ClassDefStatement)statements.get(i);
              FieldEntry fieldEntry = cdStm._cl._fieldMap.get(key);
              Statement fieldStm;
              if (fieldEntry == null) fieldStm = new GapStatement();
              else fieldStm = new ClassFieldStatement(key.toString(), fieldEntry.getValue(), 
                      fieldEntry.getVisibility().toString(), "field");
              ArrayList<Statement> temp = new ArrayList<Statement>();
              temp.add(fieldStm);
              fieldStms.add(temp);
          }
          
          retValue.append(ModelBuilder.BuildModel(fieldStms, branches));
      }
      
      String[] keyStaticFieldMap = staticFieldMap.keySet().toArray(new String[staticFieldMap.size()]);
      for(String key : keyStaticFieldMap){
          ArrayList<ArrayList<Statement>> staticFieldStms = new ArrayList<ArrayList<Statement>>();
          for(int i=0; i<statements.size(); i++){
              ClassDefStatement cdStm = (ClassDefStatement)statements.get(i);
              StaticFieldEntry staticFieldEntry = cdStm._cl._staticFieldMap.get(key);
              Statement staticFieldStm;
              if (staticFieldEntry == null){
                  staticFieldStm = new GapStatement();
              }
              else{
                  staticFieldStm = new ClassFieldStatement(key, staticFieldEntry.getValue(),
                      "private", "static");
              }
              ArrayList<Statement> temp = new ArrayList<Statement>();
              temp.add(staticFieldStm);
              staticFieldStms.add(temp);
          }
          retValue.append(ModelBuilder.BuildModel(staticFieldStms, branches));
      } 
      
      String[] keyConstMap = constMap.keySet().toArray(new String[constMap.size()]);
      for (String key : keyConstMap){
    	  ArrayList<ArrayList<Statement>> constFieldStms = new ArrayList<ArrayList<Statement>>();
    	  for(int i=0; i<statements.size(); i++){
    		  ClassDefStatement cdStm = (ClassDefStatement)statements.get(i);
    		  Expr constExpr = cdStm._cl._constMap.get(key);
    		  Statement constFieldStm;
    		  if (constExpr == null)
    			  constFieldStm = new GapStatement();
    		  else
    			  constFieldStm = new ClassConstantStatement(key, constExpr);
    		  ArrayList<Statement> temp = new ArrayList<Statement>();
              temp.add(constFieldStm);
              constFieldStms.add(temp);
    	  }
    	  retValue.append(ModelBuilder.BuildModel(constFieldStms, branches));
      }
      
      String[] keyFuncMap = functionMap.keySet().toArray(new String[functionMap.size()]);
      for(String key : keyFuncMap){
          ArrayList<String> funcBranches = new ArrayList<String>();
          ArrayList<ArrayList<Statement>> funcStms = new ArrayList<ArrayList<Statement>>();
          for(int i=0; i<statements.size(); i++){
              ClassDefStatement cdStm = (ClassDefStatement)statements.get(i);
              Function func = (Function)cdStm._cl._functionMap.get(key);
              Statement funcStm;
              if (func != null){
                  funcStm = new ClassFuncStatement(func);
                  funcBranches.add(branches.get(i));
              }
              else continue;
              ArrayList<Statement> temp = new ArrayList<Statement>();
              temp.add(funcStm);
              funcStms.add(temp);
          }
          
          retValue.append(ModelBuilder.BuildModel(funcStms, funcBranches));
      }
      
      if (this._cl.isInterface())
    	  retValue.append("eval(\" interface $__ClassName__ $__Extends__ { $__Fields__ $__Functions__ } \"); }\r\n");
      else{
    	  if (this._cl.isAbstract())	  
    		  retValue.append("eval(\" abstract class $__ClassName__ $__Extends__ $__Implements__ { $__Fields__ $__Functions__ } \"); }\r\n");
    	  else 
    		  retValue.append("eval(\" class $__ClassName__ $__Extends__ $__Implements__ { $__Fields__ $__Functions__ } \"); }\r\n");
      }
      return retValue.toString();
  }
  
  @Override
	public ArrayList<Statement> findStatements(Class<?> type, boolean recursive) {
		ArrayList<Statement> ret = new ArrayList<Statement>();
		if (this.getClass().equals(type))
			ret.add(this);
		
		for(AbstractFunction abFunc : _cl._functionMap.values()){
			if (!(abFunc instanceof Function))
				continue;
			
			Function func = (Function)abFunc;
			ret.addAll(func._statement.findStatements(type, recursive));
		}
		return ret;
	}
}

