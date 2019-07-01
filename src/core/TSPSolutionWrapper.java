package core;

import java.util.ArrayList;
import java.util.List;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * TSP Solution Wrapper containing the list of Tour Activity related to the TSP solution
 * and a String Builder storing a String representation of the result calculated by Jsprit algorithm
 * @author Francesco Raco
 */
public class TSPSolutionWrapper
{
	
	/**
	 * List of Tour Activity related to the TSP solution
	 */
	private List<TourActivity> solutionActivities = new ArrayList<TourActivity>();
	
	/**
	 * String Builder storing a String representation of the result calculated by Jsprit algorithm
	 */
	private StringBuilder sbSolution;
	
	
	/**
	 * Create object by solution, start point, Cost Matrix Wrapper
	 * and String Builder storing a String representation of the solution
	 * @param solution Solution
	 * @param startPoint Start point
	 * @param cmw Cost Matrix Wrapper
	 * @param sbSolution String Builder storing a String representation of the solution
	 * @throws NoInstructionsFoundException No Instructions Found Exception
	 */
	public TSPSolutionWrapper(VehicleRoutingProblemSolution solution, String startPoint, CostMatrixWrapper cmw, StringBuilder sbSolution) throws NoInstructionsFoundException
	{
		//Iterate over the collection of all paths (which vehicle has to travel)
		//included in the solution
		for (VehicleRoute v : solution.getRoutes())
		{
			//Add all Tour Activity to solutionActivities list			
			solutionActivities.addAll(v.getActivities());
			
			//If (sbSolution --> null) then jump to next VehicleRoute
			if (sbSolution == null) continue;
			
			//Create id locations list and add it the start point
			List<String> locations = new ArrayList<String>();
			locations.add(startPoint);
			
			//Add all other locations id to the locations list
			for (TourActivity ta : solutionActivities) locations.add(ta.getLocation().getId());
			
			//Number of locations
			final int SIZE = locations.size();
			
			//Add every location id to the String Builder containing a String representation
			//of the TSP solution
			for (int i = 0; i < SIZE; i++)
				{
					String thisPoint = locations.get(i);
					sbSolution.append(thisPoint);
					
					//Until second last location, append to sbSolution the String representation
					//of the best path between it and the next location
					if (i < SIZE - 1)
					{
						sbSolution.append(":\n\n");
						String endPoint = locations.get(i + 1);
						sbSolution.append(cmw.getPathInstructions(thisPoint, endPoint)).append("\n\n");
					}
				}
		}
		//Assign sbSolution to the private corresponding field
		this.sbSolution = sbSolution;
	}
	
	/**
	 * Constructor with solution and start point only
	 * @param solution Solution
	 * @param startPoint Start point
	 * @throws NoInstructionsFoundException No Instructions Found Exception
	 */
	public TSPSolutionWrapper(VehicleRoutingProblemSolution solution, String startPoint) throws NoInstructionsFoundException
	{
		this(solution, startPoint, null, null);
	}
	
	/**
	 * Get the list of Tour Activity related to the TSP solution
	 * @return List of Tour Activity related to the TSP solution
	 */
	public List<TourActivity> getSolutionActivities()
	{
		return solutionActivities;
	}
	
	/**
	 * Get the String Builder storing a String representation
	 * of the result calculated by Jsprit algorithm
	 * @return String Builder storing a String representation of the result calculated by Jsprit algorithm
	 */
	public StringBuilder getSBSolution()
	{
		return sbSolution;
	}
}
