package org.generate.util;

import org.generate.model.TaskNode;

import java.util.List;

/**
 * 
 * @ClassName: RandomCreater
 * @Description: random generator of DAG's parameters
 * @author YanWenjing
 * @date 2018-1-15 ÏÂÎç1:44:43
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
	public void randomCreateNodes(int id, List<TaskNode> nodeList,
			int capacity, int endTime) {
		int capacityLength = (int) (capacity * endTime);
		int nodeNum = 0;
		int totalLength = 0;

		while (totalLength < capacityLength) {
			nodeNum++;

			int taskLength = random(
					(int) (CommonParametersUtil.taskAverageLength * (1 - taskLengthRate)),
					(int) (CommonParametersUtil.taskAverageLength * (1 + taskLengthRate)));

			TaskNode taskNode;
			if (taskLength + totalLength > capacityLength)
				taskLength = capacityLength - totalLength;
			taskNode = new TaskNode(Integer.toString(id) + "_"
					+ Integer.toString(nodeNum), taskLength,
					(totalLength / capacity), (taskLength + totalLength)
							/ capacity);
			nodeList.add(taskNode);
			totalLength += taskLength;
		}
	}

	/**
	 * 
	 * @Title: randomSubmitTime
	 * @Description: generate submit time randomly
	 * @param lastDagtime
	 * @param startTime
	 * @return:
	 * @throws
	 */
	public int randomSubmitTime(int lastDagtime, int startTime) {
		return random(lastDagtime, startTime);
	}

	/**
	 * 
	 * @Title: randomTranferData
	 * @Description: transfer data
	 * @param maxlength
	 * @return:
	 * @throws
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
	 * @Description: generate task number randomly
	 * @param dagAverageSize
	 * @return:
	 * @throws
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
	 * @Description: generate task number for every level randomly
	 * @return void
	 * @throws
	 */
	public void randomLevelSizes(int[] dagLevel, int nodeNumber) {
		for (int j = 0; j < dagLevel.length; j++)
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
