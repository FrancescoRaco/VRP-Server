package core;
/*
 * This is a Nominatim API created by author Julien Delange.
 * 
 * I commented the code, indented it and added missing javadoc.
 * I changed visibility of the constructor from public to private,
 * according to Singleton pattern.
 * I added the possibility of choose an own geocoding server and to throw an appropriate exception
 * if coordinates do not exist.
 * I replaced HttpURLConnection with HttpsURLConnection in getRequest(String url) method.
 * 
 * Francesco Raco
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

/**
 * Direct Geocoding using Nominatim
 * @author Julien Delange
 *
 */
public class OpenStreetMapUtils
{
	/**
	 * Logger for configuring which message types are written
	 */
    public final static Logger log = Logger.getLogger("OpenStreeMapUtils");

    /**
     * Private and unique instance of this class according to Singleton pattern
     */
    private static OpenStreetMapUtils instance = null;
    
    private String server = "";
    
    /**
     * Parser for JSON text
     */
    private JSONParser jsonParser;
    
    /**
     * Private constructor by the name of the geocoding server 
     * @param server String address of the server
     */
    private OpenStreetMapUtils(String server)
    {
        jsonParser = new JSONParser();
        this.server = server;
    }

    /**
     * Get the unique instance allowed for OpenStreetMapUtils,
     * created by the String name of the geocoding server
     * @param server String name of the geocoding server
     * @return Unique instance allowed for this class
     */
    public static OpenStreetMapUtils getInstance(String server)
    {
    	//If no instance has been created, then create it and assign it to private field instance;
    	//otherwise no new instance can be created
        if (instance == null)
        {
            instance = new OpenStreetMapUtils(server);
        }
        
        //Return instance (unique instance allowed for this class)
        return instance;
    }
    
    /**
     * Get the unique instance allowed for OpenStreetMapUtils
     * @return unique instance allowed for OpenStreetMapUtils
     */
    public static OpenStreetMapUtils getInstance()
    {
    	return getInstance("http://racomaps.ns0.it/nominatim/");
    }


    /**
     * Get a JSON text String by String which represents URL
     * containing the query for direct geocoding
     * @param url URL containing the query for direct geocoding
     * @return JSON text String
     * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
     */
    private String getRequest(String url) throws NotExistingCoordinatesException
    {
    	//Initialize response string buffer
    	StringBuffer response = new StringBuffer();
    	
    	//Try to execute and handle IOException
    	try
    	{
    		//Create an unchangeable URL by a text String containing the query for direct geocoding
    		final URL obj = new URL(url);
        
    		//Get an URLConnection by invoking the method openConnection() on the variable obj,
    		//then downcast it to HttpsURLConnection
    		final HttpURLConnection con = (HttpURLConnection) obj.openConnection();

    		//Set the method to GET
    		con.setRequestMethod("GET");

    		//If HTTP response message is not 200 (200 = successful Http request),
    		//then throw an appropriate exception
    		if (con.getResponseCode() != 200)
    		{
    			throw new NotExistingCoordinatesException();
    		}

    		//Create a Buffered reader by an Input Streamer Reader, instantiated
    		//with an input stream as argument which reads from this open connection
    		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        
    		//String which is used to store the lines returned by the BufferedReader
    		//and append them to the String Buffer result
    		String inputLine;

    		//Create the String Buffer response and append to it every line
    		//returned by the Buffered Reader;
    		//finally close the Buffered Reader
    		while ((inputLine = in.readLine()) != null)
    		{
    			response.append(inputLine);
    		}
    		
    		//Close resource
    		in.close();
    	}
    	catch(Exception e)
    	{
    		throw new NotExistingCoordinatesException();
    	}
    	
    	//Return the JSON text String related to the String Buffer result
    	return response.toString();
    }

    /**
     * Get (Latitude, Longitude) coordinates by String representing the associated address name
     * @param address String representing the address name
     * @return Map storing 2 keys: {String lat : double value, String lon: double value}
     * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
     */
    public Map<String, Double> getCoordinates(String address) throws NotExistingCoordinatesException
    {
        //Initialize the map which stores the double values of (Latitude, Longitude)
    	//into the corresponding String keys (lat, lon)
    	Map<String, Double> res = new HashMap<String, Double>();
        
    	//Initialize queryResult to null (it will contain the result)
    	//and string buffer where to store the query for direct geocoding
    	String queryResult = null;
    	StringBuffer query = new StringBuffer();
        
    	//Create an array of String words by String argument address
    	//(sequence of characters separated by at least 1 space)
    	String[] split = address.split(" ");
         

        //Add to StringBuffer query the URL containing the Nominatim access point
        //for query sending
        query.append(server + "/search?q=");

        //If the array split does not contain even 1 word,
        //then throw an appropriate message
        if (split.length == 0)
        {
            throw new NotExistingCoordinatesException();
        }

        //For every word in the array split, append it to the StringBuffer query;
        //append to query also a "+" as delimiter between the words,
        //but not after the last word
        for (int i = 0; i < split.length; i++)
        {
            query.append(split[i]);
            if (i < (split.length - 1))
            {
                query.append("+");
            }
        }
        
        //Ending part of the query specifying format and address details
        query.append("&format=json&addressdetails=1");

        //Log a message object with debug
       //log.debug("Query:" + query);

        //Try to obtain an answer by the server, sending it an URL containing the query;
        //then store the result in queryResult local variable 
        queryResult = getRequest(query.toString());
        
        //Parse JSON text into java object from the given string
        @SuppressWarnings("deprecation")
		Object obj = JSONValue.parse(queryResult);
        
        //Log a message object with debug
        //log.debug("obj=" + obj);

        //If obj is an instance of JSONArray,
        //then downcast to JSONArray
        if (obj instanceof JSONArray)
        {
            JSONArray array = (JSONArray) obj;
            
            //If array has at least 1 element
            if (array.size() > 0)
            {
            	//get the first element of array, downcast it to JSONObject
            	//and assign it to jsonObject local variable
            	JSONObject jsonObject = (JSONObject) array.get(0);

                //Get String representation of the value contained in the keys lon ad lat
            	String lon = (String) jsonObject.get("lon");
                String lat = (String) jsonObject.get("lat");
                
                //Log message objects with debug
                //log.debug("lon=" + lon);
                //log.debug("lat=" + lat);
                
                //Put in res map the associations {String lon: Double lon, String lat: Double lat} 
                res.put("lon", Double.parseDouble(lon));
                res.put("lat", Double.parseDouble(lat));
            }
        }
        
        //Throw an appropriate exception if no coordinates found
        if (res.isEmpty()) throw new NotExistingCoordinatesException();
        
        //Return the map res which stores the double values of (Latitude, Longitude)
    	//into the corresponding String keys (lat, lon)
        return res;
    }
}

