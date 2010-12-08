 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.nic;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

import ocms.util.Log;
import ocms.util.Logger;
import ocms.util.Configuration;
import ocms.util.ProfileException;
import ocms.dataset.Tuple;
import ocms.dataset.DataSet;
import ocms.medium.WiFiMedium;
import ocms.medium.Medium;
import ocms.nic.NICException;
import ocms.eventqueue.EventConsumer;
import ocms.eventqueue.EventQueue;
import ocms.eventqueue.Event;



/**
  * Models the behaviours and features of WiFi Network Interface.
  * Any instance of a WiFiNIC should be given a Medium object. The NIC 
  * interfaces with this medium and responds to the commands it receives
  * accordingly.
  *
  * The WiFiNIC can keep track of a number of profiles (e.g. Power Profile).
  *
  * @author Hossein Falaki
  */
public class WiFiNIC implements NIC, Logger
{

    /** Name of this WiFi NIC */
    String name;

    /** The Power Profile of this NIC */
    WiFiProfile power;

    /** Medium used by this WiFi NIC */
    WiFiMedium medium;

    /** The internal state of the WiFi NIC */
    int state;

    /** The currently connected BSSID */
    String bssid;

    /** Keeps the current time */
    Double now;

    /** Keeps the transmission rate of the NIC */
    Integer sendrate;

    /** Keeps the receive rate of the NIC */
    Integer recvrate;

    /** Keeps the handler to the configuration object */
    Configuration config;


    /** Keeps the transition times of transient states such as SCANNING */
    HashMap<Integer, Double> transitiontimes;


    /** String name of the NIC states */
    private static final String[] statenames = {"OFF", "DISCONNECTED", "CONNECTED", "DISCONNECTED_SCANNING",
                                                "CONNECTED_SCANNING", "DATA_TX", "DATA_RX"};

    /** The table of possible bit rates (Kb/s)*/
    private static final HashSet<Integer> rates;

    /** The default transmission rate */
    private static final Integer DEFAULT_SEND_RATE          = 12288;

    /** The default receive rate */
    private static final Integer DEFAULT_RECV_RATE          = 9216;





    /** A constant representing the OFF state */
    public static final int OFF                 = 0;
    /** A constant representing the NIC is on and not associated with any AP */
    public static final int DISCONNECTED        = 1;
    /** A constant representing the state that the NIC is associated with an access point */
    public static final int CONNECTED           = 2;
    /** A constant representing the state that the NIC is ON, DISCONNECTED and SCANNING */
    public static final int DISC_SCANNING       = 3;
    /** A constant representing the state that the NIC is ON, ASSOCIATED and SCANNING */
    public static final int CONNECTED_SCANNING  = 4;
    /** A constant representing the state that the NIC is trasmitting data */
    public static final int DATA_TX             = 5;
    /** A constant representing the state that the NIC is receiving data*/
    public static final int DATA_RX             = 6;
    /** A constant representing the state that the NIC is turned on (equivalent to DISCONNECTED) */
    public static final int ON                  = DISCONNECTED;
    /** A constant used for sending a NOP command to the NIC */
    public static final int NOP                 = 8;
    /** A constant used for the transitive state of association */
    public static final int ASSOCIATION        = 9;

    /**
      * Static initializer
      */
    static 
    {
        rates = new HashSet<Integer>(8);
        rates.add( 6144 );
        rates.add( 9216 );
        rates.add( 12288 );
        rates.add( 18432 );
        rates.add( 24576 );
        rates.add( 36864 );
        rates.add( 49152 );
        rates.add( 55296 );

    }

    /**
      * Constructs an empty WiFiNIC.
      * Empty Constructor implemented for compatibility. 
      */
    public WiFiNIC()
    {
        name = null;
        medium = null;
        state = OFF;
        power = new WiFiProfile();
        now = (double)Integer.MIN_VALUE;
    }

