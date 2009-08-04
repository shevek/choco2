package choco.ecp.solver.search.cbj;

import choco.cp.solver.variables.integer.IntDomainVarImpl;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.explanations.dbt.JumpExplanation;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.search.AbstractGlobalSearchLimit;

import java.util.logging.Level;

// **************************************************
// *                   J-CHOCO                      *
// *   Copyright (C) F. Laburthe, 1999-2003         *
// **************************************************
// *  an open-source Constraint Programming Kernel  *
// *     for Research and Education                 *
// **************************************************

public class JumpRestartOptimizer extends JumpAbstractOptimizer {

  /**
   * counting the number of iterations
   */
  protected int nbIter = 0;

  /**
   * counting the overall number of solutions
   */
  protected int baseNbSol = 0;

  /**
   * total nb of backtracks (all trees in the optimization process)
   */
  protected int nbBkTot = 0;

  /**
   * total nb of nodes expanded in all trees
   */
  protected int nbNdTot = 0;

  public JumpRestartOptimizer(IntDomainVarImpl obj, boolean maximize) {
    super(obj, maximize);
  }

  // should we call a fullReset on limits ? (to reset cumulated counter?)
  private void newLoop() throws ContradictionException {
    nbIter = nbIter + 1;
    baseNbSol = nbSolutions;
    postTargetBound();
    problem.propagate();
  }

  private void endLoop() {
    for (int i = 0; i < limits.size(); i++) {
      AbstractGlobalSearchLimit lim = (AbstractGlobalSearchLimit) limits.get(i);
      lim.reset(false);
    }
    popTraceUntil(baseWorld + 1);
    problem.worldPopUntil(baseWorld + 1);
  }

  /**
   * loop until the lower bound equals the upper bound
   *
   * @return
   */
  private boolean oneMoreLoop() {
    return (lowerBound < upperBound);
  }

  public void incrementalRun() {
    initBounds();
    super.incrementalRun();
  }

  public Boolean nextSolution() {
    Boolean bool;
    if (oneMoreLoop() == false) return Boolean.FALSE;
    try {
      newLoop();
      nextMove = INIT_SEARCH;
      bool = super.nextSolution();
    } catch (ContradictionException e) {
      bool = Boolean.FALSE;
    }
    endLoop();
    return bool;
  }

  public void newTreeSearch() throws ContradictionException {
    super.newTreeSearch();
    nbIter = nbIter + 1;
    baseNbSol = nbSolutions;
    postNewBounds();
    problem.propagate();
  }

  public void postNewBounds() throws ContradictionException {
    if (doMaximize) {
      Explanation expl = new JumpExplanation(this.problem);
      objective.updateInf(targetLowerBound, -1, expl);
    } else {
      Explanation expl = new JumpExplanation(this.problem);
      objective.updateSup(targetUpperBound, -1, expl);
    }
  }

  /**
   * called before a new search tree is explored
   */
  public void endTreeSearch() {
    for (int i = 0; i < limits.size(); i++) {
      AbstractGlobalSearchLimit lim = (AbstractGlobalSearchLimit) limits.get(i);
      lim.reset(false);
    }
    if (logger.isLoggable(Level.SEVERE)) {
      if (problem.feasible == Boolean.TRUE) {
        logger.log(Level.INFO, "solve => " + new Integer(nbSolutions) + " solutions");
      } else {
        logger.info("solve => no solution");
      }
      for (int i = 0; i < limits.size(); i++) {
        AbstractGlobalSearchLimit lim = (AbstractGlobalSearchLimit) limits.get(i);
        logger.info(lim.pretty());
      }
    }
    popTraceUntil(baseWorld);
    problem.worldPopUntil(baseWorld);
  }

}
