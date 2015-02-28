package nccp.app.parse.proxy;

import java.io.Serializable;

import nccp.app.parse.object.Student;

public class StudentProxy implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = StudentProxy.class.getSimpleName();
	
	public String objectId;
	public String firstName;
	public String lastName;
	public String studentId;
	public int gradeLevel;
	
	public static StudentProxy fromParseObject(Student parseStudent) {
		if(parseStudent == null) {
			return null;
		}
		StudentProxy proxy = new StudentProxy();
		proxy.objectId = parseStudent.getObjectId();
		proxy.firstName = parseStudent.getFirstName();
		proxy.lastName = parseStudent.getLastName();
		proxy.studentId = parseStudent.getStudentId();
		proxy.gradeLevel = parseStudent.getGradeLevel();
		return proxy;
	}
	
	public static Student toParseObject(StudentProxy proxy) {
		if(proxy == null) {
			return null;
		}
		Student obj = new Student();
		obj.setObjectId(proxy.objectId);
		obj.setFirstName(proxy.firstName);
		obj.setLastName(proxy.lastName);
		obj.setStudentId(proxy.studentId);
		obj.setGradeLevel(proxy.gradeLevel);
		return obj;
	}
}
