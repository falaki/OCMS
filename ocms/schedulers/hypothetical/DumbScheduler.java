 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.schedulers.hypothetical;

import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;


import ocms.schedulers.Scheduler;
import ocms.schedulers.SchedulerException;
import ocms.user.User;
import ocms.dataset.Tuple;
import ocms.util.Logger;
import ocms.util.Log;
import ocms.nic.WiFiNIC;
import ocms.nic.NIC;


/**
  * This is a hypothetical scheduler that makes simplistic and non-optimal
  * decisions for a user who wants to stay connected to WiFi as much as possible.
  * This scheduler simply commands the NIC to turn ON, scan for APs and connect to it every time
  * it is queried. Therefore this scheduler is absolutely 'stateless' and can be used
  * for multiple users.
  *
  * @author     Hossein Falaki
  */
public class DumbScheduler implements Scheduler, Logger
{
    /**
      * DumbScheduler does not have a notion of time. But it is needed
      * for Logger methods. After each query the time of the query is
      * put in this variable
      */
    double now;

    /** The time when simulation ends. No command beyond this pint */
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

    /**
      * Constructs an instance of the DumbScheduler that is ready to use.
      *
      * @param      endtime             end time of simulation
      * @param      nic                 the WiFiNIC object that this scheduler should work 
      *                                 with (for getting cost values)
      */
    public DumbScheduler(double endtime, WiFiNIC nic)
    {
        now = 0.0;
        this.endtime = endtime;
        if (this.endtime == 0)
            this.endtime = nic.getMedium().getEndTime();
        this.bitrate = nic.getTxRate();

        achieved = 0.0;
    }

    /**
      * Initializes the scheduler.
      * DumbScheduler does not send any commands beyond end time.
      * 
      * @param      goal                data transmission goal of the scheduler
      * @param      energysens          energy sensitivity of the scheduler
      * @param      delaysens           delay sensitivity of the scheduler
      */
    public void initialize(double goal, double energysens, double delaysens)
    {
        if (goal == 0 )
            this.goal = Integer.MAX_VALUE;
        else
            this.goal = goal;

        lastconnected = 0;
        wasconnected = false;
        return;
    }

    /**
      * Returns the name of the scheduler.
      *
      * @return                         name of the DumbScheduler
      */
    public String getName()
    {
        return "DumbScheduler";
    }

    /**
      * Registers a new user.
      * DumbScheduler is stateless and its behaviour does not depend on the user,
      * therefore this method does not need to do anything.
      */
    public void registerUser(User user)
    {
        return;
    }

    /**
      * Returns the result of a query to the DumbScheduler.
      * This scheduler follows a very simple algorithm to respond to queries.
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
      *     available APs and their signal strnegth. The command will be WiFiNIC.CONNECTED
      *     and also contains the BSSID of the AP to connect in the second element.</li>
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
                command.add(WiFiNIC.DISC_SCANNING);
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
      * Returns the DumbScheduler's notion of time.
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


}
