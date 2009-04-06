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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements a priority aware queue (FIFO structure).
 */
public class PriorityQueue {
  private static Logger logger = Logger.getLogger("choco");

  /**
   * The number of available priority levels.
   */

  private final int levelNb;


  /**
   * The last element of each priority level.
   */

  private Entry[] levelLast;


  /**
   * The header element: an element for helping implementation.
   */

  private Entry header;


  /**
   * Maps the objects to their entry in the chained list.
   */

  private HashMap map;


  /**
   * The size of the queue.
   */

  private int size;


  /**
   * Constucts a queue with the specified number of
   * priority levels.
   */

  public PriorityQueue(int levelNb) {
    this.levelNb = levelNb;
    size = 0;
    map = new HashMap();
    header = new Entry(null, null, null, -1);
    header.next = header.previous = header;

    levelLast = new Entry[levelNb];
    for (int i = 0; i < levelNb; i++) {
      levelLast[i] = header;
    }
  }


  /**
   * Constructs a queue with 5 priority levels.
   */

  public PriorityQueue() {
    this(5);
  }


  /**
   * Adds an element to the queue. It must be a
   * {@link choco.kernel.common.util.IPrioritizable} object.
   */

  public boolean add(Object o) {
    try {
      IPrioritizable p = (IPrioritizable) o;
      return add(o, p.getPriority());
    } catch (ClassCastException e) {
      throw new UnsupportedOperationException();
    }
  }


  /**
   * Adds an element to the queue with the specified priority.
   */

  public boolean add(Object o, int priority) {
    if (!map.containsKey(o)) { // Voir si on met la condition ou si c'est une precondition

      // Update the Linked List
      Entry newEntry = new Entry(o, null, null, priority);
      newEntry.previous = levelLast[priority];
      newEntry.next = levelLast[priority].next;
      newEntry.next.previous = newEntry;
      newEntry.previous.next = newEntry;

      // Update level spans
      //levelLast[priority] = newEntry;
      // Last
      int prior = priority;
      while ((prior < levelNb) &&
          (levelLast[prior].priority <= priority)) {
        //(levelLast[prior] == header) inutile puisque
        // header.priority << toutes les priorites
        levelLast[prior] = newEntry;
        prior++;
      }

      // Update the Map
      map.put(o, newEntry);

      this.size++;
      return true;
    } else {
      if (logger.isLoggable(Level.SEVERE))
        logger.severe("PriorityQueue: Element added already in the queue !");
      return false;
    }
  }


  /**
   * Adds all the elments of a collection to the queue.
   * <p/>
   * Not yet implemented.
   */

  public boolean addAll(Collection c) {
    boolean anyAddition = false;
    for (Iterator it = c.iterator(); it.hasNext();) {
      Object o = (Object) it.next();
      anyAddition = (anyAddition || this.add(o));
    }
    return anyAddition;
  }

  /**
   * Clears all the queue.
   */

  public void clear() {
    header.next = header.previous = header;
    size = 0;
    map.clear();

    for (int i = 0; i < levelNb; i++) {
      levelLast[i] = header;
    }
  }


  /**
   * Checks if the object is in the queue.
   */

  public boolean contains(Object o) {
    return (map.containsKey(o));
  }


  /**
   * Checks if all the element are in the queue.
   * <p/>
   * Not yet implemented.
   */

  public boolean containsAll(Collection c) {
    boolean oneOut = false;
    for (Iterator it = c.iterator(); (it.hasNext() && !oneOut);) {
      Object o = (Object) it.next();
      if (!contains(o)) {
        oneOut = true;
      }
    }
    return !oneOut;
  }


  /**
   * Checks if the queue is equals to another one.
   * Naive implementation: checks the object id.
   */

  public boolean equals(Object o) {
    // A ameliorer si necessaire
    return o == this;
  }


  /**
   * Checks if the queue is empty.
   *
   * @return true if the queue is empty
   */

  public boolean isEmpty() {
    return (size == 0);
  }


  /**
   * Iterator without a valid remove method !
   * Warning : suppose the set is not modified suring iterating !
   */
  public Iterator iterator() {
    return new PQIterator();
  }


