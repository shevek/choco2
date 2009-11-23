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
package choco.cp.solver.search.integer.branching;

import static choco.kernel.solver.ContradictionException.Type.UNKNOWN;
import choco.cp.solver.constraints.global.pack.PackSConstraint;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.search.IntBranchingDecision;
import choco.kernel.solver.search.integer.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A specialized search strategy for packing problem.
 * At every backtrack, we state that the item can not be packed in equivalent bins.
 * Two bins are equivalent if they have the same remaining space.
 * @author Arnaud Malapert</br>
 * @since 7 déc. 2008 version 2.0.1</br>
 * @version 2.0.1</br>
 */
public final class PackDynRemovals extends AssignVar {

	public final PackSConstraint pack;


	public PackDynRemovals(VarSelector varSel, ValSelector valHeuri,
			PackSConstraint pack) {
		super(varSel, valHeuri);
		this.pack = pack;
	}


	public void removeEmptyBins(IntDomainVar bin) throws ContradictionException {
		final DisposableIntIterator iter=bin.getDomain().getIterator();
        try{
            while(iter.hasNext()) {
                final int b=iter.next();
                if(pack.isEmpty(b)) {
                    bin.remVal(b);
                }
            }
        }finally{
            iter.dispose();
        }
	}
	public final void fail() throws ContradictionException {
		getManager().solver.getPropagationEngine().raiseContradiction(this, UNKNOWN);
	}

	public void removeEquivalentBins(IntDomainVar bin,int bup) throws ContradictionException {
		final DisposableIntIterator iter=bin.getDomain().getIterator();
		final int space = pack.getRemainingSpace(bup);
        try{
		while(iter.hasNext()) {
			final int b=iter.next();
			if(pack.getRemainingSpace(b)==space) {bin.remVal(b);}
		}
        }finally {
            iter.dispose();
        }
	}
	
	private int reuseVal;
	/**
	 * @see choco.cp.solver.search.integer.branching.AssignVar#goUpBranch(java.lang.Object, int)
	 */
	@Override
	public void goUpBranch(final IntBranchingDecision decision) throws ContradictionException {
		super.goUpBranch(decision);
		reuseVal = decision.getBranchingValue();
		if(pack.svars[reuseVal].isInstantiated()) {
			//we cant pack another item into the bin, so the free space is lost.
			//the previous partial assignment dominates any assignment where the item is packed into antother bin
			fail();
		}else if(pack.isEmpty(reuseVal)) {
			//there was a single item into the bin, so we cant pack the item into a empty bin again
			removeEmptyBins(decision.getBranchingIntVar());
		} else {
			//there was other items into the bin, so we cant pack the item into a bin with the same available space again
			removeEquivalentBins(decision.getBranchingIntVar(), reuseVal);
		}
	}


}