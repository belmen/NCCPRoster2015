package nccp.app.parse.object;

import java.util.Locale;

import com.parse.ParseClassName;

@ParseClassName("student")
public class Student extends BaseParseObject {

	public static final String TAG_STUDENT_ID = "studentId";
	public static final String TAG_FIRST_NAME = "firstName";
	public static final String TAG_LAST_NAME = "lastName";
	public static final String TAG_GRADE_LEVEL = "gradeLevel";
	
	public String getFirstName() {
		String firstName = getString(TAG_FIRST_NAME);
		if(firstName == null) {
			firstName = "";
		}
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		put(TAG_FIRST_NAME, firstName);
	}
	
	public String getLastName() {
		String lastName = getString(TAG_LAST_NAME);
		if(lastName == null) {
			lastName = "";
		}
		return lastName;
	}
	public void setLastName(String lastName) {
		put(TAG_LAST_NAME, lastName);
	}
	public String getStudentId() {
		return getString(TAG_STUDENT_ID);
	}
	public void setStudentId(String studentId) {
		put(TAG_STUDENT_ID, studentId);
	}
	public int getGradeLevel() {
		return getInt(TAG_GRADE_LEVEL);
	}
	public void setGradeLevel(int gradeLevel) {
		put(TAG_GRADE_LEVEL, gradeLevel);
	}
	
	public String getFullName() {
		String firstName = getFirstName();
		String lastName = getLastName();
		return (firstName + " " + lastName).trim();
	}
	
	public String getFirstNameInitial() {
		String firstName = getFirstName();
		if(firstName != null && firstName.length() > 0) {
			return firstName.substring(0, 1).toUpperCase(Locale.US);
		} else {
			return "";
		}
	}
	
	public String getLastNameInitial() {
		String lastName = getLastName();
		if(lastName != null && lastName.length() > 0) {
			return lastName.substring(0, 1).toUpperCase(Locale.US);
		} else {
			return "";
		}
	}
}
