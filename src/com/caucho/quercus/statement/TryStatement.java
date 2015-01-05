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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import son.hcmus.edu.GapStatement;
import son.hcmus.edu.ModelBuilder;
import son.hcmus.edu.StatementAlignment;

import com.caucho.quercus.Location;
import com.caucho.quercus.QuercusDieException;
import com.caucho.quercus.QuercusException;
import com.caucho.quercus.QuercusExitException;
import com.caucho.quercus.QuercusRuntimeException;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.NullValue;
import com.caucho.quercus.env.QuercusLanguageException;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.expr.AbstractVarExpr;

import edu.iastate.hungnv.empiricalstudy.EmpiricalStudy;
import edu.iastate.hungnv.shadow.Env_;
import edu.iastate.hungnv.shadow.TryStatement_;

/**
 * Represents sequence of statements.
 */
public class TryStatement extends Statement {
	protected Statement _block;
	protected final ArrayList<Catch> _catchList = new ArrayList<Catch>();

	public TryStatement(Location location, Statement block) {
		super(location);

		_block = block;

		block.setParent(this);
	}

	public void addCatch(String id, AbstractVarExpr lhs, Statement block) {
		_catchList.add(new Catch(id, lhs, block));

		block.setParent(this);
	}

	public Value execute(Env env) {
		// EMPI ADDED BY HUNG
		EmpiricalStudy.inst.statementExecuted(this, env);
		// END OF ADDED CODE

		// CODE ADDED BY SON
		if (Env_.INSTRUMENT) {
			return TryStatement_.execute(env, _block, _catchList);
		}
		// END OF ADDED CODE

		try {
			return _block.execute(env);
		} catch (QuercusLanguageException e) {
			Value value = null;

			try {
				value = e.toValue(env);
			} catch (Throwable e1) {
				throw new QuercusRuntimeException(e1);
			}

			for (int i = 0; i < _catchList.size(); i++) {
				Catch item = _catchList.get(i);

				if (value != null && value.isA(item.getId())
						|| item.getId().equals("Exception")) {
					if (value != null)
						item.getExpr().evalAssignValue(env, value);
					else
						item.getExpr().evalAssignValue(env, NullValue.NULL);

					return item.getBlock().execute(env);
				}
			}

			throw e;

		} catch (QuercusDieException e) {
			for (int i = 0; i < _catchList.size(); i++) {
				Catch item = _catchList.get(i);

				if (item.getId().equals("QuercusDieException")) {
					item.getExpr().evalAssignValue(env, env.createException(e));

					return item.getBlock().execute(env);
				}
			}

			throw e;

		} catch (QuercusExitException e) {
			for (int i = 0; i < _catchList.size(); i++) {
				Catch item = _catchList.get(i);

				if (item.getId().equals("QuercusExitException")) {
					item.getExpr().evalAssignValue(env, env.createException(e));

					return item.getBlock().execute(env);
				}
			}

			throw e;

		} catch (Exception e) {
			for (int i = 0; i < _catchList.size(); i++) {
				Catch item = _catchList.get(i);

				if (item.getId().equals("Exception")) {
					Throwable cause = e;

					// if (e instanceof QuercusException && e.getCause() !=
					// null)
					// cause = e.getCause();

					item.getExpr().evalAssignValue(env,
							env.createException(cause));

					return item.getBlock().execute(env);
				}
			}

			if (e instanceof QuercusException)
				throw (QuercusException) e;
			else
				throw new QuercusException(e);
		}
	}

