package fleetmanagerMain;

import java.util.ArrayList;

public class FleetManager {
	
	private CarDatabaseHandler carHandler;
	
	public FleetManager() {
		carHandler = new CarDatabaseHandler();
		
		//Following lines are for debug purpouses
		//
		Car testcar = new Car("Ford","Fiesta","LBD-013",2001,"23-01-2009",6,100);
		
		addCarToDatabase(testcar);
		
		displayCarListInDatabase(2000,2012,"Ford","");
		listAllCars();
		
		removeCarFromDatabase(testcar);
		//
		//
		
		
		
		carHandler.closeConnection();
	}

	public static void main(String[] args) {
		new FleetManager();
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

}
