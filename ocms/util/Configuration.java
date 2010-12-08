 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.util;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileReader;




/**
  * The class used to read and parse a simulation scenario file.
  * The values presented in this file (refered to as the simulation scenario)
  * is then presented to other objects within the simulation.
  * A <i>Simulation Scenario</i> file is formatted as following:
  * <ul>
  *     <li> Each line consists of one and only one "key = value" pair. </li>
  *     <li> Blank lines are ignored</li>
  *     <li> Lines starting with # are comments and are therefore ignored </li>
  * </ul>
  *
  * @author     Hossein Falaki
  */
public class Configuration
{

    /** Maps the keys to their values */
    HashMap <String, String> values;

    /**
      * Constructs and empty and unusable Configuration object.
      */
    public Configuration()
    {
        values = new HashMap<String, String>();
    }

    /**
      * Constructs an instance of the Configuration.
      * Reads the Simulation scenario and pareses the entire file into the 
      * the internal data strucutre.
      *
      * @param      configfile          the file to be parsed as the simulation scenario
      */
    public Configuration(String configfile)
    {
        values = new HashMap<String, String>();
        Scanner input;
        int linenumber = 1;

        try
        {
            input = new Scanner( new FileReader(configfile));
            while(input.hasNextLine())
            {
                parseLine(input.nextLine(), linenumber++);
            }

            input.close();
        }
        catch (Exception e)
        {
            System.err.println("Fatal error while reading simulation scenareio" + e.toString());
        }
    }

    /**
      * Parses a single line of configuration.
      *
      * @param      line                the line to be parsed.
      * @param      linenumber          the number of the line being processed
      */
    private void  parseLine(String line, int linenumber)
    {
        if (line.startsWith("#") )
            return;

        if (line.length() == 0)
            return;

        int delimIndex = line.indexOf('=');
        if (delimIndex == -1 )
        {
            System.err.println("Invalid line in Simulation Scenario file. line: " + linenumber );
            return;
        }

        String key = line.substring(0, delimIndex).trim();
        String value = line.substring(delimIndex + 1).trim();

        if (values.containsKey(key))
            values.put(key, values.get(key) + "," + value);
        else
            values.put(key, value);
    }

    /**
      * Returns the value associated with the key.
      *
      * @param      key             the key
      * @return                      the value associated with the key
      */
    public String get(String key)
    {
        if ( values.get(key) == null)
        {
            System.err.println("Error in configuration file. No value exists for " + key);
        }
        return values.get(key);
    }

    /**
      * Parses the value associated with the key as an integer and returns it.
      *
      * @param      key             the key 
      * @return                     integer value 
      */
    public Integer getInt(String key)
    {
        int result = 0;

        if ( values.get(key) == null)
        {
            System.err.println("Error in configuration file. No value exists for " + key);
        }

        try
        {
            result = Integer.parseInt(values.get(key));
        }
        catch (NumberFormatException nfe)
        {
            System.err.println("Error: in simulation scenario file. The value of " + key + 
                    "should be an integer. I found (" + values.get(key) + ")\n");
        }

        return result;
    }


    /**
      * Parses the value associated with the key as a boolean and returns it.
      *
      * @param      key             the key 
      * @return                     boolean value 
      */
    public Boolean getBoolean(String key)
    {
        Boolean result = true;

        if ( values.get(key) == null)
        {
            System.err.println("Error in configuration file. No value exists for " + key);
        }

        if ( values.get(key).equals("true") )
            result = true;
        else if ( values.get(key).equals("false") )
            result = false;
        else 
            System.err.println("Error in configuration file. No value for " + key + " should be true or false");



        return result;
    }


    /**
      * Parses the value associated with the key as a double and returns it.
      * @param      key             the key
      * @return                     integer value
      */
    public Double getDouble(String key)
    {
        double result = 0.0;

        if ( values.get(key) == null)
        {
            System.err.println("Error in configuration file. No value exists for " + key);
        }

        try
        {
            result = Double.parseDouble(values.get(key));
        }
        catch (NumberFormatException nfe)
        {
            System.err.println("Error: in simulation scenario file. The value of " + key + 
                    "should be an double. I found (" + values.get(key) + ")\n");
        }

        return result;
    }

    /**
      * Returns an array list containing all the values associated with a key.
      *
      * @param      key             the key 
      */
    public ArrayList<String> getList(String key)
    {
        String keyvalue = this.get(key);
        ArrayList<String> result = new ArrayList<String>();
        if (keyvalue != null)
        {
            String[] items = keyvalue.split(",");
            for (int i=0; i < items.length; i++)
                result.add(items[i]);
        }

        return result;
    }




    /**
      * Adds the key and value pair to the internal map 
      *
      * @param      key             the key of the pair to be added
      * @param      value           the value of the pair to be added
      */
    public void add(String key, String value)
    {
        values.put(key, value);
    }

    /**
      * Returns a view over this object.
      * The returned Configuration only contains a subset of the values
      * of this object, those whose values start the the specified prefix
      * 
      * @param      prefix          the prefix to the keys of values 
      * @return                     a Configuration object with a subset of the values
      */
    public Configuration getView(String prefix)
    {
        Configuration returnconf = new Configuration();
        int delimIndex = 0;

        for ( String key: values.keySet() )
        {
            if (key.startsWith(prefix) )
            {
                delimIndex = key.indexOf("_");
                returnconf.add( key.substring( delimIndex + 1 ), values.get(key) );
            }
        }
        return returnconf;
    }

    /**
      * Returns true if the key exists in the configuration object.
      *
      * @param      key             the key to be checked
      * @return                     true if the key exists in the object
      */
    public boolean hasKey(String key)
    {
        return values.containsKey(key);
    }



}
