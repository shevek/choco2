/* * * * * * * * * * * * * * * * * * * * * * * * * 
 *          _       _                            *
 *         |  °(..)  |                           *
 *         |_  J||L _|        CHOCO solver       *
 *                                               *
 *    Choco is a java library for constraint     *
 *    satisfaction problems (CSP), constraint    *
 *    programming (CP) and explanation-based     *
 *    constraint solving (e-CP). It is built     *
 *    on a event-based propagation mechanism     *
 *    with backtrackable structures.             *
 *                                               *
 *    Choco is an open-source software,          *
 *    distributed under a BSD licence            *
 *    and hosted by sourceforge.net              *
 *                                               *
 *    + website : http://choco.emn.fr            *
 *    + support : choco@emn.fr                   *
 *                                               *
 *    Copyright (C) F. Laburthe,                 *
 *                  N. Jussien    1999-2008      *
 * * * * * * * * * * * * * * * * * * * * * * * * */
package choco.cp.model.managers.constraints.integer;

import choco.Choco;
import choco.cp.model.managers.IntConstraintManager;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.SignOp;
import choco.cp.solver.constraints.reified.leaves.bool.OppSignNode;
import choco.cp.solver.constraints.reified.leaves.bool.SameSignNode;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.reified.INode;

import java.util.HashSet;

/*
 *  ______
 * (__  __)
 *    ||
 *   /__\                  Choco manager
 *    \                    =============
 *    \                      Aug. 2008
 *    \                  SignOp constraint
 *    \
 *    |
 */
/**
 * A manager to build new signop constraint
 */
public class SignOpManager extends IntConstraintManager {
  public SConstraint makeConstraint(Solver solver, Variable[] vars, Object parameters, HashSet<String> options) {
    if (solver instanceof CPSolver) {
        Boolean same = (Boolean) parameters;
        return new SignOp(
          solver.getVar((IntegerVariable) vars[0]),
          solver.getVar((IntegerVariable) vars[1]),
          (parameters == Boolean.TRUE) ? true : false);
    }
    if (Choco.DEBUG) {
      LOGGER.severe("Could not found an implementation of samesign or oppositesign (signop constraint) !");
    }
    return null;
  }

    /**
     * Build arithm node from a IntegerExpressionVariable
     *
     * @param solver
     * @param cstrs  constraints (can be null)
     * @param vars   variables
     * @return
     */
    @Override
    public INode makeNode(Solver solver, Constraint[] cstrs, IntegerExpressionVariable[] vars) {
        if(solver instanceof CPSolver){
            CPSolver s = (CPSolver)solver;
            if(vars.length == 2){
                INode[] nodes = new INode[vars.length];
                for(int i = 0; i < vars.length; i++){
                    nodes[i] = vars[i].getEm().makeNode(s, vars[i].getConstraints(), vars[i].getVariables());
                }
                if (((ComponentConstraint) cstrs[0]).getParameters() == Boolean.FALSE)
                    return new OppSignNode(nodes);
                else if (((ComponentConstraint) cstrs[0]).getParameters() == Boolean.TRUE)
                    return new SameSignNode(nodes);
            }
        }
        if(Choco.DEBUG){
            throw new RuntimeException("Could not found an implementation for SignOppManager !");
        }
        return null;
    }
}
