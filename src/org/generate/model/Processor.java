package org.generate.model;

import org.generate.util.RandomParametersUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @ClassName: Processor  
 * @Description: 处理器模型
 * @author Wengie Yan 
 * @date 2019年1月18日  
 *
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
