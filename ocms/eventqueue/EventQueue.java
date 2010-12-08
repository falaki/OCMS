 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.eventqueue;


import java.util.PriorityQueue;

import ocms.util.Log;
import ocms.util.Logger;

/**
  * This class implements an eventqueue.
  * It has a PriorityQueue of {@link Event}s. In a tight loop it removes 
  * the head of queue and dispatches it to its destination, until the end 
  * of simulation.
  *
  * All other components of the simulation may enqueue events for delivery 
  * through the {@link #enqueue} method.
  *
  * @author     Hossein Falaki
  */
public class EventQueue implements Logger
{
    /** the internal PriorityQueue object */
    private PriorityQueue<Event> queue;

    /** the when simulation should end */
    private double endtime;

    /** Keeps the notion of 'past' */
    private double past;

    /** To keep track of time */
    private double now;

    /**
      * Constructs an event queue and sets the end time of the simulation.
      *
      * @param      endtime                 the time when simulation finishes
      */
    public EventQueue(double endtime)
    {
        queue = new PriorityQueue<Event>();
        this.endtime = endtime;
        past = Integer.MIN_VALUE;
    }


    /**
      * Enqueues the event in the queue.
      *
      * @param      event                   the event to be enqueued
      * @throws     EventQueueException     if the event's time is in the 'past'
      */
    public void enqueue(Event event) throws EventQueueException
    {
        if ( queue.size() == 0 )
        {
            queue.add(event);
            return;
        }

        if  (event.getTime() < past)
        {
            Log.stdout(this, event.toString());
            throw new EventQueueException("Cannot accept events for a time in the past. \n\t\t It is " + 
                    past + " now, whereas the event's time is " + event.getTime());
        }

        if  (event.getTime() > endtime)
            throw new EventQueueException("Cannot accept events for a time after the end of simulation (" +
                    endtime + ")");

        past = event.getTime();

        queue.add(event);
    }

    /**
      * Runs the main loop of the EventQueue.
      * Continuously polls the queue and delivers the events to their destinations.
      * When the handleEvent of the destination is called it has the opportunity to
      * enqueue another event in the queue.
      * 
      * @throws     EventQueueException     generated according to the possible exceptions from 
      *                                     the EventConsumers
      */
    public void run() throws EventQueueException
    {
        Event nextevent;

        while( (!queue.isEmpty() ) && (queue.peek().getTime() < endtime) ) 
        {
            nextevent = queue.poll();
            now = nextevent.getTime();

            Log.paranoid(this, "sending " + nextevent.toString());
            try
            {
                nextevent.getDest().handleEvent( nextevent, this );
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new EventQueueException("Exception while calling handleEvent of " 
                        + nextevent.getDest().getName()  + " on " + nextevent.toString() + ". Details: " + e.toString());
            }
        }
    }


    /**
      * Returns the name of the EventQueue.
      * 
      * @return                             "EventQueue"
      */
    public String getName()
    {
        return "EventQueue";
    }

    /**
      * Returns the simulation time when the method is called
      *
      * @return                             the current time in simulation
      */
    public double getTime()
    {
        return now;
    }
}

