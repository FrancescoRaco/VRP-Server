package core;
/*
 * (C) Copyright 2018 Daniel Braun (http://www.daniel-braun.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/*
 * I commented the code, indented it and added missing javadoc.
 * I created a static method for getting the unique allowed instance of this class,
 * changing the visibility of the constructor from public to private
 * (according to Singleton pattern).
 * I removed the constructor with zoomLevel,
 * but it is still possible setting it with setZoomLevel(int zoomLevel) method
 * (I preserved functionality of main method which used original code for zoom level)
 * I added the possibility of choose an own reverse geocoding server and to throw an appropriate exception
 * if coordinates do not exist.
 * I replaced HttpsURLConnection with HttpURLConnection in getJSON(String urlString) method.
 * Currently I don't use it in my project because I prefer don't change the id given by users of this application,
 * but my GeographicMap class contains a protected method
 * which implements this functionality: subclasses can see it and it is useful for future development
 * purposes.
 * 
 * Francesco Raco
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Java library for reverse geocoding using Nominatim.
 * 
 * @author Daniel Braun
 * @version 0.2
 *
 */
public class NominatimReverseGeocodingJAPI
{
	/**
	 * Unique allowed instance of this class
	 */
	private static NominatimReverseGeocodingJAPI instance = null;
	
	/**
	 * String representing NominatimInstance
	 */
	private String nominatimInstance = ""; 

	/**
	 * Zoom level integer
	 */
	private int zoomLevel = 18;
	
	/**
	 * Create instance of NominatimReverseGeocodingJAPI without specifying the zoom level
	 */
	private NominatimReverseGeocodingJAPI(String nominatimInstance)
	{
		this.nominatimInstance = nominatimInstance;
	}
	
	/**
	 * Get the JSON text String by urlString
	 * @param urlString String text which defines the url
	 * @return The JSON text String by urlString
	 * @throws NotExistingCoordinatesException 
	 */
	private String getJSON(String urlString) throws NotExistingCoordinatesException
	{
		//Initialize the String Builder result
		StringBuilder result = new StringBuilder();
		
		//Try to execute and handle eventual IOException
		try
		{
			//Create an URL by a text String containing the query for reverse geocoding
			URL url = new URL(urlString);
			
			//Get an URLConnection by invoking the method openConnection() on the variable url,
			//then downcast it to HttpsURLConnection
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			//Set the method to GET and add request property
			conn.setRequestMethod("GET");
			conn.addRequestProperty("User-Agent", "Mozilla/4.76"); 

			//Create a Buffered reader by an Input Streamer Reader, instantiated
			//with an input stream as argument which reads from this open connection
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			//String which is used to store the lines returned by the BufferedReader
			//and append them to the String Builder result
			String text;
			
			 //Append to result every line
			//returned by the Buffered Reader;
			//finally close the Buffered Reader
			while ((text = in.readLine()) != null) result.append(text);
			in.close();
		}
		catch(IOException e)
		{
			throw new NotExistingCoordinatesException();
		}
		
		//Assign to resultString the JSON text String related to the String Builder result
		String resultString = result.toString();
		
		//If resultString is empty, then throw an appropriate exception
		if (resultString.isEmpty()) throw new NotExistingCoordinatesException();
		
		//Return resultString
		return resultString;
	}
	
