 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.dataset;

import ocms.util.Block;
import ocms.dataset.Sample;
import ocms.dataset.Tuple;
import ocms.algorithms.statistics.Histogram;
import ocms.algorithms.statistics.Ranking;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.io.FileNotFoundException;

/**
  * Manages a cluster of samples.
  *
  * @author     Hossein Falaki
  */
public class DataCluster extends DataSet
{
    /** Keeps the time blocks within this cluster */
    ArrayList<Block> blocks;

    /** Keeps the centroid of this cluster */
    Sample centroid;

    /** Mapping from all possible unique GSM sets to their frequency */
    HashMap<Sample, Integer> featureCount;

    /** Unique integer ID of this data cluster */
    int id;


    /**
      * Constructs an empty DataCluster.
      *
      * @param      id          Unique integer ID of this cluster
      */
    public DataCluster(int id)
    {
        super();
        this.id = id;
        centroid = new Sample();
        featureCount = new HashMap<Sample, Integer>();
    }

    /**
      * Constructs a DataCluster object with the given dataset 
      * First, sorts the samples in the set based on the time stamp.
      * Sets the step value to DataSet.DEFAULT_STEP.
      *
      * @param  sampleset       set containing samples
      */
    public DataCluster(Set sampleset)
    {
        step = DataSet.DEFAULT_STEP;
        samples = new ArrayList<Sample>();
        int time = 0;

        start = Integer.MAX_VALUE;
        end = Integer.MIN_VALUE;
        featureCount = new HashMap<Sample, Integer>();

        for(Object s: sampleset)
        {
            this.add((Sample)s);
        }
        Collections.sort(samples);

    }


    /**
      * Adds a sample to the DataSet object.
      * It updates the centroid of the cluster that might be influenced by the new item.
      * You need to call findBlocks after one or more calls to this method.
      *
      * @param          sample              new sample to be added.
      */
    public void add(Sample newsample)
    {
        int time = newsample.getTime();
        int count;

        /* The final book keeping */
        samples.add((Sample)newsample);
        
        /* We also need to discover the start time */
        if (start > time )
            start = time;

        if (end < time )
            end = time;

        Collections.sort(samples);

        /* For using the union of all the samples as the centroid
        for (Object cellid : newsample.getGSMSet() )
            centroid.addGSM(new Tuple<String, Integer>((String)cellid, 0) );

        for (Object essid : newsample.getWiFiSet() )
            centroid.addWiFi(new Tuple<String, Integer>((String)essid, 0) );
        */

        /* For using the most frequent sample as the centroid */
        if (!featureCount.containsKey( newsample ))
        {
            featureCount.put(newsample, 1);
        }
        else
        {
            count = featureCount.get(newsample);
            featureCount.put(newsample, count + 1);
        }
    } 


    /**
      * Finds all the availability time blocks in the samples.
      */
    public void findBlocks()
    {
        blocks = new ArrayList<Block>();

        boolean inblock = false;
        double bstart = 0;
        double bend;
        Block newblock;

        // Finds all the availability blocks in the medium and constructs
        // the blocks 
        for (Sample s: samples)
        {
            if ( (!inblock) && (s.bestWiFi() != null))
            {
                inblock = true;
                bstart = s.getTime();
            }

            if ((inblock) && (s.bestWiFi() == null))
            {
                inblock = false;
                newblock = new Block(bstart, s.getTime(), this );
                newblock.setNaturalOrder(Block.STARTTIME);
                newblock.setOwner(this.getFileName());
                blocks.add(newblock);
            }
        }

        if (inblock == true)
        {
            newblock = new Block(bstart, end, this );
            newblock.setNaturalOrder(Block.STARTTIME);
            blocks.add(newblock);
        }

        Collections.sort(blocks);
    }

    /**
      * Returns an array of blocks.
      *
      * @return                 ArrayList object of blocks
      */
    public ArrayList getBlocks()
    {
        return blocks;
    }

