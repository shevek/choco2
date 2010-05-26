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
import choco.kernel.solver.constraints.integer.AbstractUnIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Implements a constraint X != C, with X a variable and C a constant.
 */
public final class NotEqualXC extends AbstractUnIntSConstraint {

	/**
	 * The search constant of the constraint
	 */
	protected final int cste;

	/**
	 * Constructs the constraint with the specified variables and constant.
	 *
	 * @param x0 the search valued domain variable
	 * @param c  the search constant used in the inequality.
	 */

	public NotEqualXC(IntDomainVar x0, int c) {
		super(x0);
		this.cste = c;
	}

	@Override
	public int getFilteredEventMask(int idx) {
		if(!v0.hasEnumeratedDomain()) return IntVarEvent.INSTINT_MASK +IntVarEvent.BOUNDS_MASK;
		else return IntVarEvent.INSTINT_MASK;
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Pretty print of the constraint.
	 */
	@Override
	public String pretty() {
		return v0 + " != " + cste;
	}

	/**
	 * The single propagation method. <br>
	 * Note that after the first propagation, the constraint is set passive
	 * (to prevent from further calls to propagation methods)
	 */

	public void propagate() throws ContradictionException {
		if (v0.removeVal(this.cste, this, false)){
            this.setEntailed();
        }else if(!v0.canBeInstantiatedTo(this.cste)){
            this.setEntailed();
        }
	}


	/**
	 * in case the bounds have changed and the domain is approximated by an interval,
	 * we need to re-propagate
	 */
	@Override
	public void awakeOnInf(int idx) throws ContradictionException {
		assert(idx == 0);
		assert(!v0.hasEnumeratedDomain());
		this.propagate();
	}

	/**
	 * in case the bounds have changed and the domain is approximated by an interval,
	 * we need to re-propagate
	 */
	@Override
	public void awakeOnSup(int idx) throws ContradictionException {
		assert(idx == 0);
		assert(!v0.hasEnumeratedDomain());
		this.propagate();
	}

	/**
	 * When the one and only variable of the constraint becomes instantiated
	 * Need to check that the value of the variable is not the value forbidden by the constraint
	 *
	 * @param idx index of the variable (always 0)
	 * @throws choco.kernel.solver.ContradictionException
	 */
	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		assert(idx == 0);
		if (v0.getVal() == this.cste) this.fail();
	}

	/**
	 * When a value is removed from the domain of the one and only variable:
	 * Nothing to be propagated.
	 *
	 * @param idx index of the variable (always 0)
	 * @param x   value that was removed
	 * @throws choco.kernel.solver.ContradictionException
	 */
	@Override
	public void awakeOnRem(int idx, int x) throws ContradictionException {
		assert(idx == 0);
	}

	/**
	 * When the whole domain of <code>v0</code> is below or above <code>cste</code>,
	 * we know for sure whether the constraint will be satisfied or not
	 */

	@Override
	public Boolean isEntailed() {
		if (!v0.canBeInstantiatedTo(this.cste))
			return Boolean.TRUE;
		else if (v0.isInstantiatedTo(this.cste))
			return Boolean.FALSE;
		else
			return null;
	}

	/**
	 * tests if the constraint is satisfied when the variables are instantiated.
	 */

	@Override
	public boolean isSatisfied(int[] tuple) {
		return (tuple[0] != this.cste);
	}

	/**
	 * tests if the constraint is consistent with respect to the current state of domains
	 *
	 * @return true iff the constraint is bound consistent (same as arc consistent)
	 */
	@Override
	public boolean isConsistent() {
		return (v0.hasEnumeratedDomain() ?
				!(v0.canBeInstantiatedTo(this.cste)) :
					((v0.getInf() != this.cste) && (v0.getSup() != this.cste)));
	}

	@Override
	public AbstractSConstraint opposite(Solver solver) {
		return (AbstractSConstraint) solver.eq(v0, cste);
	}


}
