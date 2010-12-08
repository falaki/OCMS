 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.nic;

import java.util.ArrayList;
import java.util.Set;

import ocms.medium.Medium;


/**
  * Interface to be implemented by any class that models a Network Interface (NIC).
  * Any instance of an Interface implementation is given a Medium object. The NIC 
  * interfaces with this medium and responds to the commands it receives
  * accordingly and meanwhile keeps track of its internal {@link ocms.util.Profile}s.
  *
  * @author Hossein Falaki
  */
public interface NIC
{

    /** A constant for WiFi NICs */
    public static final int WiFi        = 0;
    public static final int GSM         = 1;

    /**
      * Returns the state of the NIC.
      * The returned value should be checked against the public static fields of the 
      * NIC. 
      *
      * @return                         the String representing the state of the NIC
      */
    public abstract ArrayList checkState();

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
    public abstract boolean step(double now);

    /**
      * Commands the NIC to turn itself off.
      * The NIC does not check the medium to execute this command. 
      *
      * It logs the word "Disabled" in the log file.
      *
      * @return                         time after the command is executed
      */
    public abstract double turnOff();

    /**
      * Commands the NIC to turn itself on.
      * The NIC does not check the medium to execute this command. 
      * 
      * It logs the word "Enabled" in the log file.
      *
      * @return                         time after the command is executed
      */
    public abstract double turnOn();


    /**
      * Commands the NIC to associate to an access point.
      * Returns true if association is successful
      *
      * It logs the "ASSOCIATED bssid" in tog file.
      *
      * @param      bssid               BSSID of the AP to associate with
      * @return                         time after the command is executed
      * @throws     NICException        if association fails
      */
    public abstract double associate(String bssid) throws NICException;

    /**
      * Commands the NIC to disassociate from whatever AP it is connected to.
      * Returns true if disassociation is successful. If it is not already associated
      * with an access point the returned value will be false. To change the access point
      * to which the NIC is connected, you do not need to call this method. A direct call 
      * to {@link #associate} will do the job.
      *
      * @return                         time after the command is executed
      */
    public double disassociate();

    /**
      * Commands the NIC to transmit data until the specified time.
      * It may return before the specified time because of disconnection.
      * 
      * @param      endtime             time that transmission should stop
      * @return                         time after the command is executed
      */
    public double transmit(double endtime);

    /**
      * Commands the WiFi NIC to scan the medium.
      * Returns a set of Tuples of connection opportunities and their 'strength'.
      *
      * @param          result          reference to be returned with the set of
      *                                 available APs and their signal strength
      * @return                         time after the command is executed
      */
    public double  scan(Set<Object> result);

    /**
      * Initializes the NIC with the provided time.
      *
      * @param      starttime           the time to start the NIC at
      * @throws     NICException        if initialization fails
      */
    public void initialize(double starttime) throws NICException;

    /**
      * Returns a pointer to the medium.
      *
      * @return                         pointer to the medium of the interface
      */
    public Medium getMedium();

    /**
      * Returns the type of the NIC.
      *
      * @return                         type of the NIC.
      */
    public int getType();

    /**
      * Returns a string representation of the NIC object.
      *
      * @return                         String representation of the NIC
      */
    public String toString();

}
