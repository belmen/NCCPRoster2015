package nccp.app.data;

import java.util.List;

import nccp.app.parse.object.ProgramClass;
import nccp.app.parse.object.Student;
import android.support.v4.util.LruCache;

public class DataCache {

	private static final int DEFAULT_STUDENTS_CACHE = 20;
//	private static final int DEFAULT_ATTENDANCE_CACHE = 20;
	
	private static LruCache<String, List<Student>> mStudentsCache;
//	private static LruCache<String, List<Attendance>> mAttendanceCache;
	
	static {
		mStudentsCache = new LruCache<String, List<Student>>(DEFAULT_STUDENTS_CACHE);
//		mAttendanceCache = new LruCache<String, List<Attendance>>(DEFAULT_ATTENDANCE_CACHE);
	}
	
	public static List<Student> getStudents(ProgramClass programClass) {
		if(programClass == null) {
			return null;
		}
		return mStudentsCache.get(programClass.getObjectId());
	}
	
	public static void setStudents(ProgramClass programClass, List<Student> students) {
		if(programClass == null) {
			return;
		}
		mStudentsCache.put(programClass.getObjectId(), students);
	}
	
	public static void removeStudents(ProgramClass programClass) {
		if(programClass == null) {
			return;
		}
		mStudentsCache.remove(programClass.getObjectId());
	}
	
//	public static List<Attendance> getAttendance(Course course) {
//		if(course == null) {
//			return null;
//		}
//		return mAttendanceCache.get(course.getObjectId());
//	}
//	
//	public static void setAttendance(Course course, List<Attendance> attendances) {
//		if(course == null) {
//			return;
//		}
//		mAttendanceCache.put(course.getObjectId(), attendances);
//	}
//	
//	public static void removeAttendance(Course course) {
//		if(course == null) {
//			return;
//		}
//		mAttendanceCache.remove(course.getObjectId());
//	}
}
