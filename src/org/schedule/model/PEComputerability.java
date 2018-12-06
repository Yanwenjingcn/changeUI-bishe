package org.schedule.model;

import java.util.Map;

/**
 * 
 * @ClassName: PEComputerability
 * @Description: 
 * @author YanWenjing
 * @date 2018-1-15 обнГ3:18:24
 */
public class PEComputerability {

	private static Map<Integer, int[]> ComputeCostMap;

	private static Map<Integer, Integer> AveComputeCostMap;

	public void setComputeCostMap(Map cch) {
		this.ComputeCostMap = cch;
	}

	public static int getComputeCost(int Id, int peId) {
		return ComputeCostMap.get(Id)[peId];
	}

	public void setAveComputeCostMap(Map acch) {
		this.AveComputeCostMap = acch;
	}

	public static int getAveComputeCost(int Id) {
		return 1;
	}
}
