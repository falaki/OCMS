 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.medium;

 /**
   * This is an Exception that is thrown by an implementation of the {@link ocms.medium.Medium} Interface
   *
   * @author    Hossein Falaki
   */
public class MediumException extends Exception
{
    /**
     * Constructs a new exception with null as its detail message. 
     */
    public MediumException()
    {
        super();
    }

    /** 
      * Constructs a new exception with the specified detail message.
      *
      * @param message the detail message
      */
    public MediumException(String message)
    {
        super(message);
    }

    /**
      * Constructs a new exception with the specified detail message and cause.
      *
      * @param message  the detail message
      * @param cause    the cause
      */
    public MediumException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
      * Constructs a new exception with the specified cause.
      *
      * @param cause    the cause
      */
    public MediumException(Throwable cause)
    {
        super(cause);
    }

}
