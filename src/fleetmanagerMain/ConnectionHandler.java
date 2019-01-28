package fleetmanagerMain;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.nio.charset.StandardCharsets;


public class ConnectionHandler implements HttpHandler{
	
	private CarDatabaseHandler carHandler;																// object which handles connections to car database within the API
	
	
	boolean running=true;
	
	
	public ConnectionHandler(CarDatabaseHandler carHandler) {
		this.carHandler = carHandler;																	
	}
	
	
	//adds car to database
	//returns 0 if addition to database was successful
	//returns -1 if addition to database was unsuccessful
	private int addCarToDatabase(Car car) {
		return carHandler.executeSQLQuery(car.toAdditionQueryString());
	}
	
	//edits values of a car in database
	//by replacing a car object in database with another car
	//returns 0 if edit in database was successful
	//returns -1 if edit in database was unsuccessful
	//TODO: if previousCar cannot be removed from database and neither can nextCar, both versions will stay in database, bad outcome
	private int editCarInDatabase(Car previousCar, Car nextCar) {
		if(addCarToDatabase(nextCar) == 0) {
			if(removeCarFromDatabase(previousCar) == 0) {
				return 0;
			}else {
				removeCarFromDatabase(nextCar);
				return -1;
			}
		}
		else return -1;
	}
	
	//removes a car from database
	//returns 0 if removal from database was successful
	//returns -1 if removal from database was unsuccessful
	private int removeCarFromDatabase(Car car) {
		return carHandler.executeSQLQuery("DELETE FROM Cars WHERE Licence='" + car.getLicence() +"'");
	}
	
	
	
	//gets an object copy of a car in database
	//returns null if car is not in database
	private Car getCarInDatabase(String licence) {
		ArrayList<Car> carList = carHandler.getCarListSQLQuery("SELECT * FROM CARS WHERE Licence='" + licence + "'");
		if(carList.size() > 0)return carList.get(0);
		else return null;
	}
	
	//gets an ArrayList copy of a particular set of cars
	//returns empty ArrayList if no such cars is in database
	private ArrayList<Car> getCarListInDatabase(int yearModelMin, int yearModelMax, String brand, String model) {
		StringBuilder sb = new StringBuilder("SELECT * FROM CARS WHERE (Yearmodel BETWEEN " + yearModelMin + " AND " + yearModelMax + ")");		//creates a string with matching parameters for a SQL query
		if(!brand.equals("") && brand != null)sb.append(" AND Brand='" + brand + "'");															//if parameters have value for brand, add it to query
		if(!model.equals("") && model != null)sb.append(" AND Model='" + model + "'");															//if parameters have value for model, add it to query
		
		ArrayList<Car> carList = carHandler.getCarListSQLQuery(sb.toString());
		return carList;
	}
	
	//Lists all cars currently in database
	//returns empty list if no cars in database
	private ArrayList<Car> getAllCars() {
		return carHandler.getCarListSQLQuery("SELECT * FROM CARS");
	}
	
