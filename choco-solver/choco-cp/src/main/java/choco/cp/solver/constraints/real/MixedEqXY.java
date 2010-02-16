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
package choco.cp.solver.constraints.real;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.real.AbstractBinRealIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.real.RealIntervalConstant;
import choco.kernel.solver.variables.real.RealVar;

public class MixedEqXY extends AbstractBinRealIntSConstraint{

  public MixedEqXY(RealVar v0, IntDomainVar v1) {
    super(v0, v1);
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public boolean isConsistent() {
    return v1.getInf() <= v0.getSup() && v0.getInf() <= v1.getSup();
  }

  public boolean isSatisfied() {
    return isConsistent();
  }

  public void propagate() throws ContradictionException {
    if (v0.getInf() > v1.getInf()) {
      updateIInf();
    }
    if (v0.getSup() < v1.getSup()) {
      updateISup();
    }
    updateReal();
  }

  public void awakeOnInf(int idx) throws ContradictionException {
    if (idx == 0) {
      if (v0.getInf() > v1.getInf()) {
        updateIInf();
        updateReal();
      }
    } else if (idx == 1) {
      updateReal();
    }
  }

  public void awakeOnSup(int idx) throws ContradictionException {
    if (idx == 0) {
      if (v0.getSup() < v1.getSup()) {
        updateISup();
        updateReal();
      }
    } else if (idx == 1) {
      updateReal();
    }
  }

  public void awakeOnInst(int varIdx) throws ContradictionException {
    if (varIdx == 1) {
      updateReal();
    }
  }

  public void awakeOnRem(int varIdx, int val) throws ContradictionException {
  }

  public void awakeOnRemovals(int varIdx, DisposableIntIterator deltaDomain) throws ContradictionException {
  }

  public void awakeOnBounds(int varIdx) throws ContradictionException {
    if (varIdx == 0) {
      if (v0.getInf() > v1.getInf()) {
        updateIInf();
      }
      if (v0.getSup() < v1.getSup()) {
        updateISup();
      }
      updateReal();
    } else if (varIdx == 1) {
      updateReal();
    }
  }

  protected void updateIInf() throws ContradictionException {
    v1.updateInf((int) Math.ceil(v0.getInf()), cIdx1);
  }

  protected void updateISup() throws ContradictionException {
    v1.updateSup((int) Math.floor(v0.getSup()), cIdx1);
  }

  protected void updateReal() throws ContradictionException {
    v0.intersect(new RealIntervalConstant(v1.getInf(), v1.getSup()), cIdx0);
  }

}
