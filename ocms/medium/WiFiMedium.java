 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.medium;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import ocms.dataset.Experiment;
import ocms.dataset.Tuple;
import ocms.dataset.DataSet;
import ocms.dataset.Sample;

/**
  * An implementation of the {@link Medium} interface for WiFi.
  * A WiFiMedium object is used by any {@link ocms.nic.WiFiNIC} to check the availability
  * of WiFi access points. The information is obtained from a {@link ocms.dataset.DataSet} object.
  *
  * @author     Hossein Falaki
  */
public class WiFiMedium implements Medium, Experiment
{
    /** The Mapping from time to Tuples representing the environment */
    HashMap<Integer, HashSet<Tuple<String,Integer>>> environment;

    /** The name of this WiFi Medium */
    String name;

    /** Time step of the sample used as the representation of the environment */
    Integer timestep;

    /** time of the first sample in the dat aset */
    Integer starttime;

    /** Time of the last sample in the data set */
    Integer endtime;

    /**
      * Constructs an emtpy and non usable instance.
      *
      */
    public WiFiMedium()
    {
        environment = new HashMap<Integer, HashSet<Tuple<String, Integer>>>();
        timestep = 1;
    }

    /**
      * Constructs an instance with the provided dataset.
      * 
      * @param      dataset             the dataset to be used as the environemnt
      * @param      name                the name to be assigned to this Medium
      */
    public WiFiMedium(DataSet dataset, String name)
    {
        this.name = name;
        environment = new HashMap<Integer, HashSet<Tuple<String, Integer>>>();
        this.update(dataset);
        this.timestep = dataset.getTimeStep();
        this.starttime = dataset.getStartTime();
        this.endtime = dataset.getEndTime();
    }

    /**
      * Updates the environment with the provided dataset.
      *
      * @param      dataset             the dataset to be used to update the envoronment
      */
    public void update(DataSet dataset)
    {
        
        Sample sample;

        for(Iterator it = dataset.iterator(); it.hasNext();)
        {
            sample = (Sample)it.next();
            environment.put((Integer)sample.getTime(), (HashSet<Tuple<String, Integer>>)sample.getRichWiFiSet());
        }
    }

    /**
      * Returns the sample point associated with a time 
      * 
      * @param          time            the time
      * @return                         the index associated with a time value
      */
    public int indexOf(double time)
    {
        double frac = (time - starttime)%timestep;
        Double dindex = Math.floor(time - frac);
        return  dindex.intValue();
    }


    /**
      * Returns true if the requested BSSID is available at the requested time.
      *
      * @param      bssid               the BSSID to be checked for availability
      * @param      time                the time of the request
      * @return                         true if the requested BSSID is available
      */
    public boolean checkAvailability(String bssid, double time)
    {
        if (environment.get(indexOf(time)) == null )
            return true;

        for( Tuple wifi: environment.get(indexOf(time)) )
            if ( wifi.getKey().equals(bssid) )
               return true;

        return false;
    }

    /**
      * Returns a set of all the available WiFi APs.
      * The returned set is a {@link HashSet} of {@link Tuple}s. Each Tuple 
      * consists of a BSSID string and an integer signal strength value.
      *
      * @param      time                the time of the request
      * @return                         set of available WiFi APs
      */
    public Set<Tuple<String, Integer>> scan(double time)
    {
        Set<Tuple<String, Integer>> result = environment.get(indexOf(time));

        /*
        if (result == null)
            return new HashSet<Tuple<String, Integer>>();
        */
            
        return result;
    }

    /**
      * Returns the signal strength of an available WiFi AP.
      * If the requested BSSID does not exist an exception is thrown, therefore
      * this method should be called after iff {@link #checkAvailability} is true
      * or {@link #scan} has been called.
      *
      * @param      time                the time of the request
      * @param      bssid               the requested BSSID
      * @throws     MediumException     if the BSSID is not available at the time
      */
    public Integer signal(String bssid, double time) throws MediumException
    {
        if (!this.checkAvailability(bssid, time))
            throw new MediumException(this.name + " : The requested BSSID (" + bssid + 
                    ") is not available at time " + time);

        Integer result = Integer.MIN_VALUE;

        for (Tuple wifi: environment.get(indexOf(time)) )
            if (wifi.getKey().equals(bssid))
                result = (Integer) wifi.getValue();

        return result;
    }

    /**
      * Returns true if the WiFiMedium 'exists' at the specified time.
      * This method should be called before any querry to the medium.
      *
      * @param      time                the time of the query
      * @return                         true if the environment exists at the time
      */
    public boolean hasTime(double time)
    {
        if ( ( time >= starttime ) && (time <= endtime) )
            return true;

        return false;
    }

    /**
      * Returns the time step of the experiment used to model the environment.
      *
      * @return                     time step of the underlying experiment
      */
    public int getTimeStep()
    {
        return timestep;
    }

    /**
      * Returns the time of the last data point if the experiment used to model 
      * the environment.
      *
      * @return                     time of the last data point in the underlying experiment
      */
    public int getEndTime()
    {
        return endtime;
    }

    /**
      * Returns the time of the first data point in the experiment used to model 
      * the environment.
      *
      * @return                     time of the first data point
      */
    public int getStartTime()
    {
        return starttime;
    }

    /**
      * Sets the experiment time step.
      * This method can only be used to increase the time step. If the new
      * time step is smaller than the current one it will be silently ignored.
      *
      * @param      timestep        the new time step
      */
    public void setTimeStep(int timestep)
    {
        if (timestep > this.timestep)
            this.timestep = timestep;

    }

    /* DEBUG */
    public void printDataSet()
    {
        System.out.println(environment.toString());
    }


}
