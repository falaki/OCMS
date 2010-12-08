 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.schedulers.heuristic;

import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;


import ocms.schedulers.Scheduler;
import ocms.schedulers.SchedulerException;
import ocms.dataset.Tuple;
import ocms.user.User;
import ocms.util.Logger;
import ocms.util.Log;
import ocms.nic.WiFiNIC;
import ocms.nic.NIC;


/**
  * This is a heuristic scheduler that exponentially backs off when no AP is available.
  *
  * @author     Hossein Falaki
  */
public class EBScheduler implements Scheduler, Logger
{
    /** Keeps the latest time */
    double now;

    /** The time when simulation ends, no command beyond this pint */
    double endtime;

    /** The data transmission goal of the scheduler */
    double goal;

    /** The amount of data transmitted so far */
    double achieved;

    /** to keep track of the length of connections */
    double lastconnected;

    /** To remember if the previous state is CONNECTED*/
    boolean wasconnected;

    /** Bit rate of the WiFiNIC */
    int bitrate;

    /** The back off time */
    double backoff;

    /** The maximum back off time allowed */
    double maxbackoff;

    /**
      * Constructs an instance of the scheduler that is ready to use.
      * A value of 0 for endtime means maximum possible time. Checks with 
      * the medium of the NIC to get the correct value for this.
      *
      * @param      endtime             end time of simulation
      * @param      nic                 the WiFiNIC object that this scheduler should work 
      *                                 with (for getting cost values)
      * @param      maxbackoff          maximum allowed back-off time
      */
    public EBScheduler(double endtime, double maxbackoff, WiFiNIC nic)
    {
        now = 0.0;
        this.endtime = endtime;
        this.bitrate = nic.getTxRate();
        if (endtime == 0)
        {
            this.endtime = nic.getMedium().getEndTime();
        }

        achieved = 0.0;
        this.maxbackoff = maxbackoff;
        backoff = 1.0;
    }

    /**
      * Initializes the scheduler.
      * EBScheduler does not send any commands beyond end time.
      * 
      * @param      goal                data transmission goal of the scheduler
      * @param      energysens          energy sensitivity of the scheduler
      * @param      delaysens           delay sensitivity of the scheduler
      */
    public void initialize(double goal, double energysens, double delaysens)
    {
        this.goal = goal;
        lastconnected = 0;
        wasconnected = false;
        if (goal == 0)
            this.goal = Integer.MAX_VALUE;
    }

    /**
      * Returns the name of the scheduler.
      *
      * @return                         name of the Scheduler
      */
    public String getName()
    {
        return "EBScheduler";
    }

    /**
      * Registers a new user.
      * EBScheduler is stateless and its behavior does not depend on the user,
      * therefore this method does not need to do anything.
      */
    public void registerUser(User user)
    {
        return;
    }

    /**
      * Returns the result of a query to the Scheduler.
      * This scheduler follows an exponential back off algorithm to respond to queries.
      * The first element of the context array should be the time. That is the 
      * time when the user needs to make a decision. The second element of the 
      * context array should be the current state of the NIC.
      * <br>
      * <ul>
      *     <li>If the current state is WiFiNIC.OFF, then the command is WiFiNIC.ON</li>
      *     <li>If the current state is WiFiNIC.DISCONNECTED, then the command is 
      *     WiFiNIC.DISC_SCANNING.</li>
      *     <li>If the current state is WiFiNIC.DISC_SCANNING, then the next element of 
      *     the context array should contain a set of {@link ocms.dataset.Tuple}s of the
      *     available APs and their signal strength. The command will be WiFiNIC.CONNECTED
      *     and also contains the BSSID of the AP to connect in the second element. If there 
      *     no available AP to associate to, the command will be WiFiNIC.OFF and the next
      *     element will be the next time that the NIC should send a query. 
      *     </li>
      *     <li>If the current state is WiFiNIC.CONNECTED, then the command  will be 
      *     WiFiNIC.DATA_TX, <li>
      * </ul>
      * The scheduler keeps track of the amount of data sent and keeps the NIC OFF
      * when the goal (in terms of data transmission) has been met.
      *
      * @param      context             array of the context information
      * @return                         the scheduler decision
      * @throws     SchedulerException  if the context of the NIC does not follow the protocol
      */
    public ArrayList query(ArrayList context) throws SchedulerException
    {
        double backofftime = 0;
        double txtime = 0;

        now = (Double) context.get(0);
        Log.paranoid(this, "Query " + context);

        if (context.size() < 2 )
        {
            throw new SchedulerException("Malformed context array");
        }

        ArrayList<Object> command = null;
        int niccondition = (Integer) context.get(1);

        command = new ArrayList<Object>(3);
        command.add(NIC.WiFi);

        if (niccondition == WiFiNIC.CONNECTED)
        {
            if ( wasconnected )
                achieved += (now - lastconnected)*bitrate;

            if ( achieved < goal )
            {
                command.add(WiFiNIC.DATA_TX);
                txtime = (goal - achieved)/bitrate + now;
                if (txtime > endtime)
                    txtime = endtime;
                command.add(txtime);
                lastconnected = now;
                wasconnected = true;
            }
            else
            {
                command.add(WiFiNIC.OFF);
                command.add(endtime);
                wasconnected = false;
            }
        }

        if (niccondition == WiFiNIC.DISCONNECTED) 
        {
            if(wasconnected)
                achieved += (now - lastconnected)*bitrate;

            if (achieved < goal)
                command.add(WiFiNIC.DISC_SCANNING);
            else 
            {
                command.add(WiFiNIC.OFF);
                command.add(endtime);
            }


            wasconnected = false;
        }

        if (niccondition == WiFiNIC.DISC_SCANNING )
        {
            String ap = findNextAP( (Set)context.get(2) );
            if ( ap == null)
            {
                backoff *= 2;
                if (backoff > maxbackoff)
                    backoff = maxbackoff;

                backofftime = nextbackofftime();
                /*
                if ( now + backoff > endtime)
                    backofftime = endtime;
                else 
                    backofftime = now + backoff;
                */

                Log.debug(this, "Backing off until " + backofftime + " backoff: " + backoff + " and maxbackoff: " + maxbackoff );
                command.add(WiFiNIC.OFF);
                command.add(backofftime);
            }
            else
            {
                backoff = 1;
                command.add(WiFiNIC.CONNECTED);
                command.add(ap);
            }
        }

        if (niccondition == WiFiNIC.OFF )
        {
            if (achieved < goal)
                command.add(WiFiNIC.ON);
            else
                command.add(WiFiNIC.NOP);
        }

        Log.debug(this, "Command: " + command);

        return command;
    }


    /**
      * finds the next best AP from a list of available access points.
      * This implementation is favoring signal strength to longer future availability. 
      * Is it good or bad?
      *
      * @param      apset               a set of Tuples of BSSIDs and their signal strength
      * @return                         the BSSID of the AP with the highest signal strength
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
      * Returns the Scheduler's notion of time.
      * This is equivalent to the latest time a query was sent
      * to this scheduler.
      *
      * @return                             notion of the latest time
      */
    public double getTime()
    {
        return now;
    }

    /**
      * Returns a random value for the next scan based don the current back-off time.
      *
      * @return                     the next time to call scan
      */
    private double nextbackofftime()
    {
        double backofftime = now + ((Math.random() * (backoff/2)) + (backoff/2));
        if ( backofftime > endtime)
            backofftime = endtime;

        return Math.floor(backofftime);
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
