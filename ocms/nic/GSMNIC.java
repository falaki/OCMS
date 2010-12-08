 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.nic;

import java.util.ArrayList;
import java.util.Set;

import ocms.medium.Medium;
import ocms.medium.GSMMedium;
import ocms.util.Configuration;
import ocms.util.Logger;
import ocms.util.Log;
import ocms.dataset.Tuple;


/**
  * Models the behaviours and features of WiFi Network Interface. Any instance of a WiFiNIC 
  * should be given a Medium object. The NIC interfaces with this medium and responds to 
  * the commands it receives accordingly.
  *
  * @author Hossein Falaki
  */
public class GSMNIC implements NIC, Logger
{
    /** Current state of the GSM NIC */
    int state;

    /** Reference to the medium object of the NIC */
    GSMMedium medium;

    /** Name of the GSM NIC */
    String name;

    /** Keeps the current time */
    Double now;


    /** A constant representing the OFF state */
    public static final int OFF                 = 0;
    /** A constant representing the when the NIC is ON and not associated */
    public static final int DISCONNECTED        = 1;
    /** A constant representing the when the NIC is ON and is associated */
    public static final int CONNECTED           = 2;
    /** A constant representing the scanning state */
    public static final int SCANNING            = 3;


    /**
      * Constructs an empty GSM NIC object.
      *
      */
    public GSMNIC()
    {
        name = null;
        medium = null;
        state = DISCONNECTED;
    }

    /**
      * Constructs a named GSM NIC object and sets up its parameters.
      *
      * @param      name                name of this GSM interface
      * @param      medium              the medium object for this interface
      */
    public GSMNIC(String name, GSMMedium medium)
    {
        this.name = name;
        this.medium = medium;
        this.state = DISCONNECTED;
    }


    /**
      * Returns the state of the GSMNIC.
      * The returned value should be checked against the public static fields of the 
      * GSMNIC. 
      *
      * @return                         the ArrayList representing the state of the NIC
      */
    public ArrayList checkState()
    {
        ArrayList<Object> mystate = new ArrayList<Object>(1);

        mystate.add(state);

        return mystate;
    }

    /** 
      * Takes the NIC forward in simulation time until now.
      * Returns true if the step is successful (i.e. could be checked with the medium)
      * and false if there is no more time available in the medium. 
      *
      * All the events (e.g. disconnection) that may have occurred during the step are 
      * logged.
      *
      * @return                         true if the step is successful
      */
    public boolean step(double time)
    {
        boolean result = true;

        Log.paranoid(this, "stepping to " + time);

        if (!medium.hasTime(time))
        {
            Log.stdout(this, "Error: medium does not have time " + time);
            return false;
        }

        /* These states do not require checking the medium */
        if (state == OFF) 
        {
            now = time;
            return true;
        }



        /* If 'time' does not pass the next sample point, just move it, knowing that
           the environment does not change between sample points */
        if ( medium.indexOf(time) < medium.indexOf(now) + medium.getTimeStep() )
            now = time;

        /* Now call step() as much as possible */
        while ( now < medium.indexOf(time) )
            result &= step();

        /* At this point 'time' does not pass the next sample point, Just move it,
           knowing that the environment does not change between sample points */
        if ( now < time )
            now = time;

        return result;
    }


    /**
    * Takes the GSM NIC one step into the future.
    * Returns true if the step is successful (i.e. could be double checked 
    *  with the medium). Step is determined by the underlying medium.
    * It will move the to the next sample point in the medium. If 'now'
    * is already at a sample point it will move one step forward, and otherwise
    * 'now' will be moved enough to reach the next sample point.
    *
    * @return                     true if the step is successful
    */
    private boolean step()
    {
        /* If 'now' is not at a sample point, first move it to the next sample point.
        No need to check time availability in the medium, because it has been checked
        when 'now' has passed the sample */
        if ( now != medium.indexOf(now) )
        {
            now = (double)medium.indexOf(now) + medium.getTimeStep();

            /* These states do not require checking the environment */
            if (state == OFF)
                return true;

            if  (state == DISCONNECTED) 
                if (medium.checkAvailability(now))
                {
                    Log.info(this, "Found GSM coverage");
                    state = CONNECTED;
                    return true;
                }

            if ( !medium.checkAvailability(now ) )
            {
                Log.info(this, "lost GSM coverage");
                state = DISCONNECTED;
            }
                
            return true;
        }

        /* At this point I know that 'now' is at a sample point */
            
        if (!medium.hasTime( now + medium.getTimeStep() ) )
            return false;

        /* States that do not require looking at the medium */
        now += medium.getTimeStep();
        if (state == OFF)
            return true;

        if (state == DISCONNECTED)
            if (medium.checkAvailability(now))
            {
                Log.info(this, "Found GSM coverage");
                state = CONNECTED;
                return true;
            }


        if ( !medium.checkAvailability(now) )
        {
            Log.info(this, "lost GSM coverage");
            state = DISCONNECTED;
        }

        return true;
    }
    

