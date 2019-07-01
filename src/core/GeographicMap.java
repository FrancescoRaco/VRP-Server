package core;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.log4j.Logger;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl.Builder;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.Translation;
import com.graphhopper.util.TranslationMap;

/**
 * Geographic Map
 * @author Francesco Raco
 * 
 */
public class GeographicMap
{
	/**
	 * Logger for configuring which message types are written
	 */
    public static final Logger log = Logger.getLogger("GeographicMap");
	
    /**
	 * Path where to store GraphHopper graphs
	 */
    public static final String GRAPHSPATH = ClassLoader.getSystemResource("graphs").getPath();
    
    /**
	 * Path where to store Geographic maps
	 */
    public static final String MAPSPATH = ClassLoader.getSystemResource("maps/").getPath();
    
    /**
	 * Geocoding server
	 */
    protected String geocodingServer = "http://racomaps.ns0.it/nominatim/";
    
    /**
	 * File containing geographic map (OpenStreetMap format)
	 */
	protected String osmFile;
	
	/**
	 * National area considered
	 */
	protected Locale area;
	
	/**
	 * Vehicle selected
	 */
	protected String vehicle;
	
	/**
	 * Instance of GraphHopper: access point to OSM Map implementation and best path (among 2 points) algorithms
	 */
	protected GraphHopper hopper = new GraphHopperOSM().forServer();
	
	/**
	 * Direct Geocoding: map String address to Double values (Latitude, Longitude)
	 * @param address The String related to the name of geographic point
	 * @return Map storing Double values representing Latitude and Longitude into relative String keys "lat" and "lon" 
	 * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
	 */
	protected Map<String, Double> getDirectGeocoding(String address) throws NotExistingCoordinatesException
	{
		return OpenStreetMapUtils.getInstance(geocodingServer).getCoordinates(address);
	}
	
	/**
	 * Reverse Geocoding: map Double Values (Latitude, Longitude) to String address
	 * @param lat Latitude
	 * @param lon Longitude
	 * @return String related to the name of geographic point
	 * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
	 */
	protected String getReverseGeocoding(double lat, double lon) throws NotExistingCoordinatesException
	{
		return NominatimReverseGeocodingJAPI.getInstance(geocodingServer).getAdress(lat, lon).getDisplayName();
	}
	
	/**
	 * Create matrix containing costs of distance and time returned by invoking getBestPath on every couple of locations
	 * @param btp Enumeration representing the 2 possible criteria for best path calculation: Fastest or Shortest
	 * @param locations All locations included in the path
	 * @param cmw Cost Matrix Wrapper containing a string representation of the best path between every couple of points
	 * @return Matrix containing costs of distance and time between every couple of locations
	 * @throws PathNotFoundException Path Not Found Exception
	 */
	protected VehicleRoutingTransportCosts createCostMatrix(BestPathChoice btp, List<Location> locations, CostMatrixWrapper cmw) throws PathNotFoundException
	{
		//Create builder of asymmetric transport costs matrix
		VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
		
		//Iterate on all couple of locations (matrix is asymmetric)
		for (int i = 0; i < locations.size(); i++)
			for (int j = 0; j < locations.size(); j++)
			{
				//Jump to next iteration if locations are equal
				if (locations.get(i).equals(locations.get(j))) continue;
				
				//Get start end end locations
				Location from = locations.get(i);
				Location to = locations.get(j);
				
				//Get coordinates (Latitude, Longitude) of start and end location
				double latFrom = locations.get(i).getCoordinate().getX();
				double lonFrom = locations.get(i).getCoordinate().getY();
				double latTo = locations.get(j).getCoordinate().getX();
				double lonTo = locations.get(j).getCoordinate().getY();
				
				//Get the best path by start and end locations coordinates
				PathWrapper path = getBestPath(btp, latFrom, lonFrom, latTo, lonTo);
		
				//Get distance and time associated with the best path
				double distance = path.getDistance();
				long time = path.getTime();
				
				//Get id of from and to locations and assign them to their corresponding variable
				String fromId = from.getId();
				String toId = to.getId();
				
				//Add to the matrix builder the costs of distances and times
				costMatrixBuilder.addTransportDistance(fromId, toId, distance);
				costMatrixBuilder.addTransportTime(from.getId(), to.getId(), time);
				
				//If cost matrix wrapper is not null, then add to it
				//the instructions related to the best path between from and to locations
				if (cmw != null) cmw.addPathInstructions(fromId, toId, bestPathToString(path));
				
				//Log a message object with debug
		        log.debug("[" + from.getId() + " - " + to.getId() + "] : " + Math.round(distance / 1000) + "km; " + (time / 1000) / 60 + "mm" + " and " + time % 60 + "s");
			}
		
		//Build the costs matrix and return it
		return costMatrixBuilder.build();
	}
	
