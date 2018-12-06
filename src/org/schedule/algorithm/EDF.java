package org.schedule.algorithm;

import org.generate.util.CommonParametersUtil;
import org.schedule.model.DAGDepend;
import org.schedule.model.PE;
import org.schedule.model.Task;

import java.util.ArrayList;

/**
 * 
 * @ClassName: EDF
 * @Description: Earliest Deadline First
 * @author YWJ
 * @date 2017-9-10 ÏÂÎç10:46:13
 */
public class EDF {

	public static int tasknum;
	
	int dagnummax = 10000;
	int mesnum = 5;

	public static int[][] message;

	public static ArrayList<Task> dag_queue;

	public static ArrayList<Task> dag_queue_ori;

	public static ArrayList<Task> readyqueue;
	// Total number of tasks
	public static int courseNumber;
//current time
	public static int currentTime;

	public static int T = 1;

	public static ArrayList<PE> peList;
//task dependence
	public static DAGDepend dagdepend;

	public static int[][] petimeList;

	public static int[] petimes;
	//the number of prosessor
	public static int peNumber;
//the end time of every processor
	public static int proceesorEndTime = CommonParametersUtil.timeWindow;

	public static int timeWindow;

	public EDF(int PEnumber) {
		dagdepend = new DAGDepend();
		dag_queue = new ArrayList<Task>();
		dag_queue_ori = new ArrayList<Task>();
		readyqueue = new ArrayList<Task>();// ready list
		courseNumber = 0;
		currentTime = 0;
		petimeList = new int[PEnumber][2000];
		petimes = new int[PEnumber];
		timeWindow = proceesorEndTime / PEnumber;
		message = new int[dagnummax][mesnum];
	}

