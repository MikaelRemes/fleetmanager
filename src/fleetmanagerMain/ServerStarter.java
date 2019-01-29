package fleetmanagerMain;

import java.net.InetSocketAddress;


import com.sun.net.httpserver.HttpServer;

public class ServerStarter{
	
	static final int PORT = 8083;																		// server PORT address
	
	static final String dashName = "/cars";																//URI after "/" sign text for API 
	
	static final int maxOnlineTime = 3600;																// server online for maximum of 1 hour
	static int currentOnlineTime = 0;																	// clock for server online time

	/**
	 * Starts the server and starts server GUI
	 */
	public static void main(String[] args) {
		try {
			
			CarDatabaseHandler carDatabaseHandler = new CarDatabaseHandler();							// creates a connection to database which holds cars

			HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);						// creates the http server to port address PORT
			
			ConnectionHandler connection = new ConnectionHandler(carDatabaseHandler);					// creates a thread which handles the next httprequest and does the requested methods
							
			server.createContext(dashName, connection);													// adds the thread to http server
																										// write "http://localhost:8083/cars" to connect
	        server.setExecutor(null); 																	// creates a default executor
	        
	        Thread.sleep(100);																			// setup time for connectionHandler and a clock for server online time
	        
	        ServerUI ui = new ServerUI(server);																		//generates UI which handles starting of server, closing of server and communication to server user
	        
			
			while(currentOnlineTime <= maxOnlineTime) {													
				Thread.sleep(1000);																		// timer for clock
				if(ui.getServerStopped())break;															// if UI has stopped the server, break out of loop
				currentOnlineTime++;																	// increment clock
			}
			
			carDatabaseHandler.closeConnection();														// close connection to database which holds cars
			System.out.println("Closed connection to database");
			
			server.stop(1);																				// closes http server after timer has ran out in case user hasn't closed it yet
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
