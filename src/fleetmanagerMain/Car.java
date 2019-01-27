package fleetmanagerMain;


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
		return brand + " " + model + " " + licence + " " + yearModel + " " + inspectionDate + " " + engineSize + " " + enginePower;
	}

	public String toQueryString() {
		return "'" + brand + "','" + model + "','" + licence + "','" + yearModel + "','" + inspectionDate + "','" + engineSize + "','" + enginePower + "'";
	}


	public String getBrand() {
		return brand;
	}
	
	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getLicence() {
		return licence;
	}

	public void setLicence(String licence) {
		this.licence = licence;
	}

	public int getYearModel() {
		return yearModel;
	}

	public void setYearModel(int yearModel) {
		this.yearModel = yearModel;
	}

	public String getInspectionDate() {
		return inspectionDate;
	}

	public void setInspectionDate(String inspectionDate) {
		this.inspectionDate = inspectionDate;
	}

	public int getEngineSize() {
		return engineSize;
	}

	public void setEngineSize(int engineSize) {
		this.engineSize = engineSize;
	}

	public int getEnginePower() {
		return enginePower;
	}

	public void setEnginePower(int enginePower) {
		this.enginePower = enginePower;
	}
	

}
