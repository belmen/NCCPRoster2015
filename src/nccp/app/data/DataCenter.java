package nccp.app.data;

import java.util.ArrayList;
import java.util.List;

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
	
	private static final List<Program> DEFAULT_PROGRAM_LIST = new ArrayList<Program>();
	private static final List<Student> DEFAULT_STUDENT_LIST = new ArrayList<Student>();
	
	// Data
	private static List<Program> mPrograms = DEFAULT_PROGRAM_LIST;
	private static List<Student> mStudents = DEFAULT_STUDENT_LIST;

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
	
	public static void clearData() {
		mPrograms = DEFAULT_PROGRAM_LIST;
		mStudents = DEFAULT_STUDENT_LIST;
	}

	/**
	 * Fetch all types of data
	 * @param callback
	 */
	public static void fetchData(Callback callback) {
		new FetchDataTask(callback).execute();
	}
	
	private static class FetchDataTask extends AsyncTask<Void, Void, ParseException> {

		private Callback callback;
		
		public FetchDataTask(Callback callback) {
			this.callback = callback;
		}

		@Override
		protected ParseException doInBackground(Void... params) {
			ParseQuery<Program> programQuery = ParseQuery.getQuery(Program.class);
			try {
				mPrograms = programQuery.find();
			} catch (ParseException e) {
				return e;
			}
			ParseQuery<Student> studentQuery = ParseQuery.getQuery(Student.class);
			try {
				mStudents = studentQuery.find();
			} catch (ParseException e) {
				return e;
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