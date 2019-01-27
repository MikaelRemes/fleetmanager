package fleetmanagerMain;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.nio.charset.StandardCharsets;

public class FleetManager implements HttpHandler{
	
	private CarDatabaseHandler carHandler;
	
	// server port
	static final int PORT = 8082;
	
	boolean running=true;
	
	
	public FleetManager() {
		carHandler = new CarDatabaseHandler();
	}

	public static void main(String[] args) {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
			
			FleetManager connection= new FleetManager();
							
			server.createContext("/test", connection);
	        server.setExecutor(Executors.newFixedThreadPool(1)); 										// creates a default executor
	        server.start();
			
			System.out.println("Server online \n");
			
			while(connection.running) {
				Thread.sleep(1000);
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
			
			StringBuilder headers = new StringBuilder();
            Headers requestHeaders = exchange.getRequestHeaders();																	//Gets the headers of the http request
            for (Map.Entry<String, List<String>> header : requestHeaders.entrySet()) {
                headers.append(header);
                headers.append("\n");
            }
            
            System.out.println("connection headers: \n" + headers.toString());
			
			StringBuilder body = new StringBuilder();																				//Gets the body of the http request
            try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8.name())) {
                char[] buffer = new char[256];
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    body.append(buffer, 0, read);
                }
            }
            
            exchange.getRequestBody().close();
            System.out.println("connection body: \n" + body.toString() + "\n");      
            
            

			//client wishes to see a particular set of cars in database
			if(requestMethod.equals("GET")) {
				doGetCarResponse(exchange,body);
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
				
			} catch (Exception e) {
				System.err.println("Error closing connections: " + e.getMessage());
			} 
			
			running=false;	
			System.out.println("Server offline");
		}
	}
	
	
	public void doGetCarResponse(HttpExchange exchange, StringBuilder body) throws IOException{
		
		 Headers requestHeaders = exchange.getRequestHeaders();
         Headers responseHeaders = exchange.getResponseHeaders();
         for (Map.Entry<String, List<String>> header : requestHeaders.entrySet()) {
             responseHeaders.put(header.getKey(), header.getValue());
         }
         
         exchange.sendResponseHeaders(200, body.length() == 0 ? -1 : body.length());
         if (body.length() > 0) {
             try (OutputStream out = exchange.getResponseBody()) {
                 out.write(body.toString().getBytes(StandardCharsets.UTF_8.name()));
             }
         }
         
         System.out.println("responce head: " + responseHeaders.toString());
         System.out.println("responce body: " + body.toString());
	}
	
	public void doPostCarResponse(HttpExchange exchange, StringBuilder body) throws IOException{
		
		Headers requestHeaders = exchange.getRequestHeaders();
        Headers responseHeaders = exchange.getResponseHeaders();
        for (Map.Entry<String, List<String>> header : requestHeaders.entrySet()) {
            responseHeaders.put(header.getKey(), header.getValue());
        }
        
        exchange.sendResponseHeaders(200, body.length() == 0 ? -1 : body.length());
        if (body.length() > 0) {
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body.toString().getBytes(StandardCharsets.UTF_8.name()));
            }
        }
        
        System.out.println("responce head: " + responseHeaders.toString());
        System.out.println("responce body: " + body.toString());
	}
		
	

}
