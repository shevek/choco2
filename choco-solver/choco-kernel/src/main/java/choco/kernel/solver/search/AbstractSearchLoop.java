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
package choco.kernel.solver.search;

import static choco.kernel.solver.search.AbstractGlobalSearchStrategy.DOWN_BRANCH;
import static choco.kernel.solver.search.AbstractGlobalSearchStrategy.INIT_SEARCH;
import static choco.kernel.solver.search.AbstractGlobalSearchStrategy.OPEN_NODE;
import static choco.kernel.solver.search.AbstractGlobalSearchStrategy.RESTART;
import static choco.kernel.solver.search.AbstractGlobalSearchStrategy.STOP;
import static choco.kernel.solver.search.AbstractGlobalSearchStrategy.UP_BRANCH;


public abstract class AbstractSearchLoop implements ISearchLoop {

	public final AbstractGlobalSearchStrategy searchStrategy;

	private int nodeCount;
	
	private int backtrackCount;
	
	private int restartCount;
	
	private int depthCount;

	protected boolean stop;

	
	public AbstractSearchLoop(AbstractGlobalSearchStrategy searchStrategy) {
		this.searchStrategy = searchStrategy;
	}

	

	public final AbstractGlobalSearchStrategy getSearchStrategy() {
		return searchStrategy;
	}



	public final boolean isStopped() {
		return stop;
	}

	public final int getNodeCount() {
		return nodeCount;
	}

	public final int getBacktrackCount() {
		return backtrackCount;
	}


	public final int getRestartCount() {
		return restartCount;
	}


	public final int getDepthCount() {
		return depthCount;
	}


	@Override
	public void initialize() {
		nodeCount = 0;
		backtrackCount = 0;
		restartCount = 0;
		depthCount = 0;
		
	}


	public final Boolean run() {
		stop = false;
		initLoop();
		while (!stop) {
			switch (searchStrategy.nextMove) {
			//The order of the condition is important. 
			//SEARCH TREE MOVES
			case OPEN_NODE: {
				nodeCount++;
				openNode();
				break;
			}
			case DOWN_BRANCH: {
				depthCount++;
				downBranch();
				break;
			}
			case UP_BRANCH: {
				if (searchStrategy.isTraceEmpty()) {
					//cant backtrack from the root node
					stop = true;
				} else {
					depthCount--;
					backtrackCount++;
					upBranch();
				}
				break;
			}
			//RESTART MOVES
			case RESTART: {
				restartCount++;
				restart();
				depthCount = 0;
				break;
			}
			case INIT_SEARCH: {
				initSearch();
				break;
			}
			//FINAL MOVES
			case STOP: {
				stop = true;
				break;
			}
			}
		}
		return endLoop();
	}

	public abstract void initLoop();
	
	public abstract void openNode();
	
	public abstract void upBranch();
	
	public abstract void downBranch();
	
	public abstract void restart();
	
	public abstract void initSearch();
	
	public abstract Boolean endLoop();


}