	//handles http requests and sends appropriate response
	@Override
	public void handle(HttpExchange exchange) {		
		try {
			
			String requestMethod = exchange.getRequestMethod();
			
			System.out.println("Exchange request method: " + requestMethod + "\n");														//Checks the request method, POST,GET,DELETE etc.
			
			StringBuilder requestHeaders = new StringBuilder();
            Headers headers = exchange.getRequestHeaders();																				//Gets the headers of the http request
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
				doPatchCarResponse(exchange, requestBody.toString());
			}
			
			//TODO: invalid request
			if(!requestMethod.equals("PATCH") && !requestMethod.equals("DELETE") && !requestMethod.equals("POST") && !requestMethod.equals("GET")) {
				
			}
			
			//close input stream after handling request
			exchange.getRequestBody().close();
			
		}catch(Exception e) {
			e.printStackTrace();
		} finally {	
			System.out.println("\n REQUEST HANDLED! \n");
		}
	}
	
	/**
	 * Handles GET-request response
	 */
	public void doGetCarResponse(HttpExchange exchange) {
		try {
			
			int responseStatus = 204;																						//assigns initial value to response status
			String responseBody = "";																						//assigns initial value to response body
			
			
			if(exchange.getRequestURI().getQuery() != null) {
				try {
					StringBuilder requestValues = new StringBuilder("");													//StringBuilder for the parameters of requested car
					System.out.println("Request parameters: " + exchange.getRequestURI().getQuery());						//print the parameters of requested car for debug purposes
					requestValues.append(exchange.getRequestURI().getQuery());												//get the parameters of requested car
					
					String licenceValue = "";																				//basic value for licence if query does not specify it
					int YearMax = 2100;																						//basic value for maximum year of car model if query does not specify it
					int YearMin = 1900;																						//basic value for minimum year of car model if query does not specify it
					String Brand = "";																						//basic value for car brand if query does not specify it
					String Model = "";																						//basic value for car model if query does not specify it
					
					ArrayList<Car> requestedCars = new ArrayList<Car>();													//ArrayList of requested cars which will be returned to client
					
					if(requestValues.indexOf("GetAllCars") == -1 && requestValues.indexOf("Licence") == -1 && requestValues.indexOf("YearMin") == -1 && requestValues.indexOf("YearMax") == -1
							&& requestValues.indexOf("Brand") == -1 && requestValues.indexOf("Model") == -1) {
						throw new IllegalArgumentException("query does not contain legal parameters");						//breaks try catch block if query is invalid or has invalid parameters
					}
					if(requestValues.indexOf("GetAllCars") != -1) {															//if query has "GetAllCars" in it, returns a list which contains all cars in database
						requestedCars = getAllCars();
					}else if(requestValues.indexOf("Licence") != -1) {														// if query has "Licence" in it, returns a list which contains
						licenceValue = requestValues.substring(requestValues.indexOf("Licence") + 8);						// one specified car in database
						requestedCars.add(getCarInDatabase(licenceValue));
					}else {																									//if query has any other specifications, return a list depending on those specifications
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
					

					
					if(requestedCars != null && requestedCars.size() != 0 && requestedCars.get(0) != null) {
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
			
			
        
			Headers responseHeaders = exchange.getResponseHeaders();																//sends content type as http header
			if(responseStatus==200)responseHeaders.add("Content-type","application/json");
			if(responseStatus==400)responseHeaders.add("Content-type","text/plain");
        

			exchange.sendResponseHeaders(responseStatus, responseBody.length() == 0 ? -1 : responseBody.length());					//if body length is 0 send -1 (no body), if not send body length
																																	//http body length header
			
			if (responseBody.length() > 0) {																						//if response has a body, send it
				try (BufferedOutputStream out = new BufferedOutputStream(exchange.getResponseBody())) {
					out.write(responseBody.getBytes("UTF-8"));
				}
			}
         
			StringBuilder headers = new StringBuilder();																			//create a string of headers for debugging purpuses
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
			int responseStatus = 400;																									//assigns initial value to response status
			String responseBody = "";																									//assigns initial value to response body
			
			try {
				Gson g = new Gson();
				Car createdCar = g.fromJson(requestBody, Car.class);
				checkCarValidity(createdCar);																							//check if created car is legal
				if(addCarToDatabase(createdCar)==0)responseStatus = 201;																//car created successfully, http response 201
				else throw new IllegalArgumentException("could not add new car-object to database");									//breaks try catch block if addition to database is unsuccessful 
				
			}catch(Exception e) {
				responseStatus = 400;
				responseBody = "Error creating car";
			}
			
			
			Headers responseHeaders = exchange.getResponseHeaders();
			if(responseStatus==400)responseHeaders.add("Content-type","text/plain");
        

			exchange.sendResponseHeaders(responseStatus, responseBody.length() == 0 ? -1 : responseBody.length());					//if body length is 0 send -1 (no body), if not send body length
																																	//http body length header
			
			if (responseBody.length() > 0) {																						//if response has a body, send it
				try (BufferedOutputStream out = new BufferedOutputStream(exchange.getResponseBody())) {
					out.write(responseBody.getBytes("UTF-8"));
				}
			}
         
			StringBuilder headers = new StringBuilder();																			//create a string of headers for debugging purpuses
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
			int responseStatus = 400;																								//assigns initial value to response status
			String responseBody = "";																								//assigns initial value to response body
			
			try {
				Gson g = new Gson();
				Car removedCar = g.fromJson(requestBody, Car.class);
				if(removeCarFromDatabase(removedCar)==0)responseStatus = 204;														//car removed successfully, http response 204
				else throw new IllegalArgumentException("could not remove car-object from database");									//breaks try catch block if removal from database is unsuccessful 
				
			}catch(Exception e) {
				responseStatus = 400;
				responseBody = "Error removing car";
			}
			
			
			Headers responseHeaders = exchange.getResponseHeaders();
			if(responseStatus==400)responseHeaders.add("Content-type","text/plain");
        

			exchange.sendResponseHeaders(responseStatus, responseBody.length() == 0 ? -1 : responseBody.length());					//if body length is 0 send -1 (no body), if not send body length
			
			if (responseBody.length() > 0) {																						//if response has a body, send it
				try (BufferedOutputStream out = new BufferedOutputStream(exchange.getResponseBody())) {
					out.write(responseBody.getBytes("UTF-8"));
				}
			}
         
			StringBuilder headers = new StringBuilder();																			//create a string of headers for debugging purpuses
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
			int responseStatus = 400;																								//assigns initial value to response status
			String responseBody = "";																								//assigns initial value to response body
			
			try {
				StringBuilder requestedCars = new StringBuilder(requestBody);
				
				String removedCarStringJson = requestedCars.substring(requestedCars.indexOf("{"), requestedCars.indexOf("}") + 1);	//add the previous version of edited car JSON string to removedCarStringJson
				requestedCars.delete(requestedCars.indexOf("{"), requestedCars.indexOf("}") + 1);
				
				String createdCarStringJson = requestedCars.substring(requestedCars.indexOf("{"), requestedCars.indexOf("}") + 1);	//add the new version of edited car JSON string to removedCarStringJson
				requestedCars.delete(requestedCars.indexOf("{"), requestedCars.indexOf("}") + 1);
				
				Gson g = new Gson();
				Car removedCar = g.fromJson(removedCarStringJson, Car.class);														//create car object from JSON
				Car createdCar = g.fromJson(createdCarStringJson, Car.class);														//create car object from JSON
				checkCarValidity(createdCar);																						//check if created car is legal
				if(editCarInDatabase(removedCar, createdCar)==0)responseStatus = 204;												//car edited successfully, http response 201
				else throw new IllegalArgumentException("could not edit car-object in database");									//breaks try catch block if edit in database is unsuccessful
				
			}catch(Exception e) {
				responseStatus = 400;
				responseBody = "Error editing car";
			}
			
			
			Headers responseHeaders = exchange.getResponseHeaders();
			if(responseStatus==400)responseHeaders.add("Content-type","text/plain");
        

			exchange.sendResponseHeaders(responseStatus, responseBody.length() == 0 ? -1 : responseBody.length());					//if body length is 0 send -1 (no body), if not send body length
			
			if (responseBody.length() > 0) {																						//if response has a body, send it
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
	
	
	
	public void checkCarValidity(Car car) throws IllegalArgumentException{
		if(car.getLicence() == null || car.getLicence() == "")throw new IllegalArgumentException("licence cannot be null or empty");
	}
		
	

}
