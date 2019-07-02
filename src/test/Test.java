package test;

import java.util.List;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import core.BestPathChoice;
import core.GeographicMap;
import core.NoInstructionsFoundException;
import core.NotExistingCoordinatesException;
import core.PathNotFoundException;
import core.UncorrectQueryException;
import test.busExamples.Bus716Rome;
import test.busExamples.Bus30Rome;

/**
 * Test the efficiency of the program
 * @author Francesco Raco
 */
public class Test
{
    /**
     * Test Jsprit algorithm comparing its result with the stops order calculated by
     * transport service provider and its path distance with provider path distance
     * @param map Geographic Map
     * @param bus Bus
     * @return StopsTester object containing useful methods for the comparison
     * @throws PathNotFoundException Path Not Found Exception
     * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
     * @throws UncorrectQueryException Uncorrect Query Exception
     * @throws NoInstructionsFoundException No Instructions Found Exception
     * @throws NoStopsFoundException 
     */
    private static StopsTester testJspritAlgorithm(GeographicMap map, Bus bus) throws UncorrectQueryException, NotExistingCoordinatesException, PathNotFoundException, NoStopsFoundException, NoInstructionsFoundException
    {
    	//Get start and end point
    	String startPoint = bus.getStartPoint();
    	String endPoint = bus.getEndPoint();
    	
    	//Randomize stops order
    	bus.shuffleIntermediateStopsToBeProcessed();
    	
    	VehicleRoutingProblemSolution solution = map.solveTsp(startPoint, endPoint, bus.getIntermediateStopsToBeProcessed());
    	
    	//String list containing the correct order of stops
    	//calculated by transport service provider
    	List<String> providerOrderedStops = bus.getProviderOrderedTotalStops();
    	
    	//Return StopsTester object containing useful methods for the comparison
    	return new StopsTester(map, startPoint, solution, providerOrderedStops);
    }
    
    /**
     * Get Jsprit algorithm hits percentual
     * @param map Geographic map
     * @param bus Bus
     * @return Jsprit algorithm hits percentual
     * @throws NoInstructionsFoundException No Instructions Found Exception
     * @throws NoStopsFoundException No Stops Found Exception
     * @throws PathNotFoundException Path Not Found Exception
     * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
     * @throws UncorrectQueryException Uncorrect Query Exception
     */
    public static double getJspritAlgorithmHitsPercentual(GeographicMap map, Bus bus) throws UncorrectQueryException, NotExistingCoordinatesException, PathNotFoundException, NoStopsFoundException, NoInstructionsFoundException
    {
    	return testJspritAlgorithm(map, bus).getHitsPercentual();
    }
    
    /**
     * Get Jsprit algorithm testing info
     * @param map Geographic map
     * @param bus Bus
     * @return Jsprit algorithm testing info
     * @throws NoInstructionsFoundException No Instructions Found Exception
     * @throws NoStopsFoundException No Stops Found Exception
     * @throws PathNotFoundException Path Not Found Exception 
     * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
     * @throws UncorrectQueryException Uncorrect Query Exception
     */
    public static String getJspritAlgorithmTestingInfo(GeographicMap map, Bus bus) throws UncorrectQueryException, NotExistingCoordinatesException, PathNotFoundException, NoStopsFoundException, NoInstructionsFoundException
    {
    	return testJspritAlgorithm(map, bus).getCheckSB().toString();
    }
    
    /**
     * Get Jsprit algorithm solution info
     * @param map Geographic map
     * @param bus Bus
     * @return Jsprit algorithm solution info
     * @throws NoInstructionsFoundException No Instructions Found Exception
     * @throws PathNotFoundException Path Not Found Exception
     * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
     * @throws UncorrectQueryException Uncorrect Query Exception
     */
    public static String getJspritAlgorithmSolutionInfo(GeographicMap map, Bus bus) throws UncorrectQueryException, NotExistingCoordinatesException, PathNotFoundException, NoInstructionsFoundException
    {
    	return map.showTspSolution(bus.getStartPoint(), bus.getEndPoint(), bus.getIntermediateStopsToBeProcessed());
    }
    
    /**
     * Get GraphHopper best path between 2 specified locations
     * @param map Geographic map
     * @param btp Best Path Choice
     * @param from Start point
     * @param to End point
     * @return String representation of the best path between the 2 specified locations
     * @throws PathNotFoundException Path Not Found Exception
     * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
     */
    public static String getGraphHopperAlgorithmSolutionInfo(GeographicMap map, BestPathChoice btp, String from, String to) throws NotExistingCoordinatesException, PathNotFoundException
    {
    	return map.showBestPath(btp, from, to);
    }
    
    /**
     * Get GraphHopper fastest path between 2 specified locations
     * @param map Geographic map
     * @param from Start point
     * @param to End point
     * @return String representation of the best path between the 2 specified locations
     * @throws PathNotFoundException Path Not Found Exception
     * @throws NotExistingCoordinatesException Not Existing Coordinates Exception 
     */
    public static String getGraphHopperAlgorithmSolutionInfo(GeographicMap map, String from, String to) throws NotExistingCoordinatesException, PathNotFoundException
    {
    	return getGraphHopperAlgorithmSolutionInfo(map, BestPathChoice.FASTEST, from, to);
    }
    
    /**
     * Main method for a quick test of the program
     * @param args Default args
     * @throws PathNotFoundException Path Not Found Exception
     * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
     * @throws NoInstructionsFoundException No Instructions Found Exception
     * @throws UncorrectQueryException Uncorrect Query Exception
     */
    public static void main(String[] args) throws NotExistingCoordinatesException, PathNotFoundException, UncorrectQueryException, NoInstructionsFoundException
    {
    	
    	//String address1 = "Piazza San Giovanni in Laterano, Roma";
    	//String address2 = "Via Merulana 158, Roma";
    	
    	//Italy (geographic map)
    	//GeographicMap map = new Italy();
    	
    	//Transport line in Rome
    	//Bus bus = new Bus716Rome();
    	
    	//Show best path between 2 locations
    	//System.out.print(getGraphHopperAlgorithmSolutionInfo(map, address1, address2));
    	
    	//Show TSP solution for the Bus chosen
    	//System.out.print(getJspritAlgorithmSolutionInfo(map, bus));
    	
    	//Get Jsprit algorithm testing info
    	//System.out.print(getJspritAlgorithmTestingInfo(map, bus));
    }
}
