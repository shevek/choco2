/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package choco.cp.solver.constraints.integer;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.integer.AbstractTernIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * a constraint modelling X * Y = Z
 */
public final class TimesXYZ extends AbstractTernIntSConstraint {

  public TimesXYZ(IntDomainVar x, IntDomainVar y, IntDomainVar z) {
    super(x, y, z);
  }

    @Override
    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
    }

  @Override
public boolean isSatisfied(int[] tuple) {
    return (tuple[0] * tuple[1] == tuple[2]);
  }

  @Override
public String pretty() {
    return v0.pretty() + " * " + v1.pretty() + " = " + v2.pretty();
  }

  @Override
public void awakeOnSup(int idx) throws ContradictionException {
    if (idx == 0) {
		awakeOnX();
	} else if (idx == 1) {
		awakeOnY();
	} else if (idx == 2) {
      awakeOnZ();
      if (!(v2.canBeInstantiatedTo(0))) {
        v2.updateSup(getZmax(), this, false);
      }
    }
  }

  @Override
public void awakeOnInf(int idx) throws ContradictionException {
    if (idx == 0) {
		awakeOnX();
	} else if (idx == 1) {
		awakeOnY();
	} else if (idx == 2) {
      awakeOnZ();
      if (!(v2.canBeInstantiatedTo(0))) {
        v2.updateInf(getZmin(), this, false);
      }
    }
  }

  public void filter(int idx) throws ContradictionException {
    if (idx == 0) {
		awakeOnX();
	} else if (idx == 1) {
		awakeOnY();
	} else if (idx == 2) {
		awakeOnZ();
	}
  }

  @Override
public void awakeOnInst(int vIdx) throws ContradictionException {
    filter(vIdx);
  }

    /**
   * reaction when X (v0) is updated
   *
   * @throws ContradictionException
   */
  protected void awakeOnX() throws ContradictionException {
    if (v0.isInstantiatedTo(0)) {
      v2.instantiate(0, this, false);
    }
    if ((v2.isInstantiatedTo(0)) && (!v0.canBeInstantiatedTo(0))) {
      v1.instantiate(0, this, false);
    } else if (!v2.canBeInstantiatedTo(0)) {
      updateYandX();
    } else if (!(v2.isInstantiatedTo(0))) {
      shaveOnYandX();
    }
    if (!(v2.isInstantiatedTo(0))) {
      v2.updateInf(getZmin(), this, false);
      v2.updateSup(getZmax(), this, false);
    }
  }

  protected void awakeOnY() throws ContradictionException {
    if (v1.isInstantiatedTo(0)) {
      v2.instantiate(0, this, false);
    }
    if ((v2.isInstantiatedTo(0)) && (!v1.canBeInstantiatedTo(0))) {
      v0.instantiate(0, this, false);
    } else if (!v2.canBeInstantiatedTo(0)) {
      updateXandY();
    } else if (!(v2.isInstantiatedTo(0))) {
      shaveOnXandY();
    }
    if (!(v2.isInstantiatedTo(0))) {
      v2.updateInf(getZmin(), this, false);
      v2.updateSup(getZmax(), this, false);
    }
  }

  protected void awakeOnZ() throws ContradictionException {
    if (!(v2.canBeInstantiatedTo(0))) {
      updateX();
      if (updateY()) {
        updateXandY();
      }
    } else if (!(v2.isInstantiatedTo(0))) {
      shaveOnX();
      if (shaveOnY()) {
        shaveOnXandY();
      }
    }
    if (v2.isInstantiatedTo(0)) {
      propagateZero();
    }
  }

  @Override
