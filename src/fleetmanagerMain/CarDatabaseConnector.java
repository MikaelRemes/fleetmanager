package fleetmanagerMain;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class CarDatabaseConnector {
	private Connection conn=null;
	private Statement statement=null;

	//used to connect car database
	public CarDatabaseConnector() {
		
		try {
			Class.forName("org.sqlite.JDBC"); 										//makes sure that sqlite is loaded and registered with system
			conn = DriverManager.getConnection("jdbc:sqlite:CarDatabase.db");			//connects to database named "CarDatabase.db"
			
			System.out.println("connection successful");							//no errors in debugging so far
			
			ListCarsTemporary();
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
		
	//temporary testing method
	//TODO: works even with missing values for car
	public void ListCarsTemporary() {
		try{
			this.statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM Cars");
			
			while(rs.next()) {
				Car displaycar = new Car(rs.getString("Brand"),rs.getString("Model"),rs.getString("Licence"),rs.getInt("YearModel"),rs.getString("Inspection"),rs.getInt("EngineSize"),rs.getInt("EnginePower"));
				
				System.out.println(displaycar.toString());
			}
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void closeConnection() {
		try {
			conn.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
		

}
