package org.schedule.algorithm;

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
import org.jdom.Attribute;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.schedule.model.Task;
import org.schedule.model.DAG;
import org.schedule.model.DAGDepend;
import org.schedule.model.PE;
import org.schedule.model.Slot;
import org.schedule.model.PEComputerability;
import org.generate.util.CommonParametersUtil;

/**
 * 
 * @ClassName: OrderAsIDWithAdaptation
 * @Description: 
 * 1、不合并
 * 2、修改生成DAG的deadline的方式
 * 2、就是cp取关键路径上的时长，n取整个DAG的最大层数，ex=（deadline-cp）/n
 * 3、改变调度时对传输时长的定义,本来就没错啊
 * 4、先找比例最好的地方（此时寻找空闲块要修改）-----找不到，则寻找空隙最小的方式
 * 5、不考虑松弛问题
 * 6、调度时是会加入自己的子任务
 * 
 * @author YanWenjing   
 * @date 2017-10-21 下午7:45:26
 */
public class DCBF_ID {

	
	private static ArrayList<PE> PEList;
	private static ArrayList<DAG> DAGMapList;
	private static ArrayList<DAGDepend> DAGDependList = null;

	private static ArrayList<Task> DAG_queue;
	
	private static LinkedHashMap<Integer, Integer> DAGDependMap;
	private static LinkedHashMap<String, Double> DAGDependValueMap;

	private static ArrayList<Task> DAG_queue_personal;
	private static LinkedHashMap<Integer, Integer> DAGDependMap_personal;
	private static LinkedHashMap<String, Double> DAGDependValueMap_personal;
	private static Map<Integer, int[]> ComputeCostMap;
	private static Map<Integer, Integer> AveComputeCostMap;

	// д���ļ��Ľ��
	public static String[][] rateResult = new String[1][4];

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

	

