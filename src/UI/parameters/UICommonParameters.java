package UI.parameters;

import java.util.HashMap;

import org.eclipse.swt.widgets.Shell;

/**
 * 
* @ClassName: UICommonParameters
* @Description: 在UI包内传递的参数
* @author Wengie Yan
* @date 2018年12月12日
 */
public class UICommonParameters {
	//每个处理器展示结果显示的宽度
	public static int width;
	//每个处理器展示结果显示的高度
	public static int height;
	//每个处理器展示结果显示的时间窗
	public static int timewind;
	
	//随机产生的颜色参数
	public static int[][] color = new int[500][3];
	public static int leftmargin = 110;
	public static int maxheight = 1000;
	public static int dagnummax = 10000;
	public static int mesnum = 5;
	//每一种算法计算后会应用于展示的任务数（比如自己的算法就只展示调度成功的task）
	public static HashMap<String,Integer> TaskNums=new HashMap<>();
	//主shell
	public static Shell shell;

	//在多次分析后对每种算法的计算结果统计MAP
	public static HashMap<String, String> resultMap;
	

}
