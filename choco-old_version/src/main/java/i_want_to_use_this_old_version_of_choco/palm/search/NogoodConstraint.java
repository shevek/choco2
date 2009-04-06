package i_want_to_use_this_old_version_of_choco.palm.search;

import i_want_to_use_this_old_version_of_choco.ConstraintCollection;
import i_want_to_use_this_old_version_of_choco.ContradictionException;
import i_want_to_use_this_old_version_of_choco.integer.IntDomainVar;
import i_want_to_use_this_old_version_of_choco.palm.ExplainedConstraintPlugin;
import i_want_to_use_this_old_version_of_choco.palm.ExplainedProblem;
import i_want_to_use_this_old_version_of_choco.palm.Explanation;
import i_want_to_use_this_old_version_of_choco.palm.dbt.explain.PalmExplanation;
import i_want_to_use_this_old_version_of_choco.palm.dbt.integer.PalmIntDomain;
import i_want_to_use_this_old_version_of_choco.palm.integer.AbstractPalmLargeIntConstraint;
import i_want_to_use_this_old_version_of_choco.palm.integer.ExplainedIntVar;
import i_want_to_use_this_old_version_of_choco.palm.integer.constraints.PalmAssignment;
import i_want_to_use_this_old_version_of_choco.util.IntIterator;

import java.util.*;

// **************************************************
// *                   J-CHOCO                      *
// *   Copyright (C) F. Laburthe, 1999-2003         *
// **************************************************
// *  an open-source Constraint Programming Kernel  *
// *     for Research and Education                 *
// **************************************************

public class NogoodConstraint extends AbstractPalmLargeIntConstraint {

  /**
   * The list of nogood. A nogood is stored as a set of decision constraints.
   */
  protected LinkedList nogoods;           // a list of AbstractDecision[]
  protected LinkedList permanentMemory;   // a list of solutions (AbstractDecision[])

  private Hashtable indices;

  public NogoodConstraint(IntDomainVar[] vs) {
    super(vs);
    nogoods = new LinkedList();
    permanentMemory = new LinkedList();
    indices = new Hashtable();
    for (int i = 0; i < vs.length; i++) {
      indices.put(vs[i], new Integer(i));
    }
    this.hook = ((ExplainedProblem) this.getProblem()).makeConstraintPlugin(this);
  }

  public LinkedList getMemory() {
    return nogoods;
  }

  public int getPermanentMemorySize() {
    return permanentMemory.size();
  }

  public void addPermanentNogood(ConstraintCollection exp) {
    addFirst((PalmExplanation) exp, permanentMemory);
  }

  public void addPermanentNogood(SymbolicDecision[] exp) {
    Arrays.sort(exp);
    if (checkAddition(exp, permanentMemory)) {
      permanentMemory.addFirst(exp);
    }
  }

  public void addNogoodFirst(ConstraintCollection exp) {
    addFirst((PalmExplanation) exp, nogoods);
  }

  public void addFirst(PalmExplanation exp, LinkedList mem) {
    BitSet bset = ((PalmExplanation) exp).getBitSet();
    SymbolicDecision[] nog = new SymbolicDecision[bset.cardinality()];
    int cpt = 0;
    for (int i = bset.nextSetBit(0); i >= 0; i = bset.nextSetBit(i + 1)) {
      //DecisionConstraint dc = (DecisionConstraint) ((ExplainedProblem) this.getProblem()).getConstraintNb(i);
      nog[cpt] = (SymbolicDecision) exp.getConstraint(i); //new Assignment((ExplainedIntVar) dc.getVar(0), dc.getBranch());
      cpt++;
    }
    //if (!contains(nog,mem))
    Arrays.sort(nog);
    mem.addFirst(nog);
  }

  public void removeNogood(ConstraintCollection exp) {
    nogoods.remove(exp);
  }

  public void removeLastNogood() {
    nogoods.removeLast();
  }

  public void propagate() throws ContradictionException {
    filter();
  }

  public void filter() throws ContradictionException {
    if (!nogoods.isEmpty()) filterMem(nogoods);
    if (!permanentMemory.isEmpty()) filterMem(permanentMemory);
  }

