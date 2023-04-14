package pt.unl.fct.di.apdc.webapp.util;

public class ProfileData {
	
	public String updater;
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
	
	public ProfileData(String updater, String profileStatus, String phoneNum, String mobilePhone, String occupation, String workingPlace, String address, String city, String cp, String nif) {
		
		this.updater = updater;
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

}
