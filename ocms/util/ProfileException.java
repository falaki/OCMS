 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.util;


 /**
   * This is an Exception that is thrown by Profile
   *
   * @author    Hossein Falaki
   */
public class ProfileException extends Exception
{
    /**
     * Constructs a new exception with null as its detail message. 
     */
    public ProfileException()
    {
        super();
    }

    /** 
      * Constructs a new exception with the specified detail message.
      *
      * @param message the detail message
      */
    public ProfileException(String message)
    {
        super(message);
    }

    /**
      * Constructs a new exception with the specified detail message and cause.
      *
      * @param message  the detail message
      * @param cause    the cause
      */
    public ProfileException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
      * Constructs a new exception with the specified cause.
      *
      * @param cause    the cause
      */
    public ProfileException(Throwable cause)
    {
        super(cause);
    }

}