  public void filterMem(LinkedList memoryToFilter) throws ContradictionException {
    Iterator it = memoryToFilter.iterator();
    while (it.hasNext()) {
      SymbolicDecision[] nog = ((SymbolicDecision[]) it.next());
      int nbDecInvalid = 0;
      int numDec = -1;
      for (int i = 0; i < nog.length; i++) {
        if (!nog[i].isSatisfied()) {
          nbDecInvalid += 1;
          numDec = i;
        }
        if (nbDecInvalid > 1) break;
      }
      if (nbDecInvalid == 1) filterLastDecision(nog, numDec);    // filtre sur la derni�re d�cision non instanci�e
      if (nbDecInvalid == 0) filterLastDecision(nog, -1);        // Le nogood est valide, renvoie une contradiction
    }

  }

  // Pour l'instant la contrainte ne fonctionne qu'avec des d�cisions d'instanciations
  public void filterLastDecision(SymbolicDecision[] nogood, int numDec) throws ContradictionException {
    Explanation e = ((ExplainedProblem) this.getProblem()).makeExplanation();
    ((ExplainedConstraintPlugin) this.hook).self_explain(e);
    for (int i = 0; i < nogood.length; i++) {
      if (i != numDec) {
        if (nogood[i] instanceof Assignment || nogood[i] instanceof PalmAssignment)
          ((ExplainedIntVar) nogood[i].getVar(0)).self_explain(PalmIntDomain.DOM, e);
        else {
          throw new UnsupportedOperationException(nogood[i] + "is not yet supported by the nogoodConstraint");
        }
      }
    }
    if (numDec != -1) {
      if (nogood[numDec] instanceof Assignment || nogood[numDec] instanceof PalmAssignment) {
        ExplainedIntVar v = (ExplainedIntVar) nogood[numDec].getVar(0);
        int value = nogood[numDec].getBranch();
        v.removeVal(value, cIndices[((Integer) indices.get(v)).intValue()], e);
      } else {
        throw new UnsupportedOperationException(nogood[numDec] + "is not yet supported by the nogoodConstraint");
      }
    } else {
      ((ExplainedProblem) this.getProblem()).explainedFail(e);
    }
  }

  public void awakeOnRestoreVal(int idx, int i) throws ContradictionException {
    constAwake(false);
  }

  public void awakeOnRestoreSup(int index) throws ContradictionException {
    constAwake(false);
  }

  public void awakeOnRestoreInf(int index) throws ContradictionException {
    constAwake(false);
  }

  public void awakeOnRemovals(int idx, IntIterator deltaDomain) throws ContradictionException {
    constAwake(false);
  }

  public void awakeOnBounds(int varIndex) throws ContradictionException {
    constAwake(false);
  }

  public boolean isSatisfied() {
    throw new Error("isSatisfied is not yet implemented in NogoodConstraint");
  }

  public Set whyIsTrue() {
    throw new Error("whyIsTrue is not yet implemented in NogoodConstraint");
  }

  public Set whyIsFalse() {
    throw new Error("whyIsFalse is not yet implemented in NogoodConstraint");
  }


  public boolean checkAddition(SymbolicDecision[] cut1, LinkedList memo) {
    boolean ctp = false;
    for (Iterator iterator = memo.iterator(); iterator.hasNext();) {
      SymbolicDecision[] cut2 = (SymbolicDecision[]) iterator.next();
      int res = checkCut(cut1, cut2);
      if (res == 1) {
        if (ctp) throw new Error("Nogood managment error in NogoodConstraint");
        return false;
      } else if (res == 2) {
        ctp = true;
        iterator.remove();
        // retirer cut2 de la liste des coupes et ajouter cut
      } else if (res == 0) {
        if (ctp) throw new Error("Nogood managment error in NogoodConstraint");
        return false;
      }
    }
    return true;
  }

  /**
   * check whether cut1 contains cut2 or cut2 contains cut1
   *
   * @return 1 if cut1 contains cut2
   *         2 if cut2 contains cut1
   *         0 if cut1 is equal to cut2
   *         -1 otherwise
   */
  public int checkCut(SymbolicDecision[] cut1, SymbolicDecision[] cut2) {
    if (cut1.length > cut2.length) {  // res != 2
      for (int i = 0; i < cut2.length; i++) {
        int idx = Arrays.binarySearch(cut1, cut2[i]);
        if (idx < 0) return -1;      //
      }
      return 1; // cut1 contains cut2
    } else {
      for (int i = 0; i < cut1.length; i++) {
        int idx = Arrays.binarySearch(cut2, cut1[i]);
        if (idx < 0) return -1;      //
      }
      if (cut1.length == cut2.length)
        return 0;  // cut2 is equal to cut1
      else
        return 2; // cut2 contains cut1
    }
  }

}
