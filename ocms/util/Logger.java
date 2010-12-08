 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.util;



/**
  * interface for classes that generate log messages.
  *
  * @author     Hossein Falaki
  */
public interface Logger
{
    /**
      * Returns the name of the object.
      *
      * @return                 name of the Logger class
      */
    public String getName();

    /**
      * Rerturns the time of the object.
      *
      * @return                 time of the Logger object
      */
    public double getTime();
}

