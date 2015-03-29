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
	public static final String TAG_ATTENDED = "attended";
	public static final String TAG_TIME_IN = "timeIn";
	public static final String TAG_TIME_OUT = "timeOut";
	public static final String TAG_COMMENT = "comment";
	
	private static final SimpleDateFormat dateFormat =
			new SimpleDateFormat("M/d/yyyy", Locale.US);
	
	public static String formatDate(Date date) {
		return dateFormat.format(date);
	}

	public Student getStudent() {
		return (Student) get(TAG_STUDENT);
	}
	
	public void setStudent(Student student) {
		if(student != null) {
			put(TAG_STUDENT, student);
		}
	}
	
	public Course getCourse() {
		return (Course) get(TAG_COURSE);
	}
	
	public void setCourse(Course course) {
		if(course != null) {
			put(TAG_COURSE, course);
		}
	}
	
	public void setDate(Date date) {
		if(date != null) {
			put(TAG_DATE, formatDate(date));
		}
	}
	
	public void setDate(String dateStr) {
		if(dateStr != null) {
			put(TAG_DATE, dateStr);
		}
	}
	
	public void setDate(int year, int month, int day) {
		Calendar calendar = Calendar.getInstance(Locale.US);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		setDate(calendar.getTime());
	}
	
	public String getDate() {
		return getString(TAG_DATE);
	}

	public Date getTimeIn() {
		return getDate(TAG_TIME_IN);
	}

	public void setTimeIn(Date timeIn) {
		if(timeIn != null) {
			put(TAG_TIME_IN, timeIn);
		}
	}
	
	public void removeTimeIn() {
		remove(TAG_TIME_IN);
	}

	public Date getTimeOut() {
		return getDate(TAG_TIME_OUT);
	}

	public void setTimeOut(Date timeOut) {
		if(timeOut != null) {
			put(TAG_TIME_OUT, timeOut);
		}
	}
	
	public void removeTimeOut() {
		remove(TAG_TIME_OUT);
	}

	public boolean isAttended() {
		return getBoolean(TAG_ATTENDED);
	}

	public void setAttended(boolean attended) {
		put(TAG_ATTENDED, attended);
	}

	public String getComment() {
		return getString(TAG_COMMENT);
	}

	public void setComment(String comment) {
		put(TAG_COMMENT, comment);
	}
}
