package nccp.app.parse.object;

import java.util.ArrayList;
import java.util.List;

import com.parse.ParseClassName;

@ParseClassName("Class")
public class ProgramClass extends BaseParseObject {

	public static final String TAG_CLASS_TITLE = "title";
	public static final String TAG_COURSE_LIST = "courseList";
	public static final String TAG_STUDENT_LIST = "studentList";
	
	public String getTitle() {
		return getString(TAG_CLASS_TITLE);
	}
	
	public void setTitle(String className) {
		put(TAG_CLASS_TITLE, className);
	}

	public List<Course> getCourses() {
		return getList(TAG_COURSE_LIST);
	}

	public void setCourses(List<Course> courses) {
		put(TAG_COURSE_LIST, courses);
	}
	
	public void addCourse(Course course) {
		List<Course> courses = getCourses();
		if(courses == null) {
			courses = new ArrayList<Course>();
			setCourses(courses);
		}
		courses.add(course);
	}

	public List<Student> getStudents() {
		return getList(TAG_STUDENT_LIST);
	}

	public void setStudents(List<Student> students) {
		put(TAG_STUDENT_LIST, students);
	}
	
	public void addStudent(Student student) {
		List<Student> students = getStudents();
		if(students == null) {
			students = new ArrayList<Student>();
			setStudents(students);
		}
		students.add(student);
	}
}
