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
import java.util.Arrays;

import java.math.MathContext;
import java.math.BigDecimal;


import ocms.medium.WiFiMedium;
import ocms.user.User;
import ocms.nic.WiFiNIC;
import ocms.nic.NIC;
import ocms.dataset.Tuple;
import ocms.util.Log;
import ocms.util.Block;
import ocms.util.Logger;
import ocms.schedulers.Scheduler;
import ocms.schedulers.SchedulerException;



/**
  * This is a hypothetical scheduler that always makes optimal decisions
  * for a user who wants to stay connected to WiFi access points to transmit
  * a fixed amount of data.
  * It first finds all the <i>availability blocks</i> in the medium of the 
  * network interface. Then it runs a simple greedy algorithm that picks the
  * next longest availability block. 
  * The queries are replied based on this pre-computed schedule.
  *
  *
  * @author     Hossein Falaki
  */
public class OptimalScheduler implements Scheduler, Logger
{

    /** The Medium to be looked at for the decisions */
    WiFiMedium medium;

    /** Set of blocks found in the medium */
    HashSet<Block> blockset;

    /** 
      * Optimal Scheduler does not have any notioin of time. 
      * This is here to help log messages.
      */
    double now;

    /** The simulation start and end time */
    double endtime;
    double starttime;

    /** The scheduling data transmission goal*/
    //double goal;

    /** Bit rate of the NIC */
    int bitrate;

    /** Energy cost of the NIC */
    double energycost;

    /** The schedule */
    Block[] schedule;

    /** Helps more efficient query processing */
    int lastindex;

    /**
      * Constructs an OptimalScheduler with the provided Medium.
      * After a scheduler is created it should be initialized with a call to
      * {@link #initialize}.
      *
      * @param      medium              the medium to be used 
      * @param      endtime             the end time of simulation
      * @param      starttime           the start time of the simulation
      * @param      wifinic             the WiFiNIC object that this scheduler
      *                                 should work with (for getting cost values)
      */
    public OptimalScheduler(WiFiMedium medium, double starttime, double endtime, WiFiNIC wifinic)
    {
        this.medium = medium;
        this.endtime = endtime;
        if (this.endtime == 0)
            this.endtime = wifinic.getMedium().getEndTime();

        this.starttime = starttime;
        this.bitrate = wifinic.getTxRate();
        this.energycost = wifinic.getFixedCosts();
        now = 0.0;
        lastindex = 0;;
    }

    /**
      * Initializes the scheduler.
      * It scans the medium and identifies all the availability blocks and keeps them
      * in a list. Then it picks the largest block until the 'goal' data transmission 
      * is met (considering the bit-rate of the NIC). This schedule is kept and the 
      * queries are replied accordingly.
      * When this method is called, the scheduler will use a deadline approach to 
      * scheduling. It will try to meet the data transmission goal within the 
      * specified goal.
      *
      * @param      goal                the total amount of data that the NIC
      *                                 should send
      * @param      deadline            the latest time that the goal should be met
      */
    public void initialize(double goal, double deadline)
    {

        return;
    }


