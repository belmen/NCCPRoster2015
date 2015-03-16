package nccp.app.parse.proxy;

import java.io.Serializable;
import java.util.Date;

import nccp.app.parse.object.Course;

public class CourseProxy implements Serializable {

	public static final String TAG = CourseProxy.class.getSimpleName();
	
	public String objectId;
	public String courseName;
	public long time;
	public int duration;
	
	public static CourseProxy fromParseObject(Course parseCourse) {
		if(parseCourse == null) {
			return null;
		}
		CourseProxy proxy = new CourseProxy();
		proxy.objectId = parseCourse.getObjectId();
		proxy.courseName = parseCourse.getCourseName();
		proxy.time = parseCourse.getTime().getTime();
		proxy.duration = parseCourse.getDuration();
		return proxy;
	}
	
	public static Course toParseObject(CourseProxy proxy) {
		if(proxy == null) {
			return null;
		}
		Course obj = new Course();
		obj.setObjectId(proxy.objectId);
		obj.setCourseName(proxy.courseName);
		obj.setCourseTime(new Date(proxy.time));
		obj.setDuration(proxy.duration);
		return obj;
	}
}
