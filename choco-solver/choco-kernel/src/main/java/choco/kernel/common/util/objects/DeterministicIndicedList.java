/* ************************************************
 *           _       _                            *
 *          |  °(..)  |                           *
 *          |_  J||L _|        CHOCO solver       *
 *                                                *
 *     Choco is a java library for constraint     *
 *     satisfaction problems (CSP), constraint    *
 *     programming (CP) and explanation-based     *
 *     constraint solving (e-CP). It is built     *
 *     on a event-based propagation mechanism     *
 *     with backtrackable structures.             *
 *                                                *
 *     Choco is an open-source software,          *
 *     distributed under a BSD licence            *
 *     and hosted by sourceforge.net              *
 *                                                *
 *     + website : http://choco.emn.fr            *
 *     + support : choco@emn.fr                   *
 *                                                *
 *     Copyright (C) F. Laburthe,                 *
 *                   N. Jussien    1999-2008      *
 **************************************************/
package choco.kernel.common.util.objects;

import choco.kernel.common.IIndex;
import choco.kernel.common.util.iterators.ArrayIterator;
import choco.kernel.common.util.iterators.DisposableIterator;
import gnu.trove.TLongIntHashMap;

import java.io.Serializable;
import static java.lang.reflect.Array.newInstance;
import java.util.Arrays;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 11 juin 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*
* Structure created to recorded data of the model.
* It includes an array of indiced objects,
* a hashmap of indices.
*
* It allows deterministic iteration.
*/
public class DeterministicIndicedList<O extends IIndex> implements Serializable{

    private final Class clazz;

    /**
     * Speed hash set of constraint indices
     */
    private final TLongIntHashMap indices;

    /**
     * All the object
     */
    private O[] objects;

    /**
     * indice of the last object
     */
    private int last;

    /**
     * Constructor
     * @param clazz the super Class of include object
     * @param initialSize initial size of the structure
     */
    public DeterministicIndicedList(final Class clazz, final int initialSize) {
        this.clazz = clazz;
        indices = new TLongIntHashMap(initialSize);
        objects = (O[]) newInstance(clazz, initialSize);
        last = 0;
    }

    /**
     * Constructor
     * @param clazz the super Class of include object
     */
    public DeterministicIndicedList(final Class clazz) {
        this(clazz, 32);
    }

    public void clear(){
        indices.clear();
        Arrays.fill(objects, null);
        objects = null;
    }

    /**
     * Add object to the structure
     * @param object
     */
    public void add(final O object){
        if(!indices.containsKey(object.getIndex())){
            ensureCapacity();
            objects[last] = object;
            indices.put(object.getIndex(), last++);
        }
    }

    /**
     * Ensure that the array has a correct size
     */
    @SuppressWarnings({"unchecked"})
    private void ensureCapacity(){
        if(last >= objects.length){
            // treat the case where intial value = 1
            final int cindT = objects.length * 3/2+1;
            final O[] oldObjects = objects;
            objects = (O[]) newInstance(clazz, cindT);
            System.arraycopy(oldObjects, 0, objects, 0, last);
        }
    }


    /**
     * Remove object from the structure
     * We just swap the last object and the removed object 
     * @param object to remove
     */
    public int remove(final O object){
        if(indices.containsKey(object.getIndex())){
            final int ind = indices.get(object.getIndex());
            indices.remove(object.getIndex());
            if(last > 0 && ind < last-1){
                objects[ind] = objects[last-1];
                indices.adjustValue(objects[ind].getIndex(), -last+ind+1);
            }
            objects[--last] = null;
            return ind;
        }
        return -1;
    }

    /**
     * Indicates wether the structure contains the object
     * @param object
     * @return
     */
    public boolean contains(final O object){
        return indices.containsKey(object.getIndex());
    }

    /**
     * Get the number of objects contained
     * @return
     */
    public int size(){
        return last;
    }

    /**
     * Get the object in position i
     * @param i position of the object
     * @return the ith object
     */
    public O get(final int i){
        return objects[i];
    }

    /**
     * Get the position of the object
     * @param object required
     * @return its position
     */
    public int get(final O object){
        return indices.get(object.getIndex());
    }


    public O getLast(){
        if(last>0){
            return objects[last-1];
        }else{
            return null;
        }
    }

    /**
     * Iterator over objects
     * @return
     */
    public DisposableIterator<O> iterator(){
        return ArrayIterator.getIterator(objects, last);
    }
}
