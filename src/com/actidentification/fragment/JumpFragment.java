package com.actidentification.fragment;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actidentification.util.DataPoint;
import com.actidentification.util.Util;
import com.example.actidentification.R;

/**
 * 
 * @author maqiang
 *测试三组数据，然后将平均值存入数据库当中
 *提高准确性
 */
public class JumpFragment extends MyFragment {

	public static final String TYPE="Jump";
	
	//计时器
	Timer timer;
	Timer mTimer;
	
	int time=30;
	//打开计时器标志
	boolean startTime=false;
	
	//是否完成三组测试
	boolean isFinish=false;
	
	//各组是否测试完成
	boolean isFinish_1=false;
	boolean isFinish_2=false;
	boolean isFinish_3=false;
	
	//返回标签
	boolean isReturn=false;
	
	//测试组号
	int n=1;
	
	//时间handler
	Handler tHandler;
	//数据handler
	Handler mHandler;
	
	//第一组
	ArrayList<DataPoint> points_1=new ArrayList<DataPoint>();
	//第二组
	ArrayList<DataPoint> points_2=new ArrayList<DataPoint>();
	//第三组
	ArrayList<DataPoint> points_3=new ArrayList<DataPoint>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final LinearLayout ll=(LinearLayout)getActivity().getLayoutInflater().inflate(R.layout.fragment_layout, null);
		ll.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view,MotionEvent event) {
				ll.setFocusable(true);
				ll.setFocusableInTouchMode(true);
				ll.requestFocus();
				return false;
			}
		});
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup viewGroup,Bundle savedInstanceState) {
		View view=super.onCreateView(inflater, viewGroup, savedInstanceState);
		super.notice.setText("  点击开始按钮开始测试,测试分三组");
		super.start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(tester.getText().toString().equals("")) {
					notice.setText("用户名不能为空");
					return;
				}
				if(Util.tableIsExist(db, "tester")) {
					Cursor cursor=db.rawQuery("select * from tester", null);
					boolean isSame=false;
					while(cursor.moveToNext()) {
						String name=cursor.getString(1);
						if(name.equals(tester.getText().toString()))
							isSame=true;
					}
					if(!isSame) {
						try {
							db.execSQL("insert into tester values(null,?)",new String [] {tester.getText().toString()});
						} catch(SQLiteException e) {
							Log.v("SQLiteException", "插入出错");
						}
					}
				} else {
					Toast.makeText(getActivity(), "没有建立用户名数据库,无法继续验证", Toast.LENGTH_SHORT).show();
					return;
				}
				mTimer=new Timer();
				if(Util.tableIsExist(db, tester.getText().toString()+"_"+TYPE)) {
					new AlertDialog.Builder(getActivity())
					.setTitle("已完成测试")
					.setMessage("你已经进行过测试,点击是重新开始测试(会覆盖已有数据),点否返回")
					.setPositiveButton("是", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,int which) {
							try {
								Util.dropTable(db, tester.getText().toString(), TYPE);
							} catch(SQLiteException e) {
								Log.v("SQLiteException", "删除数据出错");
							}
							n=1;
							isFinish=false;
							start.setClickable(false);
							tester.setFocusable(false);
							notice.setText("开始第"+n+"测试");
							Toast.makeText(getActivity(), "请做动作", Toast.LENGTH_SHORT).show();
							sm.registerListener(JumpFragment.this,sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME);
							mTimer.schedule(new TimerTask() {
								@Override
								public void run() {
									mHandler.sendEmptyMessage(0x123);
								}
							}, 2000,50);
						}
						
					})
					.setNegativeButton("否",new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,int which) {
							isReturn=true;
							return;
						}
					})
					.show();
				} else {
					if(!isFinish) {
						start.setClickable(false);
						tester.setFocusable(false);
						notice.setText("开始第"+n+"组测试");
						Toast.makeText(getActivity(), "请做动作", Toast.LENGTH_SHORT).show();
						sm.registerListener(JumpFragment.this,sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME);
						mTimer.schedule(new TimerTask() {
							@Override
							public void run() {
								mHandler.sendEmptyMessage(0x123);
							}
						}, 2000,50);
					} 
				}		 			
			}
		});
		
		//为停止训练按钮绑定事件
		super.stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(tester.getText().toString().equals(""))
					return;
					new AlertDialog.Builder(getActivity())
					.setTitle("是否停止训练")
					.setMessage("点击是停止训练,所有 目前缓存的数据都会清空,点击否返回")
					.setPositiveButton("是", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,int which) {
							n=1;
							if(Util.tableIsExist(db, tester.getText().toString()+"_"+TYPE))
								Util.dropTable(db, tester.getText().toString(), TYPE);
							try {
								db.execSQL("delete from tester where testerName="+"'"+tester.getText().toString()+"'");
							} catch(SQLiteException e) {
								Log.v("SQLiteException", "删除用户名出错");
							}
							points_1.clear();
							points_2.clear();
							points_3.clear();
							tester.setFocusable(true);
							start.setClickable(true);
							sm.unregisterListener(JumpFragment.this);
							if(mTimer!=null)
								mTimer.cancel();
							if(timer!=null)
								timer.cancel();
							time=30;
							isFinish=false;
							Toast.makeText(getActivity(), "已清除", Toast.LENGTH_SHORT).show();
						}
					})
					.setNegativeButton("否",null)
					.show();
			}
		});
		
		tHandler=new Handler() {
			@Override
			public void handleMessage(Message msg) {
				notice.setText("剩余时间: "+time);
				if(time<=0) {
					time=30;
					timer.cancel();
					start.setClickable(true);
				    notice.setText("进行第"+n+"组测试");
				}
			
			}		
		};
		
		mHandler=new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what==0x123) {
					DataPoint point=new DataPoint(x,y,z);
					switch(n) {
					case 1:
						result.setText("x="+x+" y="+y+" z="+z);
						points_1.add(point);
						if(points_1.size()>=200) {
							sm.unregisterListener(JumpFragment.this);
							mTimer.cancel();
							startTime=true;
							n=2;
							startTimer();
							Toast.makeText(getActivity(), "你已经完成第一组测试,待时间到了之后,点击按钮开始第二组测试" ,Toast.LENGTH_SHORT).show();
							vibrator.vibrate(1000);
							isFinish_1=true;
						}
						break;
					case 2:
						result.setText("x="+x+" y="+y+" z="+z);
						points_2.add(point);
						if(points_2.size()>=200) {
							sm.unregisterListener(JumpFragment.this);
							mTimer.cancel();
							startTime=true;
							n=3;
							startTimer();
							Toast.makeText(getActivity(), "你已经完成第二组测试,待时间到了之后,点击按钮开始第三组测试" ,Toast.LENGTH_SHORT).show();
							vibrator.vibrate(1000);
							isFinish_2=true;
						}
						break;
					case 3:
						result.setText("x="+x+" y="+y+" z="+z);
						points_3.add(point);
						if(points_3.size()>=200) {
							sm.unregisterListener(JumpFragment.this);
							mTimer.cancel();
							startTime=true;
							n=1;
							notice.setText("测试完成");
							Toast.makeText(getActivity(), "你已经完成第三组测试,可以开始验证" ,Toast.LENGTH_SHORT).show();
							vibrator.vibrate(1000);
							isFinish_3=true;
							start.setClickable(true);
							if(isFinish_1&&isFinish_2&&isFinish_3) {
								if(!Util.tableIsExist(db, tester.getText().toString()+"_"+TYPE)) {
									try {
										Util.createTable(db, tester.getText().toString(), TYPE);
									} catch(SQLiteException e) {
										Log.v("SQLiteException", "创建表出错");
									}
								} 
								insertAverData();
								isFinish=true;
								tester.setFocusable(true);
								points_1.clear();
								points_2.clear();
								points_3.clear();
							}
					    }
						break;
						}
					}
				}
			};
			return view;
		}
	
	//辅助方法打开计时器
	private void startTimer() {
		if(startTime==true) {
			timer=new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					time--;
					tHandler.sendEmptyMessage(0x124);
				}
			},0,1000);
		}
	}
	
	//辅助方法求平均值插入数据库中
	private void insertAverData() {
		if(points_1.size()==200&&points_2.size()==200&&points_3.size()==200) {
			for(int i=0;i<points_1.size();i++) {
				try {
					Util.insertData(db, tester.getText().toString(), TYPE, (points_1.get(i).getX()+points_2.get(i).getX()+points_3.get(i).getX())/3
							, (points_1.get(i).getY()+points_2.get(i).getY()+points_3.get(i).getY())/3, 
							(points_1.get(i).getZ()+points_2.get(i).getZ()+points_3.get(i).getZ())/3);
				} catch(SQLiteException e) {
					Log.v("SQLiteException", "插入数据出错");
				}
			}
			Log.v("points_1 size()",points_1.size()+"");
			Log.v("points_2 size()",points_2.size()+"");
			Log.v("points_3 size()",points_3.size()+"");
			Log.v("points_1",points_1.toString());
			Log.v("points_2",points_2.toString());
			Log.v("points_3",points_3.toString());
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(timer!=null)
			timer.cancel();
		if(mTimer!=null)
			mTimer.cancel();
		points_1.clear();
		points_2.clear();
		points_3.clear();
	}
}
