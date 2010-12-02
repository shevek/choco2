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

package choco.kernel.model.constraints.pack;

import static choco.Choco.allDifferent;
import static choco.Choco.constantArray;
import static choco.Choco.eq;
import static choco.Choco.geq;
import static choco.Choco.makeIntVar;
import static choco.Choco.makeIntVarArray;
import static choco.Choco.makeSetVarArray;
import static choco.Options.V_BOUND;
import static choco.Options.V_ENUM;
import static choco.Options.V_NO_DECISION;
import static choco.kernel.common.util.tools.PermutationUtils.applyPermutation;
import static choco.kernel.common.util.tools.PermutationUtils.getIdentity;
import static choco.kernel.common.util.tools.PermutationUtils.getSortingPermuation;
import static choco.kernel.common.util.tools.PermutationUtils.replaceByIdentity;
import static java.lang.System.arraycopy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import choco.kernel.common.util.comparator.IPermutation;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;


/**
 * @author Arnaud Malapert</br>
 * @since 4 déc. 2008 <b>version</b> 2.0.1</br>
 * @version 2.0.1</br>
 */
public final class PackModel {

	public final IPermutation permutation;

	public final IntegerVariable[] bins;

	public final IntegerConstantVariable[] sizes;

	public final IntegerVariable[] loads;

	public final SetVariable[] items;

	public final IntegerVariable nbNonEmpty;

	public final int maxCapacity;

	public PackModel(int[] sizes, int nbBins, int capacity) {
		this(StringUtils.randomName()+'-', constantArray(sizes), nbBins, capacity);
	}
			
	public PackModel(String prefix, IntegerConstantVariable[] sizes, int nbBins, int capacity) {
		super();
		this.bins = makeIntVarArray(prefix+"B", sizes.length, 0, nbBins-1,V_ENUM);
		this.loads = makeIntVarArray(prefix+"L", nbBins, 0, capacity,V_BOUND,V_NO_DECISION);
		this.items = makeSetVarArray(prefix+"S", nbBins, 0, sizes.length - 1, V_BOUND,V_NO_DECISION);
		this.nbNonEmpty = makeIntVar(prefix+"NbNE",0, nbBins,V_BOUND);
		//handle permutation
		permutation = makePermutation(sizes);
		this.sizes= applyPermutation(permutation,sizes);
		this.maxCapacity=capacity;
	}

	public PackModel(IntegerVariable[] bins, IntegerConstantVariable[] sizes, int capacity) {
		super();
		final int nbBins = computeMax(bins);
		final String prefix = StringUtils.randomName()+"-";
		this.loads = makeIntVarArray(prefix+"L", nbBins, 0, capacity,V_BOUND,V_NO_DECISION);
		this.items = makeSetVarArray(prefix+"S", nbBins, 0, sizes.length - 1, V_BOUND,V_NO_DECISION);
		this.nbNonEmpty = makeIntVar(prefix+"NbNE",0, nbBins,V_BOUND);
		//handle permutation
		checkArrays(bins, sizes);
		permutation = makePermutation(sizes);
		this.bins = applyPermutation(permutation, bins);
		this.sizes= applyPermutation(permutation,sizes);
		this.maxCapacity=capacity;
	}	

	public PackModel(IntegerVariable[] bins, IntegerConstantVariable[] sizes, IntegerVariable[] loads) {
		this.loads = loads;
		final String prefix = StringUtils.randomName()+"-";
		this.items = makeSetVarArray(prefix+"S", loads.length, 0, sizes.length - 1, V_BOUND,V_NO_DECISION);
		this.nbNonEmpty = makeIntVar(prefix+"NbNE",0, loads.length,V_BOUND);
		//handle permutation
		checkArrays(bins, sizes);
		permutation = makePermutation(sizes);
		this.bins = applyPermutation(permutation, bins);
		this.sizes= applyPermutation(permutation,sizes);
		this.maxCapacity=computeMax(loads);
	}

	public PackModel(IntegerVariable[] bins,
			IntegerConstantVariable[] sizes, IntegerVariable[] loads,
			SetVariable[] items) {
		this(bins, sizes, loads, items, makeIntVar(StringUtils.randomName()+"-NbNE",0, loads.length,V_BOUND));
	}
	
	public PackModel(IntegerVariable[] bins,
			IntegerConstantVariable[] sizes, IntegerVariable[] loads,
			SetVariable[] items, IntegerVariable nbNonEmpty) {
		super();
		checkArrays(bins, sizes);
		checkArrays(loads, items);
		for (int i = 1; i < sizes.length; i++) {
			if(sizes[i].getValue() > sizes[i-1].getValue()) {
				throw new ModelException("sizes must be sorted according to non increasing order.");
			}
		}
		this.permutation = getIdentity();
		this.bins = bins;
		this.sizes= sizes;
		this.loads = loads;
		this.items = items;
		this.nbNonEmpty = nbNonEmpty;
		this.maxCapacity = computeMax(loads);
	}

	private IPermutation makePermutation(IntegerConstantVariable[] sizes) {
		return replaceByIdentity( getSortingPermuation(sizes,true));
	}
	
