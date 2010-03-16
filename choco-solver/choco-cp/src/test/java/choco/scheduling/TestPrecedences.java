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
package choco.scheduling;

import static choco.Choco.constant;
import static choco.Choco.endsBeforeBegin;
import static choco.Choco.makeBooleanVar;
import static choco.Choco.makeIntVar;
import static choco.Choco.makeTaskVar;
import static choco.Choco.makeTaskVarArray;
import static choco.Choco.precedenceDisjoint;
import static choco.Choco.precedenceImplied;
import static choco.Choco.precedenceReified;
import static choco.Choco.startsAfterEnd;
import static choco.Choco.startsBeforeBegin;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Ignore;
import org.junit.Test;

import samples.scheduling.pert.DeterministicPert;
import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.configure.SchedulerConfiguration;
import choco.cp.solver.constraints.global.scheduling.precedence.VariablePrecedenceDisjoint;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;



/**
 * @author Arnaud Malapert</br> 
 * @since version 2.0.0</br>
 * @version 2.0.3</br>
 */
public class TestPrecedences {

	protected final static Logger LOGGER = ChocoLogging.getTestLogger();

	public CPModel m;

	public CPSolver s;


	public void solve(int nbSol,String label) {
		long seed= SchedUtilities.RANDOM.nextLong();
		int n = 3;
		CPSolver[] solvers= new CPSolver[n];
		for (int i = 0; i < solvers.length; i++) {
			solvers[i] = new CPSolver();
			solvers[i].getSchedulerConfiguration().setForceMakespan(true);
			//solvers[i].setHorizon(Choco.MAX_UPPER_BOUND);
		}
		int cpt =0;
		SchedulerConfiguration cnf = solvers[cpt].getSchedulerConfiguration();
		cnf.setPrecedenceNetwork(true);
		cnf.setIncrementalPert(false);
		cpt++;
		cnf = solvers[cpt].getSchedulerConfiguration();
		cnf.setPrecedenceNetwork(true);
		cnf.setIncrementalPert(true);
		
		//LOGGER.setLevel(Level.INFO);
		for (CPSolver s : solvers) {
			s.read(m);
			//LOGGER.info(s.pretty());
			s.setRandomSelectors(seed);
		}
		SchedUtilities.compare(nbSol, SchedUtilities.CHECK_NODES, label, solvers);
	}

	@Test
	public void testProjectDates() {
		m=new CPModel();
		m.addVariable(makeTaskVar("alone",20, 5, "cp:bound"));
		solve(16,"project ");
		s = new CPSolver();
		s.setHorizon(15);
		s.read(m);
		s.solveAll();
		assertEquals("nbsols with horizon", 11, s.getSolutionCount());
	}



	@Test
	public void testCycle() {
		m=new CPModel();
		TaskVariable t1= makeTaskVar("t1", 20, 3);
		TaskVariable t2= makeTaskVar("t2", 20, 5);
		TaskVariable t3= makeTaskVar("t3", 20, 5);
		m.addConstraint(endsBeforeBegin(t1,t2));
		m.addConstraint(endsBeforeBegin(t2,t3));
		m.addConstraint(endsBeforeBegin(t3,t1));
		s=new CPSolver();
		s.read(m);
		s.solveAll();
		assertEquals(0, s.getNbSolutions());
		assertEquals(0, s.getNodeCount());

	}


	@Test 
	public void testPrecedenceDisjoint() {
		m = new CPModel();
		int k1 = 5, k2 = 5;
		IntegerVariable x = makeIntVar("x", 1, 10);
		IntegerVariable y = makeIntVar("y", 1, 10);
		m.addVariables("cp:bound", x, y);
		IntegerVariable z = makeIntVar("z", 0, 1);

		m.addConstraint(precedenceDisjoint(x,k1,y,k2,z));
		solve(30, "prec. disjoint");
		//s.post(new PrecedenceDisjoint(s.getVar(x),k1,s.getVar(y),k2,s.getVar(z)));
	}

