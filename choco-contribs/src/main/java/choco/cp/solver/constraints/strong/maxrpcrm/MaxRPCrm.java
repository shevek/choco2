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
package choco.cp.solver.constraints.strong.maxrpcrm;

import choco.cp.solver.constraints.strong.AbstractStrongConsistency;
import choco.cp.solver.constraints.strong.ISpecializedConstraint;
import choco.cp.solver.constraints.strong.SCConstraint;
import choco.kernel.common.util.IntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.BitSet;
import java.util.Set;

public class MaxRPCrm extends AbstractStrongConsistency<MaxRPCVariable> {

	/**
	 * File de propagation
	 */
	private final BitSet queue;

	// private final boolean[] instanciated;

	public static int nbPropag = 0;

	public static int nbArcRevise = 0;

	public static int nbPCRevise = 0;

	// private int[] counts;

	public MaxRPCrm(IntDomainVar[] variables,
			ISpecializedConstraint[] constraints, Set<String> options) {
		super(variables, constraints, MaxRPCVariable.class, options
				.contains("light") ? MaxRPCConstraintLight.class
				: MaxRPCConstraintFull.class);

		for (SCConstraint c : this.constraints) {
			((AbstractMaxRPCConstraint) c).compute3Cliques();
		}

		queue = new BitSet(getNbVars());

		// counts = new int[variables.length];
		// instanciated = new boolean[getNbVars()];
	}

	@Override
	public void awake() throws ContradictionException {
		super.awake();
		// for (int i = getNbVars(); --i >= 0;) {
		// counts[i] = getVariable(i).getSVariable().getDomainSize();
		// }
		queue.set(0, getNbVars());
		propagate();
	}

	@Override
	public void awakeOnInst(int idx) {
		touched(idx);
	}

	@Override
	public void awakeOnRemovals(int idx, IntIterator deltaDomain) {
		touched(idx);
	}

	@Override
	public void awakeOnInf(int idx) {
		touched(idx);
	}

	@Override
	public void awakeOnSup(int idx) {
		touched(idx);
	}

	@Override
	public void awakeOnRem(int idx, int x) {
		touched(idx);
	}

	@Override
	public void awakeOnBounds(int idx) {
		touched(idx);
	}

	private void touched(int idx) {
		// if (getVariable(idx).getSVariable().getDomainSize() != counts[idx]) {
		queue.set(idx);
		// counts[idx] = getVariable(idx).getSVariable().getDomainSize();
		this.constAwake(false);
		// }
	}

	/**
	 * Effectue la propagation à partir de la file courante
	 * 
	 * @throws ContradictionException
	 */
	public void propagate() throws ContradictionException {

		// CPSolver.flushLogs();
		final BitSet queue = this.queue;
		// final boolean[] instanciated = this.instanciated;
		if (queue.isEmpty()) {
			return;
		}
		nbPropag++;
		// for (int i = getNbVars(); --i >= 0;) {
		// instanciated[i] = getVariable(i).getSVariable().isInstantiated();
		// }
		try {
			while (!queue.isEmpty()) {
				propagate(poll());
			}
		} catch (ContradictionException e) {
			queue.clear();
			throw e;
		}
	}

	private void propagate(MaxRPCVariable variable)
			throws ContradictionException {
		for (AbstractMaxRPCConstraint constraint : variable.getConstraints()) {
			try {
				for (int p = 2; --p >= 0;) {
					final MaxRPCVariable varToRev = constraint.getVariable(p);

					if (varToRev != variable) {
						// && !instanciated[varToRev.getId()]) {
						nbArcRevise++;
						if (constraint.revise(p)) {
							queue.set(varToRev.getId());
						}
					}
				}
			} catch (ContradictionException e) {
				constraint.increaseWeight();
				for (Clique c : constraint.cliques) {
					c.getC0().increaseWeight();
					c.getC1().increaseWeight();
				}
				throw e;
			}
		}

		// Révisions supplémentaires : la modification de la variable
		// courante entraine-t-elle l'annulation de certains supports ?
		for (Clique clique : variable.getCliques()) {
			if (pcRevise(clique, 0)) {
				queue.set(clique.getHomeConstraint().getVariable(0).getId());
			}
			if (pcRevise(clique, 1)) {
				queue.set(clique.getHomeConstraint().getVariable(1).getId());
			}
		}
	}

	private static boolean pcRevise(Clique t, int position)
			throws ContradictionException {
		nbPCRevise++;
		try {
			return t.getHomeConstraint().revisePC(t, position);
		} catch (ContradictionException e) {
			t.getHomeConstraint().increaseWeight();
			for (Clique c : t.getHomeConstraint().cliques) {
				c.getC0().increaseWeight();
				c.getC1().increaseWeight();
			}
			throw e;
		}
	}

	/**
	 * Récupère la prochaine variable de la file (plus petit domaine)
	 * 
	 * @return
	 */
	private MaxRPCVariable poll() {
		// final Arc a = queue.poll();
		// a.setPresent(false);
		// return a;
		MaxRPCVariable bestVariable = null;
		int bestValue = Integer.MAX_VALUE;

		final BitSet queue = this.queue;

		for (int i = queue.nextSetBit(0); i >= 0; i = queue.nextSetBit(i + 1)) {
			final MaxRPCVariable variable = getVariable(i);
			final int domainSize = variable.getSVariable().getDomainSize();
			if (domainSize < bestValue) {
				bestVariable = variable;
				bestValue = domainSize;
			}
		}

		queue.clear(bestVariable.getId());
		return bestVariable;
	}

	public int getWDeg(IntDomainVar v) {
		return variablesMap.get(v).getWDeg();
	}

	public int getDDeg(IntDomainVar v) {
		return variablesMap.get(v).getDDeg();
	}

	public int getNbCliques() {
		int nbCliques = 0;
		for (SCConstraint c : constraints) {
			nbCliques += ((AbstractMaxRPCConstraint) c).getNbCliques();
		}
		return nbCliques / 3;
	}
}
