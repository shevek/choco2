package choco.kernel.solver.branch;

import java.util.logging.Level;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;
import choco.kernel.solver.search.IntBranchingDecision;

public class BranchingWithLoggingStatements extends AbstractIntBranching {
	
	/**
	 * prefixes for log statements (visualize search depth)
	 */
	public final static String[] LOG_PREFIX = { "", ".", "..", "...", "....",
		".....", "......", ".......", "........", ".........", ".........." };

	public final static Level LOGGING_LEVEL = Level.INFO;

	public AbstractIntBranching internalBranching;




	public BranchingWithLoggingStatements(AbstractIntBranching internalBranching) {
		super();
		this.internalBranching = internalBranching;
		this.setSolver(internalBranching.manager);
	}

	@Override
	public boolean finishedBranching(IntBranchingDecision decision) {
		return internalBranching.finishedBranching(decision);
	}

	@Override
	public String getDecisionLogMessage(IntBranchingDecision decision) {
		throw new SolverException("What are you doing ? It is the logging wrapper !");
	}


	public final static boolean isLoggable(final AbstractGlobalSearchStrategy manager) {
		return LOGGER.isLoggable(LOGGING_LEVEL) &&  manager.solver.getWorldIndex() < manager.solver.getLoggingMaxDepth();
	}


	public final static StringBuilder makeLoggingMsgPrefix(int worldStamp) {
		StringBuilder b  =new StringBuilder();
		b.append(LOG_PREFIX[worldStamp % (LOG_PREFIX.length)]);
		b.append('[').append(worldStamp).append(']');
		return b;
	}

	protected String makeLoggingMessage(IntBranchingDecision decision, String dirMsg,int worldStamp) {
		StringBuilder b  = makeLoggingMsgPrefix(worldStamp);
		b.append(' ').append(dirMsg);
		b.append(internalBranching.getDecisionLogMessage(decision));
		b.append(" branch ").append(decision.getBranchIndex());
		return new String(b);
	}

	@Override
	public void goDownBranch(IntBranchingDecision decision)
	throws ContradictionException {
		if (isLoggable(manager)) {
			LOGGER.log(LOGGING_LEVEL, makeLoggingMessage(decision, LOG_DOWN_MSG, manager.solver.getWorldIndex()));
		}
		internalBranching.goDownBranch(decision);

	}

	@Override
	public void goUpBranch(IntBranchingDecision decision)
	throws ContradictionException {
		if (isLoggable(manager)) {
			LOGGER.log(LOGGING_LEVEL, makeLoggingMessage(decision, LOG_UP_MSG, manager.solver.getWorldIndex() + 1));
		}
		internalBranching.goUpBranch(decision);

	}

	@Override
	public void setFirstBranch(IntBranchingDecision decision) {
		internalBranching.setFirstBranch(decision);

	}

	@Override
	public void setNextBranch(IntBranchingDecision decision) {
		internalBranching.setNextBranch(decision);

	}

	@Override
	public Object selectBranchingObject() throws ContradictionException {
		return internalBranching.selectBranchingObject();
	}


	@Override
	public void initBranching() {
		super.initBranching();
		internalBranching.initBranching();
	}

	@Override
	public void initConstraintForBranching(SConstraint c) {
		super.initConstraintForBranching(c);
		internalBranching.initConstraintForBranching(c);
	}

	public final static AbstractIntBranching setLoggingStatement(AbstractIntBranching goal) {
		AbstractIntBranching res = new BranchingWithLoggingStatements(goal);
		if (goal.getNextBranching() != null) {
			if(goal.getNextBranching() instanceof AbstractIntBranching) {
				res.setNextBranching(setLoggingStatement((AbstractIntBranching) goal.getNextBranching()));
			}else {
				throw new SolverException("cant set logging statements");
			}
		}
		return res;
	}
}