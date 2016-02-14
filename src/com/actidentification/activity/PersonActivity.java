package com.actidentification.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.actidentification.R;

public class PersonActivity extends Activity {

	TextView text;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.person_info);
		text=(TextView)findViewById(R.id.welcome);
		String user=this.getIntent().getStringExtra("user");
		text.setText("ª∂”≠ªÿ¿¥  "+user);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case android.R.id.home:
			Intent intent=new Intent(this,LoginActivity.class);
			startActivity(intent);
		}
		return true;
	}
}