	/*This method is useful for testing purpose but my project does not use it.
	*I use an other class containing the main method,
	*which is applied to my entire project.
	*
	*
	* Francesco Raco
	*/
	public static void main(String[] args) throws NotExistingCoordinatesException
	{
		if(args.length < 1)
		{
			System.out.println("use -help for instructions");
		}
		else if(args.length < 2)
		{
			if(args[0].equals("-help"))
			{
				System.out.println("Mandatory parameters:");
				System.out.println("   -lat [latitude]");
				System.out.println("   -lon [longitude]");
				System.out.println ("\nOptional parameters:");
				System.out.println("   -zoom [0-18] | from 0 (country) to 18 (street address), default 18");
				System.out.println("   -osmid       | show also osm id and osm type of the address");
				System.out.println("\nThis page:");
				System.out.println("   -help");
			}
			else
				System.err.println("invalid parameters, use -help for instructions");
		}
		else
		{
			boolean latSet = false;
			boolean lonSet = false;
			boolean osm = false;
			
			double lat = -200;
			double lon = -200;
			int zoom = 18;
			
			for (int i = 0; i < args.length; i++)
			{
				if(args[i].equals("-lat"))
				{					
					try
					{  
					    lat = Double.parseDouble(args[i+1]);  
					}  
					catch(NumberFormatException nfe)
					{  
					    System.out.println("Invalid latitude");
					    return;
					}  
					
					latSet = true;
					i++;
					continue;
				}		
				else if(args[i].equals("-lon"))
				{
					try
					{  
					    lon = Double.parseDouble(args[i+1]);  
					}  
					catch(NumberFormatException nfe)
					{  
					    System.out.println("Invalid longitude");
					    return;
					} 
					
					lonSet = true;
					i++;
					continue;
				}
				else if(args[i].equals("-zoom"))
				{
					try
					{  
					    zoom = Integer.parseInt(args[i+1]);  
					}  
					catch(NumberFormatException nfe)
					{  
					    System.out.println("Invalid zoom");
					    return;
					} 
					
					i++;
					continue;
				}
				else if(args[i].equals("-osm"))
				{
					osm = true;
				}
				else
				{
					System.err.println("invalid parameters, use -help for instructions");
					return;
				}
			}
			
			if(latSet && lonSet)
			{
				//Preserved functionality of setting zoom level for this method
				NominatimReverseGeocodingJAPI nominatim = getInstance();
				nominatim.setZoomLevel(zoom);
				
				Address address = nominatim.getAdress(lat, lon);
				System.out.println(address);
				if(osm)
				{
					System.out.print("OSM type: " + address.getOsmType()+", OSM id: " + address.getOsmId());
				}
			}
			else
			{
				System.err.println("please specifiy -lat and -lon, use -help for instructions");
			}			
		}				
	}
	
	/**
	 * Get the unique instance allowed for this class,
	 * created by String name of the reverse geocoding server
	 * @param server String name of the geocoding server
	 * @return the unique instance allowed for this class
	 */
	public static NominatimReverseGeocodingJAPI getInstance(String server)
	{
		if (instance == null) instance = new NominatimReverseGeocodingJAPI(server);
		return instance;
	}
	
	/**
	 * Get the unique instance allowed for this class,
	 * with default String name of the reverse geocoding server
	 * @return The unique instance allowed for this class
	 */
	public static NominatimReverseGeocodingJAPI getInstance()
	{
		return getInstance("http://racomaps.ns0.it/nominatim/");
	}
	
	/**
	 *  Set the String name of the reverse geocoding server
	 *  @param server String name of the reverse geocoding server
	 */
	public void setServer(String server)
	{
		this.nominatimInstance = server;
	}
	
	/**
	 * Set the zoom level
	 * @param zoomLevel Zoom level integer
	 */
	public void setZoomLevel(int zoomLevel)
	{
		//If zoom  level integer is lower than 0 OR higher than 18,
		//than print a text String error and set the zoom level integer to 18
		if(zoomLevel < 0 || zoomLevel > 18)
		{
			System.err.println("invalid zoom level, using default value");
			zoomLevel = 18;
		}
		
		//Assign argument zoomLevel to corresponding private field zoomLevel
		this.zoomLevel = zoomLevel;
	}
	
	/**
	 * Get Address by its (Latitude, Longitude) coordinates
	 * @param lat Latitude
	 * @param lon Longitude
	 * @return Address
	 * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
	 */
	public Address getAdress(double lat, double lon) throws NotExistingCoordinatesException
	{	
		//Create the String which represents the URL containing the query for reverse geocoding
		String urlString = nominatimInstance + "/reverse?format=json&addressdetails=1&lat=" + String.valueOf(lat) + "&lon=" + String.valueOf(lon) + "&zoom=" + zoomLevel ;
		
		//Send to the server an URL containing the query
		//and the zoom level integer,
		//then return the server answer
		return new Address(getJSON(urlString), zoomLevel);
	}
}