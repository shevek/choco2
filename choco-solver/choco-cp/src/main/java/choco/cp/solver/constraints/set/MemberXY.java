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

package choco.cp.solver.constraints.set;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.cp.solver.variables.set.SetVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.set.AbstractBinSetIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;

// **************************************************
// *                   J-CHOCO                      *
// *   Copyright (C) F. Laburthe, 1999-2003         *
// **************************************************
// *  an open-source Constraint Programming Kernel  *
// *     for Research and Education                 *
// **************************************************

/**
 * Ensure that an int variable belongs to a set variable
 */
public final class MemberXY extends AbstractBinSetIntSConstraint {


	public MemberXY(SetVar set, IntDomainVar iv) {
		super(iv, set);
	}

    @Override
    public int getFilteredEventMask(int idx) {
        if(idx == 0){
            return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK + IntVarEvent.REMVAL_MASK;
        }
        return SetVarEvent.REMENV_MASK + SetVarEvent.INSTSET_MASK;
    }

    public void filter() throws ContradictionException {
		DisposableIntIterator it = v0.getDomain().getIterator();
		int count = 0, val = Integer.MAX_VALUE;
		while (it.hasNext()) {
			val = it.next();
			if (v1.isInDomainEnveloppe(val)) {
				count += 1;
				if (count > 1) break;
			}
		}
        it.dispose();
		if (count == 0)
			this.fail();
		else if (count == 1) {
			v0.instantiate(val, this, false);
			v1.addToKernel(val, this, false);
		}
	}

	public void awakeOnInf(int idx) throws ContradictionException {
		filter();
	}

	public void awakeOnSup(int idx) throws ContradictionException {
		filter();
	}

	//TODO : Store the number of values shared by the Int and the Set domain
	public void awakeOnRem(int idx, int x) throws ContradictionException {
		filter();
	}

	public void awakeOnEnv(int varIdx, int x) throws ContradictionException {
		v0.removeVal(x, this, false);
		filter();
	}

	public void awakeOnInst(int varIdx) throws ContradictionException {
		if (varIdx == 0)
			v1.addToKernel(v0.getVal(), this, false);
		else
			filter();
	}


	public void propagate() throws ContradictionException {
		DisposableIntIterator it = v0.getDomain().getIterator();
        try{
            while (it.hasNext()) {
                int val = it.next();
                if (!v1.isInDomainEnveloppe(val)) {
                    v0.removeVal(val, this, false);
                }
            }
        }finally {
            it.dispose();
        }
		filter();
	}

	public boolean isSatisfied() {
		return v1.isInDomainKernel(v0.getVal());
	}

	public boolean isConsistent() {
		DisposableIntIterator it = v0.getDomain().getIterator();
		while (it.hasNext()) {
			if (!v1.isInDomainKernel(it.next())){
                it.dispose();
                return false;
            }
		}
        it.dispose();
		return true;
	}

	public String toString() {
		return v0 + " is in " + v1;
	}

	public String pretty() {
		return v0.pretty() + " is in " + v1.pretty();
	}


	public Boolean isEntailed() {
		boolean allInKernel = true;
        boolean allOutEnv = true;
        DisposableIntIterator it = v0.getDomain().getIterator();
        while(it.hasNext()){
            int val = it.next();
            if(!v1.isInDomainKernel(val)){
                allInKernel = false;
            }
            if(v1.isInDomainEnveloppe(val)){
                allOutEnv = false;
            }
        }
        it.dispose();
        if(allInKernel){
            return Boolean.TRUE;
        }else if(allOutEnv){
            return Boolean.FALSE;
        }
        return null;
	}
}
