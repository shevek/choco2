/**
 * 
 */
package choco.kernel.solver.variables.scheduling;

import choco.kernel.common.IIndex;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.structure.APartiallyStoredCstrList;
import choco.kernel.memory.structure.Couple;
import choco.kernel.memory.structure.PartiallyStoredIntVector;
import choco.kernel.memory.structure.PartiallyStoredTaskCstrList;
import choco.kernel.memory.structure.PartiallyStoredVector;
import choco.kernel.model.variables.scheduling.ITaskVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.Extension;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.PropagationEngine;
import choco.kernel.solver.propagation.event.TaskVarEvent;
import choco.kernel.solver.propagation.event.VarEvent;
import choco.kernel.solver.propagation.listener.TaskPropagator;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * @author Arnaud Malapert</br> 
 * @since 23 janv. 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public final class TaskVar<C extends AbstractSConstraint & TaskPropagator> extends AbstractTask implements Var, ITaskVariable<IntDomainVar>, IIndex {

	protected final IntDomainVar start;

	protected final IntDomainVar end;

	protected final IntDomainVar duration;

    private long index;
	/**
	 * The list of constraints (listeners) observing the variable.
	 */
	protected APartiallyStoredCstrList<C> constraints;

    protected final VarEvent<? extends Var> event;

    private final PropagationEngine propagationEngine;
	
	/**
	 * Initializes a new variable.
	 * @param solver The model this variable belongs to
	 * @param name The name of the variable
	 */
	public TaskVar(final Solver solver, final int id, final String name, final IntDomainVar start, final IntDomainVar end, final IntDomainVar duration) {
		super(id, name);
		this.start = start;
		this.end = end;
		this.duration = duration;
		constraints = new PartiallyStoredTaskCstrList<C>(solver.getEnvironment());
        index = solver.getIndexfactory().getIndex();
        this.event = new TaskVarEvent<C>(this);
        this.propagationEngine = solver.getPropagationEngine();
	}


    /**
     * Unique index of an object in the master object
     * (Different from hashCode, can change from one execution to another one)
     *
     * @return
     */
    @Override
    public long getIndex() {
        return index;
    }

    //*****************************************************************//
	//*******************  TaskVariable  ********************************//
	//***************************************************************//
	
	public final IntDomainVar start() {
		return start;
	}

	public final IntDomainVar end() {
		return end;
	}

	public final IntDomainVar duration() {
		return duration;
	}
	
	//*****************************************************************//
	//*******************  ITask  ********************************//
	//***************************************************************//
	


	
	public int getECT() {
		return end.getInf();
	}

	public int getEST() {
		return start.getInf();
	}

	public int getLCT() {
		return end.getSup();
	}

	public int getLST() {
		return start.getSup();
	}

	public int getMaxDuration() {
		return duration.getSup();
	}

	public int getMinDuration() {
		return duration.getInf();
	}

	public boolean hasConstantDuration() {
		return duration.isInstantiated();
	}

	@Override
	public boolean isScheduled() {
		return isInstantiated();
	}
	
	
	//*****************************************************************//
	//*******************  Var  ********************************//
	//***************************************************************//
	/**
	 * Returns the variable event.
	 * @return the event responsible for propagating variable modifications
	 */
	public VarEvent<? extends Var> getEvent() {
		return event;
	}


	/**
	 * Retrieve the constraint i involving the variable.
	 * Be careful to use the correct constraint index (constraints are not
	 * numbered from 0 to number of constraints minus one, since an offset
	 * is used for some of the constraints).
	 * @param i the number of the required constraint
	 * @return the constraint number i according to the variable
	 */
	public SConstraint getConstraint(final int i) {
		return constraints.getConstraint(i);
	}


	/**
	 * Returns the number of constraints involving the variable.
	 * @return the number of constraints containing this variable
	 */
	public int getNbConstraints() {
		return constraints.getNbConstraints();
	}

	/**
	 * Access the data structure storing constraints involving a given variable.
	 * @return the backtrackable structure containing the constraints
	 */
	public PartiallyStoredVector<C> getConstraintVector() {
		return constraints.getConstraintVector();
	}

	/**
	 * Access the data structure storing indices associated to constraints 
	 * involving a given variable.
	 * @return the indices associated to this variable in each constraint
	 */
	public PartiallyStoredIntVector getIndexVector() {
		return null;
	}

	/**
	 * Returns the index of the variable in its constraint i.
	 * @param constraintIndex the index of the constraint 
	 * (among all constraints linked to the variable)
	 * @return the index of the variable
	 */
	public int getVarIndex(final int constraintIndex) {
		return -1;
	}

	/**
	 * Removes (permanently) a constraint from the list of constraints 
	 * connected to the variable.
	 * @param c the constraint that should be removed from the list this variable
	 * maintains.
	 */
	public void eraseConstraint(final SConstraint<? extends Var> c) {
		constraints.eraseConstraint(c);
	}

	// ============================================
	// Managing Listeners.
	// ============================================

	 /**
	   * Adds a new constraints on the stack of constraints
	   * the addition can be dynamic (undone upon backtracking) or not.
	   * @param c the constraint to add
	   * @param varIdx the variable index accrding to the added constraint
	   * @param dynamicAddition states if the addition is definitic (cut) or
	   * subject to backtracking (standard constraint)
	   * @return the index affected to the constraint according to this variable
	   */
	  public int addConstraint(final SConstraint c, final int varIdx,
	                           final boolean dynamicAddition) {
	     return constraints.addConstraint(c, varIdx, dynamicAddition);
	  }

	/**
	 * This methods should be used if one want to access the different constraints
	 * currently posted on this variable.
	 *
	 * Indeed, since indices are not always
	 * consecutive, it is the only simple way to achieve this.
	 *
	 * Warning ! this iterator should not be used to remove elements.
	 * The <code>remove</code> method throws an
	 * <code>UnsupportedOperationException</code>.
	 *
	 * @return an iterator over all constraints involving this variable
	 */
	public DisposableIterator<SConstraint> getConstraintsIterator() {
		return constraints.getConstraintsIterator();
	}

    @SuppressWarnings({"unchecked"})
    public final DisposableIterator<Couple<C>> getActiveConstraints(C cstrCause){
        return ((PartiallyStoredTaskCstrList)constraints).getActiveConstraint(cstrCause);
    }
    
	@Override
	public boolean isInstantiated() {
		return  start.isInstantiated() && end.isInstantiated() && duration.isInstantiated();
	}
	
	public final void updateCompulsoryPart(SConstraint cause) throws ContradictionException {
		boolean fixPoint;
		do {
			fixPoint = false;
			fixPoint |= start.updateInf(end.getInf() - duration.getSup(), cause, false);
			fixPoint |= start.updateSup(end.getSup() - duration.getInf(), cause, false);
			fixPoint |= end.updateInf(start.getInf() + duration.getInf(), cause, false);
			fixPoint |= end.updateSup(start.getSup() + duration.getSup(), cause, false);
			fixPoint |= duration.updateInf(end.getInf() - start.getSup(), cause, false);
			fixPoint |= duration.updateSup(end.getSup() - start.getInf(), cause, false);
		}while (fixPoint);
	}


    /**
     * Call awake on TaskVar.
     * @param idx index of the constraint calling #awake().
     * @param constraint
     * @param forceAwake
     */
    public void updateHypotheticalDomain(int idx, final SConstraint constraint, final boolean forceAwake){
        propagationEngine.postEvent(this, TaskVarEvent.HYPDOMMOD, constraint, forceAwake);
    }


	@Override
	public Extension getExtension(int extensionNumber) {
		return null;
	}
    
    
}