public Boolean isEntailed() {
    if (this.isCompletelyInstantiated() && this.isSatisfied()) {
      return Boolean.TRUE;
    } else if (v2.isInstantiatedTo(0)) {
      if (v0.isInstantiatedTo(0) || v1.isInstantiatedTo(0)) {
		return Boolean.TRUE;
	} else if (!(v0.canBeInstantiatedTo(0)) && !(v1.canBeInstantiatedTo(0))) {
		return Boolean.FALSE;
	} else {
		return null;
	}
    } else if (!(v2.canBeInstantiatedTo(0))) {
      if (v0.getSup() < getXminIfNonZero()) {
		return Boolean.FALSE;
	} else if (v0.getInf() > getXmaxIfNonZero()) {
		return Boolean.FALSE;
	} else if (v1.getSup() < getYminIfNonZero()) {
		return Boolean.FALSE;
	} else if (v1.getInf() > getYmaxIfNonZero()) {
		return Boolean.FALSE;
	} else {
		return null;
	}
    } else {
		return null;
	}
  }

  protected int getXminIfNonZero() {
    if ((v2.getInf() >= 0) && (v1.getInf() >= 0)) {
		return ruleA1(v0, v2, v1);
	} else
    if ((v2.getSup() <= 0) && (v1.getSup() <= 0)) {
		return ruleB1(v0, v2, v1);
	} else
    if ((v2.getInf() >= 0) && (v1.getSup() <= 0)) {
		return ruleC1(v0, v2, v1);
	} else
    if ((v2.getSup() <= 0) && (v1.getInf() >= 0)) {
		return ruleD1(v0, v2, v1);
	} else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v1.getSup() <= 0)) {
		return ruleE1(v0, v2, v1);
	} else if ((v2.getSup() <= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
		return ruleF1(v0, v2, v1);
	} else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v1.getInf() >= 0)) {
		return ruleG1(v0, v2, v1);
	} else if ((v2.getInf() >= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
		return ruleH1(v0, v2, v1);
	} else
    if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
		return ruleI1(v0, v2, v1);
	} else {
		throw new SolverException("None of the cases is active!");
	}
  }

  protected int getXmaxIfNonZero() {
    if ((v2.getInf() >= 0) && (v1.getInf() >= 0)) {
		return ruleA2(v0, v2, v1);
	} else
    if ((v2.getSup() <= 0) && (v1.getSup() <= 0)) {
		return ruleB2(v0, v2, v1);
	} else
    if ((v2.getInf() >= 0) && (v1.getSup() <= 0)) {
		return ruleC2(v0, v2, v1);
	} else
    if ((v2.getSup() <= 0) && (v1.getInf() >= 0)) {
		return ruleD2(v0, v2, v1);
	} else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v1.getSup() <= 0)) {
		return ruleE2(v0, v2, v1);
	} else if ((v2.getSup() <= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
		return ruleF2(v0, v2, v1);
	} else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v1.getInf() >= 0)) {
		return ruleG2(v0, v2, v1);
	} else if ((v2.getInf() >= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
		return ruleH2(v0, v2, v1);
	} else
    if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
		return ruleI2(v0, v2, v1);
	} else {
		throw new SolverException("None of the cases is active!");
	}
  }

  protected int getYminIfNonZero() {
    if ((v2.getInf() >= 0) && (v0.getInf() >= 0)) {
		return ruleA1(v1, v2, v0);
	} else
    if ((v2.getSup() <= 0) && (v0.getSup() <= 0)) {
		return ruleB1(v1, v2, v0);
	} else
    if ((v2.getInf() >= 0) && (v0.getSup() <= 0)) {
		return ruleC1(v1, v2, v0);
	} else
    if ((v2.getSup() <= 0) && (v0.getInf() >= 0)) {
		return ruleD1(v1, v2, v0);
	} else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v0.getSup() <= 0)) {
		return ruleE1(v1, v2, v0);
	} else if ((v2.getSup() <= 0) && (v0.getInf() <= 0) && (v0.getSup() >= 0)) {
		return ruleF1(v1, v2, v0);
	} else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v0.getInf() >= 0)) {
		return ruleG1(v1, v2, v0);
	} else if ((v2.getInf() >= 0) && (v0.getInf() <= 0) && (v0.getSup() >= 0)) {
		return ruleH1(v1, v2, v0);
	} else
    if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v0.getInf() <= 0) && (v0.getSup() >= 0)) {
		return ruleI1(v1, v2, v0);
	} else {
		throw new SolverException("None of the cases is active!");
	}
  }

  protected int getYmaxIfNonZero() {
    if ((v2.getInf() >= 0) && (v0.getInf() >= 0)) {
		return ruleA2(v1, v2, v0);
	} else
    if ((v2.getSup() <= 0) && (v0.getSup() <= 0)) {
		return ruleB2(v1, v2, v0);
	} else
    if ((v2.getInf() >= 0) && (v0.getSup() <= 0)) {
		return ruleC2(v1, v2, v0);
	} else
    if ((v2.getSup() <= 0) && (v0.getInf() >= 0)) {
		return ruleD2(v1, v2, v0);
	} else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v0.getSup() <= 0)) {
		return ruleE2(v1, v2, v0);
	} else if ((v2.getSup() <= 0) && (v0.getInf() <= 0) && (v0.getSup() >= 0)) {
		return ruleF2(v1, v2, v0);
	} else if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v0.getInf() >= 0)) {
		return ruleG2(v1, v2, v0);
	} else if ((v2.getInf() >= 0) && (v0.getInf() <= 0) && (v0.getSup() >= 0)) {
		return ruleH2(v1, v2, v0);
	} else
    if ((v2.getInf() <= 0) && (v2.getSup() >= 0) && (v0.getInf() <= 0) && (v0.getSup() >= 0)) {
		return ruleI2(v1, v2, v0);
	} else {
		throw new SolverException("None of the cases is active!");
	}
  }

  protected int getZmin() {
    if ((v0.getInf() >= 0) && (v1.getInf() >= 0)) {
		return ruleA3(v2, v0, v1);
	} else
    if ((v0.getSup() <= 0) && (v1.getSup() <= 0)) {
		return ruleB3(v2, v0, v1);
	} else
    if ((v0.getInf() >= 0) && (v1.getSup() <= 0)) {
		return ruleC3(v2, v0, v1);
	} else
    if ((v0.getSup() <= 0) && (v1.getInf() >= 0)) {
		return ruleD3(v2, v0, v1);
	} else if ((v0.getInf() <= 0) && (v0.getSup() >= 0) && (v1.getSup() <= 0)) {
		return ruleE3(v2, v0, v1);
	} else if ((v0.getSup() <= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
		return ruleF3(v2, v0, v1);
	} else if ((v0.getInf() <= 0) && (v0.getSup() >= 0) && (v1.getInf() >= 0)) {
		return ruleG3(v2, v0, v1);
	} else if ((v0.getInf() >= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
		return ruleH3(v2, v0, v1);
	} else
    if ((v0.getInf() <= 0) && (v0.getSup() >= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
		return ruleI3(v2, v0, v1);
	} else {
		throw new SolverException("None of the cases is active!");
	}
  }

  protected int getZmax() {
    if ((v0.getInf() >= 0) && (v1.getInf() >= 0)) {
		return ruleA4(v2, v0, v1);
	} else
    if ((v0.getSup() <= 0) && (v1.getSup() <= 0)) {
		return ruleB4(v2, v0, v1);
	} else
    if ((v0.getInf() >= 0) && (v1.getSup() <= 0)) {
		return ruleC4(v2, v0, v1);
	} else
    if ((v0.getSup() <= 0) && (v1.getInf() >= 0)) {
		return ruleD4(v2, v0, v1);
	} else if ((v0.getInf() <= 0) && (v0.getSup() >= 0) && (v1.getSup() <= 0)) {
		return ruleE4(v2, v0, v1);
	} else if ((v0.getSup() <= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
		return ruleF4(v2, v0, v1);
	} else if ((v0.getInf() <= 0) && (v0.getSup() >= 0) && (v1.getInf() >= 0)) {
		return ruleG4(v2, v0, v1);
	} else if ((v0.getInf() >= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
		return ruleH4(v2, v0, v1);
	} else
    if ((v0.getInf() <= 0) && (v0.getSup() >= 0) && (v1.getInf() <= 0) && (v1.getSup() >= 0)) {
		return ruleI4(v2, v0, v1);
	} else {
		throw new SolverException("None of the cases is active!");
	}
  }

  private int ruleA1(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infCeilmM(a, b, c);
  }

  private int ruleB1(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infCeilMm(a, b, c);
  }

  private int ruleC1(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infCeilMM(a, b, c);
  }

  private int ruleD1(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infCeilmm(a, b, c);
  }

  private int ruleE1(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infCeilMM(a, b, c);
  }

  private int ruleF1(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infCeilmP(a, b, c);
  }

  private int ruleG1(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infCeilmm(a, b, c);
  }

  private int ruleH1(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infCeilMN(a, b, c);
  }

  private int ruleI1(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infCeilxx(a, b, c);
  }

  private int ruleA2(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supCeilMm(a, b, c);
  }

  private int ruleB2(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supCeilmM(a, b, c);
  }

  private int ruleC2(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supCeilmm(a, b, c);
  }

  private int ruleD2(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supCeilMM(a, b, c);
  }

  private int ruleE2(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supCeilmM(a, b, c);
  }

  private int ruleF2(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supCeilmN(a, b, c);
  }

  private int ruleG2(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supCeilMm(a, b, c);
  }

  private int ruleH2(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supCeilMP(a, b, c);
  }

  private int ruleI2(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supCeilEq(a, b, c);
  }

  private int ruleA3(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infFloormm(a, b, c);
  }

  private int ruleB3(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infFloorMM(a, b, c);
  }

  private int ruleC3(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infFloorMm(a, b, c);
  }

  private int ruleD3(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infFloormM(a, b, c);
  }

  private int ruleE3(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infFloorMm(a, b, c);
  }

  private int ruleF3(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infFloormM(a, b, c);
  }

  private int ruleG3(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infFloormM(a, b, c);
  }

  private int ruleH3(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infFloorMm(a, b, c);
  }

  private int ruleI3(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return infFloorxx(a, b, c);
  }

  private int ruleA4(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supFloorMM(a, b, c);
  }

  private int ruleB4(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supFloormm(a, b, c);
  }

  private int ruleC4(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supFloormM(a, b, c);
  }

  private int ruleD4(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supFloorMm(a, b, c);
  }

  private int ruleE4(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supFloormm(a, b, c);
  }

  private int ruleF4(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supFloormm(a, b, c);
  }

  private int ruleG4(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supFloorMM(a, b, c);
  }

  private int ruleH4(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supFloorMM(a, b, c);
  }

  private int ruleI4(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return supFloorEq(a, b, c);
  }

  private int infFloormm(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return b.getInf() * c.getInf();
  }

  private int infFloormM(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return b.getInf() * c.getSup();
  }

  private int infFloorMm(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return b.getSup() * c.getInf();
  }

  private int infFloorMM(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return b.getSup() * c.getSup();
  }

  private int supFloormm(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return b.getInf() * c.getInf();
  }

  private int supFloormM(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return b.getInf() * c.getSup();
  }

  private int supFloorMm(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return b.getSup() * c.getInf();
  }

  private int supFloorMM(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return b.getSup() * c.getSup();
  }

  private int getMinPositive(IntDomainVar v) {
    return 1;
  }

  private int getMaxNegative(IntDomainVar v) {
    return -1;
  }

  private int getNonZeroSup(IntDomainVar v) {
    return Math.min(v.getSup(), -1);
  }

  private int getNonZeroInf(IntDomainVar v) {
    return Math.max(v.getInf(), 1);
  }

  private int infCeilmm(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return MathUtils.divCeil(b.getInf(), getNonZeroInf(c));
  }

  private int infCeilmM(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return MathUtils.divCeil(getNonZeroInf(b), c.getSup());
  }

  private int infCeilMm(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return MathUtils.divCeil(getNonZeroSup(b), c.getInf());
  }

  private int infCeilMM(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return MathUtils.divCeil(b.getSup(), getNonZeroSup(c));
  }

  private int infCeilmP(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return MathUtils.divCeil(b.getInf(), getMinPositive(c));
  }

  private int infCeilMN(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return MathUtils.divCeil(b.getSup(), getMaxNegative(c));
  }

  private int supCeilmm(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return MathUtils.divFloor(getNonZeroInf(b), c.getInf());
  }

  private int supCeilmM(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return MathUtils.divFloor(b.getInf(), getNonZeroSup(c));
  }

  private int supCeilMm(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return MathUtils.divFloor(b.getSup(), getNonZeroInf(c));
  }

  private int supCeilMM(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return MathUtils.divFloor(getNonZeroSup(b), c.getSup());
  }

  private int supCeilmN(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return MathUtils.divFloor(b.getInf(), getMaxNegative(c));
  }

  private int supCeilMP(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return MathUtils.divFloor(b.getSup(), getMinPositive(c));
  }

  private int infFloorxx(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    if (b.getInf() * c.getSup() < b.getSup() * c.getInf()) {
		return b.getInf() * c.getSup();
	} else {
		return b.getSup() * c.getInf();
	}
  }

  private int supFloorEq(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    if (b.getInf() * c.getInf() > b.getSup() * c.getSup()) {
		return b.getInf() * c.getInf();
	} else {
		return b.getSup() * c.getSup();
	}
  }

  private int infCeilxx(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return Math.min(MathUtils.divCeil(b.getInf(), getMinPositive(c)), MathUtils.divCeil(b.getSup(), getMaxNegative(c)));
  }  //v0.18

  private int supCeilEq(IntDomainVar a, IntDomainVar b, IntDomainVar c) {
    return Math.max(MathUtils.divFloor(b.getInf(), getMaxNegative(c)), MathUtils.divFloor(b.getSup(), getMinPositive(c)));
  }   //v0.18

  @Override
public void propagate() throws ContradictionException {
      filter(0);
      filter(1);
      filter(2);
  }

  /**
   * propagate the fact that v2 (Z) is instantiated to 0
   *
   * @throws ContradictionException
   */
  public void propagateZero() throws ContradictionException {
    if (!(v1.canBeInstantiatedTo(0))) {
      v0.instantiate(0, this, false);
    }
    if (!(v0.canBeInstantiatedTo(0))) {
      v1.instantiate(0, this, false);
    }
  }

  /**
   * Updating X and Y when Z cannot be 0
   */
  protected boolean updateX() throws ContradictionException {
    boolean infChange = v0.updateInf(getXminIfNonZero(), this, false);
    boolean supChange = v0.updateSup(getXmaxIfNonZero(), this, false);
    return (infChange || supChange);
  }

  protected boolean updateY() throws ContradictionException {
    boolean infChange = v1.updateInf(getYminIfNonZero(), this, false);
    boolean supChange = v1.updateSup(getYmaxIfNonZero(), this, false);
    return (infChange || supChange);
  }

  /**
   * loop until a fix point is reach (see testProd14)
   */
  protected void updateXandY() throws ContradictionException {
    while (updateX() && updateY()) {
      ;
    }
  }

  protected void updateYandX() throws ContradictionException {
    while (updateY() && updateX()) {
      ;
    }
  }

  /**
   * Updating X and Y when Z can  be 0
   */

  protected boolean shaveOnX() throws ContradictionException {
    int xmin = getXminIfNonZero();
    int xmax = getXmaxIfNonZero();
    if ((xmin > v0.getSup()) || (xmax < v0.getInf())) {
      v2.instantiate(0, this, false);
      propagateZero();    // make one of X,Y be 0 if the other cannot be
      return false;       //no more shaving need to be performed
    } else {
      boolean infChange = (!(v1.canBeInstantiatedTo(0)) && v0.updateInf(Math.min(0, xmin), this, false));
      boolean supChange = (!(v1.canBeInstantiatedTo(0)) && v0.updateSup(Math.max(0, xmax), this, false));
      return (infChange || supChange);
    }
  }

  protected boolean shaveOnY() throws ContradictionException {
    int ymin = getYminIfNonZero();
    int ymax = getYmaxIfNonZero();
    if ((ymin > v1.getSup()) || (ymax < v1.getInf())) {
      v2.instantiate(0, this, false);
      propagateZero();    // make one of X,Y be 0 if the other cannot be
      return false;       //no more shaving need to be performed
    } else {
      boolean infChange = (!(v0.canBeInstantiatedTo(0)) && v1.updateInf(Math.min(0, ymin), this, false));
      boolean supChange = (!(v0.canBeInstantiatedTo(0)) && v1.updateSup(Math.max(0, ymax), this, false));
      return (infChange || supChange);
    }
  }

  protected void shaveOnXandY() throws ContradictionException {
    while (shaveOnX() && shaveOnY()) {
    }
  }

  protected void shaveOnYandX() throws ContradictionException {
    while (shaveOnY() && shaveOnX()) {
    }
  }

}