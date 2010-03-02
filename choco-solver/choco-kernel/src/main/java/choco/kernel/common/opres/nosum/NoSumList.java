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
package choco.kernel.common.opres.nosum;

import gnu.trove.TLinkedList;

import java.util.ListIterator;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;

/**
 * A class mainly used in Pack Constraint. 
 * The algorithm checks if there is a sum of the candidate values between the given bounds.
 *
 * @author Arnaud Malapert
 */
public class NoSumList {

	/** The candidate load. */
	private int cLoad;

	/** The required load. */
	private int rLoad;
	
	private INoSumCell[] candidatesMap;
	
	private final TLinkedList<INoSumCell> candidatesList = new TLinkedList<INoSumCell>();
	

	/**
	 * The Constructor.We assume that the items are sorted in non increasing order.
	 *
	 * @param sizes
	 */
	public NoSumList(final INoSumCell[] candidates) {
		super();
		this.candidatesMap = candidates;
	}
	
	public NoSumList(final IntDomainVar[] candidates) {
		super();
		this.candidatesMap = new INoSumCell[candidates.length];
		for (int i = 0; i < candidates.length; i++) {
			candidatesMap[i] = new NoSumCell(i, candidates[i]);
		}
	}
	
	public NoSumList(final int[] candidates) {
		super();
		this.candidatesMap = new INoSumCell[candidates.length];
		for (int i = 0; i < candidates.length; i++) {
			candidatesMap[i] = new NoSumCell(i, candidates[i]);
		}
	}

	
	
	public final void clear() {
		rLoad=0;
		cLoad=0;
		candidatesList.clear();
	}
	
	public final int getNbCandidates() {
		return candidatesList.size();
	}

	public final void fillCandidates() {
		clear();
		for (int i = 0; i < candidatesMap.length; i++) {
			candidatesList.add(candidatesMap[i]);
			cLoad+=candidatesMap[i].getVal();
		}

	}
	
	public final void setCandidatesFromVar(final SetVar svar) {
		clear();
		DisposableIntIterator iter=svar.getDomain().getKernelIterator();
		while( iter.hasNext()) {
			rLoad+=candidatesMap[iter.next()].getVal();		
		}
		iter.dispose();
		iter=svar.getDomain().getOpenDomainIterator();
		while(iter.hasNext()) {
			final int item=iter.next();
			candidatesList.add(candidatesMap[item]);
			cLoad+=candidatesMap[item].getVal();
		}
		iter.dispose();
	}


	/**
	 * Gets the candidates load.
	 *
	 * @return the candidates load
	 */
	public final int getCandidatesLoad() {
		return cLoad;
	}

	/**
	 * Gets the required load.
	 *
	 */
	public final int getRequiredLoad() {
		return rLoad;
	}

	/**
	 * Gets the candidates load.
	 *
	 * @return the candidates load
	 */
	public final int getMaximumLoad() {
		return rLoad + cLoad;
	}
	
	public final void remove(final int item) {
		if(candidatesList.remove( candidatesMap[item])) {
		cLoad-= candidatesMap[item].getVal();
		}
	}
	
	public final void pack(final int item) {
		remove(item);
		packRemoved(item);
	}

	public final ListIterator<INoSumCell> listIterator() {
		return candidatesList.listIterator();
	}
	
	public final void remove(ListIterator<INoSumCell> iter, final int item) {
		iter.remove();
		cLoad -= candidatesMap[item].getVal();
	}
	
	public final void pack(ListIterator<INoSumCell> iter, final int item) {
		remove(iter, item);
		packRemoved(item);
	}
	
	public final void undoRemove(ListIterator<INoSumCell> iter, final int item) {
		iter.add(candidatesMap[item]);
		cLoad += candidatesMap[item].getVal();
	}
	
	public final void packRemoved(final int item) {
		rLoad += candidatesMap[item].getVal();
	}
	
	


	/**
	 * No sum indicates the existence of a subset of candidate items which has a load between the parameters.
	 * <br><b>This function can return false negative.</b>
	 *
	 * @param alpha the minimum load expected
	 * @param beta the maximum load expected
	 *
	 * @return <code>true</code> if it do not exist a subset of candidate items which has a load between alpha and beta.
	 */
	public boolean noSum(final int alpha,final int beta) {
		if(alpha<=0 || beta>= getCandidatesLoad()) {return false;}
		else if(this.getCandidatesLoad()<alpha) {return true;}
		int sa=0,sb=0,sc=0;
		INoSumCell k1= candidatesList.getFirst();
		INoSumCell k2= candidatesList.getLast();
		int cpt=0;
		while(sc+ k2.getVal()<alpha) {
			sc+= k2.getVal();
			k2= candidatesList.getPrevious(k2);
		}
		sb= k2.getVal();
		while(sa<alpha && sb<=beta) {
			sa+= k1.getVal();
			//sa est croissante => s_0>=alpha pour ne pas lancer l'algorithme : ca evite la première boucle
			k1= candidatesList.getNext(k1);
			cpt++;
			if(sa<alpha) {
				k2= candidatesList.getNext(k2);
				sb+=k2.getVal();
				sc-=k2.getVal();
				while(sa+sc>=alpha) {
					k2=candidatesList.getNext(k2);
					sc-=k2.getVal();
					sb+=k2.getVal();
					INoSumCell k3=k2;
					for (int i = 0; i <= cpt; i++) {
						//FIXME a mon avis y a un bug en perspective ici
						k3= candidatesList.getPrevious(k3);
					}
					sb-=k3.getVal();
				}
			}
		}
		return sa<alpha;
	}
}