    /**
      * Constructs a named WiFiNIC object and sets up its parameters.
      * 
      * @param      name                name of this WiFi interface
      * @param      conf                Configuration view to be used to read parameters
      * @param      medium              the Medium object for this NIC
      */
    public WiFiNIC(String name, Configuration conf, WiFiMedium medium)
    {
        this.name = name;
        this.state = OFF;
        this.bssid = null;
        this.now = (double)Integer.MIN_VALUE;
        this.config = conf;
        this.medium = medium;

        /*
        try
        {
            this.medium = new WiFiMedium( new DataSet( config.get("medium")), name + "_Medium") ;
        }
        catch (FileNotFoundException fnfe)
        {
            System.err.println(name + " Data set file dies not exist: " + fnfe.toString() );
            fnfe.printStackTrace();
        }
        */

        transitiontimes = new HashMap<Integer, Double>(3);
        transitiontimes.put( CONNECTED_SCANNING, config.getDouble("conscan_time") );
        transitiontimes.put( DISC_SCANNING, config.getDouble("discscan_time") );
        transitiontimes.put( ASSOCIATION, config.getDouble("association_time") );

        setRate( config.getInt("datasendrate"), config.getInt("datarecvrate") );

        
        /* Instatiating the internal profiles */
        power = new WiFiProfile("WiFi NIC " + name, config);
    }

    /**
      * Initializes the WiFiNIC with the provided time.
      *
      * @param      starttime           the time to start the WiFiNIC
      * @throws     NICException        if the provided time is not available in 
      *                                 the medium
      */
    public void initialize(double starttime) throws NICException
    {
        if (!medium.hasTime(starttime))
            throw new NICException(name + ": " + starttime + " is not available " +
                    "in the underlying medium");

        now = starttime;
        power.initialize( now );
    }

    /**
      * Sets the rate of the interface.
      * Returns true if the new rate could be successfully set
      *
      * @param      sendrate            the new rate to be sert
      * @param      recvrate            the new rate to be sert
      */
    public void setRate(Integer sendrate, Integer recvrate)
    {
        if ( rates.contains( sendrate ) )
            this.sendrate = sendrate;
        else
        {
            System.err.println(getName() + ": Error, " + sendrate + " is not a supported "
                    + "send rate. Rolling back to the default send rate " + DEFAULT_SEND_RATE);
            this.sendrate = DEFAULT_SEND_RATE;
        }

        if (rates.contains( recvrate ) )
            this.recvrate = recvrate;
        else
        {
            System.err.println(getName() + ": Error, " + sendrate + " is not a supported "
                    + "receive rate. Rolling back to the default receive rate " + DEFAULT_RECV_RATE);
            this.recvrate = DEFAULT_RECV_RATE;
        }
    }

    /**
      * Returns the transmission bit rate of the WiFi NIC.
      *
      * @return                         the transmission bit rate of the WiFi NIC
      */
    public int getTxRate()
    {
        return sendrate;
    }

    /**
      * Returns the receive bit rate of the WiFi NIC.
      *
      * @return                         the transmission bit rate of the WiFi NIC
      */
    public int getRxRate()
    {
        return recvrate;
    }

    /**
      * Returns the sum of turning on the NIC, associating with an AP
      * and then turning off
      *
      * @return                         the cost of turning on, associating, and turning off
      */
    public int getFixedCosts()
    {
        return config.getInt("off_to_disc") + config.getInt("disc_to_con") 
            + config.getInt("con_to_disc") + config.getInt("disc_to_off");
    }



    /**
      * Returns the state of the WiFi NIC.
      * The returned value should be checked against the public static fields of the 
      * WIFINIC class.
      *
      * @return                         the array representing the state of the NIC
      */
    public ArrayList checkState()
    {
        ArrayList<Object> mystate = new ArrayList<Object>(2);
        mystate.add(state);
        if ( (state == CONNECTED) || (state == CONNECTED_SCANNING) 
                || (state == DATA_TX) || (state == DATA_RX)  )
        {
            mystate.add( bssid );
        }
        else
        {
            mystate.add(null);
        }

        return mystate;
    }

