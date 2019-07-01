package test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Bus
 * @author Francesco Raco
 */
public class Bus implements TransportLine
{
	/**
	 * Start point
	 */
	protected String startPoint;
	
	/**
	 * End point
	 */
	protected String endPoint;
	
	/**
	 * Provider ordered total stops
	 */
	protected List<String> providerOrderedTotalStops = new ArrayList<String>();
	
	/**
	 * Intermediate stops to be processed
	 */
	protected List<String> intermediateStopsToBeProcessed = new ArrayList<String>();
	
	/**
	 * Swap 2 stops of targetStops string list
	 * @param i Index of first stop
	 * @param j Index of second stop
	 */
	private void swap(int i, int j)
	{
		String firstStop = intermediateStopsToBeProcessed.get(i);
		intermediateStopsToBeProcessed.set(i, intermediateStopsToBeProcessed.get(j));
		intermediateStopsToBeProcessed.set(j, firstStop);
	}
	
	/**
	 * Create Bus object by start + end point and a variable numbers of stops
	 * @param startPoint Start point
	 * @param endPoint End point
	 * @param providerOrderedIntermediateStops Variable numbers of target Point
	 */
	public Bus(String startPoint, String endPoint, String... providerOrderedIntermediateStops)
	{
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		
		//Add start point to provider ordered total stops string list
		providerOrderedTotalStops.add(startPoint);
		
		//Add every intermediate stop to the string lists which store provider total stops and
		//stops to be processed by the Jsprit algorithm
		for (String stop : providerOrderedIntermediateStops)
		{
			providerOrderedTotalStops.add(stop);
			intermediateStopsToBeProcessed.add(stop);
		}
		
		//Add end point to provider ordered total stops string list
		providerOrderedTotalStops.add(endPoint);
	}
	
	@Override
	public String getStartPoint()
	{
		return startPoint;
	}
	
	@Override
	public String getEndPoint()
	{
		return endPoint;
	}
	
	@Override
	public List<String> getProviderOrderedTotalStops()
	{
		return providerOrderedTotalStops;
	}
	
	public List<String> getIntermediateStopsToBeProcessed()
	{
		return intermediateStopsToBeProcessed;
	}
	
	/**
	 * Randomize the order of intermediate stops to be processed
	 */
	public void shuffleIntermediateStopsToBeProcessed()
	{
		final int size = intermediateStopsToBeProcessed.size();
		if (size == 0) return;
		for (int i = 0; i < size; i++)
		{
			int j = new Random().nextInt(size);
			if (i != j) swap(i, j);
		}
	}
}
