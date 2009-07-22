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
package choco.memory;

import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.recomputation.EnvironmentRecomputation;
import choco.kernel.model.Model;
import choco.kernel.solver.Solver;
import org.junit.*;
import static samples.seminar.ExDonaldGeraldRobert.*;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 13 oct. 2008
 * Time: 10:58:27
 * To change this template use File | Settings | File Templates.
 */
public class RecomputationTest {

    IEnvironment env;

        @Before
        public void before(){
            env = new EnvironmentRecomputation();
        }

        @After
        public void after(){
            env = null;
        }

        @Test
        @Ignore
        public void donaldGeraldRobert(){
           //ChocoLogging.setVerbosity(Verbosity.SOLUTION);
        	Model m = modelIt1();
            Solver s = new CPSolver(env);
            Solver _s = new CPSolver();
            // Read the model
            s.read(m);
            _s.read(m);

            // Then solve it
            s.solve();
            //ChocoLogging.flushLogs();
            _s.solve();
            //ChocoLogging.flushLogs();
            // Print name value
            Assert.assertEquals("donald is not equal",_s.getVar(_donald).getVal(),s.getVar(_donald).getVal());
            Assert.assertEquals("gerald is not equal",_s.getVar(_gerald).getVal(),s.getVar(_gerald).getVal());
            Assert.assertEquals("robert is not equal",_s.getVar(_robert).getVal(),s.getVar(_robert).getVal());
        }
        
        


}

