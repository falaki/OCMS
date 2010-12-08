/** 
  * Opportunistic Connectivity Management Simulator
  *
  * Copyright (C) 2007 Hossein Falaki
  */

package ocms.util;


/**
  * This private class is used by the OptimalScheduler to keep the information of
  * a NIC availability block in the medium.
  * 
  * @author     Hossein Falaki
  */
public class Block implements Comparable<Block>
{

    /** Natural order values */

    /** A constant for ordering based on increasing start time */
    public static int STARTTIME             = 1;

    /** A constant for ordering based on decreasing length */
    public static int LENGTH                = 2;

    /** A constant for ordering based on increasing cost */
    public static int COST                  = 3;



    /** Start and end time of the block */
    Double starttime;
    double endtime;

    /** Length of the block */
    Double length;

    /** The cost per time  of this block */
    Double cost;

    /** Determines the natural order. */
    int naturalorder;

    /** Pointer to another object that owns this block */
    Object owner;

    /**
      * Constructs a new Block object and copied form the given one.
      *
      * @param      b           the block to be copied
      */
    public Block(Block b)
    {
        this.starttime = b.getStart();
        this.endtime = b.getEnd();
        this.owner = b.getOwner();

        naturalorder = STARTTIME;
        this.length = this.endtime - this.starttime;
        this.cost = (-1)*b.getLength();
    }


    /**
      * Constructs a block with the given parameters.
      * It sets the owner of the block.
      * It also sets the cost to the negative of the length of the block
      *
      * @param      start           start time of the block
      * @param      end             end time of the block
      * @param      owner           the owner of this block
      */
    public Block(double start, double end, Object owner)
    {
        this.starttime = start;
        this.endtime = end;
        this.owner = owner;

        naturalorder = STARTTIME;
        this.length = end - start;
        this.cost = (-1)*length;
    }

    /**
      * Constructs a block with the given parameters.
      * It also sets the cost to the negative of the length of the block
      *
      * @param      start           start time of the block
      * @param      end             end time of the block
      */
    public Block(double start, double end)
    {
        this.starttime = start;
        this.endtime = end;
        this.owner = null;

        naturalorder = STARTTIME;
        this.length = end - start;
        this.cost = (-1)*length;
    }




    /**
      * Constructs a block with the given parameters.
      *
      * @param      start           start time of the block
      * @param      end             end time of the block
      * @param      totalcost       total cost of the block
      */
    public Block(double start, double end, double totalcost)
    {
        this.starttime = start;
        this.endtime = end;
        this.cost = totalcost;
        this.owner = null;

        naturalorder = STARTTIME;

        this.length = end - start;
    }

    /**
      * Sets the owner of this block.
      *
      * @param      owner           owner of the block
      */
    public void setOwner(Object owner)
    {
        this.owner = owner;
    }

    /**
      * Sets the natural order of this block 
      *
      * @param     order           Constant value to specify the natural order
      */
    public void setNaturalOrder(int order)
    {
        this.naturalorder = order;
    }

    /**
      * Returns the end time of the block
      *
      * @return                     end time of the block 
      */
    public double getEnd()
    {
        return endtime;
    }


    /**
      * Returns the start time of the block
      *
      * @return                     start time of the block 
      */
    public double getStart()
    {
        return starttime;
    }


    /**
      * Returns the length of the block
      *
      * @return                     length of the block 
      */
    public Double getLength()
    {
        return length;
    }


    /**
      * Returns the cost per time of this block 
      *
      * @return                     cost per time
      */
    public Double getCost()
    {
        return cost;
    }

    /**
      * Returns the total cost of this block
      *
      * @return                     total cost of the block
    public double getTotalCost()
    {
        return cost;
    }
    */

    /**
      * Returns the natural order of this block.
      *
      * @return                     the natural order value
      */
    public int getNaturalOrder()
    {
        return naturalorder;
    }

    /**
      * Returns a string representation of the block 
      *
      * @return                     string representation of the block
      */
    public String toString()
    {
        /*
        String pre;
        if (owner != null)
            pre = "[" + owner.toString() + "<";
        else
            pre = "[<";
        return pre + starttime + ", " + endtime + ">:" +length + "]";
        */
        //return "<" + starttime + ", " + endtime + ">: " + length;
        //return "<" + starttime + ", " + endtime + ">: " + length + " (" + cost + " -> " + cost/length +")" ;
        return "" +length;
    }


    /**
      * Compares this block with the specified block for order based 
      * on the natural order value of this block. 
      * It is important that the natural order of both blocks be the 
      * same. If the blocks are being sorted the natural order of every
      * two blocks should be the same.
      *
      * @param      block           the block to be compared to
      * @return                     a negative integer, zero, or a positive integer
      *                             as the natural order of the  block is less, equal to,
      *                             or larger than this block
      */
    public int compareTo(Block block)
    {

        if (naturalorder != block.getNaturalOrder())
        {
            System.out.println("Programming error: " + this.toString() + " and " + block.toString() 
                    + " do not have the same natural order");
        }

        /* I am interested in the longer block first */
        if (naturalorder == LENGTH)
            return (-1)*length.compareTo(block.getLength());

        if (naturalorder == COST)
            return this.getCost().compareTo(block.getCost());


        /*The default natural order is STARTTIME */
        return starttime.compareTo(block.getStart());

    }


    /**
      * Returns the Owner of the block
      *
      * @return                 the owner of this block
      */
    public Object getOwner()
    {
        return owner;
    }

}

