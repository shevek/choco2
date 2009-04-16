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
package samples.seminar.tsp;

import static choco.Choco.*;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import gnu.trove.TIntArrayList;

import java.util.Random;

public class CycleMain {

    public static int maxValue = 100000;

    private static enum SolveType {
        PROPAGATE, SOLVE, SOLVEALL, MINIMIZE
    }

    protected int n;
    protected IntegerVariable objective;
    protected Model model;
    protected Solver solver;
    protected int[][] matrix;

    public CycleMain(int n, int[][] matrix) {
        this.n = n;
        this.matrix = matrix;
        this.model = new CPModel();
        this.solver = new CPSolver();
        this.objective = makeIntVar("cost", 0, maxValue);
        this.model.addVariable("cp:bound", this.objective);
    }

    public static void main(String[] args) {
        int n;
        int[][] matrix;
        // toy example
        if (args.length > 0 && "provided".equals(args[0])) {
            n = 10;
            matrix = instance(n);
        }
        // random gen
        else if (args.length > 0 && "random".equals(args[0])) {
            n = 10;
            int maxDist = 100;
            Random r = new Random();
            int seed = r.nextInt();
            System.out.println("seed = " + seed);
            matrix = randomInstance(seed, n, maxDist);
        }
        // default random
        else {
            n = 10;
            int maxDist = 100;
            int seed = 123456;
            matrix = randomInstance(seed, n, maxDist);
        }
        CycleMain ham = new CycleMain(n, matrix);
        ham.hamiltonianCylceProblem(true);
    }

    public void solveProblem(SolveType type, IntegerVariable[] vars) {
        solver.read(model);
        solver.setVarIntSelector(new MyVarSelector(solver.getVar(objective),
                solver.getVar(vars), 0, n - 1));
        solver.setValIntSelector(new MyValSelector(solver.getVar(objective),
                solver.getVar(vars), matrix, 0, n - 1));
        solver.setLoggingMaxDepth(50);
        System.out.println("debut de la resolution");
        switch (type) {
        case PROPAGATE:
            try {
                solver.propagate();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            break;
        case SOLVE:
            solver.solve();
            break;
        case SOLVEALL:
            solver.solveAll();
            break;
        case MINIMIZE:
            solver.minimize(solver.getVar(objective), true);
            break;
        }
        System.out.println("==========================");
    }

    public void hamiltonianCylceProblem(boolean tsp) {
        // One variable associated with each city, 0 denotes the starting node
        // and n-1 denotes the final node.
        // Each variable s[i] denotes the direct successor of the node
        // associated with s[i] in the tour.
        // In the case of TSP, the unique depot is splitted into an origin-depot
        // (0) and a destination-depot (n-1).
        IntegerVariable[] s = new IntegerVariable[n];
        // on ajuste les domaines avec la matrice definissant le graphe
        for (int i = 0; i < n; i++) {
            TIntArrayList domain = new TIntArrayList();
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] < maxValue)
                    domain.add(j);
            }
            s[i] = makeIntVar("s_" + i, domain);
        }
        model.addVariables(s);
        // s[0] != n-1 => we have to reach at least one node distinct from the
        // depot
        Constraint notWareHouse = neq(s[0], n - 1);
        model.addConstraint(notWareHouse);
        // s[n-1] = 0 => nodes 0 and n-1 denotes the depot
        Constraint wareHouse = eq(s[n - 1], 0);
        model.addConstraint(wareHouse);
        // For each pair of variables (s[i],s[j]), s[i] != s[j]
        Constraint allDiff = allDifferent(s);
        model.addConstraint(allDiff);
        // Sub-tour constraint s[i] = j, i != n-1 => s[end[j]] != k in inPath[i]
        // Associated to each varaible s[i], a value end[i] and a bitset
        // inPath[i] that respectively denote the end
        // and the current required path reaching i.
        model.addConstraint(new ComponentConstraint(
                SubTourConstraint.SubTourConstraintManager.class, null, s));
        // solve
        if (!tsp) {
            solveProblem(SolveType.SOLVEALL, s);
        } else {
            IntegerVariable[] c = new IntegerVariable[n];
            Constraint[] elements = new Constraint[n];
            for (int i = 0; i < n; i++) {
                c[i] = makeIntVar("cost_" + i, 0, Integer.MAX_VALUE);
                model.addVariable("cp:bound", c[i]);
                elements[i] = choco.Choco.nth(s[i], matrix[i], c[i]);
            }
            model.addConstraints(elements);
            // somme des couts inferieure a l'objectif
            Constraint totalCost = leq(sum(c), objective);
            model.addConstraint(totalCost);

            // Evaluation et filtrage autour d'une borne inferieure evaluee par
            // l'arpm
            IntegerVariable[] decision = new IntegerVariable[n + 1];
            System.arraycopy(s, 0, decision, 0, n);
            decision[n] = objective;
            model.addConstraint(new ComponentConstraint(
                    MinSpanningTree.MinSpanningTreeManager.class,
                    new Object[] { matrix }, decision));
            solveProblem(SolveType.MINIMIZE, decision);
        }
    }

    public static int[][] randomInstance(int seed, int size, int maxDist) {
        Random rand = new Random(seed);
        Generator gen = new Generator(rand, size, maxDist);
        return gen.generateMatrix();
    }

    public static int[][] instance(int n) {
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = maxValue; // default: the arc does not exist
            }
        }

        // instantiated arcs
        matrix[0][4] = 1;
        matrix[4][2] = 1;
        matrix[5][6] = 1;
        matrix[6][1] = 1;
        matrix[7][9] = 1;
        matrix[9][0] = 1;

        // possibles arcs
        matrix[1][3] = 1;
        matrix[1][4] = 1;
        matrix[1][5] = 1;
        matrix[1][7] = 1;
        matrix[1][8] = 1;

        matrix[2][0] = 1;
        matrix[2][3] = 1;
        matrix[2][5] = 1;
        matrix[2][8] = 1;

        matrix[3][1] = 1;
        matrix[3][3] = 1;
        matrix[3][7] = 1;
        matrix[3][8] = 1;

        matrix[8][3] = 1;
        matrix[8][5] = 1;
        matrix[8][6] = 1;

        return matrix;
    }

}
