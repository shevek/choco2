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

package choco.model.variables;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.TimesXYZ;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.Model;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.pack.PackModel;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import static choco.Choco.*;
import static java.text.MessageFormat.format;
import static junit.framework.Assert.*;

/**
 * @author Arnaud Malapert
 *
 */
public class VariablesTest {
    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

	private CPModel model;

	private void test(Variable toRemove,int nbVars) {
		LOGGER.info(model.pretty()+ '\n');
		model.removeVariable(toRemove);
		test(0, nbVars);
		
	}
	
	private void test(Constraint toRemove, int nbConstraints, int nbConstants, int nbVars) {
		LOGGER.info(model.pretty()+ '\n');
		model.removeConstraint(toRemove);
		test(nbConstraints, nbVars);
		assertEquals("nb constants",nbConstants, model.getNbConstantVars());
		
	}
	
	private void test(int nbConstraints, int nbVars) {
		LOGGER.info(model.pretty()+ '\n');
		assertEquals("nb remaining constraints",nbConstraints, model.getNbConstraints());
		assertEquals("nb remaining variables ",nbVars, model.getNbTotVars());
	}

    static{
        Options.create("foo",0);
        Options.create("bar",1);
        Options.create("fou",2);
        Options.create("bao",2);
    }

	@Before
	public void initialize() {
		model=new CPModel();
	}

	@Test
	public void testRemoveInt() {
		IntegerVariable[] v=makeIntVarArray("v",2, 0, 2);
        model.addVariables(Options.V_BOUND, v);
        model.addConstraint(neq(v[0], v[1]));
		test(v[0],0);
	}

	@Test
	public void testRemoveSet() {
		SetVariable[] v=makeSetVarArray("v",2, 0, 2);
        model.addVariables(Options.V_BOUND, v);
        model.addConstraint(neq(v[0], v[1]));
		test(v[0],0);
	}

	@Test
	public void testEnumVarWithRedundantValues() {
		int[] values = new int[] {3,5,7,3,5,9,10, 10};
		int[] res = new int[] {3,5,7,9,10};
		Assert.assertArrayEquals("sort and remove redundat values ", res, ArrayUtils.getNonRedundantSortedValues(values));
		final CPModel m = new CPModel();
		final IntegerVariable v = Choco.makeIntVar("v", values);
		m.addVariable(v);
		final CPSolver s = new CPSolver();
		s.read(m);
		IntDomainVar var = s.getVar(v);
		try {
			assertEquals("Enum Domain size",5, var.getDomainSize());
			var.updateInf(6, null, true);
			assertEquals("Enum Domain size",3, var.getDomainSize());
			var.updateSup(8, null, true);
			assertEquals("Enum Domain size",1, var.getDomainSize());
			assertTrue("Enum Inst. Domain", var.isInstantiatedTo(7));
		} catch (ContradictionException e) {
			Assert.fail("inconsitent integer enum domain");
		}
		
	}
	
	@Test
	public void testRemoveReal() {
		SetVariable[] v=makeSetVarArray("v",2, 0, 2);
        model.addVariables(Options.V_BOUND, v);
        model.addConstraint(Choco.neq(v[0], v[1]));
		test(v[0],0);
	}

	private final IntegerVariable[] bools = Choco.makeBooleanVarArray("b",3);
	
	private final IntegerVariable one = Choco.constant(1);
	
	private final Constraint[] cstr = { Choco.eq(bools[0], one), Choco.eq(bools[1], one),Choco.allDifferent(bools)}; 
	
	@Test
	public void testRemoveConstraint1() {
		model.addConstraints(cstr[0],cstr[1]);
		test(2,2);
		test(cstr[1], 1, 1, 1); //quick fix : forbid to remove constants
		test(cstr[0], 0, 1, 0); 
	}
	
	@Test
	public void testRemoveConstraint2() {
		model.addConstraints(cstr);
		test(3,3);
		test(cstr[2], 2, 1, 2);
		test(cstr[1], 1, 1, 1); //quick fix : forbid to remove constants
		test(cstr[0], 0, 1, 0);
	}

