 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.schedulers;


 /**
   * This is an Exception that is thrown by an implementation of the Scheduler interface
   *
   * @author    Hossein Falaki
   */
public class SchedulerException extends Exception
{
    /**
     * Constructs a new exception with null as its detail message. 
     */
    public SchedulerException()
    {
        super();
    }

    /** 
      * Constructs a new exception with the specified detail message.
      *
      * @param message the detail message
      */
    public SchedulerException(String message)
    {
        super(message);
    }

    /**
      * Constructs a new exception with the specified detail message and cause.
      *
      * @param message  the detail message
      * @param cause    the cause
      */
    public SchedulerException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
      * Constructs a new exception with the specified cause.
      *
      * @param cause    the cause
      */
    public SchedulerException(Throwable cause)
    {
        super(cause);
    }

}
