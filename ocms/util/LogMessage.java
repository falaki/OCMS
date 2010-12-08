 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.util;


import java.math.BigDecimal;
import java.math.MathContext;
import java.lang.Comparable;


/**
  * A LogMessage instance represents a single log event in the simulator.
  *
  * @author     Hossein Falaki
  */
public class LogMessage implements Comparable
{
    /** the log message */
    String message;

    /** the time of the log message */
    Double time;

    /** The math context object for precision  */
    static MathContext mc;

    static 
    {
        mc = new MathContext(6);
    }

    /**
      * Constructs a LogMessage object with the provided information.
      *
      * @param      level           the desired log level
      * @param      source          the generator of the log
      * @param      msg             the log message
      */
    public LogMessage( int level, Logger source, String msg)
    {
        time = source.getTime();
        message = new String( Log.levels[level] + ":\t" + new BigDecimal( (time/60), LogMessage.mc ) + "\t " + source.getName() + " " + msg);
    }

    /**
      * Returns a string representation of the log message 
      *
      */
    public String toString()
    {
        return message;
    }

    /**
      * Returns the time of the log message.
      *
      * @return                     the time of the log message
      */
    public Double getTime()
    {
        return time;
    }

    /**
      * Compares the time of this message with the specified message for order.
      * Returns a negative integer, zero, or a positive integer as its time is less than, 
      * equal to, or greater than the specified message.
      * 
      * @param      logmessage      the log message to be campared
      * @return                     a negative integer, zero, or a positive integer as its time is less than, 
      *                             equal to, or greater than the specified message.
      */
    public int compareTo(Object logmessage)
    {
        return time.compareTo( ((LogMessage)logmessage).getTime() );
    }

}
