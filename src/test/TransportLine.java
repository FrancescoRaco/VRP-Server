package test;

import java.util.List;

/**
 * Transport Line interface
 * @author Francesco Raco
 *
 */
public interface TransportLine
{
	/**
	 * Get start point
	 * @return Start point
	 */
	String getStartPoint();
	
	/**
	 * Get end point
	 * @return End point
	 */
	String getEndPoint();

	/**
	 * Get provider ordered total stops
	 * @return Provider ordered total stops
	 */
	List<String> getProviderOrderedTotalStops();
}