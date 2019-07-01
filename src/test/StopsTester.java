package test;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import com.graphhopper.PathWrapper;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import core.BestPathChoice;
import core.GeographicMap;
import core.NoInstructionsFoundException;
import core.NotExistingCoordinatesException;
import core.PathNotFoundException;

/**
 * Stops Tester: comparison of Jsprit algorithm results
 * with those calculated by transport service provider
 * @author Francesco Raco
 */
public class StopsTester
{
	/**
	 * String Builder storing a String representation of the comparison of Jsprit algorithm results
     * with those calculated by transport service provider
	 */
	private StringBuilder checkSB = new StringBuilder();
	
	/**
	 * Jsprit algorithm hits
	 */
	private int hits = 0;
	
	/**
	 * Total stops processed by Jsprit algorithm
	 */
	private int totalProcessedStops = 0;
	
	/**
	 * Provider best path distance cost
	 */
	private double providerBestPathDistance = 0;
	
	/**
	 * Provider best path time cost
	 */
	private long providerBestPathTime = 0;
	
	/**
	 * Jsprit algorithm best path distance cost
	 */
	private double algBestPathDistance = 0;
	
	/**
	 * Calculate provider best path costs
	 * @param map Geographic map
	 * @param providerTotalStops Provider total stops
	 * @param btp Best path choice
	 * @throws NoStopsFoundException No Stops Found Exception
	 * @throws PathNotFoundException Path Not Found Exception
	 * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
	 */
	private void calculateProviderBestPathCosts(GeographicMap map, List<String> providerTotalStops, BestPathChoice btp) throws NoStopsFoundException, NotExistingCoordinatesException, PathNotFoundException
	{
		//Total number of provider autobus stops
		final int SIZE = providerTotalStops.size();
		
		//Throw a specific exception if no stops found
		if (SIZE <= 0 || providerTotalStops == null) throw new NoStopsFoundException();
		
		//For every path between 2 adjacent points, increase the total distance cost
		for (int i = 0; i < SIZE - 1; i++)
		{
			PathWrapper path = map.getBestPath(btp, providerTotalStops.get(i), providerTotalStops.get(i + 1));
			providerBestPathDistance += path.getDistance();
			providerBestPathTime += path.getTime();
		}
	}
	
	/**
	 * calculate provider fastest path costs
	 * @param map Geographic map
	 * @param providerTotalStops Provider total stops 
	 * @throws PathNotFoundException Path Not Found Exception
	 * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
	 * @throws NoStopsFoundException No Stops Found Exception
	 */
	private void calculateProviderBestPathCosts(GeographicMap map, List<String> providerTotalStops) throws NoStopsFoundException, NotExistingCoordinatesException, PathNotFoundException
	{
		calculateProviderBestPathCosts(map, providerTotalStops, BestPathChoice.FASTEST);
	}
	
