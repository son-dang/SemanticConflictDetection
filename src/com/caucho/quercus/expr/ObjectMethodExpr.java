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

import com.caucho.quercus.Location;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.MethodIntern;
import com.caucho.quercus.env.NullValue;
import com.caucho.quercus.env.ObjectExtValue;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.env.StringValue;
import com.caucho.util.L10N;
import edu.iastate.hungnv.constraint.Constraint;

import edu.iastate.hungnv.debug.TraceViewer;
import edu.iastate.hungnv.shadow.Env_;
import edu.iastate.hungnv.value.Case;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.Switch;

import java.util.ArrayList;

/**
 * Represents a PHP function expression.
 */
public class ObjectMethodExpr extends AbstractMethodExpr {

    private static final L10N L = new L10N(ObjectMethodExpr.class);
    protected final Expr _objExpr;
    protected final StringValue _methodName;
    protected final Expr[] _args;

    public ObjectMethodExpr(Location location,
            Expr objExpr,
            String name,
            ArrayList<Expr> args) {
        super(location);

        _objExpr = objExpr;

        _methodName = MethodIntern.intern(name);

        _args = new Expr[args.size()];
        args.toArray(_args);
    }

    public ObjectMethodExpr(Expr objExpr, String name, ArrayList<Expr> args) {
        this(Location.UNKNOWN, objExpr, name, args);
    }

    public String getName() {
        return _methodName.toString();
    }

    /**
     * Evaluates the expression.
     *
     * @param env the calling environment.
     *
     * @return the expression value.
     */
    @Override
    public Value eval(Env env) {
        env.checkTimeout();

        Value obj = _objExpr.eval(env);

        StringValue methodName = _methodName;
        
        int hash = methodName.hashCodeCaseInsensitive();

        // INST ADDED BY HUNG
        // NOTE: This code is not guarded by if (Env_.INSTRUMENT)

        try {
            TraceViewer.inst.enterFunction(methodName.toString(), getLocation(), env.getEnv_().getScope().getConstraint());
            // END OF ADDED CODE

            // CODE ADDED BY SON
            // if object is an instance of class that is modified by branches
            // alter the default calling method
            if (obj instanceof MultiValue) {
                Switch sw = MultiValue.flatten(obj);
                Switch retValue = new Switch();

                for (Case _case : sw.getCases()) {
                    Constraint aggregateConstraint = env.getEnv_().getScope().getConstraint();
                    Constraint constraint = _case.getConstraint();
                    try {
                        Value caseValue = _case.getValue();
                        Value value = eval(env, caseValue, methodName, hash, _args);
                        if (value instanceof MultiValue) {
                            Switch _switchValue = MultiValue.flatten(value);
                            for (Case _valueCase : _switchValue.getCases()) {
                                Constraint andConstraint = Constraint.createAndConstraint(_valueCase.getConstraint(), constraint);
                                if (andConstraint.isSatisfiable()) {
                                    retValue.addCase(new Case(andConstraint, _valueCase.getValue()));
                                }
                            }
                        } else {
                            retValue.addCase(new Case(constraint, value));
                        }
                    } catch (Exception ex) {
                        Constraint afterEvalConstraint = env.getEnv_().getScope().getConstraint();
                        while (!aggregateConstraint.toString().equals(afterEvalConstraint.toString())) {
                            env.getEnv_().exitScope();
                            afterEvalConstraint = env.getEnv_().getScope().getConstraint();
                            if (afterEvalConstraint.equals(Constraint.TRUE)) {
                                break;
                            }
                        }
                    }
                }

                return retValue;
            }
            // END OF ADDED CODE
            
            Value retValue = eval(env, obj, methodName, hash, _args);
            return retValue;

            // INST ADDED BY HUNG
            // NOTE: This code is not guarded by if (Env_.INSTRUMENT)

        } finally {
            TraceViewer.inst.exitFunction(methodName.toString(), getLocation());
        }
        // END OF ADDED CODE
    }

    @Override
    public String toString() {
        StringBuilder retValue = new StringBuilder();
        retValue.append(_objExpr.toString()).append("->").append(_methodName.toString()).append("(");
        for (int i = 0; i < _args.length; i++) {
            retValue.append(_args[i].toString());
            if (i < _args.length - 1) {
                retValue.append(", ");
            }
        }
        retValue.append(")");
        return retValue.toString();
    }
}
