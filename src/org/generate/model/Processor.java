package org.generate.model;

import org.generate.util.RandomParametersUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 
* @ClassName: Processor
* @Description: processor
* @author YanWenjing
* @date 2018-1-23 ÏÂÎç2:40:53
 */
public class Processor {
	public String processorId;
	public static int capacity;
	public int startWorkTime;
	public int endWorkTime;
	public List<TaskNode> nodeList;

	//=================== constructor ========================
	
	public Processor(int id, int endTime) {
		processorId = "processor" + id;
		capacity = 1;
		startWorkTime = 0;
		endWorkTime = endTime;
		nodeList = new ArrayList<TaskNode>();
		RandomParametersUtil randomCreater = new RandomParametersUtil();

		randomCreater.randomCreateNodes(id, nodeList, capacity, endWorkTime);
	}

}