	/**
	 * Create matrix containing costs of distance and time between every couple of locations; it uses "Fastest" criterion as default choice
	 * @param locations All locations to be reached by the vehicle
	 * @param cmw Cost Matrix Wrapper containing a string representation of the best path between every couple of points
	 * @return Matrix containing costs of distance and time between every couple of locations
	 * @throws PathNotFoundException Path Not Found Exception
	 */
	protected VehicleRoutingTransportCosts createCostMatrix(List<Location> locations, CostMatrixWrapper cmw) throws PathNotFoundException
	{
		return createCostMatrix(BestPathChoice.FASTEST, locations, cmw);
	}
	
	/**
	 * Parse Allowed Vehicle Type to String value representing the vehicle type
	 * @param vehicle Allowed Vehicle Type
	 */
	protected void parseVehicle(AllowedVehicleTypes vehicle)
	{
		switch(vehicle)
		{
			case CAR: this.vehicle = "car"; break;
			case CAR4WD: this.vehicle = "car4wd"; break;
			case BIKE: this.vehicle = "bike"; break;
			case BIKE2: this.vehicle = "bike2"; break;
			case RACINGBIKE: this.vehicle = "racingbike"; break;
			case MOUNTAINBIKE: this.vehicle = "mtb"; break;
			case FOOT: this.vehicle = "foot"; break;
			case HIKE: this.vehicle = "hike"; break;
			case MOTORCYCLE: this.vehicle = "motorcycle"; break;
			case BUS: this.vehicle = "bus"; break;
			
			//case GENERIC:
			default: this.vehicle = "generic"; break;
		}
	}
	
	/**
	 * Create GeographicMap instance by OpenStreetMap file, national area and allowed vehicle type
	 * @param osmFile OpenStreetMap file
	 * @param area National area
	 * @param vehicle Allowed vehicle type
	 */
	public GeographicMap(String osmFile, Locale area, AllowedVehicleTypes vehicle)
	{
		//Assign arguments to corresponding fields
		this.osmFile = MAPSPATH + osmFile;
		this.area = area;
		
		hopper.setDataReaderFile(osmFile);
		
		//physical path where to store GraphHopper files
		hopper.setGraphHopperLocation(GRAPHSPATH);
		
		//Assign correct vehicle type string to this.vehicle field
		parseVehicle(vehicle);
		
		//Specify which vehicle types can be read by this GraphHopper instance;
		//BusFlagEncoderFactory is a modified version of DefaultFlagEncoderFactory,
		//which allows to accept bus vehicle too (without restrictions related to specific lanes);
		//the last argument 4 refers to bytes for edge flags
		hopper.setEncodingManager(EncodingManager.create(new BusFlagEncoderFactory(), this.vehicle, 4));
		hopper.importOrLoad();
	}
	
	/**
	 * Create GeographicMap instance by OpenStreetMap file, national area and path + directory where to store GraphHopper files.
	 * Default choice for vehicle type is BUS
	 * @param osmFile OpenStreetMap file
	 * @param area National area
	 */
	public GeographicMap(String osmFile, Locale area)
	{
		this(osmFile, area, AllowedVehicleTypes.BUS);
	}
	
	/**
	 * Get Geocoding server
	 * @return Geocoding server
	 */
	public String getGeocodingServer()
	{
		return geocodingServer;
	}
	
	/**
	 * Get OSM file
	 * @return OSM file
	 */
	public String getOsmFile()
	{
		return osmFile;
	}
	
	/**
	 * Get national area
	 * @return National area
	 */
	public Locale getArea()
	{
		return area;
	}
	
	/**
	 * Set geocoding server
	 * @param geocodingServer Geocoding server
	 */
	public void setGeocodingServer(String geocodingServer)
	{
		this.geocodingServer = geocodingServer;
	}
	
