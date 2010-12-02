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

package choco.model.constraints.global;

import static choco.Choco.*;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.reified.ReifiedFactory;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 16 mai 2008
 * Time: 10:40:09
 */
public class ReifiedIntConstraintTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    Model m;

    Solver s;

    @Before
    public void before(){
       m = new CPModel();
        s = new CPSolver();
    }

    @After
    public void after(){
        m = null;
        s = null;
    }

	@Test
	public void testSimpleBooleanReification() {
		for (int seed = 0; seed < 20; seed++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable b = makeIntVar("b", 0, 1);
			IntegerVariable y = makeIntVar("y", 1, 10);
			IntegerVariable z = makeIntVar("z", 1, 10);
            m.addVariables(Options.V_BOUND, b, y, z);

            //m.addVariable(b, y, z);
			s.read(m);

			s.post(ReifiedFactory.builder(s.getVar(b), s.lt(s.getVar(y), s.getVar(z)), s));

			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));
			s.solveAll();
			LOGGER.info(""+s.getNbSolutions());
			assertEquals(s.getNbSolutions(),100);
		}
	}

	@Test
	public void testSimpleBooleanReification2() {
		for (int seed = 0; seed < 20; seed++) {
			m = new CPModel();
			s = new CPSolver();
			IntegerVariable b = makeIntVar("b", 0, 1);
			IntegerVariable y = makeIntVar("y", 1, 10);
			IntegerVariable z = makeIntVar("z", 1, 10);
            m.addVariables(Options.V_BOUND, b, y, z);

            m.addVariables(b, y, z);
			s.read(m);

			s.post(ReifiedFactory.builder(s.getVar(b), s.eq(s.getVar(y), s.getVar(z)), s));

			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed + 1));
			s.solve();
			do {
				LOGGER.info(s.getVar(b) + " " + s.getVar(y) + " " + s.getVar(z));
			} while(s.nextSolution() == Boolean.TRUE);
			LOGGER.info(""+s.getNbSolutions());
			assertEquals(s.getNbSolutions(),100);
		}
	}
	

    @Test
    public void test1(){
        IntegerVariable binary = makeIntVar("bin", 0,1);
        IntegerVariable a = makeIntVar("a", 0, 10);
        IntegerVariable b = makeIntVar("b", 0, 10);

        m.addConstraint(reifiedConstraint(binary, leq(a, b)));
        m.addConstraint(lt(b,binary));
        s.read(m);
        s.solveAll();
        assertEquals(s.getNbSolutions(),1);
    }

    @Test
    public void test2(){
        IntegerVariable binary = makeIntVar("bin", 0,1);
        int a = 0;
        IntegerVariable b = makeIntVar("b", 0, 10);

        m.addConstraint(reifiedConstraint(binary, leq(a, b)));
        m.addConstraint(lt(b,binary));
        s.read(m);
        s.solveAll();
        assertEquals(s.getNbSolutions(),1);
    }

    @Test
    public void test3(){
        IntegerVariable binary = makeIntVar("bin", 0,1);
        IntegerVariable a = makeIntVar("a", 0, 10);
        IntegerVariable b = makeIntVar("b", 0, 0);

        m.addConstraint(reifiedConstraint(binary, leq(a, b)));
        m.addConstraint(lt(b,binary));
        s.read(m);
        s.solveAll();

        Model m2 = new CPModel();
        Solver s2 = new CPSolver();

        m2.addConstraint(reifiedConstraint(binary, leq(a, 0)));
        m2.addConstraint(lt(b,binary));
        s2.read(m2);
        s2.solveAll();

        assertEquals(s.getNbSolutions(),s2.getNbSolutions());
    }
}
