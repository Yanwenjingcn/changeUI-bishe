package org.comparator;
import org.schedule.model.Task;

import java.util.Comparator;

/**
 * 
* @ClassName: DAGOriComparator
* @Description: Comparator: acording to task's id
* @author YanWenjing
* @date 2018-1-23
 */
public class DAGOriComparator implements Comparator {
	public int compare(Object arg0,Object arg1){
		Task cloudlet1 = (Task)arg0;
		Task cloudlet2 = (Task)arg1;
		if((cloudlet1.getid() - cloudlet2.getid()) > 0)
			return 1;
		else if((cloudlet1.getid() - cloudlet2.getid()) < 0)
			return -1;
		else return 0;
		
	}

}
