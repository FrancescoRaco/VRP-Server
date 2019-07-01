package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cost Matrix Wrapper containing a string representation of the best path between every couple of points
 * @author Francesco Raco
 *
 */
public class CostMatrixWrapper
{
	/**
	 * Map containing a string representation of the best path (value) between every couple of points (key)
	 */
	private Map<List<String>, String> map = new HashMap<List<String>, String>();
	
	/**
	 * Create a list of points (the 2 points to be linked) by start and end points
	 * @param startPoint Start point
	 * @param endPoint End point
	 * @return List of the 2 points to be linked (first index is start, last index is destination)
	 */
	private List<String> createListPoints(String startPoint, String endPoint)
	{
		//List of the 2 points to be linked (first index is start, last index is destination)
		List<String> listPoints = new ArrayList<String>();
		listPoints.add(startPoint);
		listPoints.add(endPoint);
		
		//Return the list of 2 points (Start, End)
		return listPoints;
	}
	
	/**
	 * Default constructor
	 */
	public CostMatrixWrapper() {}
	
	/**
	 * Create Object specifying the map
	 * @param map Map containing a string representation of the best path between every couple of points
	 */
	public CostMatrixWrapper(Map<List<String>, String> map)
	{
		this.map = map;
	}
	
	/**
	 * Add to map the instructions related to the best path between start and end point
	 * @param startPoint Start point
	 * @param endPoint End point
	 * @param instructions Instructions related to the best path between start and end point
	 */
	public void addPathInstructions(String startPoint, String endPoint, String instructions)
	{
		//Create the list representing the key map (Start, End)
		List<String> listPoints = createListPoints(startPoint, endPoint);
		
		//Remove mapping if already contained
		if (map.containsKey(listPoints)) map.remove(listPoints);
		
		//Put mapping between (Start, End) and corresponding string instructions
		map.put(listPoints, instructions);
	}
	
	/**
	 * Get Path Instructions by start and end point
	 * @param startPoint Start point
	 * @param endPoint End point
	 * @return Path Instructions
	 * @throws NoInstructionsFoundException No Instructions Found Exception
	 */
	public String getPathInstructions(String startPoint, String endPoint) throws NoInstructionsFoundException
	{
		//(Start, End) String List representing the map key
		List<String> listPoints = createListPoints(startPoint, endPoint);
		
		//Instructions related to listPoints
		String instructions = map.get(listPoints);
		
		//If no instructions found, throw a specific Exception
		if (instructions == null) throw new NoInstructionsFoundException();
		
		//Return instructions
		return instructions;
		
	}
}
