package pt.unl.fct.di.apdc.webapp.util;

public class RegisterData {
	
	public String username;
	public String email;
	public String name;
	public String password;
	public String profileStatus;
	public String phoneNum;
	public String mobilePhone;
	public String occupation;
	public String workingPlace;
	public String address;
	public String city;
	public String cp;
	public String nif;
	
	public RegisterData() {
		
	}
	
	public RegisterData(String username, String email, String name, String password,
			String profileStatus, String phoneNum, String mobilePhone, String occupation, String workingPlace, String address, String city, String cp, String nif) {
		
		this.username = username;
		this.email = email;
		this.name = name;
		this.password = password;
		this.profileStatus = profileStatus;
		this.phoneNum = phoneNum;
		this.mobilePhone = mobilePhone;
		this.occupation = occupation;
		this.workingPlace = workingPlace;
		this.address = address;
		this.city = city;
		this.cp = cp;
		this.nif = nif;
		
	}

	public boolean validRegistration() {
		// TODO Auto-generated method stub
		return validEmail() && (!name.equals(null) || !username.equals(null) || !password.equals(null));
	}
	
	public boolean validEmail() {
		return email.contains("@") && (email.contains(".pt") || email.contains(".com") || email.contains(".org"));
	}

}