	@Test 
	public void testPrecedenceReified() {
		m = new CPModel();
		int k1 = 5;
		IntegerVariable x = makeIntVar("x", 1, 10);
		IntegerVariable y = makeIntVar("y", 1, 10);
		m.addVariables("cp:bound", x, y);
		IntegerVariable z = makeIntVar("z", 0, 1);

		m.addConstraint(precedenceReified(x,k1,y,z));
		solve(100, "prec. reified");
	}

	@Test 
	public void testPrecedenceImplied() {
		m = new CPModel();
		int k1 = 5;
		//ChocoLogging.setVerbosity(Verbosity.VERBOSE);
		IntegerVariable x = makeIntVar("x", 1, 10);
		IntegerVariable y = makeIntVar("y", 1, 10);
		IntegerVariable z = makeIntVar("z", 0, 1);
		//m.addVariables("cp:bound", x, y, z);

		//m.addConstraint(leq( plus(x, k1), y));
		m.addConstraint(precedenceImplied(x,k1,y,z));
		solve(115, "prec. reified");
	}


	@Test
	public void testPrecedenceVDisjoint() {
		//integer model
		CPModel m = new CPModel();
		IntegerVariable k1 = makeIntVar("dx",2,7);
		IntegerVariable k2 = makeIntVar("dy",2,7);
		IntegerVariable x = makeIntVar("x", 1, 10);
		IntegerVariable y = makeIntVar("y", 1, 10);
		IntegerVariable z = makeIntVar("z", 0, 1);
		m.addVariables("cp:bound", x, y, k1, k2, z);
		//m.addConstraint( Choco.pre)
		//          m.addConstraints(Choco.implies(Choco.eq(z,1),Choco.leq(Choco.plus(x,k1),y)));
		//          m.addConstraints(Choco.implies(Choco.eq(z,0),Choco.leq(Choco.plus(y,k2),x)));
		CPSolver s = new CPSolver();
		s.read(m);
		s.post(new VariablePrecedenceDisjoint(s.getVar(z),s.getVar(x),s.getVar(k1),
				s.getVar(y),s.getVar(k2)));
		s.setRandomSelectors(0);
		s.solveAll();
		assertEquals(1392, s.getSolutionCount());

		//task model
		m = new CPModel();
		m.addConstraint( precedenceDisjoint(
				makeTaskVar("t1", x, k1),
				makeTaskVar("t1", y, k2), 
				z));
		s = new CPSolver();
		s.read(m);
		s.setRandomSelectors(0);
		s.solveAll();
		assertEquals(1392, s.getSolutionCount());
	}

	@Test
	public void testSBB() {
		m=new CPModel();
		TaskVariable t1= makeTaskVar("T1", 6, 3);
		TaskVariable t2= makeTaskVar("T2", 6, 5);
		m.addConstraint(startsBeforeBegin(t1, t2, -1));
		solve(5,"SBB");
	}


	@Test
	public void testSmall() {
		m=new CPModel();
		TaskVariable t1= makeTaskVar("T1", 20, 10);
		TaskVariable t2= makeTaskVar("T2", 20, 10);
		TaskVariable t3= makeTaskVar("T3", 20, 10);
		m.addConstraint(endsBeforeBegin(t1, t2));
		m.addConstraint(startsBeforeBegin(t3, t2,2));
		solve(9,"small ");
	}



	@Test
	public void testSmall2() {
		m=new CPModel();
		TaskVariable t1= makeTaskVar("T1", 20, 5);
		TaskVariable t2= makeTaskVar("T2", 20, 7);
		TaskVariable t3= makeTaskVar("T3", 20, 6);
		TaskVariable t4= makeTaskVar("T4", 20, 8);
		m.addConstraint(endsBeforeBegin(t1, t2));
		m.addConstraint(endsBeforeBegin(t1, t3));
		m.addConstraint(endsBeforeBegin(t2, t4));
		m.addConstraint(endsBeforeBegin(t1, t4));

		solve(10,"middle ");
	}


