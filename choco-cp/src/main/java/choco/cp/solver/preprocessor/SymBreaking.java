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
package choco.cp.solver.preprocessor;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.kernel.common.util.IntIterator;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.model.variables.integer.IntegerVariable;

import java.util.Iterator;
import java.util.ListIterator;

/**
 * Simple symetry detection.
 */
public class SymBreaking {

    protected IntegerVariable[] maxclique = null;

    public void setMaxClique(IntegerVariable[] clique) {
        if (maxclique == null || maxclique.length < clique.length) {
            maxclique = clique;
        }
    }

    /**
     * Break symetries in graph coloring by instantiating
     * the largest clique. Conditions are :
     * - a unique domain and only difference constraints
     * @param m the cp model
     */
    public void addSymBreakingConstraint(CPModel m) {
        if (maxclique != null && checkOnlyOneDomain(m) && checkOnlyDiff(m)) {
            IntIterator it = m.getIntVar(0).getDomainIterator();
            for (int i = 0; i < maxclique.length && it.hasNext(); i++) {
                m.addConstraint(Choco.eq(maxclique[i], it.next()));
            }
        }
    }


    /**
     * Are all domains identical ?
     * @param m the cpmodel
     * @return boolean
     */
    public boolean checkOnlyOneDomain(CPModel m) {
        ListIterator<IntegerVariable> it = m.getIntVarIterator();
        if (it.hasNext()) {
            IntegerVariable v = it.next();
            int lb = v.getLowB();
            int ub = v.getUppB();
            if (v.getValues() != null)
                return false;
            while (it.hasNext()) {
                IntegerVariable v2 = it.next();
                if (v2.getLowB() != lb ||
                    v2.getUppB() != ub ||
                    v2.getValues() != null)
                return false;
            }
        }
        return true;
    }

    /**
     * Check is only difference constraints have been
     * posted to the solver
     * @param m the cp model
     * @return boolean
     */
    public boolean checkOnlyDiff(CPModel m) {
        Iterator<Constraint> it = m.getConstraintIterator();
        for (; it.hasNext();) {
            Constraint ct =  it.next();
            if (ct.getConstraintType() != ConstraintType.NEQ &&
                ct.getConstraintType() != ConstraintType.ALLDIFFERENT) {
                return false;
            }
        }
        return true;
    }

}
