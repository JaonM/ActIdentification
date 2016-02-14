package com.actidentification.util;

import java.util.ArrayList;
import java.util.Map;

import com.actidentification.activity.IdentityActivity;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class Util {

	public static void createTable(SQLiteDatabase db,String tester,String type) {
		 db.execSQL("create table "+tester+"_"+type+" (_id integer primary key autoincrement,"
		 		+ "xAccelerate,yAccelerate,zAccelerate)");
	}
	
	public static void insertData(SQLiteDatabase db,String tester,String type,
			float xAccelerate,float yAccelerate,float zAccelerate) throws SQLiteException {
		db.execSQL("insert into "+tester+"_"+type+" values(null,?,?,?)"
				,new String [] {xAccelerate+"",yAccelerate+"",zAccelerate+""});
	}
	
	public static void dropTable(SQLiteDatabase db,String tester,String type) throws SQLiteException {
		db.execSQL("drop table "+tester+"_"+type);
		if(Util.tableIsExist(db, "tester")) {
			try {
				db.execSQL("delete from tester where testerName="+"'"+tester+"'");
			} catch(SQLException e) {
				Log.v("sqlitexception", "删除元祖出错");
			}
		}
		Log.v("status","表已删除");
	} 
	
	//判断表是否存在
	public static boolean tableIsExist(SQLiteDatabase db,String tableName) {
		boolean result=false;
		if(tableName.equals(""))
			return false;
		try {
			String sql="select count(*) as c from Sqlite_master  where type ='table' and name ='"+tableName.trim()+"' ";
			Cursor cursor=db.rawQuery(sql, null);
			int count=0;
			if(cursor.moveToNext()) {
				count=cursor.getInt(0);
				if(count>0)
					result=true;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static ArrayList<DataPoint> selectFromDB(SQLiteDatabase db,String tableName) {
		if(!Util.tableIsExist(db, tableName)) 
			return null;
		String select_sql="select * from "+ tableName ;
		ArrayList<DataPoint> points=new ArrayList<DataPoint>();
		Cursor cursor=db.rawQuery(select_sql, null);
		while(cursor.moveToNext()) {
			String x=cursor.getString(1);
			String y=cursor.getString(2);
			String z=cursor.getString(3);
			DataPoint data=new DataPoint(Float.parseFloat(x),Float.parseFloat(y),Float.parseFloat(z));
			points.add(data);
		}
		return points;
	}
	
	//求knn距离
	public static boolean knnDistance(Map<String,ArrayList<Selector>> dataMap,String type,ArrayList<DataPoint> points ) {
		ArrayList<Selector> selectors=dataMap.get(type);
		if(selectors.size()==0)
			return false;
		for(DataPoint point:points) {
			float min=disSelectorToPoint(selectors.get(0),point);
			int index=0;
			for(int i=0;i<selectors.size();i++) {
				float dis=disSelectorToPoint(selectors.get(i),point);
				if(dis<min) {
					min=dis;
					index=i;
				}
			}
              //增加一个阈值使得距离超过这个值的点筛选掉
                     if(min>5.22){
                         IdentityActivity.notTestedSelector.getKNNDataPoints().add(point);
                         continue;
                     }
			selectors.get(index).getKNNDataPoints().add(point);
		}
		return true;
	}
	
	//求点到分类器的最近距离
	public static float disSelectorToPoint(Selector selector,DataPoint point) {
		float minDis=1000000000;
		ArrayList<DataPoint> selectorPoints=selector.getOriginDataPoints();
		for(DataPoint selectorPoint:selectorPoints) {
			float distance=(float) Math.sqrt((point.getX()-selectorPoint.getX())*(point.getX()-selectorPoint.getX())
					+(point.getY()-selectorPoint.getY())*(point.getY()-selectorPoint.getY())+
					(point.getZ()-selectorPoint.getZ())*(point.getZ()-selectorPoint.getZ()));
			if(distance<minDis)
				minDis=distance;
		}
		Log.v("minDistance", minDis+" ");
		return minDis;
	}
}
