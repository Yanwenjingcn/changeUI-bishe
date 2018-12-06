package org.schedule.original;

import org.generate.util.CommonParametersUtil;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.schedule.model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;


/**
 * 没有加注释和改变的
 */
public class WorkflowBased {

	// processor list
	private static ArrayList<PE> PEList;

	private static ArrayList<DAG> DAGMapList;

	private static ArrayList<Task> DAG_queue;
	// ready list
	private static ArrayList<Task> readyqueue;
	// dependence between tasks
	private static HashMap<Integer, Integer> DAGDependMap;
	// transfer data between task
	private static HashMap<String, Double> DAGDependValueMap;

	private static ArrayList<Task> DAG_queue_personal;

	private static HashMap<Integer, Integer> DAGDependMap_personal;

	private static HashMap<String, Double> DAGDependValueMap_personal;

	private static Map<Integer, int[]> ComputeCostMap;

	private static Map<Integer, Integer> AveComputeCostMap;
	// statistical data storage array
	public static String[][] rateResult = new String[1][4];
	// finish task number
	private static int islastnum = 0;

	private static double deadLineTimes = 1.1;
	// processor number
	private static int peNumber = 8;
	// statistical data storage array
	public static String[][] rate = new String[5][2];
	// current time
	public static int currentTime;

	public static int proceesorEndTime = CommonParametersUtil.timeWindow;

	public static int timeWindow;

	public static int T = 1;

	public static int fillBackTaskNum = 100000000;

	public static int[][] message;
	// Maximum number of tasks
	public static int dagNumMax = 10000;
	// Maximum time window
	public static int timewindowmax = 9000000;
	public static int mesnum = 5;

	private static HashMap<Integer, ArrayList> SlotListInPes;

	private static HashMap<Integer, HashMap> TASKListInPes;

	private static int[] pushFlag;

	private static int pushCount = 0;

	private static int pushSuccessCount = 0;

	private static int taskTotal = 0;

	private static int[][] dagResultMap = null;

	public WorkflowBased() {
		readyqueue = new ArrayList<Task>();
		DAG_queue = new ArrayList<Task>();
		DAG_queue_personal = new ArrayList<Task>();
		PEList = new ArrayList<PE>();
		DAGMapList = new ArrayList<DAG>();
		DAGDependMap = new HashMap<Integer, Integer>();
		DAGDependValueMap = new HashMap<String, Double>();
		deadLineTimes = CommonParametersUtil.deadLineTimes;
		peNumber = CommonParametersUtil.processorNumber;
		currentTime = 0;
		timeWindow = proceesorEndTime / peNumber;
		pushFlag = new int[peNumber];
		dagResultMap = new int[1000][dagNumMax];

		message = new int[dagNumMax][mesnum];

		SlotListInPes = new HashMap<Integer, ArrayList>();

		TASKListInPes = new HashMap<Integer, HashMap>();
		for (int i = 0; i < peNumber; i++) {
			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			TASKListInPes.put(i, TASKInPe);
		}
	}

	/**
	 * 
	 * @Title: runMakespan
	 * @Description:
	 * @param @throws Throwable
	 * @return void
	 * @throws
	 */
	public void runMakespan(String pathXML, String resultPath) throws Throwable {

		// init dagmap
		WorkflowBased fb = new WorkflowBased();
		DAGDepend dagdepend = new DAGDepend();
		PEComputerability vcc = new PEComputerability();
		initPE();

		initDagMap(dagdepend, vcc, pathXML);

		Date begin = new Date();
		Long beginTime = begin.getTime();
		// schedule first DAG
		scheduleFirstDAG();
		// set current time
		currentTime = DAGMapList.get(0).getsubmittime();
		for (int i = 1; i < DAGMapList.size(); i++) {
			HashMap<Integer, ArrayList> SlotListInPestemp = new HashMap<Integer, ArrayList>();
			HashMap<Integer, HashMap> TASKListInPestemp = new HashMap<Integer, HashMap>();

			computeSlot(DAGMapList.get(i).getsubmittime(), DAGMapList.get(i)
					.getDAGdeadline());
			SlotListInPestemp = copySlot();
			TASKListInPestemp = copyTASK();
			scheduleOtherDAG(i, SlotListInPestemp, TASKListInPestemp);
		}
		Date end = new Date();
		Long endTime = end.getTime();
		Long diff = endTime - beginTime;
		outputResult(diff, resultPath);
		storeResultShow();
	}

	/**
	 * 
	 * @Title: outputResult
	 * @Description: output schedule algorithm result
	 * @param diff
	 * @param resultPath:
	 * @throws
	 */
	public static void outputResult(Long diff, String resultPath) {
		int suc = 0;
		int fault = 0;
		int effective = 0;
		int tempp = timeWindow;

		for (int j = 0; j < DAGMapList.size(); j++) {
			ArrayList<Task> DAGTaskList = new ArrayList<Task>();
			for (int i = 0; i < DAGMapList.get(j).gettasklist().size(); i++) {
				Task dag_temp = (Task) DAGMapList.get(j).gettasklist().get(i);
				DAGTaskList.add(dag_temp);
			}

			if (DAGMapList.get(j).getfillbackdone()) {
				suc++;
				for (int i = 0; i < DAGMapList.get(j).gettasklist().size(); i++) {
					effective = effective + DAGTaskList.get(i).getts();
				}
			}

			if (!DAGMapList.get(j).getfillbackdone()) {
				fault++;
			}
		}

		DecimalFormat df = new DecimalFormat("0.0000");
		System.out.println("fillbackFIFOWithoutInsert:");
		System.out.println("PE's use ratio is "
				+ df.format((float) effective / (peNumber * tempp)));
		System.out.println("effective PE's use ratio is "
				+ df.format((float) effective / (tempp * peNumber)));
		System.out.println("Task Completion Rates is "
				+ df.format((float) suc / DAGMapList.size()));
		System.out.println();

		rateResult[0][0] = df.format((float) effective / (peNumber * tempp));
		rateResult[0][1] = df.format((float) effective / (tempp * peNumber));
		rateResult[0][2] = df.format((float) suc / DAGMapList.size());

		rateResult[0][3] = df.format(diff);
		int count = 0;
	}

