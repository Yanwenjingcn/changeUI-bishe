package bishe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Stack;
import java.util.Map.Entry;

import org.jdom.xpath.XPath;
import org.schedule.model.DAG;
import org.schedule.model.DAGDepend;
import org.schedule.model.PE;
import org.schedule.model.PEComputerability;
import org.schedule.model.Slot;
import org.schedule.model.Task;
import org.generate.util.CommonParametersUtil;
import org.jdom.Attribute;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


/**
 * 
 * @ClassName: FillbacknewWithoutInsert
 * @Description: 
 * 1�����ϲ�
 * 2���޸�����DAG��deadline�ķ�ʽ
 * 2������cpȡ�ؼ�·���ϵ�ʱ����nȡ����DAG����������ex=��deadline-cp��/n
 * 3���ı����ʱ�Դ���ʱ���Ķ���,������û��
 * 4��Ѱ�ҿ�϶��С�ķ�ʽ
 * 5���������ɳ�����
 * 6������ʱ�ǻ�����Լ���������
 * 
 * @author YanWenjing   
 * @date 2017-10-21 ����7:45:26
 */
public class DCBFOri {

	
	private static ArrayList<PE> PEList;
	private static ArrayList<DAG> DAGMapList;
	private static ArrayList<DAGDepend> DAGDependList = null;

	private static ArrayList<Task> Tasks_queue;
	private static ArrayList<Task> readyqueue;
	
	
	private static LinkedHashMap<Integer, Integer> DAGDependMap;
	private static LinkedHashMap<String, Double> DAGDependValueMap;

	private static ArrayList<Task> Task_queue_personal;
	private static LinkedHashMap<Integer, Integer> DAGDependMap_personal;
	private static LinkedHashMap<String, Double> DAGDependValueMap_personal;
	private static Map<Integer, int[]> ComputeCostMap;
	private static Map<Integer, Integer> AveComputeCostMap;



	// д���ļ��Ľ��
	public static String[][] rateResult = new String[1][4];

	private static int islastnum = 0;
	private static double deadLineTimes = 1.3;// deadline�ı���ֵ ��1.1��1.3��1.6��2.0��
	private static int pe_number = 8;

	public static String[][] rate = new String[5][2];

	public static int current_time;
	public static int proceesorEndTime = CommonParametersUtil.timeWindow;// ʱ�䴰
	public static int timeWindow;
	public static int T = 1;

	public static int fillbacktasknum = 10;
	public static int[][] message;
	public static int dagnummax = 10000;
	public static int timewindowmax = 9000000;
	public static int mesnum = 5;
	private static LinkedHashMap<Integer, ArrayList> SlotListInPes;
	private static LinkedHashMap<Integer, LinkedHashMap> TASKListInPes;

	// �������ϵĺ��Ʊ��
	private static int[] pushFlag;
	// ��Ҫ���Ƶ�������
	private static int pushCount = 0;
	// ���Ƴɹ��ܴ���
	private static int pushSuccessCount = 0;

	// ��������Ŀ
	private static int taskTotal = 0;
	private static int[][] dagResultMap = null;

	

	public DCBFOri() {
		readyqueue = new ArrayList<Task>();
		Tasks_queue = new ArrayList<Task>();
		Task_queue_personal = new ArrayList<Task>();
		PEList = new ArrayList<PE>();
		DAGMapList = new ArrayList<DAG>();
		DAGDependList = new ArrayList<DAGDepend>();
		
		DAGDependMap = new LinkedHashMap<Integer, Integer>();
		DAGDependValueMap = new LinkedHashMap<String, Double>();
		deadLineTimes = CommonParametersUtil.deadLineTimes;
		pe_number = CommonParametersUtil.processorNumber;
		current_time = 0;
		timeWindow = proceesorEndTime / pe_number;

		pushFlag = new int[pe_number];
		dagResultMap = new int[1000][dagnummax];

		message = new int[dagnummax][mesnum];
		SlotListInPes = new LinkedHashMap<Integer, ArrayList>(); // �����������ϵĿ��ж���Ϣ

		TASKListInPes = new LinkedHashMap<Integer, LinkedHashMap>();
		for (int i = 0; i < pe_number; i++) {
			LinkedHashMap<Integer, Integer[]> TASKInPe = new LinkedHashMap<Integer, Integer[]>();
			TASKListInPes.put(i, TASKInPe);
			// 数组0代表task开始时间，1代表task结束时间，2代表dagid，3代表id
		}

	}

	/**
	 * 
	 * @Title: runMakespan
	 * @Description: 开始fillback算法
	 * @param @throws Throwable
	 * @return void
	 * @throws
	 */
	public void runMakespan(String pathXML, String resultPath) throws Throwable {

		// init dagmap
		DCBFOri fb = new DCBFOri();
		DAGDepend dagdepend = new DAGDepend();
		PEComputerability vcc = new PEComputerability();

		// 初始化处理器
		initPE();

		// 初始化输入xml
		initdagmap(dagdepend, vcc, pathXML);

		Date begin = new Date();
		Long beginTime = begin.getTime();
		// 调度第一个DAG，初始化各个处理器上的任务分布、空闲段、各个任务的松弛情况
				// 设置当前时间是第一个DAG 的到达时间
		current_time = DAGMapList.get(0).getsubmittime();

		// 开始调度后续的作业
		for (int i = 0; i < DAGMapList.size(); i++) {

			LinkedHashMap<Integer, ArrayList> SlotListInPestemp = new LinkedHashMap<Integer, ArrayList>();
			LinkedHashMap<Integer, LinkedHashMap> TASKListInPestemp = new LinkedHashMap<Integer, LinkedHashMap>();

			// 获得适应本作业范围内的空闲块内容
			computeSlot(DAGMapList.get(i).getsubmittime(), DAGMapList.get(i).getDAGdeadline());
		
			SlotListInPestemp = copySlot();
			TASKListInPestemp = copyTASK();

			scheduleOtherDAG(i, SlotListInPestemp, TASKListInPestemp);
		}

		Date end = new Date();
		Long endTime = end.getTime();
		Long diff = endTime - beginTime;

		outputresult(diff, resultPath);	
		storeresultShow();

	}


	
	/**
	 * 输出处理器资源利用率和任务完成率
	 */
	public static void outputresult(Long diff, String resultPath) {
		int suc = 0;
		int fault = 0;
		int effective = 0;
		int notEffective = 0;
		int tempp = timeWindow;

		int successCount = 0;
		int faultCount = 0;

		for (int j = 0; j < DAGMapList.size(); j++) {
			ArrayList<Task> DAGTaskList = new ArrayList<Task>();
			for (int i = 0; i < DAGMapList.get(j).gettasklist().size(); i++) {
				Task dag_temp = (Task) DAGMapList.get(j).gettasklist().get(i);
				DAGTaskList.add(dag_temp);
			}
			// 不是第一个作业的值ֵ
				if (DAGMapList.get(j).getfillbackdone()) {
					successCount++;
					//System.out.println("dag" + DAGMapList.get(j).getDAGId()+ "调度成功，成功执行");
					suc++;
					for (int i = 0; i < DAGTaskList.size(); i++) {
						if (DAGTaskList.get(i).getfillbackdone()) {
							effective = effective + DAGTaskList.get(i).getts();
						} else {
							notEffective = notEffective+ DAGTaskList.get(i).getts();
						}

					}
				} else {
					faultCount++;
					for (int i = 0; i < DAGMapList.get(j).gettasklist().size(); i++)
						notEffective = notEffective+ DAGTaskList.get(i).getts();
				}

		}

		System.out.println("FillbacknewWithoutInsertOri��" + successCount+ "个任务调度完成");
		System.out.println("FillbacknewWithoutInsertOri��" + faultCount+ "个任务调度失败》》》");

		DecimalFormat df = new DecimalFormat("0.0000");
		System.out.println("FillbacknewWithoutInsertOri:");
		System.out.println("PE's use ratio is "
				+ df.format((float) effective / (pe_number * tempp))
				+ "\teffective=" + effective);
		System.out.println("PE's no use ratio is "
				+ df.format((float) notEffective / (pe_number * tempp))
				+ "\tnotEffective=" + notEffective);
		System.out.println("effective PE's use ratio is "
				+ df.format((float) effective / (tempp * pe_number)));
		System.out.println("Task Completion Rates is "
				+ df.format((float) successCount / DAGMapList.size()));
		System.out.println();

		rateResult[0][0] = df.format((float) effective / (pe_number * tempp));
		rateResult[0][1] = df.format((float) effective / (tempp * pe_number));
		rateResult[0][2] = df.format((float) successCount/ DAGMapList.size());

		rateResult[0][3] = df.format(diff);

//		System.out.println("任务后推成功计数=" + pushSuccessCount);
//		System.out.println("总任务数=" + taskTotal);

	}

	/**
	 * 
	 * @Title: storeresultShow
	 * @Description: 保存本算法的各个任务的开始结束时间,这里面只有调度成功的
	 * @param
	 * @return void
	 * @throws
	 */
	public static void storeresultShow() {
		int dagcount = 0;
		for (DAG dagmap : DAGMapList) {

			if (dagmap.fillbackdone) {
				ArrayList<Task> DAGTaskList = new ArrayList<Task>();

				for (int i = 0; i < dagmap.gettasklist().size(); i++) {
					Task dag = (Task) dagmap.gettasklist().get(i);
					DAGTaskList.add(dag);
					if (dag.getfillbackdone()) {
						message[dagcount][0] = dag.getdagid();
						message[dagcount][1] = dag.getid();
						message[dagcount][2] = dag.getfillbackpeid();
						message[dagcount][3] = dag.getfillbackstarttime();
						message[dagcount][4] = dag.getfillbackfinishtime();
						dagcount++;
					}
				}
			}
		}

	}

