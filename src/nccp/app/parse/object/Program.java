package nccp.app.parse.object;

import java.util.ArrayList;
import java.util.List;

import com.parse.ParseClassName;

@ParseClassName("Program")
public class Program extends BaseParseObject {

	public static final String TAG_NAME = "programName";
	public static final String TAG_DESCRIPTION = "description";
	public static final String TAG_CLASS_LIST = "classList";
	
//	private String programName;
//	private String description;
//	private List<ProgramClass> classes = new ArrayList<ProgramClass>();
	
	
	public String getProgramName() {
		return getString(TAG_NAME);
	}
	
	public void setProgramName(String programName) {
		put(TAG_NAME, programName);
	}
	
	public String getDescription() {
		return getString(TAG_DESCRIPTION);
	}
	
	public void setDescription(String description) {
		put(TAG_DESCRIPTION, description);
	}
	
	public List<ProgramClass> getClasses() {
		List<ProgramClass> classes = getList(TAG_CLASS_LIST);
		if(classes == null) {
			classes = new ArrayList<ProgramClass>();
			setClasses(classes);
		}
		return classes;
	}
	
	public void setClasses(List<ProgramClass> classes) {
		put(TAG_CLASS_LIST, classes);
	}
	
	public void addClass(ProgramClass c) {
		List<ProgramClass> classes = getClasses();
		classes.add(c);
	}
}
