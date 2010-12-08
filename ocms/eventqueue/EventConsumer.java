 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.eventqueue;



/**
  * The interface to be implemented by any class that wishes to communicate asynchronously 
  * with other objects through the EventQueue.
  * 
  * @author     Hossein Falaki
  */
public interface EventConsumer
{
    /**
      * Receives an event whose destination is this object. 
      * This method should look into the details of the events and decide
      * based on that.
      * 
      * @param      event       the event to be handled
      * @param      eventqueue  reference to the caller of this method
      */
    public void  handleEvent(Event event, EventQueue eventqueue);

    /**
      * REturns the name of the EventConsumer object.
      *
      * @return                 name of the object
      */
    public String getName();

}
