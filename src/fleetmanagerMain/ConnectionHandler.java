package fleetmanagerMain;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.nio.charset.StandardCharsets;


public class ConnectionHandler implements HttpHandler{
	
	private CarDatabaseHandler carHandler;																// object which handles connections to car database within the API
	
	static final int PORT = 8083;																		// server PORT address
	
	static final int maxOnlineTime = 60;																// server online for maximum of 1 minute
	static int currentOnlineTime = 0;																	// clock for server online time
	static int connectionNumber = 1;																	// How many connections/requests have been made
	
	boolean running=true;
	
	
	public ConnectionHandler(CarDatabaseHandler carHandler) {
		this.carHandler = carHandler;																	
	}

	public static void main(String[] args) {
		try {
			
			CarDatabaseHandler carDatabaseHandler = new CarDatabaseHandler();							// creates a connection to database which holds cars

			HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);						// creates the http server to port address PORT
			
			ConnectionHandler connection= new ConnectionHandler(carDatabaseHandler);					// creates a thread which handles the next httprequest
							
			server.createContext("/test", connection);													// adds the thread to http server
																										// write "http://localhost:8083/test" to connect
	        server.setExecutor(null); 																	// creates a default executor
	        server.start();																				
	        
	        
			System.out.println("connection handler number " + connectionNumber + " online \n");
			
			while(currentOnlineTime <= maxOnlineTime) {													
				Thread.sleep(1000);																		// setup time for connectionHandler and a clock for server online time
				
				if(connection.running==false) {															// once response has been done, creates new connectionhandler thread for next response
					connection= new ConnectionHandler(carDatabaseHandler);								// creates a new connectionhandler thread
					server.createContext("/test", connection);											// adds it to http server
					connectionNumber++;
					System.out.println("connection handler number " + connectionNumber + " online \n");
				}
				
				currentOnlineTime++;																	// increment clock
			}
			
			carDatabaseHandler.closeConnection();														// close connection to database which holds cars
			System.out.println("Closed connection to database");
			
			server.stop(1);																				// closes http server after timer has ran out
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	//adds car to database
	private void addCarToDatabase(Car car) {
		carHandler.executeSQLQuery("INSERT INTO Cars (Brand,Model,Licence,Yearmodel,Inspection,EngineSize,EnginePower) VALUES (" + car.toQueryString() + ")");
	}
	
	//edits values of a car in database
	//by replacing a car object in database with another car
	private void editCarInDatabase(Car previousCar, Car nextCar) {
		removeCarFromDatabase(previousCar);
		addCarToDatabase(nextCar);
	}
	
	//removes a car from database
	private void removeCarFromDatabase(Car car) {
		carHandler.executeSQLQuery("DELETE FROM Cars WHERE Licence='" + car.getLicence() +"'");
	}
	
	
	
	//gets an object copy of a car in database
	private Car getCarInDatabase(String licence) {
		try {
			ArrayList<Car> carList = carHandler.getCarListSQLQuery("SELECT * FROM CARS WHERE Licence='" + licence + "'");
			return carList.get(0);
		}catch(Exception e) {
			return null;
		}
	}
	
	//gets an ArrayList copy of a particular set of cars
	private ArrayList<Car> getCarListInDatabase(int yearModelMin, int yearModelMax, String brand, String model) {
		StringBuilder sb = new StringBuilder("SELECT * FROM CARS WHERE (Yearmodel BETWEEN " + yearModelMin + " AND " + yearModelMax + ")");		//creates a string with matching parameters for a SQL query
		if(!brand.equals(""))sb.append(" AND Brand='" + brand + "'");
		if(!model.equals(""))sb.append(" AND Model='" + model + "'");
		
		
		ArrayList<Car> carList = carHandler.getCarListSQLQuery(sb.toString());
		return carList;
	}
	
	//Lists all cars currently in database
	private ArrayList<Car> getAllCars() {
		return carHandler.getCarListSQLQuery("SELECT * FROM CARS");
	}
	
	//handles http requests and sends appropritate response
	@Override
	public void handle(HttpExchange exchange) {		
		try {
			
			String requestMethod = exchange.getRequestMethod();
			
			System.out.println("Exchange request method: " + requestMethod + "\n");														//Checks the request method, POST,GET,DELETE etc.
			
			StringBuilder requestHeaders = new StringBuilder();
            Headers headers = exchange.getRequestHeaders();																		//Gets the headers of the http request
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            	requestHeaders.append(header);
            	requestHeaders.append("\n");
            }
            
            System.out.println("connection headers: \n" + requestHeaders.toString());
			
			StringBuilder requestBody = new StringBuilder();																					//Gets the body of the http request
            try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8.name())) {
                char[] buffer = new char[256];
                int read;
                while ((read = reader.read(buffer)) != -1) {
                	requestBody.append(buffer, 0, read);
                }
            }
            
            
            System.out.println("connection body: \n" + requestBody.toString() + "\n");      
            
            

			//client wishes to see a particular set of cars in database
			if(requestMethod.equals("GET")) {
				doGetCarResponse(exchange);
			}
			