	@Test
	public void testDecisionOptions(){
		Model m = new CPModel();
		SetVariable s1 = makeSetVar("set1", 0, 10);
		SetVariable s2 = makeSetVar("set2", 5, 20);
		s1.getCard().addOption(Options.V_NO_DECISION);
		m.addVariables(s1, s2,constant(2), constant(1));
		CPSolver solver = new CPSolver();
		solver.read(m);
		IntDomainVar[] l =solver.getIntDecisionVars();
		LOGGER.info(format("{0}", l));
		assertEquals("only one int decision var",1,l.length);
		assertEquals("number of integer constants",2,solver.getNbIntConstants());
        boolean contains = false;
        for(int i = 0; i < l.length; i++){
            contains |= l[i].equals(solver.getVar(s2.getCard()));
        }
		assertTrue("check decision var",contains);
	}
	
	private static void checkOptions(List<String> options, int nb, int length) {
		assertEquals("Nb Options", nb, options.size());
		for (String str : options) {
			assertEquals("Option lenght: |"+str+ '|', length, str.length());
		}
	}
	
	@Test
	public void testOptions0() {
		Iterator<String> iter = StringUtils.getOptionIterator("foo");
		assertTrue(iter.hasNext());
		assertEquals("foo", iter.next());
		assertFalse(iter.hasNext());
		
		iter = StringUtils.getOptionIterator(" foo ");
		assertTrue(iter.hasNext());
		assertEquals("foo", iter.next());
		assertFalse(iter.hasNext());
		
		iter = StringUtils.getOptionIterator(" foo  bar  foo   ");
		assertTrue(iter.hasNext());
		assertEquals("foo", iter.next());
		assertTrue(iter.hasNext());
		assertEquals("bar", iter.next());
		assertTrue(iter.hasNext());
		assertEquals("foo", iter.next());
		assertFalse(iter.hasNext());
	}
	@Test
	public void testOptions1() {
		final IntegerVariable v = Choco.makeBooleanVar("b");
		v.addOptions("   foo   bar foo   fou");
		checkOptions(v.getOptions(), 3, 3);
		final SetVariable s = Choco.makeSetVar("s", 0, 18, "foo");
		s.addOptions("    bar   bar    foo    ");
		checkOptions(s.getOptions(), 2, 3);
	}
	
	@Test
	public void testOptions2() {
		final IntegerVariable v = Choco.makeBooleanVar("b");
		final Model m = new CPModel();
		m.addVariable("foo bar   foo bar   bar  bao    foo   ", v);
		checkOptions(v.getOptions(), 3, 3);
		
	}

    @Test
    public void testMultipleModel(){
        Model m1 = new CPModel();
        Model m2 = new CPModel();
        IntegerVariable v1 = Choco.makeIntVar("v1", 0,10);
        IntegerVariable v2 = Choco.makeIntVar("v2", 0,10);
        Constraint c1 = Choco.eq(v1,v2);
        Constraint c2 = Choco.neq(v1,v2);

        m1.addConstraint(c1);
        m2.addConstraint(c2);

        Assert.assertEquals("Nb constraint for v1", 1, v1.getNbConstraint(m1));
        Assert.assertEquals("Nb constraint for v1", 1, v1.getNbConstraint(m2));

        Iterator it = v1.getConstraintIterator(m1);
       assertTrue("iter has next", it.hasNext());
       assertEquals("iter next ", c1, it.next());
       assertFalse("iter next ", it.hasNext());

        it = v1.getConstraintIterator(m2);
       assertTrue("iter has next", it.hasNext());
       assertEquals("iter next ", c2, it.next());
       assertFalse("iter next ", it.hasNext());

        m1.removeConstraint(c1);

       assertEquals("", 0, v1.getNbConstraint(m1));
       assertEquals("", 1, v1.getNbConstraint(m2));

        it = v1.getConstraintIterator(m1);
       assertFalse("iter next ", it.hasNext());

        it = v1.getConstraintIterator(m2);
       assertTrue("iter has next", it.hasNext());
       assertEquals("iter next ", c2, it.next());
       assertFalse("iter next ", it.hasNext());
    }


