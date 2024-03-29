package fleetmanagerMain;


public class Car {
	private String brand;
	private String model;
	private String licence;
	private int yearModel;
	private String inspectionDate;
	private int engineSize;
	private int enginePower;
	
	
	/**
	 * constructor
	 * licence cannot be null, it is the primary key value!!
	 * @param null string values for missing string parameters and integer values 0 when missing parameters
	 */
	public Car(String brand, String model, String licence, int yearModel, String inspectionDate, int engineSize, int enginePower) throws IllegalArgumentException{
		this.brand=brand;
		this.model=model;
		if(!licence.equals("") && licence!=null)this.licence=licence;
		else throw new IllegalArgumentException("Licence cannot be null or empty");
		this.yearModel=yearModel;
		this.inspectionDate=inspectionDate;
		this.engineSize=engineSize;
		this.enginePower=enginePower;
	}
	
	/**
	 * Turns the car object into an INSERT query string
	 * @return "INSERT INTO CARS car parameters X VALUES car Y" -query string
	 */
	public String toAdditionQueryString() {
		StringBuilder carQueryString = new StringBuilder("");
		StringBuilder valuesQueryString = new StringBuilder("");
		if(brand != null) {
			carQueryString.append("'" + brand + "',");
			valuesQueryString.append("Brand,");
		}
		if(model != null) {
			carQueryString.append("'" + model + "',");
			valuesQueryString.append("Model,");
		}
		if(licence != null) {
			carQueryString.append("'" + licence + "',");
			valuesQueryString.append("Licence,");
		}
		if(yearModel != 0) {
			carQueryString.append("'" + yearModel + "',");
			valuesQueryString.append("Yearmodel,");
		}
		if(inspectionDate != null) {
			carQueryString.append("'" + inspectionDate + "',");
			valuesQueryString.append("Inspection,");
		}
		if(engineSize != 0) {
			carQueryString.append("'" + engineSize + "',");
			valuesQueryString.append("EngineSize,");
		}
		if(enginePower != 0) {
			carQueryString.append("'" + enginePower + "',");
			valuesQueryString.append("EnginePower,");
		}
		
		carQueryString.deleteCharAt(carQueryString.length() - 1);
		valuesQueryString.deleteCharAt(valuesQueryString.length()-1);
		
		return "INSERT INTO Cars (" + valuesQueryString.toString() +") VALUES (" + carQueryString.toString() + ")";
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
