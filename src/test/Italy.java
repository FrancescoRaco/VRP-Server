package test;

import java.util.Locale;
import core.GeographicMap;

/**
 * Italy (geographic map)
 * @author Francesco Raco
 */
public class Italy extends GeographicMap
{
	public Italy()
	{
		super("italy.osm.pbf", Locale.ITALY);
	}
}

