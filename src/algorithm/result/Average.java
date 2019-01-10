package algorithm.result;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import org.generate.util.CommonParametersUtil;

public class Average {

	public static void main(String[] args) throws Throwable {
//		String[] fileName=new String[5];
//		fileName[0]="edf.txt";
//		fileName[1]="etf.txt";
//		fileName[2]="fifo.txt";
//		fileName[3]="stf.txt";
//		fileName[4]="mergeDag.txt";
		
//		String[] fileName=new String[1];
//		fileName[0]="fillback.txt";

		String[] fileName=new String[6];
		fileName[0]="wb.txt";
		fileName[1]="wbCp.txt";
		fileName[2]="wbMaxDeadlineFirst.txt";
		fileName[3]="wbMinDeadlineFirst.txt";
		fileName[4]="wbMaxTaskNumFirst.txt";
		fileName[5]="wbMinTaskNumFirst.txt";
	

				
		
		
//		String[] fileName=new String[5];
//		fileName[0]="OrderAsBtoS.txt";
//		fileName[1]="OrderAsHEFT.txt";
//		fileName[2]="OrderAsIDWithAdaptation.txt";
//		fileName[3]="OrderAsOriId.txt";
//		fileName[4]="OrderAsStoB.txt";

		
//		String[] fileName=new String[7];
//		fileName[0]="OrderAsBtoS.txt";
//		fileName[1]="OrderAsBtoSWithAdaptation.txt";
//		fileName[2]="OrderAsStoBWithAdaptation.txt";
//		fileName[3]="OrderAsOriId.txt";
//		fileName[4]="OrderAsStoB.txt";
//		fileName[5]="OrderAsHEFT.txt";
//		fileName[6]="OrderAsIDWithAdaptation.txt";
		
		for(int i=0;i<fileName.length;i++){
			dagAverageSize(fileName[i]);
			dagLevelFlag(fileName[i]);
			deadLineTimes(fileName[i]);
			processorNumber(fileName[i]);
			taskAverageLength(fileName[i]);
			singleDAGPercent(fileName[i]);
		}
	}
	
	public static void deadLineTimes(String fileName) throws Throwable {

		double[] deadLineTimes = {  1.2,1.5,1.8 };
		int[] flag = {  12,15, 18 };

		for (int i = 0; i < deadLineTimes.length; i++) {
			CommonParametersUtil.setDeadLineTimes(deadLineTimes[i]);

			String inputPath = "G:\\DagCasesResult\\deadLineTimes" + deadLineTimes[i] + "\\"+fileName;

			String outputPath = "G:\\result\\"+fileName;

			writeToFile(inputPath, outputPath,flag[i],"deadLineTimes");

		}
	}

	public static void taskAverageLength(String fileName) throws Throwable {


		int[] taskAverageLength = { 20,  40, 60  };
		for (int i = 0; i < taskAverageLength.length; i++) {
			CommonParametersUtil.setTaskAverageLength(taskAverageLength[i]);
			String inputPath = "G:\\DagCasesResult\\taskAverageLength" + taskAverageLength[i] + "\\"+fileName;

			String outputPath = "G:\\result\\"+fileName;

			writeToFile(inputPath, outputPath,taskAverageLength[i],"taskAverageLength");
		}
	}

	public static void processorNumber(String fileName) throws Throwable {

		int[] processorNumber = {  4, 8, 16 };
		for (int i = 0; i < processorNumber.length; i++) {
			CommonParametersUtil.setProcessorNumber(processorNumber[i]);
			String inputPath = "G:\\DagCasesResult\\processorNumber" + processorNumber[i] + "\\"+fileName;

			String outputPath = "G:\\result\\"+fileName;

			writeToFile(inputPath, outputPath,processorNumber[i],"processorNumber");
		}
	}

	public static void dagLevelFlag(String fileName) throws Throwable {
		int[] dagLevelFlag = { 1, 2, 3 };

		for (int i = 0; i < dagLevelFlag.length; i++) {
			CommonParametersUtil.setDagLevelFlag(dagLevelFlag[i]);
			String inputPath = "G:\\DagCasesResult\\dagLevelFlag" + dagLevelFlag[i] + "\\"+fileName;

			String outputPath = "G:\\result\\"+fileName;

			writeToFile(inputPath, outputPath,dagLevelFlag[i],"dagLevelFlag");
		}
	}

	public static void dagAverageSize(String fileName) throws Throwable {
		int[] dagAverageSize = { 20,  40, 60 };
		for (int i = 0; i < dagAverageSize.length; i++) {

			CommonParametersUtil.setDagAverageSize(dagAverageSize[i]);
			String inputPath = "G:\\DagCasesResult\\dagAverageSize" + dagAverageSize[i] + "\\"+fileName;

			String outputPath = "G:\\result\\"+fileName;

			writeToFile(inputPath, outputPath,dagAverageSize[i],"dagAverageSize");
		}
	}	
	
	public static void singleDAGPercent(String fileName) throws Throwable {

		double[] singleDAGPercent = {  0.2,0.5,0.8 };
		int[] flag = {  2,5,8 };
		for (int i = 0; i < singleDAGPercent.length; i++) {
			CommonParametersUtil.setSingleDAGPercent(singleDAGPercent[i]);

			String inputPath = "G:\\DagCasesResult\\singleDAGPercent" + singleDAGPercent[i] + "\\"+fileName;

			String outputPath = "G:\\result\\"+fileName;

			writeToFile(inputPath, outputPath,flag[i],"singleDAGPercent");

		}
	}


	public static void writeToFile(String inputPath,String outputPath,int flag,String param) throws FileNotFoundException, IOException {
		int roundTimes=50;
		BufferedReader bd = new BufferedReader(new FileReader(inputPath));
		FileWriter resultWriter = null;
		resultWriter = new FileWriter(outputPath, true);
		float[] result=new float[4];
		for (int i = 0; i < roundTimes; i++) {
			String buffered = bd.readLine();
			String bufferedA[] = buffered.split("\t");
			result[0]=result[0]+Float.valueOf(bufferedA[0]);
			result[1]=result[1]+Float.valueOf(bufferedA[1]);
			result[2]=result[2]+Float.valueOf(bufferedA[2]);
			result[3]=result[3]+Float.valueOf(bufferedA[3]);	
		}
		DecimalFormat df = new DecimalFormat("0.0000");
		result[0] = Float.valueOf(String.valueOf(df.format(result[0]/roundTimes)));
		result[1] = Float.valueOf(String.valueOf(df.format(result[1]/roundTimes)));
		result[2] = Float.valueOf(String.valueOf(df.format(result[2]/roundTimes)));
		result[3] = Float.valueOf(String.valueOf(df.format(result[3]/roundTimes)));
				
		resultWriter.write(param.substring(0, 8)+"-"+flag+"\t"+result[0]+"\t"+result[1]+"\t"+result[2]+"\t"+result[3]+"\n");
		
		if (resultWriter != null) {
			resultWriter.close();
		}
	}
}
