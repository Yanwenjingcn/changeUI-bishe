package org.generate.model;

import org.generate.DagFlowGenerater;
import org.generate.util.CommonParametersUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 
* @ClassName: RandomDag
* @Description: generate DAG object
* @author YanWenjing
* @date 2018-1-23 ����2:41:20
 */
public class RandomDag {
	public String dagId;//作业编号
	public int dagLevel;//作业层数
	public int dagSize;//作业任务数
	public int levelCount;
	public int submitTime;//作业提交时间
	public int deadlineTime;//作业截止时间
	public int[] levelNodeNumber;
	public List<TaskNode> taskList;
	public List<DagEdge> edgeList;
	public List<TaskNode> lastLevelList;
	public List<TaskNode> newLevelList;
	public List<TaskNode> leafNodeList;
	
	public RandomDag(int dagId) {
		init(dagId);
		TaskNode root = new TaskNode("root_" + dagId, 0, 0, 0);
		taskList.add(root);
		lastLevelList.add(root);
		leafNodeList.add(root);
		submitTime = 0;
	}

	public RandomDag(int dagId, TaskNode root, int lastDagTime) {
		init(dagId);
		taskList.add(root);
		lastLevelList.add(root);
		leafNodeList.add(root);

		submitTime = DagFlowGenerater.randomCreater.randomSubmitTime(lastDagTime,
				root.startTime);
	}

	/**
	 * 
	 * @Title: init
	 * @Description: initial method
	 * @return void
	 * @throws
	 */
	public void init(int dagId) {
		this.dagId = "dag" + dagId;
		taskList = new ArrayList<TaskNode>();
		edgeList = new ArrayList<DagEdge>();
		lastLevelList = new ArrayList<TaskNode>();
		leafNodeList = new ArrayList<TaskNode>();
		newLevelList = new ArrayList<TaskNode>();
		dagSize = DagFlowGenerater.randomCreater
				.randomDagSize(CommonParametersUtil.dagAverageSize);

		dagLevel = DagFlowGenerater.randomCreater.randomLevelNum(dagSize,
				CommonParametersUtil.dagLevelFlag);

		levelNodeNumber = new int[dagLevel];
		DagFlowGenerater.randomCreater.randomLevelSizes(levelNodeNumber, dagSize);

		levelCount = 1;

	}

	/**
	 * 
	 * @Title: addToNewLevel
	 * @Description: add new task to next level
	 * @param taskNode:
	 * @throws
	 */
	public void addToNewLevel(TaskNode taskNode) {
		newLevelList.add(taskNode);
		if (newLevelList.size() == levelNodeNumber[levelCount - 1])
		{
			levelCount++;
			lastLevelList.clear();
			lastLevelList.addAll(newLevelList);
			newLevelList.clear();
		}
	}

	/**
	 * 
	 * @Title: generateNode
	 * @Description: generate new task and add it to task list
	 * @return void
	 * @throws
	 */
	public void generateNode(TaskNode taskNode) {
		taskList.add(taskNode);
	}

	/**
	 * 
	 * @Title: generateEdge
	 * @Description:generate new edge and add it to task list
	 * @return void
	 * @throws
	 */
	public void generateEdge(TaskNode head, TaskNode tail) {
		DagEdge dagEdge = new DagEdge(head, tail);
		edgeList.add(dagEdge);
	}

	/**
	 * 
	 * @Title: containTaskNode
	 * @Description: if a DAG contain this task
	 * @return boolean
	 * @throws
	 */
	public boolean containTaskNode(TaskNode taskNode) {
		return taskList.contains(taskNode);
	}

	/***
	 * 
	 * @Title: computeDeadLine
	 * @Description: calculate a DAG's deadline
	 * @return void
	 * @throws
	 */
	public void computeDeadLine() {
		int proceesorEndTime = CommonParametersUtil.timeWindow
				/ CommonParametersUtil.processorNumber;
		deadlineTime = (int) (submitTime + (taskList.get(taskList.size() - 1).endTime - submitTime)
				* CommonParametersUtil.deadLineTimes);
		if (deadlineTime > proceesorEndTime)
			deadlineTime = proceesorEndTime;
	}

}
