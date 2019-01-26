package fleetmanagerMain;

import java.util.Date;

public class Car {
	private String brand;
	private String model;
	private String licence;
	private int yearModel;
	private String inspectionDate;
	private int engineSize;
	private int enginePower;

	public Car(String brand, String model, String licence, int yearModel, String inspectionDate, int engineSize, int enginePower) {
		this.brand=brand;
		this.model=model;
		this.licence=licence;
		this.yearModel=yearModel;
		this.inspectionDate=inspectionDate;
		this.engineSize=engineSize;
		this.enginePower=enginePower;
		
	}
	
	
	@Override
	public String toString() {
		
		return brand + " " + model + " " + licence;
	}

}
