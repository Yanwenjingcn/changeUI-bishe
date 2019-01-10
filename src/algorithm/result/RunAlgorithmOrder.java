package algorithm.result;

import org.generate.util.CommonParametersUtil;

public class RunAlgorithmOrder {

	/**
	 * @throws Throwable
	 * @Title: main
	 * @Description: TODO
	 * @param @param args
	 * @return void
	 * @throws
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
			String basePathXML = "G:\\DagCasesXML\\singleDAGPercent"
					+ singleDAGPercent[i] + "\\";
			String resultPath = "G:\\DagCasesResult\\singleDAGPercent"
					+ singleDAGPercent[i] + "\\";
			runAlgorithmNew(basePathXML, resultPath);
			CommonParametersUtil.setSingleDAGPercent(0.5);
		}
	}

	public static void deadLineTimes() throws Throwable {
		double[] deadLineTimes = { 1.2,1.5,1.8 };
		for (int i = 0; i < deadLineTimes.length; i++) {
			CommonParametersUtil.setDeadLineTimes(deadLineTimes[i]);
			String basePathXML = "G:\\DagCasesXML\\deadLineTimes"
					+ deadLineTimes[i] + "\\";
			String resultPath = "G:\\DagCasesResult\\deadLineTimes"
					+ deadLineTimes[i] + "\\";
			runAlgorithmNew(basePathXML, resultPath);
			CommonParametersUtil.setDeadLineTimes(1.5);
		}
	}

	public static void taskAverageLength() throws Throwable {
		int[] taskAverageLength = { 20, 40, 60 };
		for (int i = 0; i < taskAverageLength.length; i++) {
			CommonParametersUtil.setTaskAverageLength(taskAverageLength[i]);

			String basePathXML = "G:\\DagCasesXML\\taskAverageLength"
					+ taskAverageLength[i] + "\\";

			String resultPath = "G:\\DagCasesResult\\taskAverageLength"
					+ taskAverageLength[i] + "\\";

			runAlgorithmNew(basePathXML, resultPath);

			CommonParametersUtil.setTaskAverageLength(40);
		}
	}

	public static void processorNumber() throws Throwable {

		int[] processorNumber = { 4, 8, 16 };
		for (int i = 0; i < processorNumber.length; i++) {
			CommonParametersUtil.setProcessorNumber(processorNumber[i]);

			String basePathXML = "G:\\DagCasesXML\\processorNumber"
					+ processorNumber[i] + "\\";

			String resultPath = "G:\\DagCasesResult\\processorNumber"
					+ processorNumber[i] + "\\";

			runAlgorithmNew(basePathXML, resultPath);

			CommonParametersUtil.setProcessorNumber(8);
		}
	}

	public static void dagLevelFlag() throws Throwable {
		int[] dagLevelFlag = { 1, 2, 3 };

		for (int i = 0; i < dagLevelFlag.length; i++) {
			CommonParametersUtil.setDagLevelFlag(dagLevelFlag[i]);

			String basePathXML = "G:\\DagCasesXML\\dagLevelFlag"
					+ dagLevelFlag[i] + "\\";

			String resultPath = "G:\\DagCasesResult\\dagLevelFlag"
					+ dagLevelFlag[i] + "\\";

			runAlgorithmNew(basePathXML, resultPath);

			CommonParametersUtil.setDagLevelFlag(2);
		}
	}

	public static void dagAverageSize() throws Throwable {
		int[] dagAverageSize = { 20, 40, 60 };
		for (int i = 0; i < dagAverageSize.length; i++) {

			CommonParametersUtil.setDagAverageSize(dagAverageSize[i]);
			// XML�ļ����õ�λ��
			String basePathXML = "G:\\DagCasesXML\\dagAverageSize"
					+ dagAverageSize[i] + "\\";

			// ��������λ��
			String resultPath = "G:\\DagCasesResult\\dagAverageSize"
					+ dagAverageSize[i] + "\\";

			runAlgorithmNew(basePathXML, resultPath);

			CommonParametersUtil.setDagAverageSize(40);
		}
	}

	public static void runAlgorithmNew(String basePathXML, String resultPath)
			throws Throwable {

		// ÿ���������100�Σ�100�εĽ�������һ���ļ��ͺá�

		for (int i = 0; i < 50; i++) {

			
			

			String pathXML = basePathXML;
			pathXML = basePathXML + i + "\\";

			
		}
	}
}
