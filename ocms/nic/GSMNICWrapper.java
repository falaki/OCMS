 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.nic;

import java.util.ArrayList;
import java.util.HashSet;

import ocms.eventqueue.Event;
import ocms.eventqueue.EventQueue;
import ocms.eventqueue.EventConsumer;
import ocms.medium.GSMMedium;
import ocms.dataset.Tuple;
import ocms.util.Logger;
import ocms.util.Log;

/**
  * Wraps a GSM NIC object to interact with the EventQueue.
  *
  * @author     Hossein Falaki
  */
public class GSMNICWrapper implements EventConsumer, Logger
{
    /** The NIC object */
    GSMNIC nic;

    /** Keeps the time */
    double now;

    /** Name of the GSMNICWrapper */
    String name;

    /**
      * Constructs a wrapper object with the given GSM NIC object.
      * And initializes the GSM NIC.
      *
      * @param      nic         the network interface to be wrapped
      */
    public GSMNICWrapper(GSMNIC nic)
    {
        this.nic = nic;
        this.name = "GSMNICWrapper";
        try
        {
            this.nic.initialize(((GSMMedium)nic.getMedium()).getStartTime() );
        }
        catch (Exception e)
        {
            System.err.println( name + ": Error in initializing the interface " + nic.getName() 
                    + ": " + e.toString() );
            e.printStackTrace();
        }
    }

    /**
      * Parses the contents of the event and calls the necessary method 
      * of the NIC object.
      * GSMNICWrapper can only handle events of type Event.COMMAND and 
      * the only such command is scan.
      * After the scan command is executed by the GSMNIC, an event of type
      * Event.STATUS is sent back. The first elements of the details array of this 
      * event are the current visible GSM cell tower IDs. The time of this event is 
      * set to the current time  of the NIC.
      *
      * @param      event       the event to be handled
      * @param      eventqueue  reference to the caller of this method
      */
    public void handleEvent(Event event, EventQueue eventqueue)
    {
        now = event.getTime();

        if (event.getType() != Event.COMMAND )
        {
            Log.info(this, "do not know how to handle events of type " + event.getType() );
            return;
        }

        Log.paranoid(this, "received " + event.toString());

        ArrayList<Object> details = new ArrayList<Object>();
        details.add("GSM");

        nic.step(event.getTime());

        int command = (Integer)event.getDetails().get(1);

        if ( command == GSMNIC.OFF )
        {
            /* No other details are needed for this command */
            now = nic.turnOff();
            Log.debug(this, "Going to step to " + (Double)event.getDetails().get(2));
            nic.step((Double)event.getDetails().get(2));
            /* This is just to update now and has no side effect */
            now =  nic.turnOff();
            details.add(nic.checkState().get(0));
        }

        if ( command ==  GSMNIC.DISCONNECTED )
        {
            /* No other details are needed for this command */
            now = nic.turnOn();
            details.add(nic.checkState().get(0));
        }

        if ( command == GSMNIC.SCANNING)
        {
            /* No other details are needed for this command */
            details.add(NIC.GSM);
            HashSet<Object> scanresult = new HashSet<Object>();
            now = nic.scan(scanresult);
            details.add(scanresult);
        }


        //Log.debug(this, "sending"  + event.getDetails() + " to " + event.getSource().getName() );

        try
        {
            eventqueue.enqueue( new Event(now, Event.STATUS, this, event.getSource(), details));
        }
        catch (Exception e)
        {
            System.out.println(name + " could not send reply message. " + e.toString() );
        }

    }

    /**
      * Returns a string representation of the NIC.
      *
      * @return                     String representation of the NIC
      */
    public String toString()
    {
        return nic.toString();
    }

    /**
      * Returns the name of the NIC.
      *
      * @return                 name of the network interface
      */
    public String getName()
    {
        return this.nic.getName() + "Wrapper";
    }

    /**
      * Returns the current time.
      *
      * @return                 current time 
      */
    public double getTime()
    {
        return now;
    }


    /**
      * Returns the GSM  NIC object.
      *
      * @return                 the GSM NIC object
      */
    public GSMNIC getNIC()
    {
        return nic;
    }

    /**
      * Returns the type of the GSM NIC
      *
      * @return                 type of the GSM NIC
      */
    public int getType()
    {
        return nic.getType();
    }




}