	/**
	 * Constructor with map, start point (associated to the TSP problem), TSP solution and
	 * a String list containing the correct order of stops calculated by
	 * the transport service provider
	 * @param map Geographic map
	 * @param startPoint Start point of TSP problem
	 * @param solution TSP Solution calculated by Jsprit algorithm
	 * @param providerOrderedStops String list containing the correct order of stops calculated by the transport service provider
	 * @throws PathNotFoundException Path Not Found Exception
	 * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
	 * @throws NoStopsFoundException No Stops Found Exception
	 * @throws NoInstructionsFoundException No Instructions Found Exception
	 */
	public StopsTester(GeographicMap map, String startPoint, VehicleRoutingProblemSolution solution,
			List<String> providerOrderedStops) throws NoStopsFoundException, NotExistingCoordinatesException, PathNotFoundException, NoInstructionsFoundException
	{
		//Calculate provider best path fastest costs
		calculateProviderBestPathCosts(map, providerOrderedStops);
		
		//Assign to algBestPathDistance the distance cost calculated by Jsprit algorithm
		algBestPathDistance = solution.getCost();
		
		//Get TSP tour activities list
    	List<TourActivity> tspSolutionActivities = map.getTspSolutionActivities(startPoint, solution);
		
		//Assign activities list to totalProcessedStops private field
		totalProcessedStops = tspSolutionActivities.size();
		
		//Provider activities (target locations do not include the start point)
		List<String> providerActivities = providerOrderedStops.subList(1, providerOrderedStops.size());
		
		//initialize String set which will eventually contain
		//wrong stops calculated by Jsprit algorithm
		Set<String> wrongStops = new TreeSet<String>();
		
		//Index which identifies a specific stop calculated by the algorithm
		int i = 0;
		
		//Index which identifies a specific stop by transport service provider
		//in the corresponding array
		int j = 0;
    	
    	//Iterate over all stops processed by Jsprit algorithm
		while (i < totalProcessedStops)
    	{
    		//Location returned by the Jsprit algorithm
			String algLocationId = tspSolutionActivities.get(i).getLocation().getId();
    		
    		//Location chosen by transport service provider
			String providerLocationId = providerActivities.get(j);
			
			//If algorithm has already mistaken current provider stop, then jump to the next one
			if (wrongStops.contains(providerLocationId))
			{
				j++;
				continue;
			}
			
			/*Append to checkSB the string representation of comparison */
			checkSB.append(algLocationId).append(": ");
    		
    		//If result is correct, then append corresponding hit string text to checkSB,
			//jump to next stop index and increase the number of hits
			if (algLocationId.equals(providerLocationId))
    		{
    			checkSB.append("SCELTA CORRETTA\n\n");
    			j++;
    			hits++;
    		}
    		else
    		{
    			//Add current algorithm stop to wrongStop set
    			wrongStops.add(algLocationId);
    			
    			//Append corresponding failure string text to checkSB
    			checkSB.append("SCELTA SBAGLIATA (\"").append(providerLocationId).append("\" ").append(" E\' LA SCELTA CORRETTA)\n\n");
    		}
			
			i++;
    	}
    	
		//Append to checkSB the statistic results of the Jsprit algorithm
		checkSB.append("\n").append("Percentuale di fermate scelte correttamente dall\' algoritmo Jsprit: [").append(hits).
		append(": scelte corrette] / [").append(totalProcessedStops).append(": numero totale fermate] = ").append(getHitsPercentual()).append("%\n\n");
		checkSB.append("Efficienza dell\' algoritmo Jsprit: {[").append(Math.round(algBestPathDistance)).append("m:").append(" distanza percorsa dall'algoritmo Jsprit] ; [").
		append(Math.round(providerBestPathDistance)).append("m:").append(" distanza percorsa dal fornitore del servizio]} --> ").append(getEfficiencyPercentual()).append("%");
	}
	
	/**
	 * Get String Builder storing the text representation of the comparison
	 * @return String Builder storing the text representation of the comparison
	 */
	public StringBuilder getCheckSB()
	{
		return checkSB;
	}
	
	/**
	 * Get provider best path distance
	 * @return Provider best path distance
	 */
	public double getProviderBestPathDistance()
	{
		return providerBestPathDistance;
	}
	
	/**
	 * Get provider best path time
	 * @return Provider best path time
	 */
	public long getProviderBestPathTime()
	{
		return providerBestPathTime;
	}
	
	/**
	 * Get Jsprit algorithm best path distance
	 * @return Jsprit algorithm best path distance
	 */
	public double getAlgBestPathDistance()
	{
		return algBestPathDistance;
	}
	
	/**
	 * Get hits percentual obtained by Jsprit algorithm
	 * @return Hits percentual obtained by Jsprit algorithm
	 */
	public double getHitsPercentual()
	{
		//Convert int to double values
		double doubleHits = hits;
		double doubleTotalProcessedStops = totalProcessedStops;
		
		//Hits percentual obtained by Jsprit algorithm
		return Math.round(100 * (doubleHits / doubleTotalProcessedStops));
	}
	
	/**
	 * Get efficiency percentual obtained by Jsprit algorithm
	 * @return Efficiency percentual obtained by Jsprit algorithm
	 */
	public double getEfficiencyPercentual()
	{
		//Difference from Jsprit algorithm path cost and transport service provider path cost
		double difference = algBestPathDistance - providerBestPathDistance;
		
		//Efficiency percentual obtained by Jsprit algorithm
		return Math.round(100 * (1 - difference / providerBestPathDistance));
	}
}