	/**
	 * 
	* @Title: checkReady
	* @Description: Determine whether a task is ready
	* @param dag
	* @param queue1
	* @param dagdepend
	* @param current
	* @return:
	* @throws
	 */
	private static boolean checkReady(Task dag, ArrayList<Task> queue1,
			DAGDepend dagdepend, int current) {

		boolean isready = true;

		if (dag.getpass() == false && dag.getdone() == false) {
			if (current >= dag.getdeadline()) {
				dag.setpass(true);
				// System.out.println(currentTime+" "+queue1.get(i).Deadline);
			}
			if (dag.getstart() == 0 && dag.getpass() == false) {
				ArrayList<Task> pre_queue = new ArrayList<Task>();
				ArrayList<Integer> pre = new ArrayList<Integer>();
				pre = dag.getpre();
				if (pre.size() >= 0) {
					for (int j = 0; j < pre.size(); j++) {
						Task buf3 = new Task();
						buf3 = getDAGById(pre.get(j));
						pre_queue.add(buf3);

						if (buf3.getpass()) {
							dag.setpass(true);
							break;
						}
						if (!buf3.done) {
							isready = false;
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
	* @Title: makespan
	* @Description: calculate schedul time
	* @param PEnumber
	* @return
	* @throws Throwable:
	* @throws
	 */
	public static int[] makespan(int PEnumber) throws Throwable {

		tasknum = dag_queue_ori.size();

		peNumber = PEnumber;
		for (Task dag_ : dag_queue_ori) {
			Task dag_new = new Task();
			dag_new.setarrive(dag_.getarrive());
			dag_new.setdeadline(dag_.getdeadline());
			dag_new.setid(dag_.getid());
			dag_new.setlength(dag_.getlength());
			dag_new.setpre(dag_.getpre());
			dag_new.setsuc(dag_.getsuc());
			dag_new.setislast(dag_.getislast());
			dag_new.setdagid(dag_.getdagid());
			dag_queue.add(dag_new);
		}

		sort(dag_queue, courseNumber);

		for (int i = 0; i < peList.size(); i++) {
			if (petimes[i] == 0)
				petimeList[i][0] = 0;
		}

		while (currentTime <= timeWindow) {
			for (Task dag : dag_queue) {
				if ((dag.getstart() + dag.getts()) == currentTime
						&& dag.getready() && dag.getdone() == false
						&& dag.getpass() == false) {
					dag.setfinish(currentTime); 
					dag.setdone(true); 
					peList.get(dag.getpeid()).setfree(true);
				}
			}

			for (Task dag : dag_queue) {
				if (dag.getarrive() <= currentTime && dag.getdone() == false
						&& dag.getready() == false && dag.getpass() == false) {
					boolean ifready = checkReady(dag, dag_queue, dagdepend,
							currentTime);
					if (ifready) {
						dag.setready(true);
						readyqueue.add(dag);
					}
				}

			}

			schedule(dag_queue, dagdepend, currentTime);

			for (Task dag : dag_queue) {
				if (dag.getstart() == currentTime && dag.getready()
						&& dag.getdone() == false && dag.getpass() == false) {
					if (dag.getdeadline() > currentTime) {
						if (dag.getts() == 0) {
							dag.setfinish(currentTime);
							dag.setdone(true);
							currentTime = currentTime - T;
						} else {
							peList.get(dag.getpeid()).setfree(false);
							peList.get(dag.getpeid()).settask(dag.getid());
						}
					} else {
						dag.setpass(true);
					}
				}

			}

			currentTime = currentTime + T;
		}

		int temp[] = new int[peNumber + 3];
		temp = storeResult();

		return temp;

	}

	/**
	 * 
	* @Title: storeResult
	* @Description: store schedule result
	* @return:
	* @throws
	 */
	public static int[] storeResult() {
		int temp[] = new int[peNumber + 3];
		int tempp = 0;
		temp[0] = currentTime - T; 
		temp[peNumber + 2] = 0;

		for (Task dag_temp : dag_queue) {
			for (int q = 1; q < 1 + peNumber; q++) {
				if (dag_temp.getpeid() == (q - 1) && dag_temp.getdone()) {
					temp[q] = temp[q] + dag_temp.getts();
					break;
				}
			}
		}

		for (Task dag : dag_queue) {
			if (dag.islast == true) {
				if (dag.done == true) {
					temp[peNumber + 1]++; 
					for (Task dag_temp : dag_queue) {
						if (dag_temp.getdagid() == dag.getdagid())
							temp[peNumber + 2] = temp[peNumber + 2]
									+ dag_temp.getts();
					}
				}
			}
		}

		int dagcount = 0;
		for (Task dag : dag_queue) {
			if (dag.done) {
				message[dagcount][0] = dag.getdagid();
				message[dagcount][1] = dag.getid();
				message[dagcount][2] = dag.getpeid();
				message[dagcount][3] = dag.getstart();
				message[dagcount][4] = dag.getfinish();
				dagcount++;
			}
		}
		return temp;
	}

	/**
	 * 
	* @Title: schedule
	* @Description: start schedule
	* @param queue1
	* @param dagdepend
	* @param current:
	* @throws
	 */
	private static void schedule(ArrayList<Task> queue1, DAGDepend dagdepend,
			int current) {

		ArrayList<Task> buff = new ArrayList<Task>();
		Task min = new Task();
		Task temp = new Task();

		for (int k = 0; k < readyqueue.size(); k++) {
			int tag = k;
			min = readyqueue.get(k);
			temp = readyqueue.get(k);
			for (int p = k + 1; p < readyqueue.size(); p++) {
				if (readyqueue.get(p).getdeadline() < min.getdeadline()) {
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

			for (Task dag : dag_queue) {
				if (buf1.getid() == dag.getid()) {
					choosePE(dag);
					break;
				}
			}
		}
		readyqueue.clear();

	}

	/**
	 * 
	* @Title: choosePE
	* @Description: choose processor for task
	* @param dag_temp:
	* @throws
	 */
	private static void choosePE(Task dag_temp) {

		ArrayList<Task> pre_queue = new ArrayList<Task>();
		ArrayList<Integer> pre = new ArrayList<Integer>();
		pre = dag_temp.getpre();

		if (pre.size() >= 0) {
			for (int j = 0; j < pre.size(); j++) {
				Task buf = new Task();
				buf = getDAGById(pre.get(j));
				pre_queue.add(buf);
			}
		}

		int temp[] = new int[peList.size()];
		for (int i = 0; i < peList.size(); i++) {
			if (pre_queue.size() == 0) {
				if (currentTime > petimeList[i][petimes[i]])
					temp[i] = currentTime;
				else
					temp[i] = petimeList[i][petimes[i]];
			} else if (pre_queue.size() == 1) {
				if (pre_queue.get(0).getpeid() == peList.get(i).getID()) {
					if (currentTime > petimeList[i][petimes[i]])
						temp[i] = currentTime;
					else
						temp[i] = petimeList[i][petimes[i]];
				} else {
					int value = (int) dagdepend.getDependValue(pre_queue.get(0)
							.getid(), dag_temp.getid());
					if ((pre_queue.get(0).getfinish() + value) > petimeList[i][petimes[i]]
							&& (pre_queue.get(0).getfinish() + value) > currentTime)
						temp[i] = pre_queue.get(0).getfinish() + value;
					else if (currentTime > (pre_queue.get(0).getfinish() + value)
							&& currentTime > petimeList[i][petimes[i]])
						temp[i] = currentTime;
					else
						temp[i] = petimeList[i][petimes[i]];
				}
			} else {
				int max = currentTime;
				for (int j = 0; j < pre_queue.size(); j++) {
					if (pre_queue.get(j).getpeid() == peList.get(i).getID()) {
						if (max < petimeList[i][petimes[i]])
							max = petimeList[i][petimes[i]];
					} else {
						int value = pre_queue.get(j).getfinish()
								+ (int) dagdepend.getDependValue(
										pre_queue.get(j).getid(),
										dag_temp.getid());
						if (value <= petimeList[i][petimes[i]]) {
							if (max < petimeList[i][petimes[i]])
								max = petimeList[i][petimes[i]];
						} else {
							if (max < value)
								max = value;
						}
					}
				}
				temp[i] = max;
			}
		}

		int min = 300000;
		int minpeid = 0;

		for (int i = 0; i < peList.size(); i++) {
			if (min > temp[i]) {
				min = temp[i];
				minpeid = i;
			}
		}

		if (min < dag_temp.getdeadline()) {
			dag_temp.setpeid(minpeid);
			dag_temp.setts(dag_temp.getlength()
					/ peList.get(minpeid).getability());
			dag_temp.setstart(min);
			dag_temp.setfinish_suppose(dag_temp.getstart() + dag_temp.getts());

			petimes[minpeid]++;
			petimeList[minpeid][petimes[minpeid]] = dag_temp
					.getfinish_suppose();
		} else {
			dag_temp.setpass(true);
		}
	}

	/**
	 * 
	 * @Title: sort
	 * @Description:Order all DAG on the basis of its arrival time
	 * @return void
	 * @throws
	 */
	private static void sort(ArrayList<Task> ready_queue, int course_num)throws Throwable {
		ArrayList<Task> buff = new ArrayList<Task>();
		Task min = new Task();
		Task temp = new Task();
		for (int i = 0; i < course_num; i++) {
			int tag = i;
			min = ready_queue.get(i);
			temp = ready_queue.get(i);

			for (int j = i + 1; j < course_num; j++) {
				if (ready_queue.get(j).getarrive() < min.getarrive()) {
					min = ready_queue.get(j);
					tag = j;
				}
			}

			if (tag != i) {
				ready_queue.set(i, min);
				ready_queue.set(tag, temp);
			}
		}
	}

	/**
	 * 
	* @Title: getDAGById
	* @Description: get dag according to id
	* @param dagId
	* @return:
	* @throws
	 */
	private static Task getDAGById(int dagId) {
		for (Task dag : dag_queue) {
			if (dag.getid() == dagId)
				return dag;
		}
		return null;
	}
}
