package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import core.GeographicMap;
import core.NoInstructionsFoundException;
import core.NotExistingCoordinatesException;
import core.PathNotFoundException;
import core.UncorrectQueryException;
import test.Bus;
import test.Italy;
import test.NoStopsFoundException;
import test.Test;

/**
 * Server
 * @author Francesco Raco
 */
public class Server extends Thread
{
	
	/**
	 * Logger for configuring which message types are written
	 */
    public final static Logger log = Logger.getLogger("Server");
    
	/**
	 * Port number
	 */
    public static final int PORT_NUMBER = 8080;

	/**
	 * Socket
	 */
    protected Socket socket;
	
	/**
	 * Geographic Map
	 */
    protected GeographicMap map;
	
	/**
	 * Map containing the status of client choices for every output type provided by the server
	 */
    protected Map<String, Boolean> vrpTypesMap = new TreeMap<String, Boolean>();
	
	/**
	 * Best path points
	 */
    protected String[] bestPathPoints;

	/**
	 * Constructor with socket and Geographic Map
	 * @param socket Socket
	 * @param map Geographic Map
	 */
    protected Server(Socket socket, GeographicMap map)
	{
		//Initialize fields
    	        this.socket = socket;
		this.map = map;
		vrpTypesMap.put("ShowSolution", false);
		vrpTypesMap.put("ShowTestSolution", false);
		
		//Begin execution calling run() method
		start();
	}
	
	/**
	 * Constructor with socket (Italy is the default Geographic Map)
	 * @param socket
	 */
    protected Server(Socket socket)
	{
		this(socket, new Italy());
	}

	/**
	 * Begin execution
	 */
    public void run()
	{
		//Initialize input and output streams + buffered reader (for input streams reading)
    	        InputStream in = null;
		PrintWriter out = null;
		BufferedReader br = null;
		
		try
		{
			//Get input and output stream (PrintWriter is needed for writing into output stream)
			in = socket.getInputStream();
			out = new PrintWriter(socket.getOutputStream(), true);
			
			//Read input stream
			br = new BufferedReader(new InputStreamReader(in));
			
			//Log a message object with debug
	                log.debug("Connection established");
			
			//Initialize query String Builder
	                StringBuilder query = new StringBuilder();
			
			//Read client data until receiving "END" string
	                String stop;
			while ((stop = br.readLine()) != null && !stop.equals("END"))
			{
				//If client asks for a specific output value regarding bus vrp, update
				//the boolean value in corresponding vrpTypesMap key
				boolean isType = false;
				for (String outputType: vrpTypesMap.keySet())
				{
					if (stop.equals(outputType))
					{
						//If this outputType has not requested yet from the server,
						//then set as true its corresponding boolean value
						if (!vrpTypesMap.get(outputType)) vrpTypesMap.put(outputType, true);
						isType = true;
					}
				}
				
				//if client query starts with following text --> bestPathPoints array will contain
				//the start and end point for best path calculation
				if (stop.startsWith("ShowSingleSourceBestPath")) bestPathPoints = stop.split(" ");
				
				//else if client query is not a desired output type --> it is a stop to be
				//appended to query string builder
				else if (!isType) query.append(stop).append("\n");
			}
			
			//Initialize solution to null
			String solution = null;
			
			//If client asked for Single Source Best Path, then get GraphHopper algorithm solution
			//represented by a string text
			if (bestPathPoints != null) solution = Test.getGraphHopperAlgorithmSolutionInfo(map, bestPathPoints[1], bestPathPoints[2]);
			
			//Else if client asked for a specific output type provided by the server,
			//then get appropriate solution ("ShowSolution" or "ShowTestSolution")
			else if (!vrpTypesMap.isEmpty())
			{
				//Get Bus object by client query
				Bus bus = ClientBusParser.parse(query.toString());
			
				//get appropriate solution ("ShowSolution" or "ShowTestSolution")
				if (vrpTypesMap.get("ShowSolution")) solution = Test.getJspritAlgorithmSolutionInfo(map, bus);
				else if (vrpTypesMap.get("ShowTestSolution")) solution = Test.getJspritAlgorithmTestingInfo(map, bus);
	                }
			
			//If client did not ask for a specific output provided by the server,
			//then tell him an appropriate message
			else
			{
				out.println("Non hai specificato una tipologia di richiesta valida!");
			}
	        
	                //If solution is non null, then send it to the client
			if (solution != null) out.println(solution);
		}
		
		//Handle Exception and close I/O stream and socket
		catch (IOException ex)
		{
			out.println("Non sono riuscito a processare la tua richiesta!");
		}
		catch (NoSpecifiedJobsException e)
		{
			out.println("Specificare almeno 3 fermate!");
		}
		catch (NoStopsFoundException e)
		{
			out.println("Non ho trovato fermate!");
		}
		catch (UncorrectQueryException e)
		{
			out.println("Richiesta non formulata correttamente!");
		}
		catch(NotExistingCoordinatesException e)
		{
			out.println("Attenzione: 1 o piu' fermate richieste non sono presenti nel database!");
		}
		catch (PathNotFoundException e)
		{
			out.println("Non ho trovato alcun percorso!");
		}
		catch (NoInstructionsFoundException e)
		{
			out.println("Non ho trovato istruzioni!");
		}
		
		finally
		{
			//Tell the client to stop listening by sending "END"
			out.println("END");
			
			//Confirmation of received data from the client 
			try
			{
				System.out.print("Il Client ha risposto: " + br.readLine() + "\n\n");
			}
			catch (IOException e)
			{
				System.out.print("Errore nella lettura della risposta del client" + "\n\n");
			}
			
			//Close the resources
			try
			{
				in.close();
				out.close();
				socket.close();
			}
			catch (IOException ex) {}
		}
	}

	/**
	 * Access point of the server
	 * @param args Args
	 */
    public static void main(String[] args)
    {
		//Initialize server
    	ServerSocket server = null;
    	
    	//Initialize Geographic map
    	GeographicMap map = new Italy();
		
    	//Endlessly listen for a Client connection on the port number chosen
    	try
        {
		server = new ServerSocket(PORT_NUMBER);
		while (true)
		{
			Socket socket = server.accept();
				
			if (socket != null) new Server(socket, map);
		}
	}
		
    	//Handle exceptions and finally close the server
    	catch (IOException ex)
	{
		System.out.println("Impossibile eseguire il server!");
	}
	finally
	{
		try
		{
			if (server != null) server.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
    }
}
