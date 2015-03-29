package nccp.app.parse.proxy;

import java.io.Serializable;
import java.util.Date;

import nccp.app.parse.object.Attendance;

public class AttendanceProxy implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String objectId;
	private String date;
	private boolean attended;
	private Date timeIn;
	private Date timeOut;
	private String comment;
	
	public static AttendanceProxy fromParseObject(Attendance parseAttendance) {
		if(parseAttendance == null) {
			return null;
		}
		AttendanceProxy proxy = new AttendanceProxy();
		proxy.objectId = parseAttendance.getObjectId();
		proxy.date = parseAttendance.getDate();
		proxy.attended = parseAttendance.isAttended();
		proxy.timeIn = parseAttendance.getTimeIn();
		proxy.timeOut = parseAttendance.getTimeOut();
		proxy.comment = parseAttendance.getComment();
		return proxy;
	}
	
	public static Attendance toParseObject(AttendanceProxy proxy) {
		if(proxy == null) {
			return null;
		}
		Attendance obj = new Attendance();
		obj.setObjectId(proxy.objectId);
		obj.setDate(proxy.date);
		obj.setAttended(proxy.attended);
		obj.setTimeIn(proxy.timeIn);
		obj.setTimeOut(proxy.timeOut);
		obj.setComment(proxy.comment);
		return obj;
	}
}
