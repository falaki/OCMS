 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.medium;

import java.util.Set;

import ocms.dataset.DataSet;
import ocms.dataset.Tuple;



/**
  * An Interface for medium. 
  * A medium is used by an interface to check 'connection opportunities' in the environment.
  * The semantics of a 'connection opportunity' depends on the implementation.
  * For example any available WiFi access point can be a 'connectino opportunity.'
  *
  * A medium reads its environment from a data set that consists of samples of the
  * environment. Samples are at discreet points of time whereas the medium accepts
  * continuous time requests. A medium assumes that throughout the time between two sample 
  * points the environment behaves as it did at the time of the first sample. This 
  * assumption should be furture verified in future work.
  *
  * @author Hossein Falaki
  */
public interface Medium
{
    /**
      * Returns true if the requested 'connection opportunity' exists in the environment.
      * The semantics of the String parameter depends on the implementation.
      * 
      * @param      opportunity       the state to be checked in the environment
      * @param      time              the time of the request.
      * @return                       true if the requested state actually holds
      */
    public boolean checkAvailability(String opportunity, double time);

    /**
      * Returns a set of all the available connection opportunities.
      * Scans the environment and retuns all the available 'connection opportuinties.
      * 
      * @param      time              the time of the request
      * @return                       set of all available connection opportunities
      */
    public Set<Tuple<String, Integer>> scan(double time);

    /**
      * Returns the strength of a 'connection opportunity'.
      * The semantics of 'strength' depends on the implementatin.
      *
      * @param      opportunity       the intended connection opportunity
      * @param      time              the time of the request
      * @return                       the 'strength' of the connection opportunity
      */
    public Integer signal(String opportunity, double time) throws MediumException;

    /**
      * Updates the environment based on the provided data set.
      * This method is probably called by the constructor to set up a medium
      * based on an instance of {@link DataSet}
      *
      * @param      dataset           the dataset to be used to update the medium
      */
    public void update(DataSet dataset);

    /**
      * Returns true if the medium 'exists' at the time.
      * This method should be called before any other querry to the medium.
      * 
      * @param      time                the time of the request
      * @return                         true if the environment exists at the time
      */
    public boolean hasTime(double time);

}

