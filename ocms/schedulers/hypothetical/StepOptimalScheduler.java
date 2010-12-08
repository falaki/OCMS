 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.schedulers.hypothetical;


import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;


import ocms.medium.WiFiMedium;
import ocms.user.User;
import ocms.nic.WiFiNIC;
import ocms.nic.NIC;
import ocms.dataset.Tuple;
import ocms.util.Log;
import ocms.util.Logger;
import ocms.schedulers.Scheduler;
import ocms.schedulers.SchedulerException;



/**
  * This is a hypothetical scheduler that always makes optimal decisions
  * for a user who wants to stay connected to WiFi access point as much as
  * possible.
  * It makes optimal decisions through direct access to
  * the Medium at any time (i.e. it knows the future). An instance of this
  * class is constructed with a reference to such a medium. To answer each query
  * the StepOptimalScheduler looks up the medium at time 'now' and future then
  * issues a command accordingly considering the current state of the user.
  *
  *
  * @author     Hossein Falaki
  */
public class StepOptimalScheduler implements Scheduler, Logger
{

    /** The Medium to be looked at for the decisions */
    WiFiMedium medium;

    /** 
      * Optimal Scheduler does not have any notioin of time. 
      * This is here to help log messages.
      */
    double now;

    /** The time when simulation ends */
    double endtime;

    /**
      * The empty constructor creates an unusable instance of StepOptimalScheduler.
      *
      */
    public StepOptimalScheduler()
    {
        medium = null;
        now = 0.0;
        this.endtime = 0.0;
    }

    /**
      * Constructs an StepOptimalScheduler with the provided Medium.
      * After a scheduler is created it should be initialized with a call to
      * {@link #initialize}.
      *
      * @param      medium              the medium to be used 
      * @param      endtime             the end time of simulation
      */
    public StepOptimalScheduler(WiFiMedium medium, double endtime)
    {
        this.medium = medium;
        this.endtime = endtime;
        if (this.endtime == 0)
            this.endtime = medium.getEndTime();

        now = 0.0;
    }

    /**
      * Initializes the scheduler.
      * An optimal scheduler does not have much to do in terms of initialization
      * but this method should still be called before any other method of the 
      * scheduler is called.
      *
      * @param      goal                data transmission goal of the scheduler
      * @param      energysens          energy sensitivity of the scheduler
      * @param      delaysens           delay sensitivity of the scheduler
      *
      */
    public void initialize(double goal, double energysens, double delaysens) 
    {
        Log.info(this, "initializing");
    }

    /**
      * Registers a new user to the scheduler.
      * This implementation of StepOptimalScheduler is agnostic to the user type. 
      * This method specified by {@link ocms.schedulers.Scheduler} does nothing.
      *
      * @param      user                the user to be registered
      */
    public void registerUser(User user)
    {
        return;
    }

    /**
      * Returns the optimal next action to taken based on future knowledge.
      * The first element of the context array should be the time. That is the
      * time when the user needs to make a decision. It could be the current time
      * for the user or some time in the future. 
      *
      * StepOptimalScheduler looks one step into the future and 
      * returns the optimal decision in the first element of the  returned array 
      * based on the current NIC state:
      * <ul>
      * <li> If the current stat is WiFiNIC.OFF, then the command is WiFiNIC.ON</li>
      * <li> If the current state is WiFiNIC.DISCONNECTED, then the command is 
      *      WiFiNIC.CONNECTED and also contains the BSSID of the AP to connect 
      *     in the second element.</li>
      * <li> If the current state is WiFiNIC.CONNECTED, then the command will be 
      *     WiFiNIC.DATA_TX.</li>
      * </ul>
      *
      * @param      context             the state of the WiFiNIC
      * @return                         the optimal COMMAND and the BSSID if action is ASSOCIATE
      * @throws     SchedulerException  if the medium is not available at the requested time
      */
    public ArrayList query(ArrayList context) throws SchedulerException
    {

        now = (Double) context.get(0);
        Log.debug(this, "Query " + context);

        if (!medium.hasTime( now ) ) 
            throw new SchedulerException("The medium is not available at time " + now);

        if (context.size() < 2 )
            throw new SchedulerException("Malformed context array");

        int niccondition = (Integer) context.get(1);
        HashSet futurestatus = (HashSet) medium.scan( now );
        ArrayList<Object> command = new ArrayList<Object>( 3 );
        command.add(NIC.WiFi);

        if (futurestatus == null) 
        {
            command.add(WiFiNIC.NOP);
        }


        if (futurestatus.size() == 0 ) 
        {
            command.add(WiFiNIC.NOP);
        }

        if ( niccondition == WiFiNIC.OFF )
        {
            command.add( WiFiNIC.ON );
        }

        if ( niccondition == WiFiNIC.DISCONNECTED)
        {
            command.add( WiFiNIC.CONNECTED );
            command.add(findNextAP( futurestatus ));
        }
        
        if ( niccondition == WiFiNIC.CONNECTED )
        {
            command.add( WiFiNIC.DATA_TX );
            command.add( endtime );
        }

        /* If the user is connected to an AP, just check if it will be available and return DATA_TX */
        Log.debug( this, "Command " + command );
        return command;
    }

    /**
      * finds the next best AP from a list of available access points.
      * This implementation is favoring signal strength to longer future availability. 
      * Is it good or bad?
      *
      * @param      apset               a set of Tuples of BSSIDs and their signal strength
      * @return                         the BSSID of the AP with the highest signal strenght
      */
    private String findNextAP( Set apset )
    {
        int maxsignal = Integer.MIN_VALUE;
        String nextap = null;
        Tuple next;
        for (Iterator it=apset.iterator(); it.hasNext(); )
        {
            next = (Tuple) it.next();
            if (maxsignal < (Integer)next.getValue() )
            {
                maxsignal = (Integer) next.getValue();
                nextap = (String) next.getKey();
            }
        }

        return nextap;
    }

    /**
      * Returns the name of the scheduler.
      *
      * @return                         name of the scheduler
      */
    public String getName()
    {
        return "StepOptimalScheduler";
    }

    /**
      * Returns the time.
      * An optimal scheduler has no notion of time, This returns the time
      * seen in the latest query to the scheduler.
      *
      * @return                         always zero
      */
    public double getTime()
    {
        return now;
    }


    /**
    * Returns the end time of the schedule that it will execute.
    *
    * @return                     end time assigned to the scheduler
    */
    public double getEndTime()
    {
        return endtime;
    }



}
