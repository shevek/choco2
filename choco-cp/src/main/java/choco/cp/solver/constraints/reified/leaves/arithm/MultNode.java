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
package choco.cp.solver.constraints.reified.leaves.arithm;

import choco.cp.solver.constraints.integer.TimesXYZ;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.reified.ArithmNode;
import choco.kernel.solver.constraints.reified.INode;
import choco.kernel.solver.constraints.reified.NodeType;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 23 avr. 2008
 * Time: 15:30:18
 */
public class MultNode extends INode implements ArithmNode {

    public MultNode(INode[] subt) {
        super(subt, NodeType.MULT);
    }

    public int eval(int[] tuple) {
        return ((ArithmNode) subtrees[0]).eval(tuple) * ((ArithmNode) subtrees[1]).eval(tuple);
    }

    public IntDomainVar extractResult(Solver s) {
        IntDomainVar v1 = subtrees[0].extractResult(s);
        IntDomainVar v2 = subtrees[1].extractResult(s);
        IntDomainVar v3 = null;
        int a = v1.getInf() * v2.getInf();
        int b = v1.getInf() * v2.getSup();
        int c = v1.getSup() * v2.getInf();
        int d = v1.getSup() * v2.getSup();
        int lb = Math.min(Math.min(Math.min(a, b), c), d);
        int ub = Math.max(Math.max(Math.max(a, b), c), d);
        if(lb==0 && ub == 1){
            v3 = s.createBooleanVar("intermult");
        }else
        if (v1.hasEnumeratedDomain() && v2.hasEnumeratedDomain()) {
            v3 = s.createEnumIntVar("intermult", lb, ub);
        } else {
            v3 = s.createBoundIntVar("intermult", lb, ub);
        }
        s.post(new TimesXYZ(v1, v2, v3));
        return v3;
    }


    public String pretty() {
        return "(" + subtrees[0].pretty() + " * " + subtrees[1].pretty() + ")";
    }


    public boolean isALinearTerm() {
        int a = subtrees[0].countNbVar();
        int b = subtrees[1].countNbVar();
        return (a == 0 || b == 0) && (subtrees[0].isALinearTerm() && subtrees[1].isALinearTerm());
    }

    public int[] computeLinearExpr(int scope) {
        int[] coeffs = subtrees[0].computeLinearExpr(scope);
        int[] coeffs2 = subtrees[1].computeLinearExpr(scope);
        if (subtrees[0].isAConstant()) {
            for (int i = 0; i < scope + 1; i++) {
                coeffs[i] = coeffs2[i] * coeffs[scope];
            }
        } else if (subtrees[1].isAConstant()) {
            for (int i = 0; i < scope + 1; i++) {
                coeffs[i] = coeffs[i] * coeffs2[scope];
            }
        } else {
            for (int i = 0; i < scope + 1; i++) {
                if (coeffs[i] == 0) {
                    coeffs[i] = coeffs2[i];
                } else if (coeffs2[i] != 0) {
                    coeffs[i] = coeffs2[i] * coeffs[i];
                }
            }
        }
//        if (subtrees[0].isAVariable()) {
//            int idx = ((VariableLeaf) subtrees[0]).getIdx();
//            coeffs[idx] = coeffs2[scope];
//        } else if (subtrees[1].isAConstant()) { //two constant
//            coeffs[scope + 1] *= coeffs2[scope + 1];
//        }
        return coeffs;
    }

}
