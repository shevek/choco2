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

package samples.tutorials.to_sort.packing.parser;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.constraints.global.pack.PackSConstraint;
import choco.cp.solver.search.BranchingFactory;
import choco.kernel.common.opres.pack.LowerBoundFactory;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.pack.PackModel;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solver;
import choco.visu.components.chart.ChocoChartFactory;
import org.jfree.chart.JFreeChart;
import parser.instance.AbstractMinimizeModel;
import parser.instance.IHeuristicAlgorithm;
import parser.instances.BasicSettings;

import static choco.Options.*;


/**
 *
 * @author Arnaud Malapert
 */
public class BinPackingModel extends AbstractMinimizeModel {

	private int nbBins;
	
	/** variables of the model*/
	private PackModel modeler;

	private Constraint pack;

	public BinPackingModel(Configuration configuration) {
		super(new BinPackingFileParser(), configuration);
	}

	@Override
	public void initialize() {
		super.initialize();
		heuristics = IHeuristicAlgorithm.SINGLOTON;
		nbBins = 0;
		modeler = null;
		pack = null;
	}

	@Override
	public Boolean preprocess() {
		final BinPackingFileParser pr = (BinPackingFileParser) parser;
		heuristics = new CompositeHeuristics1BP(pr);
		Boolean b = super.preprocess();
		if(b != null && Boolean.valueOf(b)) {
			computedLowerBound = LowerBoundFactory.computeL_DFF_1BP(pr.sizes, pr.capacity, heuristics.getObjectiveValue().intValue());
			nbBins = heuristics.getObjectiveValue().intValue() - 1;
		}else {
			computedLowerBound = 0;
			nbBins = pr.sizes.length;
		}
		return b;
	}

	@Override
	public Model buildModel() {
		CPModel m = new CPModel();
		final BinPackingFileParser pr = (BinPackingFileParser) parser;
		modeler = new PackModel("", pr.sizes, nbBins, pr.capacity);
		pack = Choco.pack(modeler, C_PACK_AR, C_PACK_DLB, C_PACK_LBE);
		m.addConstraint(pack);
		if( ! defaultConf.readBoolean(BasicSettings.LIGHT_MODEL) ) {
			pack.addOption(C_PACK_FB);
			m.addConstraints(modeler.packLargeItems()); // best symmetry breaking ? 
		}
		modeler.nbNonEmpty.addOption(V_OBJECTIVE);
		return m;
	}

	@Override
	public Solver buildSolver() {
		Solver s = super.buildSolver(); // create the solver
		s.read(model);  //read the model
		s.clearGoals();
		if(defaultConf.readBoolean(BasicSettings.LIGHT_MODEL) ) {
			s.addGoal(BranchingFactory.lexicographic(s, s.getVar(modeler.getBins())));
		}else {
			final PackSConstraint ct = (PackSConstraint) s.getCstr(pack);
			s.addGoal(BranchingFactory.completeDecreasingBestFit(s, ct));
		}
		s.generateSearchStrategy();
		return s;
	}

	@Override
	public String getValuesMessage() {
		if(solver != null && solver.existsSolution()) {
			return ( (PackSConstraint) solver.getCstr(pack) ).getSolutionMsg(); 
		} else return "";
	}	
	
	@Override
	public JFreeChart makeSolutionChart() {
		return solver != null && solver.existsSolution() ?
				ChocoChartFactory.createPackChart(getInstanceName()+" : "+getStatus(), (PackSConstraint) solver.getCstr(pack)) : null;
	}
		
}