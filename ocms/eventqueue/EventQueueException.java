 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.eventqueue;


 /**
   * This is an Exception that is thrown by the EventQueue
   *
   * @author    Hossein Falaki
   */
public class EventQueueException extends Exception
{
    /**
     * Constructs a new exception with null as its detail message. 
     */
    public EventQueueException()
    {
        super();
    }

    /** 
      * Constructs a new exception with the specified detail message.
      *
      * @param message the detail message
      */
    public EventQueueException(String message)
    {
        super(message);
    }

    /**
      * Constructs a new exception with the specified detail message and cause.
      *
      * @param message  the detail message
      * @param cause    the cause
      */
    public EventQueueException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
      * Constructs a new exception with the specified cause.
      *
      * @param cause    the cause
      */
    public EventQueueException(Throwable cause)
    {
        super(cause);
    }

}