			//client wishes to add a car to database
			if(requestMethod.equals("POST")) {
				doPostCarResponse(exchange, requestBody.toString());
			}
			
			//client wishes to delete a car in database
			if(requestMethod.equals("DELETE")) {
				doDeleteCarResponse(exchange, requestBody.toString());
			}
			
			//client wishes to edit a car in database
			if(requestMethod.equals("PATCH")) {
				
			}
			
			//invalid request
			if(!requestMethod.equals("PATCH") && !requestMethod.equals("DELETE") && !requestMethod.equals("POST") && !requestMethod.equals("GET")) {
				
			}
        
			exchange.getRequestBody().close();
			
		}catch(Exception e) {
			e.printStackTrace();
		} finally {
			
			running=false;	
			System.out.println("connection handler offline");
		}
	}
	
	
	public void doGetCarResponse(HttpExchange exchange) {
		try {
			
			int responseStatus = 400;
			String responseBody = "";
			
			
			if(exchange.getRequestURI().getQuery() != null) {
				try {
					StringBuilder requestValues = new StringBuilder("");													//get the parameters of requested car
					System.out.println("Request parameters: " + exchange.getRequestURI().getQuery());
					requestValues.append(exchange.getRequestURI().getQuery());
					
					String licenceValue = "";
					int YearMax = 3000;
					int YearMin = 0;
					String Brand = "";
					String Model = "";
					ArrayList<Car> requestedCars = new ArrayList<Car>();
					
					
					
					if(requestValues.indexOf("GetAllCars") != -1) {
						requestedCars = getAllCars();
					}else if(requestValues.indexOf("Licence") != -1) {
						licenceValue = requestValues.substring(requestValues.indexOf("Licence") + 8);
						requestedCars.add(getCarInDatabase(licenceValue));
					}else {
						if(requestValues.indexOf("YearMin") != -1) {
							if(requestValues.indexOf("&", requestValues.indexOf("YearMin")) != -1) {
								YearMin = Integer.valueOf(requestValues.substring(requestValues.indexOf("YearMin") + 8, requestValues.indexOf("&", requestValues.indexOf("YearMin"))));
								requestValues.delete(requestValues.indexOf("YearMin"), requestValues.indexOf("&", requestValues.indexOf("YearMin")) + 1);
							}else YearMin = Integer.valueOf(requestValues.substring(requestValues.indexOf("YearMin") + 8));
						}
						if(requestValues.indexOf("YearMax") != -1) {
							if(requestValues.indexOf("&", requestValues.indexOf("YearMax")) != -1) {
								YearMax = Integer.valueOf(requestValues.substring(requestValues.indexOf("YearMax") + 8, requestValues.indexOf("&", requestValues.indexOf("YearMax"))));
								requestValues.delete(requestValues.indexOf("YearMax"), requestValues.indexOf("&", requestValues.indexOf("YearMax")) + 1);
							}else YearMax = Integer.valueOf(requestValues.substring(requestValues.indexOf("YearMax") + 8));
						}
						if(requestValues.indexOf("Brand") != -1) {
							if(requestValues.indexOf("&", requestValues.indexOf("Brand")) != -1) {
								Brand = requestValues.substring(requestValues.indexOf("Brand") + 6, requestValues.indexOf("&", requestValues.indexOf("Brand")));
								requestValues.delete(requestValues.indexOf("Brand"), requestValues.indexOf("&", requestValues.indexOf("Brand")) + 1);
							}else Brand = requestValues.substring(requestValues.indexOf("Brand") + 6);
						}
						if(requestValues.indexOf("Model") != -1) {
							if(requestValues.indexOf("&", requestValues.indexOf("Model")) != -1) {
								Model = requestValues.substring(requestValues.indexOf("Model") + 6, requestValues.indexOf("&", requestValues.indexOf("Model")));
								requestValues.delete(requestValues.indexOf("Model"), requestValues.indexOf("&", requestValues.indexOf("Model")) + 1);
							}else Model = requestValues.substring(requestValues.indexOf("Model") + 6);
						}
						requestedCars = getCarListInDatabase(YearMin, YearMax, Brand, Model);
					}
					

					
					if(requestedCars != null && requestedCars.size() != 0) {
						Gson g = new Gson();
						responseBody = g.toJson(requestedCars);
						responseStatus = 200;
					}else {
						responseBody = "";
						responseStatus = 204;
					}
				}catch(Exception e) {
					responseStatus = 400;
					responseBody = "URI request parameters faulty";
				}
			}
			
			
        
			Headers responseHeaders = exchange.getResponseHeaders();
			if(responseStatus==200)responseHeaders.add("Content-type","application/json");
			if(responseStatus==400)responseHeaders.add("Content-type","text/plain");
        

			exchange.sendResponseHeaders(responseStatus, responseBody.length() == 0 ? -1 : responseBody.length());					//if body length is 0 send -1 (no body), if not send body length
			if (responseBody.length() > 0) {
				try (BufferedOutputStream out = new BufferedOutputStream(exchange.getResponseBody())) {
					out.write(responseBody.getBytes("UTF-8"));
				}
			}
         
			StringBuilder headers = new StringBuilder();
			for (Map.Entry<String, List<String>> header : responseHeaders.entrySet()) {
				headers.append(header);
				headers.append("\n");
			}
         
         
			System.out.println("response head: \n" + headers.toString());
			System.out.println("response body: \n" + responseBody.toString() + "\n");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void doPostCarResponse(HttpExchange exchange, String requestBody) throws IOException{
		
		try {
			int responseStatus = 400;
			String responseBody = "";
			
			try {
				Gson g = new Gson();
				Car createdCar = g.fromJson(requestBody, Car.class);
				addCarToDatabase(createdCar);
				responseStatus = 201;
				
			}catch(Exception e) {
				responseStatus = 400;
				responseBody = "Error creating car";
			}
			
			
			Headers responseHeaders = exchange.getResponseHeaders();
			if(responseStatus==400)responseHeaders.add("Content-type","text/plain");
        

			exchange.sendResponseHeaders(responseStatus, responseBody.length() == 0 ? -1 : responseBody.length());					//if body length is 0 send -1 (no body), if not send body length
			if (responseBody.length() > 0) {
				try (BufferedOutputStream out = new BufferedOutputStream(exchange.getResponseBody())) {
					out.write(responseBody.getBytes("UTF-8"));
				}
			}
         
			StringBuilder headers = new StringBuilder();
			for (Map.Entry<String, List<String>> header : responseHeaders.entrySet()) {
				headers.append(header);
				headers.append("\n");
			}
         
         
			System.out.println("response head: \n" + headers.toString());
			System.out.println("response body: \n" + responseBody.toString() + "\n");
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void doDeleteCarResponse(HttpExchange exchange, String requestBody) throws IOException{
		
		try {
			int responseStatus = 400;
			String responseBody = "";
			
			try {
				Gson g = new Gson();
				Car createdCar = g.fromJson(requestBody, Car.class);
				removeCarFromDatabase(createdCar);
				responseStatus = 204;
				
			}catch(Exception e) {
				responseStatus = 400;
				responseBody = "Error removing car";
			}
			
			
			Headers responseHeaders = exchange.getResponseHeaders();
			if(responseStatus==400)responseHeaders.add("Content-type","text/plain");
        

			exchange.sendResponseHeaders(responseStatus, responseBody.length() == 0 ? -1 : responseBody.length());					//if body length is 0 send -1 (no body), if not send body length
			if (responseBody.length() > 0) {
				try (BufferedOutputStream out = new BufferedOutputStream(exchange.getResponseBody())) {
					out.write(responseBody.getBytes("UTF-8"));
				}
			}
         
			StringBuilder headers = new StringBuilder();
			for (Map.Entry<String, List<String>> header : responseHeaders.entrySet()) {
				headers.append(header);
				headers.append("\n");
			}
         
         
			System.out.println("response head: \n" + headers.toString());
			System.out.println("response body: \n" + responseBody.toString() + "\n");
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void doPatchCarResponse(HttpExchange exchange, String requestBody) throws IOException{
		
		try {
			int responseStatus = 400;
			String responseBody = "";
			
			try {
				StringBuilder requestedCars = new StringBuilder(requestBody);
				
				String removedCarStringJson = requestedCars.substring(requestedCars.indexOf("{"), requestedCars.indexOf("}") + 1);
				requestedCars.delete(requestedCars.indexOf("{"), requestedCars.indexOf("}") + 1);
				
				String createdCarStringJson = requestedCars.substring(requestedCars.indexOf("{"), requestedCars.indexOf("}") + 1);
				requestedCars.delete(requestedCars.indexOf("{"), requestedCars.indexOf("}") + 1);
				
				Gson g = new Gson();
				Car removedCar = g.fromJson(removedCarStringJson, Car.class);
				Car createdCar = g.fromJson(createdCarStringJson, Car.class);
				editCarInDatabase(removedCar, createdCar);
				responseStatus = 204;
				
			}catch(Exception e) {
				responseStatus = 400;
				responseBody = "Error editing car";
			}
			
			
			Headers responseHeaders = exchange.getResponseHeaders();
			if(responseStatus==400)responseHeaders.add("Content-type","text/plain");
        

			exchange.sendResponseHeaders(responseStatus, responseBody.length() == 0 ? -1 : responseBody.length());					//if body length is 0 send -1 (no body), if not send body length
			if (responseBody.length() > 0) {
				try (BufferedOutputStream out = new BufferedOutputStream(exchange.getResponseBody())) {
					out.write(responseBody.getBytes("UTF-8"));
				}
			}
         
			StringBuilder headers = new StringBuilder();
			for (Map.Entry<String, List<String>> header : responseHeaders.entrySet()) {
				headers.append(header);
				headers.append("\n");
			}
         
         
			System.out.println("response head: \n" + headers.toString());
			System.out.println("response body: \n" + responseBody.toString() + "\n");
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
		
	

}