    /**
      * Returns the number of blocks.
      *
      * @return                 the number of blocks
      */
    public int getBlocksNum()
    {
        return blocks.size();
    }

    /**
      * Finds the inter-arrival times between blocks.
      * All the values are inserted in the Histogram object.
      *
      * @param      hist        the Histogram object to keep the values
      */
    public void interArrivalStats(Histogram hist)
    {
        Iterator<Block> bIt = blocks.iterator();
        Block curblock, prevblock = null;
        double interTime = 0;

        while (bIt.hasNext())
        {
            curblock = bIt.next();
            if ( prevblock != null)
            {
                //System.out.println("" + curblock.getStart() + " - " +  prevblock.getEnd() + " = " + (curblock.getStart() - prevblock.getEnd()));
                hist.item(curblock.getStart() - prevblock.getEnd());
            }
            prevblock = curblock;
        }

    }

    /**
      * Presents statistics about the length of availability blocks.
      * All the values are inserted in the Histogram object.
      *
      * @param      hist        the Histogram object to keep the values
      */
    public void blockLengthStats(Histogram hist)
    {
        Iterator<Block> bIt = blocks.iterator();

        while (bIt.hasNext())
        {
            //System.out.println(bIt.next().getLength());
            hist.item(bIt.next().getLength());
        }
    }

    /**
      * Returns a string representation of the blocks which indicates
      * their rank based on length
      *       
      * @return                  string representation of the blocks 
      */
    public String blockRankToString()
    {
        Block newblock;
        ArrayList<Block> ranklist = new ArrayList<Block>();
        StringBuffer sb = new StringBuffer();
        int counter = 1;

        for( Block item : blocks)
        {
            newblock =  new Block(item);
            newblock.setNaturalOrder(Block.LENGTH);
            ranklist.add(newblock);
        }

        Collections.sort(ranklist);

        for (Block item : ranklist)
            sb.append(counter++ + "\t" + item.getLength() + "\n");

        return sb.toString();

    }

    /**
      * Updates a Ranking object with all the blocks in this datacluster.
      *
      * @param      ranking         the ranking object
      */
    public void blockLengthRank(Ranking ranking)
    {
        for( Block item : blocks)
            ranking.add(item);

    }



    /**
      * Returns a string representation of the DataCluster.
      *
      * @return                 a string representation of the DataCluster object
      */
    public String toString()
    {
        StringBuffer sb = new StringBuffer( "DataCluster size: " + samples.size()
            + " (" + start + ", " + end + ")\n" );

        Iterator<Block> bIt = blocks.iterator();

        while (bIt.hasNext())
        {
            sb.append(bIt.next().toString() + "\n");
        }

        return sb.toString();
        /*
        return "DataCluster size: " + samples.size()
            + " (" + start + ", " + end + ")\n"
            + blocks.toString() + "\n";
        //return "c"+samples.size();
        */

    }

    /**
      * Returns a string representation of the data cluster suitable for GnuPlot.
      *
      */
    public String GPToString()
    {
        Collections.sort(samples);
        findBlocks();
        StringBuffer sb = new StringBuffer();
        for (Sample s: samples)
        {
            sb.append(s.getTime() + " " + id*10 + "\n");
        }

        return sb.toString();
    }


    /**
      * Returns the centroid of this cluster.
      * Currently the centroid is a set of GSM IDs. It is the set of GSM cell IDs
      * that has the highest frequency.
      *
      * @return                centroid of the cell IDs in the cluster
      */
    public Sample getCentroid()
    {
        /* For returning the most frequent sample as the centroid */
        int count = 0;
        Sample cent = null;

        for (Sample s: featureCount.keySet() )
        {
            if ( count < featureCount.get(s) )
            {
                count = featureCount.get(s);
                cent = s;
            }
        }

        return cent;

        /* For returning the union of all the samples as the centroid 
        return centroid;
        */
    }

    /**
      * Set the unique ID of this data cluster.
      *
      * @param      id          unique integer ID of this data cluster
      */
    public void setId(int id)
    {
        this.id = id;
    }

}
