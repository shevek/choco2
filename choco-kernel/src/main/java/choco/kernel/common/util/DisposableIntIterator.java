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

package choco.kernel.common.util;

/**
 *
 * @author grochart
 */
public abstract class DisposableIntIterator implements IntIterator {

    public boolean reusable;

    public void init(){
        reusable = false;
    }

  /**
   * This method allows to declare that the iterator is not usefull anymoure. It 
   * can be reused by another object.
   */
  public void dispose(){
      reusable = true;
  }


//    static List<OneValueIterator> onevalueiterator = new ArrayList(1);
    static OneValueIterator onevalueiterator;

    public static OneValueIterator getOneValueIterator(int value){
        if(onevalueiterator!= null && onevalueiterator.reusable){
            onevalueiterator.init(value);
            return onevalueiterator;
        }
        // Avoid non stop increasing list
//        if(onevalueiterator.size()>8)new OneValueIterator(value);
        
        OneValueIterator iter = new OneValueIterator(value);
//        onevalueiterator.add(iter);
        return iter;
    }


    private static class OneValueIterator extends DisposableIntIterator{

        int value;
        boolean next;

        public OneValueIterator(int value) {
            init(value);
        }

        public void init(int value){
            super.init();
            this.value = value;
            next = true;
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements. (In other
         * words, returns <tt>true</tt> if <tt>next</tt> would return an element
         * rather than throwing an exception.)
         *
         * @return <tt>true</tt> if the getIterator has more elements.
         */
        @Override
        public boolean hasNext() {
            return next;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @throws java.util.NoSuchElementException
         *          iteration has no more elements.
         */
        @Override
        public int next() {
            next = false;
            return value;
        }

        /**
         * Removes from the underlying collection the last element returned by the
         * getIterator (optional operation).  This method can be called only once per
         * call to <tt>next</tt>.  The behavior of an getIterator is unspecified if
         * the underlying collection is modified while the iteration is in
         * progress in any way other than by calling this method.
         *
         * @throws UnsupportedOperationException if the <tt>remove</tt>
         *                                       operation is not supported by this Iterator.
         * @throws IllegalStateException         if the <tt>next</tt> method has not
         *                                       yet been called, or the <tt>remove</tt> method has already
         *                                       been called after the last call to the <tt>next</tt>
         *                                       method.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
