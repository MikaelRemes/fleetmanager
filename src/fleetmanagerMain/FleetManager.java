package fleetmanagerMain;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

public class FleetManager implements Runnable{
	
	private CarDatabaseHandler carHandler;
	
	// server port
	static final int PORT = 8090;
	
	
	private ServerSocket server;
	private Socket socket;
	
	public FleetManager(Socket socket, ServerSocket serverSocket) {
		carHandler = new CarDatabaseHandler();
		this.socket=socket;
		this.server=serverSocket;
	}

	public static void main(String[] args) {
		try {
			ServerSocket server = new ServerSocket(PORT);
			
			FleetManager manager = new FleetManager(server.accept(),server);
				
			Thread thread = new Thread(manager);
			thread.start();
			
			System.out.println("Server online");
			
			
			
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
	
	@Override
	public void run() {
		BufferedReader in = null;
		PrintWriter out = null;
		BufferedOutputStream dataOut = null;
		
		try {
			
			// input stream
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			//output stream for headers
			out = new PrintWriter(socket.getOutputStream());
			
			// output stream for requested data
			dataOut = new BufferedOutputStream(socket.getOutputStream());
			
			//get first line of the request from the client (HTTP method)
			String input = in.readLine();
			System.out.println(input);
				
			//request parsing
			StringTokenizer parse = new StringTokenizer(input);
				
			//HTTP method of the client
			String method = parse.nextToken().toUpperCase(); 
				
				
			// GET method
			if (method.equals("GET")) {
				
				
					System.out.println("GET method successful");
						
				}
			
			
		}catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				//close all connections of FleetManager
				in.close();
				out.close();
				dataOut.close();
				socket.close();
				
				//TODO: these handled outside run-method
				server.close();
				carHandler.closeConnection();
				
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			} 
			System.out.println("Server offline");
		}
	}
	
	
		
	

}
