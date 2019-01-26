package fleetmanagerMain;

import java.util.ArrayList;

public class FleetManager {
	
	private CarDatabaseConnector connector;
	
	public FleetManager() {
		connector = new CarDatabaseConnector();
		
		connector.closeConnection();
	}

	public static void main(String[] args) {
		new FleetManager();
	}
	
	//adds car to database
	private void addCarToDatabase(Car car) {
		
	}
	
	//edits values of a car in database
	private void editCarInDatabase(Car car, String[] args) {
		
	}
	
	//removes a car from database
	private void removeCarFromDatabase(Car car) {
	
	}
	
	//displays the information of a particular car
	private void displayCarInDatabase(Car car) {
		
	}
	
	//displays the information of a particular set of cars
	private void displayCarListInDatabase(ArrayList<Car> cars) {
		
	}

}
