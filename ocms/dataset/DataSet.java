 /** Opportunistic Connectivity Management Simulator
  *
  * Copeyright (C) 2007 Hossein Falaki
  */

package ocms.dataset;

import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import ocms.dataset.Sample;

/** 
  * Reads and manipulates data samples from field experiments containing Time and WiFi AP observations.
  *
  * @author Hossein Falaki
  */

public class DataSet implements Experiment, Iterable
{

    private static final int TIME_INDEX = 0;            // The input is supposed to have time at index 0
    private static final int WIFI_NUM_INDEX = 1;        // The input is supposed to have wifi_num at index 1
    private static final int GSM_NUM_INDEX = 2;         // The input is supposed to have gsm_num at 2 + wifi_num
    private static final int MAX_TIME = 100000;         // The assumed maximum number of samples in an experiment

    /** The default step */
    protected static final int DEFAULT_STEP = 60;       // The assumed default time step of experiments

    /** A constant for printing GSM cell IDs for gnuplot */
    public static final int GNUPLOT_GSM                     = 0;

    /** A constant for printing WiFi BSSIDs for gnuplot */
    public static final int GNUPLOT_WIFI                    = 1;

    /** Data strucutre that maps time to data experiment sample */
    ArrayList<Sample> samples;

    /** Name of the file that is used to load the data set */
    String dsfilename;

    /** The time step of the samples in the dataset */
    int step;

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
    public DataSet()
    {
        samples = new ArrayList<Sample>();

        /* The default time step is assumed to be DEAFAULT_STEP */
        step = DEFAULT_STEP;
        start = Integer.MAX_VALUE;
        end = Integer.MIN_VALUE;

        filename = "NULL";
    }

    /** 
      * Constructs the data set with the specified FileReader. 
      * Reads the dataset from the filereader into the internal data strucutres.
      *
      * @param  timestep    time step of the samples of the experiment
      * @param  filereader  name of the to be read into memory
      * @see             #load(String filename) for the acceptable file format
      */
    public DataSet(FileReader filereader, int timestep)
    {
        step = timestep;
        samples = new ArrayList<Sample>();

        load(filereader);

        this.filename = "NULL";
    }


