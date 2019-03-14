package org.schedule.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @ClassName: DAGMap
 * @Description: DAG
 * @author YanWenjing
 * @date 2018-1-15 ����2:37:23
 */
public class DAG {

	// 本作业是否被跳过
	public boolean fillbackpass = false;
	// 本作业是否安排完毕
	public boolean fillbackdone = false;
	// 作业的编号
	public int DAGId;
	// 锁包含的任务数
	public int tasknumber;
	// 作业的截止时间
	public int DAGdeadline;
	// 作业的提交时间
	public int submittime;
	// 是否被合并
	public boolean isMerge = false;

	public boolean isSingle = false;

	public ArrayList<Task> TaskList;

	public HashMap<Integer, Integer> DAGDependMap;

	public HashMap<String, Double> DAGDependValueMap;

	public ArrayList<Task> orderbystarttime;

	public HashMap<Integer, ArrayList> taskinlevel;

	// 作业的优先级
	public int property = -1;

	public DAG() {
		TaskList = new ArrayList<Task>();
		orderbystarttime = new ArrayList<Task>();
		DAGDependMap = new HashMap<Integer, Integer>();
		DAGDependValueMap = new HashMap<String, Double>();
		taskinlevel = new HashMap<Integer, ArrayList>();
	}

	public boolean isMerge() {
		return isMerge;
	}

	public void setMerge(boolean isMerge) {
		this.isMerge = isMerge;
	}

	public boolean isSingle() {
		return isSingle;
	}

	public void setSingle(boolean isSingle) {
		this.isSingle = isSingle;
	}

	public HashMap<Integer, Integer> getDAGDependMap() {
		return DAGDependMap;
	}

	public void setDAGDependMap(HashMap<Integer, Integer> dAGDependMap) {
		DAGDependMap = dAGDependMap;
	}

	public int getProperty() {
		return property;
	}

	public void setProperty(int property) {
		this.property = property;
	}

	public void setfillbackpass(boolean pass) {
		this.fillbackpass = pass;
	}

	public boolean getfillbackpass() {
		return fillbackpass;
	}

	public void setfillbackdone(boolean done) {
		this.fillbackdone = done;
	}

	public boolean getfillbackdone() {
		return fillbackdone;
	}

	public void settasklist(ArrayList<Task> list) {
		for (int i = 0; i < list.size(); i++)
			this.TaskList.add(list.get(i));
	}

	public ArrayList gettasklist() {
		return TaskList;
	}

	public void setorderbystarttime(ArrayList<Task> list) {
		for (int i = 0; i < list.size(); i++)
			this.orderbystarttime.add(list.get(i));
	}

	public ArrayList getorderbystarttime() {
		return orderbystarttime;
	}

//	public void setdepandmap(HashMap<Integer, Integer> map) {
//		this.DAGDependMap = map;
//	}

	public void setdependvalue(HashMap<String, Double> value) {
		this.DAGDependValueMap = value;
	}

	public HashMap getdependvalue() {
		return DAGDependValueMap;
	}

	public void setDAGId(int id) {
		this.DAGId = id;
	}

	public int getDAGId() {
		return DAGId;
	}

	public void settasknumber(int num) {
		this.tasknumber = num;
	}

	public int gettasknumber() {
		return tasknumber;
	}

	public void setDAGdeadline(int deadline) {
		this.DAGdeadline = deadline;
	}

	public int getDAGdeadline() {
		return DAGdeadline;
	}

	public void setsubmittime(int submit) {
		this.submittime = submit;
	}

	public int getsubmittime() {
		return submittime;
	}

	/**
	 * 
	 * @Title: isDepend @Description:Determine whether there is a dependency between
	 * two tasks @param src @param des @return: @throws
	 */
	public boolean isDepend(String src, String des) {
		if (DAGDependValueMap.containsKey(src + " " + des)) {
			return true;
		} else
			return false;
	}

}