	/**
	 *  http://scienceblogs.com/goodmath/2007/09/critical_paths_scheduling_and.php
	 *  critical path lenght : 30 ; 19656 solutions
	 * @param horizon
	 * @return
	 */
	public void setPertExample(int horizon) {
		m=new CPModel();
		TaskVariable a= makeTaskVar("A",2,horizon,2);
		TaskVariable b= makeTaskVar("B",horizon, 2);
		TaskVariable c= makeTaskVar("C",3,horizon,3);
		TaskVariable d= makeTaskVar("D", horizon, 2);
		TaskVariable e= makeTaskVar("E", horizon, 4);
		TaskVariable f= makeTaskVar("F", horizon, 3);
		TaskVariable g= makeTaskVar("G", horizon, 5);
		TaskVariable h= makeTaskVar("H", horizon, 3);
		TaskVariable i= makeTaskVar("I", horizon, 6);
		m.addConstraint(startsAfterEnd(b,a));
		m.addConstraint(startsAfterEnd(b,c));

		m.addConstraints(startsAfterEnd(d,a,4));
		m.addConstraints(startsAfterEnd(d,b,5));

		m.addConstraint(startsAfterEnd(e,b));
		m.addConstraints(startsAfterEnd(f,c,2));
		m.addConstraint(startsAfterEnd(g,d));
		m.addConstraint(startsAfterEnd(h,g));

		m.addConstraints(startsAfterEnd(i,d,9));
		m.addConstraint(startsAfterEnd(i,e));
		m.addConstraint(startsAfterEnd(i,f));
	}

	@Test
	public void testPertExample() {
		setPertExample(29);
		solve(0,"pert example (unsat)");
		setPertExample(30);
		solve(19656,"pert example (sat)");
	}

	@Ignore
	@Test
	public void testLargePertExample() {
		setPertExample(32);
		solve(504726,"pert example (sat)");
	}


	@Test
	public void testIlogExample() {
		DeterministicPert ex=new DeterministicPert(17);
//		m=ex.getModel();
//		solve(0,"Ilog ex.");
//
//		ex=new DeterministicPert(18);
//		m=ex.getModel();
//		solve(154,"Ilog ex.");

		ex=new DeterministicPert(19);
		m=ex.getModel();
		solve(1764,"Ilog ex.");

//		ex=new DeterministicPert(28);
//		ex.requireUnaryResource();
//		m=ex.getModel();
//		solve(0,"Ilog ex.");
//
//		ex=new DeterministicPert(29);
//		ex.requireUnaryResource();
//		m=ex.getModel();
//		solve(112,"Ilog ex.");
	}


	protected TaskVariable[][] dtasks = {
			makeTaskVarArray("t", 0, 20, new int[]{3,4,5,6}),		
			makeTaskVarArray("t", 0, 20, new int[]{4,4,5,6}),
			makeTaskVarArray("t", 0, 20, new int[]{3,4,5,7}),
			makeTaskVarArray("t", 0, 20, new int[]{4,4,5,7})	
	};

	protected TaskVariable[] vtasks =makeTaskVarArray("t", 0, 20,
			new IntegerVariable[]{ makeIntVar("d1", 3, 4), constant(4), constant(5), makeIntVar("d1", 6, 7)}
	);

	private int k1 = -1, k2 = -2;
	private IntegerVariable b =  makeBooleanVar("b");

	protected Constraint makeConstraint(TaskVariable[] vars, int type) {
		switch(type) {
		case 1: return endsBeforeBegin(vars[2], vars[3]);
		case 2: return endsBeforeBegin(vars[3], vars[2]);
		case 3: return endsBeforeBegin(vars[2], vars[3], k1);
		case 4: return endsBeforeBegin(vars[3], vars[2], k2);
		case 5: return precedenceImplied(vars[2], 0, vars[3], b);
		case 6: return precedenceReified(vars[2], 0, vars[3], b);
		case 7: return precedenceDisjoint(vars[2], vars[3], b, 0, 0);
		case 8: return precedenceImplied(vars[2], k1, vars[3], b);
		case 9: return precedenceReified(vars[2], k1, vars[3], b);
		case 10: return precedenceDisjoint(vars[2], vars[3], b, k1, k2);
		default: return Choco.TRUE;
		}
	}

