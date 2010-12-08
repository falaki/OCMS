 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */


package ocms.nic;

import ocms.util.Profile;
import ocms.util.ProfileException;
import ocms.util.Configuration;



/** 
  * Wraps a Profile object and configures and manges it as a WiFi NIC Power Profile.
  * An 802.11 'station' interface can be in one of the states specified in {@link WiFiNIC}
  * 
  *
  * @author Hossein Falaki
  */
public class WiFiNICPowerProfile
{
    /** The Profile object that keeps track of the power cost */
    Profile powerprofile;

    /** The name of this WiFi NIC */
    String name;

    /** Constant for the workd STATE */
    private static final String STATE           = "STATE";

    /** Constant used in the constructor for the default costs */
    private static final int DEFAULT_COST       = 0;


    /**
      * Constructs an empty WiFiNICPowerProfile instance with the no states registered.
      * The object construced by this constructor is not usable.
      */
    public WiFiNICPowerProfile()
    {
        powerprofile = new Profile();
        name = null;
    }

    /**
      * Constructs a ready-to-use WiFiNICPowerProfile.
      * The created instance is given a name and its states are initialized.
      *
      * @param      name                the name of the WiFi NIC
      * @param      conf                Configuration object used to extract 
      *                                 the cost values
      */
    public WiFiNICPowerProfile(String name, Configuration conf)
    {
        this.name = name;

        powerprofile = new Profile(name + " Power Profile");
        /* The exception is not passed up, because no exception should occur here. */
        try
        {
            powerprofile.registerState(STATE + WiFiNIC.OFF, 0, DEFAULT_COST);
            powerprofile.registerState(STATE + WiFiNIC.DISCONNECTED, conf.getInt("disc_run"), DEFAULT_COST);
            powerprofile.registerState(STATE + WiFiNIC.CONNECTED, conf.getInt("con_run") , DEFAULT_COST);
            powerprofile.registerState(STATE + WiFiNIC.DISC_SCANNING, conf.getInt("discscan_run"), DEFAULT_COST);
            powerprofile.registerState(STATE + WiFiNIC.CONNECTED_SCANNING, conf.getInt("conscan_run"), DEFAULT_COST);
            powerprofile.registerState(STATE + WiFiNIC.DATA_TX, conf.getInt("tx_run"), DEFAULT_COST);
            powerprofile.registerState(STATE + WiFiNIC.DATA_RX, conf.getInt("rx_run"), DEFAULT_COST);

            powerprofile.setTransitionCost( STATE + WiFiNIC.OFF, STATE + WiFiNIC.DISCONNECTED
                    , conf.getInt("off_to_disc") );
            powerprofile.setTransitionCost( STATE + WiFiNIC.DISCONNECTED, STATE + WiFiNIC.OFF
                    , conf.getInt("disc_to_off") );
            powerprofile.setTransitionCost( STATE + WiFiNIC.DISCONNECTED, STATE + WiFiNIC.CONNECTED
                    , conf.getInt("disc_to_con") );
            powerprofile.setTransitionCost( STATE + WiFiNIC.CONNECTED, STATE + WiFiNIC.DISCONNECTED
                    , conf.getInt("con_to_disc") );

        }
        catch (Exception e)
        {
            System.out.println("WiFiNICPowerProfile constructor got an exception " + e.toString() + 
                    " .This might be an implementation problem. Please report this bug to mhfalaki@uwaterloo.ca");
        }
    }

    /**
      * Initializes the Power Profile with an initial time.
      *
      * @param      time                the initial time
      */
    public void initialize(double time)
    {
        try
        {
            powerprofile.initialize(STATE + WiFiNIC.OFF, time);
        }
        catch (ProfileException pe)
        {
            System.out.println(name + " could not intialize profile " + pe.toString() );
        }
    }


    /**
      * Turns on the NIC.
      * Initally after turning on the WiFi is in DISCONNECTED state
      *
      * @param      time                the time to turn on the NIC
      * @throws     ProfileException    passes the possible exceptions from {@link Profile}
      */
    public void turnOn(double time) throws ProfileException
    {
        powerprofile.changeState(STATE + WiFiNIC.DISCONNECTED, time);
    }

    /**
      * Turns the WiFi NIC off.
      * 
      * @param      time                the time to turn the NIC off
      * @throws     ProfileException    passes the possible exceptions from {@link Profile}
      */
    public void turnOff(double time) throws ProfileException
    {
        powerprofile.changeState(STATE + WiFiNIC.OFF, time);
    }

    /**
      * Associates to an access point.
      * It is assumed that any association effort is successful, therefore
      * the NIC becomes CONNECTED after this call.
      *
      * @param      time                the time to become CONNECTED
      * @throws     ProfileException    passes the possible exceptions from {@link Profile}
      */
    public void connect(double time) throws ProfileException
    {
        powerprofile.changeState(STATE + WiFiNIC.CONNECTED, time);
    }

    /**
      * Disassociates from any access point it is connected to.
      * 
      * @param      time                the time to get disconnected
      * @throws     ProfileException    passess the possible exceptions from {@link Profile}
      */
    public void disconnect(double time) throws ProfileException
    {
        powerprofile.changeState(STATE + WiFiNIC.DISCONNECTED, time);
    }

    /**
      * Transmits data.
      *
      * @param     time                 the time to start transmission 
      * @throws     ProfileException    passess the possible exceptions from {@link Profile}
      */
    public void transmit(double time) throws ProfileException
    {
        powerprofile.changeState(STATE + WiFiNIC.DATA_TX, time);
    }

    /**
      * Receives data.
      *
      * @param      time                the time to start receiving
      * @throws     ProfileException    passess the possible exceptions from {@link Profile}
      */
    public void receive(double time) throws ProfileException
    {
        powerprofile.changeState(STATE + WiFiNIC.DATA_RX, time);
    }

    /**
      * Scans the medium.
      * There are different types of scanning. It could be ACTIVE or PASSIVE.
      * Also it could be done when the NIC is CONNECTED or DISCONNECTED.
      * Currently I am only implementing two types of scanning:
      * CONNECTED_SCANNING and DISCONNECTED_SCANNING both as passive scanning
      * TODO implement active scanning.
      *
      * @param      time                the time to start scanning
      * @param      scantype            the type of scanning
      * @throws     ProfileException    passess the possible exceptions from {@link Profile}
      */
    public void scan(double time, int scantype) throws ProfileException
    {
        if (scantype == WiFiNIC.CONNECTED_SCANNING )
            powerprofile.changeState(STATE + WiFiNIC.CONNECTED_SCANNING, time );

        if (scantype == WiFiNIC.DISC_SCANNING )
            powerprofile.changeState(STATE + WiFiNIC.DISC_SCANNING, time );
    }

    /**
      * Returns the consumed energy.
      *
      * @return                 the energy consumed by the WiFi NIC
      */
    public double getCost()
    {
        return powerprofile.getCost();
    }

    /**
      * Returns a string representation of the WiFiNICPowerProfile.
      *
      * @return                 string representing the WiFi NIC Energy Profile
      */
    public String toString()
    {
        return powerprofile.toString();
    }


}
