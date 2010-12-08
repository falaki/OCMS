 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.nic;


 /**
   * This is an Exception that is thrown by an Interface
   *
   * @author    Hossein Falaki
   */
public class NICException extends Exception
{
    /**
     * Constructs a new exception with null as its detail message. 
     */
    public NICException()
    {
        super();
    }

    /** 
      * Constructs a new exception with the specified detail message.
      *
      * @param message the detail message
      */
    public NICException(String message)
    {
        super(message);
    }

    /**
      * Constructs a new exception with the specified detail message and cause.
      *
      * @param message  the detail message
      * @param cause    the cause
      */
    public NICException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
      * Constructs a new exception with the specified cause.
      *
      * @param cause    the cause
      */
    public NICException(Throwable cause)
    {
        super(cause);
    }

}