	protected void createModelEx(TaskVariable[] vars, int type) {
		m = new CPModel();
		m.addConstraints(
				endsBeforeBegin(vars[0], vars[1], 2),
				precedenceImplied(vars[0], 3, vars[2], Choco.ONE),
				precedenceReified(vars[0], 3, vars[3], Choco.ONE),
				endsBeforeBegin(vars[1], vars[3]),
				makeConstraint(vars, type)
		);

	}

	public int[] solveEx(TaskVariable[][] dtasks, TaskVariable[] vtasks, int type) {
		int[] nbsols = new int[dtasks.length + 1];
		int nbtot = 0;
		for (int i = 0; i < dtasks.length; i++) {
			createModelEx(dtasks[i], type);
			s = new CPSolver();
			s.read(m);
			s.setRandomSelectors(i);
			s.solveAll();
			nbsols[i] = s.getSolutionCount();
			nbtot += nbsols[i];
		}
		createModelEx(vtasks, type);
		solve(nbtot, "solve Decomposition vs Ex ");
		nbsols[dtasks.length] = nbtot;
		if(LOGGER.isLoggable(Level.INFO)) LOGGER.info("Number Of Solutions Vector: "+Arrays.toString(nbsols));
		return nbsols;
	}


	private void sum(int[] tab1, int[] tab2)  {
		for (int i = 0; i < tab1.length; i++) {
			tab1[i] += tab2[i];
		}
	}



	@Test
	public void testExImplied() {
		int[] bsols = solveEx(dtasks, vtasks, 0);
		//without setup times
		int[] tab1 = solveEx(dtasks, vtasks, 1);
		sum(tab1, bsols);
		int[]  tab2= solveEx(dtasks, vtasks, 5);
		assertArrayEquals("Implied - decomp vs Ex", tab1, tab2);
		//
		tab1 = solveEx(dtasks, vtasks, 3);
		sum(tab1, bsols);
		tab2= solveEx(dtasks, vtasks, 8);
		assertArrayEquals("Implied With Setup Time - decomp vs Ex", tab1, tab2);
	}

	@Test
	public void testExReified() {
		int[] tab1= solveEx(dtasks, vtasks, 0);
		//without setup times
		int[]  tab2= solveEx(dtasks, vtasks, 6);
		assertArrayEquals("Reified - decomp vs Ex", tab1, tab2);
		//with setup times
		tab2= solveEx(dtasks, vtasks, 9);
		assertArrayEquals("Reified WST - decomp vs Ex", tab1, tab2);
	}

	@Test
	public void testExDisjoint() {
		//LOGGER.setLevel(Level.INFO);
		int[] tab1= solveEx(dtasks, vtasks, 1);
		int[] tab2= solveEx(dtasks, vtasks, 2);
		sum(tab1, tab2);
		//without setup times
		tab2= solveEx(dtasks, vtasks, 7);
		assertArrayEquals("Disjoint - decomp vs Ex", tab1, tab2); 
		tab1= solveEx(dtasks, vtasks, 3);
		tab2= solveEx(dtasks, vtasks, 4);
		sum(tab1, tab2);
		//without setup times
		tab2= solveEx(dtasks, vtasks, 10);
		assertArrayEquals("Disjoint WST - decomp vs Ex", tab1, tab2); 
	}




	//	@Test
	//	public void testVariablePrecedence() {
	//		for (int seed = 0; seed < 1; seed++) {
	//			CPModel mod = new CPModel();
	//			IntegerVariable[] vars = makeIntVarArray("vs", 4, 0, 30);
	//			mod.addVariables(vars);
	//			//mod.addConstraint(leq(plus(vars[0],vars[1]),plus(vars[2],vars[3])));
	//			CPSolver cs = new CPSolver();
	//			cs.read(mod);
	//			IntDomainVar[] svars = cs.getVar(vars);
	//			cs.post(new PrecedenceWithDelta(svars[0], svars[1], svars[2], svars[3]));
	//			cs.setVarIntSelector(new RandomIntVarSelector(cs,seed));
	//			cs.setValIntSelector(new RandomIntValSelector(seed));
	//			cs.solveAll();
	//			LOGGER.info("" + cs.getNbSolutions() + " " + cs.getNodeCount() + " " + cs.getTimeCount());
	//			assertTrue(cs.getNbSolutions() == 471696);
	//		}
	//	}


}
