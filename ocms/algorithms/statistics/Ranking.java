 /** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.algorithms.statistics;


import java.util.ArrayList;
import java.util.Collections;

import ocms.util.Block;

/**
  * A Ranking object gets a number of comparable objects and produces 
  * their ranking.
  *
  * @author     Hossein Falaki
  */

public class Ranking
{
    /* Keeps the objects to be ranked */
    ArrayList<Block> list;

    /**
      * Constructs an empty Ranking object.
      *
      */
    public Ranking()
    {
        list = new ArrayList<Block>();
    }

    /**
      * Adds the object to the list of objects.
      * 
      * @param      item            the item to be added to the list of objects.
      */
    public void add(Block item)
    {
        Block newblock = new Block(item);
        newblock.setNaturalOrder(Block.LENGTH);

        list.add(newblock);
    }

    /**
      * Returns a string representation of the ranking object.
      *
      * @return                     string representation of the ranking objects
      */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        int counter = 1;
        Collections.sort(list);

        for( Block item : list)
            sb.append(counter++ + "\t" + item.getLength() + "\t" + item.getStart() + "\t" + item.getEnd() + "\n");
            //sb.append(counter++ + "\t" + item.getLength() + " (" + item.getOwner() + ")\n");

        return sb.toString();
    }

    /**
      * Returns the internal list of items.
      * 
      * @return                     internal ArrayList of items
      */
    public ArrayList<Block> getItems()
    {
        return list;
    }


}

