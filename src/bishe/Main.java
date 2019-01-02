package bishe;

import org.generate.DagFlowGenerater;

public class Main {

	public static void main(String[] args) throws Throwable {
		// TODO Auto-generated method stub
		DagFlowGenerater dagBuilder = new DagFlowGenerater();
      dagBuilder.initDags();
      
      String schedulePath = System.getProperty("user.dir") + "\\DAG_XML\\";
      

//      Semple semple=new Semple();
//      semple.runMakespan(schedulePath, "");
      
//      DCMG dcmg=new DCMG();
//      dcmg.runMakespan(schedulePath, "");
      
//      DCBFOri dcbfOri=new DCBFOri();
//      dcbfOri.runMakespan(schedulePath, "");
      
      Semple semple=new Semple();
      semple.runMakespan(schedulePath, "");
//      DCMGCom dcmgCom=new DCMGCom();
//      dcmgCom.runMakespan(schedulePath, "");
      

	}

}
