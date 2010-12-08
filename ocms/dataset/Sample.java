/**
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.dataset;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import ocms.dataset.Tuple;
import ocms.dataset.DataSet;

/**
  * Public class sample keeps and manipulates a single sample of experiments.
  * Each Sample consists of a timestamp, WiFi set and GSM set and the signal strength
  * of each accesspoint or cell tower.
  *
  * @author Hossein Falaki
  */
public class Sample implements Comparable<Sample>
{

    /** The sample timestamp */
    Integer time;

    /** The set of WiFi accesspoints */
    HashSet<Tuple<String, Integer>> wifiset;

    /** The set of GSM cell IDs */
    HashSet<Tuple<String, Integer>> gsmset;

    /**
     * Constructs an empty sample. 
     * The empty constructor constructs an empty sample instance
     */
    public Sample()
    {
        time = 0;
        wifiset = new HashSet<Tuple<String, Integer>>();
        gsmset = new HashSet<Tuple<String, Integer>>();
    }

    /**
      * Constructs a sample and sets the time.
      * Constructs an almost empty instance of Sample and sets the timestamp.
      *
      * @param  timestamp   the time that the sample has been obtained
      */
    public Sample(Integer timestamp)
    {
        time = new Integer(timestamp);
        wifiset = new HashSet<Tuple<String, Integer>>();
        gsmset = new HashSet<Tuple<String, Integer>>();
    }

    /**
      * Adds a WiFi Tuple to the set of WiFi APs.
      * Adds the Tuple to its internal set of WiFi accesspoints. This is the only possible way 
      * of adding WiFi APs to the sample.
      *
      * @param  wifituple   a tuple containing a BSSID and its measured signal strength
      */
    public void addWiFi(Tuple<String, Integer> wifituple)
    {
        wifiset.add(wifituple);
    }

    /**
      * Adds a GSM Tuple to the set of GSM IDs.
      * Adds the Tuple to its internal set of GSM cell IDs. This is the only possible way to
      * add a GSM ID to the sample.
      *
      * @param  gsmtuple    a tuple containing a GSMID and its measured signal strength
      */
    public void addGSM(Tuple<String, Integer> gsmtuple)
    {
        gsmset.add(gsmtuple);
    }

    /**
      * Sets the time of the sample.
      * Sets the time of the sample to the value of timestamp.
      *
      * @param   timestamp   the time that the sample has been measured.
      */
    public void setTime(Integer timestamp)
    {
        time = new Integer(timestamp);
    }

    /**
      * Returns the time of this instance.
      * Returns the time that the sample has been measured.
      *
      * @return     time of this sample
      */
    public Integer getTime()
    {
        return time;
    }

    /**
      * Returns the best WiFi AP in this sample.
      * Finds the WiFi accesspoint with the highest signal strenght and returns its BSSID,
      * and null if there is no WiFi AP in the sample.
      *
      * @return     the WiFi AP with highest signal strength.
      */
    public String bestWiFi()
    {
        int maxsignal = Integer.MIN_VALUE;
        String best = null;

        for( Tuple wifituple: wifiset)
        {
            if (maxsignal < (Integer)wifituple.getValue())
            {
                maxsignal = (Integer)wifituple.getValue();
                best = (String)wifituple.getKey();
            }
        }

        return best;
    }

    /**
      * returns the set of wifi aps in the sample.
      * constructs and returns a set consisting of the BSSIDs of all the wifi aps in the sample.
      *
      * @return     set of all the BSSIDs in the sample
      */
    public Set<String> getWiFiSet()
    {
        Set<String> wifiset = new HashSet<String>();

        for( Tuple wifituple: this.wifiset)
            wifiset.add((String)wifituple.getKey());

        return wifiset;
    }

    /**
      * returns the set of wifi APs and their signal strength in the sample.
      * Constructs and returns a set consisting of the BSSIDs ond the signal strength 
      * of each of the wifi APs in the sample.
      *
      * @return     set of {@link Tuple}s of BSSIDs and their signal strength
      */
    public Set<Tuple<String, Integer>>  getRichWiFiSet()
    {
        return wifiset;
    }



    /**
      * Returns the set of GSM cell IDs in the sample.
      * Constructs and returns a set consisting of the GSMIDs of all the GSM cells in the sample.
      *
      * @return     set of all the GSMIDs in the sample
      */
    public Set<String> getGSMSet()
    {
        Set<String> gsmset = new HashSet<String>();

        for( Tuple gsmtuple: this.gsmset)
            gsmset.add((String)gsmtuple.getKey());

        return gsmset;
    }

    /**
      * returns the set of GSMIDs and their signal strength in the sample.
      * Constructs and returns a set consisting of the GSMID ond the signal strength 
      * of each GSM cell tower in the sample.
      *
      * @return     set of {@link Tuple}s of GSMIDs and their signal strength
      */
    public Set<Tuple<String, Integer>> getRichGSMSet()
    {
        return gsmset;
    }

    /**
      * Removes the provided gSM cell ID from the sample.
      * If the provided ID is not a member of the GSM set, it will silently return.
      *
      * @param      cellid  GSM cell ID to be removed from the set
      */
    public void removeCellID(String cellid)
    {
        Tuple removal = null;

        for( Tuple gsmtuple: this.gsmset)
            if (gsmtuple.getKey().equals(cellid))
                removal = gsmtuple;

        if (removal != null)
            gsmset.remove(removal);

    }

    /**
      * Removes the provided WiFi ESSID from the sample.
      * If the provided ESSID is not a member of the WiFi set, will silently return.
      *
      * @param      essid       the ESSID to be removed from the sample
      */
    public void removeESSID(String essid)
    {
        Tuple removal = null;


        for( Tuple wifituple: this.wifiset)
            if (wifituple.getKey().equals(essid))
                removal = wifituple;

        if (removal != null)
            wifiset.remove(removal);
    }


    /**
      * Compares this sample with the specified sample object for order. 
      * Returns a negative integer, zero, or a positive integer as the time of 
      * this object is less than, equal to, or greater than the time of the  
      * specified object.
      *
      * @param  o   the object to be compared
      * @return     a negative integer, zero, or a positive integer as this 
      *             object is less than, equal to, or greater than the specified object.
      */
    public int compareTo(Sample o)
    {
        return time.compareTo(o.getTime());
    }


    /**
      * Returns a string representation of the sample.
      * Overrides the Object.toString() method.
      * 
      * @return     string representing the sample
      */
    public String toString()
    {
//        return " " + time + "\t" + wifiset + "\n";
        return " " + time + ":\t" + gsmset + "\n";
//        return "(" + time + ") WIFI: " + wifiset + " GSM: " + gsmset + "\n";
    }

    /**
      * Returns a string representation of the sample suitable for GnuPlot.
      * The type value can be DataSet.GNUPLOT_GSM or DataSet.GNUPLOT_WIFI
      *
      * @param      type            type of the output
      */
    public String GPtoString(int type)
    {
        StringBuffer sb = new StringBuffer();
        String[] tokens;

        if (type == DataSet.GNUPLOT_GSM)
        {
            for( Tuple gsmtuple: this.gsmset)
            {
                tokens = ((String)gsmtuple.getKey()).split(":");
                sb.append(time + " " + tokens[3] + "\n");
            }
        }

        if (type == DataSet.GNUPLOT_WIFI)
        {
            //TODO: print a number for each unique BSSID like GSM IDs
            for( Tuple wifituple: this.wifiset)
                sb.append(time + " " + wifituple.getKey() + "\n");
        }

        return sb.toString();
    }


    





}
