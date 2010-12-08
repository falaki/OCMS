 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.algorithms.statistics;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ocms.util.Block;

/**
  * A CDF object represent a Cumulative distribution function.
  *
  * @author     Hossein Falaki
  */

public class CDF
{
    /** Keeps the objects to be ranked */
    ArrayList<Double> list;

    /** Keeps sum of all the values */
    double total;


    /**
      * Constructs an empty CDF  object.
      *
      */
    public CDF()
    {
        list = new ArrayList<Double>();
        total = 0;
    }

    /**
      * Constructs a CDF object with the given ranking object.
      *
      * @param      ranking         the ranking object to build the CDF for
      */
    public CDF(Ranking ranking)
    {
        ArrayList<Block> inputlist = ranking.getItems();
        Block newblock;
        list = new ArrayList<Double>();
        total = 0;

        for (Block item : inputlist)
        {
            total += item.getLength();
            /*
            newblock = new Block(item);
            newblock.setNaturalOrder(Block.LENGTH);
            */
            list.add((Double)item.getLength());
        }
    }

    /**
      * Constructs a CDF object with the given Histogram object.
      *
      * @param      hist            the histogram object to build the CDF
      *                             for
      */
    public static String printCDF(Histogram hist)
    {
        ArrayList<Double> bins = new ArrayList<Double>(hist.getBins());
        StringBuffer sb = new StringBuffer();
        double currentvalue = 0;

        int histtotal = hist.getTotal();
        Collections.sort(bins);

        for (Double b : bins)
        {
            currentvalue += hist.getBinValue(b);
            sb.append(b + "\t" + currentvalue + "\t" + (currentvalue/histtotal)*100 + "\n");
        }
        return sb.toString();

        
    }

    /**
      * Prints the rank-size CDF using the given rank object.
      *
      * @param      ranking         the ranking object to be used.
      */
    public static String printCDF(Ranking ranking)
    {
        ArrayList<Block> inputlist = ranking.getItems();
        StringBuffer sb = new StringBuffer();
        Block newblock;
        double total = 0;
        int counter = 0;
        int totalnum = inputlist.size();
        double currentsum = 0;

        for (Block item : inputlist)
            total += item.getLength();

        Collections.sort(inputlist);

        for( Block item : inputlist)
        {
            currentsum += item.getLength();
            sb.append(((double)counter/totalnum)*100 + "\t" + item + "\t" + (currentsum/total)*100 + "\n");
            counter++;
        }

        return sb.toString();

    }



    /**
      * Adds the object to the list of objects.
      * 
      * @param      item            the item to be added to the list of objects.
      */
    public void add(Block item)
    {
        total += item.getLength();
        /*
        Block newblock = new Block(item);
        newblock.setNaturalOrder(Block.LENGTH);
        */

        list.add((Double)item.getLength());
    }

    /**
      * Returns a string representation of the CDF object.
      *
      * @return                     string representation of the ranking objects
      */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        int counter = 1;
        double totalnum = list.size();
        double currentsum = 0;
        Collections.sort(list);

        for( Double item : list)
        {
            currentsum += item;
            sb.append(((double)counter/totalnum)*100 + "\t" + item + "\t" + (currentsum/total)*100 + "\n");
            counter++;
        }

        return sb.toString();
    }


}

