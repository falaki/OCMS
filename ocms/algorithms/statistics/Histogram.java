 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.algorithms.statistics;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.lang.Math;

/**
  * This class implements a histogram object. 
  * A histogram object is a practical/discrete representation of a
  * probability distribution function. 
  * It has keeps a bin for each interval of data and keeps track of 
  * the frequency of items in each bin.
  *
  * @author     Hossein Falaki
  */
public class Histogram
{
    /* Maps the interval to the frequency*/
    HashMap<Double, Double> bins;

    /* The start value of the first bin */
    double start;

    /* The start value of the last bin */
    double end;

    /* The size of each bin */
    double interval;

    /* Sum of all the items seen so far */
    double sum;

    /* Total number of items seen so far */
    int totalcount;


    /**
      * Constructs a histogram object with the given start value and 
      * interval size.
      * The object is initialized with only one bin. Other bins
      * are created as new items are submitted to the histogram. 
      *
      * @param      start               start value
      * @param      interval            interval size
      */
    public Histogram(double start, double interval)
    {
        this.totalcount = 0;
        this.start = start;
        this.end = start;
        this.interval = interval;
        bins = new HashMap<Double, Double>();
    }

    /**
      * Updates the histogram with the arrival of a new item 
      * with the given value.
      *
      * @param      value               value of the item
      */
    public void item(double value)
    {

        /* Just ignore this item */
        if (value < start)
            return;
        
        this.totalcount++;
        double index = Math.floor( (value - start)/interval);
        double key = start + interval*index;
        double current = 0;
        if ( bins.keySet().contains(key) )
            current = bins.get(key);

        bins.put(key, current + 1);

        sum += value;

        if ( value > end )
            end = value;
    }

    /**
      * Returns the mean of the histogram.
      *
      * @return                         Mean of the histogram
      */
    public double getMean()
    {
        return sum/totalcount;
    }

    /**
      * Returns the variance of the histogram.
      *
      * @return                         Variance of the histogram
      */

    /**
      * Returns the total number of samples
      *
      * @return                         total number of samples
      */
    public int getTotal()
    {
        return totalcount;
    }

    /**
      * Returns the value of the bin
      *
      * @param      binvalue            value of the requested bin
      */
    public double getBinValue(double binvalue)
    {
        return bins.get(binvalue);
    }

    




    /**
      * Returns a string representation of the histogram.
      *
      * @return                         string representation of the histogram
      */
    public String toString()
    {
        StringBuffer result = new StringBuffer("HISTOGRAM: " 
                + totalcount + " values, mean = " + this.getMean() + "\n");
        for (double i = start; i <= end; i += interval)
        {
            if ( bins.keySet().contains(i) )
                result.append(" " + i + "\t\t" + bins.get(i) + "\n");
//            else
//                result.append(" " + i + "\t" + 0 + "\n");
        }

        return result.toString();
    }

    /**
      * Returns a set of the representative values of the bins.
      *
      * @return                     HashSet object of the values of bins
      */
    public Set<Double> getBins()
    {
        return bins.keySet();
    }
}
