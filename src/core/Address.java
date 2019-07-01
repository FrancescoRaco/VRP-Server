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
 * I have commented the code, indented it and added missing Javadoc
 * 
 * Francesco Raco
 */

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Address type used by NominatimReverseGeocodingJAPI
 * @author Daniel Braun
 *
 */
public class Address
{
	//Private fields representing typical attributes of an address
	
	/**
	 * lod (zoom level)
	 */
	private int lod = -1;
	
	/**
	 * osm_id
	 */
	private long osm_id = -1;
	
	/**
	 * osm_type
	 */
	private String osm_type = "";
	
	/**
	 * country_code
	 */
	private String country_code = "";
	
	/**
	 * country
	 */
	private String country = "";
	
	/**
	 * postecode
	 */
	private String postcode = "";
	
	/**
	 * state
	 */
	private String state = "";
	
	/**
	 * county
	 */
	private String county = "";
	
	/**
	 * city
	 */
	private String city = "";
	
	/**
	 * suburb
	 */
	private String suburb = "";
	
	/**
	 * road
	 */
	private String road = "";
	
	/**
	 * display_name
	 */
	private String display_name = "";
	
	/**
	 * Create address by a JSON text String and a zoom level integer
	 * @param json JSON Text String
	 * @param lod Zoom level integer
	 */
	public Address(String json, int lod)
	{
		try
		{			
			//Create JSONObject by JSON text String
			JSONObject jObject = new JSONObject(json);	
			
			//If JSONObject contains the "error" key,
			//then print the value of the error and return null 
			if(jObject.has("error"))
			{
				System.err.println(jObject.get("error"));
				return;
		    }
			
			//Get the values associated with the following keys
			//and assign them to the corresponding private field of this Address instance
			osm_id = jObject.getLong("osm_id");
			osm_type = jObject.getString("osm_type");
			display_name = jObject.getString("display_name");	
			
			//Get the JSONObject associated with the key "address"
			JSONObject addressObject = jObject.getJSONObject("address");			
			
			//Verify if addressObject contains the following keys:
			//if yes, then assign the associated value to the corresponding private field
			//of this Address instance
			if(addressObject.has("country_code"))
			{
				country_code = addressObject.getString("country_code");
			}			
			if(addressObject.has("country"))
			{
				country = addressObject.getString("country");
			}			
			if(addressObject.has("postcode"))
			{
				postcode = addressObject.getString("postcode");
			}			
			if(addressObject.has("state")){
				state = addressObject.getString("state");
			}			
			if(addressObject.has("county"))
			{
				county = addressObject.getString("county");
			}			
			if(addressObject.has("city"))
			{
				city = addressObject.getString("city");
			}			
			if(addressObject.has("suburb"))
			{
				suburb = addressObject.getString("suburb");
			}			
			if(addressObject.has("road"))
			{
				road = addressObject.getString("road");
			}
			
			//Assign the value of the argument lod to the corresponding private field lod
			this.lod = lod;
		}
		catch (JSONException e)
		{
			//If a JSONException occurs, then print the following text String
			//and the Stack Trace related to the JSONException
			System.err.println("Can't parse JSON string");
			e.printStackTrace();
		}
	}
	
	/**
	 * Get osm_id value
	 * @return Long osm_id
	 */
	public long getOsmId()
	{
		return osm_id;
	}
	
	/**
	 * Get osm_type value
	 * @return String osm_type
	 */
	public String getOsmType()
	{
		return osm_type;
	}
	
	/**
	 * Get lod value (zoom level integer)
	 * @return Zoom level integer
	 */
	public int getLod()
	{
		return lod;
	}
	
	/**
	 * Get country_code value
	 * @return String country_code
	 */
	public String getCountryCode()
	{
		return country_code;
	}
	
	/**
	 * Get country value
	 * @return String country
	 */
	public String getCountry()
	{
		return country;
	}
	
	/**
	 * Get postecode value
	 * @return String postecode
	 */
	public String getPostcode()
	{
		return postcode;
	}
	
	/**
	 * Get state value
	 * @return String state
	 */
	public String getState()
	{
		return state;
	}
	
	/**
	 * Get county value
	 * @return String county
	 */
	public String getCounty()
	{
		return county;
	}
	
	/**
	 * Get city value
	 * @return String city
	 */
	public String getCity()
	{
		return city;
	}
	
	/**
	 * Get suburb value
	 * @return String suburb
	 */
	public String getSuburb()
	{
		return suburb;
	}
	
	/**
	 * Get road value
	 * @return String road
	 */
	public String getRoad()
	{
		return road;
	}
	
	/**
	 * Get display_name value
	 * @return String display_name
	 */
	public String getDisplayName()
	{
		return display_name;
	}
	
	/**
	 * String view of the Address instance based on the private String field display_name
	 */
	public String toString()
	{ 
        return display_name; 
    } 

}
