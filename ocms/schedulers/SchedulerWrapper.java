 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.schedulers;

import java.util.Iterator;
import java.util.ArrayList;


import ocms.eventqueue.Event;
import ocms.eventqueue.EventConsumer;
import ocms.eventqueue.EventQueue;
import ocms.nic.NIC;



/**
  * Wraps a scheduler object to interact with the EventQueue.
  * 
  * @author     Hossein Falaki.
  */
public class SchedulerWrapper implements EventConsumer 
{
    /** The scheduler object */
    Scheduler scheduler;

    /** The GSM NIC wrapper address */
    EventConsumer gsmnic;

    /** The WiFi NIC wrapper address */
    EventConsumer wifinic;

    /**
      * Constructs a wrapper object with the given scheduler object.
      *
      * @param      scheduler           the scheduler object
      * @param      gsmnic              the GSM network interface wrapper
      * @param      wifinic             the WiFi network interface wrapper
      */
    public SchedulerWrapper(Scheduler scheduler, EventConsumer gsmnic, EventConsumer wifinic)
    {
        this.scheduler = scheduler;
        this.gsmnic = gsmnic;
        this.wifinic = wifinic;
    }

    /**
      * Initializes the scheduler object within this wrapper. 
      * This method should be called before any other call to the
      * SchedulerWrapper.
      *
      * @param      goal                data transmission goal of the scheduler
      * @param      energysens          energy sensitivity of the scheduler
      * @param      delaysens           delay sensitivity of the scheduler
      */
    public void initialize(double goal, double energysens, double delaysens)
    {
        scheduler.initialize(goal, energysens, delaysens);
    }

    /**
      * Extracts the query parameters from the message and passes it on to the
      * scheduler.
      *
      * @param      event               the event to be handled
      * @param      eventqueue          reference to the caller of this method
      */
    public void handleEvent(Event event, EventQueue eventqueue)
    {
        ArrayList command = null;
        ArrayList<Object> context= new ArrayList<Object>();
        double nexttime = 0;
        EventConsumer dest;

        if (event.getType() == Event.STATUS )
        {
            nexttime = event.getTime();
            context.add(nexttime);
    
            if ( (event.getDetails() != null) && (event.getDetails().size() != 0 ) )
            {
                for (Iterator it=event.getDetails().iterator(); it.hasNext();)
                {
                    context.add(it.next());
                }
            }
    
            try
            {
                command = scheduler.query(context);
            }
            catch (Exception se)
            {
                System.err.println(getName() + ": Error received from the scheduler (" + scheduler.getName() + "): "
                        + " on query: " + context.toString() + "\n Exception details: " + se.toString() );
                se.printStackTrace();
            }

            int recp = (Integer) command.get(0);

            if (recp == NIC.GSM)
                dest = gsmnic;
            else
                dest = wifinic;
   
            try
            {
                eventqueue.enqueue( new Event( nexttime, Event.COMMAND, (EventConsumer)this, dest, command));
            }
            catch (Exception e)
            {
                System.err.println(getName() + ": Error while sending command to " + event.getSource().getName()
                        + e.toString() );
                e.printStackTrace();
            }
    
        }
        else
        {
            System.out.println(getName() + ": Received wrong event type");
        }

    }

    /**
      * Returns the name of the scheduler object.
      *
      * @return                         name of the internal scheduler object
      */
    public String getName()
    {
        return scheduler.getName() + "Wrapper";
    }

    /**
      * Returns the end time of the scheduler that is is wrapping.
      *
      * @return                         end time of the scheduler
      */
    public double getEndTime()
    {
        return scheduler.getEndTime();
    }


}