	private static int computeMax(IntegerVariable[] vars) {
		int maxCapa = Integer.MIN_VALUE;
		for (IntegerVariable v : vars) {
			final int lm = v.getUppB();
			if(lm > maxCapa) maxCapa = lm;
		}
		return maxCapa;
	}

	
	private void checkArrays(Object[] o1, Object[] o2) {
		if(o1.length!= o2.length) throw new ModelException("Invalid Arrays sizes.");
	}
	

	public final Variable[] getVariables() {
		final int n= getNbItems();
		final int m= getNbBins();
		for (int i = 1; i < n; i++) {
			if(sizes[i].getValue() > sizes[i-1].getValue()) {
				throw new ModelException("sizes must be sorted according to non increasing order.");
			}
		}
		Variable[] vars = new Variable[2*(n+m)+1];
		arraycopy(items, 0, vars, 0, m);
		arraycopy(loads, 0, vars, m, m);
		arraycopy(bins, 0, vars, 2*m, n);
		arraycopy(sizes, 0, vars, 2*m+n,n);
		vars[vars.length-1]=nbNonEmpty;
		return vars;
	}
	
	public final int getNbBins() {
		return loads.length;
	}

	public final int getNbItems() {
		return bins.length;
	}


	public final IntegerVariable[] getBins() {
		return bins;
	}

	public final IntegerConstantVariable[] getSizes() {
		return sizes;
	}

	public final SetVariable[] getItems() {
		return items;
	}

	public final IntegerVariable[] getLoads() {
		return loads;
	}

	public final int getMaxCapacity() {
		return maxCapacity;
	}

	/**
	 * Symmetry Breaking : equal-sized items are ordered according to their indices.
	 * If the relation sizes[i] = sizes[i+1] holds, then it states the constraint bins[i] <= bins[i+1].
	 */
	public Constraint[] orderEqualSizedItems(int fromIndex) {
		final List<Constraint> cstr = new ArrayList<Constraint>();
		int oldS = -1;
		int idx = fromIndex;
		while(idx< bins.length) {
			final int newS = sizes[idx].getValue();
			if(newS==oldS) {cstr.add(geq(bins[idx],bins[idx-1]));}
			else {oldS=newS;}
			idx++;
		}
		return cstr.toArray(new Constraint[cstr.size()]);
	}


	private boolean isLargeItem(int idx) {
		return idx < bins.length && sizes[idx].getValue()> maxCapacity/2;	
	}

	private boolean isAdditionalLargeItem(int idx) {
		//the capacity is even and the size of the item is C/2.
		//So, for any previous items s_i + s_j >= C/2 (C/2+1) >= C+1
		return idx < bins.length && 
		sizes[idx].getValue() == maxCapacity/2 && 
		maxCapacity % 2 == 0;
	}

	/**
	 * Symmetry Breaking : pack the k-th largest items into the first bins.
	 * k = max{ q | sizes[q] + sizes[q+1] > maxCapacity} where the sizes are sorted according to decreasing sizes.
	 */
	public final Constraint[] packLargeItems() {
		final List<Constraint> cstr = new ArrayList<Constraint>();
		int nbP=0;
		while( isLargeItem(nbP)) {
			cstr.add( eq(bins[nbP],nbP));
			nbP++;
		}
		if( isAdditionalLargeItem(nbP)) {
			cstr.add( eq(bins[nbP],nbP));
			nbP++;
		}
		return cstr.toArray(new Constraint[cstr.size()]);
	}


	/**
	 * Redundant Constraint :  allDifferent on the k-th largest items
	 * @see PackModel#packLargeItems()
	 */
	public final Constraint allDiffLargeItems() {
		final List<IntegerVariable> vars= new LinkedList<IntegerVariable>();
		int nbP=0;
		while( isLargeItem(nbP)) {
			vars.add( bins[nbP++]);
		}
		if( isAdditionalLargeItem(nbP)) {
			vars.add( bins[nbP++]);
		}
		return allDifferent(vars.toArray(new IntegerVariable[nbP]));
	}

	/**
	 * Symmetry Breaking: bins are sorted according to non increasing loads. 
	 */
	public final Constraint[] decreasingLoads(int fromIndex) {
		return symBreakDecreasingOrder(loads, fromIndex);
	}

	/**
	 * Symmetry Breaking: bins are sorted according to non increasing cardinalities. 
	 */
	public final Constraint[] decreasingCardinalities(int fromIndex) {
		return symBreakDecreasingOrder(VariableUtils.getCardinalities(items), fromIndex);
	}

	private final static Constraint[] symBreakDecreasingOrder(IntegerVariable[] vars,int fromIndex) {
		final int nb = vars.length - fromIndex - 1;
		if(nb>0) {
			final Constraint[] cstr= new Constraint[nb];
			for (int i = fromIndex; i < vars.length-1; i++) {
				cstr[i-fromIndex] = geq(vars[i], vars[i+1]);
			}
			return cstr;
		} else {return null;}
	}



}
