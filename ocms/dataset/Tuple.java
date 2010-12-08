 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.dataset;



 /**
 *  public class Tuple keeps a generic tuple of types K and V.
 * Once a tuple is created it cannot be manipulated. Its key and value
 * can only be returned.
 * 
 * @author Hossein Falaki
 */
public class Tuple<K, V>
{ 
    /** The key of the tuple*/
    K key;
    
    /** The valueof the tuple*/
    V value;

    /** 
      * Constructs a tuple with the key and value .
      * Constructs a Tuple with the specified key and value pair.
      *
      * @param  key     the key to be set in the tuple
      * @param  value   the value to be set in the tuple
      */
    public Tuple(K key, V value)
    {
        this.key = key;
        this.value = value;
    }

    /** 
      * Returns the key of the tuple.
      * Returns the key of the tuple
      *
      * @return     the key in the tuple
      */
    public K getKey()
    {
        return this.key;
    }

    /** 
      * Returns the alue of the tuple.
      * Returns the value of the tuple
      *
      * @return     the value in the tuple
      */
    public V getValue()
    {
        return this.value;
    }


    /** Indicates whether the other object is "equal to" this one.
      * The equals method implements an equivalence relation on non-null object references.
      *
      * @param  tuple   the other tuple to compare to
      * @return         true if the other tuple is equal to this one
      */
    public boolean equals(Tuple<K, V> tuple)
    {
        if ( (this.key == tuple.key) && (this.value == tuple.value) )
        {
            return true;
        }
        else 
        {
            return false;
        }
    }

    /** Returns a string representation of the tuple.
      * Returns a string representation of the tuple(key, value) in the form of <key, valu>
      *
      * @return     the string represeting this tuple
      */
    public String toString()
    {
        return "<" + key.toString() + ", " + value.toString() + ">";
    }

}