	public DCBF_ID() {
		
		DAG_queue = new ArrayList<Task>();
		DAG_queue_personal = new ArrayList<Task>();
		PEList = new ArrayList<PE>();
		DAGMapList = new ArrayList<DAG>();
		DAGDependList = new ArrayList<DAGDepend>();
		
		DAGDependMap = new LinkedHashMap<Integer, Integer>();
		DAGDependValueMap = new LinkedHashMap<String, Double>();
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
			// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
		}

	}

	/**
	 * 
	 * @Title: runMakespan
	 * @Description: ��ʼfillback�㷨
	 * @param @throws Throwable
	 * @return void
	 * @throws
	 */
	public void runMakespan(String pathXML, String resultPath) throws Throwable {

		// init dagmap
		DCBF_ID fb = new DCBF_ID();
		DAGDepend dagdepend = new DAGDepend();
		PEComputerability vcc = new PEComputerability();

		// ��ʼ��������
		initPE();

		// ��ʼ������xml
		initdagmap(dagdepend, vcc, pathXML);

		Date begin = new Date();
		Long beginTime = begin.getTime();
		// ���ȵ�һ��DAG����ʼ�������������ϵ�����ֲ������жΡ�����������ɳ����
		// ���õ�ǰʱ���ǵ�һ��DAG �ĵ���ʱ��
		current_time = DAGMapList.get(0).getsubmittime();

		// ��ʼ���Ⱥ�������ҵ
		for (int i = 0; i < DAGMapList.size(); i++) {

			LinkedHashMap<Integer, ArrayList> SlotListInPestemp = new LinkedHashMap<Integer, ArrayList>();
			LinkedHashMap<Integer, LinkedHashMap> TASKListInPestemp = new LinkedHashMap<Integer, LinkedHashMap>();

			// �����Ӧ����ҵ��Χ�ڵĿ��п�����
			//computeSlot(DAGMapList.get(i).getsubmittime(), DAGMapList.get(i).getDAGdeadline());
		
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
	 * �����������Դ�����ʺ����������
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
			// ���ǵ�һ����ҵ��ֵ
				if (DAGMapList.get(j).getfillbackdone()) {
					successCount++;
					//System.out.println("dag" + DAGMapList.get(j).getDAGId()+ "���ȳɹ����ɹ�ִ��");
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

		System.out.println("OrderAsIDWithAdaptation��" + successCount+ "������������");
		System.out.println("OrderAsIDWithAdaptation��" + faultCount+ "���������ʧ�ܡ�����");

		DecimalFormat df = new DecimalFormat("0.0000");
		System.out.println("��������:");
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
		//PrintResult.printOrderAsIDWithAdaptationToTxt(rateResult, resultPath);

//		System.out.println("������Ƴɹ�����=" + pushSuccessCount);
//		System.out.println("��������=" + taskTotal);

	}

	/**
	 * 
	 * @Title: storeresultShow
	 * @Description: ���汾�㷨�ĸ�������Ŀ�ʼ����ʱ��,������ֻ�е��ȳɹ���
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
	 * @throws IOException 
	 * 
	* @Title: computeSlotAdaptation
	* @Description: ��������Ŀ��п�
	* @param submit
	* @param deadline:
	* @throws
	 */
	public static void computeSlotBetweenTask(int submit, int deadline) throws IOException {

		SlotListInPes.clear();

		for (int i = 0; i < pe_number; i++) {
			// ��ǰ�������Ͽ���Ƭ����������
			int Slotcount = 0;

			// ��ȡĳ�������ϵ���������
			LinkedHashMap<Integer, Integer[]> TASKInPe = new LinkedHashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(i);
			
//			for(Entry<Integer, Integer[]> map:TASKInPe.entrySet()){
//				System.out.println("��������"+i+":"+map.getKey()+"\t�����ţ�"+map.getValue()[3]+"\t��ʼʱ�䣺"+map.getValue()[0]+"\t����ʱ�䣺"+map.getValue()[1]+"\t�����������е�����"+TASKInPe.size());
//			}
			// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id

			ArrayList<Slot> slotListinpe = new ArrayList<Slot>();
			ArrayList<Slot> slotListinpe_ori = new ArrayList<Slot>();

			if (TASKInPe.size() == 0) {// ��ǰ��������û��ִ�й�����
				Slot tem = new Slot();
				tem.setPEId(i);
				tem.setslotId(Slotcount);
				tem.setslotstarttime(submit);
				tem.setslotfinishtime(deadline);
				slotListinpe.add(tem);
				Slotcount++;
			} else if (TASKInPe.size() == 1) {// �ô�������ֻ��һ����������

				if (TASKInPe.get(0)[0] > submit) {
					if (deadline <= TASKInPe.get(0)[0]) {
						Slot tem = new Slot();
						ArrayList<String> below_ = new ArrayList<String>();
						// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
						// ��������dag ������
						below_.add(TASKInPe.get(0)[2] + " "+ TASKInPe.get(0)[3] + " " + 0);
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
						// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
						// ��������dag ������
						below_.add(TASKInPe.get(0)[2] + " "+ TASKInPe.get(0)[3] + " " + 0);
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
						// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
						// ��������dag ������
						below_.add(TASKInPe.get(0)[2] + " "+ TASKInPe.get(0)[3] + " " + 0);
						tem.setPEId(i);
						tem.setslotId(Slotcount);
						tem.setslotstarttime(submit);
						tem.setslotfinishtime(TASKInPe.get(0)[0]);
						tem.setbelow(below_);
						slotListinpe.add(tem);
						Slotcount++;
						// ===================================================
						Slot temp = new Slot();
						// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
						temp.setPEId(i);
						temp.setslotId(Slotcount);
						temp.setslotstarttime(TASKInPe.get(0)[1]);
						temp.setslotfinishtime(deadline);
						slotListinpe.add(temp);
						Slotcount++;

					}
				} else if (submit <= TASKInPe.get(0)[1] && deadline > TASKInPe.get(0)[1]) {
					Slot tem = new Slot();
					// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
					tem.setPEId(i);
					tem.setslotId(Slotcount);
					tem.setslotstarttime(TASKInPe.get(0)[1]);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);
					Slotcount++;
				} else if (submit > TASKInPe.get(0)[1]&& deadline > TASKInPe.get(0)[1]) {
					Slot tem = new Slot();
					// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
					tem.setPEId(i);
					tem.setslotId(Slotcount);
					tem.setslotstarttime(submit);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);
					Slotcount++;
				}
			} else {// �ô��������ж����������
//				System.out.println("��������ţ�"+i+"\tTASKInPe.size()="+TASKInPe.size());

				// ��ȡ��������ԭ�����ڸ�������ִ�п���Ƭ�Σ������Ǵ��������еĿ��У���û������submit��deadline�����ơ�
				// ����������ϵ�һ������Ŀ�ʼʱ�䲻��Ϊ0���ͷ��һ�ο�϶
				if (TASKInPe.get(0)[0] >= 0) {
					//System.out.println("��������ţ�"+i+"\tTASKInPe.get(0)[0]="+TASKInPe.get(0)[0]);	
					Slot tem = new Slot();
					ArrayList<String> below_ = new ArrayList<String>();
					for (int k = 0; k < TASKInPe.size(); k++) {
						// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
						// ����������ҵ��id ������
						below_.add(TASKInPe.get(k)[2] + " "+ TASKInPe.get(k)[3] + " " + 0);
					}
					tem.setPEId(i);
					tem.setslotId(Slotcount);
					tem.setslotstarttime(0);
					tem.setslotfinishtime(TASKInPe.get(0)[0]);
					tem.setbelow(below_);
					slotListinpe_ori.add(tem);
					Slotcount++;
				}
				
				
				for (int j = 1; j < TASKInPe.size(); j++) {
					if (TASKInPe.get(j - 1)[1] <= TASKInPe.get(j)[0]) {
						Slot tem = new Slot();
						ArrayList<String> below_ = new ArrayList<String>();
						for (int k = j; k < TASKInPe.size(); k++) {
							// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
							// ����������ҵ��id ������ ���ĸ����п�ĺ���
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

				// �����ڵ�ǰdag��ʼʱ�䵽��ֹʱ��֮�� ��slot�����������ɵ�ǰDAG����ʼslot�ı��
				int startslot = 0;
				for (int j = 0; j < slotListinpe_ori.size(); j++) {
					Slot tem = new Slot();
					tem = slotListinpe_ori.get(j);
					if (j == 0&&(tem.slotstarttime != tem.slotfinishtime)) {
						if (submit >= 0 && submit <= tem.slotfinishtime) {
							startslot = 0;
							tem.setslotstarttime(submit);
							break;
						}
					} else if (j > 0 && j <= (slotListinpe_ori.size() - 1)) {
						if (tem.getslotstarttime() <= submit && tem.getslotfinishtime() > submit) {
							tem.setslotstarttime(submit);
							startslot = j;
							break;
						} else if (tem.getslotstarttime() > submit&& slotListinpe_ori.get(j - 1).getslotfinishtime() <= submit) {
							startslot = j;
							break;
						}
					}
					// �������ʱ����ǰ���slot��û�취ƥ����룬����ڴ��������.
//					if (j == (slotListinpe_ori.size() - 1))
//						startslot = slotListinpe_ori.size();
				}

				//���deadline��ĳ��slot�м䣬��ȡ���slot
				//Ŀǰֻȡ�ڸ�������֮����ӵ�slot
				int count = 0;
				for (int j = startslot; j < slotListinpe_ori.size(); j++) {
					Slot tem = new Slot();
					tem = slotListinpe_ori.get(j);
					if (tem.getslotfinishtime() <= deadline) {
						tem.setslotId(count);
						slotListinpe.add(tem);
						count++;
					} 		
				}	
				
//				if(i==0){
//					System.out.println("slotListinpe.size()="+slotListinpe.size());
//				}
				
			}
//			if(i==0){
//				for(int m=0;m<slotListinpe.size();m++){
//					slot tem = slotListinpe.get(m);
//					System.out.println("��������ţ�"+tem.getPEId()+"\t��ʼʱ�䣺"+tem.getslotstarttime()+"\t����ʱ�䣺"+tem.getslotfinishtime());
//				}
//			}
			SlotListInPes.put(i, slotListinpe);
		}	
	}
	

	/**
	 * 
	 * @Title: computeSlot
	 * @Description: ����relax�������¼������ʱ���SlotListInPes������SlotListInPes.put(i,
	 *               slotListinpe)==��slotListinpe��������ɸѡ���ģ�ʱ������ƥ��submit----deadlineʱ��ε�slot�ļ���
	 * @param @param submit��DAG�ύʱ��
	 * @param @param deadline��DAG��ֹʱ��
	 * @return void
	 * @throws
	 */
	public static void computeSlot(int submit, int deadline) {

		SlotListInPes.clear();

		for (int i = 0; i < pe_number; i++) {
			// ��ǰ�������Ͽ���Ƭ����������
			int Slotcount = 0;

			// ��ȡĳ�������ϵ���������
			LinkedHashMap<Integer, Integer[]> TASKInPe = new LinkedHashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(i);
			// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id

			ArrayList<Slot> slotListinpe = new ArrayList<Slot>();
			ArrayList<Slot> slotListinpe_ori = new ArrayList<Slot>();

			if (TASKInPe.size() == 0) {// ��ǰ��������û��ִ�й�����
				Slot tem = new Slot();
				tem.setPEId(i);
				tem.setslotId(Slotcount);
				tem.setslotstarttime(submit);
				tem.setslotfinishtime(deadline);
				slotListinpe.add(tem);
				Slotcount++;
			} else if (TASKInPe.size() == 1) {// �ô�������ֻ��һ����������

				if (TASKInPe.get(0)[0] > submit) {
					if (deadline <= TASKInPe.get(0)[0]) {
						Slot tem = new Slot();
						ArrayList<String> below_ = new ArrayList<String>();
						// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
						// ��������dag ������
						below_.add(TASKInPe.get(0)[2] + " "+ TASKInPe.get(0)[3] + " " + 0);
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
						// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
						// ��������dag ������
						below_.add(TASKInPe.get(0)[2] + " "+ TASKInPe.get(0)[3] + " " + 0);
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
						// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
						// ��������dag ������
						below_.add(TASKInPe.get(0)[2] + " "+ TASKInPe.get(0)[3] + " " + 0);
						tem.setPEId(i);
						tem.setslotId(Slotcount);
						tem.setslotstarttime(submit);
						tem.setslotfinishtime(TASKInPe.get(0)[0]);
						tem.setbelow(below_);
						slotListinpe.add(tem);
						Slotcount++;
						// ===================================================
						Slot temp = new Slot();
						// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
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
					// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
					tem.setPEId(i);
					tem.setslotId(Slotcount);
					tem.setslotstarttime(TASKInPe.get(0)[1]);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);
					Slotcount++;
				} else if (submit > TASKInPe.get(0)[1]
						&& deadline > TASKInPe.get(0)[1]) {
					Slot tem = new Slot();
					// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
					tem.setPEId(i);
					tem.setslotId(Slotcount);
					tem.setslotstarttime(submit);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);
					Slotcount++;
				}
			} else {// �ô��������ж����������

				// ��ȡ��������ԭ�����ڸ�������ִ�п���Ƭ�Σ������Ǵ��������еĿ��У���û������submit��deadline�����ơ�
				// ����������ϵ�һ������Ŀ�ʼʱ�䲻��Ϊ0���ͷ��һ�ο�϶
				if (TASKInPe.get(0)[0] >= 0) {
					Slot tem = new Slot();
					ArrayList<String> below_ = new ArrayList<String>();
					for (int k = 0; k < TASKInPe.size(); k++) {
						// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
						// ����������ҵ��id ������
						below_.add(TASKInPe.get(k)[2] + " "+ TASKInPe.get(k)[3] + " " + 0);
					}
					tem.setPEId(i);
					tem.setslotId(Slotcount);
					tem.setslotstarttime(0);
					tem.setslotfinishtime(TASKInPe.get(0)[0]);
					tem.setbelow(below_);
					slotListinpe_ori.add(tem);
					Slotcount++;
				}

				for (int j = 1; j < TASKInPe.size(); j++) {
					if (TASKInPe.get(j - 1)[1] <= TASKInPe.get(j)[0]) {
						Slot tem = new Slot();
						ArrayList<String> below_ = new ArrayList<String>();
						for (int k = j; k < TASKInPe.size(); k++) {
							// ����0����task��ʼʱ�䣬1����task����ʱ�䣬2����dagid��3����id
							// ����������ҵ��id ������ ���ĸ����п�ĺ���
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

				// �����ڵ�ǰdag��ʼʱ�䵽��ֹʱ��֮�� ��slot�����������ɵ�ǰDAG����ʼslot�ı��
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
								&& slotListinpe_ori.get(j - 1).getslotfinishtime() <= submit) {
							startslot = j;
							break;
						}
					}

					// �������ʱ����ǰ���slot��û�취ƥ����룬����ڴ��������.
					if (j == (slotListinpe_ori.size() - 1))
						startslot = slotListinpe_ori.size();
				}

				// ����slotListinpe���ݣ���������ɸѡ���ģ�ʱ������ƥ��submit----deadlineʱ��ε�slot�ļ���
				/**
				 * ����������Ӧ��û�����⣬��Ϊ�����ÿ���������ϵĿ���ʱ��
				 * ��������Ӧ����ֻҪ����һ�־ͺ��ˣ�����Ϊʲô�����ü��֣���������Ϊ�ڵ����㷨�о����б�����û���õ������ҵ
				 * ������һ�μ����ʱ���ּ�����һ��
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

				// �������һ�����п�
				if (TASKInPe.get(TASKInPe.size() - 1)[1] <= submit) {
					Slot tem = new Slot();
					tem.setPEId(i);
					tem.setslotId(count);
					tem.setslotstarttime(submit);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);
					// System.out.println("Ӧ����ֻ��һ�����п��"+slotListinpe.size());
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
	 * @Description: �ڵ���֮���޸�slotlistinpe���below
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
			// ��ĳ�����п���棬������п�ı��
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
	 * @Description: ��ӡ��ҵ�ĵ��Ƚ�����ļ���
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
			Task tempDag = getDAGById(dagid, o);
			ArrayList<Integer> pre = tempDag.getpre();

			for (int p : pre) {
				Task tempPre = getDAGById(dagid, p);
				if (tempPre.getfillbackstarttime() > tempDag
						.getfillbackstarttime())
					writer.write("DAG��id=" + dagid + "��������id=" + p + ";��ʼʱ�䣺"
							+ tempPre.getfillbackstarttime() + ";������id=" + o
							+ ";������ʼʱ�䣺" + tempDag.getfillbackstarttime()
							+ "\n");
			}
		}
		if (writer != null) {
			writer.close();
		}

	}

	/**
	 * @Description: �жϺ��ƿ��п��ĸ����ܷ�ʹ������ɹ�������п�
	 * 
	 * @param dagmap
	 *            ��DAG����DAG�и����������Լ�DAG�������������ϵ
	 * @param readylist
	 *            ��readylist��������
	 * 
	 * @return isslide���ܷ����
	 * @throws IOException 
	 */
	public static boolean scheduling(DAG dagmap, ArrayList<Task> readylist) throws IOException {

		boolean findsuc = true;// ��DAG�ܷ����ɹ���ֻҪһ������ʧ�ܾ���ȫ��ʧ��

		int finimintime = timewindowmax;
		int mindag = -1;
		int message[][] = new int[readylist.size()][6];
		// 0 is if success 1 means success 0 means fail,
		// 1 is earliest starttime
		// 2 is peid
		// 3 is slotid
		// 4 is if need slide
		// 5 is slide length

		int[] finish = new int[readylist.size()];// �������ִ�н���ʱ��

		// ΪDAG�г���һ���������������Ѱ�ɲ����slot��������Ϣ

		
		for (int i = 0; i < readylist.size(); i++) {
			Task dag = new Task();
			dag = readylist.get(i);
			message[i] = findslot(dagmap, dag);
			finish[i] = message[i][1] + dag.getts();
		}

		int dagId = dagmap.getDAGId();
		// ֻҪ������һ������û�ܻ���ɹ�����ô����DAGʧ��
		for (int i = 0; i < readylist.size(); i++) {
			Task tempDagResult = readylist.get(i);
			if (message[i][0] == 0) {
				dagResultMap[dagId][tempDagResult.getid()] = 1;
				findsuc = false;
			}
		}
		if (findsuc == false) {
			return findsuc;
		}

		/**
		 * ��ʱ���ȷ�ʽ�ǰ�������ID����
		 */
		mindag = 0;

		//
		ArrayList<Task> DAGTaskList = new ArrayList<Task>();
		Task dagtemp = new Task();
		for (int i = 0; i < dagmap.gettasklist().size(); i++) {
			Task dag = new Task();
			DAGTaskList.add((Task) dagmap.gettasklist().get(i));
			dag = (Task) dagmap.gettasklist().get(i);
			int tempTaskId = readylist.get(mindag).getid();
			if (dag.getid() == tempTaskId) {
				dagtemp = (Task) dagmap.gettasklist().get(i);
			}
		}

		// �����������fillbackstarttime����Ϣ
		// int startmin = finimin - readylist.get(mindag).getts();
		int startmin = message[mindag][1];
		int pemin = message[mindag][2];
		int slotid = message[mindag][3];
		dagtemp.setfillbackstarttime(startmin);
		dagtemp.setfillbackfinishtime(startmin + dagtemp.getlength());
		dagtemp.setfillbackpeid(pemin);
		dagtemp.setpeid(pemin);
		dagtemp.setfillbackready(true);
		dagtemp.setprefillbackdone(true);
		dagtemp.setprefillbackdone(true);
		dagtemp.setfillbackdone(true);

		LinkedHashMap<Integer, Integer[]> TASKInPe = new LinkedHashMap<Integer, Integer[]>();
		TASKInPe = TASKListInPes.get(pemin);

		// ==================�޸Ĵ������ϵĵ��Ƚ���������������
		// ��Ҫ�����Ŀ���λ���в������񣬲�����ԭ�����ڴ������ϵ�����
		if (TASKInPe.size() > 0) {// �ô�������ԭ��������

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

			if (below.size() > 0) {// ������slot����ԭ��������
				String buf[] = below.get(0).split(" ");
				// ���п�������
				int inpe = Integer.valueOf(buf[2]).intValue();
				// ���ƺ���������
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
				st_fi[1] = startmin + dagtemp.getlength();
				st_fi[2] = dagtemp.getdagid();
				st_fi[3] = dagtemp.getid();
				TASKInPe.put(inpe, st_fi);

				/**
				 * ����isfillback ʹ������������ɳ���һ���� ֤����������ǲ���������ҵ֮���
				 */
				dagtemp.setisfillback(true);
				// �ı���п�ĵ�below
				changeinpe(slotlistinpe, inpe);

			} else {// ������slot����ԭ��û������
				Integer[] st_fi = new Integer[4];
				st_fi[0] = startmin;
				st_fi[1] = startmin + dagtemp.getlength();
				st_fi[2] = dagtemp.getdagid();
				st_fi[3] = dagtemp.getid();
				TASKInPe.put(TASKInPe.size(), st_fi);
			}
		} else {// �ô�������ԭ��û������
			Integer[] st_fi = new Integer[4];
			st_fi[0] = startmin;
			st_fi[1] = startmin + dagtemp.getlength();
			st_fi[2] = dagtemp.getdagid();
			st_fi[3] = dagtemp.getid();
			TASKInPe.put(0, st_fi);
		}

		TASKListInPes.put(pemin, TASKInPe);
		// ���¼�����п��б�
		// computeSlot(dagmap.getsubmittime(), dagmap.getDAGdeadline());
		// computeSlotBetweenTask(dagmap.getsubmittime(),
		// dagmap.getDAGdeadline());
		// printTaskAndSlot(readylist.get(mindag).getid());
		readylist.remove(mindag);

		return findsuc;
	}
	
	
	

	private static int[] findslotAda(DAG dagmap, Task dag) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description: �ж�DAG������ڵ��ܷ��ҵ�����ʱ��η��룬������򷵻���Ӧ����Ϣ
	 * 
	 * @param dagmap
	 *            ��DAG����DAG�и����������Լ�DAG�������������ϵ
	 * @param dagtemp
	 *            ��DAG������TASK�е�һ��
	 * @return message��0 is if success(1 means success 0 means fail), 1 is
	 *         earliest start time, 2 is peid, 3 is slotid
	 * @throws IOException 
	 */
	public static int[] findslot(DAG dagmap, Task dagtemp) throws IOException {
		int message[] = new int[6];

		boolean findsuc = false;
		int startmin = timewindowmax;
		int finishmin = timewindowmax;
		int diffmin = timewindowmax;
		
		int pemin = -1;
		int slide;
		ArrayList<Integer> adoptPeList = new ArrayList<>();
		
		int[] startinpe = new int[pe_number]; // �ڴ�����i�Ͽ�ʼִ�е�ʱ��
		int[] slotid = new int[pe_number]; // ���п��ڴ�����i�ϵı��	
		//int[] diff = new int[pe_number];
		
		//===========================������Ӧ���ж�
		int[] diffAda = new int[pe_number];
		int[] startinpeAda = new int[pe_number]; // �ڴ�����i�Ͽ�ʼִ�е�ʱ��
		int[] slotidAda = new int[pe_number]; // ���п��ڴ�����i�ϵı��

		int diffRateMax = -1;
		//===========================����Ҫ
		int[] isneedslide = new int[pe_number]; // 0 means don't need 1 means
		int[] slidelength = new int[pe_number];// �ڴ�����i����Ҫ�����ĳ���
		for (int k = 0; k < pe_number; k++) {
			pushFlag[k] = 0;
		}

		Map<String, Double> DAGTaskDependValue = new LinkedHashMap<String, Double>();
		DAGTaskDependValue = dagmap.getdependvalue();

		// ��ȡ�����񼯺�
		ArrayList<Task> pre_queue = new ArrayList<Task>();
		ArrayList<Integer> pre = new ArrayList<Integer>();
		pre = dagtemp.getpre();
		if (pre.size() > 0) {
			for (int j = 0; j < pre.size(); j++) {
				Task buf = new Task();
				buf = getDAGById(dagtemp.getdagid(), pre.get(j));
				if (!buf.fillbackdone && !buf.fillbackready) {
					message[0] = 0;
					System.out.println("���ĸ��ڵ�û�����");
					return message;
				}
				pre_queue.add(buf);
			}
		}
		
		//���㷶Χ�ڵĿ��п�
		computeSlotBetweenTask(dagmap.getsubmittime(), dagmap.getDAGdeadline());
		
		boolean isAdapt=false;
		// ��ȡ�������ڸô������ϵ����翪ʼʱ��
		for (int i = 0; i < pe_number; i++) {
			int predone = 0;// �ڵ�ǰ�������ϵ�ǰ�������翪ʼִ��ʱ��
			if (pre_queue.size() == 1) {// ���������ֻ��һ��������
				if (pre_queue.get(0).getfillbackpeid() == i) {// �븸������ͬһ����������
					predone = pre_queue.get(0).getfillbackfinishtime();
				} else {// �븸������ͬһ����������
					int value = (int) (double) DAGTaskDependValue.get(String.valueOf(pre_queue.get(0).getid())+ " "+ String.valueOf(dagtemp.getid()));
					predone = pre_queue.get(0).getfillbackfinishtime() + value;
				}
			} else if (pre_queue.size() >= 1) {// �ж��������
				for (int j = 0; j < pre_queue.size(); j++) {
					if (pre_queue.get(j).getfillbackpeid() == i) {// �븸������ͬһ����������
						if (predone < pre_queue.get(j).getfillbackfinishtime()) {
							predone = pre_queue.get(j).getfillbackfinishtime();
						}
					} else {// �븸������ͬһ���������ϣ���ʼʱ��Ϊ�����������ʱ���Լ���Դ����ʱ��
						int valu = (int) (double) DAGTaskDependValue.get(String.valueOf(pre_queue.get(j).getid())+ " "+ String.valueOf(dagtemp.getid()));
						int value = pre_queue.get(j).getfillbackfinishtime()+ valu;
						if (predone < value)
							predone = value;
					}
				}
			}
			
			
			startinpeAda[i] = -1;
			diffAda[i] = -1;
			
			ArrayList<Slot> slotlistinpe = new ArrayList<Slot>();
		
			//�õ���Ӧ�ԵĿ��п�	
			for (int j = 0; j < SlotListInPes.get(i).size(); j++){
				slotlistinpe.add((Slot) SlotListInPes.get(i).get(j));
			}
			
			LinkedHashMap<Integer, Integer[]> tempSlotInfo = new LinkedHashMap<>();	
			int countSlot = 0;
			int taskLength=dagtemp.getts();
			

			
			// ��Ѱ�������ڵ�ǰ�������ϲ�������翪ʼ�Ŀ����������Ϣ
			for (int j = 0; j < SlotListInPes.get(i).size(); j++) {
				int slst = slotlistinpe.get(j).getslotstarttime();
				int slfi = slotlistinpe.get(j).getslotfinishtime();
				int slotID = slotlistinpe.get(j).getslotId();
				int slLength = slfi-slst;
				//���Ϊ0�򲻲������
				if(slLength==0){
					continue;
				}
				
				if (predone < slst) {
					if ((slst + dagtemp.getts()) <= slfi&& (slst + dagtemp.getts()) <= dagtemp.getdeadline()) {
						Integer[] slotInfo = new Integer[4];
						slotInfo[0] = slotID;// slotid[i]
						slotInfo[1] = slst;// startinpe[i]
						slotInfo[2] = (taskLength*100)/(slLength);// diffRate[i]
						//System.out.println("taskLength="+taskLength+"\tslLength="+slLength+"\tslotInfo="+slotInfo[2]);
						slotInfo[3] = 0;// isneedslide[i]
						tempSlotInfo.put(countSlot++, slotInfo);
						
					}
				} else if (predone >= slst && predone < slfi) {
					if ((predone + dagtemp.getts()) <= slfi&& (predone + dagtemp.getts()) <= dagtemp.getdeadline()) {
						Integer[] slotInfo = new Integer[4];
						slotInfo[0] = slotID;// slotid[i]
						slotInfo[1] = predone;// startinpe[i]
						slotInfo[2] = (taskLength*100)/(slLength);// diffRate[i]
						//System.out.println("taskLength="+taskLength+"\tslLength="+slLength+"\tslotInfo="+slotInfo[2]);
						slotInfo[3] = 0;// isneedslide[i]
						tempSlotInfo.put(countSlot++, slotInfo);
					}
				}
			}
			
			int diffRate = -1;
			
			for (Entry<Integer, Integer[]> map : tempSlotInfo.entrySet()) {
				Integer[] slotInfo = map.getValue();
				if (diffRate < slotInfo[2]) {
					diffRate = slotInfo[2];
					slotidAda[i] = slotInfo[0];
					startinpeAda[i] = slotInfo[1];
					diffAda[i] = slotInfo[2];
					isneedslide[i] = slotInfo[3];
				}
			}	

		}
		
		//���ܲ���ĵط�
		for (int i = 0; i < pe_number; i++) {
			if (startinpeAda[i] != -1) {
				findsuc = true;
				isAdapt=true;
				if (diffAda[i] > diffRateMax) {
					diffRateMax = diffAda[i];
				}
			}
		}
			
		// �ҵ��������Ĵ�����,//ֵ��ͬ�������ȡִ�п�ʼʱ�������
		for (int i = 0; i < pe_number; i++) {
			if (diffRateMax == diffAda[i]) {
				adoptPeList.add(i);
			}
		}		
		if (adoptPeList.size() > 1) {
			int minStart = Integer.MAX_VALUE;
			for (Integer peIndex : adoptPeList) {
				if (startinpeAda[peIndex] < minStart) {
					minStart = startinpeAda[peIndex];
					pemin = peIndex;
				}
			}
		} else if (adoptPeList.size() == 1) {
			pemin = adoptPeList.get(0);
		}
		

		
		if(!isAdapt){
			//System.out.println("�Ҳ�������ģ�"+dagtemp.getid());
			findsuc = false;
			//����ķ�ʽ�Ҳ���
			computeSlot(dagmap.getsubmittime(), dagmap.getDAGdeadline());
			pemin=-1;
			// ��ȡ�������ڸô������ϵ����翪ʼʱ��
			for (int i = 0; i < pe_number; i++) {
				int predone = 0;// �ڵ�ǰ�������ϵ�ǰ�������翪ʼִ��ʱ��
				if (pre_queue.size() == 1) {// ���������ֻ��һ��������
					if (pre_queue.get(0).getfillbackpeid() == i) {// �븸������ͬһ����������
						predone = pre_queue.get(0).getfillbackfinishtime();
					} else {// �븸������ͬһ����������
						int value = (int) (double) DAGTaskDependValue.get(String.valueOf(pre_queue.get(0).getid())+ " "+ String.valueOf(dagtemp.getid()));
						predone = pre_queue.get(0).getfillbackfinishtime() + value;
					}
				} else if (pre_queue.size() >= 1) {// �ж��������
					for (int j = 0; j < pre_queue.size(); j++) {
						if (pre_queue.get(j).getfillbackpeid() == i) {// �븸������ͬһ����������
							if (predone < pre_queue.get(j).getfillbackfinishtime()) {
								predone = pre_queue.get(j).getfillbackfinishtime();
							}
						} else {// �븸������ͬһ���������ϣ���ʼʱ��Ϊ�����������ʱ���Լ���Դ����ʱ��
							int valu = (int) (double) DAGTaskDependValue.get(String.valueOf(pre_queue.get(j).getid())+ " "+ String.valueOf(dagtemp.getid()));
							int value = pre_queue.get(j).getfillbackfinishtime()+ valu;
							if (predone < value)
								predone = value;
						}
					}
				}
				startinpe[i] = -1;

				ArrayList<Slot> slotlistinpe = new ArrayList<Slot>();
				// i:���������
				for (int j = 0; j < SlotListInPes.get(i).size(); j++)
					slotlistinpe.add((Slot) SlotListInPes.get(i).get(j));
				
				LinkedHashMap<Integer, Integer[]> tempSlotInfo = new LinkedHashMap<>();	
				int countSlot = 0;
				// ��Ѱ�������ڵ�ǰ�������ϲ�������翪ʼ�Ŀ����������Ϣ
				for (int j = 0; j < SlotListInPes.get(i).size(); j++) {
					int slst = slotlistinpe.get(j).getslotstarttime();
					int slfi = slotlistinpe.get(j).getslotfinishtime();
					int slotID = slotlistinpe.get(j).getslotId();
					
					if (predone < slst) {
						if ((slst + dagtemp.getts()) <= slfi&& (slst + dagtemp.getts()) <= dagtemp.getdeadline()) {
							startinpe[i] = slst;
							slotid[i] = slotlistinpe.get(j).getslotId();
							isneedslide[i] = 0;
							break;
						}
					} else if (predone >= slst && predone < slfi) {
						if ((predone + dagtemp.getts()) <= slfi&& (predone + dagtemp.getts()) <= dagtemp.getdeadline()) {
							startinpe[i] = predone;
							slotid[i] = slotlistinpe.get(j).getslotId();
							isneedslide[i] = 0;
							break;
						}
					}
				}
			}
				
			for (int i = 0; i < pe_number; i++) {
				if (startinpe[i] != -1) {
					findsuc = true;
					if (startinpe[i] < startmin) {
						startmin = startinpe[i];
						pemin = i;
					}
				}
			}
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
			if(isAdapt){
				message[1] = startinpeAda[pemin];
				message[3] = slotidAda[pemin];
			}else {
				message[1] = startinpe[pemin];
				message[3] = slotid[pemin];
			}
			message[2] = pemin;
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
	 * @Description: �ж�DAG����ʼ�ڵ��ܷ��ҵ�����ʱ��η���
	 * 
	 * @param dagmap
	 *            ��DAG����DAG�и����������Լ�DAG�������������ϵ
	 * @param dagtemp
	 *            ����ʼ�ڵ�
	 * @return findsuc���ܷ����
	 */
	public static boolean findfirsttaskslot(DAG dagmap, Task dagtemp) {
		// perfinish is the earliest finish time minus task'ts time, the
		// earliest start time

		boolean findsuc = false;
		int startmin = timewindowmax;
		int finishmin = 0;
		int pemin = -1;
		int slide;
		int[] startinpe = new int[pe_number];// �ڴ�����i�Ͽ�ʼִ�е�����ʱ��
		int[] slotid = new int[pe_number];// ����ڴ�����i��ִ�У�����������slot��id
		// int[] slidinpe = new int[pe_number];//����ڴ�����i��ִ�У�����������slot��id

		computeSlot(dagmap.getsubmittime(), dagmap.getDAGdeadline());
		// �������д�����
		for (int i = 0; i < pe_number; i++) {
			
			startinpe[i] = -1;
			ArrayList<Slot> slotlistinpe = new ArrayList<Slot>();
			for (int j = 0; j < SlotListInPes.get(i).size(); j++)
				slotlistinpe.add((Slot) SlotListInPes.get(i).get(j));

			// ����������������������ʱ��Ҫ��Ŀ��ж�
			for (int j = 0; j < SlotListInPes.get(i).size(); j++) {
				int slst = slotlistinpe.get(j).getslotstarttime();
				int slfi = slotlistinpe.get(j).getslotfinishtime();
				int slotId=slotlistinpe.get(j).getslotId();
				// ÿ������ĵ���ʱ���ʼ������ҵ����ύʱ�䡣
				// �����һ������ĵ���ʱ�������ҵ�ύ��ʱ�䡣dagtemp.getarrive()
				if (dagtemp.getarrive() <= slst) {// predone<=slst
					if ((slst + dagtemp.getts()) <= slfi && // s1+c<f1
							(slst + dagtemp.getts()) <= dagtemp.getdeadline()) {
						startinpe[i] = slst;
						slotid[i] = slotId;
						break;
					}
				} else {// predone>slst
					if ((dagtemp.getarrive() + dagtemp.getts()) <= slfi // predone+c<f1
							&& (dagtemp.getarrive() + dagtemp.getts()) <= dagtemp.getdeadline()) {
						startinpe[i] = dagtemp.getarrive();
						slotid[i] =slotId;
						break;
					} 
				}
			}

		}
		// �ҵ���ʼʱ������Ĵ�����
		for (int i = 0; i < pe_number; i++) {
			if (startinpe[i] != -1) {
				findsuc = true;
				// ѡ�ڿ�ʼʱ������Ŀ��п�
				if (startinpe[i] < startmin) {
					startmin = startinpe[i];
					pemin = i;
				}
			}
		}
		
		if (findsuc == false) {
			System.out.println("����ĵ�һ���ڵ�û���ҵ����ʴ������ĵط�����");
		}
		// ���������һ�����������ܹ������������
		if (findsuc) {
			
			finishmin = startmin + dagtemp.getlength();
			dagtemp.setfillbackstarttime(startmin);
			dagtemp.setfillbackfinishtime(finishmin);
			dagtemp.setfillbackpeid(pemin);
			dagtemp.setfillbackready(true);

			LinkedHashMap<Integer, Integer[]> TASKInPe = new LinkedHashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(pemin);

			if (TASKInPe.size() > 0) {// ԭ���Ĵ��������ж���������

				ArrayList<Slot> slotlistinpe = new ArrayList<Slot>();
				for (int j = 0; j < SlotListInPes.get(pemin).size(); j++)
					slotlistinpe.add((Slot) SlotListInPes.get(pemin).get(j));

				ArrayList<String> below = new ArrayList<String>();

				Slot slottem = new Slot();
				// �ҵ�slotlistinpe��Ҫ������Ǹ�slot����
				for (int i = 0; i < slotlistinpe.size(); i++) {
					if (slotlistinpe.get(i).getslotId() == slotid[pemin]) {
						slottem = slotlistinpe.get(i);
						break;
					}
				}

				// �õ�below
				for (int i = 0; i < slottem.getbelow().size(); i++) {
					below.add(slottem.getbelow().get(i));
				}

				if (below.size() > 0) {// Ҫ�����λ�ú����ж������
					// ����������뵽��Ӧ��λ��
					String buf[] = below.get(0).split(" ");
					int inpe = Integer.valueOf(buf[2]).intValue();

					// �����п��������ڴ������ϵ�ִ�д��򶼺���һ��λ��
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
					st_fi[2] = dagtemp.getdagid();
					st_fi[3] = dagtemp.getid();
					TASKInPe.put(inpe, st_fi);
					
					dagtemp.setisfillback(true);
					
				} else {// Ҫ�����λ�ú���û������Ҳ�Ͳ���Ҫ�ƶ�
					Integer[] st_fi = new Integer[4];
					st_fi[0] = startmin;
					st_fi[1] = finishmin;
					st_fi[2] = dagtemp.getdagid();
					st_fi[3] = dagtemp.getid();
					TASKInPe.put(TASKInPe.size(), st_fi);
				}

			} else {// ����ô�������ԭ��û������
				Integer[] st_fi = new Integer[4];
				st_fi[0] = startmin;
				st_fi[1] = finishmin;
				st_fi[2] = dagtemp.getdagid();
				st_fi[3] = dagtemp.getid();
				TASKInPe.put(TASKInPe.size(), st_fi);
			}

			// �����µĿ��п��б�
			computeSlot(dagmap.getsubmittime(), dagmap.getDAGdeadline());
			
		} else {
			return false;
		}

		return findsuc;// �����Ƿ��ҵ�λ�ò�������
		
		

	}

	/**
	 * @Description: �ж�backfilling�����ܷ�ɹ�, �ӵڶ����ύ����ʼ����
	 * 
	 * @param dagmap
	 *            ��DAG����DAG�и����������Լ�DAG�������������ϵ
	 * @return fillbacksuc��backfilling�����ĳɹ����
	 * @throws IOException 
	 */
	public static boolean fillback(DAG dagmap) throws IOException {

		int runtime = dagmap.getsubmittime();
		boolean fillbacksuc = true; // ֻҪ��һ������ʧ�ܾ���ȫ��ʧ��

		boolean notfini = true; // ������ȫ��������ִ�гɹ�

		ArrayList<Task> readylist = new ArrayList<Task>();
		ArrayList<Task> DAGTaskList = new ArrayList<Task>();

		int DAGID = dagmap.getDAGId();

		for (int i = 0; i < dagmap.gettasklist().size(); i++) {
			DAGTaskList.add((Task) dagmap.gettasklist().get(i));
		}

		while (runtime <= dagmap.getDAGdeadline() && notfini && fillbacksuc) {
			
			// ������������Ϊ��ǰʱ��ִ����ϵ��������ò���
			for (Task dag : DAGTaskList) {
				if ((dag.getfillbackstarttime() + dag.getts()) == runtime
						&& dag.getfillbackready()
						&& dag.getfillbackdone() == false
						&& dag.getprefillbackdone() == true
						&& dag.getprefillbackready() == true) {
					dag.setfillbackfinishtime(runtime);
					dag.setfillbackdone(true);
				}
			}

			/**
			 * 
			 * �Ӵ˷�Ϊ������ 1����һ���������жϵ��ȳɹ��ĵ����������Ƿ�ִ�н����������������ý���ʱ��
			 * 2����ǰʱ���Ƿ��п���ȥ���ȵ����񣨾��������о�ȥ����û�о��Թ���
			 * 
			 */
			for (Task dag : DAGTaskList) {
				// =====================���ñ���ҵ�ĵ�һ������============
				if (dag.getid() == 0 && dag.getfillbackready() == false) {
					if (findfirsttaskslot(dagmap, DAGTaskList.get(0))) {// ��ǰDAG����ʼ�ڵ����ҵ�����ʱ��η���
						DAGTaskList.get(0).setprefillbackready(true);//
						DAGTaskList.get(0).setprefillbackdone(true);

						if (dag.getts() == 0) {// ������������Ϊ�˹�һ������ӽ�ȥ����ʼ�ڵ�
							dag.setfillbackfinishtime(dag.getfillbackstarttime());
						} else {
							dag.setfillbackfinishtime(dag.getfillbackstarttime() + dag.getts());
						}
						dag.setfillbackdone(true);
						dag.setfillbackready(true);
						// ����ǵ��ڵ�������ô�͵��ȳɹ���ѽ~~
						if (dagmap.isSingle)
							return true;
					} else {// ��ʼ�������ʧ��
						fillbacksuc = false;
						System.out.println("DAG" + DAGID + "�ĵ�һ����������ʧ�ܣ��Ӷ�����ʧ��");
						return fillbacksuc;
					}
				}

				// ================��ѯ��ǰ��������и������Ƿ�����ɣ��������ͽ���ǰ��������������
				// ================�鿴��ǰʱ����û�о�������������������===========
				if (dag.getfillbackdone() == false&& dag.getfillbackready() == false) {
					ArrayList<Integer> pre = new ArrayList<Integer>();
					pre = dag.getpre();
					if (pre.size() > 0) {
						boolean ready = true;
						for (int j = 0; j < pre.size(); j++) {
							Task buf = new Task();
							buf = getDAGById(dag.getdagid(), pre.get(j));
							if (buf.getfillbackdone() && buf.getfillbackready()) {
								continue;
							} else {
								ready = false;
								break;
							}
						}
						if (ready) {
							// ��ǰ��������������
							readylist.add(dag);
							dag.setprefillbackready(true);
							dag.setprefillbackdone(true);
							dag.setfillbackready(true);
						}
					}
				}
			}
			
			if (readylist.size() > 0) {
				if (!scheduling(dagmap, readylist)) {
					fillbacksuc = false;
					return fillbacksuc;
				}
				//System.out.println("�Ѿ�������һ��+++++++++++++++++++++++");
			}
						
			// ����ҵ�����е����񶼵��ȳɹ�����δ�������Ϊfalse����������ѭ��
			notfini = false;
			// ��ѯ��ǰʱ�̱�DAG�����е������Ƿ����Ѿ�ִ�гɹ������������񶼳ɹ�������ʱ��ѭ��
			// ����ҵ����û���ȳɹ�������
			for (Task dag : DAGTaskList) {
				if (dag.getfillbackdone() == false) {
					notfini = true;
					break;
				}
			}
			runtime = runtime + T;

		}

		// ����������������ҵ�Ļ������ʱ��
		if (!notfini) {
			for (Task dag : DAGTaskList) {
				dag.setfillbackfinishtime(dag.getfillbackstarttime()+ dag.getts());
			}
		} else {
			fillbacksuc = false;
			System.out.println("��DAGΪ" + dagmap.getDAGId()+ ",�����˳��������ǣ�ʱ���Ѿ������ˣ�������ҵ�л���û�б�������ɵ�����");
		}
		return fillbacksuc;
	}

	
	


	/**
	 * 
	 * @Title: restoreSlotandTASK
	 * @Description: ��ԭSlotListInPes��TASKListInPes
	 * @param SlotListInPestemp
	 *            �����ڻ�ԭ��SlotListInPes
	 * @param TASKListInPestemp
	 *            �����ڻ�ԭ��TASKListInPes
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
	 * @Description: ʹ��Backfilling���ȵ�i��DAG�������ȳɹ�������LevelRelaxing������
	 *               �����޸�TASKListInPes�и���TASK�Ŀ�ʼ����ʱ�䣬�����Ȳ��ɹ���ȡ����DAG��ִ��
	 * @param @param i��DAG��ID
	 * @param @param SlotListInPestemp�����ڻ�ԭ��SlotListInPes
	 * @param @param TASKListInPestemp�����ڻ�ԭ��TASKListInPes
	 * @return void
	 * @throws
	 */
	public static void scheduleOtherDAG(int i,LinkedHashMap<Integer, ArrayList> SlotListInPestemp,LinkedHashMap<Integer, LinkedHashMap> TASKListInPestemp) throws Exception {

		int arrive = DAGMapList.get(i).getsubmittime();
		if (arrive > current_time)
			current_time = arrive;
		// �жϱ�DAG��backfilling�����ܷ�ɹ�
		boolean fillbacksuc = fillback(DAGMapList.get(i));
		//System.out.println("===============��ǰ��ҵ+" + i + "�Ƿ���ȳɹ���" + fillbacksuc);

		// ������ɹ�
		if (!fillbacksuc) {
			// �޸�����֮ǰ����ò
			restoreSlotandTASK(SlotListInPestemp, TASKListInPestemp);

			DAGMapList.get(i).setfillbackdone(false);
			// ������ҵ����
			DAGMapList.get(i).setfillbackpass(true);

			// ������ҵ����������Ϊ���ԣ�pass��
			ArrayList<Task> DAGTaskList = new ArrayList<Task>();
			for (int j = 0; j < DAGMapList.get(i).gettasklist().size(); j++) {
				DAGTaskList.add((Task) DAGMapList.get(i).gettasklist().get(j));
				DAGTaskList.get(j).setfillbackpass(true);
			}

		} else { // �����DAG��backfilling�����ɹ�
			DAGMapList.get(i).setfillbackdone(true);
			DAGMapList.get(i).setfillbackpass(false);		
			//printTaskAndSlot(1);
			repairTaskList();
		}

		// System.out.println("===============��һ���᲻��ɹ���"+DAGMapList.get(0).getfillbackdone());
	}

	/**
	 * 
	 * @Title: repairTaskList
	 * @Description: ����TaskList�е�ʱ������
	 * @throws
	 */
	private static void repairTaskList() {

		for (int k = 0; k < TASKListInPes.size(); k++) {

			LinkedHashMap<Integer, Integer[]> TASKInPe = TASKListInPes.get(k);
			for (int j = 0; j < TASKInPe.size(); j++) {
				Integer[] temp = new Integer[4];
				temp = TASKInPe.get(j);

				Task tempDag = new Task();
				tempDag = getDAGById(temp[2], temp[3]);

				temp[0] = tempDag.getfillbackstarttime();
				temp[1] = tempDag.getfillbackfinishtime();
				TASKInPe.put(j, temp);
			}
			TASKListInPes.put(k, TASKInPe);
		}

	}

	/**
	 * @throws IOException
	 * 
	 * @Title: printTaskAndSlot
	 * @Description: TODO:
	 * @throws
	 */
	private static void printTaskAndSlot(int index) throws IOException {
		// TODO Auto-generated method stub

		FileWriter writer = new FileWriter("G:\\task.txt", true);
		FileWriter slotWriter = new FileWriter("G:\\slot.txt", true);

		for (int i = 0; i < pe_number; i++) {
			LinkedHashMap<Integer, Integer[]> taskList = TASKListInPes.get(i);
			ArrayList slotList = SlotListInPes.get(i);
			
			for (int j = 0; j < taskList.size(); j++) {
				Integer[] result = taskList.get(j);
				Task tempDag = new Task();
				tempDag = getDAGById(result[2], result[3]);
				writer.write(index+ "\t��ǰ��������" + i + ":" + j + "\t����DAG"
						+ tempDag.getdagid() + "��" + tempDag.getid() + "\t��ʼʱ��"
						+ result[0] + "=" + tempDag.getfillbackstarttime()
						+ "\t����ʱ��" + result[1] + "="
						+ tempDag.getfillbackfinishtime() + "\t���񳤶ȣ�"
						+ tempDag.getlength() + "\n");
			}

			for (int k = 0; k < slotList.size(); k++) {
				Slot tempSlot = (Slot) slotList.get(k);
				slotWriter.write(index + "\t��������ţ�" + tempSlot.getPEId() + ":"
						+ tempSlot.getslotId() + "\t��ʼʱ��"
						+ tempSlot.getslotstarttime() + "\t����ʱ��"
						+ tempSlot.getslotfinishtime() + "\n");
			}

		}
		writer.write("**********************************************\n");
		slotWriter.write("**********************************************\n");
		if (writer != null) {
			writer.close();
		}
		if (slotWriter != null) {
			slotWriter.close();
		}

	}

	/**
	 * 
	 * @Title: printTest
	 * @Description: ���Դ�ӡ
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
			Task tempDag = getDAGById(dagid, o);
			ArrayList<Integer> pre = tempDag.getpre();
			for (int p : pre) {
				Task tempPre = getDAGById(dagid, p);
				// if(tempPre.getfillbackstarttime()>tempDag.getfillbackstarttime())
				writer.write("DAG��id=" + i + "��������id=" + p + ";��ʼʱ�䣺"
						+ tempPre.getfillbackstarttime() + ";������id=" + o
						+ ";������ʼʱ�䣺" + tempDag.getfillbackstarttime() + "\n");
			}
		}
		if (writer != null) {
			writer.close();
		}

		/**
		 * 
		 */
	}

	/**
	 * 
	 * @Title: copySlot
	 * @Description: 
	 *               �������ڵ�SlotListInPes������ŵ������д���������ƥ�䵱ǰDAG��subimit--deadlineʱ��ε�slot
	 *               �������ڻ�ԭ
	 * @param @return
	 * @return LinkedHashMap��SlotListInPestemp
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
	 * @Description: �������ڵ�TASKListInPes�����ڻ�ԭ
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
	 * @Description:����DAGMAPʵ������ʼ��
	 * 
	 * @param dagdepend
	 *            ��������������ϵ
	 * @param vcc
	 *            ����������
	 */
	public static void initdagmap(DAGDepend dagdepend, PEComputerability vcc,String pathXML) throws Throwable {
		int pre_exist = 0;
	
		File file = new File(pathXML);
		String[] fileNames = file.list();
		
		/**
		 * �����Ϊ-2����Ϊ����ǰ���ɵ�deadlineOld���ļ�������ɾ�����ˣ���������Ŷ��
		 */
		int num = fileNames.length - 1;	
		BufferedReader bd = new BufferedReader(new FileReader(pathXML+ "Deadline.txt"));
		String buffered;

		for (int i = 0; i < num; i++) {
			// ÿ��DAG��һ��dagmap
			DAG dagmap = new DAG();
			DAGDepend dagdepend_persional = new DAGDepend();
			
			DAG_queue_personal.clear();

			// ��ȡDAG��arrivetime��deadline��task����
			buffered = bd.readLine();
			String bufferedA[] = buffered.split(" ");
			int buff[] = new int[4];

			buff[0] = Integer.valueOf(bufferedA[0].split("dag")[1]).intValue();// dagID
			buff[1] = Integer.valueOf(bufferedA[1]).intValue();// tasknum
			buff[2] = Integer.valueOf(bufferedA[2]).intValue();// arrivetime
			buff[3] = Integer.valueOf(bufferedA[3]).intValue();// deadline
			int deadline = buff[3];
			// ����Ҳ�ǰ�����Щ��ǰ����ȥ��0.0.0�Ľڵ�
			int tasknum = buff[1];
			// ��Ǳ���ҵ�Ƿ��ǵ�������ҵ
			if (tasknum == 1)
				dagmap.setSingle(true);

			taskTotal = taskTotal + tasknum;
			int arrivetime = buff[2];

			// ��ÿ��DAG��������������������Լ�������Ϣ
			pre_exist = initDAG_createDAGdepend_XML(i, pre_exist, tasknum,arrivetime, pathXML);

			vcc.setComputeCostMap(ComputeCostMap);
			vcc.setAveComputeCostMap(AveComputeCostMap);

			dagdepend_persional.setDAGList(DAG_queue_personal);
			dagdepend_persional.setDAGDependMap(DAGDependMap_personal);
			dagdepend_persional.setDAGDependValueMap(DAGDependValueMap_personal);

			// �Ժ���ǰ����DAG��ÿ������Ľ�ֹʱ��
			
			//createDeadline_XML(deadline);

			// ΪDAG_queue�е��������ý�ֹʱ��
			int number_1 = DAG_queue.size();
			int number_2 = DAG_queue_personal.size();
			for (int k = 0; k < number_2; k++) {
				DAG_queue.get(number_1 - number_2 + k).setdeadline(DAG_queue_personal.get(k).getdeadline());
			}

			dagmap.settasknumber(tasknum);
			dagmap.setDAGId(i);
			dagmap.setDAGdeadline(deadline);
			dagmap.setsubmittime(arrivetime);
			dagmap.settasklist(DAG_queue_personal);
			dagmap.setDAGDependMap(DAGDependMap_personal);
			dagmap.setdependvalue(DAGDependValueMap_personal);
			
			createDeadline(deadline, dagdepend_persional, dagmap);
			
			//System.out.println("��ʼ��������");
			DAGMapList.add(dagmap);
			DAGDependList.add(dagdepend_persional);
			

			
		}

//
//		System.out.println("���ϲ�����ҵ��ԭʼid���ϣ�" + mergeId.size()
//				+ "\t�ϲ��Ĵ���ҵ��ÿ��С��ҵ�����һ������ļ��ϣ�" + mergeDAGEndNode.size()
//				+ "\t�ϲ��Ĵ���ҵ�У��ɹ����ȵ�ԭʼ��ҵid��" + successMergeJob.size());

		// ��������DAGxml�ļ����ɵ�
		dagdepend.setdagmaplist(DAGMapList);
		dagdepend.setDAGList(DAG_queue);
		dagdepend.setDAGDependMap(DAGDependMap);
		dagdepend.setDAGDependValueMap(DAGDependValueMap);
	}
	

	private static void createDeadline_XML(int dead_line) throws Throwable {
        //�������ļ�������
        int maxability = 1;
        /**
         * �������Ǹ�ʱ�䴰�ڲ�������̫���bug���ڵ�
         */
        int max = Integer.MAX_VALUE;
        for (int k = DAG_queue_personal.size() - 1; k >= 0; k--) {

            ArrayList<Integer> suc = new ArrayList<Integer>();
            //��ȡ��task����task�б�
            suc = DAG_queue_personal.get(k).getsuc();

            if (suc.size() > 0) {
                for (int j = 0; j < suc.size(); j++) {
                    int tem;
                    Task subTask = getDAGById_task(suc.get(j));

                    //�����task��Ӧ������ʼʱ��
                    tem = (subTask.getdeadline() - (subTask.getlength() / maxability));

                    //��Ѱ������task����ʼʱ���������ʱ��
                    if (max > tem)
                        max = tem;
                }
                DAG_queue_personal.get(k).setdeadline(max);
            } else {
            	DAG_queue_personal.get(k).setdeadline(dead_line);
            }

        }
    }

	
	/**
	 * 
	 * @Title: printInitDagMap
	 * @Description: ��ӡ��ʼ�����ɵ�����DAG������
	 * @param i
	 * @throws IOException
	 *             :
	 * @throws
	 */
	public static void printInitDagMap(int i) throws IOException {
		FileWriter writer = new FileWriter("G:\\DAGMapList.txt", true);
		DAG tempTestJob = DAGMapList.get(i);

		System.out.println("��ǰ��ҵ��������Ŀ==========>:"
				+ tempTestJob.gettasknumber() + "\t��ţ�" + i);

		int dagid = tempTestJob.DAGId;
		int mergeDagIndex = i;

		int num = tempTestJob.tasknumber;

		for (int o = 0; o < num; o++) {
			// DAG tempDag = getMergeDAGById(mergeDagIndex, o);
			Task tempDag = getDAGById(mergeDagIndex, o);
			writer.write("��ҵ��ţ�" + tempDag.getdagid() + ":" + tempDag.getid()
					+ "\t����ʱ�䣺" + tempDag.getarrive() + "\t����ʱ�䣺"
					+ tempDag.getdeadline() + "\tԭDAG��ţ�"
					+ tempDag.getOriDAGID() + ":" + tempDag.getOriTaskId() + "\n");
		}
		if (writer != null) {
			writer.close();
		}

	}

	// ===============================================================

	/**
	 * @Description:����DAX�ļ�ΪDAG����໥������ϵ�����´�����DAG_queue_personal��DAG_queue��AveComputeCostMap��ComputeCostMap��DAGDependValueMap_personal��DAGDependValueMap��DAGDependMap_personal��DAGDependMap
	 * 
	 * @param i
	 *            ��DAGID
	 * @param preexist
	 *            �������еĹ�������������ȫ����ӵ�һ�����У��ڱ�DAGǰ����preexist������
	 * @param tasknumber
	 *            ��DAG���������
	 * @param arrivetimes
	 *            ��DAG����ʱ��
	 * @return back�������еĹ�������������ȫ����ӵ�һ�����У��ڱ�DAGȫ����Ӻ���back������
	 */
	@SuppressWarnings("rawtypes")
	private static int initDAG_createDAGdepend_XML(int i, int preexist,
			int tasknumber, int arrivetimes, String pathXML)
			throws NumberFormatException, IOException, JDOMException {

		int back = 0;
		DAGDependMap_personal = new LinkedHashMap<Integer, Integer>();
		DAGDependValueMap_personal = new LinkedHashMap<String, Double>();
		ComputeCostMap = new LinkedHashMap<Integer, int[]>();
		AveComputeCostMap = new LinkedHashMap<Integer, Integer>();

		// ��ȡXML������
		SAXBuilder builder = new SAXBuilder();
		// ��ȡdocument����
		Document doc = builder.build(pathXML + "/dag" + (i + 1) + ".xml");
		// ��ȡ���ڵ�
		Element adag = doc.getRootElement();

		for (int j = 0; j < tasknumber; j++) {
			Task dag = new Task();
			Task dag_persional = new Task();

			dag.setid(Integer.valueOf(preexist + j).intValue());
			// Ϊÿ����������������DAG�ĵ���ʱ��
			dag.setarrive(arrivetimes);
			dag.setdagid(i);
			dag_persional.setid(Integer.valueOf(j).intValue());
			dag_persional.setarrive(arrivetimes);
			dag_persional.setdagid(i);

			XPath path = XPath
					.newInstance("//job[@id='" + j + "']/@tasklength");
			List list = path.selectNodes(doc);
			Attribute attribute = (Attribute) list.get(0);
			// x������ĳ���
			int x = Integer.valueOf(attribute.getValue()).intValue();
			dag.setlength(x);
			dag.setts(x);
			dag_persional.setlength(x);
			dag_persional.setts(x);

			if (j == tasknumber - 1) {
				dag.setislast(true);
			}

			DAG_queue.add(dag); // ����DAG�������б�
			DAG_queue_personal.add(dag_persional); // ��ǰDAG��һ���������������б�

			int sum = 0;
			int[] bufferedDouble = new int[PEList.size()];
			for (int k = 0; k < PEList.size(); k++) { // x������ĳ���
				bufferedDouble[k] = Integer.valueOf(x
						/ PEList.get(k).getability());
				sum = sum + Integer.valueOf(x / PEList.get(k).getability());
			}
			ComputeCostMap.put(j, bufferedDouble); // ��ǰ������ÿ���������ϵĴ�����
			AveComputeCostMap.put(j, (sum / PEList.size())); // ��ǰ���������д������ϵ�ƽ��������
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

			DAGDependMap.put(presuc[0], presuc[1]); // ����DAG�����������ӳ�䣬���ս���ŵ�������������һ��������
			DAGDependValueMap.put((presuc[0] + " " + presuc[1]),(double) datasize);
			DAG_queue.get(presuc[0]).addToSuc(presuc[1]);
			DAG_queue.get(presuc[1]).addToPre(presuc[0]);

			DAGDependMap_personal.put(Integer.valueOf(pre_suc[0]).intValue(),Integer.valueOf(pre_suc[1]).intValue());
			DAGDependValueMap_personal.put((pre_suc[0] + " " + pre_suc[1]),(double) datasize);

			int tem0 = Integer.parseInt(pre_suc[0]);
			int tem1 = Integer.parseInt(pre_suc[1]);
			DAG_queue_personal.get(tem0).addToSuc(tem1);
			DAG_queue_personal.get(tem1).addToPre(tem0);

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
	* @Description: ���ݹؼ�·����ȡÿ������Ľ�ֹʱ��
	* @param dead_line
	* @param dagdepend_persion
	* @param dagmap
	* @throws Throwable:
	* @throws
	 */
	private static void createDeadline(int dead_line,DAGDepend dagdepend_persion, DAG dagmap) throws Throwable {

		int max;
		//����ؼ�·���������ÿ��ֵõľ�ֵ
		int avgDiff=getRelaxDeadline(dead_line,dagdepend_persion,dagmap);		
		Map<String, Double> transferValueMap=dagdepend_persion.getDAGDependValueMap();
		
		//�Ӻ���ǰ��
		for (int k = DAG_queue_personal.size() - 1; k >= 0; k--) {
			max = Integer.MAX_VALUE;

			int from=DAG_queue_personal.get(k).getid();
			ArrayList<Integer> suc = new ArrayList<Integer>();
			suc = DAG_queue_personal.get(k).getsuc();

			// ѡ�������������������Ŀ�ʼʱ��Ϊ�Լ��Ľ�ֹʱ�䣬���Ե����ݵĴ��俪��
			if (suc.size() > 0) {
				for (int j = 0; j < suc.size(); j++) {
					int tem = 0;
					Task tempChild = new Task(); // ��ȡ��������������
					tempChild = getDAGById_task(suc.get(j));
					int to=suc.get(j);
					String key=from+" "+to;
					double value=transferValueMap.get(key);
					int childExe=tempChild.getlength()+avgDiff;
					
					tem=(int) (tempChild.getdeadline()-childExe-value);
					//System.out.println("������"+from+"\t ������"+to+"�Ľ�ֹʱ���ǣ�"+tempChild.getdeadline()+"\t������"+(tempChild.getlength()+avgDiff)+"����ʱ��Ϊ:"+value+" \t��ʱ�Ľ�ֹʱ��Ϊ��"+tem+"\t");
					
					if (max > tem)
						max = tem;
				}		
				DAG_queue_personal.get(k).setdeadline(max);

			} else {
				DAG_queue_personal.get(k).setdeadline(dead_line);
			}
		}
	}
	
	//============================��ؼ�·��
	/**
	 * @return 
	 * @param dagmap 
	 * 
	* @Title: createDeadlineCriticalPath
	* @Description: ͨ���ؼ�·���������µ�deadline
	* @param dead_line
	* @param dagdepend_persion
	* @throws Throwable:
	* @throws
	 */
	private static int getRelaxDeadline(int dead_line,DAGDepend dagdepend, DAG dagmap) throws Throwable {
		int taskSize = dagmap.gettasknumber();
		ArrayList<Task> taskList=dagmap.gettasklist();
		Stack<Integer> topo = new Stack<Integer>(); // ��������Ķ���ջ
		int[] ve = null; //	����������緢��ʱ��
		int[] vl = null; // ���������ٷ���ʱ��

		//���ͼ����������ѹ��ջ�У����õ�ÿ����������翪ʼʱ��
		ve=topologicalSort(dagmap,ve,topo,dagdepend);
		ArrayList<Integer> topoList=new ArrayList<>();
		while (!topo.isEmpty()) {
			topoList.add(topo.pop());
		}
		//���б��򣬴�С��������
		Collections.reverse(topoList);
		
//		StringBuffer sb=new StringBuffer();
//		for(int k=0;k<topoList.size();k++){
//			sb.append(topoList.get(k)).append(" ");
//		
//		}
//		System.out.println("ԭ�������˽ṹ��"+sb.toString());
		
		
		//�õ��ؼ�·��
		criticalPath(topoList,ve,dagmap,dagdepend);
		
		//�õ�����DAG��������
		int levelNum=getMaxLevelNum(topoList,dagmap,dagdepend);
		//System.out.println("ԭ��������:"+levelNum);

		//���ÿ�������ƽ����������
		int endIndex=topoList.get(topoList.size()-1);
		Task tempDag=taskList.get(endIndex);
		int newDeadline=ve[topoList.get(topoList.size()-1)]+tempDag.getlength()+dagmap.getsubmittime();
		int deadline=dagmap.getDAGdeadline();
		
		int deadlineDiff=deadline-newDeadline;
		
		int ex=(deadlineDiff)/levelNum;
		//System.out.println(dagmap.getDAGId()+"\tnewDeadline="+newDeadline+"\tdeadline="+deadline+"\tdeadlineDiff="+deadlineDiff+"\tlevelNum="+levelNum+"\tdeadline_ex="+ex);
		return ex;
		
	}
	
	


	/**
	 * 
	* @Title: topologicalSort
	* @Description: ������������򣬷��ص����������������翪ʼִ��ʱ��
	* @param dagmap
	* @param ve
	* @param topo
	* @param dagdepend
	* @return:
	* @throws
	 */
	private static int[] topologicalSort(DAG dagmap, int[] ve,  Stack topo, DAGDepend dagdepend) {
			int count = 0; //����������
	        int[] inDegree = findInDegree(dagmap); //�������������
	        int taskSize=dagmap.gettasknumber();
	        Stack<Integer> noInputTask = new Stack<Integer>();  //����ȵ� ����ջ
	        
	        ArrayList<Task> taskList=new ArrayList<>();
			taskList=dagmap.gettasklist();			
	        //�ҵ���һ�����Ϊ0�Ľڵ�
	        for(int i = 0; i < taskSize; i++){
	        	if(inDegree[i] == 0){
	        		noInputTask.push(i);  //���Ϊ0�Ľ�ջ
	        	}	
	        }
	        ve = new int[taskSize]; //��ʼ��,����洢���Ǹ����ڵ�����翪ʼִ��ʱ��

	        Map<String,Double> valueMap=dagmap.getdependvalue();
	        while( !noInputTask.isEmpty()){
	        	//��ǰ������
	            int currentTask = (Integer) noInputTask.pop();
	            Task curDag=taskList.get(currentTask);
	            topo.push(currentTask); //i�Ŷ�����Tջ������ 
	            
	            for(int m=0;m<taskSize;m++){
	            	
	            	Task tempDag=taskList.get(m);
	            	ArrayList<Integer> parents=tempDag.getpre();
	            	
	            	//��ǰɾ�����������������ĸ�����
	            	if(parents.contains(currentTask)){
	            		inDegree[m]--;
	            		if(inDegree[m]==0){
	            			noInputTask.push(m);//��ǰ������û�����
	            		}
	            		String key=currentTask+" "+m;
	            		Double value=valueMap.get(key)+curDag.getlength();//�ü���ִ��ʱ��
	            		 if(ve[currentTask] + value> ve[m])
	 	                    ve[m] = (int) (ve[currentTask] + value);
	            	}
	            }
	        } 
	        
//	        for(Entry<String, Double> map:valueMap.entrySet()){
//	        	System.out.println(""+map.getKey()+"\t���룺"+map.getValue());
//	        }
	        return ve;
	}

	/**
	 * 
	* @Title: criticalPath
	* @Description: ������ؼ�·�������عؼ�·�����������
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
         // ��ʼ���������¼�����ٷ���ʱ��Ϊ���һ����������ʱ��
         for(int i = 0; i < taskSize; i++){
             vl[i] = ve[taskSize - 1]; 
         }
		Map<String, Double> valueMap = dagdepend.getDAGDependValueMap();
         for(int k=topoList.size()-1;k>=0;k--){
			int currentTask = (int) topoList.get(k);// �õ���ǰ����ı��

			Task tempDag = taskList.get(currentTask);
			ArrayList<Integer> childs = tempDag.getsuc();
			if (childs == null)
				continue;
			
			for (int p = 0; p < childs.size(); p++) {
				int childNo=childs.get(p);
				String key = currentTask + " " + childNo;
				Double value = valueMap.get(key);			
				int diff = (int) (vl[childNo] - value - tempDag.getlength());
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
            	// System.out.println("�ؼ�·����"+i);
            	 criticalNum++;
             }
         }         
         return criticalNum;
    } 

	
	/**
	 * @param topoList 
	 * 
	* @Title: getMaxLevelNum
	* @Description: ��ȡDAG��������
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
			int currentTask = (int) topoList.get(k);// �õ���ǰ����ı��	
			Task tempDag = taskList.get(currentTask);
			ArrayList<Integer> childs = tempDag.getsuc();
			
			if (childs == null){//�����һ���ڵ�
				level[currentTask]=1;
			}else{
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
	* @Description: ��ø���������Ӧ�����
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
	 * @Description: ����PEʵ������ʼ�������ô������ļ�������Ϊ1
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
	 * @Description:����DAGID��TASKID���ظ�TASKʵ��
	 * 
	 * @param DAGId
	 *            ��DAGID
	 * @param dagId
	 *            ��TASKID
	 * @return DAG��TASKʵ��
	 */
	private static Task getDAGById(int DAGId, int dagId) {

		for (int i = 0; i < DAGMapList.get(DAGId).gettasknumber(); i++) {
			Task temp = (Task) DAGMapList.get(DAGId).gettasklist().get(i);
			if (temp.getid() == dagId)
				return temp;
		}

		return null;
	}


	/**
	 * @Description:����TASKID���ظ�TASKʵ��
	 * 
	 * @param dagId
	 *            ��TASKID
	 * @return DAG��TASKʵ��
	 */
	private static Task getDAGById_task(int dagId) {
		for (Task dag : DAG_queue_personal) {
			if (dag.getid() == dagId)
				return dag;
		}
		return null;
	}

}
