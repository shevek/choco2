package choco.cp.model.managers.constraints.global;

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.scheduling.PrecedenceImplied;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

public class PrecedenceImpliedManager extends AbstractPrecedenceManager {

	@Override
	protected SConstraint makeIntConstraintB0(CPSolver s, IntDomainVar x1,
			int k1, IntDomainVar x2, int k2) {
		return CPSolver.TRUE;
	}

	
	@Override
	protected SConstraint makeIntConstraint(CPSolver s, IntDomainVar x1,
			int k1, IntDomainVar x2, int k2, IntDomainVar dir) {
		return new PrecedenceImplied(x1, k1, x2, dir);
	}
	

	@Override
	protected SConstraint makeTaskConstraintB0(CPSolver s, TaskVar t1, int k1,
			TaskVar t2, int k2) {
		return CPSolver.TRUE;
	}


	@Override
	protected SConstraint makeTaskConstraint(CPSolver s, TaskVar t1, int k1,
			TaskVar t2, int k2, IntDomainVar dir) {
		final PrecedenceImplied c =  ( 
				t1.duration().isInstantiated() ? 
						new PrecedenceImplied(t1.start(), t1.duration().getVal() + k1, t2.start(), dir) :
							new PrecedenceImplied(t1.end(), k1, t2.start(), dir)
		);
		c.setTasks(t1, t2);
		return c;
	}


}
