package nccp.app.bean;

import java.io.Serializable;
import java.util.Locale;

import com.parse.ParseObject;

/**
 * Java object for student
 * @author Belmen
 *
 */
public class Student implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String TAG_OBJECT = "student";
	public static final String TAG_FIRST_NAME = "firstName";
	public static final String TAG_LAST_NAME = "lastName";
	public static final String TAG_ID = "id";
	public static final String TAG_GRADE = "grade";
	public static final String TAG_SITE = "site";
	public static final String TAG_PROGRAM = "program";
	public static final String TAG_ADDRESS = "address";
	public static final String TAG_EMERGENCY_CONTACT_NAME = "emergencyContactName";
	public static final String TAG_EMERGENCY_CONTACT_PHONE = "emergencyContactPhone";
	public static final String TAG_EMERGENCY_CONTACT_RELATIONSHIP = "emergencyContactRelationship";
	
	private String firstName;
	private String lastName;
	private String id;
	private int grade;
	private String site;
	private String program;
	private String address;
	private String emergencyContactName;
	private String emergencyContactPhone;
	private String emergencyContactRelationship;
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getGrade() {
		return grade;
	}
	public void setGrade(int grade) {
		this.grade = grade;
	}
	public String getSite() {
		return site;
	}
	public void setSite(String site) {
		this.site = site;
	}
	public String getProgram() {
		return program;
	}
	public void setProgram(String program) {
		this.program = program;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getEmergencyContactName() {
		return emergencyContactName;
	}
	public void setEmergencyContactName(String emergencyContactName) {
		this.emergencyContactName = emergencyContactName;
	}
	public String getEmergencyContactPhone() {
		return emergencyContactPhone;
	}
	public void setEmergencyContactPhone(String emergencyContactPhone) {
		this.emergencyContactPhone = emergencyContactPhone;
	}
	public String getEmergencyContactRelationship() {
		return emergencyContactRelationship;
	}
	public void setEmergencyContactRelationship(String emergencyContactRelationship) {
		this.emergencyContactRelationship = emergencyContactRelationship;
	}
	
	public String getFirstNameInitial() {
		if(firstName != null && firstName.length() > 0) {
			return firstName.substring(0, 1).toUpperCase(Locale.US);
		} else {
			return "";
		}
	}
	
	public String getLastNameInitial() {
		if(lastName != null && lastName.length() > 0) {
			return lastName.substring(0, 1).toUpperCase(Locale.US);
		} else {
			return "";
		}
	}
	
	public static Student fromParseObject(ParseObject obj) {
		if(obj == null)  {
			return null;
		}
		Student student = new Student();
		student.setFirstName(obj.getString(Student.TAG_FIRST_NAME));
		student.setLastName(obj.getString(Student.TAG_LAST_NAME));
		student.setId(obj.getString(Student.TAG_ID));
		student.setGrade(obj.getInt(Student.TAG_GRADE));
		student.setSite(obj.getString(Student.TAG_SITE));
		student.setProgram(obj.getString(Student.TAG_PROGRAM));
		student.setAddress(obj.getString(Student.TAG_ADDRESS));
		student.setEmergencyContactName(obj.getString(Student.TAG_EMERGENCY_CONTACT_NAME));
		student.setEmergencyContactPhone(obj.getString(Student.TAG_EMERGENCY_CONTACT_PHONE));
		student.setEmergencyContactRelationship(obj.getString(Student.TAG_EMERGENCY_CONTACT_RELATIONSHIP));
		return student;
	}
	
	public static ParseObject toParseObject(Student bean) {
		if(bean == null) {
			return null;
		}
		ParseObject parse = new ParseObject(Student.TAG_OBJECT);
		parse.put(Student.TAG_FIRST_NAME, bean.getFirstName());
		parse.put(Student.TAG_LAST_NAME, bean.getLastName());
		parse.put(Student.TAG_ID, bean.getId());
		parse.put(Student.TAG_GRADE, bean.getGrade());
		parse.put(Student.TAG_SITE, bean.getSite());
		parse.put(Student.TAG_PROGRAM, bean.getProgram());
		parse.put(Student.TAG_ADDRESS, bean.getAddress());
		parse.put(Student.TAG_EMERGENCY_CONTACT_NAME, bean.getEmergencyContactName());
		parse.put(Student.TAG_EMERGENCY_CONTACT_PHONE, bean.getEmergencyContactPhone());
		parse.put(Student.TAG_EMERGENCY_CONTACT_RELATIONSHIP, bean.getEmergencyContactRelationship());
		return parse;
	}
}