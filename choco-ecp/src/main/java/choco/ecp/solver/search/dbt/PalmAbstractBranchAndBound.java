package choco.ecp.solver.search.dbt;


import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.real.RealVar;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * J-CHOCO
 * Copyright (C) F. Laburthe, 1999-2003
 * <p/>
 * An open-source Constraint Programming Kernel
 * for Research and Education
 * <p/>
 * Created by: Guillaume on 20 juil. 2004
 */

public abstract class PalmAbstractBranchAndBound extends PalmGlobalSearchStrategy {
  /**
   * States if the solver is maximizing (or minimizing).
   */

  protected boolean maximizing = false;

  /**
   * The variable that should be maximized (or minimized).
   */

  protected Var objective;

  /**
   * Dynamic cut constraints.
   */

  protected LinkedList dynamicCuts;


  public PalmAbstractBranchAndBound(Solver pb, Var obj, boolean max) {
    super(pb);
    objective = obj;
    maximizing = max;
    dynamicCuts = new LinkedList();
  }

  public void incrementalRun() {
    try {
      //problem.post(problem.geq(objective,lowerBound));
      //problem.post(problem.leq(objective,upperBound));
      if (Logger.getLogger("choco").isLoggable(Level.INFO))
        Logger.getLogger("choco").info("Initial Propagation");
      solver.propagate();
    } catch (ContradictionException e) {
      if (Logger.getLogger("choco").isLoggable(Level.INFO))
        Logger.getLogger("choco").info("Optimality proven");
      this.finished = true;
      this.solver.setFeasible(Boolean.FALSE);
    }
    this.solver.setFeasible(Boolean.TRUE);
    while (this.solver.isFeasible().booleanValue()) {
      if (Logger.getLogger("choco").isLoggable(Level.INFO))
        Logger.getLogger("choco").info("Searching for one solution");
      runonce();
      recordSolution();
      if (this.solver.isFeasible().booleanValue()) {
        LOGGER.info("... solution with cost " + objective + ":" +
            (objective instanceof RealVar ? "" + ((RealVar) objective).getValue() : "") +
            (objective instanceof IntDomainVar ? "" + ((IntDomainVar) objective).getVal() : ""));
        postDynamicCut();
      }
    }
  }

  /**
   * solving the feasibility problem
   * same code as in run(PalmGlobalSearchSolver) in order to be able
   * to store consecutive solutions ...
   */
  public void runonce() {
    //long time = System.currentTimeMillis();
    try {
      while (!this.finished) {
        try {
          if (Logger.getLogger("choco").isLoggable(Level.FINE))
            Logger.getLogger("choco").fine("New extension launched.");
          this.extend();
          this.solver.propagate();
        } catch (PalmContradiction e) {
          this.repair();
        }
      }
      this.solver.setFeasible(Boolean.TRUE);
    } catch (ContradictionException e) {
      this.finished = true;
      this.solver.setFeasible(Boolean.FALSE);
    }
    //this.setRuntimeStatistic(PalmSolver.CPU, (int) (System.currentTimeMillis() - time));
  }

  public void postDynamicCut() {
    SConstraint cut;
    try {
      reset();
      cut = getDynamicCut();
      dynamicCuts.add(cut);
      solver.post(cut);
      try {
        solver.propagate();
      } catch (PalmContradiction e) {
        repair();
      }
    } catch (ContradictionException e) {
      this.finished = true;
      this.solver.setFeasible(Boolean.FALSE);
    }
  }

  public abstract SConstraint getDynamicCut();

  public abstract Number getOptimumValue();
}
