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
package samples.multicostregular.planner;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.memory.IStateInt;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Jan 22, 2009
 * Time: 2:40:43 PM
 */
public class CenterVarSelec extends AbstractIntVarSelector {


    int center;
    boolean l;
    IStateInt lastr;
    IStateInt lastl;


    public CenterVarSelec(IntDomainVar[] vars)
    {
        this.l = true;
        this.vars = vars;
        center =   this.vars.length/2;
        lastr = vars[0].getSolver().getEnvironment().makeInt(center);
        lastl = vars[0].getSolver().getEnvironment().makeInt(center+1);




    }


    public IntDomainVar selectIntVar() throws ContradictionException
    {
        IntDomainVar out = null;

        if (l)
        {
            l=!l;
            for (int i = lastr.get() ; i >= 0 ; i--)
                if(!vars[i].isInstantiated())
                {
                    lastr.set(i-1);
                    return vars[i];

                }
            for (int i = lastl.get() ; i < vars.length ; i++)
                if(!vars[i].isInstantiated())
                {
                    lastl.set(i+1);
                    return vars[i];
                }

        }
        else        {
            l=!l;            
            for (int i = lastl.get() ; i < vars.length ; i++)
                if(!vars[i].isInstantiated())
                {
                    lastl.set(i+1);
                    return vars[i];
                }
            for (int i =lastr.get() ; i >= 0 ; i--)
                if(!vars[i].isInstantiated())
                {
                    lastr.set(i-1);
                    return vars[i];
                }
        }

        return out;

    }
}