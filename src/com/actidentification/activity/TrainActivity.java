package com.actidentification.activity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.actidentification.fragment.JumpFragment;
import com.actidentification.fragment.MyFragment;
import com.actidentification.fragment.RunFragment;
import com.actidentification.fragment.WalkFragment;
import com.actidentification.fragment.WaveFragment;
import com.actidentification.util.Util;
import com.example.actidentification.R;

public class TrainActivity extends Activity  implements 
                                            ActionBar.OnNavigationListener{

	ActionBar actionBar;
	String [] actions=new String [] {"跑步","挥手","走路","跳跃"};
	
	//管理Fragment
	FragmentManager fManager;
	FragmentTransaction ft;
	
	RunFragment runFragment;
	WalkFragment walkFragment;
	WaveFragment waveFragment;
	JumpFragment jumpFragment;
	
	//tag标识当前fragment
	String tag=null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.train_layout);
		getOverflowMenu();
		actionBar=getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setDisplayHomeAsUpEnabled(true);
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
				android.R.id.text1,actions);
		actionBar.setListNavigationCallbacks(adapter, this);
        fManager=getFragmentManager();
        runFragment=new RunFragment();
        walkFragment=new WalkFragment();
        waveFragment=new WaveFragment();
        jumpFragment=new JumpFragment();
	}
	
	@Override
	public boolean onNavigationItemSelected(int position,long itemId) {

		switch(position) {
		case 0:
		//	RunFragment rf=new RunFragment();
			tag="Run";
			ft=fManager.beginTransaction();
			ft.replace(R.id.container, runFragment);
			ft.commit();
		    break;
		case 1:
		//	WaveFragment wf=new WaveFragment();
			tag="Wave";
			ft=fManager.beginTransaction();
			ft.replace(R.id.container, waveFragment);
			ft.commit();
			break;
		case 2:
		//	WalkFragment walkFragment=new WalkFragment();
			ft=fManager.beginTransaction();
			tag="Walk";
			ft.replace(R.id.container, walkFragment);
			ft.commit();
			break;
		case 3:
			ft=fManager.beginTransaction();
			tag="Walk";
			ft.replace(R.id.container, jumpFragment);
			ft.commit();
			break;
		}
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater=new MenuInflater(this);
		inflater.inflate(R.menu.train_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.clear:
			clearData();
			break;
		case R.id.export:
			try {
				if(exportData())
					Toast.makeText(this, "导出成功", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show();
			} catch(IOException e) {
				Log.v("IOException", "读写错误");
			}
			break;
		case android.R.id.home:
			Intent intent=new Intent(this,IdentityActivity.class);
			startActivity(intent);
		}
		return true;
	}
	
	private void getOverflowMenu() {
		try {
			ViewConfiguration config=ViewConfiguration.get(this);
			Field menu=ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			menu.setAccessible(true);
			menu.setBoolean(config, false);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//辅助方法清楚数据
	private void clearData() {
		/**
		if(!tag.equals(null)) {
			if(tag.equals("Run")) {
				if(Util.tableIsExist(RunFragment.getDatabase(), runFragment.tester.getText().toString()+"_"+
			                                RunFragment.TYPE)) {
					try {
						Util.dropTable(RunFragment.getDatabase(), runFragment.tester.getText().toString(), RunFragment.TYPE);
						Toast.makeText(this, "清除数据成功", Toast.LENGTH_SHORT).show();
					} catch(SQLiteException e) {
						Log.v("SQLiteException", "删除出错");
					}
				}
				else {
					Toast.makeText(this, "数据不存在", Toast.LENGTH_SHORT).show();
				}
			} else if (tag.equals("Wave")) {
				if(Util.tableIsExist(WaveFragment.getDatabase(), waveFragment.tester.getText().toString()+"_"+
                        WaveFragment.TYPE)) {
					try {
						Util.dropTable(WaveFragment.getDatabase(), waveFragment.tester.getText().toString(), WaveFragment.TYPE);
						Toast.makeText(this, "清除数据成功", Toast.LENGTH_SHORT).show();
						} catch(SQLiteException e) {
							Log.v("SQLiteException", "删除出错");
						}
					}
				else {
					Toast.makeText(this, "数据不存在", Toast.LENGTH_SHORT).show();
					}
			}
			else if(tag.equals("Walk")) {
				if(Util.tableIsExist(WalkFragment.getDatabase(), walkFragment.tester.getText().toString()+"_"+
                        WalkFragment.TYPE)) {
					try {
						Util.dropTable(WalkFragment.getDatabase(), walkFragment.tester.getText().toString(), WalkFragment.TYPE);
						Toast.makeText(this, "清除数据成功", Toast.LENGTH_SHORT).show();
						} catch(SQLiteException e) {
							Log.v("SQLiteException", "删除出错");
						}
					}
				else {
					Toast.makeText(this, "数据不存在", Toast.LENGTH_SHORT).show();
				}
			}
			
		}
		*/
		String [] types=new String [] {RunFragment.TYPE,WaveFragment.TYPE,WalkFragment.TYPE,JumpFragment.TYPE};
		for(String type:types) {
			if(Util.tableIsExist(MyFragment.getDatabase(), MyFragment.tester.getText().toString()+"_"+
                    type)) {
				try {
					Util.dropTable(MyFragment.getDatabase(), MyFragment.tester.getText().toString(), type);
					Toast.makeText(this, type+"清除数据成功", Toast.LENGTH_SHORT).show();
					} catch(SQLiteException e) {
						Log.v("SQLiteException", "删除出错");
						}
				}
			else {
				Toast.makeText(this, type+"数据不存在", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	//辅助方法导出数据库
	private boolean exportData() throws IOException {
		boolean result=false;
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Log.v("here","here1");
			File sdCardDir=Environment.getExternalStorageDirectory();
			String data=sdCardDir.getCanonicalPath()+"/TrainData.txt";
			FileOutputStream fos=new FileOutputStream(data);
			BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(fos));
			String [] types=new String [] {RunFragment.TYPE,WaveFragment.TYPE,WalkFragment.TYPE,JumpFragment.TYPE};
			Log.v("testerNames",MyFragment.testerNames.toString());
			SQLiteDatabase database=null;
			database=MyFragment.getDatabase();
			//更改,从数据库中调出用户名
			ArrayList<String> testerNames=new ArrayList<String >();
			if(Util.tableIsExist(database, "tester")) {
				Cursor cursor=database.rawQuery("select * from tester ", null);
				while(cursor.moveToNext()) {
					testerNames.add(cursor.getString(1));
				}
			}
			for(String testerName:testerNames) {
				Log.v("testerName", testerName);
				for(String type:types) {
					Log.v("type", type);
					//SQLiteDatabase database=null;
					//database=MyFragment.getDatabase();
					String select_sql="select * from "+testerName+"_"+type;
					if(database==null) {
						Log.v("here","here2");
						Toast.makeText(this, "没有相应数据库", Toast.LENGTH_SHORT).show();
						return false;
					}
					if(Util.tableIsExist(database, testerName+"_"+type) ) {
						Log.v("here", "here3");
						result=true;
						Cursor cursor=database.rawQuery(select_sql, null);
						bw.write("\r\n");
						bw.write(testerName+"_"+type+"\r\n");
						while(cursor.moveToNext()) {
							bw.write(cursor.getString(1)+" "+cursor.getString(2)+" "+cursor.getString(3)+"\r\n");
						}	
					}
				}
			}
			bw.close();
		}
		return result;
	}
	
}
