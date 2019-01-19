package org.generate.util;

import org.generate.model.TaskNode;

import java.util.List;

/**
 * 
 * @ClassName: RandomCreater
 * @Description: random generator of DAG's parameters
 * @author YanWenjing
 * @date 2018-1-15 ����1:44:43
 */

/**
 * 
 * @ClassName: RandomParametersUtil  
 * @Description: 相关参数的随机生成
 * @author Wengie Yan 
 * @date 2019年1月19日  
 *
 */
public class RandomParametersUtil {

	public double taskLengthRate = 0.5;
	public double levelRate = 0.5;

	/**
	 * 
	 * @Title: randomCreateNodes
	 * @Description: TODO
	 * @param id
	 * @param nodeList
	 * @param capacity
	 * @param endTime :
	 * @throws
	 */
	
	/**
	 * 
	 * @Title: randomCreateNodes  
	 * @Description: 将处理器上时间窗口中内容随机切分为多个任务
	 * @param id
	 * @param nodeList
	 * @param capacity
	 * @param endTime
	 * @return void
	 */
	public void randomCreateNodes(int id, List<TaskNode> nodeList,int capacity, int endTime) {
		int capacityLength = (int) (capacity * endTime);
		int nodeNum = 0;
		int totalLength = 0;

		while (totalLength < capacityLength) {
			nodeNum++;

			//任务长度[0.5ATL,1.5ATL]
			int taskLength = random((int) (CommonParametersUtil.taskAverageLength * (1 - taskLengthRate)),(int) (CommonParametersUtil.taskAverageLength * (1 + taskLengthRate)));

			TaskNode taskNode;
			if (taskLength + totalLength > capacityLength)//检查处理器最后一个任务的长度是否会超过时间窗总长度，调整最后一个任务的长度
				taskLength = capacityLength - totalLength;
			taskNode = new TaskNode(Integer.toString(id) + "_"+ Integer.toString(nodeNum), taskLength,(totalLength / capacity), (taskLength + totalLength)/ capacity);
			nodeList.add(taskNode);
			totalLength += taskLength;
		}
	}


	/**
	 * 
	 * @Title: randomSubmitTime  
	 * @Description: 随机生成作业提交时间
	 * @param lastDagtime
	 * @param startTime
	 * @return
	 * @return int
	 */
	public int randomSubmitTime(int lastDagtime, int startTime) {
		return random(lastDagtime, startTime);
	}

	
	/**
	 * 
	 * @Title: randomTranferData  
	 * @Description: 随机生成任务间传输数据量
	 * @param maxlength
	 * @return
	 * @return int
	 */
	public int randomTranferData(int maxlength) {
		if (maxlength == 0)
			return 0;
		else
			return random(1, maxlength);
	}


	/**
	 * 
	 * @Title: randomDagSize  
	 * @Description: 随机生成作业大小，区间[0.5ADS,1.5ADS]
	 * @param dagAverageSize
	 * @return
	 * @return int
	 */
	public int randomDagSize(int dagAverageSize) {
		return random((int) (dagAverageSize * 0.5),
				(int) (dagAverageSize * 1.5));

	}

	/**
	 * 
	 * @Title: randomLevelNum
	 * @Description: generate DAG's level numbers randomly
	 * @return int
	 * @throws
	 */
	
	/**
	 * 
	 * @Title: randomLevelNum  
	 * @Description: 根据作业并行度生成作业应有的层数
	 * @param dagSize
	 * @param levelFlag
	 * @return
	 * @return int
	 */
	public int randomLevelNum(int dagSize, int levelFlag) {
		int sqrt = (int) Math.sqrt(dagSize - 2);
		if (levelFlag == 1)
			return random(1, sqrt);
		else if (levelFlag == 2)
			return random(sqrt, sqrt + 3);
		else if (levelFlag == 3)
			return random(sqrt + 3, dagSize - 2);
		else
			return sqrt;
	}

	
	/**
	 * 
	 * @Title: randomLevelSizes  
	 * @Description: 生成每层应有的任务数
	 * @param dagLevel
	 * @param nodeNumber
	 * @return void
	 */
	public void randomLevelSizes(int[] dagLevel, int nodeNumber) {
		for (int j = 0; j < dagLevel.length; j++)//初始化每层至少有一个任务
			dagLevel[j] = 1;
		
		int i = nodeNumber - dagLevel.length;

		while (i > 0) {
			for (int j = 0; j < dagLevel.length; j++)

				if (random(0, 1) == 1) {
					dagLevel[j]++;
					i--;
					if (i == 0)
						break;
				}
			if (i == 0)
				break;
		}
	}

	/**
	 * 
	 * @Title: random
	 * @Description: generate a random number between min and max
	 * @param min
	 * @param max
	 * @return:
	 * @throws
	 */
	public int random(int min, int max) {
		return (int) (min + Math.random() * (max - min + 1));
	}

}
