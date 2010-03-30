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

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.real.AbstractLargeRealSConstraint;
import choco.kernel.solver.constraints.real.RealExp;
import choco.kernel.solver.variables.real.RealInterval;
import choco.kernel.solver.variables.real.RealIntervalConstant;
import choco.kernel.solver.variables.real.RealMath;
import choco.kernel.solver.variables.real.RealVar;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * A basic constraint using HC4 algorithm for filtering values with respect to a mathematical equation.
 */
public final class Equation extends AbstractLargeRealSConstraint {
  private RealInterval cste;
  private RealExp exp;
  private RealExp[] subExps;

  private int nbBoxedVars = 0;
  private RealVar[] boxedVars;
  private RealExp[][] subExpsWX;
  private RealExp[][] subExpsWOX;
  private static final int boxConsistencyDepth = 6;

  private final Solver solver;

  public Equation(final Solver solver, final RealVar[] collectedVars, final RealExp exp, final RealInterval cste) {
    super(collectedVars);
    initEquation(exp, cste);
      this.solver = solver;
  }

  public Equation(final Solver solver, final RealVar[] collectedVars, final RealExp exp) {
    this(solver, collectedVars, exp, new RealIntervalConstant(0.0, 0.0));
  }

  public String pretty() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Equation ").append(exp.pretty()).append(" = ").append(cste.pretty());
    return sb.toString();
  }

//  public Object clone() throws CloneNotSupportedException {
//    Equation newc = (Equation) super.clone();
//    newc.initEquation(this.solver, this.exp, this.cste);
//    return newc;
//  }

  void initEquation(final RealExp exp, final RealInterval cste) {
    this.cste = cste;
    this.exp = exp;
    boxedVars = new RealVar[vars.length];
    subExpsWX = new RealExp[vars.length][];
    subExpsWOX = new RealExp[vars.length][];

    // Collect the subexpressions
    final List<RealExp> collectedSubExp = new ArrayList<RealExp>();
    exp.subExps(collectedSubExp);
    subExps = new RealExp[collectedSubExp.size()];
    subExps = collectedSubExp.toArray(subExps);
  }

  public void addBoxedVar(final RealVar var) {
    if (nbBoxedVars == boxedVars.length) {
      LOGGER.log(Level.SEVERE, "Cannot box more variables than variables involved in the constraint !!");
      return;
    }
    final List<RealExp> wx = new ArrayList<RealExp>();
    final List<RealExp> wox = new ArrayList<RealExp>();
    this.exp.isolate(var, wx, wox);
    if (wx.isEmpty()) {
      LOGGER.log(Level.SEVERE, "Cannot box variables not involved in the constraint !!");
      return;
    }
    boxedVars[nbBoxedVars] = var;
    subExpsWX[nbBoxedVars] = wx.toArray(new RealExp[wx.size()]);
    subExpsWOX[nbBoxedVars] = wox.toArray(new RealExp[wox.size()]);
    nbBoxedVars++;
  }

  public void boxAllVars() {
      for (final RealVar var : vars) {
          this.addBoxedVar(var);
      }
  }

  // ==== Propag ====

  public void propagate() throws ContradictionException {
    // Hull consitency: HC4
    this.tighten(subExps);
    this.proj();

    // Box consistency
    for (int i = 0; i < nbBoxedVars; i++) {
      bc(boxedVars[i], subExpsWX[i], subExpsWOX[i]);
    }
  }

  private boolean not_inconsistent(final RealExp[] wx) {
    boolean contradiction = false;
    try {
      tighten(wx);
    } catch (ContradictionException e) {
      contradiction = true;
    }
      return !contradiction && (this.exp.getInf() <= this.cste.getSup() && this.exp.getSup() >= this.cste.getInf());
  }

  void bc(final RealVar var, final RealExp[] wx, final RealExp[] wox) throws ContradictionException {
    RealInterval[] unexplored = new RealInterval[this.boxConsistencyDepth * 2];
    int[] depths = new int[this.boxConsistencyDepth * 2];
    int depth = 0;
    int idx = 0;
    boolean fin = false;

    double leftB = 0, rightB = 0;
    final RealInterval oldValue = new RealIntervalConstant(var);

    tighten(wox);

    // Left bound !
    while (!fin) {
      if (not_inconsistent(wx)) {
        if (this.boxConsistencyDepth <= depth) {
          leftB = var.getInf();
          rightB = var.getSup(); // Valeur provisoire
          fin = true;
        } else {
          final RealInterval left = RealMath.firstHalf(var);
          final RealInterval right = RealMath.secondHalf(var);

          var.silentlyAssign(left);
          depth++;
          unexplored[idx] = right;
          depths[idx] = depth;
          idx++;
        }
      } else if (idx != 0) {
        var.silentlyAssign(unexplored[--idx]);
        depth = depths[idx];
      } else {
        propagationEngine.raiseContradiction(this);
      }
    }

    // Reversing not explored intervals (in order to avoid to check already checked parts of the search space.

    final RealInterval[] tmp1 = new RealInterval[this.boxConsistencyDepth * 2];
    final int[] tmp2 = new int[this.boxConsistencyDepth * 2];

    for (int i = 0; i < idx; i++) {
      final int j = idx - i - 1;
      tmp1[i] = unexplored[j];
      tmp2[i] = depths[j];
    }

    unexplored = tmp1;
    depths = tmp2;

    // Right bound if needed
    if (idx != 0) {
      var.silentlyAssign(unexplored[--idx]);
      depth = depths[idx];
      fin = false;

      while (!fin) {
        if (not_inconsistent(wx)) {
          if (this.boxConsistencyDepth <= depth) {
            rightB = var.getSup();
            fin = true;
          } else {
            final RealInterval left = RealMath.firstHalf(var);
            final RealInterval right = RealMath.secondHalf(var);

            var.silentlyAssign(right);
            depth++;
            unexplored[idx] = left;
            depths[idx] = depth;
            idx++;
          }
        } else if (idx != 0) {
          var.silentlyAssign(unexplored[--idx]);
          depth = depths[idx];
        } else {
          fin = true;
        }
      }
    }

    // Propagation
    var.silentlyAssign(oldValue);
    var.intersect(new RealIntervalConstant(leftB, rightB));
  }


  // ==== Variable Management ====


  // ==== Constraint properties ====

public boolean isSatisfied() {
    boolean ok1 = true;
    solver.worldPushDuringPropagation();
    try{
        solver.propagate();
    }catch (ContradictionException e){
        ok1 = false;
    }
    solver.worldPopDuringPropagation();
    return ok1;
  }

  public boolean isConsistent() {
    return false;
  }


  void tighten(final RealExp[] exps) throws ContradictionException {
    for (int i = 0; i < exps.length; i++) {
      final RealExp exp = exps[i];
      exp.tighten();
      if (exp.getInf() > exp.getSup())
        this.fail();
    }
  }

  void proj() throws ContradictionException {
    subExps[subExps.length - 1].intersect(cste);
    int i = subExps.length - 1;
    while (i > 0) {
      subExps[i].project();
      i--;
    }
  }
}