  /**
   * Pops the first var in the queue.
   */

  public Object popFirst() {
    if (size == 0)
      throw new NoSuchElementException();
    Object ret = header.next.element;
    remove(ret);
    return ret;
  }


  /**
   * Removes the specified object.
   */

  public boolean remove(Object o) {
    Entry entry = (Entry) map.get(o);
    if (entry != null) {
      // Update map
      map.remove(o);

      // Update LinkedList
      entry.previous.next = entry.next;
      entry.next.previous = entry.previous;

      // Update level Span
      int prio = entry.priority;
      while ((prio < levelNb) && (levelLast[prio] == entry)) {
        levelLast[prio] = entry.previous;
        prio++;
      }

      entry = null; // force GC... normalement inutile...

      size--;
      return true;
    } else
      return false;
  }


  /**
   * Removes all the specified objects.
   * <p/>
   * Not yet implemented.
   */

  public boolean removeAll(Collection c) {
    boolean oneWasIn = false;
    for (Iterator it = c.iterator(); it.hasNext();) {
      Object o = (Object) it.next();
      if (remove(o)) {
        oneWasIn = true;
      }
    }
    return oneWasIn;
  }


  /**
   * Not yet implemented.
   */

  public boolean retainAll(Collection c) {
    // TODO : a completer
    throw new UnsupportedOperationException();
  }


  /**
   * Returns the size of the queue.
   */

  public int size() {
    return this.size;
  }


  /**
   * Returns an array with all the objects in the queue.
   */

  public Object[] toArray() {
    Object[] ret = new Object[size];
    int i = 0;
    Entry current = header.next;
    while (i < size) {
      ret[i] = current.element;
      current = current.next;
      i++;
    }
    if (current != header)
      if (logger.isLoggable(Level.SEVERE))
        logger.log(Level.SEVERE, "Model in PriorityQueue implementation !");
    return ret;
  }


  /**
   * Not yet implemented.
   */

  public Object[] toArray(Object[] a) {
    // TODO : a completer
    throw new UnsupportedOperationException();
  }


  /**
   * Updates the location of the element in the list.
   * The object must be {@link choco.kernel.common.util.IPrioritizable}.
   */

  public void updatePriority(Object o) {
    try {
      IPrioritizable p = (IPrioritizable) o;
      updatePriority(o, p.getPriority());
    } catch (ClassCastException e) {
      throw new UnsupportedOperationException();
    }
  }


  /**
   * Updates the location of the element in the list with the
   * specified priority.
   */

  public void updatePriority(Object o, int priority) {
    Entry entry = (Entry) map.get(o);
    if (entry != null) {
      if (priority != entry.priority) { // sinon, il n'y a rien a faire !
        // *** Remove ***
        // Update level span
        int prio = entry.priority;
        while ((prio < levelNb) && (levelLast[prio] == entry)) {
          levelLast[prio] = entry.previous;
          prio++;
        }

        // Update Linked List
        entry.previous.next = entry.next;
        entry.next.previous = entry.previous;

        // *** Add ***
        entry.priority = priority;
        // Update the Linked List
        entry.previous = levelLast[priority];
        entry.next = levelLast[priority].next;
        entry.next.previous = entry;
        entry.previous.next = entry;

        // Update level spans
        int prior = priority;
        while ((prior < levelNb) &&
            (levelLast[prior].priority <= priority)) {
          levelLast[prior] = entry;
          prior++;
        }
      }
    } else if (logger.isLoggable(Level.SEVERE))
      logger.log(Level.SEVERE, "Model in the PriorityQueue update.");
  }


  private static class Entry {
    int priority;
    Object element;
    Entry next;
    Entry previous;

    Entry(Object element, Entry next, Entry previous, int priority) {
      this.element = element;
      this.next = next;
      this.previous = previous;
      this.priority = priority;
    }
  }


  private class PQIterator implements Iterator {
    Entry cursor = header;

    public boolean hasNext() {
      return cursor.next != header;
    }

    public Object next() {
      cursor = cursor.next;
      return cursor.element;
    }

    public void remove() {
    }
  }
}
