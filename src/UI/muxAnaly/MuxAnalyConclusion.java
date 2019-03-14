package UI.muxAnaly;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class MuxAnalyConclusion {
	
	static HashMap<String, Double> ERs=new HashMap<>();
	static HashMap<String, Double> UERs=new HashMap<>();
	static HashMap<String, Double> CRs=new HashMap<>();
	static HashMap<String, Double> ETs=new HashMap<>();
	
	static Table targetTable=null;
	

	public static void setConclusion(Table table, HashMap<String, String> resultMap) {
		targetTable=table;
		
		//解析计算结果
		splitResultMap(resultMap);

		//1 处理器利用率最高
		setHighER();
		//2 处理器利用率最低
		setLowER();
		insertBlankData("", "", "", "");
		
		
		//3 处理器有效利用率最高
		setHighUER();
		//4 处理器有效利用率最低
		setLowUER();
		insertBlankData("", "", "", "");
		
		//5 完成率最高
		setHighCR();
		//6 完成率最低
		setLowCR();
		insertBlankData("", "", "", "");
		
		//7、执行时间最长的
		setHighET();
		//8、执行时间最短的
		setLowET();
		insertBlankData("", "", "", "");
		
		
	}
	


	private static void setLowET() {
		double data=100000;
		String algorithm="";
		String title="Minimum Execution Time";
		String conclusion="";
		for(Map.Entry<String, Double> map:ETs.entrySet()){
			if(map.getValue()<data){
				data=map.getValue();
				algorithm=map.getKey();
			}
		}
		insertData(title,algorithm,data+"ms",conclusion);
		
	}

	private static void setHighET() {

		double data=0;
		String algorithm="";
		String title="Maximum Execution Time";
		String conclusion="";
		for(Map.Entry<String, Double> map:ETs.entrySet()){
			if(map.getValue()>data){
				data=map.getValue();
				algorithm=map.getKey();
			}
		}
		insertData(title,algorithm,data+"ms",conclusion);
		
	}

	
	private static void setLowCR() {
		double data=100;
		String algorithm="";
		String title="Minimum Success Rate";
		String conclusion="";
		for(Map.Entry<String, Double> map:CRs.entrySet()){
			if(map.getValue()<data){
				data=map.getValue();
				algorithm=map.getKey();
			}
		}
		insertData(title,algorithm,data+"%",conclusion);
		
	}

	private static void setHighCR() {

		double data=0;
		String algorithm="";
		String title="Maximum Success Rate";
		String conclusion="";
		for(Map.Entry<String, Double> map:CRs.entrySet()){
			if(map.getValue()>data){
				data=map.getValue();
				algorithm=map.getKey();
			}
		}
		insertData(title,algorithm,data+"%",conclusion);
		
	}
	
	
	
	private static void setLowUER() {
		double data=100;
		String algorithm="";
		String title="Minimum Effective Utilization Rate";
		String conclusion="";
		for(Map.Entry<String, Double> map:UERs.entrySet()){
			if(map.getValue()<data){
				data=map.getValue();
				algorithm=map.getKey();
			}
		}
		insertData(title,algorithm,data+"%",conclusion);
		
	}

	private static void setHighUER() {
		double data=0;
		String algorithm="";
		String title="Maximum Effective Utilization Rate";
		String conclusion="";
		for(Map.Entry<String, Double> map:UERs.entrySet()){
			if(map.getValue()>data){
				data=map.getValue();
				algorithm=map.getKey();
			}
		}
		insertData(title,algorithm,data+"%",conclusion);
		
	}



	private static void setLowER() {
		double data=100;
		String algorithm="";
		String title="Minimum Utilization Rate";
		String conclusion="";
		for(Map.Entry<String, Double> map:ERs.entrySet()){
			if(map.getValue()<data){
				data=map.getValue();
				algorithm=map.getKey();
			}
		}
		insertData(title,algorithm,data+"%",conclusion);
		
	}

	private static void setHighER() {

		double data=0;
		String algorithm="";
		String title="Maximum Utilization Rate";
		String conclusion="";
		for(Map.Entry<String, Double> map:ERs.entrySet()){
			if(map.getValue()>data){
				data=map.getValue();
				algorithm=map.getKey();
			}
		}
		insertData(title,algorithm,data+"%",conclusion);
		
	}
	
	private static void insertData(String title, String algorithm, String data, String conclusion) {
		TableItem item;
		item = new TableItem(targetTable, SWT.NONE);
		item.setText(new String[] { title,algorithm.toUpperCase(),String.valueOf(data),conclusion});
		
	}
	
	private static void insertBlankData(String title, String algorithm, String data, String conclusion) {
		TableItem item;
		item = new TableItem(targetTable, SWT.NONE);
		item.setText(new String[] { title,algorithm,data,conclusion});
	}
	
	/**
	 * 分解结果
	 * @param resultMap
	 */
	private static void splitResultMap(HashMap<String, String> resultMap) {
		
		ERs.clear();
		UERs.clear();
		CRs.clear();
		ETs.clear();
		
		for(Map.Entry<String, String> map:resultMap.entrySet()){
			String value=map.getValue();
			ERs.put(map.getKey(), Double.valueOf(value.split(",")[0]));
			UERs.put(map.getKey(), Double.valueOf(value.split(",")[1]));
			CRs.put(map.getKey(), Double.valueOf(value.split(",")[2]));
			ETs.put(map.getKey(), Double.valueOf(value.split(",")[3]));
		}
	}
	

}
