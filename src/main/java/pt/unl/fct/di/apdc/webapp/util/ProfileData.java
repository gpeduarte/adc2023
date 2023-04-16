package pt.unl.fct.di.apdc.webapp.util;

public class ProfileData {
	
	public AuthToken token;
	public String email;
	public String name;
	public String role;
	public boolean isActive;
	public String profileStatus;
	public String phoneNum;
	public String mobilePhone;
	public String occupation;
	public String workingPlace;
	public String address;
	public String city;
	public String cp;
	public String nif;
	
	public ProfileData() {}
	
	public ProfileData(String email, String name, String role, boolean isActive, String profileStatus, String phoneNum, String mobilePhone,
			String occupation, String workingPlace, String address, String city, String cp, String nif, AuthToken token) {

		this.email = email;
		this.name = name;
		this.role = role;
		this.isActive = isActive;
		this.profileStatus = profileStatus;
		this.phoneNum = phoneNum;
		this.mobilePhone = mobilePhone;
		this.occupation = occupation;
		this.workingPlace = workingPlace;
		this.address = address;
		this.city = city;
		this.cp = cp;
		this.nif = nif;
		this.token = token;
		
	}

}
