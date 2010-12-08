 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.schedulers.heuristic;

import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;


import ocms.schedulers.Scheduler;
import ocms.schedulers.SchedulerException;
import ocms.dataset.Tuple;
import ocms.user.User;
import ocms.user.UIDataSet;
import ocms.util.Logger;
import ocms.util.Log;
import ocms.nic.NIC;
import ocms.nic.WiFiNIC;
import ocms.nic.GSMNIC;


/**
  * This is a heuristic scheduler that scans the medium at fixed intervals
  * and also when ever the user unlocks the screen.
  * 
  *
  * @author     Hossein Falaki
  */
public class UserStaticScheduler implements Scheduler, Logger
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

    /** The scanning interval*/
    double interval;

    /** The User dataset */
    ArrayList<Integer> usertimes;

    /** The last user time index */
    int lastuserindex;

    /** helper variable */
    double nextuser;

    /**
      * Constructs an instance of the scheduler that is ready for use.
      * A value of 0 for endtime means maximum possible time. Checks with 
      * the medium of the NIC to get the correct value for this.
      *
      * @param      endtime             end time of simulation
      * @param      wifinic             the WiFiNIC object that this scheduler should work 
      *                                 with (for getting cost values)
      * @param      interval            static scanning interval
      * @param      user                the user dataset to be used by the
      *                                 the scheduler
      */
    public UserStaticScheduler(double endtime, double interval, WiFiNIC wifinic, UIDataSet user)
    {
        now = 0.0;
        lastuserindex=0;
        nextuser = 0;
        this.endtime = endtime;
        this.bitrate = wifinic.getTxRate();
        if (endtime == 0)
            this.endtime = wifinic.getMedium().getEndTime();

        achieved = 0.0;
        this.interval = interval;

        this.usertimes = user.getTouchTimes();

    }

    /**
      * Initializes the scheduler.
      * UserStaticScheduler does not send any commands beyond end time.
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
        return "UserStaticScheduler";
    }

    /**
      * Registers a new user.
      * UserStaticScheduler is stateless and its behavior does not depend on the user,
      * therefore this method does not need to do anything.
      */
    public void registerUser(User user)
    {
        return;
    }

    /**
      * Returns the result of a query to the Scheduler.
      * This scheduler statically scans for WiFi APs in fixed intervals.
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
      *     is  no  AP to associate to, the command will be WiFiNIC.OFF and the next
      *     element will be the next time that the NIC should send a query. 
      *     </li>
      *     <li>If the current state is WiFiNIC.CONNECTED, then the command  will be 
      *     WiFiNIC.DATA_TX, <li>
      *     <li>If the current state is WiFiNIC.ASSOCIATION, it means the previous
      *     association command has failed. A new scan command is returned (this will
      *     cause a cache replace).
      *     </li>
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
        double txtime = 0;
        double nextscan = 0;

        now = (Double) context.get(0);
        Log.paranoid(this, "Query " + context);
        Set apscan;


        ArrayList<Object> command = new ArrayList<Object>(3);
        command.add(NIC.WiFi);

        if (context.size() < 2 )
        {
            throw new SchedulerException("Malformed context array");
        }

        int niccondition = (Integer) context.get(1);

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
                /* This case will never happen because I want to use this
                 * simulator with a goal of zero in all the simulations.
                 */
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
            {
                /* Send a scan command */
                command.add(WiFiNIC.DISC_SCANNING);
            }
            else 
            {
                /* This case will never happen because I want to use this
                 * simulator with a goal of zero in all the simulations.
                 */
                command.add(WiFiNIC.OFF);
                command.add(endtime);
            }
            wasconnected = false;
        }

        /* TODO: to be fixed */
        if (niccondition == WiFiNIC.DISC_SCANNING )
        {
            apscan = (Set)context.get(2);
            Log.debug(this, "WiFi Scan: " + apscan);
            String ap = findNextAP(apscan);
            if ( ap == null)
            {
                /* TODO:  Computing nextscan should be redone*/
                nextscan = now + interval;
                nextuser = getNextUserTime();
                if (nextscan > nextuser)
                {
                    Log.debug(this, "UserScan scheduled");
                    nextscan = nextuser;
                }

                command.add(WiFiNIC.OFF);
                if (nextscan > endtime)
                    command.add(endtime);
                else 
                    command.add(nextscan);
            }
            else
            {
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

        if (niccondition == WiFiNIC.ASSOCIATION)
        {
            /* The previous association command has failed. Order scan */
            command.add(WiFiNIC.DISC_SCANNING);
        }

        Log.debug(this, "Command: " + command);
        return command;
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
      * Returns the end time of the schedule that it will execute.
      *
      * @return                     end time assigned to the scheduler
      */
    public double getEndTime()
    {
        return endtime;
    }




    /**
      * Finds the next best AP from a list of available access points.
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
        if ( apset == null)
            return nextap;

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
      * Returns the next time that the user interacts with the phone.
      *
      * @return                         next time of user interaciton
      */
    private double getNextUserTime()
    {
        for(int i = lastuserindex; i < usertimes.size(); i++)
            if (usertimes.get(i) > now)
            {
                lastuserindex = i;
                return usertimes.get(i);
            }
        return Integer.MAX_VALUE;
    }

}
