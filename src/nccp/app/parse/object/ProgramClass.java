package nccp.app.parse.object;

import java.util.ArrayList;
import java.util.List;

import com.parse.ParseClassName;

@ParseClassName("Class")
public class ProgramClass extends BaseParseObject {

	public static final String TAG_CLASS_TITLE = "title";
	public static final String TAG_COURSE_LIST = "courseList";
//	public static final String TAG_STUDENT_LIST = "studentList";
	
//	private List<Student> cachedStudents = null;
	
	public String getTitle() {
		return getString(TAG_CLASS_TITLE);
	}
	
	public void setTitle(String className) {
		put(TAG_CLASS_TITLE, className);
	}

	public List<Course> getCourses() {
		List<Course> courses = getList(TAG_COURSE_LIST);
		if(courses == null) {
			courses = new ArrayList<Course>();
			setCourses(courses);
		}
		return courses;
	}

	public void setCourses(List<Course> courses) {
		put(TAG_COURSE_LIST, courses);
	}
	
	public void addCourse(Course course) {
		List<Course> courses = getCourses();
		courses.add(course);
	}

//	public List<Student> getCachedStudents() {
//		return cachedStudents;
//	}
//
//	public void setCachedStudents(List<Student> cachedStudents) {
//		this.cachedStudents = cachedStudents;
//	}

//	public List<Student> getStudents() {
//		List<Student> students = getList(TAG_STUDENT_LIST);
//		if(students == null) {
//			students = new ArrayList<Student>();
//			setStudents(students);
//		}
//		return students;
//	}
//
//	public void setStudents(List<Student> students) {
//		put(TAG_STUDENT_LIST, students);
//	}
//	
//	public void addStudent(Student student) {
//		List<Student> students = getStudents();
//		students.add(student);
//	}
}