	/**
	 * 
	 * @Title: storeresultShow
	 * @Description: store result
	 * @param
	 * @return void
	 * @throws
	 */
	public static void storeResultShow() {
		int dagcount = 0;
		for (DAG dagmap : DAGMapList) {

			if (dagmap.fillbackdone) {
				ArrayList<Task> DAGTaskList = new ArrayList<Task>();

				for (int i = 0; i < dagmap.gettasklist().size(); i++) {
					Task dag = (Task) dagmap.gettasklist().get(i);
					DAGTaskList.add(dag);
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

	/**
	 * 
	* @Title: computeSlot
	* @Description: calculate appropriate block for DAG
	* @param submit
	* @param deadline:
	* @throws
	 */
	public static void computeSlot(int submit, int deadline) {

		SlotListInPes.clear();

		for (int i = 0; i < peNumber; i++) {
			int Slotcount = 0;

			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(i);
			ArrayList<Slot> slotListinpe = new ArrayList<Slot>();
			ArrayList<Slot> slotListinpe_ori = new ArrayList<Slot>();

			if (TASKInPe.size() == 0) {
				Slot tem = new Slot();
				tem.setPEId(i);
				tem.setslotId(Slotcount);
				tem.setslotstarttime(submit);
				tem.setslotfinishtime(deadline);
				slotListinpe.add(tem);
				Slotcount++;
			} else if (TASKInPe.size() == 1) {

				if (TASKInPe.get(0)[0] > submit) {
					if (deadline <= TASKInPe.get(0)[0]) {
						Slot tem = new Slot();
						ArrayList<String> below_ = new ArrayList<String>();
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

						below_.add(TASKInPe.get(0)[2] + " "
								+ TASKInPe.get(0)[3] + " " + 0);
						tem.setPEId(i);
						tem.setslotId(Slotcount);
						tem.setslotstarttime(submit);
						tem.setslotfinishtime(TASKInPe.get(0)[0]);
						tem.setbelow(below_);
						slotListinpe.add(tem);
						Slotcount++;
						Slot temp = new Slot();

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
					tem.setPEId(i);
					tem.setslotId(Slotcount);
					tem.setslotstarttime(TASKInPe.get(0)[1]);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);
					Slotcount++;
				} else if (submit > TASKInPe.get(0)[1]
						&& deadline > TASKInPe.get(0)[1]) {
					Slot tem = new Slot();

					tem.setPEId(i);
					tem.setslotId(Slotcount);
					tem.setslotstarttime(submit);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);
					Slotcount++;
				}
			} else {
				if (TASKInPe.get(0)[0] >= 0) {
					Slot tem = new Slot();
					ArrayList<String> below_ = new ArrayList<String>();
					for (int k = 0; k < TASKInPe.size(); k++) {
						below_.add(TASKInPe.get(k)[2] + " "
								+ TASKInPe.get(k)[3] + " " + 0);
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
							below_.add(TASKInPe.get(k)[2] + " "
									+ TASKInPe.get(k)[3] + " " + j);
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

					if (j == (slotListinpe_ori.size() - 1))
						startslot = slotListinpe_ori.size();
				}
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

				if (TASKInPe.get(TASKInPe.size() - 1)[1] <= submit) {
					Slot tem = new Slot();
					tem.setPEId(i);
					tem.setslotId(count);
					tem.setslotstarttime(submit);
					tem.setslotfinishtime(deadline);
					slotListinpe.add(tem);

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
	 * @Title: changeInPE
	 * @Description: modified result
	 * @param slotlistinpe
	 * @param inpe:
	 * @throws
	 */
	public static void changeInPE(ArrayList<Slot> slotlistinpe, int inpe) {
		ArrayList<String> below = new ArrayList<String>();

		for (int i = 0; i < slotlistinpe.size(); i++) {
			ArrayList<String> belowte = new ArrayList<String>();

			Slot slottem = slotlistinpe.get(i);

			for (int j = 0; j < slottem.getbelow().size(); j++) {
				below.add(slottem.getbelow().get(j));
			}

			String belowbuf[] = below.get(0).split(" ");
			int buffer = Integer.valueOf(belowbuf[2]).intValue();
			if (buffer >= inpe) {
				buffer += 1;
				for (int j = 0; j < below.size(); j++) {
					String belowbuff = belowbuf[0] + " " + belowbuf[1] + " "
							+ buffer;
					belowte.add(belowbuff);
				}
				slottem.getbelow().clear();
				slottem.setbelow(belowte);
			}
		}
	}

	/**
	 * 
	 * @Title: changeTaskListInPE
	 * @Description: modified result
	 * @param dagmap:
	 * @throws
	 */
	private static void changeTaskListInPE(DAG dagmap) {
		for (int i = 0; i < peNumber; i++) {
			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(i);
			for (int j = 0; j < TASKInPe.size(); j++) {
				if (TASKInPe.get(j)[2] == dagmap.getDAGId()) {
					Task temp = new Task();
					temp = getDAGById(TASKInPe.get(j)[2], TASKInPe.get(j)[3]);
					TASKInPe.get(j)[0] = temp.getfillbackstarttime();
					TASKInPe.get(j)[1] = temp.getfillbackfinishtime();
				}
			}
			TASKListInPes.put(i, TASKInPe);
		}
	}

	/**
	 * 
	 * @Title: scheduling
	 * @Description: start scheduling
	 * @param dagmap
	 * @param readylist
	 * @return:
	 * @throws
	 */
	public static boolean scheduling(DAG dagmap, ArrayList<Task> readylist) {
		boolean findsuc = true;

		do {
			int finimin = timewindowmax;
			int mindag = -1;
			int message[][] = new int[readylist.size()][6];
			// 0 is if success 1 means success 0 means fail,
			// 1 is earliest starttime
			// 2 is peid
			// 3 is slotid
			// 4 is if need slide
			// 5 is slide length

			int[] finish = new int[readylist.size()];

			for (int i = 0; i < readylist.size(); i++) {
				Task dag = new Task();
				dag = readylist.get(i);
				message[i] = findSlot(dagmap, dag);
				finish[i] = message[i][1] + dag.getts();
			}

			int dagId = dagmap.getDAGId();

			for (int i = 0; i < readylist.size(); i++) {
				Task tempDagResult = readylist.get(i);

				if (message[i][0] == 0) {
					dagResultMap[dagId][tempDagResult.getid()] = 1;
					findsuc = false;
					// return findsuc;
				}
			}

			if (findsuc == false) {
				return findsuc;
			}

			for (int i = 0; i < readylist.size(); i++) {
				if (finimin > finish[i]) {
					finimin = finish[i];
					mindag = i;
				}
			}

			ArrayList<Task> DAGTaskList = new ArrayList<Task>();
			Task dagtemp = new Task();
			for (int i = 0; i < dagmap.gettasklist().size(); i++) {
				Task dag = new Task();
				DAGTaskList.add((Task) dagmap.gettasklist().get(i));
				dag = (Task) dagmap.gettasklist().get(i);
				if (dag.getid() == readylist.get(mindag).getid())
					dagtemp = (Task) dagmap.gettasklist().get(i);
			}

			int startmin = finimin - readylist.get(mindag).getts();
			int pemin = message[mindag][2];
			int slotid = message[mindag][3];
			dagtemp.setfillbackstarttime(startmin);
			dagtemp.setfillbackpeid(pemin);
			dagtemp.setfillbackready(true);
			dagtemp.setprefillbackdone(true);
			dagtemp.setprefillbackdone(true);

			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(pemin);

			// 0 is if success 1 means success 0 means fail,
			// 1 is earliest starttime
			// 2 is peid
			// 3 is slotid
			// 4 is if need slide
			// 5 is slide length

			if (message[mindag][4] == 1) {
				int slide = message[mindag][5];
				ArrayList<String> below = new ArrayList<String>();
				ArrayList<Slot> slotafter = new ArrayList<Slot>();

				Slot slottemp = new Slot();
				ArrayList<Slot> slotlistinpe = new ArrayList<Slot>();
				for (int j = 0; j < SlotListInPes.get(pemin).size(); j++)
					slotlistinpe.add((Slot) SlotListInPes.get(pemin).get(j));
				for (int j = 0; j < slotlistinpe.size(); j++) {
					if (slotlistinpe.get(j).getslotId() == slotid) {
						slottemp = slotlistinpe.get(j);
						break;
					}
				}

				for (int i = 0; i < slottemp.getbelow().size(); i++) {
					below.add(slottemp.getbelow().get(i));
				}

				for (int j = 0; j < slotlistinpe.size(); j++) {
					if (slotlistinpe.get(j).getslotstarttime() > slottemp
							.getslotstarttime()) {
						slotafter.add(slotlistinpe.get(j));
					}
				}

				if (below.size() <= fillBackTaskNum) {
					int count = 0;

					for (int i = 0; i < below.size(); i++) {
						boolean flag = false;
						String buf[] = below.get(i).split(" ");

						int DAGId = Integer.valueOf(buf[0]).intValue();
						int TASKId = Integer.valueOf(buf[1]).intValue();
						int inpe = Integer.valueOf(buf[2]).intValue();

						Task dag_temp = new Task();
						dag_temp = getDAGById(DAGId, TASKId);

						int temp = slide;
						if (count < slotafter.size()) {

							if ((dag_temp.getfillbackstarttime() + dag_temp
									.getts()) == slotafter.get(count)
									.getslotstarttime()) {

								if ((slotafter.get(count).getslotfinishtime() - slotafter
										.get(count).getslotstarttime()) < slide) {
									slide = slide
											- (slotafter.get(count)
													.getslotfinishtime() - slotafter
													.get(count)
													.getslotstarttime());
								} else {
									flag = true;
								}
								count++;
							}
						}

						getDAGById(DAGId, TASKId).setfillbackstarttime(
								getDAGById(DAGId, TASKId)
										.getfillbackstarttime() + temp);

						if (getDAGById(DAGId, TASKId).getfillbackfinishtime() != 0)
							getDAGById(DAGId, TASKId).setfillbackfinishtime(
									getDAGById(DAGId, TASKId)
											.getfillbackfinishtime() + temp);

						TASKInPe.get(inpe + i)[0] = getDAGById(DAGId, TASKId)
								.getfillbackstarttime();
						TASKInPe.get(inpe + i)[1] = getDAGById(DAGId, TASKId)
								.getfillbackstarttime()
								+ getDAGById(DAGId, TASKId).getts();
						TASKInPe.get(inpe + i)[2] = DAGId;
						TASKInPe.get(inpe + i)[3] = TASKId;

						if (flag)
							break;
					}

				} else {
					int count = 0;
					for (int i = 0; i < fillBackTaskNum; i++) {
						boolean flag = false;
						String buf[] = below.get(i).split(" ");
						int DAGId = Integer.valueOf(buf[0]).intValue();
						int TASKId = Integer.valueOf(buf[1]).intValue();
						int inpe = Integer.valueOf(buf[2]).intValue();
						Task dag_temp = new Task();
						dag_temp = getDAGById(DAGId, TASKId);

						int temp = slide;

						if (count < slotafter.size()) {
							if ((dag_temp.getfillbackstarttime() + dag_temp
									.getts()) == slotafter.get(count)
									.getslotstarttime()) {
								if ((slotafter.get(count).getslotfinishtime() - slotafter
										.get(count).getslotstarttime()) < slide) {
									slide = slide
											- (slotafter.get(count)
													.getslotfinishtime() - slotafter
													.get(count)
													.getslotstarttime());
								} else {
									slide = 0;
									flag = true;
								}
								count++;
							}
						}

						getDAGById(DAGId, TASKId).setfillbackstarttime(
								getDAGById(DAGId, TASKId)
										.getfillbackstarttime() + temp);

						if (getDAGById(DAGId, TASKId).getfillbackfinishtime() != 0)
							getDAGById(DAGId, TASKId).setfillbackfinishtime(
									getDAGById(DAGId, TASKId)
											.getfillbackfinishtime() + temp);

						TASKInPe.get(inpe + i)[0] = getDAGById(DAGId, TASKId)
								.getfillbackstarttime();
						TASKInPe.get(inpe + i)[1] = getDAGById(DAGId, TASKId)
								.getfillbackstarttime()
								+ getDAGById(DAGId, TASKId).getts();
						TASKInPe.get(inpe + i)[2] = DAGId;
						TASKInPe.get(inpe + i)[3] = TASKId;

						if (flag)
							break;
					}

				}
			}

			if (TASKInPe.size() > 0) {
				ArrayList<Slot> slotlistinpe = new ArrayList<Slot>();

				for (int j = 0; j < SlotListInPes.get(pemin).size(); j++)
					slotlistinpe.add((Slot) SlotListInPes.get(pemin).get(j));

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

				if (below.size() > 0) {
					String buf[] = below.get(0).split(" ");

					int inpe = Integer.valueOf(buf[2]).intValue();

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
					st_fi[1] = finimin;
					st_fi[2] = dagtemp.getdagid();
					st_fi[3] = dagtemp.getid();
					TASKInPe.put(inpe, st_fi);

					dagtemp.setisfillback(true);

					changeInPE(slotlistinpe, inpe);

				} else {
					Integer[] st_fi = new Integer[4];
					st_fi[0] = startmin;
					st_fi[1] = finimin;
					st_fi[2] = dagtemp.getdagid();
					st_fi[3] = dagtemp.getid();
					TASKInPe.put(TASKInPe.size(), st_fi);
				}

			} else {
				Integer[] st_fi = new Integer[4];
				st_fi[0] = startmin;
				st_fi[1] = finimin;
				st_fi[2] = dagtemp.getdagid();
				st_fi[3] = dagtemp.getid();
				TASKInPe.put(TASKInPe.size(), st_fi);
			}
			computeSlot(dagmap.getsubmittime(), dagmap.getDAGdeadline());

			readylist.remove(mindag);

		} while (readylist.size() > 0);

		return findsuc;
	}

	/**
	 * 
	 * @Title: findSlot
	 * @Description: find appropriate block
	 * @param dagmap
	 * @param dagtemp
	 * @return:
	 * @throws
	 */
	public static int[] findSlot(DAG dagmap, Task dagtemp) {
		int message[] = new int[6];

		boolean findsuc = false;
		int startmin = timewindowmax;
		int finishmin = timewindowmax;
		int pemin = -1;
		int slide;
		int[] startinpe = new int[peNumber];
		int[] slotid = new int[peNumber];
		int[] isneedslide = new int[peNumber]; // 0 means don't need 1 means need slide
		int[] slidelength = new int[peNumber];

		for (int k = 0; k < peNumber; k++) {
			pushFlag[k] = 0;
		}

		Map<String, Double> DAGTaskDependValue = new HashMap<String, Double>();
		DAGTaskDependValue = dagmap.getdependvalue();

		ArrayList<Task> pre_queue = new ArrayList<Task>();
		ArrayList<Integer> pre = new ArrayList<Integer>();
		pre = dagtemp.getpre();
		if (pre.size() >= 0) {
			for (int j = 0; j < pre.size(); j++) {
				Task buf = new Task();
				buf = getDAGById(dagtemp.getdagid(), pre.get(j));
				pre_queue.add(buf);
			}
		}

		for (int i = 0; i < peNumber; i++) {

			int predone = 0;

			if (pre_queue.size() == 1) {
				if (pre_queue.get(0).getfillbackpeid() == i) {
					predone = pre_queue.get(0).getfillbackfinishtime();
				} else {
					int value = (int) (double) DAGTaskDependValue.get(String
							.valueOf(pre_queue.get(0).getid())
							+ " "
							+ String.valueOf(dagtemp.getid()));
					predone = pre_queue.get(0).getfillbackfinishtime() + value;
				}
			} else if (pre_queue.size() >= 1) {
				for (int j = 0; j < pre_queue.size(); j++) {
					if (pre_queue.get(j).getfillbackpeid() == i) {
						if (predone < pre_queue.get(j).getfillbackfinishtime()) {
							predone = pre_queue.get(j).getfillbackfinishtime();
						}
					} else {
						int valu = (int) (double) DAGTaskDependValue.get(String
								.valueOf(pre_queue.get(j).getid())
								+ " "
								+ String.valueOf(dagtemp.getid()));
						int value = pre_queue.get(j).getfillbackfinishtime()
								+ valu;
						if (predone < value)
							predone = value;
					}
				}
			}

			startinpe[i] = -1;
			ArrayList<Slot> slotlistinpe = new ArrayList<Slot>();

			for (int j = 0; j < SlotListInPes.get(i).size(); j++)
				slotlistinpe.add((Slot) SlotListInPes.get(i).get(j));

			for (int j = 0; j < SlotListInPes.get(i).size(); j++) {
				int slst = slotlistinpe.get(j).getslotstarttime();
				int slfi = slotlistinpe.get(j).getslotfinishtime();

				if (predone <= slst) {
					if ((slst + dagtemp.getts()) <= slfi
							&& (slst + dagtemp.getts()) <= dagtemp
									.getdeadline()) {
						startinpe[i] = slst;
						slotid[i] = slotlistinpe.get(j).getslotId();
						isneedslide[i] = 0;
						break;
					} else if ((slst + dagtemp.getts()) > slfi
							&& (slst + dagtemp.getts()) <= dagtemp
									.getdeadline()) {
						continue;
					}
				} else if (predone > slst && predone < slfi) {
					if ((predone + dagtemp.getts()) <= slfi
							&& (predone + dagtemp.getts()) <= dagtemp
									.getdeadline()) {
						startinpe[i] = predone;
						slotid[i] = slotlistinpe.get(j).getslotId();
						isneedslide[i] = 0;
						break;
					} else if ((predone + dagtemp.getts()) > slfi
							&& (predone + dagtemp.getts()) <= dagtemp
									.getdeadline()) {
						continue;
					}
				}
			}
		}

		for (int i = 0; i < peNumber; i++) {
			if (startinpe[i] != -1) {
				findsuc = true;
				if (startinpe[i] < startmin) {
					startmin = startinpe[i];
					pemin = i;
				}
			}
		}
		// 0 is if success 1 means success 0 means fail, 1 is earliest start time, 2 is peid, 3 is slotid
		if (findsuc) {
			message[0] = 1;
			message[1] = startmin;
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
	 * 
	 * @Title: findFirstTaskSlot
	 * @Description: find first task appropriate block
	 * @param dagmap
	 * @param dagtemp
	 * @return:
	 * @throws
	 */
	public static boolean findFirstTaskSlot(DAG dagmap, Task dagtemp) {
		// perfinish is the earliest finish time minus task'ts time, the earliest start time

		boolean findsuc = false;
		int startmin = timewindowmax;
		int finishmin = 0;
		int pemin = -1;
		int slide;
		int[] startinpe = new int[peNumber];
		int[] slotid = new int[peNumber];

		for (int i = 0; i < peNumber; i++) {
			startinpe[i] = -1;
			ArrayList<Slot> slotlistinpe = new ArrayList<Slot>();
			for (int j = 0; j < SlotListInPes.get(i).size(); j++)
				slotlistinpe.add((Slot) SlotListInPes.get(i).get(j));

			for (int j = 0; j < SlotListInPes.get(i).size(); j++) {
				int slst = slotlistinpe.get(j).getslotstarttime();
				int slfi = slotlistinpe.get(j).getslotfinishtime();

				if (dagtemp.getarrive() <= slst) {// predone<=slst
					if ((slst + dagtemp.getts()) <= slfi && // s1+c<f1
							(slst + dagtemp.getts()) <= dagtemp.getdeadline()) {
						startinpe[i] = slst;
						slotid[i] = slotlistinpe.get(j).getslotId();
						break;
					} else if ((slst + dagtemp.getts()) > slfi
							&& (slst + dagtemp.getts()) <= dagtemp
									.getdeadline()) {
						continue;

					}
				} else {// predone>slst
					if ((dagtemp.getarrive() + dagtemp.getts()) <= slfi // predone+c<f1
							&& (dagtemp.getarrive() + dagtemp.getts()) <= dagtemp
									.getdeadline()) {
						startinpe[i] = dagtemp.getarrive();
						slotid[i] = slotlistinpe.get(j).getslotId();
						break;
					} else if ((dagtemp.getarrive() + dagtemp.getts()) > slfi
							&& (dagtemp.getarrive() + dagtemp.getts()) <= dagtemp
									.getdeadline()) {
						continue;
					}
				}
			}
		}

		for (int i = 0; i < peNumber; i++) {
			if (startinpe[i] != -1) {
				findsuc = true;
				if (startinpe[i] < startmin) {
					startmin = startinpe[i];
					pemin = i;
				}
			}
		}

		if (findsuc) {
			finishmin = startmin + dagtemp.getts();
			dagtemp.setfillbackstarttime(startmin);
			dagtemp.setfillbackpeid(pemin);
			dagtemp.setfillbackready(true);

			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(pemin);

			if (TASKInPe.size() > 0) {

				ArrayList<Slot> slotlistinpe = new ArrayList<Slot>();
				for (int j = 0; j < SlotListInPes.get(pemin).size(); j++)
					slotlistinpe.add((Slot) SlotListInPes.get(pemin).get(j));

				ArrayList<String> below = new ArrayList<String>();

				Slot slottem = new Slot();

				for (int i = 0; i < slotlistinpe.size(); i++) {
					if (slotlistinpe.get(i).getslotId() == slotid[pemin]) {
						slottem = slotlistinpe.get(i);
						break;
					}
				}

				for (int i = 0; i < slottem.getbelow().size(); i++) {
					below.add(slottem.getbelow().get(i));
				}

				if (below.size() > 0) {
					String buf[] = below.get(0).split(" ");
					int inpe = Integer.valueOf(buf[2]).intValue();

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
				} else {
					Integer[] st_fi = new Integer[4];
					st_fi[0] = startmin;
					st_fi[1] = finishmin;
					st_fi[2] = dagtemp.getdagid();
					st_fi[3] = dagtemp.getid();
					TASKInPe.put(TASKInPe.size(), st_fi);
				}
			} else {
				Integer[] st_fi = new Integer[4];
				st_fi[0] = startmin;
				st_fi[1] = finishmin;
				st_fi[2] = dagtemp.getdagid();
				st_fi[3] = dagtemp.getid();
				TASKInPe.put(TASKInPe.size(), st_fi);
			}
			computeSlot(dagmap.getsubmittime(), dagmap.getDAGdeadline());
		}
		return findsuc;
	}

	/**
	 * 
	 * @Title: fillBack
	 * @Description: fillBack method
	 * @param dagmap
	 * @return:
	 * @throws
	 */
	public static boolean fillBack(DAG dagmap) {

		int runtime = dagmap.getsubmittime();
		boolean fillbacksuc = true;
		boolean fini = true;

		ArrayList<Task> readylist = new ArrayList<Task>();
		ArrayList<Task> DAGTaskList = new ArrayList<Task>();
		Map<String, Double> DAGTaskDependValue = new HashMap<String, Double>();
		DAGTaskDependValue = dagmap.getdependvalue();

		int DAGID = dagmap.getDAGId();

		for (int i = 0; i < dagmap.gettasklist().size(); i++) {
			DAGTaskList.add((Task) dagmap.gettasklist().get(i));
		}

		do {

			for (Task dag : DAGTaskList) {
				if ((dag.getfillbackstarttime() + dag.getts()) == runtime
						&& dag.getfillbackready()
						&& dag.getfillbackdone() == false) {
					dag.setfillbackfinishtime(runtime);
					dag.setfillbackdone(true);
				}
			}

			for (Task dag : DAGTaskList) {

				if (dag.getid() == 0 && dag.getfillbackready() == false) {
					if (findFirstTaskSlot(dagmap, DAGTaskList.get(0))) {
						DAGTaskList.get(0).setprefillbackready(true);
						DAGTaskList.get(0).setprefillbackdone(true);
						if (dag.getts() == 0) {
							dag.setfillbackfinishtime(dag
									.getfillbackstarttime());
							dag.setfillbackdone(true);
						}
					} else {
						fillbacksuc = false;
						return fillbacksuc;
					}
				}

				if (dag.getfillbackdone() == false
						&& dag.getfillbackready() == false) {
					ArrayList<Task> pre_queue = new ArrayList<Task>();
					ArrayList<Integer> pre = new ArrayList<Integer>();
					pre = dag.getpre();

					if (pre.size() > 0) {
						boolean ready = true;
						for (int j = 0; j < pre.size(); j++) {
							Task buf = new Task();
							buf = getDAGById(dag.getdagid(), pre.get(j));
							pre_queue.add(buf);
							if (!buf.getfillbackdone()) {
								ready = false;
								break;
							}
						}
						if (ready) {
							readylist.add(dag);
							dag.setprefillbackready(true);
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
			}
			fini = true;
			for (Task dag : DAGTaskList) {
				if (dag.getfillbackdone() == false) {
					fini = false;
					break;
				}
			}
			runtime = runtime + T;
		} while (runtime <= dagmap.getDAGdeadline() && !fini && fillbacksuc);
		if (fini) {
			for (Task dag : DAGTaskList) {
				dag.setfillbackfinishtime(dag.getfillbackstarttime()
						+ dag.getts());
			}
		} else {
			fillbacksuc = false;
		}
		return fillbacksuc;
	}

	/**
	 * 
	 * @Title: restoreSlotandTASK
	 * @Description: restore the experimental environment
	 * @param SlotListInPestemp
	 * @param TASKListInPestemp
	 *            :
	 * @throws
	 */
	public static void restoreSlotandTASK(
			HashMap<Integer, ArrayList> SlotListInPestemp,
			HashMap<Integer, HashMap> TASKListInPestemp) {

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
			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			for (int j = 0; j < TASKListInPestemp.get(k).size(); j++) {
				Integer[] temp = new Integer[4];
				temp = (Integer[]) TASKListInPestemp.get(k).get(j);
				TASKInPe.put(j, temp);
			}
			TASKListInPes.put(k, TASKInPe);
		}
	}

	/**
	 * 
	 * @Title: scheduleOtherDAG
	 * @Description: schedule other DAG
	 * @param i
	 * @param SlotListInPestemp
	 * @param TASKListInPestemp:
	 * @throws
	 */
	public static void scheduleOtherDAG(int i,
			HashMap<Integer, ArrayList> SlotListInPestemp,
			HashMap<Integer, HashMap> TASKListInPestemp) {

		int arrive = DAGMapList.get(i).getsubmittime();
		if (arrive > currentTime)
			currentTime = arrive;

		boolean fillbacksuc = fillBack(DAGMapList.get(i));

		if (!fillbacksuc) {

			restoreSlotandTASK(SlotListInPestemp, TASKListInPestemp);

			DAGMapList.get(i).setfillbackdone(false);
			DAGMapList.get(i).setfillbackpass(true);
			ArrayList<Task> DAGTaskList = new ArrayList<Task>();
			for (int j = 0; j < DAGMapList.get(i).gettasklist().size(); j++) {
				DAGTaskList.add((Task) DAGMapList.get(i).gettasklist().get(j));
				DAGTaskList.get(j).setfillbackpass(true);
			}
		} else {
			DAGMapList.get(i).setfillbackdone(true);
		}
	}

	/**
	 * 
	 * @Title: copySlot
	 * @Description: copy slot before schedule
	 * @return:
	 * @throws
	 */
	public static HashMap copySlot() {
		HashMap<Integer, ArrayList> SlotListInPestemp = new HashMap<Integer, ArrayList>();

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
	 * @Description: copy task before schedule
	 * @return:
	 * @throws
	 */
	public static HashMap copyTASK() {
		HashMap<Integer, HashMap> TASKListInPestemp = new HashMap<Integer, HashMap>();
		for (int k = 0; k < TASKListInPes.size(); k++) {
			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			for (int j = 0; j < TASKListInPes.get(k).size(); j++) {
				Integer[] temp = new Integer[4];
				temp = (Integer[]) TASKListInPes.get(k).get(j);
				TASKInPe.put(j, temp);
			}
			TASKListInPestemp.put(k, TASKInPe);
		}
		return TASKListInPestemp;
	}

	/**
	 * 
	 * @Title: firstDAGSchedule
	 * @Description: schedule first DAG use fifo algorithm
	 * @param dagmap:
	 * @throws
	 */
	public static void firstDAGSchedule(DAG dagmap) {

		int time = currentTime;

		ArrayList<Task> DAGTaskList = new ArrayList<Task>();
		Map<String, Double> DAGTaskDependValue = new HashMap<String, Double>();
		DAGTaskDependValue = dagmap.getdependvalue();

		for (int i = 0; i < dagmap.gettasklist().size(); i++) {
			DAGTaskList.add((Task) dagmap.gettasklist().get(i));
		}

		while (time <= timeWindow) {
			boolean fini = true;
			for (Task dag : DAGTaskList) {
				if (dag.getfillbackdone() == false
						&& dag.getfillbackpass() == false) {
					fini = false;
					break;
				}
			}

			if (fini) {
				break;
			}
			for (Task dag : DAGTaskList) {
				if ((dag.getfillbackstarttime() + dag.getts()) == time
						&& dag.getfillbackready()
						&& dag.getfillbackdone() == false) {
					dag.setfillbackfinishtime(time);
					dag.setfillbackdone(true);
					PEList.get(dag.getfillbackpeid()).setfree(true);
				}
			}

			for (Task dag : DAGTaskList) {
				if (dag.getarrive() <= time && dag.getfillbackdone() == false
						&& dag.getfillbackready() == false
						&& dag.getfillbackpass() == false) {
					boolean ifready = checkReady(dag, DAGTaskList,
							DAGTaskDependValue, time);
					if (ifready) {
						dag.setfillbackready(true);
						readyqueue.add(dag);
					}
				}
			}

			schedule(DAGTaskList, DAGTaskDependValue, time);

			for (Task dag : DAGTaskList) {
				if (dag.getfillbackstarttime() == time
						&& dag.getfillbackready()
						&& dag.getfillbackdone() == false) {

					if (dag.getdeadline() >= time) {
						if (dag.getts() == 0) {
							dag.setfillbackfinishtime(time);
							dag.setfillbackdone(true);
							time = time - T;
						} else {
							PEList.get(dag.getfillbackpeid()).setfree(false);
							PEList.get(dag.getfillbackpeid()).settask(
									dag.getid());
						}
					} else {
						dag.setfillbackpass(true);
					}
				}
			}
			time = time + T;
		}
	}

	/**
	 * 
	 * @Title: schedule
	 * @Description: schedule ready list
	 * @param DAGTaskList
	 * @param DAGTaskDependValue
	 * @param time:
	 * @throws
	 */
	private static void schedule(ArrayList<Task> DAGTaskList,
			Map<String, Double> DAGTaskDependValue, int time) {

		ArrayList<Task> buff = new ArrayList<Task>();
		Task min = new Task();
		Task temp = new Task();

		for (int k = 0; k < readyqueue.size(); k++) {
			int tag = k;
			min = readyqueue.get(k);
			temp = readyqueue.get(k);
			for (int p = k + 1; p < readyqueue.size(); p++) {
				if (readyqueue.get(p).getarrive() < min.getarrive()) {
					min = readyqueue.get(p);
					tag = p;
				}
			}
			if (tag != k) {
				readyqueue.set(k, min);
				readyqueue.set(tag, temp);
			}
		}

		for (int i = 0; i < readyqueue.size(); i++) {
			Task buf1 = new Task();
			buf1 = readyqueue.get(i);

			for (Task dag : DAGTaskList) {
				if (buf1.getid() == dag.getid()) {
					choosePE(dag, DAGTaskDependValue, time);
					break;
				}
			}
		}
		readyqueue.clear();
	}

	/**
	 * 
	 * @Title: checkReady
	 * @Description: check ready list
	 * @param dag
	 * @param DAGTaskList
	 * @param DAGTaskDependValue
	 * @param time
	 * @return:
	 * @throws
	 */
	private static boolean checkReady(Task dag, ArrayList<Task> DAGTaskList,
			Map<String, Double> DAGTaskDependValue, int time) {

		boolean isready = true;

		if (dag.getfillbackpass() == false && dag.getfillbackdone() == false) {
			if (time > dag.getdeadline()) {
				dag.setfillbackpass(true);
			}
			if (dag.getfillbackstarttime() == 0
					&& dag.getfillbackpass() == false) {
				ArrayList<Task> pre_queue = new ArrayList<Task>();
				ArrayList<Integer> pre = new ArrayList<Integer>();
				pre = dag.getpre();
				if (pre.size() >= 0) {
					for (int j = 0; j < pre.size(); j++) {
						Task buf3 = new Task();
						buf3 = getDAGById(dag.getdagid(), pre.get(j));
						pre_queue.add(buf3);

						if (buf3.getfillbackpass()) {
							dag.setfillbackpass(true);
							isready = false;
							break;
						}

						if (!buf3.getfillbackdone()) {
							isready = false;
							break;
						}

					}
				}
			}
		}

		return isready;
	}

	/**
	 * 
	 * @Title: choosePE
	 * @Description: choose appropriate processor for every task
	 * @param dag_temp
	 * @param DAGTaskDependValue
	 * @param time
	 *            :
	 * @throws
	 */
	private static void choosePE(Task dag_temp,
			Map<String, Double> DAGTaskDependValue, int time) {

		ArrayList<Task> pre_queue = new ArrayList<Task>();
		ArrayList<Integer> pre = new ArrayList<Integer>();
		pre = dag_temp.getpre();
		if (pre.size() >= 0) {
			for (int j = 0; j < pre.size(); j++) {
				Task buf = new Task();
				buf = getDAGById(dag_temp.getdagid(), pre.get(j));
				pre_queue.add(buf);
			}
		}

		int temp[] = new int[PEList.size()];

		for (int i = 0; i < PEList.size(); i++) {
			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(i);

			if (pre_queue.size() == 0) {
				if (TASKInPe.size() == 0) {
					temp[i] = time;
				} else {
					if (time > TASKInPe.get(TASKInPe.size() - 1)[1])
						temp[i] = time;
					else
						temp[i] = TASKInPe.get(TASKInPe.size() - 1)[1];
				}
			} else if (pre_queue.size() == 1) {
				if (pre_queue.get(0).getfillbackpeid() == PEList.get(i).getID()) {
					if (TASKInPe.size() == 0) {
						temp[i] = time;
					} else {
						if (time > TASKInPe.get(TASKInPe.size() - 1)[1])
							temp[i] = time;
						else
							temp[i] = TASKInPe.get(TASKInPe.size() - 1)[1];
					}
				} else {
					int value = (int) (double) DAGTaskDependValue.get(String
							.valueOf(pre_queue.get(0).getid())
							+ " "
							+ String.valueOf(dag_temp.getid()));
					if (TASKInPe.size() == 0) {
						if ((pre_queue.get(0).getfillbackfinishtime() + value) < time)
							temp[i] = time;
						else
							temp[i] = pre_queue.get(0).getfillbackfinishtime()
									+ value;
					} else {
						if ((pre_queue.get(0).getfillbackfinishtime() + value) > TASKInPe
								.get(TASKInPe.size() - 1)[1]
								&& (pre_queue.get(0).getfillbackfinishtime() + value) > time)
							temp[i] = pre_queue.get(0).getfillbackfinishtime()
									+ value;
						else if (time > (pre_queue.get(0)
								.getfillbackfinishtime() + value)
								&& time > TASKInPe.get(TASKInPe.size() - 1)[1])
							temp[i] = time;
						else
							temp[i] = TASKInPe.get(TASKInPe.size() - 1)[1];
					}
				}
			} else {
				int max = time;

				for (int j = 0; j < pre_queue.size(); j++) {
					if (pre_queue.get(j).getfillbackpeid() == PEList.get(i)
							.getID()) {
						if (TASKInPe.size() != 0) {
							if (max < TASKInPe.get(TASKInPe.size() - 1)[1])
								max = TASKInPe.get(TASKInPe.size() - 1)[1];
						}
					} else {
						int valu = (int) (double) DAGTaskDependValue.get(String
								.valueOf(pre_queue.get(j).getid())
								+ " "
								+ String.valueOf(dag_temp.getid()));
						int value = pre_queue.get(j).getfillbackfinishtime()
								+ valu;

						if (TASKInPe.size() == 0) {
							if (max < value)
								max = value;
						} else {
							if (value <= TASKInPe.get(TASKInPe.size() - 1)[1]) {
								if (max < TASKInPe.get(TASKInPe.size() - 1)[1])
									max = TASKInPe.get(TASKInPe.size() - 1)[1];
							} else {
								if (max < value)
									max = value;
							}
						}
					}
				}
				temp[i] = max;
			}
		}

		int min = timewindowmax;
		int minpeid = -1;
		for (int i = 0; i < PEList.size(); i++) {
			if (min > temp[i]) {
				min = temp[i];
				minpeid = i;
			}
		}
		if (min <= dag_temp.getdeadline()) {

			HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
			TASKInPe = TASKListInPes.get(minpeid);

			dag_temp.setfillbackpeid(minpeid);
			dag_temp.setts(dag_temp.getlength());
			dag_temp.setfillbackstarttime(min);
			dag_temp.setfinish_suppose(dag_temp.getfillbackstarttime()
					+ dag_temp.getts());
			Integer[] st_fi = new Integer[4];
			st_fi[0] = dag_temp.getfillbackstarttime();
			st_fi[1] = dag_temp.getfillbackstarttime() + dag_temp.getts();
			st_fi[2] = dag_temp.getdagid();
			st_fi[3] = dag_temp.getid();
			TASKInPe.put(TASKInPe.size(), st_fi);
		} else {
			dag_temp.setfillbackpass(true);
		}
	}

	/**
	 * 
	 * @Title: scheduleFirstDAG
	 * @Description: schedule first DAG of this DAG flow:
	 * @throws
	 */
	public static void scheduleFirstDAG() {

		firstDAGSchedule(DAGMapList.get(0));

		Task tem = (Task) DAGMapList.get(0).gettasklist()
				.get(DAGMapList.get(0).gettasknumber() - 1);

		if (tem.getfillbackdone()) {
			DAGMapList.get(0).setfillbackdone(true);
			DAGMapList.get(0).setfillbackpass(false);
		}
		changeTaskListInPE(DAGMapList.get(0));
	}
	
	/**
	 * 
	 * @Title: initDAG_createDAGdepend_XML
	 * @Description: initial DAG ,add DAG dependence
	 * @param i
	 * @param preexist
	 * @param tasknumber
	 * @param arrivetimes
	 * @param pathXML
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 * @throws
	 */
	@SuppressWarnings("rawtypes")
	private static int initDAG_createDAGdepend_XML(int i, int preexist,
			int tasknumber, int arrivetimes, String pathXML)
			throws NumberFormatException, IOException, JDOMException {

		int back = 0;
		DAGDependMap_personal = new HashMap<Integer, Integer>();
		DAGDependValueMap_personal = new HashMap<String, Double>();
		ComputeCostMap = new HashMap<Integer, int[]>();
		AveComputeCostMap = new HashMap<Integer, Integer>();

		SAXBuilder builder = new SAXBuilder();

		Document doc = builder.build(pathXML + "/dag" + (i + 1) + ".xml");

		Element adag = doc.getRootElement();

		for (int j = 0; j < tasknumber; j++) {
			Task dag = new Task();
			Task dag_persional = new Task();

			dag.setid(Integer.valueOf(preexist + j).intValue());
			dag.setarrive(arrivetimes);
			dag.setdagid(i);
			dag_persional.setid(Integer.valueOf(j).intValue());
			dag_persional.setarrive(arrivetimes);
			dag_persional.setdagid(i);

			XPath path = XPath
					.newInstance("//job[@id='" + j + "']/@tasklength");
			List list = path.selectNodes(doc);
			Attribute attribute = (Attribute) list.get(0);
			int x = Integer.valueOf(attribute.getValue()).intValue();
			dag.setlength(x);
			dag.setts(x);
			dag_persional.setlength(x);
			dag_persional.setts(x);

			if (j == tasknumber - 1) {
				dag.setislast(true);
				islastnum++;
			}

			DAG_queue.add(dag);
			DAG_queue_personal.add(dag_persional);

			int sum = 0;
			int[] bufferedDouble = new int[PEList.size()];
			for (int k = 0; k < PEList.size(); k++) {
				bufferedDouble[k] = Integer.valueOf(x
						/ PEList.get(k).getability());
				sum = sum + Integer.valueOf(x / PEList.get(k).getability());
			}
			ComputeCostMap.put(j, bufferedDouble);
			AveComputeCostMap.put(j, (sum / PEList.size()));
		}

		XPath path1 = XPath.newInstance("//uses[@link='output']/@file");
		List list1 = path1.selectNodes(doc);
		for (int k = 0; k < list1.size(); k++) {
			Attribute attribute1 = (Attribute) list1.get(k);
			String[] pre_suc = attribute1.getValue().split("_");

			int[] presuc = new int[2];
			presuc[0] = Integer.valueOf(pre_suc[0]).intValue() + preexist;
			presuc[1] = Integer.valueOf(pre_suc[1]).intValue() + preexist;

			XPath path2 = XPath.newInstance("//uses[@file='"
					+ attribute1.getValue() + "']/@size");
			List list2 = path2.selectNodes(doc);
			Attribute attribute2 = (Attribute) list2.get(0);
			int datasize = Integer.valueOf(attribute2.getValue()).intValue();

			DAGDependMap.put(presuc[0], presuc[1]);
			DAGDependValueMap.put((presuc[0] + " " + presuc[1]),
					(double) datasize);
			DAG_queue.get(presuc[0]).addToSuc(presuc[1]);
			DAG_queue.get(presuc[1]).addToPre(presuc[0]);

			DAGDependMap_personal.put(Integer.valueOf(pre_suc[0]).intValue(),
					Integer.valueOf(pre_suc[1]).intValue());
			DAGDependValueMap_personal.put((pre_suc[0] + " " + pre_suc[1]),
					(double) datasize);

			int tem0 = Integer.parseInt(pre_suc[0]);
			int tem1 = Integer.parseInt(pre_suc[1]);
			DAG_queue_personal.get(tem0).addToSuc(tem1);
			DAG_queue_personal.get(tem1).addToPre(tem0);
		}

		back = preexist + tasknumber;
		return back;
	}

	/**
	 * 
	 * @Title: createDeadline_XML
	 * @Description: calculate deadline for every task
	 * @param dead_line
	 * @param dagdepend_persion
	 * @throws Throwable:
	 * @throws
	 */
	private static void createDeadline_XML(int dead_line,
			DAGDepend dagdepend_persion) throws Throwable {
		int maxability = 1;
		int max = 10000;

		for (int k = DAG_queue_personal.size() - 1; k >= 0; k--) {

			ArrayList<Task> suc_queue = new ArrayList<Task>();
			ArrayList<Integer> suc = new ArrayList<Integer>();
			suc = DAG_queue_personal.get(k).getsuc();

			if (suc.size() > 0) {
				for (int j = 0; j < suc.size(); j++) {
					int tem = 0;
					Task buf3 = new Task();
					buf3 = getDAGById_task(suc.get(j));
					suc_queue.add(buf3);
					tem = (int) (buf3.getdeadline() - (buf3.getlength() / maxability));
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
	 * @Title: initDagMap
	 * @Description: initial DAG dependence
	 * @param dagdepend
	 * @param vcc
	 * @param pathXML
	 * @throws Throwable :
	 * @throws
	 */
	public static void initDagMap(DAGDepend dagdepend, PEComputerability vcc,
			String pathXML) throws Throwable {
		int pre_exist = 0;

		File file = new File(pathXML);
		String[] fileNames = file.list();
		int num = fileNames.length - 1;

		BufferedReader bd = new BufferedReader(new FileReader(pathXML
				+ "Deadline.txt"));
		String buffered;
		for (int i = 0; i < num; i++) {

			DAG dagmap = new DAG();
			DAGDepend dagdepend_persional = new DAGDepend();
			DAG_queue_personal.clear();

			buffered = bd.readLine();
			String bufferedA[] = buffered.split(" ");
			int buff[] = new int[4];

			buff[0] = Integer.valueOf(bufferedA[0].split("dag")[1]).intValue();// dagID
			buff[1] = Integer.valueOf(bufferedA[1]).intValue();// tasknum
			buff[2] = Integer.valueOf(bufferedA[2]).intValue();// arrivetime
			buff[3] = Integer.valueOf(bufferedA[3]).intValue();// deadline
			int deadline = buff[3];
			int tasknum = buff[1];
			taskTotal = taskTotal + tasknum;
			int arrivetime = buff[2];

			pre_exist = initDAG_createDAGdepend_XML(i, pre_exist, tasknum,
					arrivetime, pathXML);

			vcc.setComputeCostMap(ComputeCostMap);
			vcc.setAveComputeCostMap(AveComputeCostMap);

			dagdepend_persional.setDAGList(DAG_queue_personal);
			dagdepend_persional.setDAGDependMap(DAGDependMap_personal);
			dagdepend_persional
					.setDAGDependValueMap(DAGDependValueMap_personal);

			createDeadline_XML(deadline, dagdepend_persional);

			int number_1 = DAG_queue.size();
			int number_2 = DAG_queue_personal.size();
			for (int k = 0; k < number_2; k++) {
				DAG_queue.get(number_1 - number_2 + k).setdeadline(
						DAG_queue_personal.get(k).getdeadline());
			}

			dagmap.settasknumber(tasknum);
			dagmap.setDAGId(i);
			dagmap.setDAGdeadline(deadline);
			dagmap.setsubmittime(arrivetime);
			dagmap.settasklist(DAG_queue_personal);
			dagmap.setdepandmap(DAGDependMap_personal);
			dagmap.setdependvalue(DAGDependValueMap_personal);
			DAGMapList.add(dagmap);
		}
		dagdepend.setdagmaplist(DAGMapList);
		dagdepend.setDAGList(DAG_queue);
		dagdepend.setDAGDependMap(DAGDependMap);
		dagdepend.setDAGDependValueMap(DAGDependValueMap);

	}

	/**
	 * 
	 * @Title: initPE
	 * @Description: initial processor
	 * @throws Throwable:
	 * @throws
	 */
	private static void initPE() throws Throwable {

		for (int i = 0; i < peNumber; i++) {
			PE pe = new PE();
			pe.setID(i);
			pe.setability(1);
			pe.setfree(true);
			pe.setAvail(0);
			PEList.add(pe);
		}
	}

	/**
	 * 
	 * @Title: getDAGById
	 * @Description: get dag according to DAG id and task id
	 * @param DAGId
	 * @param dagId
	 * @return:
	 * @throws
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
	 * 
	 * @Title: getDAGById_task
	 * @Description: get dag according to TASK id
	 * @param dagId
	 * @return:
	 * @throws
	 */
	private static Task getDAGById_task(int dagId) {
		for (Task dag : DAG_queue_personal) {
			if (dag.getid() == dagId)
				return dag;
		}
		return null;
	}

}