    /**
      * Commands the NIC to turn itself off.
      * The NIC does not check the medium to execute this command. 
      *
      * It logs the word "Disabled" in the log file.
      *
      * @return                         time after the command is executed
      */
    public double turnOff()
    {
        if (state == OFF)
            return now;

        state = OFF;

        Log.info(this, "disabled");
        return now;
    }

    /**
      * Commands the NIC to turn itself on.
      * The NIC does not check the medium to execute this command. 
      * 
      * It logs the word "Enabled" in the log file.
      *
      * @return                         time after the command is executed
      */
    public double turnOn()
    {
        if (state != OFF)
            return now;

        if (medium.checkAvailability(now))
            state = CONNECTED;
        else
            state = DISCONNECTED;

        Log.info(this, "enabled");
        return now;
    }


    /**
      * This method does not do anything for the GSM NIC.
      *
      *
      * @param      gsmid               GSMID of the cell to associate with
      * @return                         time after the command is executed
      * @throws     NICException        if association fails
      */
    public double associate(String bssid) throws NICException
    {
        Log.debug(this, "associate() should should not be called!");
        return now;
    }

    /**
      * Commands the NIC to disassociate from whatever GSM cell it is connected to.
      * Returns true if disassociation is successful. If it is not already associated
      * with any cell  the returned value will be false.
      *
      * @return                         time after the command is executed
      */
    public double disassociate()
    {
        if (state != OFF)
            state = DISCONNECTED;

        return now;
    }


    /**
      * Commands the NIC to transmit data until the specified time.
      * It may return before the specified time because of disconnection.
      * This method is not implemented for the GSM NIC yet. 
      * 
      * @param      endtime             time that transmission should stop
      * @return                         time after the command is executed
      */
    public double transmit(double endtime)
    {
        Log.debug(this, "transmit() method is not implemented in the GSM NIC");
        step(endtime);
        return now;
    }



    /**
      * Commands the WiFi NIC to scan the medium.
      * Returns a set of Tuples of connection opportunities and their 'strength'.
      * Each {@link ocms.dataset.Tuple} has a GSM ID and the signal strength of
      * that cell.
      *
      * @param          result          reference to be returned with the set of
      *                                 visible cells and their signal strength
      * @return                         time after the command is executed
      */
    public double  scan(Set<Object> result)
    {

        if (state == OFF)
        {
            Log.debug(this, "is off. Cannot scan");
            return now;
        }

        Log.info(this, "Scanning");
        Set<Tuple<String, Integer>> scanresult = medium.scan(now);

        if (scanresult != null)
        {
            Log.paranoid(this, "Scan returned " + scanresult);

            for (Tuple id : scanresult)
                result.add((Object)id);
        }
        else
        {
            Log.paranoid(this, "Scan returned null");
        }

        return now;
    }


    /**
      * Initializes the NIC with the provided time.
      *
      * @param      starttime           the time to start the NIC at
      * @throws     NICException        if initialization fails
      */
    public void initialize(double starttime) throws NICException
    {
        if (!medium.hasTime(starttime))
            throw new NICException(name + ": " + starttime + " is not available " +
                    "in the underlying medium");

        now = starttime;
    }

    /**
      * Returns a pointer to the medium.
      *
      * @return                         pointer to the medium of the interface
      */
    public Medium getMedium()
    {
        return medium;
    }

    /**
      * Returns the type of the NIC.
      * Should be checked against constants in {@links ocms.nic.NIC}.
      *
      * @return                         type of the NIC
      */
    public int getType()
    {
        return NIC.GSM;
    }

    /**
      * Returns a string representation of the NIC object.
      *
      * @return                         String representation of the NIC
      */
    public String toString()
    {
        return name;
    }

    /**
      * Returns the current time of the GSM NIC.
      * 
      * @return                         current time of the NIC
      */
    public double getTime()
    {
        return now;
    }

    /**
      * Returns the name of the GSM NIC object.
      *
      * @return                         name of the object.
      */
    public String getName()
    {
        return name;
    }


}
