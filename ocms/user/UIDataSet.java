 /** Opportunistic Connectivity Management Simulator
  *
  * Copeyright (C) 2007 Hossein Falaki
  */

package ocms.user;

import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import ocms.dataset.Tuple;
import ocms.util.Block;
import ocms.algorithms.statistics.Ranking;
import ocms.algorithms.statistics.Histogram;


/** 
  * Reads and manipulates data samples from field experiments containing Time and WiFi AP observations.
  *
  * @author Hossein Falaki
  */

public class UIDataSet 
{

    private static final int TIME_INDEX = 0;            // The input is supposed to have time at index 0
    private static final int STATE_INDEX = 1;           // The input is supposed to have the state at index 1
    private static final int MAX_TIME = 100000;         // The assumed maximum number of samples in an experiment
    private static final String ON_STR = "on";
    private static final String OFF_STR = "off";
    private static final int ON = 0;
    private static final int OFF = 1;




    /** Data structure that maps time to data experiment sample */
    ArrayList<Tuple<Integer, Integer>> samples;

    /** Data structure that keeps the time blocks */
    ArrayList<Block> blocks;


    /** Time when that the experiment starts */
    int start;

    /** The time of the last sample of the data set */
    int end;

    /** Keeps the name of the source file */
    String filename;

    /** 
      * Constructs an empty dataset.
      * The empty constructor.
      */
    public UIDataSet()
    {
        samples = new ArrayList<Tuple<Integer, Integer>>();

        /* The default time step is assumed to be DEAFAULT_STEP */
        start = Integer.MAX_VALUE;
        end = Integer.MIN_VALUE;

        filename = "NULL";
    }

    /** 
      * Constructs the data set with the specified FileReader. 
      * Reads the dataset from the filereader into the internal data structures.
      *
      * @param  filereader  name of the to be read into memory
      * @see             #load(String filename) for the acceptable file format
      */
    public UIDataSet(FileReader filereader, int timestep)
    {
        samples = new ArrayList<Tuple<Integer, Integer>>();

        load(filereader);

        this.filename = "NULL";
    }


    /** 
      * Constructs the data set with the specified file. 
      * Reads the content of the file into the internal mapping data structure.
      * In each line of the file the following fields should be tab or space separated:
      * timestamp on/off
      *
      * @param  filename    name of the to be read into memory
      * @throws FileNotFoundException  if the file does not exist
      */
    public UIDataSet(String filename) throws FileNotFoundException
    {
        this.filename = filename;

        samples = new ArrayList<Tuple<Integer, Integer>>();
        try
        {
            load(filename);
        }
        catch (Exception e)
        {
            if (e.getClass().getName() == "FileNotFoundException")
                throw (FileNotFoundException)e;
            else 
            {
                System.out.println("Unexpected exception in Class DataSet while loading " + 
                        filename +  " : " + e.toString());
                e.printStackTrace();
            }
        }
    }




    /** 
      * Loads data from the specified FileReader into the memory.
      * Reads the dataset from the filereader into the internal data structures.
      *
      * @param filereader  name of the to be read into memory
      * @see             #load(String filename) for the acceptable file format
      */
    public void load(FileReader filereader)
    {
        this.filename = "NULL";

        Scanner input;

        input  = new Scanner(filereader);
        while ( input.hasNextLine() ) {
            parseLine(input.nextLine());
        }

        input.close();

        findBlocks();
    }


    /** 
      * Loads the contents of file into the memory.
      * Reads the content of the file into the internal mapping data strucutre.
      * In each line of the file the following fields should be tab or space seperated:
      * timestamp on/off
      *
      * @param filename  name of the to be read into memory
      * @throws FileNotFoundException  if the file does not exist
      */
    public void load(String filename) throws FileNotFoundException
    {
        this.filename = filename;

        Scanner input = null;

        try
        {
            input  = new Scanner(new FileReader(filename));
            while ( input.hasNextLine() ) 
            {
                parseLine(input.nextLine());
            }
            input.close();
        } 
        catch (Exception e)
        {
            if (e.getClass().getName() == "FileNotFoundException")
                throw (FileNotFoundException)e;
            else 
            {
                System.out.println("Unexpected exceptioin: " + e.toString() + 
                        "\n in Class DataSet while loading " +  filename );
                e.printStackTrace();
            }


        }

        input.close();

        findBlocks();
    }


