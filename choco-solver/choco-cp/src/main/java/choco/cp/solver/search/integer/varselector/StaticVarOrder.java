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
package choco.cp.solver.search.integer.varselector;

import choco.kernel.memory.IStateInt;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A variable selector selecting the first non instantiated variable according to a given static order
 */
public class StaticVarOrder extends AbstractIntVarSelector {

    private final IStateInt last;

    public StaticVarOrder(Solver solver) {
        super(solver);
        this.last = solver.getEnvironment().makeInt(0);
    }

    public StaticVarOrder(Solver solver, IntDomainVar[] vars) {
        super(solver, vars);
        this.last = solver.getEnvironment().makeInt(0);
    }

    public IntDomainVar selectIntVar() {
        //<hca> it starts at last.get() and not last.get() +1 to be
        //robust to restart search loop
        for (int i = last.get(); i < vars.length; i++) {
            if (!vars[i].isInstantiated()) {
                last.set(i);
                return vars[i];

            }
        }
        return null;
    }
}
