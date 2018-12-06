package UI.muxAnaly;

import org.generate.DagFlowGenerater;
import org.generate.util.CommonParametersUtil;
import org.schedule.algorithm.Makespan;
import org.temp.*;

import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;

public class BatchCalculate {

	private static int count = 10;
	
	public static HashMap<String, String> resultMap=new HashMap<>();

	public BatchCalculate(int roundTime) {
		count = roundTime;
	}
	
	

	public static void roundAnaly() throws Throwable {

		String schedulePath = System.getProperty("user.dir") + "\\DAG_XML\\";

		//1、循环计算
		for (int i = 0; i < count; i++) {

			DagFlowGenerater dagBuilder = new DagFlowGenerater();
			dagBuilder.initDags();
			//执行普遍的算法
			Makespan makespan=new Makespan();
			makespan.runMakespan_xml();
			
			if(CommonParametersUtil.Workflowbased==1){
				Semple semple=new Semple();
				semple.runMakespan(schedulePath, "E:\\DagCasesResult\\");
			}
		}

		//2、计算均值
		calculateAvg();
	}



	private static void calculateAvg() throws FileNotFoundException {
		resultMap.clear();
		
		String path1 = "D:\\fifo.txt";
		if(CommonParametersUtil.FIFO==1){
			calcute(path1, count);
		}else {
			clearFile(path1);
		}
		
		String path2 = "D:\\edf.txt";
		if(CommonParametersUtil.EDF==1){
			
			calcute(path2, count);
		}else {
			clearFile(path2);
		}
		

		String path3 = "D:\\stf.txt";
		if(CommonParametersUtil.STF==1){
			calcute(path3, count);
		}else {
			clearFile(path3);
		}
		
		String path4 = "D:\\eftf.txt";
		if(CommonParametersUtil.EFTF==1){
			calcute(path4, count);
		}else {
			clearFile(path4);
		}
		
		String path5 = "D:\\semple.txt";
		if(CommonParametersUtil.Workflowbased==1){
			calcute(path5, count);
		}else {
			clearFile(path5);
		}
		
	}

	private static void clearFile(String path) {
		File f = new File(path);
		FileWriter fw;
		try {
			fw = new FileWriter(f);
			fw.write("");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private static void calcute(String path, int count) throws FileNotFoundException {

		File file = new File(path);
		BufferedReader reader = null;
		int lineCount=0;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			double ER = 0;
			double CR = 0;
			double UER=0;
			double ET=0;//执行时长
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				System.out.println(file.getName()+"\t"+tempString);
				String[] split = tempString.split("\t");
				ER += Double.valueOf(split[0]);
				UER += Double.valueOf(split[1]);
				CR += Double.valueOf(split[2]);
				ET += Double.valueOf(split[3]);
				lineCount++;
			}
			reader.close();

			File f = new File(path);
			FileWriter fw = new FileWriter(f);
			fw.write("");
			fw.close();
			
			DecimalFormat df = new DecimalFormat("0.0000");
			resultMap.put(file.getName(), df.format(((double) ER / lineCount)) + "," +df.format(((double) UER / lineCount))+ "," + df.format(((double) CR / lineCount))+ "," +df.format( ((double) ET / lineCount)));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
	}

	public HashMap<String, String> getResult() {
		System.out.println("resultMap.size()"+resultMap.size());
		return resultMap;
	}
}