    /** Parses a single line of input and puts its elements in the associated data structures.
      * Treats the content of line as a single line of an input data file, and parses the line 
      * accordingly.
      * In each line of the file the following fields should be tab or space seperated:
      * timestamp on/off
      *
      * @param      line            the line to be parsed
      */
    private void parseLine(String line)
    {
        int time = 0;                       // The time of the sample this line is representing
        int state = 0;
        String[] items = line.split(" +");  // All the 'tokens' of this line


        /* This line does not contain any WiFi or GSM. It may be a bogus line */
        if (items.length < 2)
        {
            //System.out.println(" DataSet: found a bogus line while loading " + dsfilename 
            //        + " : [" + line + "]" );
            return;
        }


        /* Parsing the timestamp of this line */
        try
        {
            time = Integer.parseInt(items[TIME_INDEX]);
        }
        catch (NumberFormatException nfe)
        {
            System.out.println("NumberFormatException while parsing time: "
                    + nfe.getMessage() + " in file " + filename);
            System.out.println("Details: line is [" + line + "]");
        }


        /* Parsing the state of the current line*/
        if (items[STATE_INDEX].equals(ON_STR))
            state = ON;
        else if (items[STATE_INDEX].equals(OFF_STR))
            state = OFF;

        Tuple<Integer, Integer> newsample = new Tuple<Integer, Integer>((Integer)time, (Integer)state);

        this.add(newsample);
        
    }

    /**
      * Adds a sample to the DataSet object.
      *
      * @param          sample              new sample to be added.
      */
    public void add(Tuple<Integer, Integer> newsample)
    {
        int time = (Integer)newsample.getKey();

        /* The final book keeping */
        samples.add(newsample);
        
        /* We also need to discover the start time */
        if (start > time )
            start = time;

        if (end < time )
            end = time;
    } 

    /**
      * Finds all the interaction blocks of time in the dataset.
      *
      */
    public void findBlocks()
    {
        blocks = new ArrayList<Block>();
        int lasttime = 0;
        int laststate = ON + OFF;
        Iterator<Tuple<Integer, Integer>> it = samples.iterator();
        Tuple<Integer, Integer> t;
        Block newblock;

        while(it.hasNext())
        {
            t = (Tuple<Integer, Integer>)it.next();
            if ((t.getValue().equals(OFF)) && (laststate == ON) )
            {
               blocks.add( new Block(lasttime, t.getKey()));
            }
            lasttime = (Integer)t.getKey();
            laststate = (Integer)t.getValue();
        }
    }

    /**
      * Returns a sorted ArrayList of the time that the screen has turned
      * on.
      *
      * @return                     sorted list of the time that the user
      *                             touches the keyboard (or Screen)
      */
    public ArrayList<Integer> getTouchTimes()
    {
        ArrayList<Integer> result = new ArrayList<Integer>();

        for( Tuple<Integer, Integer> it : samples)
            if (it.getValue().equals(ON))
                result.add(it.getKey());

        Collections.sort(result);

        return result;
    }

    /**
     * Returns an iterator over a set of samples of the dataset.
     * 
     * @return      the Iterator over the set of samples
     */
    public Iterator iterator()
    {
        return samples.iterator();
    }

    /**
      * Returns a set containing all the samples.
      *
      * @return             set of all the samples
      */
    public Set getSampleSet()
    {
        Set<Tuple> result = new HashSet<Tuple>(samples.size());

        for (Tuple s: samples)
            result.add(s);

        return result;
    }


    /** 
      * Returns a String representation of the data set.
      * For debugging purposes dumps the data structure into a String for printing.
      */
    public String toString()
    {
        return "UIDataSet size: " + samples.size() 
            + " [" + start + ", " + end + "]\n" + 
            samples.toString();
    }

    /**
      * Returns a string representation of the data set suitable for GnuPlot plotting.
      * The type value should be one of DataSet.GNUPLOT_GSM or DataSet.GNUPLOT_WIFI.
      * 
      */
    public String GPtoString()
    {
        StringBuffer sb = new StringBuffer();
        for (Tuple s: samples)
            sb.append(s.getKey() + "\t" + s.getValue());

        return sb.toString();
    }


    /** 
      * Returns the start time of the data set 
      * 
      * @return             time of the first sample in the data set
      */
    public int getStartTime()
    {
        return start;
    }

    /**
      * Returns the time of the last sample in the data set
      *
      * @return             time of the last sample in the data set
      */
    public int getEndTime()
    {
        return end;
    }

    /**
      * Returns the size of this data set.
      *
      * @return             number of samples in the data set
      */
    public int size()
    {
        return samples.size();
    }

    /**
      * Returns the name of the source file.
      *
      * @return             name of the source file
      */
    public String getFileName()
    {
        return filename;
    }

    /**
      * Sets the file name for this data dataset.
      *
      * @param      filenaem    name of the source file
      */
    public void setFileName(String filename)
    {
        this.filename = filename;
    }


    /**
    * Updates a Ranking object with all the blocks in this
    *
    * @param      ranking         the ranking object
    */
    public void blockLengthRank(Ranking ranking)
    {
        for( Block item : blocks)
            ranking.add(item);
    }

    /**
    * Updates a Histogram object with all the blocks.
    *
    * @param      hist           the histogram object
    */
    public void blockLengthHist(Histogram hist)
    {
        for( Block b : blocks)
            hist.item(b.getLength());
    }

    public String printBlockLength()
    {
        StringBuffer sb = new StringBuffer();

        for (Block b: blocks)
            sb.append(b.getLength() + "\n");

        return sb.toString();
    }




}
