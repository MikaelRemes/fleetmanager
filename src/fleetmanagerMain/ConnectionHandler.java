package fleetmanagerMain;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.nio.charset.StandardCharsets;

public class ConnectionHandler implements HttpHandler{
	
	private CarDatabaseHandler carHandler;
	
	// server port
	static final int PORT = 8082;
	
	static final int maxOnlineTime = 60;														//server online for maximum of 1 minute
	static int currentOnlineTime = 0;
	static int connectionNumber = 1;															//How many connections have been made
	
	boolean running=true;
	
	
	public ConnectionHandler() {
		carHandler = new CarDatabaseHandler();
	}

	public static void main(String[] args) {
		try {

			HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
			
			ConnectionHandler connection= new ConnectionHandler();
							
			server.createContext("/test", connection);													// write "http://localhost:8082/test" to connect
	        server.setExecutor(null); 																	// creates a default executor
	        server.start();
			
			System.out.println("connection handler number " + connectionNumber + " online \n");
			
			while(currentOnlineTime <= maxOnlineTime) {
				Thread.sleep(500);																		//setup time for connectionHandler
				
				if(connection.running==false) {															//once response has been done, creates new connectionhandler for next response
					connection= new ConnectionHandler();
					server.createContext("/test", connection);
					connectionNumber++;
					System.out.println("connection handler number " + connectionNumber + "online \n");
				}
				
				Thread.sleep(500);																		//setup time for connectionHandler
				currentOnlineTime++;
			}
			
			
			server.stop(1);
			
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
	
	//displays the information of a particular car
	private void displayCarInDatabase(Car car) {
		ArrayList<Car> carList = carHandler.getCarListSQL("SELECT * FROM CARS WHERE Licence='" + car.getLicence() + "'");
		for(Car carX : carList) {
			System.out.println(carX.toString());
		}
	}
	
	//displays the information of a particular set of cars
	private void displayCarListInDatabase(int yearModelMin, int yearModelMax, String brand, String model) {
		StringBuilder sb = new StringBuilder("SELECT * FROM CARS WHERE Yearmodel BETWEEN " + yearModelMin + " AND " + yearModelMax);		//creates a string with matching parameters for a SQL query
		if(!brand.equals(""))sb.append(" AND Brand='" + brand + "'");
		if(!model.equals(""))sb.append(" AND Model='" + model + "'");
		
		
		ArrayList<Car> carList = carHandler.getCarListSQL(sb.toString());
		for(Car car : carList) {
			System.out.println(car.toString());
		}
	}
	
	//Lists all cars currently in database
	private void listAllCars() {
		ArrayList<Car> carList = carHandler.getCarListSQL("SELECT * FROM CARS");
		for(Car car : carList) {
			System.out.println(car.toString());
		}
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
            
            exchange.getRequestBody().close();
            System.out.println("connection body: \n" + requestBody.toString() + "\n");      
            
            

			//client wishes to see a particular set of cars in database
			if(requestMethod.equals("GET")) {
				doGetCarResponse(exchange,requestHeaders, requestBody);
			}
			
			//client wishes to add a car to database
			if(requestMethod.equals("POST")) {
				
			}
			
			//client wishes to delete a car in database
			if(requestMethod.equals("DELETE")) {
				
			}
			
			//client wishes to edit a car in database
			if(requestMethod.equals("PATCH")) {
				
			}
			
			//invalid request
			if(!requestMethod.equals("PATCH") && !requestMethod.equals("DELETE") && !requestMethod.equals("POST") && !requestMethod.equals("GET")) {
				
			}
        
            
		}catch(Exception e) {
			e.printStackTrace();
		} finally {
			
			try {
				//TODO: these handled outside method
				carHandler.closeConnection();
				System.out.println("Closed connection to database");
				
			} catch (Exception e) {
				System.err.println("Error closing connections: " + e.getMessage());
			} 
			
			running=false;	
			System.out.println("connection handler offline");
		}
	}
	
	
	public void doGetCarResponse(HttpExchange exchange, StringBuilder requestHeaders, StringBuilder requestBody) throws IOException{
		
      
        String responseBody = "Hello";
        
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.add("Content-type","text/plain");
        

         exchange.sendResponseHeaders(200, responseBody.length() == 0 ? -1 : responseBody.length());					//if body length is 0 send -1 (no body), if not send body length
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
	}
	
	public void doPostCarResponse(HttpExchange exchange, Headers requestHeaders, StringBuilder body) throws IOException{
		
		
        Headers responseHeaders = exchange.getResponseHeaders();
        
        
        System.out.println("responce head: \n" + responseHeaders.toString());
        System.out.println("responce body: \n" + body.toString());
	}
		
	

}