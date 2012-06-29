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

package samples.tutorials.puzzles;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.branching.AssignOrForbidIntVarVal;
import choco.cp.solver.search.integer.valselector.MaxVal;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.variables.integer.IntegerVariable;
import org.kohsuke.args4j.Option;
import samples.tutorials.PatternExample;

import java.text.MessageFormat;

import static choco.Choco.*;

/**
 * The Magic Serie problem
 */
public class MagicSerie extends PatternExample {

    @Option(name = "-n", usage = "Order of the magic serie (default : 5)", required = false)
    public int n = 1000;

    protected IntegerVariable[] vars;


    @Override
    public void printDescription() {

        LOGGER.info("A magic sequence of length n is a sequence of integers x0 . . xn-1 between 0 and n-1, ");
        LOGGER.info("such that for all i in 0 to n-1, the number i occurs exactly xi times in the sequence.");
        LOGGER.info("(http://www.csplib.org/)");
        LOGGER.info(MessageFormat.format("Here n = {0}\n\n", n));
    }

    @Override
    public void buildModel() {
        model = new CPModel();
        vars = new IntegerVariable[n];
        for (int i = 0; i < n; i++) {
            vars[i] = makeIntVar("" + i, 0, n - 1, choco.Options.V_BOUND);
        }
        for (int i = 0; i < n; i++) {
            model.addConstraint(occurrence(vars[i], vars, i));
        }
        model.addConstraint(eq(sum(vars), n));     // contrainte redondante 1
        int[] coeff2 = new int[n - 1];
        IntegerVariable[] vs2 = new IntegerVariable[n - 1];
        for (int i = 1; i < n; i++) {
            coeff2[i - 1] = i;
            vs2[i - 1] = vars[i];
        }
        model.addConstraint(eq(scalar(coeff2, vs2), n)); // contrainte redondante 2

    }

    @Override
    public void buildSolver() {
        solver = new CPSolver();
        solver.read(model);
        solver.addGoal(new AssignOrForbidIntVarVal(new StaticVarOrder(solver, solver.getVar(vars)), new MaxVal()));
        ChocoLogging.toSearch();
    }

    @Override
    public void prettyOut() {
        /*if (LOGGER.isLoggable(Level.INFO)) {
            StringBuilder st = new StringBuilder();
            // Print of the solution
            if (solver.existsSolution()) {
                for (int i = 0; i < n; i++) {
                    st.append(MessageFormat.format("{0} ", solver.getVar(vars[i]).getVal()));
                }
            } else st.append("\nno solution to display!");
            LOGGER.info(st.toString());
        }*/
    }

    @Override
    public void solve() {
        solver.solve();
    }


    public static void main(String[] args) {
        new MagicSerie().execute(args);
    }

}

