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
import ocms.medium.WiFiMedium;
import ocms.dataset.Tuple;
import ocms.util.Logger;
import ocms.util.Log;

/**
  * Wraps a NIC object to interact with the EventQueue.
  *
  * @author     Hossein Falaki
  */
public class WiFiNICWrapper implements EventConsumer, Logger
{
    /** The NIC object */
    WiFiNIC nic;

    /** Keeps the time */
    double now;

    /** Name of the WiFiNICWrapper */
    String name;

    /**
      * Constructs a wrapper object with the given WiFi NIC object.
      * And initializes the WiFi NIC.
      *
      * @param      nic         the network interface to be wrapped
      */
    public WiFiNICWrapper(WiFiNIC nic)
    {
        this.nic = nic;
        this.name = "WiFiNICWrapper";
        try
        {
            this.nic.initialize(((WiFiMedium)nic.getMedium()).getStartTime() );
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
      * WiFiNICWrapper can only handle events of type Event.COMMAND. The 
      * first element should be ignored (it is the type of the NIC). The
      * second element of the details field if such an event is the next
      * stat that the NIC should transition to. If this command requires
      * any arguments, they are found in the next elements of the details
      * array. <br>
      * After the command is executed by the WiFiNIC, an event of type
      * Event.STATUS is sent back. The first element of the details array of this 
      * event should be the current state of the NIC. The time of this event is 
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

        nic.step(event.getTime());

        int command = (Integer)event.getDetails().get(1);

        if ( command == WiFiNIC.OFF )
        {
            /* No other details are needed for this command */
            now = nic.turnOff();
            Log.debug(this, "Going to step to " + (Double)event.getDetails().get(2));
            nic.step((Double)event.getDetails().get(2));
            /* This is just to update now and has no side effect */
            now =  nic.turnOff();
            details.add(nic.checkState().get(0));
        }

        if ( command ==  WiFiNIC.ON )
        {
            /* No other details are needed for this command */
            now = nic.turnOn();
            details.add(nic.checkState().get(0));
        }

        if ( command == WiFiNIC.CONNECTED )
        {
            /* The BSSID to connect to should be the next element of details 
             of the event */
            try
            {
                now = nic.associate((String)event.getDetails().get(2) );
                details.add(nic.checkState().get(0));
                details.add(nic.checkState().get(1));

            }
            catch (NICException ne)
            {
                details.add(nic.ASSOCIATION);
            }
        }


        if ( command == WiFiNIC.DISC_SCANNING)
        {
            /* No other details are needed for this command */
            details.add(WiFiNIC.DISC_SCANNING);
            HashSet<Object> scanresult = new HashSet<Object>();
            now = nic.scan(scanresult);
            details.add(scanresult);
        }

        if (command == WiFiNIC.DATA_TX )
        {
            /* The end time of data transmission should be the next element of details 
            of the event */
            double endtime = (Double) event.getDetails().get(2);
            now = nic.transmit( endtime );
            details.add( nic.checkState().get(0));
        }

        if (command == WiFiNIC.NOP )
        {
            now = nic.nop();
            details.add( nic.checkState().get(0));
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
      * Returns the WiFi NIC object.
      *
      * @return                 the WiFi NIC object
      */
    public WiFiNIC getNIC()
    {
        return nic;
    }

    /**
      * Returns the type of the WiFi NIC
      *
      * @return                 type of the WiFi NIC
      */
    public int getType()
    {
        return nic.getType();
    }




}