	/**
	 * Calculate best path between 2 locations; 2 possible criteria for best path calculation: Fastest or Shortest
	 * @param btp Enumeration representing the 2 possible criteria for best path calculation: Fastest or Shortest
	 * @param from Start location
	 * @param to End Location
	 * @return Best Path 
	 * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
	 * @throws PathNotFoundException Path Not Found Exception
	 */
	public PathWrapper getBestPath(BestPathChoice btp, String from, String to) throws NotExistingCoordinatesException, PathNotFoundException
	{
		//Map the address of start and end points into (Latitude, Longitude) cooordinates
		Map<String, Double> fromCoords = getDirectGeocoding(from);
		Map<String, Double> toCoords = getDirectGeocoding(to);
		
		//Return the best path specifying the start and end coordinates
		return getBestPath(btp, fromCoords.get("lat"), fromCoords.get("lon"), toCoords.get("lat"), toCoords.get("lon"));
	}
	
	/**
	 * Calculate best path between 2 locations; 2 possible criteria for best path calculation: Fastest or Shortest
	 * @param btp Enumeration representing the 2 possible criteria for best path calculation: Fastest or Shortest
	 * @param fromLat Start location latitude
	 * @param fromLon Start location longitude
	 * @param toLat End location latitude
	 * @param toLon End location longitude
	 * @return Best Path
	 * @throws PathNotFoundException Path Not Found Exception
	 */
	public PathWrapper getBestPath(BestPathChoice btp, double fromLat, double fromLon, double toLat, double toLon) throws PathNotFoundException
	{
		//Create a GraphHopper best path request by the coordinates of start and end locations
		GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon);
				
		//specify criterion for best path calculation in GraphHopper request
		switch(btp)
		{
			case FASTEST: req.setWeighting("fastest"); break;
			case SHORTEST: req.setWeighting("shortest"); break;
		}
			    
		//Set vehicle and national area in GraphHopper request
		req.setVehicle(vehicle);
		req.setLocale(area);
				
		//Get paths returned by GraphHopper and throw exception if no path is returned
		GHResponse rsp = hopper.route(req);
		if(rsp.hasErrors()) throw new PathNotFoundException();
				
