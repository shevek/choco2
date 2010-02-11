package samples.multicostregular.nsp;

import choco.cp.model.managers.IntConstraintManager;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Dec 8, 2008
 * Time: 11:55:09 PM
 */


public class NSPStruct extends AbstractLargeIntSConstraint {

    public IStateInt[][] need;
    IStateInt nbInstanciated;
    IStateInt[] instPerDay;
    IStateInt[] sumPerDay;
    IStateInt positiveSum;
    public NSPInstance instance;
    private final IEnvironment environment;


    public NSPStruct(IntDomainVar[] vars, NSPInstance instance, IEnvironment environment) {
        super(vars);
        this.environment = environment;
        this.instance = instance;
        this.nbInstanciated = environment.makeInt(0);
        this.positiveSum = environment.makeInt(0);

        this.need = new IStateInt[instance.nbDays][instance.nbShifts];

        this.instPerDay =  new IStateInt[instance.nbDays];
        this.sumPerDay =  new IStateInt[instance.nbDays];

        for (int i = 0 ; i < this.instPerDay.length ; i++)
        {
            this.instPerDay[i] = environment.makeInt(0);
            this.sumPerDay[i] = environment.makeInt(0);
        }


    }

    protected IStateInt getTypeNeed(int nbDays, int shiftType)
    {
        IStateInt tmp = need[nbDays][shiftType] ;
        if (tmp == null)
        {
            tmp = need[nbDays][shiftType] = environment.makeInt(0);
        }
        return tmp;
    }


    public void awakeOnInst(int idx) throws ContradictionException {
        int day = idx%instance.nbDays;
        this.nbInstanciated.add(1);
        IStateInt tmp = getTypeNeed(day,vars[idx].getVal());
        tmp.add(-1);
        if (tmp.get() >=0 )
        {
            this.positiveSum.add(-1);
        }

        for (int i = 0 ; i < instance.nbDays ; i++) {
            for  (int j =0 ; j < instance.nbShifts ; j++)
                System.out.print(getTypeNeed(i,j).get()+"\t");
            System.out.println("");
        }
        System.out.println("");


      /*  if (this.positiveSum.get() > (this.vars.length-this.nbInstanciated.get()))
            this.fail();
        if (this.sumPerDay[day].get() > instance.nbNurses - this.instPerDay[day].get())
            this.fail();*/

    }
    public void awakeOnRemovals(int idx, DisposableIntIterator it){}
    public void awakeOnRem(int idx, int val){}
    public void awakeOnSup(int idx){}
    public void awakeOnInf(int idx){}

    public void propagate() {}


    public void awake() throws ContradictionException
    {
        for (int i =0 ; i < instance.nbDays ;i++)
            for (int j = 0 ;j < instance.nbShifts ;j++)
                getTypeNeed(i,j).set(instance.coverages[i][j]);

        for (int i = 0 ; i < instance.nbDays ;i++)
        {
            for (int j = 0 ; j < instance.nbNurses ; j++)
            {
                if (vars[j*instance.nbDays+i].isInstantiated())
                {
                    this.nbInstanciated.add(1);
                    this.instPerDay[i].add(1);
                    int k =  vars[j*instance.nbDays+i].getVal();
                    getTypeNeed(i,k).add(-1);
                }
            }
        }

        for (int i = 0 ; i < instance.nbDays ;i++)
        {
            for (int j = 0 ; j < instance.nbShifts ; j++)
            {
                int tmp = getTypeNeed(i,j).get();
                if (tmp>0) {
                    this.positiveSum.add(tmp);
                    this.sumPerDay[i].add(tmp);

                }


            }
        }

    }

    public static class NSPStrucManager extends IntConstraintManager
    {

        public SConstraint makeConstraint(Solver solver, IntegerVariable[] variables, Object parameters, Set<String> options) {

            if (parameters instanceof NSPInstance)
            {
                NSPInstance instance = (NSPInstance) parameters;
                return new NSPStruct(solver.getVar(variables),instance, solver.getEnvironment());
            }
            return null;
        }
    }
}
