package org.schedule.algorithm;

import org.generate.util.CommonParametersUtil;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.schedule.model.*;
import org.schedule.utils.ReadyQueueUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class WorkflowBased {

    // 统计数据存储
    public static String[][] rateResult = new String[1][4];
    // 比例统计数据存储
    public static String[][] rate = new String[5][2];
    // 当前时刻
    public static int currentTime;
    //处理器的截止时间
    public static int proceesorEndTime = CommonParametersUtil.timeWindow;
    public static int timeWindow;
    //时间步长
    public static int T = 1;
    public static int fillBackTaskNum = 10000000;
    public static int[][] message;
    // 最大任务数
    public static int dagNumMax = 10000;
    // 最大时间窗时间
    public static int timewindowmax = 9000000;
    public static int mesnum = 5;
    //处理器列表
    private static ArrayList<PE> PEList;
    //作业列表
    private static ArrayList<DAG> DAGMapList;
    //任务队列
    private static ArrayList<Task> Task_queue;
    // 就绪队列
    private static ArrayList<Task> readyTaskQueue;
    // dependence between tasks
    private static HashMap<Integer, Integer> DAGDependMap;
    // transfer data between task
    private static HashMap<String, Double> DAGDependValueMap;
    private static ArrayList<Task> TASK_queue_personal;
    private static HashMap<Integer, Integer> DAGDependMap_personal;
    private static HashMap<String, Double> DAGDependValueMap_personal;
    private static Map<Integer, int[]> ComputeCostMap;
    private static Map<Integer, Integer> AveComputeCostMap;
    public static int finishTaskNum=0;

    // 处理器个数
    private static int peNumber = 8;
    //所有处理器上空隙列表
    private static HashMap<Integer, ArrayList> SlotListInPes;

    //所有处理器上任务数
    private static HashMap<Integer, HashMap> TASKListInPes;

    private static int[] pushFlag;

    private static int taskTotal = 0;

    private static int[][] dagResultMap = null;


    //初始化
    public WorkflowBased() {
        readyTaskQueue = new ArrayList<Task>();
        Task_queue = new ArrayList<Task>();
        TASK_queue_personal = new ArrayList<Task>();
        PEList = new ArrayList<PE>();
        DAGMapList = new ArrayList<DAG>();
        DAGDependMap = new HashMap<Integer, Integer>();
        DAGDependValueMap = new HashMap<String, Double>();

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


//    /**
//     * @param slotlistinpe
//     * @param inpe:
//     * @throws
//     * @Title: changeInPE
//     * @Description: 改变在插入任务后，改变每个slot的below，其实用不到，应为总是会重新计算slot的，所以就会更新这个below值
//     */
//    public static void changeBelowInPE(ArrayList<Slot> slotlistinpe, int inpe) {
//        ArrayList<String> below = new ArrayList<String>();
//
//        for (int i = 0; i < slotlistinpe.size(); i++) {
//            ArrayList<String> belowte = new ArrayList<String>();
//
//            Slot slottem = slotlistinpe.get(i);
//
//            for (int j = 0; j < slottem.getbelow().size(); j++) {
//                below.add(slottem.getbelow().get(j));
//            }
//
//            String belowbuf[] = below.get(0).split(" ");
//            int buffer = Integer.valueOf(belowbuf[2]).intValue();
//            if (buffer >= inpe) {
//                buffer += 1;
//                for (int j = 0; j < below.size(); j++) {
//                    String belowbuff = belowbuf[0] + " " + belowbuf[1] + " " + buffer;
//                    belowte.add(belowbuff);
//                }
//                slottem.getbelow().clear();
//                slottem.setbelow(belowte);
//            }
//        }
//    }

    /**
     * @param dagmap:
     * @throws
     * @Title: changeTaskListInPE
     * @Description: modified result
     */
    private static void changeTaskListInPE(DAG dagmap) {
        for (int i = 0; i < peNumber; i++) {
            HashMap<Integer, Integer[]> TASKInPe =  TASKListInPes.get(i);
            for (int j = 0; j < TASKInPe.size(); j++) {
                if (TASKInPe.get(j)[2] == dagmap.getDAGId()) {
                    Task task = getTaskByDagIdAndTaskId(TASKInPe.get(j)[2], TASKInPe.get(j)[3]);
                    TASKInPe.get(j)[0] = task.getfillbackstarttime();
                    TASKInPe.get(j)[1] = task.getfillbackfinishtime();
                }
            }
            TASKListInPes.put(i, TASKInPe);
        }
    }

    /**
     * @param dagmap
     * @param readylist
     * @throws
     * @Title: scheduling
     * @Description: 开始对其它作业进行调度，这里是对整个就绪队列为对象进行调度
     * @return:
     */
    public static boolean scheduleReadyTasks(DAG dagmap, ArrayList<Task> readylist) throws Exception {
        boolean findsuc = true;

        while (readylist.size() > 0) {
            //最早结束时间
            int earliestFinish_Time = timewindowmax;
            int earliestFinish_readyListIndex = -1;
            int[] finish = new int[readylist.size()];

            int message[][] = new int[readylist.size()][6];
            // 0 is if success 1 means success 0 means fail,
            // 1 is earliest starttime
            // 2 is peid
            // 3 is slotid
            // 4 is if need slide
            // 5 is slide length


            /**
             * 1、这个findSlot里面应该是超过deadline才会被标记为失败，
             * 2、并且是任务计算结束时间超过deadline也会标记失败
             *
             *
             * 【问题】：
             * 1、这个里面这个deadline很关键啊！！！+++++++++++++++++++++++++++++++++++
             */

            for (int i = 0; i < readylist.size(); i++) {
                Task task = new Task();
                task = readylist.get(i);
                //对每一任务找寻合适空隙
                message[i] = findSlot(dagmap, task);
                //每个任务的计算结束时间
                finish[i] = message[i][1] + task.getts();
            }

            /**
             * 现在就绪队列中每一个任务都有经历过了一次空隙找寻，有对应的空隙结果
             * 【下面就是要找一个任务真实的调度在处理器上，然后就绪队列中其它的任务再一次进入调度】
             *
             * 【问题】：
             * 1、不应该是就算在这一秒这个任务没能调度在上面，然后时间没有超时的，还是可以参与下一秒的调度？？？？？？
             *
             * 2、这个deadline是没有考虑任务传输的呀？是没有，因为你并不知道这个任务最后会被分发到哪个处理器上执行，所以这个deadline肯定是一超过任务必定失败的。
             */

            for (int i = 0; i < readylist.size(); i++) {
                //如果有任务调度失败
                if (message[i][0] == 0) {
                    findsuc = false;
                }
            }
            //如果里面存在任务调度失败，则返回失败，整个作业失败
            if (findsuc == false) {
                return findsuc;
            }

            /**
             * 【如果要修改就绪队列里面任务的调度顺序，修改这里】，比如:
             * 1、最早开始执行的任务优先调度
             * 2、优先级高的先调度
             * 3、最早结束的优先调度
             *
             *
             * 这个都是在这里修改
             */

            //此处：【所有任务找到适宜的调度位置】,找寻结束时间最早的任务
            for (int i = 0; i < readylist.size(); i++) {
                if (earliestFinish_Time > finish[i]) {
                    earliestFinish_Time = finish[i];
                    earliestFinish_readyListIndex = i;
                }
            }


            Task targetTask_originTask = new Task();
            //原任务对象的id
            int originTaskId=readylist.get(earliestFinish_readyListIndex).getid();

            //readylist中的不是原任务对象，这里要获取原任务对象，这样的修改才是正确的
            for (int i = 0; i < dagmap.gettasklist().size(); i++) {
                Task tempTask = new Task();
                tempTask = (Task) dagmap.gettasklist().get(i);
                if (tempTask.getid() == originTaskId)
                    targetTask_originTask = (Task) dagmap.gettasklist().get(i);
            }




            //选中任务的开始时间
            int earliestFinish_StartTime = earliestFinish_Time - targetTask_originTask.getts();
            //选中任务所分配的处理器编号
            int earliestFinish_PEId = message[earliestFinish_readyListIndex][2];
            //选中任务的所分配的空隙编号（【这个空隙编号不是包含处理器所有原生空隙的编号，而是在任务submit---deadline区间内经过转换后适宜的空隙的编号，从0开始】）
            //但是在全局SlotListInPes中的本身就是适宜时间范围内的slot，所以除了Slot对象的blow字段还能得到slot的原生编号外，其它的都已经是转换后的了。
            int earliestFinish_slotId = message[earliestFinish_readyListIndex][3];

            targetTask_originTask.setfillbackstarttime(earliestFinish_StartTime);
            targetTask_originTask.setfillbackpeid(earliestFinish_PEId);
            //设置自己的就绪标记，就是自己父任务都调度好了，然后自己也找到了可以放入自己的位置，已经满足调度的需求了
            targetTask_originTask.setfillbackready(true);
            //设置父任务的状态标记
            targetTask_originTask.setprefillbackdone(true);
            targetTask_originTask.setprefillbackpass(false);

            //获取被调度上的处理器上原有的任务列表
            HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
            TASKInPe = TASKListInPes.get(earliestFinish_PEId);

            // 0 is if success 1 means success 0 means fail,
            // 1 is earliest starttime
            // 2 is peid
            // 3 is slotid
            // 4 is if need slide
            // 5 is slide length


            if (TASKInPe.size() > 0) {

                ArrayList<Slot> slotlistinpe = SlotListInPes.get(earliestFinish_PEId);

                //找到目标空隙
                Slot targetSlot = new Slot();
                for (int i = 0; i < slotlistinpe.size(); i++) {
                    if (slotlistinpe.get(i).getslotId() == earliestFinish_slotId) {
                        targetSlot = slotlistinpe.get(i);
                        break;
                    }
                }

                ArrayList<String> below =targetSlot.getbelow();

                if (below.size() > 0) { //如果这个空隙后面的任务数大于0
                    String buf[] = below.get(0).split(" ");

                    //原生空隙编号,其实也就是对应着接下来要插的这个任务的编号（从0开始）
                    int originSlotId = Integer.valueOf(buf[2]).intValue();

                    //将要插入空隙后面的任务信息都往后移一位
                    for (int i = TASKInPe.size(); i > originSlotId; i--) {
                        Integer[] st_fitemp = new Integer[4];
                        st_fitemp[0] = TASKInPe.get(i - 1)[0];
                        st_fitemp[1] = TASKInPe.get(i - 1)[1];
                        st_fitemp[2] = TASKInPe.get(i - 1)[2];
                        st_fitemp[3] = TASKInPe.get(i - 1)[3];
                        TASKInPe.put(i, st_fitemp);
                    }

                    Integer[] st_fi = new Integer[4];
                    st_fi[0] = earliestFinish_StartTime;
                    st_fi[1] = earliestFinish_Time;
                    st_fi[2] = targetTask_originTask.getdagid();
                    st_fi[3] = targetTask_originTask.getid();
                    TASKInPe.put(originSlotId, st_fi);

                    targetTask_originTask.setisfillback(true);

                   // changeInPE(slotlistinpe, originSlotId);

                } else {
                    Integer[] st_fi = new Integer[4];
                    st_fi[0] = earliestFinish_StartTime;
                    st_fi[1] = earliestFinish_Time;
                    st_fi[2] = targetTask_originTask.getdagid();
                    st_fi[3] = targetTask_originTask.getid();
                    TASKInPe.put(TASKInPe.size(), st_fi);
                }

            } else {
                Integer[] st_fi = new Integer[4];
                st_fi[0] = earliestFinish_StartTime;
                st_fi[1] = earliestFinish_Time;
                st_fi[2] = targetTask_originTask.getdagid();
                st_fi[3] = targetTask_originTask.getid();
                TASKInPe.put(TASKInPe.size(), st_fi);
            }

            //重新计算新的空隙
            computeSlot(dagmap.getsubmittime(), dagmap.getDAGdeadline());

            //删除这个已经调度成功的任务
            readylist.remove(earliestFinish_readyListIndex);

        }

        return findsuc;
    }





    /**
     * @param dagmap
     * @param currentTask
     * @throws
     * @Title: findSlot
     * @Description: 为就绪队列中的任务查找合适的放置slot
     * @return:
     */
    public static int[] findSlot(DAG dagmap, Task currentTask) {

        int message[] = new int[6];

        boolean findsuc = false;
        int startmin = timewindowmax;
        int pemin = -1;
        int[] startinpe = new int[peNumber];
        int[] slotid = new int[peNumber];
        int[] isneedslide = new int[peNumber]; // 0 means don't need 1 means need slide
        int[] slidelength = new int[peNumber];

//        for (int k = 0; k < peNumber; k++) {
//            pushFlag[k] = 0;
//        }

        Map<String, Double> DAGTaskDependValue = dagmap.getdependvalue();

        //得到该任务的父任务列表,不是直接操作父任务对象，得到相关参数就好
        ArrayList<Task> pre_queue = new ArrayList<Task>();
        ArrayList<Integer> pre =currentTask.getpre();
        if (pre.size() >= 0) {
            for (int j = 0; j < pre.size(); j++) {
                Task task = getTaskByDagIdAndTaskId(currentTask.getdagid(), pre.get(j));
                pre_queue.add(task);
            }
        }

        for (int i = 0; i < peNumber; i++) {

            //父任务的执行结束时间
            int predone = -1;

            if (pre_queue.size() == 1) {//只有一个父任务
                if (pre_queue.get(0).getfillbackpeid() == i) {//与父任务在同一个处理器上
                    predone = pre_queue.get(0).getfillbackfinishtime();
                } else {
                    //获得两者之间的传输时延
                    int value = (int) (double) DAGTaskDependValue.get(String.valueOf(pre_queue.get(0).getid()) + " " + String.valueOf(currentTask.getid()));
                    //加上时延后的父任务结束时间
                    predone = pre_queue.get(0).getfillbackfinishtime() + value;
                }
            } else if (pre_queue.size() > 1) {//不止一个父任务
                for (int j = 0; j < pre_queue.size(); j++) {
                    if (pre_queue.get(j).getfillbackpeid() == i) {
                        if (predone < pre_queue.get(j).getfillbackfinishtime()) {//与父任务在同一处理器上
                            predone = pre_queue.get(j).getfillbackfinishtime();
                        }
                    } else {//与父任务不在同一处理器上
                        //获得两者之间的传输时延
                        int valu = (int) (double) DAGTaskDependValue.get(String.valueOf(pre_queue.get(j).getid()) + " " + String.valueOf(currentTask.getid()));
                        //加上时延后的父任务结束时间
                        int value = pre_queue.get(j).getfillbackfinishtime() + valu;
                        //找到最大的时延
                        if (predone < value)
                            predone = value;
                    }
                }
            }else {
                System.out.println("没有父任务。。。无法执行结果");
            }

            startinpe[i] = -1;
            ArrayList<Slot> slotlistinpe = new ArrayList<Slot>();

            for (int j = 0; j < SlotListInPes.get(i).size(); j++)
                slotlistinpe.add((Slot) SlotListInPes.get(i).get(j));

            //对适宜的空隙进行遍历
            for (int j = 0; j < SlotListInPes.get(i).size(); j++) {
                int slst = slotlistinpe.get(j).getslotstarttime();
                int slfi = slotlistinpe.get(j).getslotfinishtime();

                if (predone <= slst) {
                    if ((slst + currentTask.getts()) <= slfi
                            && (slst + currentTask.getts()) <= currentTask.getdeadline()) {
                        startinpe[i] = slst;
                        slotid[i] = slotlistinpe.get(j).getslotId();
                        isneedslide[i] = 0;
                        break;
                    } else if ((slst + currentTask.getts()) > slfi
                            && (slst + currentTask.getts()) <= currentTask.getdeadline()) {
                        continue;
                    }
                } else if (predone > slst && predone < slfi) {
                    if ((predone + currentTask.getts()) <= slfi
                            && (predone + currentTask.getts()) <= currentTask.getdeadline()) {
                        startinpe[i] = predone;
                        slotid[i] = slotlistinpe.get(j).getslotId();
                        isneedslide[i] = 0;
                        break;
                    } else if ((predone + currentTask.getts()) > slfi
                            && (predone + currentTask.getts()) <= currentTask.getdeadline()) {
                        continue;
                    }
                }
            }
        }


        //对于每一个处理器上的插入结果，找到可最早开始的处理器编号。
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
            if (isneedslide[pemin] == 1)//需要后推
                message[5] = slidelength[pemin];
            else
                message[5] = -1;
        } else {//该作业不能插入任何一个处理器上
            message[0] = 0;
        }
        return message;
    }


    /**
     * @param dagmap
     * @param dagtemp
     * @throws
     * @Title: findFirstTaskSlot
     * @Description: 找到本作业第一个任务所在的空隙
     * @return:
     */
    public static boolean findFirstTaskSlot(DAG dagmap, Task dagtemp) throws Exception {
        // perfinish is the earliest finish time minus task'ts time, the earliest start time

        boolean findsuc = false;
        int startmin = timewindowmax;
        int finishmin = 0;
        int pemin = -1;
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

            HashMap<Integer, Integer[]> TASKInPe = TASKListInPes.get(pemin);

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
     * @param dagmap
     * @throws
     * @Title: fillBack
     * @Description: fillBack method
     * @return:
     */
    public static boolean fillBack(DAG dagmap) throws Exception {

        int runtime = dagmap.getsubmittime();
        boolean fillbacksuc = true;
        boolean fini = true;

        ArrayList<Task> readylist = new ArrayList<Task>();
        ArrayList<Task> DAGTaskList = new ArrayList<Task>();
        Map<String, Double> DAGTaskDependValue = new HashMap<String, Double>();
        DAGTaskDependValue = dagmap.getdependvalue();


        DAGTaskList = dagmap.gettasklist();


        //对于每一秒进行一轮调度【本轮要是有任务调度失败就认定为整个作业失败，因为现在的就绪任务都失败了，以后的肯定也执行超时的】
        do {

            //轮询任务找寻当前时刻执行结束的任务。
            for (Task task : DAGTaskList) {//当前时刻，有任务在此刻执行结束，设置其执行结束时间以及其完成标记
                if ((task.getfillbackstarttime() + task.getts()) == runtime
                        && task.getfillbackready()//且这个任务是就绪的
                        && task.getfillbackdone() == false) {//且这个任务没有标记完成
                    task.setfillbackfinishtime(runtime);
                    task.setfillbackdone(true);
                }
            }


            //找寻对应的就绪队列
            for (Task task : DAGTaskList) {

                if (task.getid() == 0 && task.getfillbackready() == false) {
                    //为第一个任务也就是头结点找寻合适的slot
                    if (findFirstTaskSlot(dagmap, DAGTaskList.get(0))) {
                        //设置父任务的标记为true
                        task.setprefillbackready(true);
                        task.setprefillbackdone(true);

                        task.setfillbackpass(false);
                        //如果这个任务的执行时长为0，那么可以直接在这里标记为成功的。
                        if (task.getts() == 0) {
                            task.setfillbackfinishtime(task.getfillbackstarttime());
                            task.setfillbackdone(true);
                        }
                    } else {//如果头结点失败了就是整个作业失败了
                        fillbacksuc = false;
                        return fillbacksuc;
                    }
                }


                //构建就绪队列
                if (task.getarrive() <= runtime && task.getfillbackdone() == false
                        && task.getfillbackready() == false
                        && task.getfillbackpass() == false) {
                    //检查这个任务是否就绪
                    boolean ifready = checkReady(task, DAGTaskList, DAGTaskDependValue, runtime);
                    if (ifready) {
                        //【不是在这里设置任务的task.setfillbackready(true);】
                        task.setfillbackready(true);
                        task.setprefillbackready(true);
                        task.setprefillbackdone(true);
                        //加入就绪队列
                        readylist.add(task);
                    }
                }

            }


            //本轮调度
            if (readylist.size() > 0) {
                if (!scheduleReadyTasks(dagmap, readylist)) {//本轮就绪队列调度中有任务失败，则整个作业失败
                    fillbacksuc = false;
                    return fillbacksuc;
                }
            }



            fini = true;
            for (Task task : DAGTaskList) {
                if (task.getfillbackdone() == false) {//本轮调度中有任务调度失败了
                    fini = false;
                    break;
                }
            }
            runtime = runtime + T;
        } while (runtime <= dagmap.getDAGdeadline() && !fini && fillbacksuc);


        if (fini) {//如果调度成功，设置每个task的结束时间
            for (Task dag : DAGTaskList) {
                dag.setfillbackfinishtime(dag.getfillbackstarttime() + dag.getts());
            }
        } else {
            fillbacksuc = false;

        }
        return fillbacksuc;
    }


    /**
     * @param i
     * @param SlotListInPestemp：备份的slot
     * @param TASKListInPestemp:备份的task
     * @throws
     * @Title: scheduleOtherDAG
     * @Description: schedule other DAG
     */
    public static void scheduleOtherDAG(int i, HashMap<Integer, ArrayList> SlotListInPestemp, HashMap<Integer, HashMap> TASKListInPestemp) throws Exception {

        int arrive = DAGMapList.get(i).getsubmittime();
        if (arrive > currentTime)
            currentTime = arrive;

        DAG currentDAG=DAGMapList.get(i);
        //作业是否调度成功标记
        boolean flag = fillBack(currentDAG);

        if (!flag) {

            //恢复以前的task、slot情况
            restoreSlotandTASK(SlotListInPestemp, TASKListInPestemp);

            //将作业标记为未完成与过期
            currentDAG.setfillbackdone(false);
            currentDAG.setfillbackpass(true);

            //将整个作业的任务都设置为过期
            for (int j = 0; j <currentDAG.gettasklist().size(); j++) {
                Task task= (Task) currentDAG.gettasklist().get(j);
                task.setprefillbackdone(false);
                task.setfillbackpass(true);
                task.setready(false);
                task.setprefillbackdone(false);
                task.setprefillbackpass(true);
                task.setprefillbackready(false);
            }
        } else {
            currentDAG.setfillbackdone(true);
        }
    }


    /**
     * @param submit
     * @param deadline:
     * @throws
     * @Title: computeSlot
     * @Description: 计算处理上的空隙
     */
    public static void computeSlot(int submit, int deadline) throws Exception {

        //清除上一轮的空隙结果
        SlotListInPes.clear();

        for (int i = 0; i < peNumber; i++) {//针对每一个处理器开始进行处理

            int slotIndex = 0;

            //得到这个处理器上的task列表
            HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
            TASKInPe = TASKListInPes.get(i);

            ArrayList<Slot> slotListinpe = new ArrayList<Slot>();
            //处理器上有多任务时，原本处理器上所有空隙集合（没有根据submit、deadline进行筛选）
            ArrayList<Slot> slotListinpe_ori = new ArrayList<Slot>();

            if (TASKInPe.size() == 0) {//如果该处理器上本身是没有任务的，那么空隙就只有一个
                Slot slotTemp = new Slot();
                slotTemp.setPEId(i);
                //空隙编号从0开始
                slotTemp.setslotId(slotIndex);
                slotTemp.setslotstarttime(submit);
                slotTemp.setslotfinishtime(deadline);
                slotListinpe.add(slotTemp);
                slotIndex++;
            } else if (TASKInPe.size() == 1) {//如果其上本身只有一个任务，那么认为有两个空隙
                Integer taskStartTime = TASKInPe.get(0)[0];
                Integer taskEndTime = TASKInPe.get(0)[1];
                Integer dagId = TASKInPe.get(0)[2];
                Integer taskId = TASKInPe.get(0)[3];

                if (taskStartTime >= submit && submit <= deadline) {//submit---taskstart————
                    if (deadline <= taskStartTime) {//submit---deadline---taskstart————，只会产生一个块
                        Slot slotTemp = new Slot();
                        ArrayList<String> below_ = new ArrayList<String>();
                        //此块之后的task信息（作业id，任务id，空隙编号【这个编号是处理器上空隙的原生编号】）
                        below_.add(dagId + " " + taskId + " " + 0);
                        slotTemp.setPEId(i);
                        slotTemp.setslotId(slotIndex);
                        slotTemp.setslotstarttime(submit);
                        slotTemp.setslotfinishtime(deadline);
                        slotTemp.setbelow(below_);
                        slotListinpe.add(slotTemp);
                        slotIndex++;
                    } else if (deadline <= taskEndTime) {//submit---taskstart————deadline————taskend，只会产生一个块
                        Slot slotTemp = new Slot();
                        ArrayList<String> below_ = new ArrayList<String>();
                        //此块之后的task信息（作业id，任务id，空隙编号）
                        below_.add(dagId + " " + taskId + " " + 0);
                        slotTemp.setPEId(i);
                        slotTemp.setslotId(slotIndex);
                        slotTemp.setslotstarttime(submit);
                        slotTemp.setslotfinishtime(taskStartTime);
                        slotTemp.setbelow(below_);
                        slotListinpe.add(slotTemp);
                        slotIndex++;
                    } else {//submit---taskstart————taskend---deadline，只会产生两个个块
                        Slot slotTemp = new Slot();
                        ArrayList<String> below_ = new ArrayList<String>();
                        //此块之后的task信息（作业id，任务id，空隙编号）
                        below_.add(dagId + " " + taskId + " " + 0);
                        slotTemp.setPEId(i);
                        slotTemp.setslotId(slotIndex);
                        slotTemp.setslotstarttime(submit);
                        slotTemp.setslotfinishtime(taskStartTime);
                        slotTemp.setbelow(below_);
                        slotListinpe.add(slotTemp);
                        slotIndex++;

                        Slot temp = new Slot();

                        temp.setPEId(i);
                        temp.setslotId(slotIndex);
                        temp.setslotstarttime(taskEndTime);
                        temp.setslotfinishtime(deadline);
                        slotListinpe.add(temp);
                        slotIndex++;

                    }
                } else if (submit <= taskEndTime && deadline >= taskEndTime && submit <= deadline) {//taskstart————submit————taskend---deadline
                    Slot slotTemp = new Slot();
                    slotTemp.setPEId(i);
                    slotTemp.setslotId(slotIndex);
                    slotTemp.setslotstarttime(taskEndTime);
                    slotTemp.setslotfinishtime(deadline);
                    slotListinpe.add(slotTemp);
                    slotIndex++;
                } else if (submit > taskEndTime && deadline > taskEndTime && submit <= deadline) {//taskstart————taskend----submit---deadline，会产生一个
                    Slot slotTemp = new Slot();
                    slotTemp.setPEId(i);
                    slotTemp.setslotId(slotIndex);
                    slotTemp.setslotstarttime(submit);
                    slotTemp.setslotfinishtime(deadline);
                    slotListinpe.add(slotTemp);
                    slotIndex++;
                }
            } else {//如果该处理器上有多个任务，这里只是算出来所有的空隙，与具体的作业开启时间没有联系

                //将处理器的开头的那个【0,0-n】也算在了空隙中
                if (TASKInPe.get(0)[0] >= 0) {
                    Slot tem = new Slot();
                    ArrayList<String> below_ = new ArrayList<String>();
                    //提取这个空隙后的task列表信息
                    for (int k = 0; k < TASKInPe.size(); k++) {
                        below_.add(TASKInPe.get(k)[2] + " " + TASKInPe.get(k)[3] + " " + 0);
                    }
                    tem.setPEId(i);
                    tem.setslotId(slotIndex);
                    tem.setslotstarttime(0);
                    tem.setslotfinishtime(TASKInPe.get(0)[0]);
                    tem.setbelow(below_);
                    slotListinpe_ori.add(tem);
                    slotIndex++;
                }

                //获取该处理器上的剩余其它空隙。不包含最后的那个大空隙
                for (int j = 1; j < TASKInPe.size(); j++) {
                    if (TASKInPe.get(j - 1)[1] <= TASKInPe.get(j)[0]) {
                        Slot tem = new Slot();
                        ArrayList<String> below_ = new ArrayList<String>();
                        for (int k = j; k < TASKInPe.size(); k++) {
                            //提取此块之后的task信息（作业id，任务id，空隙编号）
                            below_.add(TASKInPe.get(k)[2] + " " + TASKInPe.get(k)[3] + " " + j);
                        }
                        tem.setPEId(i);
                        tem.setslotId(slotIndex);
                        tem.setslotstarttime(TASKInPe.get(j - 1)[1]);
                        tem.setslotfinishtime(TASKInPe.get(j)[0]);
                        tem.setbelow(below_);
                        //原本处理器上所有空隙集合
                        slotListinpe_ori.add(tem);
                        slotIndex++;
                    } else {
                        throw new Exception("------------------本处理器上任务的安排时间存在重叠");
                    }
                }


                //找寻【起始空隙编号】，这个slotListinpe_ori中是没有包含最后那个大空隙的
                int startslot = 0;
                for (int j = 0; j < slotListinpe_ori.size(); j++) {
                    Slot slotOrigin = slotListinpe_ori.get(j);

                    if (j == 0 && (slotOrigin.slotstarttime != slotOrigin.slotfinishtime)) {
                        if (submit >= 0 && submit < slotOrigin.slotfinishtime) {
                            startslot = 0;
                            slotOrigin.setslotstarttime(submit);
                            break;
                        }
                    } else if (j > 0 && j <= (slotListinpe_ori.size() - 1)) {
                        if (slotOrigin.getslotstarttime() <= submit // --slotstarttime--submit--slotfinishtime--
                                && slotOrigin.getslotfinishtime() > submit) {
                            slotOrigin.setslotstarttime(submit);
                            startslot = j;
                            break;
                        } else if (slotOrigin.getslotstarttime() > submit // slotfinishtime(前一个空隙)--submit---slotstarttime
                                && slotListinpe_ori.get(j - 1).getslotfinishtime() <= submit) {
                            startslot = j;
                            break;
                        }
                    }

                    //如果只有最后一个大空隙匹配,那么编号设置得打一些，这样就可以跳过下面的for循环。
                    if (j == (slotListinpe_ori.size() - 1))
                        startslot = slotListinpe_ori.size();
                }

                int slotNewIndex = 0;
                for (int j = startslot; j < slotListinpe_ori.size(); j++) {
                    Slot slotTemp = slotListinpe_ori.get(j);
                    int slotStartTime = slotTemp.getslotstarttime();
                    int slotFinishTime = slotTemp.getslotfinishtime();

                    if (slotFinishTime <= deadline) {
                        //符合范围的slot会有一个新的编号，从0开始
                        slotTemp.setslotId(slotNewIndex);
                        slotListinpe.add(slotTemp);
                        slotNewIndex++;
                    } else if (slotStartTime < deadline && slotFinishTime > deadline) {// ---slotstarttime---deadline---slotfinishtime---
                        slotTemp.setslotId(slotNewIndex);
                        slotTemp.setslotfinishtime(deadline);
                        slotListinpe.add(slotTemp);
                        break;
                    }
                }

                if (TASKInPe.get(TASKInPe.size() - 1)[1] <= submit) {//如果处理任务器上最后一个任务的结束时间在submit之前，那么只有一个空隙
                    Slot slotTemp = new Slot();
                    slotTemp.setPEId(i);
                    slotTemp.setslotId(slotNewIndex);
                    slotTemp.setslotstarttime(submit);
                    slotTemp.setslotfinishtime(deadline);
                    slotListinpe.add(slotTemp);

                } else if (TASKInPe.get(TASKInPe.size() - 1)[1] < deadline && TASKInPe.get(TASKInPe.size() - 1)[1] > submit) {//对最后一个大空隙处理
                    Slot slotTemp = new Slot();
                    slotTemp.setPEId(i);
                    slotTemp.setslotId(slotNewIndex);
                    slotTemp.setslotstarttime(TASKInPe.get(TASKInPe.size() - 1)[1]);
                    slotTemp.setslotfinishtime(deadline);
                    slotListinpe.add(slotTemp);
                }
            }

            //将当前处理器的空隙结果放入全局变量中
            SlotListInPes.put(i, slotListinpe);
        }
    }

    //==================================调度第一个作业==========================
    //==================================调度第一个作业==========================
    //==================================调度第一个作业==========================
    //==================================调度第一个作业==========================
    //==================================调度第一个作业==========================

    /**
     * @throws
     * @Title: scheduleFirstDAG
     * @Description: 调度第一个作业
     */
    public static void scheduleFirstDAG() {

        firstDAGSchedule(DAGMapList.get(0));

        Task tem = (Task) DAGMapList.get(0).gettasklist().get(DAGMapList.get(0).gettasknumber() - 1);

        if (tem.getfillbackdone()) {
            DAGMapList.get(0).setfillbackdone(true);
            DAGMapList.get(0).setfillbackpass(false);
        }
        changeTaskListInPE(DAGMapList.get(0));
    }


    /**
     * @param dagmap:
     * @throws
     * @Title: firstDAGSchedule
     * @Description: 调度整体的第一个作业
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

            //task中存在调度失败的或者过期的，则会退出本作业的调度
            for (Task task : DAGTaskList) {
                if (task.getfillbackdone() == false && task.getfillbackpass() == false) {
                    fini = false;
                    break;
                }
            }
            if (fini) {
                break;
            }

            //轮询任务找寻当前时刻执行结束的任务。
            for (Task task : DAGTaskList) {
                if ((task.getfillbackstarttime() + task.getts()) == time//当前时刻有一个任务执行结束
                        && task.getfillbackready()//且这个任务是就绪的
                        && task.getfillbackdone() == false) {//且这个任务没有标记完成
                    //设置结束时间
                    task.setfillbackfinishtime(time);
                    task.setfillbackdone(true);
                    //释放这个处理器，证明这个处理器现在没有任务在其上运行，可以参与调度
                    PEList.get(task.getfillbackpeid()).setfree(true);
                }
            }

            //轮询任务找寻当前时刻可以就绪的任务并加入就绪队列
            for (Task task : DAGTaskList) {
                if (task.getarrive() <= time && task.getfillbackdone() == false
                        && task.getfillbackready() == false
                        && task.getfillbackpass() == false) {
                    //检查这个任务是否就绪
                    boolean ifready = checkReady(task, DAGTaskList, DAGTaskDependValue, time);
                    if (ifready) {
                        task.setfillbackready(true);
                        readyTaskQueue.add(task);
                    }
                }
            }

            //当前轮的调度
            schedule(DAGTaskList, DAGTaskDependValue, time);

            //判断得到调度，即从当前时刻被调度上处理器开始执行的任务是否执行结束时间会超过deadline，如果会，则这个任务调度失败。
            for (Task task : DAGTaskList) {
                if (task.getfillbackstarttime() == time
                        && task.getfillbackready()
                        && task.getfillbackdone() == false) {//执行开始时间是当前时间的task


                    //【这里的deadline是这个任务的最晚截止时间】
                    //当这个task的开始时间与执行结束时间都是小于它的最晚截止时间的时候任务才能被成功的执行，不然都是炸
                    if (task.getdeadline() >= time && (time + task.getts()) <= task.getdeadline()) {
                        if (task.getts() == 0) {
                            //任务的执行时间是0的，在这里就设置好执行结束时间。
                            task.setfillbackfinishtime(time);
                            task.setfillbackdone(true);
                            time = time - T;
                        } else {//执行时间不为0，则对应处理器不是空闲的，执行结束时间前面会设置
                            PEList.get(task.getfillbackpeid()).setfree(false);
                            PEList.get(task.getfillbackpeid()).settask(task.getid());
                        }
                    } else {//执行时间超过了deadline，任务过期【可以不在这里结束本作业的调度，因为while循环的开头就检查了，本轮设置pass，下一秒开始变回检测到并退出】
                        task.setfillbackpass(true);
                    }
                }
            }
            //当前时间增加步长
            time = time + T;
        }
    }

    /**
     * @param DAGTaskList
     * @param DAGTaskDependValue
     * @param time:
     * @throws
     * @Title: schedule
     * @Description: schedule ready list
     */
    private static void schedule(ArrayList<Task> DAGTaskList, Map<String, Double> DAGTaskDependValue, int time) {


        //【目前这里调整与没有调整是没有什么区别的，因为现在每个task的到达时间都是一样的呀，除非改变成里面每个任务的就绪时间成为它的到达时间】
        ReadyQueueUtil.changeReadyQueue(readyTaskQueue);

        //当前时刻将整个就绪队列调度完毕才将时间推进到下一秒。
        for (int i = 0; i < readyTaskQueue.size(); i++) {
            Task readyTask = new Task();
            readyTask = readyTaskQueue.get(i);
            //为当前task选择合适的处理器
            for (Task dag : DAGTaskList) {
                //找到原本任务对象，而不是对readylist中的task对象进行调整，调整它没有用。
                if (readyTask.getid() == dag.getid()) {
                    choosePE(dag, DAGTaskDependValue, time);
                    break;
                }
            }
        }

        //清除就绪队列
        readyTaskQueue.clear();
    }

    /**
     * @param task
     * @param DAGTaskList
     * @param DAGTaskDependValue
     * @param time
     * @throws
     * @Title: checkReady
     * @Description: 检查这个任务是否已经就绪
     * @return:
     */
    private static boolean checkReady(Task task, ArrayList<Task> DAGTaskList, Map<String, Double> DAGTaskDependValue, int time) {

        boolean isready = true;


            if (time > task.getdeadline()) {//如果已经超过了这个dag的最晚开始时间，则该任务被设置为过期
                task.setfillbackpass(true);
                return false;
            }

            if (task.getfillbackstarttime() == -1
                    && task.getfillbackpass() == false) {//这个task不过期且调度时间是默认值

            //获得该task的父task列表
            ArrayList<Integer> pre = task.getpre();

            if (pre.size() >= 0) {//如果存在父task
                for (int j = 0; j < pre.size(); j++) {
                    Task temp = new Task();
                    temp = getTaskByDagIdAndTaskId(task.getdagid(), pre.get(j));
                    if (temp.getfillbackpass()) {//如果父task过期，则不就绪,自己也将被设置为过期
                        task.setfillbackpass(true);
                        return false;
                    }

                    if (!temp.getfillbackdone()) {//父任务调度失败，则不就绪
                        return false;
                    }
                }
            }
        }


        return isready;
    }

    /**
     * @param targetTask
     * @param DAGTaskDependValue
     * @param time               :
     * @throws
     * @Title: choosePE
     * @Description: 为每一个task选择合适PE【这个里面没有考虑如果选择在这个处理器上执行的会不会执行结束时间超出deadline】
     * <p>
     * <p>
     * 【备注：
     * 1、这里选择的是第一个作业的调度方式
     * 2、对于第一个作业如果要修改挑选处理器的方式要在这里修改，比如，当执行开始时间相同的处理器有多个，则随机挑选处理器的方式就可以在这里尝试。
     * 3、一定要记得检测时间是否会有重叠的现象】
     */
    private static void choosePE(Task targetTask, Map<String, Double> DAGTaskDependValue, int time) {

        ArrayList<Task> pre_queue = new ArrayList<Task>();
        ArrayList<Integer> pre = new ArrayList<Integer>();

        //获取每个task的父task数目
        pre = targetTask.getpre();

        //将其对应的父task加入到父任务队列中
        if (pre.size() >= 0) {
            for (int j = 0; j < pre.size(); j++) {
                Task buf = new Task();
                buf = getTaskByDagIdAndTaskId(targetTask.getdagid(), pre.get(j));
                pre_queue.add(buf);
            }
        }

        //在处理器上的最早开始时间【其中只考虑了计算开始时间，没有考虑是否这个开始时间会超过本身的deadline】
        int temp[] = new int[PEList.size()];

        //针对每个处理器做处理
        for (int i = 0; i < PEList.size(); i++) {
            HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
            //得到某个处理器上的
            TASKInPe = TASKListInPes.get(i);

            if (pre_queue.size() == 0) {//没有父任务
                if (TASKInPe.size() == 0) {//该处理器上没有任务
                    temp[i] = time;
                } else {//该处理器上有任务
                    if (time > TASKInPe.get(TASKInPe.size() - 1)[1])
                        temp[i] = time;
                    else
                        temp[i] = TASKInPe.get(TASKInPe.size() - 1)[1];
                }
            } else if (pre_queue.size() == 1) {//父任务为1
                if (pre_queue.get(0).getfillbackpeid() == PEList.get(i).getID()) {//如果父任务的调度位置是当前的这个处理器
                    if (TASKInPe.size() == 0) {
                        temp[i] = time;
                    } else {
                        if (time > TASKInPe.get(TASKInPe.size() - 1)[1])
                            temp[i] = time;
                        else
                            temp[i] = TASKInPe.get(TASKInPe.size() - 1)[1];
                    }
                } else {//如果父任务与自己不在同一个处理器上
                    //传输时延
                    int value = (int) (double) DAGTaskDependValue.get(String.valueOf(pre_queue.get(0).getid()) + " " + String.valueOf(targetTask.getid()));

                    if (TASKInPe.size() == 0) {//处理器上本没有任务
                        if ((pre_queue.get(0).getfillbackfinishtime() + value) < time)
                            temp[i] = time;
                        else
                            temp[i] = pre_queue.get(0).getfillbackfinishtime() + value;
                    } else {//处理器上有任务
                        if ((pre_queue.get(0).getfillbackfinishtime() + value) > TASKInPe.get(TASKInPe.size() - 1)[1]
                                && (pre_queue.get(0).getfillbackfinishtime() + value) > time)
                            temp[i] = pre_queue.get(0).getfillbackfinishtime() + value;
                        else if (time > (pre_queue.get(0).getfillbackfinishtime() + value)
                                && time > TASKInPe.get(TASKInPe.size() - 1)[1])
                            temp[i] = time;
                        else
                            temp[i] = TASKInPe.get(TASKInPe.size() - 1)[1];
                    }
                }
            } else {//有多个父任务
                int max = time;

                for (int j = 0; j < pre_queue.size(); j++) {
                    if (pre_queue.get(j).getfillbackpeid() == PEList.get(i).getID()) {//与父任务在同一个处理器上
                        if (TASKInPe.size() != 0) {
                            if (max < TASKInPe.get(TASKInPe.size() - 1)[1])
                                max = TASKInPe.get(TASKInPe.size() - 1)[1];
                        }
                    } else {//与父任务不不再同一个处理器上
                        //传输时延
                        int valu = (int) (double) DAGTaskDependValue.get(String.valueOf(pre_queue.get(j).getid()) + " " + String.valueOf(targetTask.getid()));
                        //在本处理器上的最早开始时间
                        int value = pre_queue.get(j).getfillbackfinishtime() + valu;

                        if (TASKInPe.size() == 0) {//处理器上本没有任务
                            if (max < value)
                                max = value;
                        } else {//处理器上有任务
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

        boolean flag = false;
        int taskDeadline = targetTask.getdeadline();

        //找到所有处理器得到的时间中最早开始的时间并且要考虑其中的deadline

        //【但是其中没有考虑放在这个上面的时候，最后执行完毕的时候会不会超出deadline】
        //【更新，其实更上层有对截止时间超出task的deadline（即，task的最晚开始时间）的检测】
        for (int i = 0; i < PEList.size(); i++) {
            if (temp[i] < taskDeadline && min > temp[i]) {
                min = temp[i];
                minpeid = i;
                flag = true;
            }
        }

        if (flag) {
            HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
            //这个TASKInPe是全局的那个
            TASKInPe = TASKListInPes.get(minpeid);

            //设置任务的执行处理器编号
            targetTask.setfillbackpeid(minpeid);
            //设置任务的执行时间
            targetTask.setts(targetTask.getlength());
            //设置任务的执行开始时间
            targetTask.setfillbackstarttime(min);
            //设置任务可能执行完毕时间【没有考虑是否执行完毕时会超出deadline】-----------------》记得修改
            targetTask.setfinish_suppose(targetTask.getfillbackstarttime() + targetTask.getts());

            Integer[] st_fi = new Integer[4];
            st_fi[0] = targetTask.getfillbackstarttime();
            st_fi[1] = targetTask.getfillbackstarttime() + targetTask.getts();
            st_fi[2] = targetTask.getdagid();
            st_fi[3] = targetTask.getid();
            //【真实的将这个任务放在了这个处理器上】
            TASKInPe.put(TASKInPe.size(), st_fi);
        } else {
            targetTask.setfillbackpass(true);
        }

    }


    //-------------------------------从输出文档中转化作业为内存中的对象-------------------------------------
    //-------------------------------从输出文档中转化作业为内存中的对象-------------------------------------
    //-------------------------------从输出文档中转化作业为内存中的对象-------------------------------------
    //-------------------------------从输出文档中转化作业为内存中的对象-------------------------------------
    //-------------------------------从输出文档中转化作业为内存中的对象-------------------------------------

    /**
     * @param i
     * @param preexist
     * @param tasknumber
     * @param arrivetimes
     * @param pathXML
     * @return
     * @throws NumberFormatException
     * @throws IOException
     * @throws
     * @Title: initDAG_createDAGdepend_XML
     * @Description: initial DAG ,add DAG dependence
     */
    @SuppressWarnings("rawtypes")
    private static int initDAG_createDAGdepend_XML(int i, int preexist, int tasknumber, int arrivetimes, String pathXML)
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
                //islastnum++;
            }

            Task_queue.add(dag);
            TASK_queue_personal.add(dag_persional);

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
            Task_queue.get(presuc[0]).addToSuc(presuc[1]);
            Task_queue.get(presuc[1]).addToPre(presuc[0]);

            DAGDependMap_personal.put(Integer.valueOf(pre_suc[0]).intValue(),
                    Integer.valueOf(pre_suc[1]).intValue());
            DAGDependValueMap_personal.put((pre_suc[0] + " " + pre_suc[1]),
                    (double) datasize);

            int tem0 = Integer.parseInt(pre_suc[0]);
            int tem1 = Integer.parseInt(pre_suc[1]);
            TASK_queue_personal.get(tem0).addToSuc(tem1);
            TASK_queue_personal.get(tem1).addToPre(tem0);
        }

        back = preexist + tasknumber;
        return back;
    }

    /**
     * @param dead_line
     * @param dagdepend_persion
     * @throws Throwable:
     * @throws
     * @Title: createDeadline_XML
     * @Description: 为每一个task按照比例计算deadline,这个deadline是此任务计算中最晚的截止时间，其中是没有考虑传输时延的，因为你也不知道会在哪个处理器上执行
     */
    private static void createDeadline_XML(int dead_line, DAGDepend dagdepend_persion) throws Throwable {
        //处理器的计算能力
        int maxability = 1;
        int max = 10000;

        for (int k = TASK_queue_personal.size() - 1; k >= 0; k--) {

          //  ArrayList<Task> suc_queue = new ArrayList<Task>();
            ArrayList<Integer> suc = new ArrayList<Integer>();
            //获取该task的子task列表
            suc = TASK_queue_personal.get(k).getsuc();

            if (suc.size() > 0) {
                for (int j = 0; j < suc.size(); j++) {
                    int tem;
                    Task subTask = getTaskByTaskId(suc.get(j));
                    //suc_queue.add(subTask);
                    //这个子task对应的最晚开始时间
                    tem = (subTask.getdeadline() - (subTask.getlength() / maxability));
                    //找寻所有子task最晚开始时间中最早的时间
                    if (max > tem)
                        max = tem;
                }
                TASK_queue_personal.get(k).setdeadline(max);
            } else {
                TASK_queue_personal.get(k).setdeadline(dead_line);
            }

        }
    }

    /**
     * @param dagdepend
     * @param vcc
     * @param pathXML
     * @throws Throwable :
     * @throws
     * @Title: initDagMap
     * @Description: 初始化作业
     */
    public static void initDagMap(DAGDepend dagdepend, PEComputerability vcc,
                                  String pathXML) throws Throwable {
        int pre_exist = 0;

        File file = new File(pathXML);
        String[] fileNames = file.list();
        int num = fileNames.length - 1;

        BufferedReader bd = new BufferedReader(new FileReader(pathXML + "Deadline.txt"));
        String buffered;
        for (int i = 0; i < num; i++) {

            DAG dagmap = new DAG();
            DAGDepend dagdepend_persional = new DAGDepend();
            TASK_queue_personal.clear();

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

            dagdepend_persional.setDAGList(TASK_queue_personal);
            dagdepend_persional.setDAGDependMap(DAGDependMap_personal);
            dagdepend_persional
                    .setDAGDependValueMap(DAGDependValueMap_personal);

            createDeadline_XML(deadline, dagdepend_persional);

            int number_1 = Task_queue.size();
            int number_2 = TASK_queue_personal.size();
            for (int k = 0; k < number_2; k++) {
                Task_queue.get(number_1 - number_2 + k).setdeadline(TASK_queue_personal.get(k).getdeadline());
            }

            dagmap.settasknumber(tasknum);
            dagmap.setDAGId(i);
            dagmap.setDAGdeadline(deadline);
            dagmap.setsubmittime(arrivetime);
            dagmap.settasklist(TASK_queue_personal);
            dagmap.setDAGDependMap(DAGDependMap_personal);
            dagmap.setdependvalue(DAGDependValueMap_personal);
            DAGMapList.add(dagmap);
        }
        dagdepend.setdagmaplist(DAGMapList);
        dagdepend.setDAGList(Task_queue);
        dagdepend.setDAGDependMap(DAGDependMap);
        dagdepend.setDAGDependValueMap(DAGDependValueMap);

    }

    /**
     * @throws Throwable:
     * @throws
     * @Title: initPE
     * @Description: 初始化处理器
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


    //----------------------------工具方法-------------------------------
    //----------------------------工具方法-------------------------------
    //----------------------------工具方法-------------------------------
    //----------------------------工具方法-------------------------------
    //----------------------------工具方法-------------------------------


    /**
     * @param diff
     * @param resultPath:
     * @throws
     * @Title: outputResult
     * @Description: 控制台打印结算的结果
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
        System.out.println("WorkFlowBasedWithoutInsert:");
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
    }

    /**
     * @param
     * @return void
     * @throws
     * @Title: storeresultShow
     * @Description: 存储调度成功的作业结果用于展示
     */
    public static void storeResultShow() {
        int dagcount = 0;
        for (DAG dagmap : DAGMapList) {

            if (dagmap.fillbackdone) {//只有调度成功的任务才会被展示
                //任务存储对象
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
     * @param SlotListInPestemp：
     * @param TASKListInPestemp  :
     * @throws
     * @Title: restoreSlotandTASK
     * @Description: 恢复本轮调度之前处理器上的task、slot环境
     */
    public static void restoreSlotandTASK(HashMap<Integer, ArrayList> SlotListInPestemp, HashMap<Integer, HashMap> TASKListInPestemp) {

        SlotListInPes.clear();
        TASKListInPes.clear();

        for (int k = 0; k < SlotListInPestemp.size(); k++) {
            ArrayList<Slot> slotListinpe = new ArrayList<Slot>();
            for (int j = 0; j < SlotListInPestemp.get(k).size(); j++) {
                Slot slottemp = (Slot) SlotListInPestemp.get(k).get(j);
                slotListinpe.add(slottemp);
            }
            SlotListInPes.put(k, slotListinpe);
        }
        for (int k = 0; k < TASKListInPestemp.size(); k++) {
            HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
            for (int j = 0; j < TASKListInPestemp.get(k).size(); j++) {
                Integer[] temp = (Integer[]) TASKListInPestemp.get(k).get(j);
                TASKInPe.put(j, temp);
            }
            TASKListInPes.put(k, TASKInPe);
        }
    }


    /**
     * @throws
     * @Title: copySlot
     * @Description: 将本轮调度之前的处理器上空隙状态（即每个处理器上空隙情况）保存下来，以便恢复
     * @return:
     */
    public static HashMap copySlot() {
        //将本轮调度之前的处理器上空隙状态（即每个处理器上空隙情况）保存下来，以便恢复
        HashMap<Integer, ArrayList> SlotListInPestemp = new HashMap<Integer, ArrayList>();
        //复制过程
        for (int k = 0; k < SlotListInPes.size(); k++) {

            ArrayList<Slot> slotListinpe = new ArrayList<Slot>();

            for (int j = 0; j < SlotListInPes.get(k).size(); j++) {
                Slot slottemp = (Slot) SlotListInPes.get(k).get(j);
                slotListinpe.add(slottemp);
            }

            SlotListInPestemp.put(k, slotListinpe);
        }
        //返回
        return SlotListInPestemp;
    }

    /**
     * @throws
     * @Title: copyTASK
     * @Description: 将本轮调度之前的处理器状态（即每个处理器上任务情况）保存下来，以便恢复
     * @return:
     */
    public static HashMap copyTASK() {
        //将本轮调度之前的处理器状态（即每个处理器上任务情况）保存下来，以便恢复
        HashMap<Integer, HashMap> TASKListInPestemp = new HashMap<Integer, HashMap>();
        //复制过程
        for (int k = 0; k < TASKListInPes.size(); k++) {
            HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
            for (int j = 0; j < TASKListInPes.get(k).size(); j++) {
                Integer[] temp =(Integer[]) TASKListInPes.get(k).get(j);
                TASKInPe.put(j, temp);
            }
            TASKListInPestemp.put(k, TASKInPe);
        }

        return TASKListInPestemp;
    }


    /**
     * @param DAGId
     * @param dagId
     * @throws
     * @Title: getDAGById
     * @Description: 根据作业id和任务id获取任务
     * @return:
     */
    private static Task getTaskByDagIdAndTaskId(int DAGId, int dagId) {
        for (int i = 0; i < DAGMapList.get(DAGId).gettasknumber(); i++) {
            Task temp = (Task) DAGMapList.get(DAGId).gettasklist().get(i);
            if (temp.getid() == dagId)
                return temp;
        }
        return null;
    }


    /**
     * @throws
     * @Title: getDAGById_task
     * @Description: 根据任务id获取任务
     * @return:
     */
    private static Task getTaskByTaskId(int taskId) {
        for (Task task : TASK_queue_personal) {
            if (task.getid() == taskId)
                return task;
        }
        return null;
    }


    /**
     * 根据Dag的id获取作业对象
     *
     * @param DAGId
     * @return
     */
    private static DAG getDagByDagId(int DAGId) {
        DAG dag = DAGMapList.get(DAGId);
        return dag;
    }


    /**
     * @param @throws Throwable
     * @return void
     * @throws
     * @Title: runMakespan
     * @Description:
     */
    public void runMakespan(String pathXML, String resultPath) throws Throwable {

        // 初始化作业映射
        WorkflowBased fb = new WorkflowBased();
        DAGDepend dagdepend = new DAGDepend();
        PEComputerability vcc = new PEComputerability();

        //初始化处理器
        initPE();

        initDagMap(dagdepend, vcc, pathXML);

        Date begin = new Date();
        Long beginTime = begin.getTime();
        // 调度第一个作业，第一个
        scheduleFirstDAG();

        // set current time
        currentTime = DAGMapList.get(0).getsubmittime();

        //开始其它作业的调度
        for (int i = 1; i < DAGMapList.size(); i++) {
            HashMap<Integer, ArrayList> SlotListInPestemp = new HashMap<Integer, ArrayList>();
            HashMap<Integer, HashMap> TASKListInPestemp = new HashMap<Integer, HashMap>();
            //计算当前的空隙情况
            computeSlot(DAGMapList.get(i).getsubmittime(), DAGMapList.get(i).getDAGdeadline());
            //备份处理器状态
            SlotListInPestemp = copySlot();
            TASKListInPestemp = copyTASK();
            //开始其它作业调度
            scheduleOtherDAG(i, SlotListInPestemp, TASKListInPestemp);
        }

        Date end = new Date();
        Long endTime = end.getTime();
        Long diff = endTime - beginTime;
        //控制台输出结果
        outputResult(diff, resultPath);
        storeResultShow();
    }

}
