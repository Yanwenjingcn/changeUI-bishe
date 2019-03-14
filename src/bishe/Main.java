package bishe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.generate.DagFlowGenerater;
import org.schedule.algorithm.DCBF_ID;
import org.schedule.model.DAG;
import org.temp.Semple;

public class Main {

	public static void main(String[] args) throws Throwable {
		// TODO Auto-generated method stub
//		DagFlowGenerater dagBuilder = new DagFlowGenerater();
//		dagBuilder.initDags();  
		String schedulePath = System.getProperty("user.dir") + "\\DAG_XML\\";
		DCBF_ID d = new DCBF_ID();
		d.runMakespan(schedulePath, "");
//      
//      
// 
//      WB wb=new WB();
//      wb.runMakespan(schedulePath, "");
//      WB_CP wb_CP=new WB_CP();
//      wb_CP.runMakespan(schedulePath, "");
//      
//      wbMinTaskNums wb_SF=new wbMinTaskNums();
//      wb_SF.runMakespan(schedulePath, "");
//      
//      wbMaxTaskNums wb_LF=new wbMaxTaskNums();
//      wb_LF.runMakespan(schedulePath, "");
//		
//		
//	
//		
//		ArrayList<DAG> temp =new ArrayList<DAG>();
//
//		DAG dag1=new DAG();
//		dag1.setDAGdeadline(10);
//		temp.add(dag1);
//		
//		DAG dag2=new DAG();
//		dag2.setDAGdeadline(20);
//		
//		DAG dag3=new DAG();
//		dag3.setDAGdeadline(30);
//		
//		DAG dag4=new DAG();
//		dag4.setDAGdeadline(40);
//		temp.add(dag1);
//		temp.add(dag2);
//		temp.add(dag3);
//		temp.add(dag4);
//		
//        //********比较方式：
//        Collections.sort(temp, new Comparator<DAG>() {
//
//            public int compare(DAG o1, DAG o2) {
//                if (o1.getDAGdeadline() < o2.getDAGdeadline()) {
//                    return 1;//返回1，为从大到小；返回-1为从小到大
//                } else if (o1.getDAGdeadline() > o2.getDAGdeadline()) {
//                    return -1;
//                }
//                return 0;
//            }
//
//        });
//        
//        for(DAG dag:temp) {
//        	System.out.println(dag.getDAGdeadline());
//        }

	}

}
