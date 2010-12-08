 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.schedulers.caching;

import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;


import ocms.schedulers.Scheduler;
import ocms.schedulers.SchedulerException;
import ocms.dataset.Tuple;
import ocms.user.User;
import ocms.util.Logger;
import ocms.util.Log;
import ocms.nic.NIC;
import ocms.nic.WiFiNIC;
import ocms.nic.GSMNIC;


/**
  * This is a heuristic scheduler that scans the medium at fixed intervals.
  * It uses GSM cell tower IDs as hits. It assumes that if the set of visible
  * cell IDs has not changed, the mobile user has not moved since the last
  * scan, therefore a new scan will not reveal any information.
  * It keeps a cache of previous WiFi scans and records the visible GSM cell 
  * IDs along with each WiFi scan. When it comes to a new WiFi scan, if the 
  * current visible set of cell IDs is the same as one of the recent records,
  * the scheduler uses the scan result from the cache instead.
  *
  * @author     Hossein Falaki
  */
public class GSMCachingScheduler implements Scheduler, Logger
{

    /** A constant for empty gsm cache result */
    private final String EMPTY                  = "EMPTY";

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

    /** If set, the pack off we be randomized */
    boolean randomize;

    /** mapping from GSM set to AP set */
    HashMap<Set<String>, Set> cache;

    Set<String> lastgsmset;


    /**
      * Constructs an instance of the scheduler that is ready for use.
      * A value of 0 for endtime means maximum possible time. Checks with 
      * the medium of the NIC to get the correct value for this.
      *
      * @param      endtime             end time of simulation
      * @param      wifinic             the WiFiNIC object that this scheduler should work 
      *                                 with (for getting cost values)
      * @param      interval            static scanning interval
      * @param      randomize           indicates if the process should be randomized
      */
    public GSMCachingScheduler(double endtime, double interval, WiFiNIC wifinic, boolean randomize)
    {
        now = 0.0;
        this.endtime = endtime;
        this.bitrate = wifinic.getTxRate();
        if (endtime == 0)
            this.endtime = wifinic.getMedium().getEndTime();

        achieved = 0.0;
        this.interval = interval;
        this.randomize = randomize;

        cache = new HashMap<Set<String>, Set>();

    }

    /**
      * Initializes the scheduler.
      * GSMCachingScheduler does not send any commands beyond end time.
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
        return "GSMCachingScheduler";
    }

    /**
      * Registers a new user.
      * GSMCachingScheduler is stateless and its behavior does not depend on the user,
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
      *     <li>If the current state is NIC.GSM, then the rest of the context array 
      *     contains the visible GSM cell tower IDs. This context is returned to 
      *     the scheduler when it issues a command with the first element set to NIC.GSM.
      *     </li>
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
        String cacheResult = null;
        Set apscan;


        ArrayList<Object> command = new ArrayList<Object>(3);
        command.add(NIC.WiFi);

        if (context.get(1).equals("GSM"))
        {
            cacheResult = gsmCache((Set)context.get(3));
            if ( cacheResult == null)
            {
                /* Cache miss: Send a scan command to the WiFi NIC */
                //Log.info(this, "GSMCache miss");
                command.add(WiFiNIC.DISC_SCANNING);
            }
            else
            {
                //Log.info(this, "GSM cache hit: " + cacheResult);
                /* Cache hit: Send the appropriate command to the WiFi NIC */
                if (cacheResult.equals(EMPTY))
                {
                    /* There is no AP to connect to: Turn the WiFi NIC off */
                    nextscan = now + interval;
                    command.add(WiFiNIC.OFF);
                    if (nextscan > endtime)
                        command.add(endtime);
                    else 
                        command.add(nextscan);
                }
                else
                {
                    /* Associate to cacheResult */
                    command.add(WiFiNIC.CONNECTED);
                    command.add(cacheResult);
                }
            }
            Log.debug(this, "Command: " + command);
            return command;
        }

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
                /* Instead of directly sending a scan command, send a GSM scan command */
                command.set(0, NIC.GSM);
                command.add(GSMNIC.SCANNING);
            }
            else 
            {
                command.add(WiFiNIC.OFF);
                command.add(endtime);
            }
            wasconnected = false;
        }

        if (niccondition == WiFiNIC.DISC_SCANNING )
        {
            apscan = (Set)context.get(2);
            Log.debug(this, "WiFi Scan: " + apscan);
            if ( (lastgsmset != null) && (lastgsmset.size() != 0))
                cache.put(lastgsmset, apscan);
            else
            {
                Log.debug(this, "Cache not updated");
            }
            String ap = findNextAP(apscan);
            if ( ap == null)
            {
                nextscan = now + interval;
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
      * Checks the cache of recent scans.
      * It assumes that the visible GSM cell tower IDs are placed in the arraylist
      * after the first element.
      *
      * @param      gsmresult       ArrayList object containing the visible GSM cell IDs
      * @returns                    the result of cache consulting 
      */
    private String gsmCache(Set gsmresult)
    {
        Set<String> cellids = new HashSet<String>();
        for (Object id : gsmresult)
            cellids.add((String)((Tuple)id).getKey());

        Log.paranoid(this, "Cache query: \nCache:" + cellids );
        Log.paranoid(this, "Cache contents: " + cacheToString());

        String result = null;
        if (gsmresult == null)
            return result;


        lastgsmset = cellids;

        if (cache.containsKey(cellids))
        {
            result = findNextAP(cache.get(cellids));
            Log.debug(this, "Cache hit: " + cache.get(cellids));
            if (result == null)
                result = EMPTY;
        }
        else
        {
            Log.debug(this, "Cache miss");
        }

        return result;
    }

    private String cacheToString()
    {
        StringBuffer sb = new StringBuffer("\n");
        for( Set key : cache.keySet())
            sb.append("Cache: " + key.toString() + "-->" + cache.get(key).toString() + "\n");

        return sb.toString();
    }


}
