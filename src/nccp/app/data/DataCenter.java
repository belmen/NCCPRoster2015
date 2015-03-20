package nccp.app.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nccp.app.parse.object.Program;
import nccp.app.parse.object.Student;
import nccp.app.utils.Logger;
import android.os.AsyncTask;

import com.parse.ParseException;
import com.parse.ParseQuery;

public class DataCenter {

	public static final String TAG = DataCenter.class.getSimpleName();
	
	public interface Callback {
		void onFetched(ParseException e);
	}
	
	private static final int FETCH_PROGRAMS = 1;
	private static final int FETCH_STUDENTS = 2;
	private static final int FETCH_ALL = 0xffff;
	private static final List<Program> DEFAULT_PROGRAM_LIST = new ArrayList<Program>();
	private static final List<Student> DEFAULT_STUDENT_LIST = new ArrayList<Student>();
	
	// Data
	private static List<Program> mPrograms = DEFAULT_PROGRAM_LIST;
	private static List<Student> mStudents = DEFAULT_STUDENT_LIST;
	private static Map<String, Student> mStudentIdMap = new HashMap<String, Student>();

	/**
	 * Get fetched programs
	 * @return
	 */
	public static List<Program> getPrograms() {
		return mPrograms;
	}
	
	/**
	 * Get fetched students
	 * @return
	 */
	public static List<Student> getStudents() {
		return mStudents;
	}
	
	public static void addStudent(Student student) {
		if(!mStudentIdMap.containsKey(student.getObjectId())) {
			mStudents.add(student);
			mStudentIdMap.put(student.getObjectId(), student);
		}
	}
	
	public static void removeStudent(Student student) {
		if(mStudentIdMap.containsKey(student.getObjectId())) {
			mStudents.remove(student);
			mStudentIdMap.remove(student.getObjectId());
		}
	}
	
	/**
	 * Get student by its parse object ID
	 * @param objectId
	 * @return
	 */
	public static Student getStudentByObjectId(String objectId) {
		return mStudentIdMap.get(objectId);
	}
	
	public static void clearData() {
		mPrograms = DEFAULT_PROGRAM_LIST;
		mStudents = DEFAULT_STUDENT_LIST;
	}

	/**
	 * Fetch all types of data
	 * @param callback
	 */
	public static void fetchData(Callback callback) {
		new FetchDataTask(FETCH_ALL, callback).execute();
	}
	
//	public static void fetchPrograms(Callback callback) {
//		new FetchDataTask(FETCH_PROGRAMS, callback).execute();
//	}
	
//	public static void fetchStudents(Callback callback) {
//		new FetchDataTask(FETCH_STUDENTS, callback).execute();
//	}
	
	private static class FetchDataTask extends AsyncTask<Void, Void, ParseException> {

		private int flags;
		private Callback callback;
		
		public FetchDataTask(int flags, Callback callback) {
			this.flags = flags;
			this.callback = callback;
		}

		@Override
		protected ParseException doInBackground(Void... params) {
			if((flags & FETCH_PROGRAMS) != 0) {
				ParseQuery<Program> programQuery = ParseQuery.getQuery(Program.class);
				try {
					mPrograms = programQuery.find();
				} catch (ParseException e) {
					return e;
				}
			}
			if((flags & FETCH_STUDENTS) != 0) {
				ParseQuery<Student> studentQuery = ParseQuery.getQuery(Student.class);
				try {
					mStudents = studentQuery.find();
					mStudentIdMap.clear();
					if(mStudents != null) { // Add them into map
						for(Student student : mStudents) {
							mStudentIdMap.put(student.getObjectId(), student);
						}
					}
				} catch (ParseException e) {
					return e;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(ParseException e) {
			if(e != null) {
				Logger.e(TAG, e.getMessage(), e);
			}
			if(callback != null) {
				callback.onFetched(e);
			}
		}
	}
}