    /** 
      * Takes the WiFi NIC to a future time in simulation.
      * Returns true if the step forward is successful (could be checked with the medium)
      * and false if there is no more time available in the medium.
      * This method should be called in conjunction with {@link #checkState}, because the state
      * of the WiFi NIC may change at some point of time during the step.
      *
      * @return                         true if the step is successful
      */
    public boolean step(double time) 
    {
        boolean result = true;
        
        Log.paranoid(this, "stepping to " + time );

        if (!medium.hasTime(time))
        {
            Log.stdout(this, "Error: medium does not have time " + time);
            return false;
        }

        /* These states do not require checking the medium */
        if ( (state == OFF) || (state == DISCONNECTED) || (state == DISC_SCANNING) )
        {
            now = time;
            return true;
        }

        /* If 'time' does not pass the next sample point, just move it, knowing that
           the environment does not change between sample points */
        if ( medium.indexOf(time) < medium.indexOf(now) + medium.getTimeStep() )
        {
            now = time;
        }

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
      * Takes the WiFi NIC one step into the fiture.
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
            if ( (state == OFF) || (state == DISCONNECTED) || (state == DISC_SCANNING) )
                return true;

            if ( !medium.checkAvailability(bssid, now ) )
            {
                Log.info(this, "lost " + bssid);
    
                try
                {
                    power.disconnect(now);
                }
                catch (ProfileException pe)
                {
                    System.out.println(name + ": bad 'disconnect' command sent to NIC Profile " +
                           " while stepping the time. Details: " + pe.toString() );
                }
    
                state = DISCONNECTED;
                bssid = null;
            }
            return true;
        }


        
        /* At this point I know that 'now' is at a sample point */

        if (!medium.hasTime( now + medium.getTimeStep() ) )
            return false;

        /* States that do not require looking at the medium */
        now += medium.getTimeStep();
        if ( (state == OFF) || (state == DISCONNECTED) || (state == DISC_SCANNING) )
            return true;

        if ( !medium.checkAvailability(bssid, now) )
        {
            Log.info(this, "lost " + bssid);
    
            try
            {
                power.disconnect(now);
            }
            catch (ProfileException pe)
            {
                System.out.println(name + ": bad 'disconnect' command sent to NIC Profile" + 
                        " while stepping the time. Details: " 
                        + pe.toString() );
            }

            state = DISCONNECTED;
            bssid = null;
        }

