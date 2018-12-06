package org.schedule.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * 
 * @ClassName: DAGdepend
 * @Description: Dependence between DAG
 * @author YWJ
 * @date 2017-9-10 ÏÂÎç8:19:48
 */
public class DAGDepend {


	private List<Task> DAGList;

	private Map<Integer, Integer> DAGDependMap;

	private Map<String, Double> DAGDependValueMap;

	public ArrayList<DAG> DAGMapList;


	public void setdagmaplist(ArrayList<DAG> list) {
		this.DAGMapList = list;
	}

	public ArrayList getdagmaplist() {
		return DAGMapList;
	}

	/**
	 * 
	 * @Title:
	 * @Description: 
	 * @return boolean
	 * @throws
	 */
	public boolean isDepend(String src, String des) {
		if (DAGDependValueMap.containsKey(src + " " + des)) {
			return true;
		} else
			return false;
	}

	/**
	 * 
	 * @Title: getDependValue
	 * @Description:
	 * @return double
	 * @throws
	 */
	public double getDependValue(int src, int des) {
		return DAGDependValueMap.get(String.valueOf(src) + " "
				+ String.valueOf(des));
	}

	public void setDAGList(List cl) {
		this.DAGList = cl;
	}

	public List getDAGList() {
		return DAGList;
	}

	public void setDAGDependMap(Map cd) {
		this.DAGDependMap = cd;
	}

	public Map getDAGDependMap() {
		return DAGDependMap;
	}

	public void setDAGDependValueMap(Map cdv) {
		this.DAGDependValueMap = cdv;
	}

	public Map getDAGDependValueMap() {
		return DAGDependValueMap;
	}

}
