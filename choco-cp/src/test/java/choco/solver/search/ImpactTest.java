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

package choco.solver.search;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.branching.DomOverWDegBranching;
import choco.cp.solver.search.integer.branching.ImpactBasedBranching;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: GROCHART
 * Date: 17 mai 2008
 * Time: 12:36:46
 */
public class ImpactTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    @Test
    public void test1() {
//	public static void main(String[] args) {
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable[] vs1 = Choco.makeIntVarArray("v1", 10, 2, 10);
        m.addVariables(Options.V_ENUM, vs1);

        m.addVariables(vs1);
        for (int i = 0; i < vs1.length; i++) {
            for (int j = i + 1; j < vs1.length; j++) {
                m.addConstraint(Choco.neq(vs1[i], vs1[j]));
            }
        }
        s.read(m);
        ImpactBasedBranching ibb = new ImpactBasedBranching(s);
        ibb.getImpactStrategy().initImpacts(100);
        s.attachGoal(ibb);
        s.setFirstSolution(true);
        s.generateSearchStrategy();
        s.printRuntimeStatistics();
        s.launch();
    }

    @Test
    public void test2() {
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable[] vs1 = Choco.makeIntVarArray("v1", 10, 2, 10);

        m.addVariables(vs1);
        for (int i = 0; i < vs1.length; i++) {
            for (int j = i + 1; j < vs1.length; j++) {
                m.addConstraint(Choco.neq(vs1[i], vs1[j]));
            }
        }
        s.read(m);

        ImpactBasedBranching ibb = new ImpactBasedBranching(s);
        ibb.getImpactStrategy().initImpacts(100);
        s.attachGoal(ibb);
        s.setFirstSolution(true);
        s.generateSearchStrategy();
        s.printRuntimeStatistics();
        s.launch();
    }

    @Test
    public void testMagicSquare() {
        testMagicSquare(9);
    }

    public void testMagicSquare(int n) {
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] vars = Choco.makeIntVarArray("C", n * n, 1, n * n);
        m.addVariables(Options.V_ENUM, vars);

        IntegerVariable sum = Choco.makeIntVar("sum", 1, n * n * (n * n + 1) / 2);
        m.addVariable(sum);

        m.addConstraint(Options.C_ALLDIFFERENT_BC, Choco.allDifferent(vars));
        for (int i = 0; i < n; i++) {
            IntegerVariable[] col = new IntegerVariable[n];
            IntegerVariable[] row = new IntegerVariable[n];

            for (int j = 0; j < n; j++) {
                col[j] = vars[i * n + j];
                row[j] = vars[j * n + i];
            }

            m.addConstraint(Choco.eq(Choco.sum(col), sum));
            m.addConstraint(Choco.eq(Choco.sum(row), sum));
        }

        m.addConstraint(Choco.eq(sum, n * (n * n + 1) / 2));

        CPSolver s = new CPSolver();
        s.read(m);

        ImpactBasedBranching ibb = new ImpactBasedBranching(s);
        ibb.getImpactStrategy().initImpacts(1000000);

        s.setTimeLimit(65000);
        s.setGeometricRestart(14, 1.5d);
        s.generateSearchStrategy();
        s.clearGoals();
        s.addGoal(ibb);
        s.setFirstSolution(true);
        s.launch();

        LOGGER.info(n + " Nb noeuds = " + s.getNodeCount());
        LOGGER.info(n + " Temps = " + s.getTimeCount());

        for (int i = 0; i < n; i++) {
            StringBuffer st = new StringBuffer();
            for (int j = 0; j < n; j++) {
                st.append(MessageFormat.format("{0} ", s.getIntVar(i * n + j).getVal()));
            }
            LOGGER.info(st.toString());
        }

        assertTrue(String.format("%s >= 5000", s.getNodeCount()),s.getNodeCount() < 5000);
    }

    @Test
    public void testMagicSquareRestartDwdeg() {
        int n = 5;
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] vars = Choco.makeIntVarArray("C", n * n, 1, n * n);
        m.addVariables(Options.V_ENUM, vars);

        IntegerVariable sum = Choco.makeIntVar("sum", 1, n * n * (n * n + 1) / 2);
        m.addVariable(sum);

        m.addConstraint(Options.C_ALLDIFFERENT_BC, Choco.allDifferent(vars));
        for (int i = 0; i < n; i++) {
            IntegerVariable[] col = new IntegerVariable[n];
            IntegerVariable[] row = new IntegerVariable[n];

            for (int j = 0; j < n; j++) {
                col[j] = vars[i * n + j];
                row[j] = vars[j * n + i];
            }

            m.addConstraint(Choco.eq(Choco.sum(col), sum));
            m.addConstraint(Choco.eq(Choco.sum(row), sum));
        }

        m.addConstraint(Choco.eq(sum, n * (n * n + 1) / 2));

        CPSolver s = new CPSolver();
        s.read(m);

        s.setGeometricRestart(14, 1.5d);
        s.setFirstSolution(true);
        s.generateSearchStrategy();
        s.attachGoal(new DomOverWDegBranching(s, new IncreasingDomain()));
        s.launch();
        LOGGER.info(n + " Nb noeuds = " + s.getNodeCount());
        LOGGER.info(n + " Nb backs = " + s.getBackTrackCount());
        LOGGER.info(n + " Temps = " + s.getTimeCount());

        for (int i = 0; i < n; i++) {
            StringBuffer st = new StringBuffer();
            for (int j = 0; j < n; j++) {
                st.append(MessageFormat.format("{0} ", s.getIntVar(i * n + j).getVal()));
            }
            LOGGER.info(st.toString());
        }
        LOGGER.info("" + s.getNodeCount());
        assertTrue(s.getNodeCount() < 5000);
    }

    public static void main(String[] args) {
        int n = 7;
        Model m = new CPModel();
        m.setDefaultExpressionDecomposition(true);
        IntegerVariable[] vars = Choco.makeIntVarArray("C", n * n, 1, n * n);
        m.addVariables(Options.V_ENUM, vars);

        IntegerVariable sum = Choco.makeIntVar("sum", 1, n * n * (n * n + 1) / 2);
        m.addVariable(Options.C_ALLDIFFERENT_CLIQUE, sum);

        m.addConstraint(Options.C_ALLDIFFERENT_BC, Choco.allDifferent(vars));
        for (int i = 0; i < n; i++) {
            IntegerVariable[] col = new IntegerVariable[n];
            IntegerVariable[] row = new IntegerVariable[n];

            for (int j = 0; j < n; j++) {
                col[j] = vars[i * n + j];
                row[j] = vars[j * n + i];
            }

            m.addConstraint(Choco.eq(Choco.sum(col), sum));
            m.addConstraint(Choco.eq(Choco.sum(row), sum));
        }

        m.addConstraint(Choco.eq(sum, n * (n * n + 1) / 2));

        CPSolver s = new CPSolver();
        s.read(m);
        s.monitorBackTrackLimit(true);
        s.solve();
        System.out.println(n + " Nb noeuds = " + s.getNodeCount());
        System.out.println(n + " Temps = " + s.getTimeCount());
        System.out.println(n + " back = " + s.getBackTrackCount());

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print(s.getIntVar(i * n + j).getVal() + " ");
            }
            System.out.println("");
        }
    }

}