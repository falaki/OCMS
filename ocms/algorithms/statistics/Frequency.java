 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.algorithms.statistics;

import ocms.dataset.DataSet;
import ocms.dataset.Sample;

import java.util.HashMap;
import java.util.Iterator;

/**
  * Given a DataSet object, finds the frequency of each cell ID or ESSID among all the sample.
  * It provides methods to manipulate samples based on the frequency information in the given 
  * DataSet object.
  *
  * @author     Hossein Falaki
  */
public class Frequency
{
    /** A constant for GSM */
    public static final int GSM             = 0;

    /** A constant for WiFi */
    public static final int WIFI            = 1;

    /** The type variable of this Frequency object */
    int type;

    /** The DataSet object used by this object. */
    DataSet dataset;

    /** Maps a cell ID to its count */
    HashMap<String, Integer> freq;


    /**
      * Initializes the Frequency object with a given DataSet object.
      * It finds the frequency of each of the cell IDs in the data set
      * and keeps this information in a hash map. The variable type
      * determines whether to act on GSM IDs or WiFi IDs. It should be
      * either Frequency.WIFI or Frequency.GSM
      *
      * @param      dataset             DataSet object
      * @param      type                type of the Frequency object 
      */
    public Frequency(DataSet dataset, int type)
    {
        this.type = type;
        this.dataset = dataset;
        count();
    }

    /**
      * Returns a map of unique cell IDs to their frequency in the given sample set.
      *
      */
    private void count()
    {
        freq = new HashMap<String, Integer>();
        Iterator it = dataset.iterator();
        Iterator cellit;
        Sample s;
        String cellid;

        while (it.hasNext())
        {
            s = (Sample)it.next();
            if (type == WIFI)
                cellit = (s.getWiFiSet()).iterator();
            else
                cellit = (s.getGSMSet()).iterator();

            while (cellit.hasNext())
            {
                cellid = (String)cellit.next();
                if (!freq.containsKey(cellid))
                    freq.put(cellid, 1);
                else
                    freq.put(cellid, freq.get(cellid) + 1);
            }
        }
    }

    /**
      * Filters the data with both a low pass and a high pass filter.
      * 
      * @param      highthreshold       high pass filter threshold
      * @param      lowthreshold        low pass filter threshold
      */
    public void filter(double highthreshold, double lowthreshold)
    {
        highpass(highthreshold);
        lowpass(lowthreshold);

        /* DEBUGGING 
        int counter;
        System.out.println("Frequency: remaining features count: " + freq.keySet().size() );
        for (Object o : freq.keySet() )
        {
            counter = freq.get((String)o);
            System.out.println(o.toString() + ": " + (double)counter/dataset.size());
        }
        */

    }

    /**
      * Filters the given string from the dataset.
      *
      * @param      id              the string to be filtered
      */
    public void filter(String id)
    {
        Iterator sIt = dataset.iterator();
        while (sIt.hasNext())
        {
            if (type == GSM)
                ((Sample)sIt.next()).removeCellID( id );
            if (type == WIFI)
                ((Sample)sIt.next()).removeESSID( id );
        }
        count();
    }

    /**
      * Filters cell IDs with frequency lower than the specified threshold.
      *
      * @param      threshold           high pass filter frequency threshold
      */
    public void highpass(double threshold)
    {
        int total = dataset.size();
        Iterator sIt;


        for (String cellid : freq.keySet() )
        {
            sIt = dataset.iterator();
            if ((double)freq.get(cellid)/total < threshold)
            {
                while (sIt.hasNext())
                {
                    if (type == GSM)
                        ((Sample)sIt.next()).removeCellID( cellid );
                    if (type == WIFI)
                        ((Sample)sIt.next()).removeESSID( cellid );
                }
            }
        }
        count();
    }

    /**
      * Filters cell IDs with frequency higher than the specified threshold.
      *
      * @param      threshold           low pass filter frequency threshold
      */
    public void lowpass(double threshold)
    {
        int total = dataset.size();
        Iterator sIt;

        for (String cellid : freq.keySet() )
        {
            sIt = dataset.iterator();
            if ((double)freq.get(cellid)/total > threshold)
            {
                while (sIt.hasNext())
                {
                    if (type == GSM)
                        ((Sample)sIt.next()).removeCellID( cellid );
                    if (type == WIFI)
                        ((Sample)sIt.next()).removeESSID( cellid );
                }
            }
        }
        count();
    }

    /**
      * Returns the mapping from feature to count.
      *
      * @return                         mapping from unique feature to their count
      */
    public HashMap<String, Integer> getFreq()
    {
        count();
        return freq;
    }

}