	/**
	 * 
	 * @Title: computeSlot
	 * @Description: 根据relax后结果重新计算空闲时间段SlotListInPes。其中SlotListInPes.put(i,
	 *               slotListinpe)==》slotListinpe的内容是筛选过的，时间上能匹配submit----
	 *               deadline时间段的slot的集合
	 * @param @param submit，DAG提交时间
	 * @param @param deadline，DAG截止时间
	 * @return void
	 * @throws
	 */
	public static void computeSlot(int submit, int deadline) {

		SlotListInPes.clear();

		for (int i = 0; i < pe_number; i++) {
			// 当前处理器上空闲片段总数计数
			int Slotcount = 0;

			// 获取某处理器上的任务内容
			LinkedHashMap<Integer, Integer[]> TASKInPe = new LinkedHashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(i);
			// 数组0代表task开始时间，1代表task结束时间，2代表dagid，3代表id

			ArrayList<Slot> slotListinpe = new ArrayList<Slot>();
			ArrayList<Slot> slotListinpe_ori = new ArrayList<Slot>();

			if (TASKInPe.size() == 0) {// 当前处理器上没有执行过任务
				Slot tem = new Slot();
				tem.setPEId(i);
				tem.setslotId(Slotcount);
				tem.setslotstarttime(submit);
				tem.setslotfinishtime(deadline);
				slotListinpe.add(tem);
				Slotcount++;
			} else if (TASKInPe.size() == 1) {// 该处理器上只有一个任务运行

				if (TASKInPe.get(0)[0] > submit) {
					if (deadline <= TASKInPe.get(0)[0]) {
						Slot tem = new Slot();
						ArrayList<String> below_ = new ArrayList<String>();
						// 数组0代表task开始时间，1代表task结束时间，2代表dagid，3代表id
						// 任务所属dag 任务编号
						below_.add(TASKInPe.get(0)[2] + " "
								+ TASKInPe.get(0)[3] + " " + 0);
						tem.setPEId(i);
						tem.setslotId(Slotcount);
						tem.setslotstarttime(submit);
						tem.setslotfinishtime(deadline);
						tem.setbelow(below_);
						slotListinpe.add(tem);
						Slotcount++;
					} else if (deadline <= TASKInPe.get(0)[1]) {
						Slot tem = new Slot();
						ArrayList<String> below_ = new ArrayList<String>();
						// 数组0代表task开始时间，1代表task结束时间，2代表dagid，3代表id
						// 任务所属dag 任务编号
						below_.add(TASKInPe.get(0)[2] + " "
								+ TASKInPe.get(0)[3] + " " + 0);
						tem.setPEId(i);
						tem.setslotId(Slotcount);
						tem.setslotstarttime(submit);
						tem.setslotfinishtime(TASKInPe.get(0)[0]);
						tem.setbelow(below_);
						slotListinpe.add(tem);
						Slotcount++;
					} else {
						Slot tem = new Slot();
						ArrayList<String> below_ = new ArrayList<String>();
						// 数组0代表task开始时间，1代表task结束时间，2代表dagid，3代表id
						// 任务所属dag 任务编号
						below_.add(TASKInPe.get(0)[2] + " "
								+ TASKInPe.get(0)[3] + " " + 0);
						tem.setPEId(i);
						tem.setslotId(Slotcount);
						tem.setslotstarttime(submit);
						tem.setslotfinishtime(TASKInPe.get(0)[0]);
						tem.setbelow(below_);
						slotListinpe.add(tem);
						Slotcount++;
						// ===================================================
						Slot temp = new Slot();
						// 数组0代表task开始时间，1代表task结束时间，2代表dagid，3代表id
						temp.setPEId(i);
						temp.setslotId(Slotcount);
						temp.setslotstarttime(TASKInPe.get(0)[1]);
						temp.setslotfinishtime(deadline);
						slotListinpe.add(temp);
						Slotcount++;

					}
				} else if (submit <= TASKInPe.get(0)[1]
						&& deadline > TASKInPe.get(0)[1]) {
					Slot tem = new Slot();
					// 数组0代表task开始时间，1代表task结束时间，2代表dagid，3代表id
					tem.setPEId(i);
					tem.setslotId(Slotcount);
					tem.setslotstarttime(TASKInPe.get(0)[1]);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);
					Slotcount++;
				} else if (submit > TASKInPe.get(0)[1]
						&& deadline > TASKInPe.get(0)[1]) {
					Slot tem = new Slot();
					// 数组0代表task开始时间，1代表task结束时间，2代表dagid，3代表id
					tem.setPEId(i);
					tem.setslotId(Slotcount);
					tem.setslotstarttime(submit);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);
					Slotcount++;
				}
			} else {// 该处理器上有多个任务运行

				// 获取处理器上原本所在各任务间的执行空闲片段
				// 这里是处理上所有的空闲，还没有算上submit和deadline的限制

				// 如果处理器上第一个任务的开始时间不是为0，最开头有一段空隙
				if (TASKInPe.get(0)[0] >= 0) {
					Slot tem = new Slot();
					ArrayList<String> below_ = new ArrayList<String>();
					for (int k = 0; k < TASKInPe.size(); k++) {
						// 数组0代表task开始时间，1代表task结束时间，2代表dagid，3代表id
						// 任务所属的业务id 任务标号
						below_.add(TASKInPe.get(k)[2] + " "
								+ TASKInPe.get(k)[3] + " " + 0);
					}
					tem.setPEId(i);
					tem.setslotId(Slotcount);
					// System.out.println("����Ӧ����0�Ŷ�"+Slotcount);
					tem.setslotstarttime(0);
					tem.setslotfinishtime(TASKInPe.get(0)[0]);
					tem.setbelow(below_);
					slotListinpe_ori.add(tem);
					Slotcount++;
				}

				/**
				 * 在第一个的FIFO调度算法中会出现问题，但是只会在第一个中出现，可以忽略不计较
				 */
				for (int j = 1; j < TASKInPe.size(); j++) {
					if (TASKInPe.get(j - 1)[1] <= TASKInPe.get(j)[0]) {
						Slot tem = new Slot();
						ArrayList<String> below_ = new ArrayList<String>();
						for (int k = j; k < TASKInPe.size(); k++) {
							// 数组0代表task开始时间，1代表task结束时间，2代表dagid，3代表id
							// 任务所属的业务id 任务标号 在哪个空闲块的后面
							below_.add(TASKInPe.get(k)[2] + " "+ TASKInPe.get(k)[3] + " " + j);
						}
						tem.setPEId(i);
						tem.setslotId(Slotcount);
						tem.setslotstarttime(TASKInPe.get(j - 1)[1]);
						tem.setslotfinishtime(TASKInPe.get(j)[0]);
						tem.setbelow(below_);
						slotListinpe_ori.add(tem);
						Slotcount++;
					}

				}

				// 计算在当前dag开始时间到截止时间之间 的slot。
				// 计算能容纳当前DAG的起始slot的编号
				int startslot = 0;
				for (int j = 0; j < slotListinpe_ori.size(); j++) {
					Slot tem = new Slot();
					tem = slotListinpe_ori.get(j);

					if (j == 0 && (tem.slotstarttime != tem.slotfinishtime)) {
						if (submit >= 0 && submit < tem.slotfinishtime) {
							startslot = 0;
							tem.setslotstarttime(submit);
							break;
						}
					} else if (j > 0 && j <= (slotListinpe_ori.size() - 1)) {
						if (tem.getslotstarttime() <= submit // --slotstarttime--submit--slotfinishtime--
								&& tem.getslotfinishtime() > submit) {
							tem.setslotstarttime(submit);
							startslot = j;
							break;
						} else if (tem.getslotstarttime() > submit // slotfinishtime(��һ��slot)--submit---slotstarttime
								&& slotListinpe_ori.get(j - 1)
										.getslotfinishtime() <= submit) {
							startslot = j;
							break;
						}
					}

					// 如果到达时间在前面的slot都没办法匹配插入，则放在处理器最后.
					if (j == (slotListinpe_ori.size() - 1))
						startslot = slotListinpe_ori.size();
				}

				// 设置slotListinpe内容，这里面是筛选过的，时间上能匹配submit----deadline时间段的slot的集合
				/**
				 * 讲道理这里应该没有问题，因为会计算每个处理器上的空闲时间
				 * 但讲道理应该是只要计算一轮就好了，但是为什么会计算好几轮，可能是因为在调度算法中就绪列表里面没有用到这个作业
				 * ，等下一次计算的时候又计算了一轮
				 */
				int count = 0;
				for (int j = startslot; j < slotListinpe_ori.size(); j++) {
					Slot tem = new Slot();
					tem = slotListinpe_ori.get(j);

					if (tem.getslotfinishtime() <= deadline) {
						tem.setslotId(count);
						slotListinpe.add(tem);
						count++;
					} else if (tem.getslotfinishtime() > deadline
							&& tem.getslotstarttime() < deadline) {// ---slotstarttime---deadline---slotfinishtime---
						tem.setslotId(count);
						tem.setslotfinishtime(deadline);
						slotListinpe.add(tem);
						break;
					}
				}

				// 设置最后一个空闲块
				if (TASKInPe.get(TASKInPe.size() - 1)[1] <= submit) {
					Slot tem = new Slot();
					tem.setPEId(i);
					tem.setslotId(count);
					tem.setslotstarttime(submit);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);
					// System.out.println("应该是只有一个空闲块的"+slotListinpe.size());
				} else if (TASKInPe.get(TASKInPe.size() - 1)[1] < deadline
						&& TASKInPe.get(TASKInPe.size() - 1)[1] > submit) {
					Slot tem = new Slot();
					tem.setPEId(i);
					tem.setslotId(count);
					tem.setslotstarttime(TASKInPe.get(TASKInPe.size() - 1)[1]);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);
				}
			}
			SlotListInPes.put(i, slotListinpe);
		}

	}

	/**
	 * 
	 * @Title: changeinpe
	 * @Description: 在调度之后修改slotlistinpe后的below
	 * @param @param slotlistinpe
	 * @param @param inpe
	 * @return void
	 * @throws
	 */
	public static void changeinpe(ArrayList<Slot> slotlistinpe, int inpe) {
		ArrayList<String> below = new ArrayList<String>();

		for (int i = 0; i < slotlistinpe.size(); i++) {
			ArrayList<String> belowte = new ArrayList<String>();
			Slot slottem = slotlistinpe.get(i);
			for (int j = 0; j < slottem.getbelow().size(); j++) {
				below.add(slottem.getbelow().get(j));
			}
			String belowbuf[] = below.get(0).split(" ");
			// 在某个空闲块后面，这个空闲块的编号
			int buffer = Integer.valueOf(belowbuf[2]).intValue();
			if (buffer >= inpe) {
				buffer += 1;
				for (int j = 0; j < below.size(); j++) {
					String belowbuff = belowbuf[0] + " " + belowbuf[1] + " "+ buffer;
					belowte.add(belowbuff);
				}
				slottem.getbelow().clear();
				slottem.setbelow(belowte);
			}
		}
	}

	/**
	 * 
	 * @Title: printDagMap
	 * @Description: 打印作业的调度结果至文件中
	 * @param dagmap
	 * @throws IOException
	 *             :
	 * @throws
	 */
	private static void printDagMap(DAG dagmap) throws IOException {
		FileWriter writer = new FileWriter("G:\\dagmap.txt", true);
		DAG tempTestJob = dagmap;
		int dagid = tempTestJob.DAGId;
		int num = tempTestJob.tasknumber;

		for (int o = 0; o < num; o++) {
			Task tempDag = getTaskByDagIdAndTaskId(dagid, o);
			ArrayList<Integer> pre = tempDag.getpre();

			for (int p : pre) {
				Task tempPre = getTaskByDagIdAndTaskId(dagid, p);
				if (tempPre.getfillbackstarttime() > tempDag
						.getfillbackstarttime())
					writer.write("DAG的id=" + dagid + "；父任务id=" + p + ";开始时间："
							+ tempPre.getfillbackstarttime() + ";子任务id=" + o
							+ ";子任务开始时间：" + tempDag.getfillbackstarttime()
							+ "\n");
			}
		}
		if (writer != null) {
			writer.close();
		}

	}

	/**
	 * @Description: 判断后移空闲块后的负载能否使子任务成功放入空闲块
	 * 
	 * @param dagmap
	 *            ，DAG包括DAG中各个子任务，以及DAG中任务间依赖关系
	 * @param readylist
	 *            ，readylist就绪队列
	 * 
	 * @return isslide，能否放入
	 * @throws IOException 
	 */
	public static boolean scheduling(DAG dagmap, ArrayList<Task> readylist) throws IOException {
		boolean findsuc = true;// 本DAG能否回填成功，只要一个任务失败就是全部失败

			
			int finimintime = timewindowmax;
			int mindag = -1;
			int message[][] = new int[readylist.size()][6];
			// 0 is if success 1 means success 0 means fail,
			// 1 is earliest starttime
			// 2 is peid
			// 3 is slotid
			// 4 is if need slide
			// 5 is slide length

			int[] finish = new int[readylist.size()];// 该任务的执行结束时间

			// 为DAG中除第一个任务外的任务找寻可插入的slot并返回信息
			for (int i = 0; i < readylist.size(); i++) {
				Task task = new Task();
				task = readylist.get(i);
				message[i] = findslot(dagmap, task);
				finish[i] = message[i][1] + task.getts();
			}

			
			int dagId = dagmap.getDAGId();
			// 只要其中有一个任务没能回填成功，那么整个DAG失败
			for (int i = 0; i < readylist.size(); i++) {
				Task tempTaskResult = readylist.get(i);
				if (message[i][0] == 0) {
					dagResultMap[dagId][tempTaskResult.getid()] = 1;
					findsuc = false;
				}
			}
			if (findsuc == false) {
				return findsuc;
			}

			/**
			 * 此时调度方式是按照任务ID而来
			 */
			mindag=0;
			
//			for (int i = 0; i < readylist.size(); i++) {
//				if (finimintime > finish[i]) {
//					finimintime = finish[i];
//					mindag = i;
//				}
//			}

			// 找到这个执行结束时间最早的任务
			ArrayList<Task> DAGTaskList = new ArrayList<Task>();
			Task targetTask = new Task();
			for (int i = 0; i < dagmap.gettasklist().size(); i++) {
				Task task = new Task();
				DAGTaskList.add((Task) dagmap.gettasklist().get(i));
				task = (Task) dagmap.gettasklist().get(i);
				int targetTaskId = readylist.get(mindag).getid();
				if (task.getid() == targetTaskId) {
					targetTask = (Task) dagmap.gettasklist().get(i);
				}
			}
			
			// 设置这个任务fillbackstarttime等信息
			// int startmin = finimin - readylist.get(mindag).getts();
			int startmin = message[mindag][1];
			int pemin = message[mindag][2];
			int slotid = message[mindag][3];
			targetTask.setfillbackstarttime(startmin);
			targetTask.setfillbackfinishtime(startmin+targetTask.getlength());
			targetTask.setfillbackpeid(pemin);
			targetTask.setpeid(pemin);
			targetTask.setfillbackready(true);
			targetTask.setprefillbackdone(true);
			targetTask.setprefillbackdone(true);
			targetTask.setfillbackdone(true);

			LinkedHashMap<Integer, Integer[]> TASKInPe = new LinkedHashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(pemin);
			
			
			// ==================修改处理器上的调度结果，插入这个任务
						// 在要操作的空闲位置中插入任务，并后移原本就在处理器上的任务
			if (TASKInPe.size() > 0) {// 该处理器上原本有任务
				
				ArrayList<Slot> slotlistinpe = new ArrayList<Slot>();
				for (int j = 0; j < SlotListInPes.get(pemin).size(); j++)
					slotlistinpe.add((Slot) SlotListInPes.get(pemin).get(j));

				// 0 is if success 1 means success 0 means fail,
				// 1 is earliest starttime
				// 2 is peid
				// 3 is slotid��������������п����������������ϵ�id
				// 4 is if need slide
				// 5 is slide length
				ArrayList<String> below = new ArrayList<String>();

				Slot slottem = new Slot();
				for (int i = 0; i < slotlistinpe.size(); i++) {
					if (slotlistinpe.get(i).getslotId() == slotid) {
						slottem = slotlistinpe.get(i);
						break;
					}
				}

				for (int i = 0; i < slottem.getbelow().size(); i++) {
					below.add(slottem.getbelow().get(i));
				}

				if (below.size() > 0) {// 如果这个slot后面原本有任务
					String buf[] = below.get(0).split(" ");
					//空闲块的总序号
					int inpe = Integer.valueOf(buf[2]).intValue();
					// 后移后续的任务
					for (int i = TASKInPe.size(); i > inpe; i--) {
						Integer[] st_fitemp = new Integer[4];
						st_fitemp[0] = TASKInPe.get(i - 1)[0];
						st_fitemp[1] = TASKInPe.get(i - 1)[1];
						st_fitemp[2] = TASKInPe.get(i - 1)[2];
						st_fitemp[3] = TASKInPe.get(i - 1)[3];
						TASKInPe.put(i, st_fitemp);
					}

					Integer[] st_fi = new Integer[4];
					st_fi[0] = startmin;
					st_fi[1] = startmin + targetTask.getlength();
					st_fi[2] = targetTask.getid();
					st_fi[3] = targetTask.getid();
					TASKInPe.put(inpe, st_fi);

					/**
					 * 设置isfillback 使这个任务跳过松弛这一步骤 证明这个任务是插在其它作业之间的
					 */
					targetTask.setisfillback(true);
					// 改变空闲块的的below
					changeinpe(slotlistinpe, inpe);

				} else {// 如果这个slot后面原本没有任务
					Integer[] st_fi = new Integer[4];
					st_fi[0] = startmin;
					st_fi[1] = startmin + targetTask.getlength();
					st_fi[2] = targetTask.getid();
					st_fi[3] = targetTask.getid();
					TASKInPe.put(TASKInPe.size(), st_fi);
				}
			} else {// 该处理器上原本没有任务
				Integer[] st_fi = new Integer[4];
				st_fi[0] = startmin;
				st_fi[1] = startmin + targetTask.getlength();
				st_fi[2] = targetTask.getid();
				st_fi[3] = targetTask.getid();
				TASKInPe.put(0, st_fi);
			}
			
			TASKListInPes.put(pemin, TASKInPe);
			// 重新计算空闲块列表
			computeSlot(dagmap.getsubmittime(), dagmap.getDAGdeadline());
			// mindag是个就绪队列中的标记
			
			readylist.remove(mindag);
			
		return findsuc;
	}

	/**
	 * @Description: 判断DAG中其余节点能否找到空闲时间段放入，如果能则返回相应的信息
	 * 
	 * @param dagmap
	 *            ，DAG包括DAG中各个子任务，以及DAG中任务间依赖关系
	 * @param curTask
	 *            ，DAG中其余TASK中的一个
	 * @return message，0 is if success(1 means success 0 means fail), 1 is
	 *         earliest start time, 2 is peid, 3 is slotid
	 */
	public static int[] findslot(DAG dagmap, Task curTask) {
		int message[] = new int[6];

		boolean findsuc = false;
		int startmin = timewindowmax;
		int finishmin = timewindowmax;
		int diffmin = timewindowmax;
		int pemin = -1;
		int slide;
		int[] startinpe = new int[pe_number]; // 在处理器i上开始执行的时间
		int[] slotid = new int[pe_number]; // 空闲块在处理器i上的编号
		int[] isneedslide = new int[pe_number];  // 0 means don't need 1 means
												// need slide
		int[] slidelength = new int[pe_number];// 在处理器i上需要滑动的长度
		int[] diff = new int[pe_number];

		/**
		 * 归0
		 */
		for (int k = 0; k < pe_number; k++) {
			pushFlag[k] = 0;
		}

		Map<String, Double> DAGTaskDependValue = new LinkedHashMap<String, Double>();
		DAGTaskDependValue = dagmap.getdependvalue();

		// 获取父任务集合
		ArrayList<Task> pre_queue = new ArrayList<Task>();
		ArrayList<Integer> pre = new ArrayList<Integer>();
		pre = curTask.getpre();
		if (pre.size() > 0) {
			for (int j = 0; j < pre.size(); j++) {
				Task buf = new Task();
				buf = getTaskByDagIdAndTaskId(curTask.getdagid(), pre.get(j));
				if (!buf.fillbackdone && !buf.fillbackready) {
					message[0] = 0;
					System.out.println("他的父节点没能完成");
					return message;
				}
				pre_queue.add(buf);
			}
		}

		
//		int faDone = 0;
			// 获取本任务的最早开始时间
		for (int i = 0; i < pe_number; i++) {
			int predone = 0;// 在当前处理器上当前任务最早开始执行时间
			if (pre_queue.size() == 1) {// 如果该任务只有一个父任务
				if (pre_queue.get(0).getfillbackpeid() == i) {// 与父任务在同一个处理器上
					predone = pre_queue.get(0).getfillbackfinishtime();
				} else {// 与父任务不在同一个处理器上
					int value = (int) (double) DAGTaskDependValue.get(String.valueOf(pre_queue.get(0).getid())+ " "+ String.valueOf(curTask.getid()));
					predone = pre_queue.get(0).getfillbackfinishtime() + value;
				}
			} else if (pre_queue.size() >= 1) {// 有多个父任务
				for (int j = 0; j < pre_queue.size(); j++) {
					if (pre_queue.get(j).getfillbackpeid() == i) {// 与父任务在同一个处理器上
						if (predone < pre_queue.get(j).getfillbackfinishtime()) {
							predone = pre_queue.get(j).getfillbackfinishtime();
						}
					} else {// 与父任务不在同一个处理器上，开始时间为：父任务结束时间以及资源传输时间
						int valu = (int) (double) DAGTaskDependValue.get(String.valueOf(pre_queue.get(j).getid())+ " "+ String.valueOf(curTask.getid()));
						int value = pre_queue.get(j).getfillbackfinishtime()+ valu;
						if (predone < value)
							predone = value;
					}
				}
			}

			
			startinpe[i] = -1;
			diff[i] = -1;

			ArrayList<Slot> slotlistinpe = new ArrayList<Slot>();
			// i:处理器编号
			for (int j = 0; j < SlotListInPes.get(i).size(); j++)
				slotlistinpe.add((Slot) SlotListInPes.get(i).get(j));

			LinkedHashMap<Integer, Integer[]> tempSlotInfo = new LinkedHashMap<>();
			int countSlot = 0;
			// 找寻本任务在当前处理器上插入的最早开始的空余块的相关信息
			for (int j = 0; j < SlotListInPes.get(i).size(); j++) {
				int slst = slotlistinpe.get(j).getslotstarttime();
				int slfi = slotlistinpe.get(j).getslotfinishtime();
				int slotID = slotlistinpe.get(j).getslotId();
				
				if (predone < slst) {
					if ((slst + curTask.getts()) <= slfi&& (slst + curTask.getts()) <= curTask.getdeadline()) {
						Integer[] slotInfo = new Integer[4];
						slotInfo[0] = slotID;// slotid[i]
						slotInfo[1] = slst;// startinpe[i]
						slotInfo[2] = 0;// diff[i]
						slotInfo[3] = 0;// isneedslide[i]
						tempSlotInfo.put(countSlot++, slotInfo);
					}
				} else if (predone >= slst && predone < slfi) {
					if ((predone + curTask.getts()) <= slfi&& (predone + curTask.getts()) <= curTask.getdeadline()) {
						Integer[] slotInfo = new Integer[4];
						slotInfo[0] = slotID;// slotid[i]
						slotInfo[1] = predone;// startinpe[i]
						slotInfo[2] = predone-slst;// diff[i]
						slotInfo[3] = 0;// isneedslide[i]
						tempSlotInfo.put(countSlot++, slotInfo);
					}
				}
			}
			
			
			// 找到这个处理器上空隙最小的点
			int diffTemp = timewindowmax;
			for (Entry<Integer, Integer[]> map : tempSlotInfo.entrySet()) {
				Integer[] slotInfo = map.getValue();
				if (diffTemp > slotInfo[2]) {
					diffTemp = slotInfo[2];
					slotid[i] = slotInfo[0];
					startinpe[i] = slotInfo[1];
					diff[i] = slotInfo[2];
					isneedslide[i] = slotInfo[3];
				}
			}
			// System.out.println("处理器上"+i+"上有合适的空闲id："+tempSlotInfo.size()+"\t当前的任务编号为："+dagtemp.getid());
		}
		
		for (int i = 0; i < pe_number; i++) {
			if (startinpe[i] != -1) {
				findsuc = true;
				if (diff[i] < diffmin) {
					diffmin = diff[i];
				}
			}
		}
		
		// 找出相同空隙大小的处理器,// 空隙相同的情况下取执行开始时间最早的
		ArrayList<Integer> adoptPeList = new ArrayList<>();
		for (int i = 0; i < pe_number; i++) {
			if (diffmin == diff[i]) {
				adoptPeList.add(i);
			}
		}		
		if (adoptPeList.size() > 1) {
			int minStart = Integer.MAX_VALUE;
			for (Integer peIndex : adoptPeList) {
				if (startinpe[peIndex] < minStart) {
					minStart = startinpe[peIndex];
					pemin = peIndex;
				}
			}
		} else if (adoptPeList.size() == 1) {
			pemin = adoptPeList.get(0);
		}

		// System.out.println("pemin="+pemin);
		// 0 is if success 1 means success 0 means fail,
		// 1 is earliest starttime
		// 2 is peid
		// 3 is slotid
		// 4 is if need slide
		// 5 is slide length
		if (findsuc) {
			message[0] = 1;
			message[1] = startinpe[pemin];
			message[2] = pemin;
			message[3] = slotid[pemin];
			message[4] = isneedslide[pemin];
			if (isneedslide[pemin] == 1)
				message[5] = slidelength[pemin];
			else
				message[5] = -1;
		} else {
			message[0] = 0;
		}

		return message;
	}

	/**
	 * @Description: 判断DAG中起始节点能否找到空闲时间段放入
	 * 
	 * @param dagmap
	 *            ，DAG包括DAG中各个子任务，以及DAG中任务间依赖关系
	 * @param curTask
	 *            ，起始节点
	 * @return findsuc，能否放入
	 */
	public static boolean findfirsttaskslot(DAG dagmap, Task curTask) {
		// perfinish is the earliest finish time minus task'ts time, the
		// earliest start time

		boolean findsuc = false;
		int startmin = timewindowmax;
		int finishmin = 0;
		int pemin = -1;
		int slide;
		int[] startinpe = new int[pe_number];// 在处理器i上开始执行的最早时间
		int[] slotid = new int[pe_number];// 如果在处理器i上执行，本任务插入的slot的id
		// int[] slidinpe = new int[pe_number];//如果在处理器i上执行，本任务插入的slot的id

		// 遍历所有处理器
		for (int i = 0; i < pe_number; i++) {
			
			startinpe[i] = -1;
			ArrayList<Slot> slotlistinpe = new ArrayList<Slot>();
			for (int j = 0; j < SlotListInPes.get(i).size(); j++)
				slotlistinpe.add((Slot) SlotListInPes.get(i).get(j));

			// 遍历本处理器上所有满足时间要求的空闲段
			for (int j = 0; j < SlotListInPes.get(i).size(); j++) {
				int slst = slotlistinpe.get(j).getslotstarttime();
				int slfi = slotlistinpe.get(j).getslotfinishtime();
				int slotId=slotlistinpe.get(j).getslotId();
				// 每个任务的到达时间初始是整个业务的提交时间。
				// 这里第一个任务的到达时间就是作业提交的时间。dagtemp.getarrive()
				if (curTask.getarrive() <= slst) {// predone<=slst
					if ((slst + curTask.getts()) <= slfi && // s1+c<f1
							(slst + curTask.getts()) <= curTask.getdeadline()) {
						startinpe[i] = slst;
						slotid[i] = slotId;
						break;
					}
				} else {// predone>slst
					if ((curTask.getarrive() + curTask.getts()) <= slfi // predone+c<f1
							&& (curTask.getarrive() + curTask.getts()) <= curTask.getdeadline()) {
						startinpe[i] = curTask.getarrive();
						slotid[i] =slotId;
						break;
					} 
				}
			}

		}
		// 找到开始时间最早的处理器
		for (int i = 0; i < pe_number; i++) {
			if (startinpe[i] != -1) {
				findsuc = true;
				// 选在开始时间最早的空闲块
				if (startinpe[i] < startmin) {
					startmin = startinpe[i];
					pemin = i;
				}
			}
		}
		
		if (findsuc == false) {
			System.out.println("任务的第一个节点没有找到合适处理器的地方插入");
		}
		// 如果至少有一个处理器上能够插入这个任务
		if (findsuc) {
			
			finishmin = startmin + curTask.getlength();
			curTask.setfillbackstarttime(startmin);
			curTask.setfillbackfinishtime(finishmin);
			curTask.setfillbackpeid(pemin);
			curTask.setfillbackready(true);

			LinkedHashMap<Integer, Integer[]> TASKInPe = new LinkedHashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(pemin);

			if (TASKInPe.size() > 0) {// 原本的处理器上有多个任务存在

				ArrayList<Slot> slotlistinpe = new ArrayList<Slot>();
				for (int j = 0; j < SlotListInPes.get(pemin).size(); j++)
					slotlistinpe.add((Slot) SlotListInPes.get(pemin).get(j));

				ArrayList<String> below = new ArrayList<String>();

				Slot slottem = new Slot();
				// 找到slotlistinpe中要插入的那个slot对象
				for (int i = 0; i < slotlistinpe.size(); i++) {
					if (slotlistinpe.get(i).getslotId() == slotid[pemin]) {
						slottem = slotlistinpe.get(i);
						break;
					}
				}

				// 得到below
				for (int i = 0; i < slottem.getbelow().size(); i++) {
					below.add(slottem.getbelow().get(i));
				}

				if (below.size() > 0) {// 要插入的位置后面有多个任务
					// 将本任务插入到对应的位置
					String buf[] = below.get(0).split(" ");
					int inpe = Integer.valueOf(buf[2]).intValue();

					// 将空闲块后的任务在处理器上的执行次序都后移一个位置
					for (int i = TASKInPe.size(); i > inpe; i--) {
						Integer[] st_fitemp = new Integer[4];
						st_fitemp[0] = TASKInPe.get(i - 1)[0];
						st_fitemp[1] = TASKInPe.get(i - 1)[1];
						st_fitemp[2] = TASKInPe.get(i - 1)[2];
						st_fitemp[3] = TASKInPe.get(i - 1)[3];
						TASKInPe.put(i, st_fitemp);
					}
					Integer[] st_fi = new Integer[4];
					st_fi[0] = startmin;
					st_fi[1] = finishmin;
					st_fi[2] = curTask.getdagid();
					st_fi[3] = curTask.getid();
					TASKInPe.put(inpe, st_fi);
					
					curTask.setisfillback(true);
					
				} else {// 要插入的位置后面没有任务，也就不需要移动
					Integer[] st_fi = new Integer[4];
					st_fi[0] = startmin;
					st_fi[1] = finishmin;
					st_fi[2] = curTask.getdagid();
					st_fi[3] = curTask.getid();
					TASKInPe.put(TASKInPe.size(), st_fi);
				}

			} else {// 如果该处理器上原本没有任务
				Integer[] st_fi = new Integer[4];
				st_fi[0] = startmin;
				st_fi[1] = finishmin;
				st_fi[2] = curTask.getdagid();
				st_fi[3] = curTask.getid();
				TASKInPe.put(TASKInPe.size(), st_fi);
			}

			// 计算新的空闲块列表
			computeSlot(dagmap.getsubmittime(), dagmap.getDAGdeadline());
			
		} else {
			return false;
		}

		return findsuc;// 返回是否找到位置插入任务
		
		

	}

	/**
	 * @Description: 判断backfilling操作能否成功, 从第二个提交任务开始操作
	 * 
	 * @param dagmap
	 *            ，DAG包括DAG中各个子任务，以及DAG中任务间依赖关系
	 * @return fillbacksuc，backfilling操作的成功与否
	 * @throws IOException 
	 */
	public static boolean fillback(DAG dagmap) throws IOException {

		int runtime = dagmap.getsubmittime();
		boolean fillbacksuc = true; // 只要有一个任务失败就是全部失败

		boolean notfini = true;  // 并不是全部的任务都执行成功
		ArrayList<Task> readylist = new ArrayList<Task>();
		ArrayList<Task> DAGTaskList = new ArrayList<Task>();

		int DAGID = dagmap.getDAGId();

		for (int i = 0; i < dagmap.gettasklist().size(); i++) {
			DAGTaskList.add((Task) dagmap.gettasklist().get(i));
		}

		while (runtime <= dagmap.getDAGdeadline() && notfini && fillbacksuc) {
			
			// 遍历所有任务，为当前时刻执行完毕的任务设置参数
			for (Task task : DAGTaskList) {
				if ((task.getfillbackstarttime() + task.getts()) == runtime
						&& task.getfillbackready()
						&& task.getfillbackdone() == false
						&& task.getprefillbackdone() == true
						&& task.getprefillbackready() == true) {
					task.setfillbackfinishtime(runtime);
					task.setfillbackdone(true);
				}
			}

			/**
			 * 
			 * 从此分为两部分 1、第一部分用于判断调度成功的的任务现在是否执行结束，若结束则设置结束时间
			 * 2、当前时刻是否有可以去调度的任务（就绪任务，有就去调度没有就略过）
			 * 
			 */
			for (Task task : DAGTaskList) {
				// =====================设置本作业的第一个任务============
				if (task.getid() == 0 && task.getfillbackready() == false) {
					if (findfirsttaskslot(dagmap, DAGTaskList.get(0))) {// 当前DAG中起始节点能找到空闲时间段放入
						DAGTaskList.get(0).setprefillbackready(true);//
						DAGTaskList.get(0).setprefillbackdone(true);

						if (task.getts() == 0) {// 如果这个任务是为了归一化而添加进去的起始节点
							task.setfillbackfinishtime(task.getfillbackstarttime());
						} else {
							task.setfillbackfinishtime(task.getfillbackstarttime() + task.getts());
						}
						task.setfillbackdone(true);
						task.setfillbackready(true);
						// 如果是单节点任务，那么就调度成功了呀~~
						if (dagmap.isSingle)
							return true;
					} else {// 起始任务插入失败
						fillbacksuc = false;
						System.out.println("DAG" + DAGID + "的第一个任务设置失败，从而整体失败");
						return fillbacksuc;
					}
				}

				// ================查询当前任务的所有父任务是否都以完成，若就绪就将当前任务加入就绪队列
				// ================查看当前时刻有没有就绪的任务加入就绪队列===========
				if (task.getfillbackdone() == false&& task.getfillbackready() == false) {
					ArrayList<Integer> pre = new ArrayList<Integer>();
					pre = task.getpre();
					if (pre.size() > 0) {
						boolean ready = true;
						for (int j = 0; j < pre.size(); j++) {
							Task buf = new Task();
							buf = getTaskByDagIdAndTaskId(task.getdagid(), pre.get(j));
							if (buf.getfillbackdone() && buf.getfillbackready()) {
								continue;
							} else {
								ready = false;
								break;
							}
						}
						if (ready) {
							// 当前任务加入就绪队列
							readylist.add(task);
							task.setprefillbackready(true);
							task.setprefillbackdone(true);
							task.setfillbackready(true);
						}
					}
				}
			}
			
			if (readylist.size() > 0) {						
				if (!scheduling(dagmap, readylist)) {
					fillbacksuc = false;
					return fillbacksuc;
				}

			}
						
			// 本作业中所有的任务都调度成功，则未完成设置为false，可以跳出循环
			notfini = false;
			// 查询当前时刻本DAG中所有的任务是否都是已经执行成功，若所有任务都成功则跳出时间循环
			// 本作业中有没调度成功的任务。
			for (Task task : DAGTaskList) {
				if (task.getfillbackdone() == false) {
					notfini = true;
					break;
				}
			}

			runtime = runtime + T;

		}

		// 完整的设置整个作业的回填结束时间
		if (!notfini) {
			for (Task task : DAGTaskList) {
				task.setfillbackfinishtime(task.getfillbackstarttime()+ task.getts());
			}
		} else {
			fillbacksuc = false;
			System.out.println("本DAG为" + dagmap.getDAGId()+ ",这里退出的理由是：时间已经过期了，但是作业中还有没有被调度完成的任务");
		}
		return fillbacksuc;
	}

	
	


	/**
	 * @throws Exception 
	 * @throws IOException
	 * 
	 * @Title: scheduleOtherDAG
	 * @Description: 使用Backfilling调度第i个DAG，若调度成功，进行LevelRelaxing操作，
	 *               并且修改TASKListInPes中各个TASK的开始结束时间，若调度不成功，取消该DAG的执行
	 * @param @param i，DAG的ID
	 * @param @param SlotListInPestemp，用于还原的SlotListInPes
	 * @param @param TASKListInPestemp，用于还原的TASKListInPes
	 * @return void
	 * @throws
	 */
	public static void restoreSlotandTASK(
			LinkedHashMap<Integer, ArrayList> SlotListInPestemp,
			LinkedHashMap<Integer, LinkedHashMap> TASKListInPestemp) {

		SlotListInPes.clear();
		TASKListInPes.clear();

		for (int k = 0; k < SlotListInPestemp.size(); k++) {
			ArrayList<Slot> slotListinpe = new ArrayList<Slot>();
			for (int j = 0; j < SlotListInPestemp.get(k).size(); j++) {
				Slot slottemp = new Slot();
				slottemp = (Slot) SlotListInPestemp.get(k).get(j);
				slotListinpe.add(slottemp);
			}
			SlotListInPes.put(k, slotListinpe);
		}
		for (int k = 0; k < TASKListInPestemp.size(); k++) {
			LinkedHashMap<Integer, Integer[]> TASKInPe = new LinkedHashMap<Integer, Integer[]>();
			for (int j = 0; j < TASKListInPestemp.get(k).size(); j++) {
				Integer[] temp = new Integer[4];
				temp = (Integer[]) TASKListInPestemp.get(k).get(j);
				TASKInPe.put(j, temp);
			}

			TASKListInPes.put(k, TASKInPe);
		}
		// repairTaskList();
	}

	/**
	 * @throws Exception 
	 * @throws IOException
	 * 
	 * @Title: scheduleOtherDAG
	 * @Description: 使用Backfilling调度第i个DAG，若调度成功，进行LevelRelaxing操作，
	 *               并且修改TASKListInPes中各个TASK的开始结束时间，若调度不成功，取消该DAG的执行
	 * @param @param i，DAG的ID
	 * @param @param SlotListInPestemp，用于还原的SlotListInPes
	 * @param @param TASKListInPestemp，用于还原的TASKListInPes
	 * @return void
	 * @throws
	 */
	public static void scheduleOtherDAG(int i,LinkedHashMap<Integer, ArrayList> SlotListInPestemp,LinkedHashMap<Integer, LinkedHashMap> TASKListInPestemp) throws Exception {

		int arrive = DAGMapList.get(i).getsubmittime();
		if (arrive > current_time)
			current_time = arrive;
		// 判断本DAG的backfilling操作能否成功
		boolean fillbacksuc = fillback(DAGMapList.get(i));
		//System.out.println("===============当前作业+" + i + "是否调度成功：" + fillbacksuc);

		// 如果不成功
		if (!fillbacksuc) {
			// 修复调度之前的样貌
			restoreSlotandTASK(SlotListInPestemp, TASKListInPestemp);

			DAGMapList.get(i).setfillbackdone(false);
			// 整个作业忽略
			DAGMapList.get(i).setfillbackpass(true);

			// 设置作业中所有任务都为忽略（pass）
			ArrayList<DAG> DAGTaskList = new ArrayList<DAG>();
			for (int j = 0; j < DAGMapList.get(i).gettasklist().size(); j++) {
				DAGTaskList.add((DAG) DAGMapList.get(i).gettasklist().get(j));
				DAGTaskList.get(j).setfillbackpass(true);
			}

		} else {// 如果本DAG的backfilling操作成功
			DAGMapList.get(i).setfillbackdone(true);
			DAGMapList.get(i).setfillbackpass(false);		
			//printTaskAndSlot(1);
			repairTaskList();
		}

		// System.out.println("===============第一个会不会成功："+DAGMapList.get(0).getfillbackdone());
	}

	/**
	 * 
	 * @Title: repairTaskList
	 * @Description: 修正TaskList中的时间问题
	 * @throws
	 */
	private static void repairTaskList() {

		for (int k = 0; k < TASKListInPes.size(); k++) {

			LinkedHashMap<Integer, Integer[]> TASKInPe = TASKListInPes.get(k);
			for (int j = 0; j < TASKInPe.size(); j++) {
				Integer[] temp = new Integer[4];
				temp = TASKInPe.get(j);

				Task tempDag = new Task();
				tempDag = getTaskByDagIdAndTaskId(temp[2], temp[3]);

				temp[0] = tempDag.getfillbackstarttime();
				temp[1] = tempDag.getfillbackfinishtime();
				TASKInPe.put(j, temp);
			}
			TASKListInPes.put(k, TASKInPe);
		}

	}

	/**
	 * 
	 * @Title: printTest
	 * @Description: 测试打印
	 * @param i
	 * @throws IOException
	 *             :
	 * @throws
	 */
	public static void printRelation(int i) throws IOException {

		FileWriter writer = new FileWriter("G:\\x.txt", true);
		DAG tempTestJob = DAGMapList.get(i);
		int dagid = tempTestJob.DAGId;
		int num = tempTestJob.tasknumber;
		for (int o = 0; o < num; o++) {
			Task tempDag = getTaskByDagIdAndTaskId(dagid, o);
			ArrayList<Integer> pre = tempDag.getpre();
			for (int p : pre) {
				Task tempPre = getTaskByDagIdAndTaskId(dagid, p);
				// if(tempPre.getfillbackstarttime()>tempDag.getfillbackstarttime())
				writer.write("DAG的id=" + i + "；父任务id=" + p + ";开始时间："
						+ tempPre.getfillbackstarttime() + ";子任务id=" + o
						+ ";子任务开始时间：" + tempDag.getfillbackstarttime() + "\n");
			}
		}
		if (writer != null) {
			writer.close();
		}
	}

	/**
	 * 
	 * @Title: copySlot
	 * @Description: 
	 *               保存现在的SlotListInPes（里面放的是所有处理器各自匹配当前DAG的subimit--deadline时间段的slot
	 *               ），用于还原
	 * @param @return
	 * @return LinkedHashMap，SlotListInPestemp
	 * @throws
	 */
	public static LinkedHashMap copySlot() {
		LinkedHashMap<Integer, ArrayList> SlotListInPestemp = new LinkedHashMap<Integer, ArrayList>();

		for (int k = 0; k < SlotListInPes.size(); k++) {

			ArrayList<Slot> slotListinpe = new ArrayList<Slot>();

			for (int j = 0; j < SlotListInPes.get(k).size(); j++) {
				Slot slottemp = new Slot();
				slottemp = (Slot) SlotListInPes.get(k).get(j);
				slotListinpe.add(slottemp);
			}

			SlotListInPestemp.put(k, slotListinpe);
		}

		return SlotListInPestemp;
	}

	/**
	 * 
	 * @Title: copyTASK
	 * @Description: 保存现在的TASKListInPes，用于还原
	 * @param @return
	 * @return LinkedHashMap
	 * @throws
	 */
	public static LinkedHashMap copyTASK() {
		LinkedHashMap<Integer, LinkedHashMap> TASKListInPestemp = new LinkedHashMap<Integer, LinkedHashMap>();

		for (int k = 0; k < TASKListInPes.size(); k++) {
			LinkedHashMap<Integer, Integer[]> TASKInPe = new LinkedHashMap<Integer, Integer[]>();
			for (int j = 0; j < TASKListInPes.get(k).size(); j++) {
				Integer[] temp = new Integer[4];
				temp = (Integer[]) TASKListInPes.get(k).get(j);
				TASKInPe.put(j, temp);
			}
			TASKListInPestemp.put(k, TASKInPe);
		}

		return TASKListInPestemp;
	}


	
	



	// ==================��ʼ������DAGxml�е�����=================
	// ==================��ʼ������DAGxml�е�����=================
	// ==================��ʼ������DAGxml�е�����=================
	// ==================��ʼ������DAGxml�е�����=================
	// ==================��ʼ������DAGxml�е�����=================
	// ==================��ʼ������DAGxml�е�����=================

	/**
	 * @Description:创建DAGMAP实例并初始化
	 * 
	 * @param dagdepend
	 *            ，工作流依赖关系
	 * @param vcc
	 *            ，计算能力
	 */
	public static void initdagmap(DAGDepend dagdepend, PEComputerability vcc,String pathXML) throws Throwable {
		int pre_exist = 0;
	
		File file = new File(pathXML);
		String[] fileNames = file.list();
		
		/**
		 * 这里改为-2，因为后以前生成的deadlineOld的文件，就是删除不了，真是生气哦。
		 */
		int num = fileNames.length - 2;	
		BufferedReader bd = new BufferedReader(new FileReader(pathXML+ "Deadline.txt"));
		String buffered;

		for (int i = 0; i < num; i++) {
			// 每个DAG有一个dagmap
			DAG dagmap = new DAG();
			DAGDepend dagdepend_persional = new DAGDepend();
			
			Task_queue_personal.clear();

			// 获取DAG的arrivetime和deadline，task个数
			buffered = bd.readLine();
			String bufferedA[] = buffered.split(" ");
			int buff[] = new int[4];

			buff[0] = Integer.valueOf(bufferedA[0].split("dag")[1]).intValue();// dagID
			buff[1] = Integer.valueOf(bufferedA[1]).intValue();// tasknum
			buff[2] = Integer.valueOf(bufferedA[2]).intValue();// arrivetime
			buff[3] = Integer.valueOf(bufferedA[3]).intValue();// deadline
			int deadline = buff[3];
			// 其中也是包含这些以前加上去的0.0.0的节点
			int tasknum = buff[1];
			// 标记本作业是否是单任务作业
			if (tasknum == 1)
				dagmap.setSingle(true);

			taskTotal = taskTotal + tasknum;
			int arrivetime = buff[2];

			// 对每个DAG创建其任务间的相关依赖以及基本信息
			pre_exist = initDAG_createDAGdepend_XML(i, pre_exist, tasknum,arrivetime, pathXML);

			vcc.setComputeCostMap(ComputeCostMap);
			vcc.setAveComputeCostMap(AveComputeCostMap);

			dagdepend_persional.setDAGList(Task_queue_personal);
			dagdepend_persional.setDAGDependMap(DAGDependMap_personal);
			dagdepend_persional.setDAGDependValueMap(DAGDependValueMap_personal);

			// 自后往前计算DAG中每个任务的截止时间
			
			//createDeadline_XML(deadline, dagdepend_persional);

			// 为DAG_queue中的数据设置截止时间
			int number_1 = Tasks_queue.size();
			int number_2 = Task_queue_personal.size();
			for (int k = 0; k < number_2; k++) {
				Tasks_queue.get(number_1 - number_2 + k).setdeadline(Task_queue_personal.get(k).getdeadline());
				//Tasks_queue.get(number_1 - number_2 + k).setdeadline(Task_queue_personal.get(k).getSlotDeadLine());
			}

			dagmap.settasknumber(tasknum);
			dagmap.setDAGId(i);
			dagmap.setDAGdeadline(deadline);
			dagmap.setsubmittime(arrivetime);
			dagmap.settasklist(Task_queue_personal);
			dagmap.setDAGDependMap(DAGDependMap_personal);
			dagmap.setdependvalue(DAGDependValueMap_personal);
			
			createDeadline(deadline, dagdepend_persional, dagmap);
			
			//System.out.println("初始化的内容");
			DAGMapList.add(dagmap);
			DAGDependList.add(dagdepend_persional);
			

			
		}

		//
//		System.out.println("被合并的作业的原始id集合：" + mergeId.size()
//				+ "\t合并的大作业中每个小作业的最后一个任务的集合：" + mergeDAGEndNode.size()
//				+ "\t合并的大作业中，成功调度的原始作业id：" + successMergeJob.size());

		// 所有输入DAGxml文件构成的
		dagdepend.setdagmaplist(DAGMapList);
		dagdepend.setDAGList(Tasks_queue);
		dagdepend.setDAGDependMap(DAGDependMap);
		dagdepend.setDAGDependValueMap(DAGDependValueMap);
	}

	
	/**
	 * 
	 * @Title: printInitDagMap
	 * @Description: 打印初始化生成的所有DAG的内容
	 * @param i
	 * @throws IOException
	 *             :
	 * @throws
	 */
	public static void printInitDagMap(int i) throws IOException {
		FileWriter writer = new FileWriter("G:\\DAGMapList.txt", true);
		DAG tempTestJob = DAGMapList.get(i);

		System.out.println("当前作业的任务数目==========>:"+ tempTestJob.gettasknumber() + "\t编号：" + i);

		int dagid = tempTestJob.DAGId;
		int mergeDagIndex = i;

		int num = tempTestJob.tasknumber;

		for (int o = 0; o < num; o++) {
			// DAG tempDag = getMergeDAGById(mergeDagIndex, o);
			Task tempDag = getTaskByDagIdAndTaskId(mergeDagIndex, o);
			writer.write("作业编号：" + tempDag.getdagid() + ":" + tempDag.getid()
			+ "\t到达时间：" + tempDag.getarrive() + "\t结束时间："
			+ tempDag.getdeadline() + "\t原DAG编号："
			+ tempDag.getOriDAGID() + ":" + tempDag.getOriTaskId() + "\n");
		}
		if (writer != null) {
			writer.close();
		}

	}

	// ===============================================================

	/**
	 * @Description:根据DAX文件为DAG添加相互依赖关系。更新创建了DAG_queue_personal、DAG_queue、AveComputeCostMap、ComputeCostMap、DAGDependValueMap_personal、DAGDependValueMap、DAGDependMap_personal、DAGDependMap
	 * 
	 * @param i
	 *            ，DAGID
	 * @param preexist
	 *            ，将所有的工作流中子任务全部添加到一个队列，在本DAG前已有preexist个任务
	 * @param tasknumber
	 *            ，DAG中任务个数
	 * @param arrivetimes
	 *            ，DAG到达时间
	 * @return back，将所有的工作流中子任务全部添加到一个队列，在本DAG全部添加后，有back个任务
	 */
	@SuppressWarnings("rawtypes")
	private static int initDAG_createDAGdepend_XML(int i, int preexist,int tasknumber, int arrivetimes, String pathXML)
			throws NumberFormatException, IOException, JDOMException {

		int back = 0;
		DAGDependMap_personal = new LinkedHashMap<Integer, Integer>();
		DAGDependValueMap_personal = new LinkedHashMap<String, Double>();
		ComputeCostMap = new LinkedHashMap<Integer, int[]>();
		AveComputeCostMap = new LinkedHashMap<Integer, Integer>();

		// 获取XML解析器
		SAXBuilder builder = new SAXBuilder();
		// 获取document对象
		Document doc = builder.build(pathXML + "/dag" + (i + 1) + ".xml");
		// 获取根节点
		Element adag = doc.getRootElement();

		for (int j = 0; j < tasknumber; j++) {
			Task task = new Task();
			Task task_persional = new Task();

			task.setid(Integer.valueOf(preexist + j).intValue());
			// 为每个任务都设置上所属DAG的到达时间
			task.setarrive(arrivetimes);
			task.setdagid(i);
			task_persional.setid(Integer.valueOf(j).intValue());
			task_persional.setarrive(arrivetimes);
			task_persional.setdagid(i);

			XPath path = XPath.newInstance("//job[@id='" + j + "']/@tasklength");
			List list = path.selectNodes(doc);
			Attribute attribute = (Attribute) list.get(0);
			// x：任务的长度
			int x = Integer.valueOf(attribute.getValue()).intValue();
			task.setlength(x);
			task.setts(x);
			task_persional.setlength(x);
			task_persional.setts(x);

			if (j == tasknumber - 1) {
				task.setislast(true);
				islastnum++;
			}

			Tasks_queue.add(task); // 所有DAG的任务列表
			Task_queue_personal.add(task_persional); // 当前DAG（一个）的自有任务列表

			int sum = 0;
			int[] bufferedDouble = new int[PEList.size()];
			for (int k = 0; k < PEList.size(); k++) { // x：任务的长度
				bufferedDouble[k] = Integer.valueOf(x/ PEList.get(k).getability());
				sum = sum + Integer.valueOf(x / PEList.get(k).getability());
			}
			ComputeCostMap.put(j, bufferedDouble); // 当前任务在每个处理器上的处理开销
			AveComputeCostMap.put(j, (sum / PEList.size())); // 当前任务在所有处理器上的平均处理开销
		}

		XPath path1 = XPath.newInstance("//uses[@link='output']/@file");
		List list1 = path1.selectNodes(doc);
		for (int k = 0; k < list1.size(); k++) {
			Attribute attribute1 = (Attribute) list1.get(k);
			String[] pre_suc = attribute1.getValue().split("_");

			int[] presuc = new int[2];
			presuc[0] = Integer.valueOf(pre_suc[0]).intValue() + preexist;
			presuc[1] = Integer.valueOf(pre_suc[1]).intValue() + preexist;

			XPath path2 = XPath.newInstance("//uses[@file='"+ attribute1.getValue() + "']/@size");
			List list2 = path2.selectNodes(doc);
			Attribute attribute2 = (Attribute) list2.get(0);
			int datasize = Integer.valueOf(attribute2.getValue()).intValue();

			DAGDependMap.put(presuc[0], presuc[1]); // 所有DAG的任务的依赖映射，最终结果放的是这个任务最后一个子任务
			DAGDependValueMap.put((presuc[0] + " " + presuc[1]),(double) datasize);
			Tasks_queue.get(presuc[0]).addToSuc(presuc[1]);
			Tasks_queue.get(presuc[1]).addToPre(presuc[0]);

			DAGDependMap_personal.put(Integer.valueOf(pre_suc[0]).intValue(),Integer.valueOf(pre_suc[1]).intValue());
			DAGDependValueMap_personal.put((pre_suc[0] + " " + pre_suc[1]),(double) datasize);

			int tem0 = Integer.parseInt(pre_suc[0]);
			int tem1 = Integer.parseInt(pre_suc[1]);
			Task_queue_personal.get(tem0).addToSuc(tem1);
			Task_queue_personal.get(tem1).addToPre(tem0);

		}

		// for(Entry<Integer, Integer> map:DAGDependMap_personal.entrySet()){
		// System.out.println("======>dag:"+i+"\t"+map.getKey()+":"+map.getValue());
		// }
		// System.out.println("DAGDependValueMap_personal="+DAGDependValueMap_personal.size());
		//
		back = preexist + tasknumber;
		return back;
	}

	
	/**
	 * 
	* @Title: createDeadline
	* @Description: 依据关键路径求取每个任务的截止时间
	* @param dead_line
	* @param dagdepend_persion
	* @param dagmap
	* @throws Throwable:
	* @throws
	 */
	private static void createDeadline(int dead_line,DAGDepend dagdepend_persion, DAG dagmap) throws Throwable {

		int max;
		//计算关键路径，求得其每层分得的均ֵ
		int avgDiff=getRelaxDeadline(dead_line,dagdepend_persion,dagmap);		
		Map<String, Double> transferValueMap=dagdepend_persion.getDAGDependValueMap();
		
		//从后往前推
		for (int k = Task_queue_personal.size() - 1; k >= 0; k--) {
			max = Integer.MAX_VALUE;

			int from=Task_queue_personal.get(k).getid();
			ArrayList<Integer> suc = new ArrayList<Integer>();
			suc = Task_queue_personal.get(k).getsuc();

			// 选择所有子任务的中最早的开始时间为自己的截止时间，忽略的数据的传输开销
			if (suc.size() > 0) {
				for (int j = 0; j < suc.size(); j++) {
					int tem = 0;
					Task tempChild =getTaskBytaskId(suc.get(j));// 获取这个任务的子任务
					int to=suc.get(j);
					String key=from+" "+to;
					double value=transferValueMap.get(key);
					int childExe=tempChild.getlength()+avgDiff;
					
					tem=(int) (tempChild.getdeadline()-childExe-value);
					//System.out.println("父任务："+from+"\t 子任务："+to+"的截止时间是："+tempChild.getdeadline()+"\t伸缩后："+(tempChild.getlength()+avgDiff)+"传输时间为:"+value+" \t此时的截止时间为："+tem+"\t");
					
					if (max > tem)
						max = tem;
				}		
				Task_queue_personal.get(k).setdeadline(max);

			} else {
				Task_queue_personal.get(k).setdeadline(dead_line);
			}
		}
	}
	
	//============================��ؼ�·��
	/**
	 * @return 
	 * @param dagmap 
	 * 
	* @Title: createDeadlineCriticalPath
	* @Description: 通过关键路径，构建新的deadline
	* @param dead_line
	* @param dagdepend_persion
	* @throws Throwable:
	* @throws
	 */
	private static int getRelaxDeadline(int dead_line,DAGDepend dagdepend, DAG dagmap) throws Throwable {
		int taskSize = dagmap.gettasknumber();
		ArrayList<Task> taskList=dagmap.gettasklist();
		Stack<Integer> topo = new Stack<Integer>(); // 拓扑排序的顶点栈
		int[] ve = null; //	各顶点的最早发生时间
		int[] vl = null; // 各顶点的最迟发生时间

		//求得图的拓扑排序压入栈中，并得到每个任务的最早开始时间
		ve=topologicalSort(dagmap,ve,topo,dagdepend);
		ArrayList<Integer> topoList=new ArrayList<>();
		while (!topo.isEmpty()) {
			topoList.add(topo.pop());
		}
		//将列表反序，从小到大排列
		Collections.reverse(topoList);

		//得到关键路径
		criticalPath(topoList,ve,dagmap,dagdepend);
		
		//得到整个DAG的最大层数
		int levelNum=getMaxLevelNum(topoList,dagmap,dagdepend);
		//System.out.println("原来的最大层:"+levelNum);

		//求得每个任务的平均分配冗余
		int endIndex=topoList.get(topoList.size()-1);
		Task tempTask=taskList.get(endIndex);
		int newDeadline=ve[topoList.get(topoList.size()-1)]+tempTask.getlength()+dagmap.getsubmittime();
		int deadline=dagmap.getDAGdeadline();
		
		int deadlineDiff=deadline-newDeadline;
		
		int ex=(deadlineDiff)/levelNum;
		//System.out.println(dagmap.getDAGId()+"\tnewDeadline="+newDeadline+"\tdeadline="+deadline+"\tdeadlineDiff="+deadlineDiff+"\tlevelNum="+levelNum+"\tdeadline_ex="+ex);
		return ex;
		
	}
	
	


	/**
	 * 
	* @Title: topologicalSort
	* @Description: 获得其拓扑排序，返回的是其各个任务的最早开始执行时间
	* @param dagmap
	* @param ve
	* @param topo
	* @param dagdepend
	* @return:
	* @throws
	 */
	private static int[] topologicalSort(DAG dagmap, int[] ve,  Stack topo, DAGDepend dagdepend) {
			int count = 0;//输出顶点计数
	        int[] inDegree = findInDegree(dagmap); //求各个顶点的入度
	        int taskSize=dagmap.gettasknumber();
	        Stack<Integer> noInputTask = new Stack<Integer>(); //零入度的 顶点栈
	        
	        ArrayList<Task> taskList=new ArrayList<>();
			taskList=dagmap.gettasklist();			
			 //找到第一个入度为0的节点
	        for(int i = 0; i < taskSize; i++){
	        	if(inDegree[i] == 0){
	        		noInputTask.push(i);  //入度为0的进栈
	        	}	
	        }
	        ve = new int[taskSize];//初始化,里面存储的是各个节点的最早开始执行时间

	        Map<String,Double> valueMap=dagmap.getdependvalue();
	        while( !noInputTask.isEmpty()){
	        	//当前的任务
	            int currentTask = (Integer) noInputTask.pop();
	            Task curDag=taskList.get(currentTask);
	            topo.push(currentTask); //i号顶点入T栈并计数 
	            
	            for(int m=0;m<taskSize;m++){
	            	
	            	Task tempDag=taskList.get(m);
	            	ArrayList<Integer> parents=tempDag.getpre();
	            	
	            	//当前删除的任务是这个任务的父任务
	            	if(parents.contains(currentTask)){
	            		inDegree[m]--;
	            		if(inDegree[m]==0){
	            			noInputTask.push(m);//当前的任务没有入度
	            		}
	            		String key=currentTask+" "+m;
	            		Double value=valueMap.get(key)+curDag.getlength();//得加上执行时间
	            		 if(ve[currentTask] + value> ve[m])
	 	                    ve[m] = (int) (ve[currentTask] + value);
	            	}
	            }
	        } 
	        
//	        for(Entry<String, Double> map:valueMap.entrySet()){
//	        	System.out.println(""+map.getKey()+"\t距离："+map.getValue());
//	        }
	        return ve;
	}

	/**
	 * 
	* @Title: criticalPath
	* @Description: 计算其关键路径，返回关键路径上任务个数
	* @param topoList
	* @param ve
	* @param dagmap
	* @param dagdepend
	* @return
	* @throws Exception:
	* @throws
	 */
	public static int criticalPath(ArrayList<Integer> topoList, int[] ve, DAG dagmap, DAGDepend dagdepend) throws Exception{  
		int taskSize=dagmap.gettasknumber();
		ArrayList<Task> taskList=new ArrayList<>();
		taskList=dagmap.gettasklist();
		
         int[] vl = new int[taskSize];
         // 初始化各顶点事件的最迟发生时间为最后一个任务的完成时间
         for(int i = 0; i < taskSize; i++){
             vl[i] = ve[taskSize - 1]; 
         }
		Map<String, Double> valueMap = dagdepend.getDAGDependValueMap();
         for(int k=topoList.size()-1;k>=0;k--){
			int currentTask = (int) topoList.get(k);// 得到当前任务的编号

			Task tempTask = taskList.get(currentTask);
			ArrayList<Integer> childs = tempTask.getsuc();
		
			if (childs == null)
				continue;
			
			for (int p = 0; p < childs.size(); p++) {
				int childNo=childs.get(p);
				String key = currentTask + " " + childNo;
				Double value = valueMap.get(key);			
				int diff = (int) (vl[childNo] - value - tempTask.getlength());
				if ((diff) < vl[currentTask]) {
					vl[currentTask] = diff;
				}
			}
		} 
         
         int criticalNum=0;
         ArrayList<Integer> criticalNodeList=new ArrayList<>();
         for(int i = 0; i < taskSize; i++){  
        	 int ee= ve[i];
        	 int el=vl[i];
             if(ee==el){
            	 criticalNodeList.add(i);
            	// System.out.println("关键路径："+i);
            	 criticalNum++;
             }
         }         
         return criticalNum;
    } 

	
	/**
	 * @param topoList 
	 * 
	* @Title: getMaxLevelNum
	* @Description: 求取DAG的最大层数
	* @param dagmap
	* @param dagdepend
	* @return:
	* @throws
	 */
	private static int getMaxLevelNum(ArrayList<Integer> topoList, DAG dagmap, DAGDepend dagdepend) {
		
		ArrayList<Task> taskList=dagmap.gettasklist();
		int taskSize=dagmap.gettasknumber();
		int[] level=new int[taskSize];
		Map<String, Double> valueMap = dagmap.getdependvalue();
		
		for(int k=topoList.size()-1;k>=0;k--){
			int maxlevel=0;	
			int currentTask = (int) topoList.get(k);// 得到当前任务的编号	
			Task tempTask = taskList.get(currentTask);
			ArrayList<Integer> childs = tempTask.getsuc();
			
			if (childs == null){//是最后一个节点
				level[currentTask]=1;
			}else{//否则找孩子节点中
				for (int p = 0; p < childs.size(); p++) {
					int childNo=childs.get(p);
					if(maxlevel<level[childNo]){
						maxlevel=level[childNo];
					}
				}
				level[currentTask]=maxlevel+1;			
			}
		} 		
		return level[0];
	}
	
	
	
	/**
	 * 
	* @Title: findInDegree
	* @Description: 获得各个任务相应的入度
	* @param dagmap
	* @return:
	* @throws
	 */
	private static int[] findInDegree(DAG dagmap) {
		int taskSize=dagmap.gettasknumber();
		int[] indegree  = new int[taskSize];
		
		ArrayList<Task> taskList=new ArrayList<>();
		taskList=dagmap.gettasklist();
        for(int i = 0; i < taskSize; i++){
        	Task tempDag=taskList.get(i);
        	ArrayList<Integer> parents=tempDag.getpre();
        	indegree[i]=parents.size();	
        }
        return indegree; 
	}
	//========================================================================
	
	/**
	 * 
	 * @Title: initPE
	 * @Description: 创建PE实例并初始化。设置处理器的计算能力为1
	 * @param @throws Throwable
	 * @return void
	 * @throws
	 */
	private static void initPE() throws Throwable {

		for (int i = 0; i < pe_number; i++) {
			PE pe = new PE();
			pe.setID(i);
			pe.setability(1);
			pe.setfree(true);
			pe.setAvail(0);
			PEList.add(pe);
		}
	}

	/**
	 * @Description:根据DAGID和TASKID返回该TASK实例
	 * 
	 * @param DAGId
	 *            ，DAGID
	 * @param taskId
	 *            ，TASKID
	 * @return DAG，TASK实例
	 */
	private static Task getTaskByDagIdAndTaskId(int DAGId, int taskId) {

		for (int i = 0; i < DAGMapList.get(DAGId).gettasknumber(); i++) {
			Task temp = (Task) DAGMapList.get(DAGId).gettasklist().get(i);
			if (temp.getid() == taskId)
				return temp;
		}

		return null;
	}


	/**
	 * @Description:根据TASKID返回该TASK实例
	 * 
	 * @param dagId
	 *            ，TASKID
	 * @return DAG，TASK实例
	 */
	private static Task getTaskBytaskId(int taskId) {
		for (Task task : Task_queue_personal) {
			if (task.getid() == taskId)
				return task;
		}
		return null;
	}

}
