package i_want_to_use_this_old_version_of_choco.real.search;

import i_want_to_use_this_old_version_of_choco.ContradictionException;
import i_want_to_use_this_old_version_of_choco.real.RealMath;
import i_want_to_use_this_old_version_of_choco.real.RealVar;
import i_want_to_use_this_old_version_of_choco.search.AbstractGlobalSearchLimit;

/**
 * J-CHOCO
 * Copyright (C) F. Laburthe, 1999-2003
 * <p/>
 * An open-source Constraint Programming Kernel
 * for Research and Education
 * <p/>
 * Created by: Guillaume on 20 juil. 2004
 */
public class RealOptimizeWithRestarts extends AbstractRealOptimize {
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

  public RealOptimizeWithRestarts(RealVar obj, boolean maximize) {
    super(obj, maximize);
  }

  public void newTreeSearch() throws ContradictionException {
    super.newTreeSearch();
    nbIter = nbIter + 1;
    baseNbSol = nbSolutions;
    postTargetBound();
    problem.propagate();
  }

  /**
   * called before a new search tree is explored
   */
  public void endTreeSearch() {
    for (int i = 0; i < limits.size(); i++) {
      AbstractGlobalSearchLimit lim = (AbstractGlobalSearchLimit) limits.get(i);
      lim.reset(false);
    }
    popTraceUntil(baseWorld);
    problem.worldPopUntil(baseWorld);
  }

  // should we call a fullReset on limits ? (to reset cumulated counter?)
  private void newLoop() {
    initBounds();
    // time_set()
  }

  private void endLoop() {
/*
    -> let t := time_get() in
       trace(SVIEW,"Optimisation over => ~A(~A) = ~S found in ~S iterations, [~S], ~S ms.\n",
              (if a.doMaximize "max" else "min"),a.objective.name,
              getBestObjectiveValue(a),  // v1.013 using the accessor
              a.nbIter,a.limits,t)]
*/
  }

  private void recordNoSolution() {
    // (trace(SVIEW,"... no solution with ~A:~S [~S]\n",obj.name,objtgt,a.limits),
    if (doMaximize) {
      upperBound = Math.min(upperBound, RealMath.prevFloat(getObjectiveTarget()));
    } else {
      lowerBound = Math.max(lowerBound, RealMath.nextFloat(getObjectiveTarget()));
    }
  }

  /**
   * loop until the lower bound equals the upper bound
   *
   * @return true if one more loop is needed
   */
  private boolean oneMoreLoop() {
    return (lowerBound < upperBound);
  }

  /*
   * @deprecated replaced by the incrementalRun
   */
  /*public void run() {
    int w = problem.getWorldIndex() + 1;
    AbstractProblem pb = problem;
    boolean finished = false;
    newLoop();
    try {
      pb.propagate();
    } catch (ContradictionException e) {
      finished = true;
      recordNoSolution();
    }
    if (!finished) {
      pb.worldPush();
      while (oneMoreLoop()) {
        boolean foundSolution = false;
        try {
          newTreeSearch();
          if (mainGoal.explore(1)) {
            foundSolution = true;
          }
        } catch (ContradictionException e) {
          logger.fine("boff :(");
        }
        endTreeSearch();
        if (!foundSolution) {
          recordNoSolution();
        }
      }
      assert(problem.getWorldIndex() == w);
      problem.worldPop();
    }
    endLoop();
    if ((maxNbSolutionStored > 0) && existsSolution()) {
      restoreBestSolution();
    }
    for (int i = 0; i < limits.size(); i++) {
      AbstractGlobalSearchLimit lim = (AbstractGlobalSearchLimit) limits.get(i);
      if (logger.isLoggable(Level.SEVERE))
        logger.severe(lim.pretty());
    }
  } */
}
