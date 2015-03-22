package nccp.app.parse.object;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.parse.ParseClassName;

@ParseClassName("Attendance")
public class Attendance extends BaseParseObject {

	public static final String TAG_STUDENT = "student";
	public static final String TAG_COURSE = "course";
	public static final String TAG_DATE = "date";
//	public static final String TAG_ATTENDED = "attended";
	public static final String TAG_TIME_IN = "timeIn";
	public static final String TAG_TIME_OUT = "timeOut";
	
	private static final SimpleDateFormat dateFormat =
			new SimpleDateFormat("M/d/yyyy", Locale.US);
	
	public static String formatDate(Date date) {
		return dateFormat.format(date);
	}

	public Student getStudent() {
		return (Student) get(TAG_STUDENT);
	}
	
	public void setStudent(Student student) {
		put(TAG_STUDENT, student);
	}
	
	public Course getCourse() {
		return (Course) get(TAG_COURSE);
	}
	
	public void setCourse(Course course) {
		put(TAG_COURSE, course);
	}
	
	public void setDate(Date date) {
		put(TAG_DATE, formatDate(date));
	}
	
	public void setDate(int year, int month, int day) {
		Calendar calendar = Calendar.getInstance(Locale.US);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		setDate(calendar.getTime());
	}

	public Date getTimeIn() {
		return getDate(TAG_TIME_IN);
	}

	public void setTimeIn(Date timeIn) {
		put(TAG_TIME_IN, timeIn);
	}

	public Date getTimeOut() {
		return getDate(TAG_TIME_OUT);
	}

	public void setTimeOut(Date timeOut) {
		put(TAG_TIME_OUT, timeOut);
	}
}
