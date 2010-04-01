/* * * * * * * * * * * * * * * * * * * * * * * * * 
 *          _       _                            *
 *         |   (..)  |                           *
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
 *                  N. Jussien    1999-2010      *
 * * * * * * * * * * * * * * * * * * * * * * * * */
package choco.kernel.common.util.iterators;

import choco.kernel.common.IIndex;
import choco.kernel.common.util.objects.DeterministicIndicedList;
import gnu.trove.TIntHashSet;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 1 avr. 2010br/>
 * Since : Choco 2.1.1<br/>
 */
public final class TIHIterator<E extends IIndex> extends DisposableIterator<E> {

    /**
     * The inner class is referenced no earlier (and therefore loaded no earlier by the class loader)
     * than the moment that getInstance() is called.
     * Thus, this solution is thread-safe without requiring special language constructs.
     * see http://en.wikipedia.org/wiki/Singleton_pattern
     */
    private static final class Holder {
        private Holder() {
        }

        private static TIHIterator instance = TIHIterator.build();

        private static void set(final TIHIterator iterator) {
            instance = iterator;
        }
    }

    private int current;
    private int[] indices;
    private DeterministicIndicedList<E> elements;

    private TIHIterator() {
    }

    private static TIHIterator build() {
        return new TIHIterator();
    }

    @SuppressWarnings({"unchecked"})
    public static synchronized <E extends IIndex> TIHIterator <E> getIterator(final TIntHashSet indices,
                                                               final DeterministicIndicedList<E> elements) {
        TIHIterator it = Holder.instance;
        if (!it.isReusable()) {
            it = build();
        }
        it.init(indices, elements);
        return it;
    }

    /**
     * Freeze the iterator, cannot be reused.
     */
    public void init(final TIntHashSet theIndices, final DeterministicIndicedList<E> theElements) {
        super.init();
        current = 0;
        indices = theIndices.toArray();
        elements = theElements;
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    @Override
    public boolean hasNext() {
        return current < indices.length;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @throws java.util.NoSuchElementException
     *          iteration has no more elements.
     */
    @Override
    public E next() {
        return elements.get(indices[current++]);
    }


    /**
     * This method allows to declare that the iterator is not used anymoure. It
     * can be reused by another object.
     */
    @Override
    public void dispose() {
        super.dispose();
        Holder.set(this);
    }
}