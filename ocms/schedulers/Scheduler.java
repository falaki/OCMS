 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.schedulers;

import java.util.ArrayList;

import ocms.user.User;




/**
  * The Scheduler interface defines what any scheduler should implement.
  * At each point of 'time' the scheduler should be able to issue a 'command' 
  * to be execued by a user. Each scheduler should be able to handle many users. 
  *
  * @author     Hossein Falaki
  */
public interface Scheduler
{

    /**
      * Queries the scheduler for an action.
      * The scheduler decides the action for the user based on the context 
      * information passed to it through the parameters.
      * The scheduling decision is returned through an ArrayList. To parse this 
      * array the caller should know what scheduler type it is calling (i.e. the
      * syntax and semantics of the reteurned values as well as the parameters are
      * decided by the implementation)
      *
      * @param          context             array of the information that might affect 
      *                                     the result of the query
      * @return                             the decision made by the scheduler
      * @throws         SchedulerException  if the scheduler has not been initialized or
      *                                     any other problem
      */
    public ArrayList query(ArrayList context) throws SchedulerException;

    /**
      * Initializes the scheduler.
      * One of the two initialize(*) methods should be called before any call to {@link #query}. 
      * If the scheduler works with  sensitivity factors: 
      * delay sensitivity and energy sensitivity this method should be called. The scheduler tries to meet 
      * a goal (in terms of data transmission) and at the same time minimize 
      * the total cost. The cost is:
      *             (delay_sensitivity x delay + energy_sensitivity x energy)
      *
      * @param          goal                data transmission goal of the scheudler
      * @param          energysens          energy sensitivity of the user
      * @param          delaysens           delay sensitivity of the user
      */
    public void initialize(double goal, double energysens, double delaysens);

    /**
      * Initializes the scheduler.
      * One of the two initialize(*) methods should be called before any call to {@link #query}. 
      * If the scheduler works with a deadline this method should be called. 
      * The scheduler will try meet the data transmission goal within the specified
      * deadline
      *
      * @param          goal                data transmission goal of the scheudler
      * @param          deadline            deadline to achieve the goal
    public void initialize(double goal, double deadline);
    */

    /**
      * Registers a new user in the scheduler.
      * This method may be called before or after initialization. 
      *
      * @param          user            a reference to the User object to be registered
      */
    public void registerUser(User user);


    /**
      * Returns the name of the scheduler.
      *
      * @return                         String name of the scheduler
      */
    public String getName();

    /**
      * Returns the end time of the schedule that it executes.
      *
      * @return                         end time assigned to the scheduler
      */
    public double getEndTime();

}
