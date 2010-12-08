 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.util;


import java.util.HashMap;
import java.math.BigDecimal;
import java.math.MathContext;



 /**
 * A Profile keeps and updates a profile of some aspect of a mobile device.
 * Each profile keeps track of the costs of some <i>state machine</i>.
 * There are two types of costs for each state: fixed <i>one-time cost</i>, and 
 * <i>running cost</i>. Each state transition incurs the transition one-time cost.
 * As long as the state variable keeps its value, the running-cost is 
 * computed,
 *
 * The transition cost may depend on both the current and the next state, while 
 * the running time only depends on the current state and the time spent in it.
 *
 *
 * @author  Hossein Falaki
 */
public class Profile
{
    /** name of the profile */
    private String name;

    /** The total cost up to now */
    private double cost;

    /** The latest time */
    private double now;

    /** The current state */
    private String state;

    /** Maps state names to their running-time costs */
    HashMap<String, Integer> runningtime;

    /** Maps state names to their default one-time costs */
    HashMap<String, Integer> defaults;

    /** Maps state names to a map of all other states, used for one-time costs */
    HashMap<String, HashMap<String, Integer>> onetime;

    /** The math context object for precision */
    MathContext mc;

    /** The unit of the cost */
    String unit;

    /**
      * Constructs a profile. 
      * Sets the total computed cost and the current time to zero.
      * Before being used the profile should be set up to know its states and their costs.
      * This constructor does not set the name of the profile.
      *
      */
    public Profile()
    {
        cost = 0.0;
        now = 0.0;
        state = null;
        unit = null;
        mc = new MathContext(20);
        runningtime = new HashMap<String, Integer>();
        defaults = new HashMap<String, Integer>();
        onetime = new HashMap<String, HashMap<String, Integer>>();
    }

    /**
      * Constructs a named profile. 
      * Sets the total computed cost and the current time to zero.
      * Before being used the profile should be set up to know its states and their costs.
      *
      * @param  name    name of the profile
      * @param  unit    unit of the cost
      */
    public Profile(String name, String unit)
    {
        this.name = name;
        this.unit = unit;
        cost = 0.0;
        now = 0.0;
        state = null;
        mc = new MathContext(20);
        runningtime = new HashMap<String, Integer>();
        defaults = new HashMap<String, Integer>();
        onetime = new HashMap<String, HashMap<String, Integer>>();
    }


    /**
      * Registers a State and its associated costs.
      * If the transition cost to this state from any other state is unique,
      * then the <i>fixedcost</i> will be used, otherwise {@link #setTransitionCost}
      * should be used. If the <i>newstate</i> has already been registered, throws a ProfileException.
      * 
      * @param  newstate       the state to be registered
      * @param  runningtime    the runningtime cost of the new state
      * @param  fixedcost      fixed cost used for transitioin to the new state from any other state
      */
    public void registerState(String newstate, Integer runningtime, Integer fixedcost) throws ProfileException
    {
       if (this.runningtime.keySet().contains(newstate) )
       {
           throw new ProfileException(name + ": State " + newstate + " has already been registered.");
       }
       else
       {
           this.runningtime.put(newstate, runningtime);
           defaults.put(newstate, fixedcost);
       }

       return;
    }

    /**
      * Registers the one-time transition cost.
      * If the transition cost from <i>currentstate</i> to <i>nextstate</i> is different from the
      * default fixed transition cost of the <i>nextstate</i>, it should be registered through
      * a call to this method. Both <i>currentstate</i> and <i>newstate</i> should be already 
      * registered otherwise a ProfileException is thrown.
      *
      * @param  currentstate    the state to transition from
      * @param  nextstate       the state to transition to
      * @param  transitioncost  the one-time transition cost from currentstate to nextstate
      */
    public void setTransitionCost(String currentstate, String nextstate, Integer transitioncost) throws ProfileException
    {
        if (!runningtime.keySet().contains(currentstate))
        {
            throw new ProfileException(name + ": State " + currentstate + " is not registered.");
        }

        if (!runningtime.keySet().contains(nextstate))
        {
            throw new ProfileException(name + ": State " + nextstate + " is not registered.");
        }

        HashMap<String, Integer> internalmap;
        if ( onetime.keySet().contains(currentstate) )
        {
            internalmap = onetime.get(currentstate);
            if (internalmap != null)
            {
                internalmap.put(nextstate, transitioncost);
                return;
            }
        }

        internalmap = new HashMap<String, Integer>();
        internalmap.put(nextstate, transitioncost);
        onetime.put(currentstate, internalmap);

    }

    /**
      * Initializes the profile.
      * Specifies what is the very initial state of the profile and what is the inital time. 
      * This method should be called once and only once, otherwise a ProfileException is thrown.
      *
      * @param  initialstate            the state to be set as the initial state
      * @param  initialtime             the time to be set as the initial time
      * @throws ProfileException        if it is called for the second time
      */
    public void initialize(String initialstate, double initialtime) throws ProfileException
    {
        if (state != null )
            throw new ProfileException(name + ": The profile has already been initialized");

        state = initialstate;
        now = initialtime;
    }


    /**
      * Computes the cost of transitioning to the new state.
      * Based on the already registered costs the cost of a transition from the current 
      * state to <i>newstate</i> at time <i>time</i> is computed. <i>time</i> should be
      * greater than the value used for the last call to this method, and <i>newstate</i> 
      * should be already registered, otherwise a ProfileException is thrown.
      * This method should only be called after the profile has been initialized with an
      * initial state, otherwise a ProfileException is thrown.
      *
      * @param  newstate            the state to transition to
      * @param  time                the time of the transition
      * @throws ProfileException    if the time is in the past or the state has not been registered
      */
    public void changeState(String newstate, double time) throws ProfileException
    {
        if ( time < now )
        {
            throw new ProfileException(name + ": Time " + time + " has passed. it is now " + now + ".");
        }

        if (!runningtime.keySet().contains(newstate))
        {
            throw new ProfileException(name + ": State " + newstate + " is not registered.");
        }

        if ( state == null)
        {
            throw new ProfileException(name + ": Profile has not been initialized with an inital state.");
        }

        /* Try to be robust here. If the new state is the same as the current state
         * Just return quietly.
         */
        if ( state.equals( newstate) )
        {
            return;
        }

        /* The running time of the current state up to now is first added */
        cost += runningtime.get(state)*(time - now);

        /* The transition cost is added */
        int fixed = defaults.get(newstate);

        if (onetime.keySet().contains(state))
            if (onetime.get(state).keySet().contains(newstate) )
                fixed = onetime.get(state).get(newstate);

        cost += fixed;

        /* The internal state and time are updated */
        now = time;
        state = newstate;

        return;
    }

    /**
      * Returns the current cost 
      *
      * @return     the computed cost up to now
      */
    public double getCost()
    {
        return cost;
    }

    /**
      * Returns a string representation of the profile
      *
      * @return     string representing the profile
      */
    public String toString()
    {
        return  name + ": " + new BigDecimal(cost, mc ) + " " + unit;
    }


}
