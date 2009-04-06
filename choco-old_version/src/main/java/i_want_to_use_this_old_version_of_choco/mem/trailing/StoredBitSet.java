// **************************************************
// *  CHOCO: an open-source Constraint Programming  *
// *     System for Research and Education          *
// *                                                *
// *    contributors listed in dev.i_want_to_use_this_old_version_of_choco.Entity.java    *
// *           Copyright (C) F. Laburthe, 1999-2006 *
// **************************************************
package i_want_to_use_this_old_version_of_choco.mem.trailing;

import i_want_to_use_this_old_version_of_choco.mem.IStateBitSet;
import i_want_to_use_this_old_version_of_choco.prop.VarEvent;
import i_want_to_use_this_old_version_of_choco.util.BitSet;
import i_want_to_use_this_old_version_of_choco.util.IntIterator;

/**
 * A set of bits (0/1 values) with backtrackable structures. Implemented with
 * a backtrackable vector of integer.
 */
public class StoredBitSet implements IStateBitSet {
  /**
   * A stored integer vector containing the bit representation.
   */
  protected final StoredIntVector representedBy;

  /**
   * States if the size of bit set is fixed.
   */
  protected final boolean fixedSize;

  protected final int sizeIfFixed;

  /**
   * Builds a new stored bit set.
   * @param env the environment reponsible of worlds management
   * @param initialSize the initial size (number of bits needed) 
   */
  // note: shifting by 5 bits amounts to dividing by 32
  public StoredBitSet(final EnvironmentTrailing env, final int initialSize) {
    this(env, initialSize, false);
  }

  /**
   * Builds a new stored bit set.
   * @param env the environment reponsible of worlds management
   * @param initialSize the initial size (number of bits needed)
   * @param fixed sepcifies if the bit size is fixed or not
   */
  // note: shifting by 5 bits amounts to dividing by 32
  public StoredBitSet(final EnvironmentTrailing env, final int initialSize,
                      final boolean fixed) {
    representedBy = new StoredIntVector(env, largeIndex(initialSize) + 1, 0);
    fixedSize = fixed;
    if (fixedSize)
      sizeIfFixed = representedBy.size();
    else
      sizeIfFixed = -1;
  }

  /**
   * Returns the index of the word in the "bits" array.
   * @param bitIndex the index among all integer bits
   * @return the index of the integer containing data for the requires
   * global index
   */
  private static int largeIndex(final int bitIndex) {
    return (bitIndex >> 5);
  }

  /**
   * Returns the index within the right word.
   * @param index the index among all integer bits
   * @return the index of the bit in the integer containing this one
   */
  private static int smallIndex(final int index) {
    return (index & 31); // equivalent to (index % 32);
  }

  public int cardinality() {
    int s = 0;
    for (int i = getRepresentedBySize() -1; i >=0 ; i--) {
      s += cardinality(representedBy.get(i));
    }
    return s;
  }

  protected int getRepresentedBySize() {
    if (fixedSize) return sizeIfFixed;
    return representedBy.size();
  }

  /**
   * Static methods to comput the number of on bits in an integer.
   * @param bits the integer in which we want to count the on bits
   * @return the number of bits on within an int
   */
  private static int cardinality(final int bits) {
    int c = 0;
    for (int b = 0; b <= 31; b++) {
      if (BitSet.getBit(bits, b)) {
        c++;
      }
    }
    return c;
  }

  public void set(final int bitIndex) {
    this.ensureCapacity(bitIndex);
    int lidx = largeIndex(bitIndex);
    int sidx = smallIndex(bitIndex);
    int mask = 1 << sidx;
    int word = representedBy.get(lidx);
    if ((word & mask) == 0) {
      // the bit was not already set.....
      representedBy.set(lidx, word | mask);
    }
  }

  public void clear(final int bitIndex) {
    int lidx = largeIndex(bitIndex);
    if (lidx < getRepresentedBySize()) {
      int sidx = smallIndex(bitIndex);
      int mask = 1 << sidx;
      int word = representedBy.get(lidx);
      if ((word & mask) != 0) {
        // the bit was already set.....
        representedBy.set(lidx, word & ~mask);
      }
    }
  }

