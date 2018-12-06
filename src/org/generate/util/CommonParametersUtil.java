package org.generate.util;

/**
 * 
 * @ClassName: BuildParameters
 * @Description: generate default parameters
 * @author YWJ
 * @date 2017-9-9 ����3:23:04
 */
public class CommonParametersUtil {
	// timeWindow default 40000
	public static int timeWindow = 40000;
	// Average task length��option:20,40,60 default 40��
	public static int taskAverageLength = 40;
	// Average dag size��20,40,60 default 40��
	public static int dagAverageSize = 40;
	
	public static int dagLevelFlag = 2;

	public static double deadLineTimes = 1.1;
	// number of processor��4,8,16��
	public static int processorNumber = 8;





	public static int FIFO=0;
	public static int EDF=0;
	public static int STF=0;
	public static int EFTF=0;
	public static int Workflowbased=0;

	public static int defaultRoundTime=2;

}
