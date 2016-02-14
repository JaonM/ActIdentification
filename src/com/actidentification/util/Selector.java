package com.actidentification.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author maqiang
 *每个测试用户都有对应的分类器Selector
 *
 */

public class Selector {

	public static final String KNN_DATA="knn_data";
	/**
	 * 用dataMap存储从数据库收集来的数据
	 * 
	 * @param String 表示两种数据类型，一种是用户用来验证身份的活动类型，一种是验证者经过
	 * KNN算法比较后的最近邻类型
	 * 
	 * @param ArrayList<DataPoint> 表示对应数据类型点的集合
	 * 
	 */
	//Map<String,ArrayList<DataPoint>> dataMap=new HashMap<String,ArrayList<DataPoint>>();
	
	//ArrayList<DataPoint> originDataPoints表示训练时的数据
	ArrayList<DataPoint> originDataPoints=null;
	
	//根据KNN比较后的arraylist
	ArrayList<DataPoint> knnDataPoints=new ArrayList<DataPoint>();
	
	//表示测试者
	private String testerName;
	//表示活动类型 
	private String type;
	
	public Selector(String testerName,String type) {
		this.testerName=testerName;
		this.type=type;
	}
	

	//将数据存入到originDataPoints中 
	public void setOriginData(ArrayList<DataPoint> dataPoints) {
		originDataPoints=dataPoints;
	}
	
	//得到originDataPoint
	public ArrayList<DataPoint> getOriginDataPoints() {
		return originDataPoints;
	}
	
	//获得knnDataPoints
	public ArrayList<DataPoint> getKNNDataPoints() {
		return knnDataPoints;
	}
	
	//获取活动类型
	public String getType() {
		return type;
	}
	
	//获取测试者
	public String getTesterName() {
		return testerName;
	}
}
