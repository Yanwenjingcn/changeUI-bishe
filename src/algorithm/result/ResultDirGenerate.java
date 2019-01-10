package algorithm.result;

import java.io.File;
import java.io.IOException;

import org.generate.util.CommonParametersUtil;

/**
 * 
* @ClassName: ResultDirGenerate
* @Description: ���ɽ���ļ���
* @author YanWenjing
* @date 2017-9-22 ����1:06:27
 */
public class ResultDirGenerate {


	static File fileResult;
	static File resultTxt;
	
	static String wb="wb.txt";
	static String wbCp="wbCp.txt";
	static String wbMaxTaskNums="wbMaxTaskNumFirst.txt";
	static String wbMinTaskNums="wbMinTaskNumFirst.txt";
	
	static String wbMinDeadlineFirst="wbMinDeadlineFirst.txt";
	static String wbMaxDeadlineFirst="wbMaxDeadlineFirst.txt";
	

	/**
	 * @throws IOException 
	 * @Title: main
	 * @Description: TODO
	 * @param @param args
	 * @return void
	 * @throws
	 */
	public static void main(String[] args) throws IOException {

		int[] dagAverageSize = { 20, 40, 60 };
		for (int i = 0; i < dagAverageSize.length; i++) {
			CommonParametersUtil.setDagAverageSize(dagAverageSize[i]);
			String basePathResult="G:\\DagCasesResult\\dagAverageSize"+ dagAverageSize[i] + "\\";
			cycle(basePathResult);

		}
		
		
		int[] dagLevelFlag = { 1,2,3 };
		for (int i = 0; i < dagLevelFlag.length; i++) {
			CommonParametersUtil.setDagLevelFlag(dagLevelFlag[i]);
			String basePathResult="G:\\DagCasesResult\\dagLevelFlag"+ dagLevelFlag[i] + "\\";
			cycle(basePathResult);
		}
		

		
		// ����Ԫ�ĸ�����2,4,8,16,32��String processorNumber = String.valueOf(BuildParameters.processorNumber);
		int[] processorNumber = {4,8,16 };
		for (int i = 0; i < processorNumber.length; i++) {
			CommonParametersUtil.setProcessorNumber(processorNumber[i]);
			String basePathResult="G:\\DagCasesResult\\processorNumber"+ processorNumber[i] + "\\";
			cycle(basePathResult);
		}
		

		// �����ƽ�����ȣ�20,30,40,50 Ĭ��ֵ30��
		//String taskAverageLength = String.valueOf(BuildParameters.taskAverageLength);
		
		int[] taskAverageLength = { 20, 40, 60 };
		for (int i = 0; i < taskAverageLength.length; i++) {
			CommonParametersUtil.setTaskAverageLength(taskAverageLength[i]);
			
			String basePathResult="G:\\DagCasesResult\\taskAverageLength"+ taskAverageLength[i] + "\\";
			cycle(basePathResult);
		}
		
		
		
		
		// deadline�ı���ֵ ��1.1��1.3��1.6��2.0��
		//String deadLineTimes = String.valueOf(BuildParameters.deadLineTimes);

	
		double[] deadLineTimes = {1.2,1.1,1.3};
		
		for (int i = 0; i < deadLineTimes.length; i++) {
			CommonParametersUtil.setDeadLineTimes(deadLineTimes[i]);
			
			String basePathResult="G:\\DagCasesResult\\deadLineTimes"+ deadLineTimes[i] + "\\";
			cycle(basePathResult);
			
		}
		
		double[] singleDAGPercent = {0.2,0.5,0.8};
		for (int i = 0; i < singleDAGPercent.length; i++) {
			CommonParametersUtil.setSingleDAGPercent(singleDAGPercent[i]);
			String basePathResult="G:\\DagCasesResult\\singleDAGPercent"+ singleDAGPercent[i] + "\\";
			cycle(basePathResult);	
		}
		
		
		
	}


	/**
	 * @throws IOException 
	 * 
	* @Title: cycle
	* @Description: TODO
	* @param @param basePathResult
	* @return void
	* @throws
	 */
	public static void cycle(String basePathResult) throws IOException {

		String pathResult = basePathResult;
		fileResult=new File(pathResult);
		fileResult.mkdirs();
		
		
		resultTxt=new File(basePathResult+wb);
		resultTxt.createNewFile();
		
		resultTxt=new File(basePathResult+wbCp);
		resultTxt.createNewFile();
		
		resultTxt=new File(basePathResult+wbMaxTaskNums);
		resultTxt.createNewFile();
		
		resultTxt=new File(basePathResult+wbMinTaskNums);
		resultTxt.createNewFile();
		
		resultTxt=new File(basePathResult+wbMaxDeadlineFirst);
		resultTxt.createNewFile();
		
		
		resultTxt=new File(basePathResult+wbMinDeadlineFirst);
		resultTxt.createNewFile();		
	

		
	}

}
