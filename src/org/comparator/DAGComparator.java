package org.comparator;
import org.schedule.model.Task;

import java.util.Comparator;

/**
 * 
* @ClassName: DAGComparator
* @Description: Comparator: acording to HEFTRank
* @author YanWenjing
* @date 2018-1-23
 */
public class DAGComparator implements Comparator {

	public int compare(Object arg0,Object arg1){
		Task cloudlet1 = (Task)arg0;
		Task cloudlet2 = (Task)arg1;
		if((cloudlet1.getUpRankValue() - cloudlet2.getUpRankValue()) > 0)
			return -1;
		else if((cloudlet1.getUpRankValue() - cloudlet2.getUpRankValue()) < 0)
			return 1;
		else return 0;
		
	}

}