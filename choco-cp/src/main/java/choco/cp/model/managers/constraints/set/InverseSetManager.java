package choco.cp.model.managers.constraints.set;

import choco.cp.model.managers.SetConstraintManager;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.set.InverseSet;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;

import java.util.HashSet;

public class InverseSetManager extends SetConstraintManager {

	@Override
	public SConstraint makeConstraint(Solver solver, SetVariable[] variables,
			Object parameters, HashSet<String> options) {
		if (solver instanceof CPSolver) {
			final CPSolver s = (CPSolver) solver;
			final int nbIntVars = (Integer) parameters;
			return new InverseSet(
					VariableUtils.getIntVar(s, variables, 0, nbIntVars),
					VariableUtils.getSetVar(s, variables, nbIntVars, variables.length)
			);
		}
		return null;
	}

}