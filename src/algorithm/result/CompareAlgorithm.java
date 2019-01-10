package algorithm.result;

import org.generate.util.CommonParametersUtil;

import bishe.WB;
import bishe.WB_CP;
import bishe.wbMaxDeadlineFirst;
import bishe.wbMaxTaskNums;
import bishe.wbMinDeadlineFirst;
import bishe.wbMinTaskNums;

public class CompareAlgorithm {

	/**
	 * @throws Throwable @Title: main @Description: TODO @param @param args @return
	 * void @throws
	 */
	public static void main(String[] args) throws Throwable {

		singleDAGPercent();
		dagAverageSize();
		dagLevelFlag();
		processorNumber();
		taskAverageLength();
		
		deadLineTimes();

	}

	public static void singleDAGPercent() throws Throwable {
		double[] singleDAGPercent = { 0.2, 0.5, 0.8 };
		for (int i = 0; i < singleDAGPercent.length; i++) {
			CommonParametersUtil.setSingleDAGPercent(singleDAGPercent[i]);
			String basePathXML = "G:\\DagCasesXML\\singleDAGPercent" + singleDAGPercent[i] + "\\";
			String resultPath = "G:\\DagCasesResult\\singleDAGPercent" + singleDAGPercent[i] + "\\";
			runAlgorithm(basePathXML, resultPath);

			CommonParametersUtil.setSingleDAGPercent(0.5);
		}
	}

	public static void deadLineTimes() throws Throwable {
		// deadline�ı���ֵ ��1.1��1.3��1.6��2.0��
		// String deadLineTimes = String.valueOf(BuildParameters.deadLineTimes);

		double[] deadLineTimes = { 1.2, 1.1, 1.3 };

		for (int i = 0; i < deadLineTimes.length; i++) {
			CommonParametersUtil.setDeadLineTimes(deadLineTimes[i]);

			String basePathXML = "G:\\DagCasesXML\\deadLineTimes" + deadLineTimes[i] + "\\";

			String resultPath = "G:\\DagCasesResult\\deadLineTimes" + deadLineTimes[i] + "\\";

			runAlgorithm(basePathXML, resultPath);

			CommonParametersUtil.setDeadLineTimes(1.2);

		}
	}

	public static void taskAverageLength() throws Throwable {
		// �����ƽ�����ȣ�20,30,40,50 Ĭ��ֵ30��
		// String taskAverageLength =
		// String.valueOf(BuildParameters.taskAverageLength);

		int[] taskAverageLength = { 20, 40, 60 };
		// int[] taskAverageLength = {60 };
		for (int i = 0; i < taskAverageLength.length; i++) {
			CommonParametersUtil.setTaskAverageLength(taskAverageLength[i]);

			String basePathXML = "G:\\DagCasesXML\\taskAverageLength" + taskAverageLength[i] + "\\";

			String resultPath = "G:\\DagCasesResult\\taskAverageLength" + taskAverageLength[i] + "\\";

			runAlgorithm(basePathXML, resultPath);

			CommonParametersUtil.setTaskAverageLength(40);
		}
	}

	public static void processorNumber() throws Throwable {
		// ����Ԫ�ĸ�����2,4,8,16,32��String processorNumber =
		// String.valueOf(BuildParameters.processorNumber);

		int[] processorNumber = { 4, 8, 16 };
		for (int i = 0; i < processorNumber.length; i++) {
			CommonParametersUtil.setProcessorNumber(processorNumber[i]);

			String basePathXML = "G:\\DagCasesXML\\processorNumber" + processorNumber[i] + "\\";

			String resultPath = "G:\\DagCasesResult\\processorNumber" + processorNumber[i] + "\\";

			runAlgorithm(basePathXML, resultPath);

			CommonParametersUtil.setProcessorNumber(8);
		}
	}

	public static void dagLevelFlag() throws Throwable {
		int[] dagLevelFlag = { 1, 2, 3 };

		for (int i = 0; i < dagLevelFlag.length; i++) {
			CommonParametersUtil.setDagLevelFlag(dagLevelFlag[i]);

			String basePathXML = "G:\\DagCasesXML\\dagLevelFlag" + dagLevelFlag[i] + "\\";

			String resultPath = "G:\\DagCasesResult\\dagLevelFlag" + dagLevelFlag[i] + "\\";

			runAlgorithm(basePathXML, resultPath);

			CommonParametersUtil.setDagLevelFlag(2);
		}
	}

	public static void dagAverageSize() throws Throwable {
		int[] dagAverageSize = { 20, 40, 60 };
		for (int i = 0; i < dagAverageSize.length; i++) {

			CommonParametersUtil.setDagAverageSize(dagAverageSize[i]);
			// XML�ļ����õ�λ��
			String basePathXML = "G:\\DagCasesXML\\dagAverageSize" + dagAverageSize[i] + "\\";

			// ��������λ��
			String resultPath = "G:\\DagCasesResult\\dagAverageSize" + dagAverageSize[i] + "\\";

			runAlgorithm(basePathXML, resultPath);

			CommonParametersUtil.setDagAverageSize(40);
		}
	}

	public static void runAlgorithm(String basePathXML, String resultPath) throws Throwable {

		for (int i = 0; i < 50; i++) {

			String pathXML = basePathXML;
			pathXML = basePathXML + i + "\\";

			WB wb = new WB();//后推、不改变作业顺序
			wb.runMakespan(pathXML, resultPath);

			WB_CP wb_CP = new WB_CP();//关键路径，不改变作业顺序
			wb_CP.runMakespan(pathXML, resultPath);

			wbMinTaskNums wb_SF = new wbMinTaskNums();//后推、少任务数优先
			wb_SF.runMakespan(pathXML, resultPath);

			wbMaxTaskNums wb_LF = new wbMaxTaskNums();//后推，多任务数优先
			wb_LF.runMakespan(pathXML, resultPath);
			
			wbMinDeadlineFirst wbMinDeadlineFirst=new wbMinDeadlineFirst();
			wbMinDeadlineFirst.runMakespan(pathXML, resultPath);
			
			wbMaxDeadlineFirst wbMaxDeadlineFirst=new wbMaxDeadlineFirst();
			wbMaxDeadlineFirst.runMakespan(pathXML, resultPath);

		}

	}

}
