package nccp.app.ui;

import java.util.List;

import nccp.app.R;
import nccp.app.adapter.DatabaseAdapter;
import nccp.app.parse.ParseAppManager;
import nccp.app.utils.Const;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ChooseDBActivity extends ActionBarActivity {

	public static final String TAG = ChooseDBActivity.class.getSimpleName();

	private ListView mLvDatabase;
	private DatabaseAdapter mAdapter;
	private List<String> mDatabases = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_db_activity);
		
		initToolbar();
		initViews();
		
		showDatabase();
	}

	private void initToolbar() {
		Toolbar tb = (Toolbar) findViewById(R.id.choose_db_toolbar);
		setSupportActionBar(tb);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private void initViews() {
		mLvDatabase = (ListView) findViewById(R.id.choose_db_list);
		mLvDatabase.setOnItemClickListener(onDatabaseClickListener);
		mAdapter = new DatabaseAdapter(ChooseDBActivity.this);
		mLvDatabase.setAdapter(mAdapter);
	}

	private void showDatabase() {
		mDatabases = ParseAppManager.getAppList();
		mAdapter.setData(mDatabases);
		mAdapter.notifyDataSetChanged();
	}
	
	private OnItemClickListener onDatabaseClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			String database = (String) parent.getItemAtPosition(position);
			Intent intent = new Intent();
			intent.putExtra(Const.EXTRA_DATABASE, database);
			setResult(RESULT_OK, intent);
			finish();
		}
	};
}
