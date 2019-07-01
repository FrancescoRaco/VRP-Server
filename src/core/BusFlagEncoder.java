package core;

import java.util.Arrays;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.util.PMap;

/**
 * Bus Flag Encoder
 * @author Francesco Raco
 *
 */
public class BusFlagEncoder extends CarFlagEncoder
{

    /**
     * Constructor with PMap object as argument, passing properties values of speed_bits, speed_factor and turn_costs with Overloading
     * @param properties Properties map with convenient accessors
     */
	public BusFlagEncoder(PMap properties)
    {
        this(
                (int) properties.getLong("speed_bits", 5),
                properties.getDouble("speed_factor", 5),
                properties.getBool("turn_costs", false) ? 1 : 0
        	);
        
        //Assign properties argument to corresponding field
        this.properties = properties;
        
        //Set Block Fords to corresponding boolean value of block_fords key
        this.setBlockFords(properties.getBool("block_fords", true));
    }
    
    /**
     * No arguments constructor passing Car Flag Encoder values with Overloading
     */
	public BusFlagEncoder()
    {
        this(5, 5, 0);
    }

    /**
     * Constructor with string property argument by which create a PMap object to pass with Overloading
     * @param propertiesStr String property argument by which create a PMap object to pass with Overloading
     */
	public BusFlagEncoder(String propertiesStr)
    {
        this(new PMap(propertiesStr));
    }

    /**
     * Constructor with 3 arguments: speed bits, speed factor, max turn costs
     * @param speedBits Speed bits
     * @param speedFactor Speed factor
     * @param maxTurnCosts Max turn costs
     */
    public BusFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts)
    {
    	//Pass the 3 arguments to super constructor
    	super(speedBits, speedFactor, maxTurnCosts);
    	
    	//Remove car restrictions and add the most appropriate ones
    	restrictions.clear();
        restrictions.addAll(Arrays.asList("service", "bus", "psv", "bus:lanes", "psv:lanes", "vehicle", "access"));
        
        //Add the most appropriate intended values
        intendedValues.add("psv");
        intendedValues.add("bus");
        intendedValues.add("no|yes");
        intendedValues.add("yes|no");
        intendedValues.add("no|no|yes");
        intendedValues.add("no|yes|no");
        intendedValues.add("yes|no|no");
        intendedValues.add("designated");
        

        //Remove bus barriers
        absoluteBarriers.remove("bus_trap");
        absoluteBarriers.remove("sump_buster");
    }

    /**
     * Choose correct bus string value so that Encoding Manager can identify this Flag Encoder
     */
    @Override
    public String toString()
    {
        return "bus";
    }
}