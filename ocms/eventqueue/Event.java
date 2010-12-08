 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.eventqueue;


import java.lang.Comparable;
import java.util.ArrayList;

/**
  * An event used for asynchronous communication between diffent classes through 
  * the EventQueue. 
  * Each event has a timestamp and a destination object (of type EventConsumer) and
  * a details Array. The details array will be interpreted by the destination of
  * the event. Essentially it is a protocol between the the event generator and
  * the event consumer. 
  *
  * @author     Hossein Falaki
  */
public class Event implements Comparable
{
    /** A constant for events of type STATUS */
    public static final int STATUS                              = 1;

    /** A constant for events of type COMMAND */
    public static final int COMMAND                             = 2;

    /** A constant for events related to GSM */
    public static final int GSM                                 = 3;

    /** The timestamp of the event */
    private Double time;

    /** The details of the event*/
    private ArrayList details;

    /** the destination of the event */
    private EventConsumer destination;

    /** The source of the event */
    private EventConsumer source;

    /** the type of the message */
    private int type;


    /**
      * Constructs an event with the given time, destination and details.
      * The destination should be an {@link ocms.eventqueue.EventConsumer} and 
      * its handleEvent() method will be called.
      *
      * @param      time            time to be set as the event timestamp
      * @param      destination     destination of the message
      * @param      source          the source of the event
      * @param      details         details of the event
      */
    public Event(double time, int type, EventConsumer source, EventConsumer destination, ArrayList details)
    {
        this.time = time;
        this.destination = destination;
        this.details = details;
        this.source = source;
        this.type = type;
    }


    /**
      * Sets the timestamp of the event.
      *
      * @return                     new timestamp of the event
      */
    public Double getTime()
    {
        return time;
    }

    /**
      * Returns the destination of the event
      *
      * @return                     destination of the event
      */
    public EventConsumer getDest()
    {
        return this.destination;
    }

    /**
      * Returns the source of the event.
      *
      * @return                     source of the event
      */
    public EventConsumer getSource()
    {
        return this.source;
    }

    /**
      * Compares the two arguments for an order. 
      * Returns a negative integer, zero, or a positive integer as the
      * first argument is less than, equal to, or greater than the second.
      * For this implementation the timestamps determine the ordering.
      *
      * @param      event           the event to be compared to
      * @return                     a negative integer, zero, or a positive 
      *                             integer as this is less than, 
      *                             equal to, or greater than the event.
      */
    public int compareTo(Object event)
    {
        return time.compareTo( (((Event)event).getTime()) );
    }


    /**
      * Returns the details of the event
      *
      * @return                     ArrayList containing the details of the event
      */
    public ArrayList getDetails()
    {
        return details;
    }

    /**
      * Returns the type of the event.
      * Should be checked against the constants of the {@link Event}.
      *
      * @return                     type value of the event
      */
    public int getType()
    {
        return type;
    }

    /**
      * Returns a string representation of the Event.
      *
      * @return                     a string representing the event
      */
    public String toString()
    {
        String result = "Event[time=" + time + ", type="+type + ", details="; 
        if (details != null)
            result += details.toString();
        else 
            result += "null";
        result += " From " + source.getName() + " To " + destination.getName() + "]";

        return result;
    }

}
