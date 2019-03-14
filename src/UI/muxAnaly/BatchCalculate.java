package UI.muxAnaly;

import org.generate.DagFlowGenerater;
import org.generate.util.CommonParametersUtil;
import org.schedule.algorithm.Makespan;
import org.temp.*;

import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * 
 * @ClassName: BatchCalculate
 * @Description: 多次分析时用于进行计算与结果统计
 * @author Wengie Yan
 * @date 2018年12月12日
 */
public class BatchCalculate {

	//默认计算轮次为2
	private static int count = 2;
	
	//在多次分析后对每种算法的计算结果统计Map
	public static HashMap<String, String> resultMap=new HashMap<>();


	/**
	 * 
	 * @Description: 构造函数
	 * @param roundTime：用户选定的计算轮次
	 */
	public BatchCalculate(int roundTime) {
		count = roundTime;
	}

	/**
	 * 
	 * @Title: roundAnaly
	 * @Description: 轮次计算并计算结果平均值
	 * @param @throws Throwable
	 * @return void
	 * @throws
	 */
	public static void roundAnaly() throws Throwable {

		String schedulePath = System.getProperty("user.dir") + "\\DAG_XML\\";
		
		System.out.println("计算中CommonParametersUtil.defaultRoundTime="+CommonParametersUtil.defaultRoundTime);

		System.out.println("count="+count);
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



	/**
	 * 
	 * @Title: calculateAvg
	 * @Description: 统计每种算法执行结果的平均值
	 * @param @throws FileNotFoundException
	 * @return void
	 * @throws
	 */
	private static void calculateAvg() throws FileNotFoundException {
		//清空结果Map
		resultMap.clear();
		
		String path1 = "D:\\fifo.txt";
		if(CommonParametersUtil.FIFO==1){
			calcute(path1,"FIFO");
		}else {
			clearFile(path1);
		}
		
		String path2 = "D:\\edf.txt";
		if(CommonParametersUtil.EDF==1){
			
			calcute(path2,"EDF");
		}else {
			clearFile(path2);
		}
		

		String path3 = "D:\\stf.txt";
		if(CommonParametersUtil.STF==1){
			calcute(path3,"STF");
		}else {
			clearFile(path3);
		}
		
		String path4 = "D:\\eftf.txt";
		if(CommonParametersUtil.EFTF==1){
			calcute(path4,"EFTF");
		}else {
			clearFile(path4);
		}
		
		String path5 = "D:\\semple.txt";
		if(CommonParametersUtil.Workflowbased==1){
			calcute(path5,"WorkflowBased");
		}else {
			clearFile(path5);
		}
		
		/**
		 * 添加新算法这里要改
		 */
		
	}

	/**
	 * 
	 * @Title: clearFile
	 * @Description: 如果算法没有参与计算，则要清空计算结果
	 * @param @param path
	 * @return void
	 * @throws
	 */
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



	/**
	 * 
	 * @Title: calcute
	 * @Description: 真正计算的位置
	 * @param path：结果文件所在的位置
	 * @throws FileNotFoundException
	 * @return void
	 */
	private static void calcute(String path,String algoName) throws FileNotFoundException {

		File file = new File(path);
		BufferedReader reader = null;
		int lineCount=0;//统计有多少行，这样就可以获取平均数值（其实传参也可以完成）
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
			
			DecimalFormat df = new DecimalFormat("0.00");
			resultMap.put(algoName, df.format(((double) ER / lineCount)*100) + "," +df.format(((double) UER / lineCount)*100)+ "," + df.format(((double) CR / lineCount)*100)+ "," +Float.parseFloat(df.format( ((double) ET / lineCount))));
			
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