    /**
      * Initializes the scheduler.
      * It scans the medium and identifies all the availability blocks and keeps them
      * in a list. Then it picks the largest block until the 'goal' data transmission 
      * is met (considering the bit-rate of the NIC). This schedule is kept and the 
      * queries are replied accordingly.
      * When this method is called the scheduler will use sensitivity factors for
      * optimal scheduling (Caution: delay sensitivity should be further studied)
      *
      * @param      goal                the total amount of data that the NIC
      *                                 should send
      * @param      energysens          the energy sensitivity factor
      * @param      delaysens           the delay sensitivity
      *
      */
    public void initialize(double goal, double energysens, double delaysens) 
    {
        double achievable = 0;

        Log.info(this, "initializing");

        blockset = new HashSet<Block>();

        int stime = medium.indexOf(starttime);
        int etime = medium.indexOf(endtime);
        int step = medium.getTimeStep();

        boolean inblock = false;

        double bstart = 0;
        double bend;

        /* Finds all the availability blocks in the medium and constructs
           the blocks */
        for (int time = stime; time <= etime; time += step)
        {
            if ( medium.scan(time) != null)
            {
                if ( (!inblock) && (medium.scan(time).size() != 0))
                {
                    inblock = true;
                    bstart = time;
                }
    
                if ((inblock) && (medium.scan(time).size() == 0))
                {
                    inblock = false;
                    blockset.add( new Block(bstart, time, energysens*energycost/(time - bstart) + delaysens*bstart) );
                }
            }
        }

        /* For the very last block */
        if (inblock)
            blockset.add( new Block(bstart, etime, energysens*energycost/(etime - bstart) + delaysens*bstart) );




        int i = 0;

        /* DEBUG 
        Block[] debugblocks = new Block[blockset.size()];
        for ( Block b : blockset )
        {
            b.setNaturalOrder(Block.STARTTIME);
            debugblocks[i++] = b;
        }

        Arrays.sort( debugblocks );

        Log.stdout(this, "All the blocks");
        for (int k=0; k < debugblocks.length; k++)
            Log.stdout(this, debugblocks[k].toString());

        END DEBUG */

        Block[] blocks = new Block[blockset.size()];

        /* puts the blocks in an array for sorting */
        i = 0;
        for ( Block b : blockset )
        {
            b.setNaturalOrder(Block.COST);
            blocks[i++] = b;
            achievable += b.getLength();
        }

        /* Sorts the blocks based on their length */
        Arrays.sort( blocks );



        i = 0;
        int j = 0;
        int achieved = 0;
        int lastachieved = 0;
        achievable *= bitrate;
        Block[] tempschedule = new Block[blockset.size()];

        Log.stdout(this, "Maximum capacity " + new BigDecimal(achievable, new MathContext(10)) );

        if (goal == 0)
            goal = achievable;

        while ((achieved < goal) && (i<blockset.size()) )
        {
            lastachieved = achieved;
            achieved += (blocks[i].getLength()) * bitrate;
            blocks[i].setNaturalOrder(Block.STARTTIME);
            tempschedule[j++] = blocks[i++];
        }

        if ( (achieved < goal) && ( i == blockset.size() ))
        {
            Log.info(this, "Goal " + goal + " is not achievable");
            Log.stdout(this, "Goal " + goal + " is not achievable");
        }
        else if ( achieved > goal )
        {
            tempschedule[j-1] = new Block( blocks[i-1].getStart(), blocks[i-1].getStart() 
                    + Math.ceil((goal - lastachieved)/bitrate)
                    , energysens*energycost + delaysens*blocks[i-i].getStart()  );
            tempschedule[j-1].setNaturalOrder(Block.STARTTIME);
        }
        /*
        else
        {
            Log.info(this, "Unknown error while initializing");
            Log.stdout(this, "Unknown error while initializing");
            System.err.println(getName() + "Unknown error while initializing");
        }
        */

        schedule = new Block[j];
        for (int k =0; k < j; k++)
            schedule[k] = tempschedule[k];

        Arrays.sort( schedule );

        Log.stdout(this, "\nScheduled blocks");
        for (int k=0; k < schedule.length; k++)
            Log.stdout(this, schedule[k].toString());

    }

    /**
      * Registers a new user to the scheduler.
      * This implementation of OptimalScheduler is agnostic to the user type. 
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
      *
      * @param      context             the state of the WiFiNIC and possibly the BSSID
      * @return                         the optimal COMMAND and the BSSID if action is ASSOCIATE
      * @throws     SchedulerException  if the medium is not available at the requested time
      */
    public ArrayList query(ArrayList context) throws SchedulerException
    {
        now = (Double) context.get(0);
        if (!medium.hasTime( now ) ) 
            throw new SchedulerException("The medium is not available at time " + now);


        ArrayList<Object> command = new ArrayList<Object>( 3 );
        HashSet futurestatus = (HashSet) medium.scan( now );
        int currentstate = (Integer)context.get(1);
        String ap = null;
        command.add(NIC.WiFi);

        int index = -1;
        for (int i = lastindex; i < schedule.length; i++)
        {
            if ( (schedule[i].getStart() <= now) && ( schedule[i].getEnd() > now ) )
            {
                index = i;
            }
        }

        if (index != -1)
        {
            lastindex = index;

            if (currentstate == WiFiNIC.CONNECTED)
            {
                command.add( WiFiNIC.DATA_TX );
                command.add(schedule[index].getEnd());
            }
            else if (currentstate == WiFiNIC.OFF)
            {
                command.add( WiFiNIC.ON );
            }
            else if ( currentstate == WiFiNIC.DISCONNECTED ) 
            {
                command.add( WiFiNIC.DISC_SCANNING );
            }
            else if ( currentstate == WiFiNIC.DISC_SCANNING)
            {
                ap = findNextAP( (Set)context.get(2) );
                if ( ap == null )
                    command.add(WiFiNIC.DISC_SCANNING); //Should not happen
                else
                {
                    command.add(WiFiNIC.CONNECTED);
                    command.add(ap);
                }
            }

            /*
            else if (currentstate == WiFiNIC.OFF)
            {
                command.add( WiFiNIC.CONNECTED );
                command.add(findNextAP( futurestatus ));
            }
            else if ( currentstate == WiFiNIC.DISCONNECTED ) 
            {
                command.add( WiFiNIC.CONNECTED );
                command.add(findNextAP( futurestatus ));
            }
            */

            Log.debug( this, " Query returned " + command );
            return command;
        }


        command.add( WiFiNIC.OFF );

        if ( now < schedule[0].getStart())
        {
            command.add(schedule[0].getStart());
            Log.debug( this, " Query returned " + command );
            return command;
        }

        if ( lastindex == (schedule.length - 1))
        {
            command.add( endtime);
        }
        else
        {
            for (int i = lastindex; i < schedule.length - 1; i++)
                if ( (now >= schedule[i].getEnd()) && ( now < schedule[i+1].getStart()) )
                    command.add(schedule[i + 1].getStart());
        }


        Log.debug( this, " Query returned " + command );
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
        return "OptimalScheduler";
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

