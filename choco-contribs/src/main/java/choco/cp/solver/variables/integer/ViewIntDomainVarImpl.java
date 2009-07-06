/* ************************************************
 *           _       _                            *
 *          |  °(..)  |                           *
 *          |_  J||L _|        CHOCO solver       *
 *                                                *
 *     Choco is a java library for constraint     *
 *     satisfaction problems (CSP), constraint    *
 *     programming (CP) and explanation-based     *
 *     constraint solving (e-CP). It is built     *
 *     on a event-based propagation mechanism     *
 *     with backtrackable structures.             *
 *                                                *
 *     Choco is an open-source software,          *
 *     distributed under a BSD licence            *
 *     and hosted by sourceforge.net              *
 *                                                *
 *     + website : http://choco.emn.fr            *
 *     + support : choco@emn.fr                   *
 *                                                *
 *     Copyright (C) F. Laburthe,                 *
 *                   N. Jussien    1999-2008      *
 **************************************************/
package choco.cp.solver.variables.integer;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomain;
import choco.kernel.solver.variables.integer.IntDomainVar;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 10 mars 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public class ViewIntDomainVarImpl extends IntDomainVarImpl{

    private final int coefficient, constant;

    /**
     * Constructs a new variable for the specified model and with the
     * specified name and bounds.
     *
     * @param solver     The model of the variable.
     * @param name       Its name.
     * @param domainType the type of encoding for the domain (BOUNDS, BITSET, ...)
     * @param a          Its minimal value.
     * @param b          Its maximal value.
     */
    public ViewIntDomainVarImpl(Solver solver, String name, int domainType, int a, int b, int coeff, int cst) {
        super(solver, name, domainType, a, b);
        coefficient = coeff;
        constant = cst;
    }

    public ViewIntDomainVarImpl(Solver solver, String name, int domainType, int[] sortedValues, int coeff, int cst) {
        super(solver, name, domainType, sortedValues);
        coefficient = coeff;
        constant = cst;
    }


  // ============================================
  // Methods of the interface
  // ============================================

  /**
   * Checks if the variable is instantiated to a specific value.
   */

  public boolean isInstantiatedTo(int x) {
    return isInstantiated() && (getVal() == x);
  }


  /**
   * Checks if the variables is instantiated to any value.
   */

  public boolean isInstantiated() {
      return domain.getSize() == 1;//(value.isKnown());
  }


  /**
   * Checks if a value is still in the domain.
   */

  public boolean canBeInstantiatedTo(int x) {
    //return domain.contains(x);
    return (getInf() <= x && x <= getSup() && (domain == null || domain.contains(x/coefficient-constant)));
  }

    /**
     * Checks if a value is still in the domain assuming the value is
     * in the initial bound of the domain
     */
    public boolean fastCanBeInstantiatedTo(int x) {
        return domain.contains(x/coefficient-constant);
    }

  

  public boolean hasEnumeratedDomain() {
    return domain.isEnumerated();
  }

  public boolean hasBooleanDomain() {
    return domain.isBoolean();
  }

  public IntDomain getDomain() {
    return domain;
  }

  /**
   * Gets the domain size.
   */

  public int getDomainSize() {
    return domain.getSize();
  }

  /**
   * Checks if it can be equals to another variable.
   */

  public boolean canBeEqualTo(IntDomainVar x) {
    if (x.getInf() <= this.getSup()) {
      if (this.getInf() <= x.getSup()) {
        if (!this.hasEnumeratedDomain() || !x.hasEnumeratedDomain())
          return true;
        else {
            DisposableIntIterator it = this.getDomain().getIterator();
          for (; it.hasNext();) {
            int v = it.next();
            if (x.canBeInstantiatedTo(v)){
                it.dispose();
                return true;
            }
          }
            it.dispose();
          return false;
        }
      } else
        return false;
    } else
      return false;
  }


  /**
   * Checks if the variables can be instantiated to at least one value
   * in the array.
   *
   * @param sortedValList The value array.
   * @param nVals         The number of interesting value in this array.
   */

  public boolean canBeInstantiatedIn(int[] sortedValList, int nVals) {
    if (getInf() <= sortedValList[nVals - 1]) {
      if (getSup() >= sortedValList[0]) {
        if (domain == null)
          return true;
        else {
          for (int i = 0; i < nVals; i++) {
            if (canBeInstantiatedTo(sortedValList[i]))
              return true;
          }
          return false;
        }
      } else
        return false;
    } else
      return false;
  }


  /**
   * Returns a randomly choosed value in the domain.
   * <p/>
   * Not implemented yet.
   */

  public int getRandomDomainValue() {
    if (domain == null)
      return getInf();
// TODO     return inf.get() + random(sup.get() - inf.get() + 1);
    else
      return constant + coefficient*domain.getRandomValue();
  }


  /**
   * Gets the next value in the domain.
   */

  public int getNextDomainValue(int currentv) {
    if (currentv/coefficient-constant < getInf())
      return getInf();
    else if (domain == null)
      return currentv + 1;
    else
      return constant + coefficient*domain.getNextValue(currentv);
  }


  /**
   * Gets the previous value in the domain.
   */

  public int getPrevDomainValue(int currentv) {
    if (currentv/coefficient-constant > getSup())
      return getSup();
    else if (domain == null)
      return currentv - 1;
    else
      return constant + coefficient*domain.getPrevValue(currentv);
  }



    private int computeValue(int x, boolean floor){
        return (x- constant)/coefficient + (floor?0:1);
    }

  /**
   * Internal var: update on the variable lower bound caused by its i-th
   * constraint.
   * Returns a boolean indicating whether the call indeed added new information
   *
   * @param x   The new lower bound.
   * @param idx The index of the constraint (among all constraints linked to
   *            the variable) responsible for the update.
   */

  public boolean updateInf(int x, int idx) throws ContradictionException {
          //logOnInf(x);
          return domain.updateInf(computeValue(x, false), idx);
  }

  /**
   * Internal var: update on the variable upper bound caused by its i-th
   * constraint.
   * Returns a boolean indicating whether the call indeed added new information.
   *
   * @param x   The new upper bound
   * @param idx The index of the constraint (among all constraints linked to
   *            the variable) responsible for the update
   */

  public boolean updateSup(int x, int idx) throws ContradictionException {
          //logOnSup(x);
          return domain.updateSup(computeValue(x, true), idx);
  }


  /**
   * Internal var: update (value removal) on the domain of a variable caused by
   * its i-th constraint.
   * <i>Note:</i> Whenever the hole results in a stronger var (such as a bound update or
   * an instantiation, then we forget about the index of the var generating constraint.
   * Indeed the propagated var is stronger than the initial one that
   * was generated; thus the generating constraint should be informed
   * about such a new var.
   * Returns a boolean indicating whether the call indeed added new information.
   *
   * @param x   The removed value
   * @param idx The index of the constraint (among all constraints linked to the variable) responsible for the update
   */

  public boolean removeVal(int x, int idx) throws ContradictionException {
      //logOnRem(x);
      return domain.removeVal(computeValue(x, true), idx);
  }

  /**
   * Internal var: remove an interval (a sequence of consecutive values) from
   * the domain of a variable caused by its i-th constraint.
   * Returns a boolean indicating whether the call indeed added new information.
   *
   * @param a   the first removed value
   * @param b   the last removed value
   * @param idx the index of the constraint (among all constraints linked to the variable)
   *            responsible for the update
   */

  public boolean removeInterval(int a, int b, int idx) throws ContradictionException {
      //logOnRemInt(a, b);
	  return domain.removeInterval(computeValue(a, false), computeValue(b, true), idx);
  }

  /**
   * Internal var: instantiation of the variable caused by its i-th constraint
   * Returns a boolean indicating whether the call indeed added new information.
   *
   * @param x   the new upper bound
   * @param idx the index of the constraint (among all constraints linked to the
   *            variable) responsible for the update
   */

  public boolean instantiate(int x, int idx) throws ContradictionException {
     //logOnInst(x);
      return domain.instantiate(computeValue(x, false), idx);
  }



  /**
   * Gets the minimal value of the variable.
   */

  public int getInf() {
    return coefficient*domain.getInf()+constant;
  }


  /**
   * Gets the maximal value of the variable.
   */

  public int getSup() {
    return coefficient*domain.getSup()+constant;
  }


  /**
   * Gets the value of the variable if instantiated.
   */

  public int getVal() {
    return getInf();
  }

  /**
   * pretty printing
   *
   * @return a String representation of the variable
   */
  public String toString() {
    return (super.toString() + ":" + (isInstantiated() ? getVal() : "?"));
  }

  /**
   * pretty printing
   *
   * @return a String representation of the variable
   */
  public String pretty() {
    return (this.toString() + "[" + this.domain.getSize() + "]" + this.domain.pretty());
  }


}