  public void clear() {
	for (int i = 0; i < representedBy.size(); i++) {
		representedBy.set(i,0);		  
	}
  }

  public void set(final int index, final boolean value) {
    if (value) {
      set(index);
    } else {
      clear(index);
    }
  }

  public boolean get(final int bitIndex) {
    int lidx = largeIndex(bitIndex);
    int sidx = smallIndex(bitIndex);
    int mask = 1 << sidx;
    int word = representedBy.get(lidx);
    return ((word & mask) != 0);
  }

  // TODO: write the currentElement file + prevSetBit + nextClearBit
  public int nextSetBit(final int fromIndex) {
    int repSize = getRepresentedBySize();
    if (fromIndex < 0) {
      throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
    } else if (fromIndex >= repSize * 32 - 1) {
      return -1;
    }
    //System.out.println("" + fromIndex + " - " + largeIndex(fromIndex));
    int lidx = largeIndex(fromIndex);
    int sidx = smallIndex(fromIndex);
    // int mask = 1 << sidx;
    int word = representedBy.get(lidx);

    word = (word & (((0xffffffff) >> sidx) << sidx));
    // 1...(21 set bits)...1000 with sidx=3

    while ((word == 0) && (lidx < repSize - 1)) {
      // <fla>: added -1
      word = representedBy.get(++lidx);
    }
    if (word == 0) {  // the bitset contains no entry after fromIndex
      return -1;
    } else {
      sidx = trailingZeroCnt(word);
    }
    return (lidx * 32 + sidx);
  }

  /**
   * Counts the number of clear bits starting from the lightest (rightmost) one
   * assumes val contains some bits that are set.
   *
   * @param val the integer, taken as a 32-bit set
   * @return the index of the rightmost bit that is set
   */
  public static int trailingZeroCnt(final int val) {
    int offset = 0;
    int v = val;
    while ((v & 1) == 0) {
      offset++;
      v >>= 1;
    }
    return offset;
  }

  public int prevSetBit(final int fromIndex) {
    int repSize = getRepresentedBySize();
    if (fromIndex < 0) {
      throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
    } else if (fromIndex > repSize * 32) {
      throw new IndexOutOfBoundsException("fromIndex > 32*size: " + fromIndex);
    }

    int lidx = largeIndex(fromIndex);
    int sidx = smallIndex(fromIndex);
    //    int mask = (((0xffffffff) << (31 - sidx)) >> (31 - sidx));
    // 0001...(21 set bits)...1 with sidx=3
    //    int mask = ~(((0xffffffff) >> (sidx + 1)) << (sidx + 1));
    // 0...(28 clear bits)...01111 with sidx=3
    int mask = ((sidx == 31) ?
        0xffffffff :
        ~(((0xffffffff) >> (sidx + 1)) << (sidx + 1)));
    // 0...(28 clear bits)...01111 with sidx=3

    int word = representedBy.get(lidx);
    word = (word & mask);

    while ((word == 0) && (lidx > 0)) {
      word = representedBy.get(--lidx);
    }
    if (word == 0) {  // the bitset contains no entry before fromIndex
      return -1;
    } else {
      sidx = (31 - startingZeroCnt(word));
    }
    return (lidx * 32 + sidx);
  }

  /**
   * Counts the number of clear bits starting from the heaviest (leftmost) one
   * assumes val contains some bits that are set.
   *
   * @param val the integer, taken as a 32-bit set
   * @return the index of the leftmost bit that is set
   */
  public static int startingZeroCnt(final int val) {
    int mask = (1 << 31);
    int offset = 0;
    int v = val;
    while ((v & mask) == 0) {
      offset++;
      v <<= 1;
    }
    return offset;
  }

  public int capacity() {
    return (getRepresentedBySize() << 5);
  }

  public void ensureCapacity(final int bitIndex) {
    while (bitIndex >= this.capacity()) {
      representedBy.add(0);
    }
  }

  public IntIterator getCycleButIterator(final int avoidIndex) {
    int n = this.cardinality();
    if (avoidIndex != VarEvent.NOCAUSE && this.get(avoidIndex)) n -= 1;
    if (n > 0) {
      return new StoredBitSet.CyclicIterator(this, avoidIndex);
    } else {
      return new StoredBitSet.EmptyIterator();
    }
  }



}

