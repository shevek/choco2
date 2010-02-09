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
package choco.kernel.memory.copy;

import choco.kernel.memory.IStateVector;

/**
 * Created by IntelliJ IDEA.
 * User: Julien
 * Date: 29 mars 2007
 * Time: 12:22:50
 * To change this template use File | Settings | File Templates.
 */
public final class RcVector<E> implements IStateVector<E>, RecomputableElement {


    /**
     * Contains the elements of the vector.
     */

    private Object[] elementData;


    /**
     * A backtrackable search with the size of the vector.
     */

    private RcInt size;


    /**
     * The current environment.
     */

    private final EnvironmentCopying environment;


    /**
     * Constructs a stored search vector with an initial size, and initial values.
     *
     * @param env The current environment.
     */

    private int timeStamp;

    public RcVector(EnvironmentCopying env) {
        int initialCapacity = MIN_CAPACITY;
        int w = env.getWorldIndex();

        this.environment = env;
        this.elementData = new Object[initialCapacity];
        timeStamp = env.getWorldIndex();
        this.size = new RcInt(env, 0);
        env.add(this);
    }


    public RcVector(int[] entries) {
        // TODO
        throw new UnsupportedOperationException();
    }

    /**
     * Constructs an empty stored search vector.
     * @param env The current environment.
     */



    /**
     * Returns the current size of the stored search vector.
     */

    public int size() {
        return size.get();
    }


    /**
     * Checks if the vector is empty.
     */

    public boolean isEmpty() {
        return (size.get() == 0);
    }

/*    public Object[] toArray() {
        // TODO : voir ci c'est utile
        return new Object[0];
    }*/


    /**
     * Checks if the capacity is great enough, else the capacity
     * is extended.
     *
     * @param minCapacity the necessary capacity.
     */

    public void ensureCapacity(int minCapacity) {
        int oldCapacity = elementData.length;
        if (minCapacity > oldCapacity) {
            Object[] oldData = elementData;
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            elementData = new Object[newCapacity];
            System.arraycopy(oldData, 0, elementData, 0, size.get());
        }
    }


    /**
     * Adds a new search at the end of the vector.
     *
     * @param i The search to add.
     */

    public boolean add(E i) {
        timeStamp = environment.getWorldIndex();
        int newsize = size.get() + 1;
        ensureCapacity(newsize);
        size.set(newsize);
        elementData[newsize - 1] = i;
        return true;
    }

    /**
     * removes the search at the end of the vector.
     * does nothing when called on an empty vector
     */

    public void removeLast() {
        timeStamp = environment.getWorldIndex();
        int newsize = size.get() - 1;
        if (newsize >= 0)
            size.set(newsize);
    }

    /**
     * Returns the <code>index</code>th element of the vector.
     */

    public E get(int index) {
        if (index < size.get() && index >= 0) {
            return (E)elementData[index];
        }
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size.get());
    }


    /**
     * Assigns a new value <code>val</code> to the element <code>index</code>.
     */

    public E set(int index, E val) {
        if (index < size.get() && index >= 0) {
            E oldValue = (E)elementData[index];
            if (val != oldValue) {
                elementData[index] = val;
            }
            timeStamp = environment.getWorldIndex();
            return oldValue;
        }
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size.get());
    }

    public void _set(E[] vals) {
        timeStamp = environment.getWorldIndex();
        System.arraycopy(vals,0,elementData,0,vals.length);
    }

    public void _set(E[] vals, int timeStamp) {
        this.timeStamp = timeStamp;
        System.arraycopy(vals,0,elementData,0,vals.length);
    }

    public E[] deepCopy() {
        Object[] ret = new Object[size.get()];
        System.arraycopy(elementData,0,ret,0,size.get());
        return (E[])ret;
    }

    public int getType() {
        return VECTOR;
    }

    public int getTimeStamp() {
        return timeStamp;
    }
}
