package org.generate.util;

/**
 * 
 * @ClassName: BuildParameters
 * @Description: generate default parameters
 * @author YWJ
 * @date 2017-9-9 ����3:23:04
 */
public class CommonParametersUtil {
	
	
	public static int taskAverageLength = 40;

	public static int dagAverageSize = 40;
	
	public static int dagLevelFlag = 2;

	public static double deadLineTimes = 1.2;
	
	public static int processorNumber = 8;
	
	public static double singleDAGPercent = 0.5;// 单个DAG产生的概率


	public static double getSingleDAGPercent() {
		return singleDAGPercent;
	}

	public static void setSingleDAGPercent(double singleDAGPercent) {
		CommonParametersUtil.singleDAGPercent = singleDAGPercent;
	}

	public static int FIFO=0;
	public static int EDF=0;
	public static int STF=0;
	public static int EFTF=0;
	public static int Workflowbased=0;

	public static int defaultRoundTime=2;
	
	// timeWindow default 40000
	public static int timeWindow = 40000;

	public static int getTimeWindow() {
		return timeWindow;
	}

	public static void setTimeWindow(int timeWindow) {
		CommonParametersUtil.timeWindow = timeWindow;
	}

	public static int getTaskAverageLength() {
		return taskAverageLength;
	}

	public static void setTaskAverageLength(int taskAverageLength) {
		CommonParametersUtil.taskAverageLength = taskAverageLength;
	}

	public static int getDagAverageSize() {
		return dagAverageSize;
	}

	public static void setDagAverageSize(int dagAverageSize) {
		CommonParametersUtil.dagAverageSize = dagAverageSize;
	}

	public static int getDagLevelFlag() {
		return dagLevelFlag;
	}

	public static void setDagLevelFlag(int dagLevelFlag) {
		CommonParametersUtil.dagLevelFlag = dagLevelFlag;
	}

	public static double getDeadLineTimes() {
		return deadLineTimes;
	}

	public static void setDeadLineTimes(double deadLineTimes) {
		CommonParametersUtil.deadLineTimes = deadLineTimes;
	}

	public static int getProcessorNumber() {
		return processorNumber;
	}

	public static void setProcessorNumber(int processorNumber) {
		CommonParametersUtil.processorNumber = processorNumber;
	}

	public static int getFIFO() {
		return FIFO;
	}

	public static void setFIFO(int fIFO) {
		FIFO = fIFO;
	}

	public static int getEDF() {
		return EDF;
	}

	public static void setEDF(int eDF) {
		EDF = eDF;
	}

	public static int getSTF() {
		return STF;
	}

	public static void setSTF(int sTF) {
		STF = sTF;
	}

	public static int getEFTF() {
		return EFTF;
	}

	public static void setEFTF(int eFTF) {
		EFTF = eFTF;
	}

	public static int getWorkflowbased() {
		return Workflowbased;
	}

	public static void setWorkflowbased(int workflowbased) {
		Workflowbased = workflowbased;
	}

	public static int getDefaultRoundTime() {
		return defaultRoundTime;
	}

	public static void setDefaultRoundTime(int defaultRoundTime) {
		CommonParametersUtil.defaultRoundTime = defaultRoundTime;
	}

	

}
