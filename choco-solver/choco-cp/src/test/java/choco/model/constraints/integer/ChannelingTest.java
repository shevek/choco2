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
package choco.model.constraints.integer;

import choco.Choco;
import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.BranchingFactory;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.scheduling.SchedUtilities;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 13 oct. 2005
 * Time: 12:37:51
 */
public class ChannelingTest {

	protected final static Logger LOGGER = ChocoLogging.getTestLogger();

	CPModel m;
	CPSolver s;

	@Before
	public void before(){
		s = new CPSolver();
		m = new CPModel();
	}
	@After
	public void after(){
		s = null;
		m = null;
	}

	@Test
	public void test1() {
		IntegerVariable y01 = makeIntVar("y01", 0, 1);
		IntegerVariable x1 = makeIntVar("x1", 0, 5);
		Constraint A = boolChanneling(y01, x1, 4);
		m.addConstraint(A);
		s.read(m);
		s.solveAll();
		assertEquals(6, s.getNbSolutions());
	}

	@Test
	public void test2() {
		IntegerVariable x1 = makeIntVar("x1", 0, 5);
		IntegerVariable x2 = makeIntVar("x2", 0, 5);
		IntegerVariable y1 = makeIntVar("y1", 0, 1);
		IntegerVariable y2 = makeIntVar("y2", 0, 1);
		IntegerVariable z = makeIntVar("z", 0, 5);
		m.addConstraint(boolChanneling(y1, x1, 4));
		m.addConstraint(boolChanneling(y2, x2, 1));
		m.addConstraint(eq(plus(y1, y2), z));
		s.read(m);
		s.maximize(s.getVar(z), false);
		LOGGER.info(s.getVar(x1).getVal() + " " + s.getVar(x2).getVal() + " " + s.getVar(z).getVal());
		assertEquals(4, s.getVar(x1).getVal());
		assertEquals(1, s.getVar(x2).getVal());
		assertEquals(2, s.getVar(z).getVal());
	}

	@Test
	public void test3() {
		int n = 5;
		IntegerVariable[] x = new IntegerVariable[n];
		IntegerVariable[] y = new IntegerVariable[n];
		for (int i = 0; i < n; i++) {
			x[i] = makeIntVar("x" + i, 0, n - 1);
			y[i] = makeIntVar("y" + i, 0, n - 1);
		}
		m.addConstraint(inverseChanneling(x, y));
		s.read(m);
		s.solve();
		do {
			for (int i = 0; i < n; i++) {
				//LOGGER.info("" + x[i] + ":" + x[i].getVal() + " <=> " + y[x[i].getVal()] + ":" + y[x[i].getVal()].getVal());
				assertTrue(s.getVar(y[s.getVar(x[i]).getVal()]).getVal() == i);
			}
		} while (s.nextSolution() == Boolean.TRUE);
		assertEquals(120, s.getNbSolutions());
	}

	@Test
	public void test4() {
		int n = 5;
		int lb = 7;
		IntegerVariable[] x = new IntegerVariable[n];
		IntegerVariable[] y = new IntegerVariable[n];
		for (int i = 0; i < n; i++) {
			x[i] = makeIntVar("x" + i, lb, lb + n - 1);
			y[i] = makeIntVar("y" + i, lb, lb + n - 1);
		}
		m.addConstraint(inverseChanneling(x, y));
		s.read(m);
		s.solve();
		do {
			for (int i = 0; i < n; i++) {
				//LOGGER.info("" + x[i] + ":" + x[i].getVal() + " <=> " + y[x[i].getVal() - lb] + ":" + y[x[i].getVal() - lb].getVal());
				assertTrue(s.getVar(y[s.getVar(x[i]).getVal() - lb]).getVal() == (i + lb));
			}
		} while (s.nextSolution() == Boolean.TRUE);
		assertEquals(120, s.getNbSolutions());
	}

	@Test
	public void test5() {
		int n = 5;
		IntegerVariable[] bv = Choco.makeBooleanVarArray("b", n);
		m.addConstraint( Choco.domainChanneling(Choco.makeIntVar("v", 0, n, Options.V_ENUM), bv));
		CPModel m1 = new CPModel();
		IntegerVariable iv = Choco.makeIntVar("v", 0, n-1, Options.V_ENUM);
		for (int i = 0; i < n; i++) {
			m1.addConstraint( Choco.boolChanneling(bv[i], iv, i));
		}
		s.read(m);
		CPSolver s1 = new CPSolver();
		s1 .read(m1);
		SchedUtilities.compare( n, SchedUtilities.NO_CHECK_NODES, "channeling", s, s1);
	}


    @Test
    public void test6(){
        IntegerVariable xi = makeIntVar("x1", 0, 5);
        IntegerVariable yij = makeIntVar("y1", 0, 0);
        int j = 10000;
        m.addConstraint(boolChanneling(yij, xi, j));
		s.read(m);
		s.solveAll();
		assertEquals(6, s.getSolutionCount());
    }

    @Test
	public void test7() {
        for(int i = 0; i < 1000; i++){
            m = new CPModel();
            s= new CPSolver();
            final int n = 5;
            final IntegerVariable[] bv = Choco.makeBooleanVarArray("n", n+1, Options.V_NO_DECISION);
            final IntegerVariable vm = Choco.makeIntVar("vm", 0, n, Options.V_ENUM);
            m.addConstraint( Choco.domainChanneling(vm, bv));
            m.addConstraint( Choco.eq(vm, n-1));
            s.read(m);
            BranchingFactory.randomSearch(s, s.getVar(new IntegerVariable[]{vm}), i);
            s.solveAll();
            Assert.assertEquals(1, s.getSolutionCount());
        }
	}

    @Test
    public void testFHER(){

        CPModel model = new CPModel();
        int nbVMs = 4;
        int nbNodes = 4;
        IntegerVariable [] assigns = new IntegerVariable[4];
        IntegerVariable [][] bools = new IntegerVariable[nbVMs][nbNodes + 2];
        IntegerVariable [][] invBools = new IntegerVariable[nbNodes + 2][nbVMs];

        int i = 0;
        for (i = 0; i < assigns.length; i++) {
            assigns[i] = Choco.makeIntVar("VM" + i, 0, nbNodes + 1, Options.V_ENUM);
        }
        for (i = 0; i < nbVMs; i++) {
            for (int j = 0; j < nbNodes + 2; j++) {
                bools[i][j] = Choco.makeBooleanVar("VM" + i + " on N" + j);
                invBools[j][i] = bools[i][j];
            }
        }

        for (i = 0; i < nbVMs; i++) {
            model.addConstraint(Choco.neq(assigns[i], 4));
            model.addConstraint(Choco.neq(assigns[i], 5));
            model.addConstraint(Choco.domainChanneling(assigns[i], bools[i]));
        }
        CPSolver solver = new CPSolver();
        for (int j = 0; j < nbNodes; j++) {
            model.addConstraint(Choco.leq(Choco.sum(invBools[j]), 1));
        }
        solver.read(model);
         solver.setVarIntSelector(new StaticVarOrder(solver, solver.getVar(assigns[0],assigns[1], assigns[2], assigns[3])));
        solver.solve();
    }

}
