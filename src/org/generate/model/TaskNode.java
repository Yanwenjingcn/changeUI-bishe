package org.generate.model;

/**
 * 
* @ClassName: TaskNode
* @Description: DAG'S task
* @author YanWenjing
* @date 2018-1-23 ÏÂÎç2:41:43
 */
public class TaskNode {

	public String nodeId;

	public int taskLength;

	public int startTime;

	public int endTime;
	
	
	public TaskNode(String nodeId, int taskLength, int startTime,int endTime) {
		this.nodeId = nodeId;
		this.taskLength = taskLength;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	/**
	 * 
	* @Title: getProcessorId
	* @Description: return processor's id
	* @return:
	* @throws
	 */
	public int getProcessorId(){
		String[] processorId = nodeId.split("_");

		if(!processorId[0].equals("root")&&!processorId[0].equals("foot"))
		  return Integer.parseInt(processorId[0]);	
		else
		  return 0;
	}
}

