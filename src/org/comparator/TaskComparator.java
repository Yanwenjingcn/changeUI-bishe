package org.comparator;

import org.schedule.model.Task;

import java.util.Comparator;

/**
 * 
* @ClassName: TaskComparator
* @Description: Comparator: acording to HEFTRank
* @author YanWenjing
* @date 2018-1-23 ÏÂÎç2:39:27
 */
public class TaskComparator implements Comparator {
	public int compare(Object arg0,Object arg1){
		Task task1 = (Task)arg0;
		Task task2 = (Task)arg1;
		if((task1.getUpRankValue() - task2.getUpRankValue()) > 0)
			return -1;
		else if((task1.getUpRankValue() - task2.getUpRankValue()) < 0)
			return 1;
		else return 0;
		
	}

}