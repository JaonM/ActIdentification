package com.actidentification.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author maqiang
 *ÿ�������û����ж�Ӧ�ķ�����Selector
 *
 */

public class Selector {

	public static final String KNN_DATA="knn_data";
	/**
	 * ��dataMap�洢�����ݿ��ռ���������
	 * 
	 * @param String ��ʾ�����������ͣ�һ�����û�������֤��ݵĻ���ͣ�һ������֤�߾���
	 * KNN�㷨�ȽϺ�����������
	 * 
	 * @param ArrayList<DataPoint> ��ʾ��Ӧ�������͵�ļ���
	 * 
	 */
	//Map<String,ArrayList<DataPoint>> dataMap=new HashMap<String,ArrayList<DataPoint>>();
	
	//ArrayList<DataPoint> originDataPoints��ʾѵ��ʱ������
	ArrayList<DataPoint> originDataPoints=null;
	
	//����KNN�ȽϺ��arraylist
	ArrayList<DataPoint> knnDataPoints=new ArrayList<DataPoint>();
	
	//��ʾ������
	private String testerName;
	//��ʾ����� 
	private String type;
	
	public Selector(String testerName,String type) {
		this.testerName=testerName;
		this.type=type;
	}
	

	//�����ݴ��뵽originDataPoints�� 
	public void setOriginData(ArrayList<DataPoint> dataPoints) {
		originDataPoints=dataPoints;
	}
	
	//�õ�originDataPoint
	public ArrayList<DataPoint> getOriginDataPoints() {
		return originDataPoints;
	}
	
	//���knnDataPoints
	public ArrayList<DataPoint> getKNNDataPoints() {
		return knnDataPoints;
	}
	
	//��ȡ�����
	public String getType() {
		return type;
	}
	
	//��ȡ������
	public String getTesterName() {
		return testerName;
	}
}
