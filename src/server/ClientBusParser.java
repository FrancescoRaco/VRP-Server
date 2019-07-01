package server;

import test.Bus;

/**
 * Client Bus Parser
 * @author Francesco Raco
 *
 */
public class ClientBusParser
{
	/**
	 * Get Bus object by String query
	 * @param query String representing bus
	 * @return Bus by String query
	 * @throws NoSpecifiedJobsException No Specified Jobs Exception
	 */
	public static Bus parse(String query) throws NoSpecifiedJobsException
	{
		//String array representing bus stops
		String[] stops = query.split("\n");
		
		//If (stops <= 3) then there are not enough specified stops
		int size = stops.length;
		if (size <= 3) throw new NoSpecifiedJobsException();
		
		//Get start and end point
		String startPoint = stops[0];
		String endPoint = stops[size - 1];
		
		//Get target locations
		String[] targetLocations = new String[size - 2];
		
		//Assign every intermediate stop to targetLocations string array
		for (int i = 1; i < size - 1; i++) targetLocations[i - 1] = stops[i];
		
		//Return Bus by client query
		return new Bus(startPoint, endPoint, targetLocations);
	}
}
