package fleetmanagerMain;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class ServerStarter {
	
	static final int PORT = 8083;																		// server PORT address
	
	static final int maxOnlineTime = 3600;																// server online for maximum of 1 hour
	static int currentOnlineTime = 0;																	// clock for server online time

	/**
	 * Starts the server.
	 */
	public static void main(String[] args) {
		try {
			
			CarDatabaseHandler carDatabaseHandler = new CarDatabaseHandler();							// creates a connection to database which holds cars

			HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);						// creates the http server to port address PORT
			
			ConnectionHandler connection = new ConnectionHandler(carDatabaseHandler);					// creates a thread which handles the next httprequest and does the requested methods
							
			server.createContext("/test", connection);													// adds the thread to http server
																										// write "http://localhost:8083/test" to connect
	        server.setExecutor(null); 																	// creates a default executor
	        server.start();																				
	        
	        
			System.out.println("server and connection handler online \n");
			
			while(currentOnlineTime <= maxOnlineTime) {													
				Thread.sleep(1000);																		// setup time for connectionHandler and a clock for server online time
				
				currentOnlineTime++;																	// increment clock
			}
			
			carDatabaseHandler.closeConnection();														// close connection to database which holds cars
			System.out.println("Closed connection to database");
			
			server.stop(1);																				// closes http server after timer has ran out
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