	@Override
	public ArrayList<Statement> alignStatement(ArrayList<Statement> statements) {
		// align statements in try block
		ArrayList<ArrayList<Statement>> tryInput = new ArrayList<ArrayList<Statement>>();

		for (Statement stm : statements) {
			TryStatement tryStm = (TryStatement) stm;
			tryInput.add(new ArrayList<Statement>(Arrays.asList(tryStm._block
					.createBlock()._statements)));
		}

		ArrayList<ArrayList<Statement>> alignTryResult = StatementAlignment
				.alignImpl(tryInput);
		for (int i = 0; i < statements.size(); i++) {
			TryStatement tryStm = (TryStatement) statements.get(i);
			tryStm._block = new BlockStatement(Location.UNKNOWN,
					alignTryResult.get(i));
		}

		// align catch blocks
		HashMap<String, AbstractVarExpr> catchMap = new HashMap<String, AbstractVarExpr>();
		for (Statement stm : statements) {
			TryStatement tryStm = (TryStatement) stm;
			for (Catch c : tryStm._catchList) {
				String id = c._id.substring(c._id.lastIndexOf("\\") + 1) + " "
						+ c._lhs.toString();
				c._id = c._id.substring(c._id.lastIndexOf("\\") + 1);
				catchMap.put(id, c._lhs);
			}
		}

		for (Entry<String, AbstractVarExpr> entry : catchMap.entrySet()) {
			String cId = entry.getKey().substring(0, 
					entry.getKey().lastIndexOf(" "));
			ArrayList<ArrayList<Statement>> catchInput = new ArrayList<ArrayList<Statement>>();
			for (Statement stm : statements) {
				TryStatement tryStm = (TryStatement) stm;
				Statement catchBlock = null;
				for (Catch c : tryStm._catchList) {
					String id = c._id.substring(c._id.lastIndexOf("\\") + 1)
							+ " " + c._lhs.toString();
					if (id.equals(entry.getKey())) {
						catchBlock = c._block;
						break;
					}
				}

				if (catchBlock == null) {
					catchBlock = new GapStatement();
					tryStm._catchList.add(new Catch(cId, entry.getValue(),
							catchBlock));
				}

				catchInput.add(new ArrayList<Statement>(Arrays
						.asList(catchBlock.createBlock()._statements)));
			}
			
			ArrayList<ArrayList<Statement>> catchAlignResult = StatementAlignment.alignImpl(catchInput);
			for (int i=0; i<statements.size(); i++){
				TryStatement tryStm = (TryStatement)statements.get(i);
				for (Catch c : tryStm._catchList) {
					String id = c._id.substring(c._id.lastIndexOf("\\") + 1)
							+ " " + c._lhs.toString();
					if (id.equals(entry.getKey())) {
						c._block = new BlockStatement(Location.UNKNOWN, catchAlignResult.get(i));
					}
				}
			}

		}

		return statements;
	}
	
	@Override
	public String buildModel(ArrayList<Statement> statements,
			List<String> branches) {
		// build try block
		 StringBuilder retValue = new StringBuilder("try{\r\n");
		 ArrayList<ArrayList<Statement>> input = new ArrayList<ArrayList<Statement>>();
		 for (Statement stm : statements){
	          TryStatement tryStm = (TryStatement)stm;
	          input.add(new ArrayList<Statement>(Arrays.asList(tryStm._block.createBlock()._statements)));
	      }
		 retValue.append(ModelBuilder.BuildModel(input, branches));
		 retValue.append("}\r\n");
		 
		// build catch blocks
		HashSet<String> catchIds = new HashSet<String>();
		for(Catch c : _catchList)
			catchIds.add(c._id.substring(c._id.lastIndexOf("\\") + 1)
					+ " " + c._lhs.toString());
		
		for (String id : catchIds){
			retValue.append("catch(" + id + "){\r\n");
			
			ArrayList<ArrayList<Statement>> catchStmInput = new ArrayList<ArrayList<Statement>>();
			for (Statement stm : statements){
				TryStatement tryStm = (TryStatement)stm;
				for (Catch c : tryStm._catchList){
					String cId = c._id.substring(c._id.lastIndexOf("\\") + 1)
							+ " " + c._lhs.toString();
					
					if (cId.equals(id)){
						catchStmInput.add(new ArrayList<Statement>(Arrays.asList(c._block.createBlock()._statements)));
					}
				}
			}
			retValue.append(ModelBuilder.BuildModel(catchStmInput, branches));
			retValue.append("}\r\n");
		}
		
		return retValue.toString();
	}

	@Override
	public String toString() {
		StringBuilder retValue = new StringBuilder("try");
		retValue.append(_block.createBlock().toString());
		for(Catch c : _catchList){
			retValue.append("catch("
					+ c._id.substring(c._id.lastIndexOf("\\") + 1)
					+ " " + c._lhs.toString()
					+ ")");
			retValue.append(c._block.createBlock().toString());
		}
		return retValue.toString();
	}
	
	@Override
	public ArrayList<Statement> findStatements(Class<?> type, boolean recursive) {
		ArrayList<Statement> ret = new ArrayList<Statement>();
		if (this.getClass().equals(type))
			ret.add(this);
		if (recursive){
			ret.addAll(_block.findStatements(type, recursive));
			for(Catch _catch : _catchList)
				ret.addAll(_catch.getBlock().findStatements(type, recursive));
		}
		return ret;
	}
	
	public static class Catch {
		private String _id;
		private AbstractVarExpr _lhs;
		private Statement _block;

		Catch(String id, AbstractVarExpr lhs, Statement block) {
			_id = id;
			_lhs = lhs;
			_block = block;

			if (id == null)
				throw new NullPointerException();
		}

		public String getId() {
			return _id;
		}

		public AbstractVarExpr getExpr() {
			return _lhs;
		}

		public Statement getBlock() {
			return _block;
		}
	}
}