        return true;
    }

    /**
      * Commands the WiFi NIC to turn itself off.
      * The NIC does not check the medium to execute this command. It logs the word 
      * "Disabled" in the log file.
      *
      * @return                         time after the command is executed
      */
    public double turnOff()
    {
        if ( state == OFF )
            return now;

        state = OFF;
        bssid = null;

        /* Updating the internal profiles */
        try
        {
            power.turnOff(now);
        }
        catch (ProfileException pe)
        {
            System.out.println(name + ": bad command sent to NIC Profile. " +
                   "While turning off Details: "  + pe.toString() );
                   
        }

        Log.info( this, "disabled");
        return now;
    }

    /**
      * Commands the WiFi NIC to turn itself on.
      * The NIC does not check the medium to execute this command. It logs the word
      * "Enabled" in the log file.
      *
      * @return                         time after the command is executed
      */
    public double  turnOn()
    {
        if (state != OFF)
            return now;

        state = DISCONNECTED;
        bssid = null;


        /* Updating the internal profiles */
        try
        {
            power.turnOn(now);
        }
        catch (ProfileException pe)
        {
            System.out.println(name + ": bad command sent to NIC Profile. "
                   + "while turning on Details: " + pe.toString() );
        }

        Log.info(this, "enabled");
        return now;
    }

    /**
      * Commands the WiFi NIC to associate to an access point.
      * Returns true if association is successful
      *
      * @param      newbssid            BSSID of the AP to associate with
      * @return                         time after the command is executed
      * @throws     NICException        if association fails
      */
    public double associate(String newbssid) throws NICException
    {

        /* If the NIC is off */
        if (state == OFF)
            this.turnOn();

        step( now + transitiontimes.get(ASSOCIATION));

        /* If the NIC is already connected to the desired AP */
        if ( (bssid != null) && (this.bssid.equals(newbssid)) )
        {
            Log.debug(this, "Already connected to " + newbssid);
            return now;
        }


        if (!medium.checkAvailability(newbssid, now) )
        {
            Log.info(this, " associating to " + newbssid + " failed");
            state = DISCONNECTED;
            throw new NICException("association failed");
        }

        try
        {
            power.connect(now);
        }
        catch (ProfileException pe)
        {
            System.out.println(name + ": bad command sent to NIC Profile. "
                   + "While associating Details: " + pe.toString() );
        }

        state = CONNECTED;
        this.bssid = newbssid;

        Log.info(this, "associating " + newbssid + " succeeded");

        return now;
    }


    /**
      * Commands the WiFi NIC to disassociate from whatever AP it is connected to.
      * Returns true if disassociation is successful. If it is not already associated
      * with an access point the returned value will be false. To change the access point
      * to which the NIC is connected, you do not need to call this method. A direct call 
      * to {@link #associate} will do the job.
      *
      * @return                         time after the command is executed
      */
    public double disassociate()
    {
        if ( bssid == null ) 
        {
            Log.debug(this, "already disconnected");
            return now;
        }

       
        try
        {
            power.disconnect(now);
        }
        catch (ProfileException pe)
        {
            System.out.println(name + ": bad command sent to NIC Profile. "
                   + "While disassociating Details: " + pe.toString() );
        }

        bssid = null;
        state = DISCONNECTED;
       
        Log.info(this, "disassociating from " + bssid );
        
        return now;
    }

    /**
      * Commands the WiFi NIC to start sending data.
      * Returns true if sending could successfully start. A failure may be because of
      * not being associated or being in any state other than CONNECTED.
      *
      * @return                          true if transmission successfully started.
      */
    private boolean startTransmission()
    {
        if (state == CONNECTED)
        {
            try
            {
                power.transmit(now);
            }
            catch (ProfileException pe)
            {
                System.out.println(name + ": bad command sent to NIC Profile. "
                       + "While starting transmission Details: " + pe.toString() );
            }

            state = DATA_TX;
            Log.info(this, "Transmission started");
            return true;
        }

        return false;
    }

    /**
      * Commands the WiFi NIC to stop data transmission.
      * Returns true iff the NIC was already in transmission mode and false if not.
      *
      * @return                         true iff the NIC has been in transmission mode
      */
    private boolean stopTransmission()
    {

        Log.info(this, "Transmission ended");
        if (state == DATA_TX)
        {
            try
            {
                power.connect(now);
            }
            catch (ProfileException pe)
            {
                System.out.println(name + ": bad command sent to NIC Profile. "
                        + "While stopping transmission Details: " + pe.toString() );
            }

            state = CONNECTED;
            return true;
        }
        return false;
    }


    /**
      * Commands the WiFi NIC to transmit data until the specified time or it is looses 
      * the connection.
      * A value of 0 as endtime means transmit for ever.
      *
      * @param      endtime             the time that transmission should stop
      * @return                         time after the command is executed
      */
    public double transmit(double endtime)
    {

        /* Start transmission */
        if ( !startTransmission() )
        {
            Log.debug(this, ": Transmission did not start successfully");
            return now;                   //Transmission did not start successfully
        }

        if( endtime == 0 )
            endtime = Double.MAX_VALUE;

        /* now run step until the connection is lost */
        while ( (state == DATA_TX) && (now < endtime) )
        {
            if (!step())
            {
                /* This is a bad case, It means the NIC has been commanded
                   to transmit data beyond the time that is available in the data set
                   supported to the medium. Nothing can be done, just give a notice
                   both in the log file and in the standard error. */
                Log.info(this, "Logical error: Reached the end time in medium. " 
                        + "Transmission end time should be less than " + now);

                System.err.println("Logical error: Reached the end time in medium. " 
                        + "Simulation end time should be less than " + now 
                        + "\n THE RESULTS ARE NOT RELIABLE");
                return now;
            }
        }

        /* This call will have no effect if we left the while loop because 
           'state' is not DATA_TX any more. But if we left the loop because
           the transmission time is over this call actually stops transmission
           and puts the NIC in CONNECTED mode again.*/
        stopTransmission();

        return now;
        
    }



    /**
      * Commands the WiFi NIC to start receiving data.
      * Returns true if receiving could successfully start. A failure may be because of
      * not being associated or being in any state other than CONNECTED.
      *
      * return                          true if transmission successfully started.
      */
    private boolean startReceive()
    {
        if (state == CONNECTED)
        {
            try
            {
                power.receive(now);
            }
            catch (ProfileException pe)
            {
                System.out.println(name + ": bad command sent to NIC Profile. "
                        + "While starting receive Details: " + pe.toString() );
            }

            state = DATA_RX;
            return true;
        }

        return false;
    }


    /**
      * Commands the WiFi NIC to stop receiving data.
      * Returns true iff the NIC was already in receiving mode and false if not.
      *
      * @return                         true iff the NIC has been in transmission mode
      */
    private boolean stopReceive()
    {
        if (state == DATA_RX)
        {
            try
            {
                power.connect(now);
            }
            catch (ProfileException pe)
            {
                System.out.println(name + ": bad command sent to NIC Profile. "
                        + "While stopping receive Details: " + pe.toString() );
            }

            state = CONNECTED;
            return true;
        }
        return false;
    }

    /**
      * Commands the WiFi NIC to scan the medium.
      * Returns a set of Tuples. Each {@link ocms.dataset.Tuple} has a BSSID and
      * signal strength of an AP.
      * This is a transient state which means the NIC leaves this state after some specified
      * time. The time that the NIC will spend in this state is read from the simulation 
      * scenario. The other implication of being a transietion state is that it moves time
      * to a value in the future.
      *
      * @param      result              reference be returned with the set of available APs 
                                        and their signal strength
      * @return                         time after the command is executed
      */
    public double  scan(Set<Object> result)
    {
        int scantype;
        int initialstate = state;
        Set scanresult;

        if (state == CONNECTED)
        {
            scantype = CONNECTED_SCANNING;
        } 
        else if (state == DISCONNECTED)
        {
            scantype = DISC_SCANNING;
        }
        else
        {
            Log.debug(this, "Scan resulted in null");
            return now;
        }

        try
        {
            power.scan(now, scantype);
        }
        catch (ProfileException pe)
        {
            System.out.println(name + ": bad command sent to NIC Profile. "
                    + " While scanning Details: " + pe.toString() );
        }

        Log.info(this, "Scanning");
        step( now + transitiontimes.get( scantype ) );
        scanresult =  medium.scan(now);

        try
        {
            if (initialstate == CONNECTED)
                power.connect(now);
            if (initialstate == DISCONNECTED)
                power.disconnect(now);
        }
        catch (ProfileException pe)
        {
            System.out.println(name + ": bad command sent to NIC Profile, "
                    +" while scanning:  Details: " + pe.toString() );
        }

        if ( scanresult != null)
        {
            Log.paranoid(this, "Scan returned " + scanresult );
    
            for (Iterator it = scanresult.iterator(); it.hasNext();)
            {
                result.add((Object)it.next() );
            }
        }
        else
        {
            Log.paranoid(this, "Scan returned null" );
        }

        return now;
    }

    /**
      * This is a NOP command to the WiFi NIC.
      * 
      * @return                         time after the command is executed 
      */
    public double nop()
    {
        Log.debug(this, "NOP");
        step();
        return now;
    }


    /**
      * Retruns a pointer to the medium.
      *
      * @return                         pointer to the NIC medium.
      */
    public WiFiMedium getMedium()
    {
        return  medium;
    }


    /**
      * Returns the name of the WiFi NIC.
      * Specified by the {@link ocms.util.Logger} interface.
      *
      * @return                         name of the WiFi NIC
      */
    public String getName()
    {
        return name;
    }

    /**
      * Returns the current time of the WiFi NIC.
      * Specified by the {@link ocms.util.Logger} interface.
      *
      * @return                         the current time of the WiFi NIC object.
      */
    public double getTime()
    {
        return now;
    }

    /**
      * Returns the type of the NIC (which is WiFi in this case).
      *
      * @return                         type of the NIC
      */
    public int getType()
    {
        return NIC.WiFi;
    }

    /**
      * Returns a string representation of the WiFiNIC object.
      * The string includes details of all the profiles that the WiFi NIC
      * is keeping.
      *
      * @return                         String representation of the WiFi NIC
      */
    public String toString()
    {
        return power.toString();
    }


}
