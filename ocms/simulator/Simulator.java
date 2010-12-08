 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.simulator;


import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import ocms.dataset.DataSet;
import ocms.dataset.Sample;
import ocms.medium.Medium;
import ocms.medium.WiFiMedium;
import ocms.medium.GSMMedium;
import ocms.util.Configuration;
import ocms.util.Log;
import ocms.util.Logger;
import ocms.nic.NIC;
import ocms.nic.WiFiNIC;
import ocms.nic.GSMNIC;
import ocms.nic.WiFiNICWrapper;
import ocms.nic.GSMNICWrapper;
import ocms.schedulers.Scheduler;
import ocms.schedulers.SchedulerWrapper;
import ocms.schedulers.hypothetical.OptimalScheduler;
import ocms.schedulers.hypothetical.StepOptimalScheduler;
import ocms.schedulers.hypothetical.DumbScheduler;
import ocms.schedulers.heuristic.EBScheduler;
import ocms.schedulers.heuristic.LBScheduler;
import ocms.schedulers.heuristic.UserStaticScheduler;
import ocms.schedulers.caching.GSMCachingScheduler;
import ocms.schedulers.SchedulerException;
import ocms.eventqueue.EventQueue;
import ocms.eventqueue.Event;
import ocms.eventqueue.EventConsumer;
import ocms.eventqueue.EventQueueException;
import ocms.algorithms.statistics.Frequency;
import ocms.user.UIDataSet;


/**
  * The main class of the Opportunistic Connectivity Management Simulator (OCMS).
  * This is a single thread simulator for opportunistic connectivity management. 
  * The simulator object (created by the main method), reads the simulation scenario file.
  * This file specifies the following information:
  * <ul> 
  *     <li><b>Simulation Configuration:</b> 
  *             including: (a) network interface parameters (b) scheduling algorithm 
  *             (c) log file
  *     </li> 
  *     <li><b>Simulation Dataset:</b> 
  *             This are traces from real-world experiments that are used by the simulator to simulate the 
  *             same real-world scenario.
  *     </li>
  * </ul>
  * 
  *
  * @author     Hossein Falaki
  */
public class Simulator implements Logger
{
    /** Constants used in the Simulation class */

    /** These are the keys that will be passed to a Configuration object 
      * to get the corresponding values 
      */

    /** Number of interfaces */
    private final String NIC_NUM            = "interface_num";

    /** WIFI type value */
    private final String WIFI_TYPE          = "WIFI";

    /** Name of the interfaces Before passing it to the Configuration object add 'number' to the end. */
    private final String INTERFACE          = "interface_";

    /** To get the type of an interface */
    private final String _TYPE              = "_type";

    /** Data set file */
    private final String _FILE              = "_file";

    /** The logfile name */
    private final String LOGFILE           = "logfile_name";

    /** The log level */
    private final String LOG_LEVEL          = "log_level";

    /** The scheduler type */
    private final String SCHEDULER_TYPE     = "scheduler_type";

    /** OPTIMA: scheduler type value */
    private final String OPTIMAL            = "OPTIMAL";

    /** EB: scheduler type value */
    private final String EB                 = "EB";

    /** LB: scheduler type value */
    private final String LB                 = "LB";

    /** STATIC: scheduler type value */
    private final String STATIC             = "STATIC";

    /** USERSTATIC: scheduler type value */
    private final String USERSTATIC             = "USERSTATIC";


    /** STEPOPTIMAL scheduler type value */
    private final String STEPOPTIMAL        = "STEPOPTIMAL";

    /** DUMB scheduler type value */
    private final String DUMB               = "DUMB";

    /** The scheduler interface */
    private final String _INTERFACE         = "_interface";

    /** The scheduler GSM interface */
    private final String _GSMINTERFACE      = "_gsminterface";


    /** The start time of simulation */
    private final String START              = "start_time";
    
    /** The ending time of the simulation */
    private final String END                = "end_time";

    /** The command to the simulator object to finish simulation */
    public final static int FINISH          = 0;


