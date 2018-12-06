package org.generate.model;

import org.generate.DagFlowGenerater;

/**
 * 
* @ClassName: DagEdge
* @Description: DAG's edge
* @author YanWenjing
* @date 2018-1-23 ÏÂÎç2:40:18
 */
public class DagEdge {

	public TaskNode head;
	
	public TaskNode tail;
	
	public int transferData;
	

	
	public DagEdge(TaskNode head, TaskNode tail) {
		this.head = head;
		this.tail = tail;
		
		if(head.getProcessorId() == tail.getProcessorId()) 
			this.transferData = DagFlowGenerater.randomCreater.randomTranferData((head.taskLength+tail.taskLength)/2);
		else 
		{
			if(tail.startTime!=100*10000)
				this.transferData = DagFlowGenerater.randomCreater.randomTranferData(tail.startTime - head.endTime);
			else
				this.transferData = 0;
		}
		}
	
	public DagEdge(TaskNode head, TaskNode tail , int transferData) {
		this.head = head;
		this.tail = tail;
		this.transferData = 0;	
	}
}
