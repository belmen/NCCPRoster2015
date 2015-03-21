package nccp.app.parse.object;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.parse.ParseClassName;

@ParseClassName("Course")
public class Course extends BaseParseObject implements Comparable<Course> {

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
	
	public void setDayOfWeek(int dayOfWeek) {
		Calendar c = Calendar.getInstance(Locale.US);
		c.setTime(getTime());
		c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		put(TAG_COURSE_TIME, c.getTime());
	}
	
	public void setCourseTime(int hourOfDay, int minute) {
		Calendar c = Calendar.getInstance(Locale.US);
		c.setTime(getTime());
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

	@Override
	public String toString() {
		return getCourseName();
	}

	@Override
	public int compareTo(Course another) {
		int dowDiff = getDayOfWeek() - another.getDayOfWeek();
		if(dowDiff != 0) {
			return dowDiff;
		}
		int hodDiff = getHourOfDay() - another.getHourOfDay();
		if(hodDiff != 0) {
			return hodDiff;
		}
		int minuteDiff = getMinute() - another.getMinute();
		if(minuteDiff != 0) {
			return minuteDiff;
		}
		return 0;
	}
}
