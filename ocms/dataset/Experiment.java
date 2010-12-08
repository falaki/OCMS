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

import ocms.dataset.Sample;

/** 
  * Interface for any class that stores the result of an experiment.
  *
  * @author Hossein Falaki
  */

public interface Experiment 
{

    /**
      * Sets the experiment time step.
      * Sets the time step of the experiment that has been loaded or is going to 
      * be loaded.
      *
      * @param  timestep    the time step of the experiment
      */
    public void setTimeStep(int timestep);
    
    /** 
      * Returns a String representation of the experiment.
      * Dumps the data structure into a String for printing.
      */
    public String toString();
    

    /** 
      * Returns the start time of the experiment.
      * 
      * @return             time of the first sample in the experiment
      */
    public int getStartTime();
    

    /**
      * Returns the time of the last sample in the experiment.
      *
      * @return             time of the last sample in the experiment
      */
    public int getEndTime();
    

    /**
      * Returns the time step of the experiment.
      * 
      * @return             time step of the experiment data points
      */
    public int getTimeStep();
}