    /**
     * default goal should detect "no_decision" option and not add default goal over it.
     */
    @Test
    public void testB2987013_1(){
        int[] sizes = new int[]{11,
                10, 10, 10, 10, 10, 10, 10, 10, 10,
                9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
                8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
                7, 7, 7, 7};
        int C = 32;
        int n = sizes.length;
        int nbin = 12;//number of bins
        CPModel m = new CPModel();
        IntegerVariable[] items = Choco.makeIntVarArray("items", n, 0, nbin-1, "cp:enum");
        SetVariable[] setbin = Choco.makeSetVarArray("bins",nbin, 0,n-1, Options.V_NO_DECISION);
        IntegerVariable[] load = Choco.makeIntVarArray("load", nbin, 0,C, "cp:bound");
        IntegerConstantVariable[] size = new IntegerConstantVariable[n];
        for (int i = 0; i < n; i++) {
            size[i] = Choco.constant(sizes[i]);
        }
        m.addConstraint(Choco.pack(new PackModel(items, size, load, setbin), Options.C_PACK_AR, Options.C_PACK_DLB,Options.C_PACK_FB));

        CPSolver s = new CPSolver();
        s.read(m);
        s.setNodeLimit(40);
        s.solve();
        Assert.assertTrue(s.getNodeCount() > 34);
    }

    @Test
    public void testB2987013_2(){
        CPModel model = new CPModel();
        IntegerVariable x = Choco.makeIntVar("x", 1,10, Options.V_NO_DECISION);
        IntegerVariable y = Choco.makeIntVar("y", 1,10, Options.V_NO_DECISION);
        IntegerVariable z = Choco.makeIntVar("z", 1,10, Options.V_NO_DECISION);

        model.addConstraint(Choco.times(x,y,z));


        CPSolver solver = new CPSolver();
        solver.read(model);
        solver.generateSearchStrategy();
        AbstractIntBranchingStrategy branching = solver.getSearchStrategy().mainGoal;
        Assert.assertNull(branching);
    }

    @Test
    public void testB2987013_3(){
        CPSolver solver = new CPSolver();
        IntDomainVar x = solver.createEnumIntVar("x", 1,10);
        IntDomainVar y = solver.createEnumIntVar("y", 1,10);
        IntDomainVar z = solver.createEnumIntVar("z", 1,10);

        solver.post(new TimesXYZ(x,y,z));

        solver.generateSearchStrategy();
        AbstractIntBranchingStrategy branching = solver.getSearchStrategy().mainGoal;
        Assert.assertNotNull(branching);
    }

    @Test
    public void testB2987013_4(){
        CPModel model = new CPModel();
        IntegerVariable x = Choco.makeIntVar("x", 1,10);
        IntegerVariable y = Choco.makeIntVar("y", 1,10);
        IntegerVariable z = Choco.makeIntVar("z", 1,10, Options.V_NO_DECISION);

        model.addConstraint(Choco.times(x,y,z));


        CPSolver solver = new CPSolver();
        solver.read(model);
        solver.generateSearchStrategy();
        AbstractIntBranchingStrategy branching = solver.getSearchStrategy().mainGoal;
        Assert.assertNotNull(branching);
    }

    @Test
    public void testDecisionVars(){
        CPModel model = new CPModel();
        IntegerVariable x = Choco.makeIntVar("x", 1,10);
        IntegerVariable y = Choco.makeIntVar("y", 1,10);
        IntegerVariable z = Choco.makeIntVar("z", 1,10, Options.V_NO_DECISION);

        model.addVariables(x,y,z);

        CPSolver solver = new CPSolver();
        solver.read(model);
        IntDomainVar[] vars = solver.getIntDecisionVars();
        Assert.assertEquals(2, vars.length);
        Assert.assertEquals(solver.getVar(x), vars[0]);
        Assert.assertEquals(solver.getVar(y), vars[1]);
    }

    @Test(expected = ModelException.class)
    public void testDoubleOption(){
        Options.create("foo", 2);
    }
}