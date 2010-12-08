 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.algorithms.statistics;

import ocms.dataset.DataSet;
import ocms.dataset.Sample;

import java.util.HashSet;
import java.util.Iterator;


/**
  * Given a DataSet object, counts the number of unique elements (ESSID or
  * GSMID).
  * It provides methods to manipulate samples based on the frequency
  * information in the given DataSet object.
  *
  * @author     Hossein Falaki
  */
public class Counter
{
    /** A constant for GSM */
    public static final int GSM             = 0;

    /** A constant for WiFi */
    public static final int WIFI            = 1;

    /** Maps a cell ID to its count */
    HashSet<String> gsmcountset;
    HashSet<String> wificountset;

    /**
      * Constructs an empty Counter object with an empty set.
      *
      */
    public Counter()
    {
        gsmcountset = new HashSet<String>();
        wificountset = new HashSet<String>();
    }

    /**
      * Loads and counts the content of the given dataset object.
      *
      * @param      dataset         DataSet object to be added to the
      *                             counter
      */
    public void load(DataSet dataset)
    {
        Iterator it = dataset.iterator();
        Sample sample;

        while(it.hasNext())
        {
            sample = (Sample)it.next();
            gsmcountset.addAll(sample.getGSMSet());
            wificountset.addAll(sample.getWiFiSet());
        }
    }

    /**
      * Returns the number of unique GSM IDs in the countset.
      *
      * @return                     number of elements of the countset
      */
    public int getGSMCount()
    {
        return gsmcountset.size();
    }

    /**
      * Returns the number of unique ESSIDs in the countset.
      *
      * @return                     number of elements of the countset
      */
    public int getWiFiCount()
    {
        return wificountset.size();
    }



}