		//Return best path among those returned by GraphHopper
		return rsp.getBest();
	}
	

	/**
	 * Calculate best path between 2 locations with default "Fastest" criterion
	 * @param from Start location
	 * @param to End location
	 * @return Best path 
	 * @throws PathNotFoundException Path Not Found Exception
	 * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
	 */
	public PathWrapper getBestPath(String from, String to) throws NotExistingCoordinatesException, PathNotFoundException
	{
		return getBestPath(BestPathChoice.FASTEST, from, to);
	}
	
	/**
	 * Calculate best path between 2 specified locations and invoke bestPasthToString(path) method
	 * @param btp Best Path Choice
	 * @param from Start point
	 * @param to End point
	 * @return String representation of the best path between the 2 specified locations
	 * @throws PathNotFoundException Path Not Found Exception
	 * @throws NotExistingCoordinatesException Not Existing Coordinates Exception 
	 */
	public String showBestPath(BestPathChoice btp, String from, String to) throws NotExistingCoordinatesException, PathNotFoundException
	{
		PathWrapper path = getBestPath(btp, from, to);
		return bestPathToString(path);
	}
	
	/**
	 * Get a String representation of the best path between 2 locations
	 * @param path Path of which a String representation is needed
	 * @return String representation of the chosen path 
	 * @throws PathNotFoundException Path Not Found
	 */
	public String bestPathToString(PathWrapper path) throws PathNotFoundException
	{
		
		//Get all instructions related to the best path
		InstructionList il = path.getInstructions();
        
		//Manage the translations in-memory and load the translation files from classpath
		TranslationMap tm = new TranslationMap().doImport();
        
		//Return the Translation object for the specified locale and fall back to english if the locale was not found
		Translation tr = tm.getWithFallBack(area);
        
        //Initialize StringBuilder representation of the best path
		StringBuilder infoBuilder = new StringBuilder().append("Tempo stimato: ");
		infoBuilder.append((path.getTime() / 1000) / 60).append("min").append(" e ").append(path.getTime() % 60).append("s \n");
		infoBuilder.append("Distanza stimata: ").append(Math.round(path.getDistance() / 1000)).append("km").append(" e ").append(Math.round(path.getDistance() % 1000)).append("m \n\n");
        
        //If there is at least 1 instruction, iterate over the list of instructions related to the best path
        if (path.getInstructions() != null)
        {
            for(Instruction instruction : il)
            	{
            		//Append to StringBuilder infoBuilder the description of specific instruction
            		//+ time in seconds and distance in meters until no new instruction
            		infoBuilder.append(instruction.getTurnDescription(tr)).append(" per ").append(Math.round(instruction.getTime() / 1000)).append("s");
            		infoBuilder.append(" e ").append(Math.round(instruction.getDistance())).append("m\n");
            	}
        }
        
        //Return normalized String related to StringBuilder infoBuilder,
        //containing a representation of the best path (removing eventual spaces at start and end)
        return Normalizer.normalize(infoBuilder.toString().trim(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "'");
	}
	
	/**
	 * Solve Travelling Salesman Problem
	 * @param cmw Cost Matrix Wrapper containing a string representation of the best path between every couple of points
	 * @param startPoint Start Location of the vehicle
	 * @param endPoint End Location of the vehicle
	 * @param intermediateLocations Intermediate locations to be reached by the vehicle
	 * @return Solution path with lowest estimated cost (default is distance related to fastest path)
	 * @throws UncorrectQueryException Uncorrect Query Exception 
	 * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
	 * @throws PathNotFoundException Path Not Found Exception
	 */
	public VehicleRoutingProblemSolution solveTsp(CostMatrixWrapper cmw, String startPoint, String endPoint, List<String> intermediateLocations) throws UncorrectQueryException, NotExistingCoordinatesException, PathNotFoundException
	{
		if (intermediateLocations.isEmpty() || intermediateLocations == null || startPoint == null || endPoint == null) throw new UncorrectQueryException();
		
		//Define type and capacity of the vehicle
		VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(0,Integer.MAX_VALUE);
		VehicleType vehicleType = vehicleTypeBuilder.build();
		Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
		
		//Map the address of startPoint into (Latitude, Longitude) coordinates
		Map<String, Double> startCoords = getDirectGeocoding(startPoint);
		
		//Create start location by name and coordinates
		Location startLocation = Location.Builder.newInstance().setId(startPoint).setCoordinate(new Coordinate(startCoords.get("lat"), startCoords.get("lon"))).build();
		
		//Map the address of endPoint into (Latitude, Longitude) coordinates
		Map<String, Double> endCoords = getDirectGeocoding(endPoint);
				
		//Create end location by name and coordinates
		Location endLocation = Location.Builder.newInstance().setId(endPoint).setCoordinate(new Coordinate(endCoords.get("lat"), endCoords.get("lon"))).build();
		
		//Assign (Latitude, Longitude) coordinates of startPoint to the vehicle as its start location
		vehicleBuilder.setStartLocation(startLocation);
		
		//Assign (Latitude, Longitude) coordinates of endPoint to the vehicle as its end location
		vehicleBuilder.setEndLocation(endLocation);
		
		//Assign type to vehicle and build it
		vehicleBuilder.setType(vehicleType); 
		Vehicle vehicle = vehicleBuilder.build();
		
		//initialize jobs and location lists
		List<Service> jobs = new ArrayList<Service>();
		List<Location> locations = new ArrayList<Location>();
		
		//Add the end location to the jobs list as a new Service
		jobs.add(Service.Builder.newInstance(endPoint).setLocation(endLocation).build());
		
		//Add start and end locations to the locations list
		locations.add(startLocation);
		locations.add(endLocation);
		
		//Assign 1 job per intermediateLocations to the vehicle
		for (String id : intermediateLocations)
		{
			//Get the coordinates of the locations and assign them to corresponding double variable
			Map<String, Double> coords = getDirectGeocoding(id);
			double lat = coords.get("lat");
			double lon = coords.get("lon");
			
			//Create location l by id and (Latitude, Longitude) coordinates and add it to locations list
			Location l = Location.Builder.newInstance().setId(id).setCoordinate(new Coordinate(lat, lon)).build();
			locations.add(l);
			
			//Add location l to the jobs list as a new Service
			jobs.add(Service.Builder.newInstance(id).setLocation(l).build());
		}
		
		//Create matrix containing costs of distance and time between every couple of locations
		//included in the path
		VehicleRoutingTransportCosts costMatrix = createCostMatrix(locations, cmw);
		
		//Create builder of VRP specifying matrix of costs (distance and time) and vehicle
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance().setFleetSize(FleetSize.INFINITE).setRoutingCost(costMatrix).addVehicle(vehicle);
		
		//Assign every given jobs to the VRP builder
		for (Service job : jobs) vrpBuilder.addJob(job);
		
		//Build VRP and create Jsprit algorithm to solve the given VRP
		VehicleRoutingProblem vrp = vrpBuilder.build();
		VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
		
		//Set max iterations of the Jsprit algorithm
		vra.setMaxIterations(2000);
		
		//Calculate and return the path with lowest distance cost (related to the fastest path)
		//among the solutions returned by Jsprit algorithm
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		return Solutions.bestOf(solutions);
	}
	
	/**
	 * Solve Travelling Salesman Problem without specifying cost matrix wrapper object
	 * @param startPoint Start Location of the vehicle
	 * @param endPoint End Location of the vehicle
	 * @param intermediateLocations Intermediate locations to be reached by the vehicle
	 * @return Solution path with lowest estimated cost (default is distance related to fastest path)
	 * @throws PathNotFoundException Path Not Found Exception 
	 * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
	 * @throws UncorrectQueryException Uncorrect Query Exception
	 */
	public VehicleRoutingProblemSolution solveTsp(String startPoint, String endPoint, List<String> intermediateLocations) throws UncorrectQueryException, NotExistingCoordinatesException, PathNotFoundException
	{
		return solveTsp(null, startPoint, endPoint, intermediateLocations);
	}
	
	/**
	 * Get the list of Tour Activities associated to the Jsprit solution of the specified TSP
	 * @param startPoint Start point
	 * @param solution Jsprit solution of the specified TSP
	 * @return List of Tour Activities associated to the Jsprit solution of the specified TSP
	 * @throws NoInstructionsFoundException No Instructions Found Exception
	 */
	public List<TourActivity> getTspSolutionActivities(String startPoint, VehicleRoutingProblemSolution solution) throws NoInstructionsFoundException
	{
		//Return the list of Tour Activities associated with this TSP solution
		return new TSPSolutionWrapper(solution, startPoint).getSolutionActivities();
	}
	
	/**
	 * Solve Travelling Salesman Problem and store the solution into a String object
	 * @param startPoint Start Location of the vehicle
	 * @param endPoint End Location of the vehicle
	 * @param intermediateLocations Intermediate locations to be reached by the vehicle
	 * @return String representation of the best TSP solution with lowest estimated cost
	 * @throws PathNotFoundException Path Not Found Exception 
	 * @throws NotExistingCoordinatesException Not Existing Coordinates Exception
	 * @throws UncorrectQueryException Uncorrect Query Exception
	 * @throws NoInstructionsFoundException No Instructions Found Exception
	 */
	public String showTspSolution(String startPoint, String endPoint, List<String> intermediateLocations) throws UncorrectQueryException, NotExistingCoordinatesException, PathNotFoundException, NoInstructionsFoundException
	{
		//Cost Matrix Wrapper containing a string representation of the best path between every couple of points
		CostMatrixWrapper cmw = new CostMatrixWrapper();
		
		//Solve Travelling Salesman Problem and store the best solution with lowest distance estimated cost
		//(related to the fastest path) 
		VehicleRoutingProblemSolution solution = solveTsp(cmw, startPoint, endPoint, intermediateLocations);
		
		//String Builder representation of the best TSP solution with lowest estimated cost
		//(related to the fastest path) 
		StringBuilder tspBuilder = new StringBuilder();
		tspBuilder.append("Distanza percorsa: ").append(Math.round(solution.getCost() / 1000)).append("km\n\n");
		
		tspBuilder = new TSPSolutionWrapper(solution, startPoint, cmw, tspBuilder).getSBSolution();
		  
		//Return String related to tspBuilder StringBuilder,
		//containing a representation of the best path returned by the Jsprit algorithm
		return tspBuilder.toString();
	}
}