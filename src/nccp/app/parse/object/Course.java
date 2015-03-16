package nccp.app.parse.object;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.parse.ParseClassName;

@ParseClassName("Course")
public class Course extends BaseParseObject {

	public static final String TAG_COURSE_NAME = "courseName";
	public static final String TAG_COURSE_TIME = "courseTime";
	public static final String TAG_DURATION = "duration";
	
	public String getCourseName() {
		return getString(TAG_COURSE_NAME);
	}
	
	public void setCourseName(String courseName) {
		put(TAG_COURSE_NAME, courseName);
	}
	
	public int getDuration() {
		return getInt(TAG_DURATION);
	}
	
	public void setDuration(int duration) {
		put(TAG_DURATION, duration);
	}
	
	public void setCourseTime(Date time) {
		put(TAG_COURSE_TIME, time);
	}
	
	public void setCourseTime(int dayOfWeek, int hourOfDay, int minute) {
		Calendar c = Calendar.getInstance(Locale.US);
		c.setTime(new Date(0));
		c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		c.set(Calendar.MINUTE, minute);
		put(TAG_COURSE_TIME, c.getTime());
	}
	
	public Date getTime() {
		Date time = getDate(TAG_COURSE_TIME);
		if(time == null) {
			time = new Date(0);
		}
		return time;
	}
	
	public int getDayOfWeek() {
		Calendar c = Calendar.getInstance(Locale.US);
		c.setTime(getTime());
		return c.get(Calendar.DAY_OF_WEEK);
	}
	
	public int getHourOfDay() {
		Calendar c = Calendar.getInstance(Locale.US);
		c.setTime(getTime());
		return c.get(Calendar.HOUR_OF_DAY);
	}
	
	public int getMinute() {
		Calendar c = Calendar.getInstance(Locale.US);
		c.setTime(getTime());
		return c.get(Calendar.MINUTE);
	}
}
