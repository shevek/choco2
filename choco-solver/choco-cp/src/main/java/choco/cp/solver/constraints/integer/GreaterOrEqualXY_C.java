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


package choco.cp.solver.constraints.integer;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Implements a constraint X + Y >= C, with X and Y two variables and C a constant.
 */
public final class GreaterOrEqualXY_C extends AbstractBinIntSConstraint {

	/**
	 * The search constant of the constraint
	 */
	protected final int cste;

	/**
	 * Constructs the constraint with the specified variables and constant.
	 *
	 * @param x0 Should be greater than <code>x0+c</code>.
	 * @param x1 Should be less than <code>x0-c</code>.
	 * @param c  The search constant used in the inequality.
	 */

	public GreaterOrEqualXY_C(IntDomainVar x0, IntDomainVar x1, int c) {
		super(x0, x1);
		this.cste = c;
	}


	@Override
	public int getFilteredEventMask(int idx) {
		return IntVarEvent.INSTINTbitvector + IntVarEvent.BOUNDSbitvector;
		// return 0x0B;
	}


	private final void updateInfV0() throws ContradictionException {
		v0.updateInf(cste - v1.getSup(), this, false);
	}

	private final void updateInfV1() throws ContradictionException {
		v1.updateInf( cste - v0.getSup(), this, false);
	}
	/**
	 * The propagation on constraint awake events.
	 *
	 * @throws choco.kernel.solver.ContradictionException
	 *
	 */

	public void propagate() throws ContradictionException {
		updateInfV0();
		updateInfV1();
	}


	/**
	 * Propagation when a minimal bound of a variable was modified.
	 *
	 * @param idx The index of the variable.
	 * @throws choco.kernel.solver.ContradictionException
	 *
	 */

	@Override
	public void awakeOnInf(int idx) throws ContradictionException {
		if( v0.getInf() + v1.getInf() >= cste) setEntailed();
	}


	/**
	 * Propagation when a maximal bound of a variable was modified.
	 *
	 * @param idx The index of the variable.
	 * @throws choco.kernel.solver.ContradictionException
	 *
	 */

	@Override
	public void awakeOnSup(int idx) throws ContradictionException {
		if (idx == 0) updateInfV1();
		else assert(idx == 1); updateInfV0();
	}


	/**
	 * Propagation when a variable is instantiated.
	 *
	 * @param idx The index of the variable.
	 * @throws choco.kernel.solver.ContradictionException
	 *
	 */

	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		if (idx == 0) updateInfV1();
		else updateInfV0();
		assert(v0.getInf() + v1.getInf() >= this.cste);
		this.setEntailed();
	}

	/**
	 * Checks if the listeners must be checked or must fail.
	 */

	@Override
	public Boolean isEntailed() {
		if (v0.getSup() + v1.getSup() < this.cste)
			return Boolean.FALSE;
		else if (v0.getInf() + v1.getInf() >= this.cste)
			return Boolean.TRUE;
		return null;
	}


	/**
	 * Checks if the constraint is satisfied when the variables are instantiated.
	 *
	 * @return true if the constraint is satisfied
	 */

	@Override
	public boolean isSatisfied(int[] tuple) {
		return tuple[0] + tuple[1] >= this.cste;
	}

	/**
	 * tests if the constraint is consistent with respect to the current state of domains
	 *
	 * @return true iff the constraint is bound consistent (weaker than arc consistent)
	 */
	@Override
	public boolean isConsistent() {
		return ((v0.getInf() + v1.getSup() >=  this.cste) && (v0.getSup() + v1.getInf() >= this.cste));
	}

	@Override
	public AbstractSConstraint opposite(Solver solver) {
		return (AbstractSConstraint) solver.lt(solver.plus(v0, v1),cste);
	}

	@Override
	public String pretty() {
		StringBuffer sb = new StringBuffer();
		sb.append(v0).append(" + ").append(v1);
		sb.append(" >= ").append(this.cste);
		return sb.toString();
	}

}
