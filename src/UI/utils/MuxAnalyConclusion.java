package UI.utils;

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
		
		//7
		setHighET();
		
		setLowET();
		
		insertBlankData("", "", "", "");
		
		
	}
	


	private static void setLowET() {
		double data=1;
		String algorithm="";
		String title="执行时间最短";
		String conclusion="";
		for(Map.Entry<String, Double> map:ETs.entrySet()){
			if(map.getValue()<data){
				data=map.getValue();
				algorithm=map.getKey();
			}
		}
		insertData(title,algorithm,data,conclusion);
		
	}

	private static void setHighET() {

		double data=0;
		String algorithm="";
		String title="执行时间最长";
		String conclusion="";
		for(Map.Entry<String, Double> map:ETs.entrySet()){
			if(map.getValue()>data){
				data=map.getValue();
				algorithm=map.getKey();
			}
		}
		insertData(title,algorithm,data,conclusion);
		
	}

	
	private static void setLowCR() {
		double data=1;
		String algorithm="";
		String title="完成率最低";
		String conclusion="";
		for(Map.Entry<String, Double> map:CRs.entrySet()){
			if(map.getValue()<data){
				data=map.getValue();
				algorithm=map.getKey();
			}
		}
		insertData(title,algorithm,data,conclusion);
		
	}

	private static void setHighCR() {

		double data=0;
		String algorithm="";
		String title="完成率最高";
		String conclusion="";
		for(Map.Entry<String, Double> map:CRs.entrySet()){
			if(map.getValue()>data){
				data=map.getValue();
				algorithm=map.getKey();
			}
		}
		insertData(title,algorithm,data,conclusion);
		
	}
	
	
	
	private static void setLowUER() {
		double data=1;
		String algorithm="";
		String title="处理器有效利用率最低";
		String conclusion="";
		for(Map.Entry<String, Double> map:UERs.entrySet()){
			if(map.getValue()<data){
				data=map.getValue();
				algorithm=map.getKey();
			}
		}
		insertData(title,algorithm,data,conclusion);
		
	}

	private static void setHighUER() {
		double data=0;
		String algorithm="";
		String title="处理器有效利用率最高";
		String conclusion="";
		for(Map.Entry<String, Double> map:UERs.entrySet()){
			if(map.getValue()>data){
				data=map.getValue();
				algorithm=map.getKey();
			}
		}
		insertData(title,algorithm,data,conclusion);
		
	}



	private static void setLowER() {
		double data=1;
		String algorithm="";
		String title="处理器利用率最低";
		String conclusion="";
		for(Map.Entry<String, Double> map:ERs.entrySet()){
			if(map.getValue()<data){
				data=map.getValue();
				algorithm=map.getKey();
			}
		}
		insertData(title,algorithm,data,conclusion);
		
	}

	private static void setHighER() {

		double data=0;
		String algorithm="";
		String title="处理器利用率最高";
		String conclusion="";
		for(Map.Entry<String, Double> map:ERs.entrySet()){
			if(map.getValue()>data){
				data=map.getValue();
				algorithm=map.getKey();
			}
		}
		insertData(title,algorithm,data,conclusion);
		
	}
	
	private static void insertData(String title, String algorithm, double data, String conclusion) {
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
