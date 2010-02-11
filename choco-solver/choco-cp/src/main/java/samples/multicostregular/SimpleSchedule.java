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
package samples.multicostregular;

import static choco.Choco.*;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.constraints.automaton.FA.Automaton;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * This class present a simple scheduling problem defined in multicost-regular documentation.
 * It consists on finding a minimal cost schedule for a person with some work regulations
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Jul 6, 2009
 * Time: 7:43:05 AM
 */
public class SimpleSchedule extends CPModel
{
    private static int DAY = 0;
    private static int NIGHT = 1;
    private static int REST = 2;

    private static String[] map = new String[]{"Day","Night","Rest"};

    /**
     * Model variable for the
     */
    IntegerVariable[] sequence;

    /**
     * Bounds within whoms the accepted schedule must cost.
     */
    IntegerVariable[] bounds;

    /**
     * The cost matrix which gives assgnement cost (used for counters too)
     */
    int[][][][] costMatrix;

    /**
     * Automaton which embeds the work regulations that may be
     * represented by regular expressions
     */
    Automaton auto;


    /**
     * Simple Constructor for the simple schedule model
     */
    public SimpleSchedule()
    {
        super();
        this.makeModel();
    }


    /**
     * Construct the variables needed for the 14 day schedule
     */
    private void makeVariables()
    {
        this.sequence = makeIntVarArray("x",14,0,2,"cp:enum");
        this.bounds =  new IntegerVariable[4];
        this.bounds[0] = makeIntVar("z_0",30,80,"cp:bound");
        this.bounds[1] = makeIntVar("day",0,7,"cp:bound");
        this.bounds[2] = makeIntVar("night",3,7,"cp:bound");
        this.bounds[3] = makeIntVar("rest",7,9,"cp:bound");

    }

    /**
     * make Cost Matrix that embeds financial cost and counters
     */
    private void makeCostMatrix()
    {
        int[][][][] csts = new int[14][3][this.auto.getNbStates()][4];
        for (int i = 0 ; i < csts.length ; i++)
        {
            for (int j = 0 ;j < csts[i].length ; j++)
            {
                for (int s = 0 ; s < csts[i][j].length ; s++)
                {
                    if (s == 0)
                    {
                        if (j == DAY)
                            csts[i][j][s] = new int[]{3,1,0,1};
                        else if (j == NIGHT)
                            csts[i][j][s] = new int[]{8,0,1,1};
                    }
                    else if (s == 1)
                    {
                        if (j == DAY)
                            csts[i][j][s] = new int[]{5,1,0,1};
                        else if (j == NIGHT)
                            csts[i][j][s] = new int[]{9,0,1,1};
                    }
                    else if (s == 2)
                        if (j == REST)
                            csts[i][j][s] = new int[]{2,0,0,0};

                }
            }
        }
        this.costMatrix = csts;

    }


    /**
     * Construct the automaton that allows patterns describe in multicost-regular doc.
     */
    private void makeAutomaton()
    {
        this.auto = new Automaton();
        int idx = this.auto.addState();
        this.auto.setStartingState(idx);
        this.auto.setAcceptingState(idx);
        idx = this.auto.addState();
        this.auto.addTransition(this.auto.getStartingState(),idx,DAY);
        int next = this.auto.addState();
        this.auto.addTransition(idx,next,new int[]{DAY,NIGHT});
        this.auto.addTransition(next,auto.getStartingState(),REST);
        auto.addTransition(auto.getStartingState(),next,NIGHT);

    }


    /**
     * build the model
     */
    public void makeModel()
    {
        this.makeVariables();
        this.makeAutomaton();
        this.makeCostMatrix();


        this.addVariables(sequence);
        this.addVariables(bounds);
        this.addConstraint(multiCostRegular(sequence,bounds,auto,costMatrix));
    }



    /**
     * Print a schedule once the model is solved by the given solver
     * @param s the CPSolver that solved this model
     */
    public void printSolution(CPSolver s)
    {
        StringBuffer b = new StringBuffer("[");
        for (IntegerVariable v : sequence)
            b.append(map[s.getVar(v).getVal()]).append("-");
        b.deleteCharAt(b.length()-1);
        b.append("]");
        System.out.println("Schedule: "+b);
        System.out.println("Cost: "+s.getVar(bounds[0]).getVal());
        System.out.println("Nb Days: "+s.getVar(bounds[1]).getVal());
        System.out.println("Nb Nights: "+s.getVar(bounds[2]).getVal());
        System.out.println("Nb Rests: "+(14-s.getVar(bounds[3]).getVal()));


    }

    public IntegerVariable getCostVariable()
    {
        return bounds[0];
    }



    public static void main(String[] args)
    {
     

        SimpleSchedule m = new SimpleSchedule();
        CPSolver s = new CPSolver();

        s.read(m);
        IntDomainVar z = s.getVar(m.getCostVariable());



        if (s.solve())
        {
           do {
               m.printSolution(s);
               System.out.println("");
               s.postCut(s.gt(z,z.getVal()));
           }
           while(s.nextSolution());
        }

        s.printRuntimeStatistics();
        System.out.println(s.getNbSolutions()+"[+0] solutions");
    }
}