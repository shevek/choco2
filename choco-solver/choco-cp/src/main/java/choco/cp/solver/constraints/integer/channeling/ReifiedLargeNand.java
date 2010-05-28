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
package choco.cp.solver.constraints.integer.channeling;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 18 mai 2010<br/>
 * Since : Choco 2.1.1<br/>
 *
 * A constraint that ensures :
 * b = x1 NAND x2 ... NAND xn
 * where b, and x1,... xn are boolean variables (of domain {0,1})
 */
public final class ReifiedLargeNand extends AbstractLargeIntSConstraint {

    /**
     * Nb literals set to 1 (true).
     */
    private final IStateInt toONE;


    /**
     * A constraint to ensure :
     * b = OR_{i} vars[i]
     *
     * @param vars
     * @param environment
     */
    public ReifiedLargeNand(IntDomainVar[] vars, IEnvironment environment) {
        super(vars);
        toONE = environment.makeInt(0);
    }

    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK;
    }

    public void propagate() throws ContradictionException {
        if(vars[0].isInstantiatedTo(0)){
            for(int i = 1 ; i < vars.length; i++){
                vars[i].instantiate(1, this, false);
            }
            setEntailed();
        }else{
            int toONE = 0;
            int lastIdx = 0;
            for(int i = 1; i < vars.length; i++){
                if(vars[i].isInstantiatedTo(0)){
                    vars[0].instantiate(1, this, false);
                    setEntailed();
                    return;
                }else if(vars[i].isInstantiatedTo(1)){
                    toONE++;
                }else{
                    lastIdx = i;
                }
            }
            if(toONE == vars.length-1){
                vars[0].instantiate(0, this, false);
                setEntailed();
                return;
            }else if((toONE == vars.length - 2)
                    && (vars[0].isInstantiatedTo(1))){
                vars[lastIdx].instantiate(0, this, false);
                setEntailed();
                return;
            }
            this.toONE.set(toONE);

        }
    }

    @Override
    public void awakeOnInst(int idx) throws ContradictionException {
        int val = vars[idx].getVal();
        switch (idx){
            case 0:
                switch (val){
                    case 0:
                        for(int i = 1 ; i < vars.length; i++){
                            vars[i].instantiate(1, this, false);
                        }
                        setEntailed();
                        break;
                    case 1:
                        if(toONE.get()>= vars.length-2){
                            filter();
                        }
                        break;
                }
                break;
            default:
                switch (val){
                    case 0:
                        vars[0].instantiate(1, this, false);
                        setEntailed();
                        break;
                    case 1:
                        toONE.add(1);
                        // traitement de bool = 1 et 1 var inconnue
                        if(toONE.get()>= vars.length-2){
                            filter();
                        }
                        break;
                }
                break;
        }

    }

    private void filter() throws ContradictionException {
        int toONE = this.toONE.get();
        int n = vars.length-1;
        if(toONE == n){
            vars[0].instantiate(0, this, false);
            setEntailed();
        }else if(vars[0].isInstantiatedTo(1)){
            for(int i = n; i > 0; i--){
                if(!vars[i].isInstantiated()){
                    vars[i].instantiate(0, this, false);
                    setEntailed();
                    break;
                }
                // speed up
                else if(!vars[n+1-i].isInstantiated()){
                    vars[n+1-i].instantiate(0, this, false);
                    setEntailed();
                    break;
                }
            }
        }
    }

    public void awakeOnInf(int varIdx) throws ContradictionException {
    }

    public void awakeOnSup(int varIdx) throws ContradictionException {
    }

    public void awakeOnBounds(int varIndex) throws ContradictionException {
    }

    public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
    }


    public boolean isSatisfied(int[] tuple) {
        if (tuple[0] == 0) {
            for (int i = 1; i < tuple.length; i++) {
                if (tuple[i] != 1) return false;
            }
            return true;
        } else {
            for (int i = 1; i < tuple.length; i++) {
                if (tuple[i] == 0) return true;
            }
            return false;
        }
    }

    public Boolean isEntailed() {
        if(vars[0].isInstantiated()){
            int val = vars[0].getVal();
            if(val==0){
                for (int i = 1; i < vars.length; i++) {
                   if (vars[i].isInstantiatedTo(0)){
                        return Boolean.FALSE;
                   }
                }
                return Boolean.TRUE;
            }else{
                for (int i = 1; i < vars.length; i++) {
                   if (vars[i].isInstantiatedTo(0)){
                        return Boolean.TRUE;
                   }
                }
                return Boolean.FALSE;
            }
        }
        return null;
    }

}