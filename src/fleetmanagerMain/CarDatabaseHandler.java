package fleetmanagerMain;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class CarDatabaseHandler {
	private Connection conn=null;
	private Statement statement=null;

	
	/**
	 * constructor
	 * Connects to car database
	 */
	public CarDatabaseHandler() {
		
		try {
			Class.forName("org.sqlite.JDBC"); 																//makes sure that sqlite is loaded and registered with system
			conn = DriverManager.getConnection("jdbc:sqlite:CarDatabase.db");								//connects to database named "CarDatabase.db"
			
			System.out.println("connected to database successfully");										//connection to car database has been established
					
			
		}catch(Exception e) {
			System.out.println("did not connect to database");												//connection to database faulty
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * executes a specified SQL query
	 * @param SQL query as string
	 * @return returns 0 if query ran without errors, returns -1 if query is unsuccessful
	 */
	public int executeSQLQuery(String query) {
		try{
			this.statement = conn.createStatement();														//prepares to run query
			statement.execute(query);																		//runs a specified query
			return 0;
			
		}catch(Exception e) {
			System.out.println("Error in query: ");															//error when running SQL query
			e.printStackTrace();
			return -1;
		}
	}
	
	
	
	/**
	 * @param search query as string
	 * @return returns the results of the query as an arraylist object which contains car objects. In the case of error, returns null
	 */
	public ArrayList<Car> getCarListSQLQuery(String query){
		ArrayList<Car> cars = new ArrayList<Car>();															//initializes result ArrayList
		try {
			this.statement = conn.createStatement();														//prepares to run query
			ResultSet rs = statement.executeQuery(query);													//runs the specified query and adds results to resultSet
			while(rs.next()) {
				Car displaycar = new Car(rs.getString("Brand"),rs.getString("Model"),rs.getString("Licence"),rs.getInt("YearModel"),rs.getString("Inspection"),rs.getInt("EngineSize"),rs.getInt("EnginePower"));
				cars.add(displaycar);
			}
		}catch(Exception e) {
			System.out.println("Error in query: ");															//error when running SQL query
			e.printStackTrace();
			
			return null;
		}
		
		return cars;
	}
	
	/**
	 * closes connection to cardatabase
	 */
	public void closeConnection() {
		try {
			conn.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
		

}