    /** 
      * Constructs the data set with the specified file. 
      * Reads the content of the file into the internal mapping data strucutre.
      * In each line of the file the following fields should be tab or space seperated:
      * timestamp number-of-wifi-AP list-of-wifi-APs number-of-GSM-IDs list-of-GSM-IDS
      *
      * @param  timestep    time step of the samples of the experiment
      * @param  filename    name of the to be read into memory
      * @throws FileNotFoundException  if the file does not exist
      */
    public DataSet(String filename, int timestep) throws FileNotFoundException
    {
        this.filename = filename;

        this.step = timestep;
        samples = new ArrayList<Sample>();
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
      * Constructs the data set with the specified file. 
      * Reads the content of the file into the internal mapping data structure.
      * In each line of the file the following fields should be tab or space separated:
      * timestamp number-of-wifi-AP list-of-wifi-APs number-of-GSM-IDs list-of-GSM-IDS
      *
      * @param  filename     name of the to be read into memory
      * @throws FileNotFoundException  if the file does not exist
      */
    public DataSet(String filename) throws FileNotFoundException
    {
        this.filename = filename;

        /* The default step is assumed to be DEFAULT_STEP */
        step = DEFAULT_STEP;
        samples = new ArrayList<Sample>();
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
      * Reads the dataset from the filereader into the internal data strucutres.
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
    }


    /** 
      * Loads the contents of file into the memory.
      * Reads the content of the file into the internal mapping data strucutre.
      * In each line of the file the following fields should be tab or space seperated:
      * timestamp number-of-wifi-AP list-of-wifi-APs-and-signal number-of-GSM-IDs list-of-GSM-IDS-and-signal
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
    }

    /**
      * Sets the experiment time step.
      * Sets the time step of the experiment that has been loaded or is going to 
      * be loaded.
      *
      * @param  timestep    the time step of the experiment
      */
    public void setTimeStep(int timestep)
    {
        step = timestep;
    }


    /** Parses a single line of input and puts its elements in the associated data structures.
      * Treats the content of line as a single line of an input data file, and parses the line 
      * accordingly.
      * In each line of the file the following fields should be tab or space seperated:
      * timestamp number-of-wifi-AP list-of-wifi-APs-and-signals number-of-GSM-IDs list-of-GSM-IDS-and-signals
      *
      * @param      line            the line to be parsed
      */
    private void parseLine(String line)
    {
        int time = 0;                       // The time of the sample this line is representing
        int wifinum = 0;                    // Will hold the number of WiFi APs in this line
        int gsmnum = 0;                     // Will hold the number of GSM IDs in this line
        String ID;                          // Will keep the WiFi or GSM ID
        int signal;                         // Will keep the WiFi or GSM signal strength
        String[] items = line.split(" +");  // All the 'tokens' of this line


        /* This line does not contain any WiFi or GSM. It may be a bogus line */
        if (items.length < 3)
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


        /* Parsing the number of wifi APs in this line */
        try
        {
            wifinum = Integer.parseInt(items[WIFI_NUM_INDEX]);
        }
        catch (NumberFormatException nfe)
        {
            System.out.println("NumberFormatException while parsing " +
                    "wifinum: " + nfe.getMessage() + " in file " + filename );
            System.out.println("Details: line is [" + line + "]");
        }

        /* Parsing the number of GSM IDs in this line */
        try
        {
            gsmnum = Integer.parseInt(items[GSM_NUM_INDEX + wifinum*2]);
        }
        catch (Exception nfe)
        {
            //nfe.printStackTrace();
            System.out.println("Error while parsing gsmnum: " +
                    nfe.toString() + "on file " + filename );

            System.out.println("Details: line is [" + line + "]");
        }

        Sample newsample = new Sample(time * step);

        /* Adding all the WiFi APs into the wifiset */
        for ( int i=0; i < wifinum; i++ )
        {
            ID = items[WIFI_NUM_INDEX + 1 + i*2];
            try
            {
                signal = Integer.parseInt(items[WIFI_NUM_INDEX + 2 + i*2]);
            }
            catch (NumberFormatException nfe)
            {
                System.out.println("NumberFormatException while parsing WiFi signal: " + nfe.getMessage() + " in file " + filename);
                System.out.println("Details: line is [" + line + "]");
                signal = 0;
            }
            newsample.addWiFi(new Tuple<String, Integer>(ID, signal));
        }

        /* Adding all the GSM IDs into the gsmset */
        for ( int i=0; i < gsmnum; i++ )
        {
            ID = items[GSM_NUM_INDEX + wifinum*2 + 1 + i*2];
            try
            {
                signal = Integer.parseInt(items[GSM_NUM_INDEX + wifinum*2 + 2 + i*2]);
            }
            catch (NumberFormatException nfe)
            {
                System.out.println("NumberFormatException while parsing GSM signal: " + nfe.getMessage() + " in file " + filename);
                System.out.println("Details: line is [" + line + "]");
                signal = 0;
            }
            newsample.addGSM(new Tuple<String, Integer>(ID, signal));
        }

        this.add(newsample);
        
    }

    /**
      * Adds a sample to the DataSet object.
      *
      * @param          sample              new sample to be added.
      */
    public void add(Sample newsample)
    {
        int time = newsample.getTime();

        /* The final book keeping */
        samples.add((Sample)newsample);
        
        /* We also need to discover the start time */
        if (start > time )
            start = time;

        if (end < time )
            end = time;
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
        Set<Sample> result = new HashSet<Sample>(samples.size());

        for (Sample s: samples)
            result.add(s);

        return result;
    }


    /** 
      * Returns a String representation of the data set.
      * For debugging purposes dumps the data structure into a String for printing.
      */
    public String toString()
    {
        return "DataSet size: " + samples.size() 
            + " [" + start + ", " + end + "]\n" ;
//            + samples.toString();
    }

    /**
      * Returns a string representation of the data set suitable for GnuPlot plotting.
      * The type value should be one of DataSet.GNUPLOT_GSM or DataSet.GNUPLOT_WIFI.
      * 
      * @param      type            type of gnuplot input to return
      */
    public String GPtoString(int type)
    {
        StringBuffer sb = new StringBuffer();
        for (Sample s: samples)
            sb.append(s.GPtoString(type));

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
      * Returns the time step of the data set
      * 
      * @return             time step of the data set 
      */
    public int getTimeStep()
    {
        return step;
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

}
