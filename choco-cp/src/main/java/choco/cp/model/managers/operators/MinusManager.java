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
package choco.cp.model.managers.operators;

import choco.Choco;
import choco.cp.model.managers.RealConstraintManager;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.real.exp.RealMinus;
import choco.cp.solver.constraints.reified.leaves.arithm.MinusNode;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ExpressionManager;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.real.RealExpressionVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.real.RealExp;
import choco.kernel.solver.constraints.reified.INode;

import java.util.HashSet;

/* User:    charles
 * Date:    20 août 2008
 */
public class MinusManager extends RealConstraintManager implements ExpressionManager {

    /**
     * Build a constraint for the given solver and "model variables"
     *
     * @param solver
     * @param variables
     * @param parameters : a "hook" to attach any kind of parameters to constraints
     * @param options
     * @return
     */
    @Override
    public SConstraint makeConstraint(Solver solver, Variable[] variables, Object parameters, HashSet<String> options) {
        return null;
    }

    /**
     * Build arithm node from a IntegerExpressionVariable
     *
     * @param solver
     * @param vars
     * @return
     */
    public INode makeNode(Solver solver, Constraint[] cstrs, IntegerExpressionVariable[] vars) {
        if(solver instanceof CPSolver){
            CPSolver s = (CPSolver)solver;
            if(vars.length == 2){
                INode[] nodes = new INode[vars.length];
                for(int i = 0; i < vars.length; i++){
                    nodes[i] = vars[i].getEm().makeNode(s, vars[i].getConstraints(), vars[i].getVariables());
                }
                return new MinusNode(nodes);
            }
        }
        if(Choco.DEBUG){
            throw new RuntimeException("Could not found an implementation for MinusManager !");
        }
        return null;
    }


    /**
     * Build a real expression
     * @param solver
     * @param vars
     * @return
     */
    public RealExp makeRealExpression(Solver solver, RealExpressionVariable... vars){
        if(solver instanceof CPSolver){
            CPSolver s = (CPSolver)solver;
            if(vars.length == 2){
                RealExp r1 = getRealExp(s, vars[0]);
                RealExp r2 = getRealExp(s, vars[1]);
                return new RealMinus(s, r1, r2);
            }
        }
        if(Choco.DEBUG){
            throw new RuntimeException("Could not found an implementation for MinusManager !");
        }
        return null;
    }
}