    /** The configuration object */
    Configuration config;

    /** Array of interfaces */
    EventConsumer[] interfaces;

    /** Map of names to interface objects */
    HashMap<String, EventConsumer> interface_names;

    /** Map of interfaces to interface types */
    HashMap<EventConsumer, Integer> interface_types;

    /** Number of network interfaces */
    int nicnum;

    /** The scheduler object */
    SchedulerWrapper scheduler;

    /** the EventQueue object */
    EventQueue eq;

    /** Name of the simulator object */
    String name;

    /** the RTT of a query to the scheduler 
      * Currently it is not used and is set to zero 
      */
    double querytime;


    UIDataSet userDataSet;




   /**
      * The main method of the simulator.
      * Creates an object of the simulator and calls it {@link #run} method.
      * The address to the simulation scenario should be passed to the main method.
      *
      * @param      argv            list of orguments passed to the program. The first argument should be 
      *                             the address to the simulation scenario. The others are ignored.
      */
    public static void main(String[] argv)
    {
        Simulator sim = new Simulator(argv[0]);
        sim.run();
    }


    /**
      * Constructs an empty simulator object.
      * 
      */
    public Simulator()
    {
        nicnum = 0;
        config = null;
        interface_names = null;
        interface_types = null;
        name = "simulator";
        userDataSet = null;
    }


