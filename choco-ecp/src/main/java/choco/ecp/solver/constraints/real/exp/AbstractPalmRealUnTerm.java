package choco.ecp.solver.constraints.real.exp;

import choco.ecp.solver.PalmSolver;
import choco.ecp.solver.explanations.ExplainedSolver;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.variables.real.PalmRealInterval;
import choco.kernel.common.util.objects.ConstraintCollection;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.real.RealExp;
import choco.kernel.solver.constraints.real.exp.AbstractRealUnTerm;
import choco.kernel.solver.variables.real.RealInterval;

/**
 * J-CHOCO
 * Copyright (C) F. Laburthe, 1999-2003
 * <p/>
 * An open-source Constraint Programming Kernel
 * for Research and Education
 * <p/>
 * Created by: Guillaume on 24 juin 2004
 */

/**
 * Abstract implementation of a real binary term (like addition for instance).
 */
public abstract class AbstractPalmRealUnTerm extends AbstractRealUnTerm implements PalmRealInterval {
  /**
   * PalmExplanation of the last lower bound affectation.
   */
  protected Explanation explanationOnInf;

  /**
   * PalmExplanation of the last upper bound affectation.
   */
  protected Explanation explanationOnSup;

  /**
   * Creates an abstract binary term with the two sub-expressions.
   * @param pb
   * @param exp1
   */
  public AbstractPalmRealUnTerm(Solver pb, RealExp exp1) {
    super(pb, exp1);
    explanationOnInf = ((PalmSolver) pb).makeExplanation();
    explanationOnSup = ((PalmSolver) pb).makeExplanation();
  }

  /**
   * Updates the interval such that this interval is included in the interval parameter.
   *
   * @param interval The interval this expression should be included in.
   * @param index    The index of the constraint responsible of this reduction.
   * @throws ContradictionException
   */
  public void intersect(RealInterval interval, int index) throws ContradictionException {
    if (interval.getInf() > inf.get()) {
      inf.set(interval.getInf());
      explanationOnInf.empties();
      ((PalmRealInterval) interval).self_explain(PalmRealInterval.INF, explanationOnInf);
    }
    if (interval.getSup() < sup.get()) {
      sup.set(interval.getSup());
      explanationOnSup.empties();
      ((PalmRealInterval) interval).self_explain(PalmRealInterval.SUP, explanationOnSup);
    }
    if (inf.get() > sup.get()) {
      Explanation e = ((PalmSolver) this.getSolver()).makeExplanation();
      this.self_explain(PalmRealInterval.DOM, e);
      ((ExplainedSolver) this.getSolver()).explainedFail(e);
    }
  }

  /**
   * Explains the state of this expression (lower/upper bounds or all domain)
   *
   * @param select Specifies which part of the domain should be explained.
   * @param e      Specifies the explanation in which these explaining constraints should be added.
   */
  public void self_explain(int select, ConstraintCollection e) {
    switch (select) {
      case PalmRealInterval.INF:
        e.merge(this.explanationOnInf);
        break;
      case PalmRealInterval.SUP:
        e.merge(this.explanationOnSup);
        break;
      case PalmRealInterval.DOM:
        e.merge(this.explanationOnInf);
        e.merge(this.explanationOnSup);
        break;
    }
  }
}
