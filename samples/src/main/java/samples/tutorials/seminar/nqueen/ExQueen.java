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

package samples.tutorials.seminar.nqueen;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.branching.AssignOrForbidIntVarVal;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

import java.util.logging.Logger;

import static choco.Choco.*;


/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 29 mai 2008
 * Since : Choco 2.0.0
 *
 */
public class ExQueen {

    protected final static Logger LOGGER = ChocoLogging.getMainLogger();

	public static void nQueensNaif(int n) {
		Model m = new CPModel();

		IntegerVariable[] queens = new IntegerVariable[n];
		for (int i = 0; i < n; i++) {
			queens[i] = makeIntVar("Q" + i, 1, n);
		}

		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				int k = j - i;
				m.addConstraint(neq(queens[i], queens[j]));
				m.addConstraint(neq(queens[i], plus(queens[j], k)));  // diagonal constraints
				m.addConstraint(neq(queens[i], minus(queens[j], k))); // diagonal constraints
			}
		}

		Solver s = new CPSolver();
		s.read(m);

		int timeLimit = 60000;
		s.setTimeLimit(timeLimit);
		s.solve();

	}

	public static void nQueensAlldiff(int n) {
		Model m = new CPModel();

		IntegerVariable[] queens = Choco.makeIntVarArray("Q", n, 1,n);
        IntegerVariable[] diag1 = Choco.makeIntVarArray("D1", n, 1, 2*n);
		IntegerVariable[] diag2 = Choco.makeIntVarArray("D2", n, -n, n);

		m.addConstraint(Options.C_ALLDIFFERENT_AC, allDifferent(queens));
		for (int i = 0; i < n; i++) {
			m.addConstraint(eq(diag1[i], plus(queens[i], i)));
			m.addConstraint(eq(diag2[i], minus(queens[i], i)));
		}
		m.addConstraint(Options.C_ALLDIFFERENT_AC, allDifferent(diag1));
		m.addConstraint(Options.C_ALLDIFFERENT_AC, allDifferent(diag2));

		Solver s = new CPSolver();
		s.read(m);

        System.out.printf("%s\n", s.pretty());
		int timeLimit = 60000;
		s.setTimeLimit(timeLimit);
        s.addGoal(new AssignOrForbidIntVarVal(new StaticVarOrder(s,s.getVar(queens)), new MinVal()));
//		s.solve();
        s.solveAll();
	}

	public static void nQueensNaifRed(int n) {
		Model m = new CPModel();

		IntegerVariable[] queens = new IntegerVariable[n];
		IntegerVariable[] queensdual = new IntegerVariable[n];

		for (int i = 0; i < n; i++) {
			queens[i] = makeIntVar("Q" + i, 1, n);
			queensdual[i] = makeIntVar("QD" + i, 1, n);
		}

		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				int k = j - i;
				m.addConstraint(neq(queens[i], queens[j]));
				m.addConstraint(neq(queens[i], plus(queens[j], k)));  // diagonal constraints
				m.addConstraint(neq(queens[i], minus(queens[j], k))); // diagonal constraints
			}
		}
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				int k = j - i;
				m.addConstraint(neq(queensdual[i], queensdual[j]));
				m.addConstraint(neq(queensdual[i], plus(queensdual[j], k)));  // diagonal constraints
				m.addConstraint(neq(queensdual[i], minus(queensdual[j], k))); // diagonal constraints
			}
		}
		m.addConstraint(inverseChanneling(queens, queensdual));

		Solver s = new CPSolver();
		s.read(m);

		s.setVarIntSelector(new MinDomain(s,s.getVar(queens)));

		int timeLimit = 60000;
		s.setTimeLimit(timeLimit);
		s.solve();
	}

	public static void nQueensAlldiffRed(int n) {
		Model m = new CPModel();

		IntegerVariable[] queens = new IntegerVariable[n];
		IntegerVariable[] queensdual = new IntegerVariable[n];
		IntegerVariable[] diag1 = new IntegerVariable[n];
		IntegerVariable[] diag2 = new IntegerVariable[n];
		IntegerVariable[] diag1dual = new IntegerVariable[n];
		IntegerVariable[] diag2dual = new IntegerVariable[n];

		for (int i = 0; i < n; i++) {
			queens[i] = makeIntVar("Q" + i, 1, n);
			queensdual[i] = makeIntVar("Qd" + i, 1, n);
			diag1[i] = makeIntVar("D1" + i, 1, 2 * n);
			diag2[i] = makeIntVar("D2" + i, -n, n);
			diag1dual[i] = makeIntVar("D1" + i, 1, 2 * n);
			diag2dual[i] = makeIntVar("D2" + i, -n, n);
		}

		m.addConstraint(allDifferent(queens));
		m.addConstraint(allDifferent(queensdual));
		for (int i = 0; i < n; i++) {
			m.addConstraint(eq(diag1[i], plus(queens[i], i)));
			m.addConstraint(eq(diag2[i], minus(queens[i], i)));

			m.addConstraint(eq(diag1dual[i], plus(queensdual[i], i)));
			m.addConstraint(eq(diag2dual[i], minus(queensdual[i], i)));
		}

		m.addConstraint(allDifferent(diag1));
		m.addConstraint(allDifferent(diag2));
		m.addConstraint(allDifferent(diag1dual));
		m.addConstraint(allDifferent(diag2dual));
		m.addConstraint(inverseChanneling(queens,queensdual));

		Solver s = new CPSolver();
		s.read(m);


		int timeLimit = 60000;
		s.setTimeLimit(timeLimit);
		s.solve();
	}

	public static void heuristicNqueensNaifRed(int n) {
		Model m = new CPModel();

		IntegerVariable[] queens = new IntegerVariable[n];
		IntegerVariable[] queensdual = new IntegerVariable[n];

		for (int i = 0; i < n; i++) {
			queens[i] = makeIntVar("Q" + i, 1, n);
			queensdual[i] = makeIntVar("QD" + i, 1, n);
		}

		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				int k = j - i;
				m.addConstraint(neq(queens[i], queens[j]));
				m.addConstraint(neq(queens[i], plus(queens[j], k)));  // diagonal constraints
				m.addConstraint(neq(queens[i], minus(queens[j], k))); // diagonal constraints
			}
		}
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				int k = j - i;
				m.addConstraint(neq(queensdual[i], queensdual[j]));
				m.addConstraint(neq(queensdual[i], plus(queensdual[j], k)));  // diagonal constraints
				m.addConstraint(neq(queensdual[i], minus(queensdual[j], k))); // diagonal constraints
			}
		}
		m.addConstraint(inverseChanneling(queens, queensdual));

		Solver s = new CPSolver();
		s.read(m);


		int timeLimit = 60000;
		s.setTimeLimit(timeLimit);
		s.solve();
	}


	public static void heuristicNqueensAlldiffRed(int n) {
		Model m = new CPModel();

		IntegerVariable[] queens = new IntegerVariable[n];
		IntegerVariable[] queensdual = new IntegerVariable[n];
		IntegerVariable[] diag1 = new IntegerVariable[n];
		IntegerVariable[] diag2 = new IntegerVariable[n];
		IntegerVariable[] diag1dual = new IntegerVariable[n];
		IntegerVariable[] diag2dual = new IntegerVariable[n];

		for (int i = 0; i < n; i++) {
			queens[i] = makeIntVar("Q" + i, 1, n);
			queensdual[i] = makeIntVar("QD" + i, 1, n);
			diag1[i] = makeIntVar("D1" + i, 1, 2 * n);
			diag2[i] = makeIntVar("D2" + i, -n, n);
			diag1dual[i] = makeIntVar("D1" + i, 1, 2 * n);
			diag2dual[i] = makeIntVar("D2" + i, -n, n);
		}

		m.addConstraint(allDifferent(queens));
		m.addConstraint(allDifferent(queensdual));
		for (int i = 0; i < n; i++) {
			m.addConstraint(eq(diag1[i], plus(queens[i], i)));
			m.addConstraint(eq(diag2[i], minus(queens[i], i)));

			m.addConstraint(eq(diag1dual[i], plus(queensdual[i], i)));
			m.addConstraint(eq(diag2dual[i], minus(queensdual[i], i)));
		}

		m.addConstraint(allDifferent(diag1));
		m.addConstraint(allDifferent(diag2));
		m.addConstraint(allDifferent(diag1dual));
		m.addConstraint(allDifferent(diag2dual));
		m.addConstraint(inverseChanneling(queens,queensdual));

		Solver s = new CPSolver();
		s.read(m);

		s.setVarIntSelector(new MinDomain(s,s.getVar(queens)));
		s.setValIntSelector(new NQueenValueSelector(s.getVar(queensdual)));

		int timeLimit = 60000;
		s.setTimeLimit(timeLimit);
		s.solve();

	}


	public static void main(String[] args) {
		int nbQueens = 12;
		//ChocoLogging.setVerbosity(Verbosity.VERBOSE);
		/*LOGGER.info("************* Nqueens naif *************");
//		nQueensNaif(nbQueens); // (1)
		LOGGER.info("");
		LOGGER.info("****************************************");
		LOGGER.info("");*/

        ChocoLogging.toVerbose();
        ChocoLogging.setEveryXNodes(Integer.MAX_VALUE);
		LOGGER.info("*********** Nqueens alldiff ************");
		nQueensAlldiff(nbQueens); // (2)
		LOGGER.info("");
		LOGGER.info("****************************************");
		LOGGER.info("");

		/*LOGGER.info("******* Nqueens naif redondant *********");
//		nQueensNaifRed(nbQueens); // (3)
		LOGGER.info("");
		LOGGER.info("****************************************");
		LOGGER.info("");

		LOGGER.info("****** Nqueens alldiff redondant *******");
//		nQueensAlldiffRed(nbQueens); // (4)
		LOGGER.info("");
		LOGGER.info("****************************************");
		LOGGER.info("");

		LOGGER.info("***** Nqueens naif redondant heur ******");
//		heuristicNqueensNaifRed(nbQueens); // (5)
		LOGGER.info("");
		LOGGER.info("****************************************");
		LOGGER.info("");

		LOGGER.info("**** Nqueens alldiff redondant heur ****");
//		heuristicNqueensAlldiffRed(nbQueens);  // (6)
		LOGGER.info("");
		LOGGER.info("****************************************");
		LOGGER.info("");*/
		ChocoLogging.flushLogs();
	}

}
