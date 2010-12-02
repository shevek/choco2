/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package samples.tutorials.scheduling;


import static choco.Choco.boolChanneling;
import static choco.Choco.constant;
import static choco.Choco.constantArray;
import static choco.Choco.cumulativeMax;
import static choco.Choco.eq;
import static choco.Choco.makeBooleanVarArray;
import static choco.Choco.makeIntVar;
import static choco.Choco.makeTaskVarArray;
import static choco.Choco.startsAt;
import static choco.Choco.sum;
import static choco.Options.NO_OPTION;
import static choco.Options.V_BOUND;
import static choco.Options.V_OBJECTIVE;

import java.util.Arrays;
import java.util.logging.Level;

import samples.tutorials.PatternExample;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.visu.components.chart.ChocoChartFactory;


public class CumulativeScheduling extends PatternExample {

	protected final static int NT = 11, NF = 3, N =  NT + NF;

	private final static int[] HEIGHTS =  new int[]{2, 1, 4, 2, 3, 1, 5, 6, 2, 1, 3, 1, 1, 2};

	private final static int[] DURATIONS =  new int[]{1, 1, 1, 2, 1, 3, 1, 1, 3, 4, 2, 3, 1, 1};

	private final static int HORIZON = 6;

	private final static int CAPACITY = 7;

	private boolean useAlternativeResource;

	private IntegerVariable[] usages, heights;

	private TaskVariable[] tasks;

	private Constraint cumulative;

	@Override
	public void setUp(Object parameters) {
		if (parameters instanceof Boolean) {
			useAlternativeResource = ((Boolean) parameters).booleanValue();
		}else {
			useAlternativeResource = true;
		}
		super.setUp(parameters);
	}

	@Override
	public void buildModel() {
		//the fake tasks to establish the profile capacity of the ressource are the NF firsts.
		tasks = makeTaskVarArray("t", 0, HORIZON, DURATIONS, V_BOUND);
		model = new CPModel();
		//set fake tasks to establish the profile capacity
		model.addConstraints(
				startsAt(tasks[0], 1),
				startsAt(tasks[1], 2),
				startsAt(tasks[2], 3)
		);
		usages = makeBooleanVarArray("U", NT);
		//state the objective function
		model.addConstraint(eq( sum(usages), makeIntVar("obj", 0, NT, V_BOUND, V_OBJECTIVE)));
		if(useAlternativeResource) {
			heights = constantArray(HEIGHTS);
			cumulative = cumulativeMax("alt-cumulative", tasks, heights, usages, constant(CAPACITY),NO_OPTION);
		}else {
			heights = new IntegerVariable[N];
			//post the channeling to know if the task uses the resource or not.
			for (int i = 0; i < NF; i++) {
				heights[i] = constant(HEIGHTS[i]);
			}
			for (int i = NF; i < N; i++) {
				heights[i] =  makeIntVar("H_" + i, new int[]{0, HEIGHTS[i]});
				model.addConstraint(boolChanneling(usages[i- NF], heights[i], HEIGHTS[i]));
			}
			cumulative =cumulativeMax("cumulative", tasks, heights, constant(CAPACITY), "");
		}
		model.addConstraint(cumulative);
	}

	@Override
	public void buildSolver() {
		solver = new CPSolver();
		solver.read(model);
	}

	@Override
	public void prettyOut() {
		if(LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info((useAlternativeResource ? "Alternative Resource" : "Channeling Constraints")+" Model: \n");
			if(solver.existsSolution()) {
				LOGGER.info("makespan: "+solver.getObjectiveValue()+"\n"+Arrays.toString(usages));
				final String title = "Cumulative Packing Constraint Visualization";
				ChocoChartFactory.createAndShowGUI(title, ChocoChartFactory.createCumulativeChart(title, (CPSolver) solver, cumulative, true));
			}
		}
	}

	@Override
	public void solve() {
		solver.maximize(false);
	}

	@Override
	public void execute() {
		super.execute(Boolean.TRUE);
	}

	public static void main(String[] args) {
		new CumulativeScheduling().execute(Boolean.FALSE);
		new CumulativeScheduling().execute(Boolean.TRUE);
	}

}