    /**
      * Constructs a simulation object with the given configuration file.
      * Parses the configuration file and uses its parameters to construct
      * the simulator object. It constructs an array of interfaces and 
      * initializes each one with an instance of the right type of NIC
      * with the corresponding parameters from the simulation scenario.
      *
      * @param      configfile      address of the configuration file on the file system.
      */
    public Simulator(String configfile)
    {
        name = "simulator";
        interface_names = new HashMap<String, EventConsumer>();
        interface_types = new HashMap<EventConsumer, Integer>();
        config =  new Configuration( configfile );
        nicnum = config.getInt(NIC_NUM);
        String nicname;
        DataSet dataset = null;
        Frequency freq;
        ArrayList<String> filterList;
        WiFiMedium wifimedium;
        GSMMedium gsmmedium;

        if (config.hasKey("user"))
            try
            {
                userDataSet = new UIDataSet(config.get("user"));
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

        // Initializing the Log
        if (config.get("STO").equals("OFF"))
            Log.disableSto();

        int loglevel = Log.OFF;
        if (config.get(LOG_LEVEL).equals("DEBUG") )
        {
            loglevel = Log.DEBUG;
            Log.stdout(this, "DEBUG logging enabled");
        }
        else if (config.get(LOG_LEVEL).equals("PARANOID") )
        {
            loglevel = Log.PARANOID;
            Log.stdout(this, "PARANOID logging enabled");
        }
        else if (config.get(LOG_LEVEL).equals("INFO") )
        {
            loglevel = Log.INFO;
            Log.stdout(this,"INFO logging enabled");
        }


        try
        {
            Log.initialize(loglevel, config.get(LOGFILE));
            Log.stdout(this, "logging to " + config.get(LOGFILE));
        }
        catch (Exception ioe )
        {
            System.err.println(name + ": Error in flushing the log file: " + ioe.toString());
            ioe.printStackTrace();
        }


        interfaces = new EventConsumer[nicnum];
        try
        {
            dataset = new DataSet( config.get("dataset"), config.getInt("dataset_timestep") );
        }
        catch (FileNotFoundException fnfe)
        {
            System.err.println(name + " Data set file dies not exist: " + fnfe.toString() );
            System.exit(1);
        }


        for (int i = 0; i < nicnum; i++)
        {
            nicname = config.get(INTERFACE + i );

            if ( (config.get(nicname + _TYPE )).equals("WIFI") )
            {
                freq = new Frequency( dataset, Frequency.WIFI );
                if (config.hasKey("dataset_filter"))
                {
                    filterList = config.getList("dataset_filter");
                    for (int filterindex = 0; filterindex < filterList.size(); filterindex++)
                        freq.filter( filterList.get(filterindex));
                }
                if (config.hasKey("dataset_filter_highpass_freq"))
                    freq.highpass( config.getDouble("dataset_filter_highpass_freq"));
    
                wifimedium = new WiFiMedium( dataset, nicname + "_Medium");
                interfaces[ i ] = new WiFiNICWrapper (new WiFiNIC( nicname
                        , config.getView(nicname)
                        , wifimedium ));
    
                interface_names.put(nicname, interfaces[ i ] );
                interface_types.put(interfaces[ i ], NIC.WiFi );
                Log.stdout(this, "Created and initialized " + nicname );

            }
            else if ( (config.get(nicname + _TYPE )).equals("GSM") )
            {
                gsmmedium = new GSMMedium( dataset, nicname + "_Medum");
                interfaces[ i ] = new GSMNICWrapper ( new GSMNIC (nicname, gsmmedium));
                interface_names.put(nicname, interfaces[ i ] );
                interface_types.put(interfaces[ i ], NIC.GSM );
                Log.stdout(this, "Created and initialized " + nicname );
            }


        }

        /* Creating the scheduler and the scheduler wrapper objects */
        if ( (config.get( SCHEDULER_TYPE )).equals(STEPOPTIMAL) )
        {
            Log.stdout(this, "Initializing StepOptimalScheduler");
            scheduler = new SchedulerWrapper( new StepOptimalScheduler( 
                                             ((WiFiNICWrapper)interface_names.get( config.get( STEPOPTIMAL + _INTERFACE ))).getNIC().getMedium()
                                             , config.getDouble(END) )
                        , (GSMNICWrapper)null
                        , ((WiFiNICWrapper)interface_names.get( config.get( STEPOPTIMAL + _INTERFACE ))) );
        } 
        else if ( (config.get( SCHEDULER_TYPE )).equals(DUMB) )
        {
            Log.stdout(this, "Initializing DumbScheduler");
            scheduler = new SchedulerWrapper( new DumbScheduler( config.getDouble(END)
                                                , ((WiFiNICWrapper)interface_names.get( config.get( DUMB + _INTERFACE ))).getNIC() )
                          , (GSMNICWrapper)null
                          , ((WiFiNICWrapper)interface_names.get( config.get( DUMB + _INTERFACE ))) ) ;

            config.add("DUMB_energysens", "0");
            config.add("DUMB_delaysens", "0");

        }
        else if ( (config.get( SCHEDULER_TYPE )).equals(OPTIMAL) )
        {
            Log.stdout(this, "Initializing OptimalScheduler");
            scheduler = new SchedulerWrapper( new OptimalScheduler (
                                            ((WiFiNICWrapper)interface_names.get( config.get( OPTIMAL + _INTERFACE ))).getNIC().getMedium()
                                           , config.getDouble(START) 
                                           , config.getDouble(END)
                                           , ((WiFiNICWrapper)interface_names.get( config.get( OPTIMAL + _INTERFACE ))).getNIC())
                        , (GSMNICWrapper)null
                        , ((WiFiNICWrapper)interface_names.get( config.get( OPTIMAL + _INTERFACE ))) );

        } 
        else if ( (config.get( SCHEDULER_TYPE )).equals(EB) )
        {
            Log.stdout(this, "Initializing EBScheduler");
            scheduler = new SchedulerWrapper( new EBScheduler( config.getDouble(END)
                                            , config.getDouble( EB + "_maxbackoff")
                                            , ((WiFiNICWrapper)interface_names.get( config.get( EB + _INTERFACE ))).getNIC() )
                        , (GSMNICWrapper)null
                        , ((WiFiNICWrapper)interface_names.get( config.get( EB + _INTERFACE ))));
            config.add("EB_energysens", "0");
            config.add("EB_delaysens", "0");

        }
        else if ( (config.get( SCHEDULER_TYPE )).equals(LB) )
        {
            Log.stdout(this, "Initializing LBScheduler");
            scheduler = new SchedulerWrapper( new LBScheduler( config.getDouble(END)
                                            , config.getDouble( LB + "_backoffstep")
                                            , config.getDouble( LB + "_initialbackoff")
                                            , ((WiFiNICWrapper)interface_names.get( config.get( EB + _INTERFACE ))).getNIC()
                                            , config.getBoolean( LB + "_randomize"))
                       , (GSMNICWrapper)null
                       , ((WiFiNICWrapper)interface_names.get( config.get( EB + _INTERFACE ))) );
            config.add("LB_energysens", "0");
            config.add("LB_delaysens", "0");

        }
        else if ( (config.get( SCHEDULER_TYPE )).equals(STATIC) )
        {
            Log.stdout(this, "Initializing GSMCachingScheduler");
            scheduler = new SchedulerWrapper( new GSMCachingScheduler( config.getDouble(END)
                                            , config.getDouble( STATIC + "_interval")
                                            , ((WiFiNICWrapper)interface_names.get( config.get( STATIC + _INTERFACE ))).getNIC()
                                            , config.getBoolean( STATIC + "_randomize"))
                        , ((GSMNICWrapper)interface_names.get( config.get( STATIC + _GSMINTERFACE )))
                        , ((WiFiNICWrapper)interface_names.get( config.get( STATIC + _INTERFACE ))));
            config.add("STATIC_energysens", "0");
            config.add("STATIC_delaysens", "0");

        }
        else if ( (config.get( SCHEDULER_TYPE )).equals(USERSTATIC) )
        {
            Log.stdout(this, "Initializing UserStaticScheduler");
            scheduler = new SchedulerWrapper( new UserStaticScheduler( config.getDouble(END)
                                            , config.getDouble( USERSTATIC + "_interval")
                                            ,
                                            ((WiFiNICWrapper)interface_names.get(
                                                config.get( USERSTATIC + _INTERFACE ))).getNIC()
                                            , userDataSet)
                        , (GSMNICWrapper)null
                        , ((WiFiNICWrapper)interface_names.get(
                                config.get(USERSTATIC + _INTERFACE ))));
            config.add("USERSTATIC_energysens", "0");
            config.add("USERSTATIC_delaysens", "0");

        }


        scheduler.initialize( config.getDouble( config.get(SCHEDULER_TYPE) + "_goal")
                , config.getDouble( config.get(SCHEDULER_TYPE) + "_energysens")
                , config.getDouble( config.get(SCHEDULER_TYPE) + "_delaysens") );



        // Creating the EventQueue object
        eq = new EventQueue(scheduler.getEndTime() );



    }

    /**
      * Returns the name of this object 
      *
      * @return                 name of this object
      */
    public String getName()
    {
        return name;
    }

    /**
      * Returns a default time value.
      *
      * @return                 always returns zero
      */
    public double getTime()
    {
        return 0.0;
    }


    /**
      * Runs the simulation.
      * Enqueues the start and end commands in the event queue and starts it.
      *
      */
    public void run()
    {
        ArrayList<Object> turnoncommand = new ArrayList<Object>(2);

        for (int i = 0; i < nicnum; i++)
        {
            if ( interface_types.get( interfaces[i] ) == NIC.WiFi )
            {
                turnoncommand.add(NIC.WiFi);
                turnoncommand.add((Integer) WiFiNIC.ON);
                try
                {
                    eq.enqueue( new Event( (double)config.getInt(START)
                                , Event.COMMAND
                                , (EventConsumer)scheduler 
                                , (EventConsumer)interfaces[i], turnoncommand ));
                }
                catch (EventQueueException e)
                {
                    System.err.println(name + ": Error in communication with the event queue: " + e.toString());
                    e.printStackTrace();
                }

            }
        }

        try
        {
            eq.run();
        }
        catch (Exception e)
        {
            System.err.println(name + ": Error while running the event queue: " + e.toString());
            e.printStackTrace();
        }


        for (int i = 0; i < nicnum; i++)
        {
            if (interface_types.get(interfaces[i]) == NIC.WiFi)
                System.out.println(interfaces[i].toString());
        }

        Log.flush();

    }
}
