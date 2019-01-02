package bishe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.generate.DagFlowGenerater;
import org.generate.util.CommonParametersUtil;
import org.schedule.model.DAG;
import org.schedule.model.DAGDepend;
import org.schedule.model.PE;
import org.schedule.model.PEComputerability;
import org.schedule.model.Slot;
import org.schedule.model.Task;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

/**
 * <p>
 * 修改内容：
 * 1、就绪队列调度方式：在调度就绪队列时，选择 开始时间最早的 任务作为调度对象(如果有相同则不是随机选取而是选的最后一个)
 * 2、deadline分层方式：从后往前推的
 * 3、添加字段：为任务添加 propertity 字段，任务调度时，同时开始的任务，换照优先级进行排序调度。
 * 4、作业顺序：gap=50，任务数少的优先
 */
public class CombinationOfDAG {

    // 统计数据存储
    public static String[][] rateResult = new String[1][4];
    // 比例统计数据存储
    public static String[][] rate = new String[5][2];
    // 当前时刻
    public static int currentTime;
    //总的处理器的截止时间（8个处理器截止时间的总和）
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
    //用于计算deadline还是有用的
    private static ArrayList<Task> TASK_queue_personal;
    private static HashMap<Integer, Integer> DAGDependMap_personal;
    private static HashMap<String, Double> DAGDependValueMap_personal;
    private static Map<Integer, int[]> ComputeCostMap;
    private static Map<Integer, Integer> AveComputeCostMap;

    // 处理器个数
    private static int peNumber = 8;
    //所有处理器上空隙列表
    private static HashMap<Integer, ArrayList> SlotListInPes;

    //所有处理器上任务数
    private static HashMap<Integer, HashMap> TASKListInPes;

    private static int[] pushFlag;

    private static int taskTotal = 0;

    private static int[][] dagResultMap = null;
    
    public static int finishTaskCount=0;
    
    public static ArrayList<Integer> mergeDAGEndNode=new ArrayList<Integer>();
    //合并后的总长度
    	//static List<Integer> mergeId;//被合并的作业的原始id集合
    	//static ArrayList<Integer> mergeDAGEndNode;//合并的大作业中每个小作业的最后一个任务的集合
    	static ArrayList<Integer> successMergeJob=new ArrayList<Integer>();//合并的大作业中，成功调度的原始作业id



    //初始化
    public CombinationOfDAG() {
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

    
public static void main(String[] args) throws Throwable {
    	
    	// 初始化作业映射
    	DagFlowGenerater dagBuilder = new DagFlowGenerater();
        dagBuilder.initDags();
        
        String schedulePath = System.getProperty("user.dir") + "\\DAG_XML\\";
        
        CombinationOfDAG fb = new CombinationOfDAG();
        DAGDepend dagdepend = new DAGDepend();
        PEComputerability vcc = new PEComputerability();

        //初始化处理器
        initPE();

        initDagMap(dagdepend, vcc, schedulePath);
      
        Date begin = new Date();
        Long beginTime = begin.getTime();

        // 设置当前时间是第一个DAG 的到达时间
        currentTime = DAGMapList.get(0).getsubmittime();

        //开始其它作业的调度
        for (int i = 0; i < DAGMapList.size(); i++) {
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
     //   Long endTime = end.getTime();
        Long diff = (end.getTime() - begin.getTime())/1000;
        //控制台输出结果
        outputResult(diff, "");
        storeResultShow();
	}


    /**
     * 一、对DAG的调度顺序进行修改
     *
     * 改为将时间分段，在同一时间段提交的任务按照 |优先级| 任务数| 时间段 等进行排序
     * 备注：一般来说没有几个作业是在统一时间提交的。
     *
     * 当前参数： 任务数
     * @throws IOException 
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     */
    private static void changeInitDAGMapOrder() throws IllegalAccessException, InvocationTargetException, IOException {
    	  int gap = 200;

          //1.按照时间间隔将任务分段
          HashMap<Integer, ArrayList<DAG>> Gapmap = new HashMap<Integer, ArrayList<DAG>>();

          for (int i = 0; i < DAGMapList.size(); i++) {
              DAG dagTemp = DAGMapList.get(i);
              int submit = dagTemp.getsubmittime();
              int gapIndex = submit / gap;
              if (!Gapmap.containsKey(gapIndex)) {
                  ArrayList<DAG> temp = new ArrayList<DAG>();
                  temp.add(dagTemp);
                  Gapmap.put(gapIndex, temp);
              } else {
                  Gapmap.get(gapIndex).add(dagTemp);
              }
          }

          System.out.println("刚生成时DAGMapList的大小："+DAGMapList.size());
          
          //2、清空列表
          DAGMapList.clear();
         


          int merDagCount=0;
          //3、将每段的的DAG进行合并
          for (int i = 0; i < timeWindow / gap; i++) {
              if (!Gapmap.containsKey(i)) {
                  continue;
              }

              ArrayList<DAG> tempDAGList = Gapmap.get(i);
              //System.out.println("当前间隙:"+i*gap+"\t任务数:"+tempDAGList.size());
              //合并这些DAG
              mergeDAG(tempDAGList,i*gap,merDagCount);
              merDagCount++;
          }
          
    }
    
    private static void mergeDAG(ArrayList<DAG> mergeDAGList,int headNodeTime,int mergeDAGID) throws IllegalAccessException, InvocationTargetException, IOException {
		//获取待合并的作业ID
    	ArrayList<Integer> mergeIdList=new ArrayList<>();
    	//合并后这个大的DAG中共有多少个task
		int tailTaskId=0;
		
		for(DAG dag:mergeDAGList){
				mergeIdList.add(dag.getDAGId());
				dag.setMerge(true);
				tailTaskId=tailTaskId+dag.gettasknumber();
				//System.out.println("====>待合并的任务"+tempDag.getdagid());
			
		}	
	
		
		ArrayList<Integer> mergeDAGEndNode=new ArrayList<>();
		
		/**
		 * 不需要合并
		 */
//		if(mergeIdList.size()<=1){
//			mergeIdList.clear();
//			for(int i=0;i<tempDAGMapList.size();i++){
//				DAGMap t=new DAGMap();
//				t=tempDAGMapList.get(i);
//				DAGMapList.add(t);
//			}
//			DAGMapList.get(0).setMerge(false);
//			for(int i=0;i<DAGMapList.size();i++){
//				DAGMap t=new DAGMap();
//				t=DAGMapList.get(i);
//				ArrayList<DAG> taskList=t.gettasklist();
//				for(int k=0;k<t.gettasknumber();k++){
//					DAG tempDag=taskList.get(k);
//					int oriDagId=tempDag.getDAGId();
//					int oriTaskId=tempDag.getid();
//					tempDag.setOriDagId(oriDagId);
//					tempDag.setOriID(oriTaskId);
//					tempDag.setdagid(i);
//				}
//				t.setDAGId(i);
//			}
//			return ;
//		}
		
		
		DAG merDAG=new DAG();
		//合并的dag的合并后id
		merDAG.setDAGId(mergeDAGID);
		//设置此dag是否是合并过
		merDAG.setMerge(true);
		//设置这个大的dag的提交时间，就是为分段的起始时间
		merDAG.setsubmittime(headNodeTime);
		
		ComputeCostMap = new HashMap<Integer, int[]>();
		AveComputeCostMap = new HashMap<Integer, Integer>();
		DAGDependMap_personal = new HashMap<Integer, Integer>();
		DAGDependValueMap_personal = new HashMap<String, Double>();
		
		ArrayList<Task> merDAGTaskList=new ArrayList<>();
		
		int currentTaskId=0;
		//创建头结点
		Task newHeadTask = new Task();
	//	newHeadTask.setOriDagId(0);

		newHeadTask.setdagid(mergeDAGID);
		/**
		 * 后续合并的添加的头结点应该是属于哪个dag的呢？
		 */
		newHeadTask.setOriDAGID(0);
		newHeadTask.setOriTaskId(0);

		newHeadTask.setlength(0);
		newHeadTask.setts(0);
		newHeadTask.setid(currentTaskId);//设置头结点的编号（应该是为0的）
		if(newHeadTask.getid()!=0) {
			System.out.println("error:这里应该是0");
		}
		newHeadTask.setdagid(0);
		newHeadTask.setarrive(0);
		newHeadTask.setdeadline(0);
		//这个是个啥啊？
		//newHeadTask.setSlotDeadLine(0);
		ArrayList<Integer> newHeadParent=new ArrayList<>();
		ArrayList<Integer> newHeadChild=new ArrayList<>();
		//以上是新的头结点的设置，还没有结束
		newHeadTask.setpre(newHeadParent);
		merDAGTaskList.add(newHeadTask);//将头结点添加到合并的大dag中
		
		//创建合并dag的尾task
		Task newTailTask = new Task();
		newTailTask.setid(tailTaskId+1);//设置尾task 的编号
		//System.out.println("tailTaskId="+tailTaskId);
		
		ArrayList<Integer> newTailPre=new ArrayList<>();
		ArrayList<Integer> newTailChild=new ArrayList<>();
		
		int merDagDeadline=0;//合并dag的截止时间（为所有参与合并的dag中最长的那一个）
		int merDAGTaskNum=0;
		int flag=1;
		currentTaskId++;
	
		
		for(int i=0;i<mergeDAGList.size();i++) {
			int mergeId=i;
			
			//得到当前需要合并的作业对象
			DAG currentMerDag=mergeDAGList.get(mergeId);
			//合并的dag中tasknumber增加
			int currentDagTaskNumber=currentMerDag.gettasknumber();
			merDAGTaskNum=merDAGTaskNum+currentDagTaskNumber;
			//比较deadline
			if(merDagDeadline<currentMerDag.getDAGdeadline()){
				merDagDeadline=currentMerDag.getDAGdeadline();
			}
			
			ArrayList<Task> taskList=currentMerDag.gettasklist();
			
			for (int j = 0; j < currentDagTaskNumber; j++) {
				//新的task
				Task merTask = new Task();
				//原作业所对应的任务
				Task oriTask = taskList.get(j);	
				
				Task dag_persional = new Task();
				
				BeanUtils.copyProperties(merTask,oriTask);
				BeanUtils.copyProperties(dag_persional,oriTask);
				
				
				merTask.setOriDAGID(oriTask.getdagid());//存储原始所属dag编号
				merTask.setOriTaskId(oriTask.getid());//存储原始task编号
				
				merTask.setdagid(mergeDAGID);//设置所属的（合并）dag编号
				merTask.setid(currentTaskId);//设置在合并dag中的task编号
				currentTaskId++;
				
				dag_persional.setid(Integer.valueOf(j).intValue());

				//计算在处理器上的开销，其实可以不用管，本身也用不到？
				int x=merTask.getlength();
				int sum = 0;
				int[] bufferedDouble = new int[PEList.size()];
				for (int k = 0; k < PEList.size(); k++) { // x：任务的长度
					bufferedDouble[k] = Integer.valueOf(x/ PEList.get(k).getability());
					sum = sum + Integer.valueOf(x / PEList.get(k).getability());
				}
				ComputeCostMap.put(j, bufferedDouble); // 当前任务在每个处理器上的处理开销
				AveComputeCostMap.put(j, (sum / PEList.size())); // 当前任务在所有处理器上的平均处理开销
				
				//复制父节点(偏移量+原编号)
				ArrayList<Integer> parent=oriTask.getpre();
				ArrayList<Integer> newParent=new ArrayList<>();
				for(Integer per:parent){
					newParent.add(per+flag);
				}
				//复制子节点
				ArrayList<Integer> newChild=new ArrayList<>();
				ArrayList<Integer> child=oriTask.getsuc();
				for(Integer chi:child){
					newChild.add(chi+flag);
				}	
				
				//如果当前task为原dag的头结点，那么它的父节点中应该加入我们创建的新头结点
				if(j==0){
					newHeadChild.add(merTask.getid());
					newParent.add(0);//为原本开始的任务指定父节点为新加入的
					
					DAGDependMap_personal.put(0, merTask.getid());
					//在传输数据中加入
					int from=0;
					int to=merTask.getid();
					String key=from+" "+to;
					DAGDependValueMap_personal.put(key, (double) 0);
					//System.out.println("加入新的头结点的链接信息："+key);
				}
				//如果当前task为原dag的尾结点，那么它的子节点中应该加入我们创建的新尾结点
				if(j==currentDagTaskNumber-1){
					mergeDAGEndNode.add(merTask.getid());
					newChild.add(newTailTask.getid());
					newTailPre.add(merTask.getid());
					DAGDependMap_personal.put(merTask.getid(), newTailTask.getid());
					int from=merTask.getid();
					int to=newTailTask.getid();
				
					String key=from+" "+to;
					DAGDependValueMap_personal.put(key, (double) 0);
					
				}
				/**改变父子列表内容
				 * 
				 */
				merTask.replacePre(newParent);
				merTask.replaceChild(newChild);
				merDAGTaskList.add(merTask); // 当前DAG（一个）的自有任务列表

			}

			
			HashMap<Integer, Integer> currentTaskDependMap = new HashMap<Integer, Integer>();
			currentTaskDependMap=currentMerDag.getDAGDependMap();
			HashMap<String, Double> currentTaskDependValueMap = new HashMap<String, Double>();
			currentTaskDependValueMap=currentMerDag.getdependvalue();

			//这个没有用呀，这个里面是有错误的，保存下来的只有最后一条依赖啊
			for(Entry<Integer, Integer> map:currentTaskDependMap.entrySet()){
				int key=map.getKey()+flag;
				int value=map.getValue()+flag;
				DAGDependMap_personal.put(key, value);
			}
			
			//修改依赖数值
			for(Entry<String, Double> mmap:currentTaskDependValueMap.entrySet()){
				String[] key=mmap.getKey().split(" ");
				int newFrom=Integer.valueOf(key[0]).intValue()+flag;
				int newTo=Integer.valueOf(key[1]).intValue()+flag;
				
				String newKey=newFrom+" "+newTo;
				Double value=mmap.getValue();
				DAGDependValueMap_personal.put(newKey, value);
			}
			flag=flag+currentDagTaskNumber;
				
		}
		
		
		newHeadTask.setsuc(newHeadChild);//此时合并dag的头task设置完毕

		//设置尾节点信息
		newTailTask.setdagid(0);
		newTailTask.setOriTaskId(currentTaskId);
		newTailTask.setlength(0);
		newTailTask.setts(0);
		newTailTask.setid(currentTaskId);
		newTailTask.setdagid(0);
		newTailTask.setarrive(0);
		newTailTask.setdeadline(merDagDeadline);
		/**
		 * 这是个啥啊
		 */
		//newTailTask.setSlotDeadLine(merDagDeadline);
		newTailTask.setpre(newTailPre);
		newTailTask.setsuc(newTailChild);
		merDAGTaskList.add(newTailTask);

		
		merDAG.settasklist(merDAGTaskList);
		merDAG.settasknumber(merDAGTaskList.size());
		merDAG.setDAGdeadline(merDagDeadline);
		merDAG.setDAGDependMap(DAGDependMap_personal);//没用呀
		merDAG.setdependvalue(DAGDependValueMap_personal);	
		DAGMapList.add(merDAG);


		/**
		 * 
		 * 
		 * 
		 */
//		FileWriter writer = new FileWriter("G:\\initTaskList.txt", true);	
//		for(DAG mdag:merDAGTaskList){
//			StringBuffer spre=new StringBuffer();
//			for(Integer pre:mdag.getpre()){
//				spre.append(pre).append(";");
//			}
//			StringBuffer schi=new StringBuffer();
//			for(Integer chi:mdag.getsuc()){
//				schi.append(chi).append(";");
//			}
//			writer.write(""+mdag.getDAGId()+":"+mdag.getid()+"\t原始信息："+mdag.getOriDagId()+":"+mdag.getid()+
//					"\t父节点有："+spre.toString()+"\t子节点有："+schi.toString()+"\n");
//		}
//		if (writer != null) {
//			writer.close();
//		}
	}


    /**
     * 二、task就绪队列调度顺序
     *
     * 备注：是先将整个就绪队列中的task都算出能插入的位置后再考虑根据 策略 选择到底调度哪一个task
     *
     * 【如果要修改就绪队列里面任务的调度顺序，修改这里】，比如:
     * 1、最早开始执行的任务优先调度（********，如果有多个时间相同的则随机选择一个）
     * 2、优先级高的先调度
     * 3、最早结束的优先调度
     *
     * 当前参数：最早开始时间
     *
     * @param message
     * @param readylist
     * @return 在就绪队列中的下标
     */
    private static int chooseReadyListIndex(int[][] message, ArrayList<Task> readylist) {
        int readyListIndex = -1;

        int earliestStart_Time = timewindowmax;

        for (int i = 0; i < readylist.size(); i++) {
            if (earliestStart_Time > message[i][1]) {
                earliestStart_Time = message[i][1];
                readyListIndex = i;
            }
        }
        return readyListIndex;
    }


    /**
     * 三、每一个task按照比例计算deadline
     * <p>
     * 注意：1、这个deadline是此任务计算中最晚的截止时间，其中是没有考虑传输时延的，因为你也不知道会在哪个处理器上执行
     *
     * @param dead_line
     * @throws Throwable:
     * @throws
     * @Title: createDeadline_XML
     * @Description:
     */
    private static void createDeadline_XML(int dead_line) throws Throwable {
        //处理器的计算能力
        int maxability = 1;
        int max = 10000;
        for (int k = TASK_queue_personal.size() - 1; k >= 0; k--) {

            ArrayList<Integer> suc = new ArrayList<Integer>();
            //获取该task的子task列表
            suc = TASK_queue_personal.get(k).getsuc();

            if (suc.size() > 0) {
                for (int j = 0; j < suc.size(); j++) {
                    int tem;
                    Task subTask = getTaskByTaskId(suc.get(j));

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
     * 四、设置作业的优先级
     *
     * 设置：此轮优先级用随机数产生
     *
     * @param dagmap
     */
    private static void setDAGProperty(DAG dagmap) {
        //创建优先级
        double random = Math.random();
        if(random<0.2){
            dagmap.setProperty(1);
        }else if(random<0.8){
            dagmap.setProperty(2);
        }else {
            dagmap.setProperty(3);
        }
    }


    //========================================  调度主体  ================================
    //========================================  调度主体  ================================
    //========================================  调度主体  ================================


    /**
     * @param slotlistinpe
     * @param inpe:
     * @throws
     * @Title: changeInPE
     * @Description: 改变在插入任务后，改变每个slot的below，其实用不到，应为总是会重新计算slot的，所以就会更新这个below值
     */
    public static void changeBelowInPE(ArrayList<Slot> slotlistinpe, int inpe) {
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
                    String belowbuff = belowbuf[0] + " " + belowbuf[1] + " " + buffer;
                    belowte.add(belowbuff);
                }
                slottem.getbelow().clear();
                slottem.setbelow(belowte);
            }
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

        do {

            //最早结束时间


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
             * 答：如果在这一秒都不能调度成功，那么下一秒也一定不会成功
             *
             * 2、这个deadline是没有考虑任务传输的呀？
             * 答：是没有，因为你并不知道这个任务最后会被分发到哪个处理器上执行，所以这个deadline肯定是一超过任务必定失败的。
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


            //===========================开始选定需要调度的任务==================================

            //需要调度的任务在就绪队列中的编号
            int target_readyListIndex = (int) chooseReadyListIndex(message, readylist);

            //原任务对象的id
            int originTaskId = readylist.get(target_readyListIndex).getid();
            //得到原任务
            Task targetTask_originTask = new Task();

            //readylist中的不是原任务对象，这里要获取原任务对象正，这样的修改才是确的
            for (int i = 0; i < dagmap.gettasklist().size(); i++) {
                Task tempTask = (Task) dagmap.gettasklist().get(i);
                if (tempTask.getid() == originTaskId)
                    targetTask_originTask = (Task) dagmap.gettasklist().get(i);
            }


            //==============此时需要调度的任务已经选定================


            //选中任务的开始时间
            int target_StartTime = message[target_readyListIndex][1];
            //选中任务所分配的处理器编号
            int target_PEId = message[target_readyListIndex][2];
            //选中任务的所分配的空隙编号（【这个空隙编号不是包含处理器所有原生空隙的编号，而是在任务submit---deadline区间内经过转换后适宜的空隙的编号，从0开始】）
            //但是在全局SlotListInPes中的本身就是适宜时间范围内的slot，所以除了Slot对象的blow字段还能得到slot的原生编号外，其它的都已经是转换后的了。
            int target_slotId = message[target_readyListIndex][3];

            targetTask_originTask.setfillbackstarttime(target_StartTime);
            targetTask_originTask.setfillbackpeid(target_PEId);
            //设置自己的就绪标记，就是自己父任务都调度好了，然后自己也找到了可以放入自己的位置，已经满足调度的需求了
            targetTask_originTask.setfillbackready(true);
            //设置父任务的状态标记
            targetTask_originTask.setprefillbackdone(true);
            targetTask_originTask.setprefillbackpass(false);

            //获取被调度上的处理器上原有的任务列表
            HashMap<Integer, Integer[]> TASKInPe = new HashMap<Integer, Integer[]>();
            TASKInPe = TASKListInPes.get(target_PEId);

            // 0 is if success 1 means success 0 means fail,
            // 1 is earliest starttime
            // 2 is peid
            // 3 is slotid
            // 4 is if need slide
            // 5 is slide length

            if (TASKInPe.size() > 0) {

                ArrayList<Slot> slotlistinpe = SlotListInPes.get(target_PEId);

                //找到目标空隙
                Slot targetSlot = new Slot();
                for (int i = 0; i < slotlistinpe.size(); i++) {
                    if (slotlistinpe.get(i).getslotId() == target_slotId) {
                        targetSlot = slotlistinpe.get(i);
                        break;
                    }
                }

                ArrayList<String> below = targetSlot.getbelow();

                if (below.size() > 0) { //如果这个空隙后面的任务数大于0
                    String buf[] = below.get(0).split(" ");

                    //原生空隙编号,其实也就是对应着接下来要插的这个任务在处理器上的编号（从0开始）
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
                    st_fi[0] = message[target_readyListIndex][1];
                    st_fi[1] = message[target_readyListIndex][1] + targetTask_originTask.getts();
                    st_fi[2] = targetTask_originTask.getdagid();
                    st_fi[3] = targetTask_originTask.getid();
                    TASKInPe.put(originSlotId, st_fi);

                    targetTask_originTask.setisfillback(true);


                } else {
                    Integer[] st_fi = new Integer[4];
                    st_fi[0] = target_StartTime;
                    st_fi[1] = target_StartTime + targetTask_originTask.getts();
                    st_fi[2] = targetTask_originTask.getdagid();
                    st_fi[3] = targetTask_originTask.getid();
                    TASKInPe.put(TASKInPe.size(), st_fi);
                }

            } else {
                Integer[] st_fi = new Integer[4];
                st_fi[0] = target_StartTime;
                st_fi[1] = target_StartTime + targetTask_originTask.getts();
                st_fi[2] = targetTask_originTask.getdagid();
                st_fi[3] = targetTask_originTask.getid();
                TASKInPe.put(TASKInPe.size(), st_fi);
            }

            //重新计算新的空隙
            computeSlot(dagmap.getsubmittime(), dagmap.getDAGdeadline());

            //删除这个已经调度成功的任务
            readylist.remove(target_readyListIndex);

        } while (readylist.size() > 0);

        return findsuc;
    }


    /**
     * @param dagmap
     * @param dagtemp
     * @throws
     * @Title: findSlot
     * @Description: find appropriate block
     * @return:
     */
    public static int[] findSlot(DAG dagmap, Task dagtemp) {
        int message[] = new int[6];

        boolean findsuc = false;
        int startmin = timewindowmax;
        int finishmin = timewindowmax;
        int pemin = -1;
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
                Task buf = getTaskByDagIdAndTaskId(dagtemp.getdagid(), pre.get(j));
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

    public static void scheduleOtherDAG(int i, HashMap<Integer, ArrayList> SlotListInPestemp, HashMap<Integer, HashMap> TASKListInPestemp) throws Exception {

        int arrive = DAGMapList.get(i).getsubmittime();
        if (arrive > currentTime)
            currentTime = arrive;

        DAG currentDAG = DAGMapList.get(i);
        //  System.out.println("当前调度的是："+currentDAG.getDAGId()+" "+currentDAG.gettasknumber());
        //作业是否调度成功标记
        boolean flag = fillBack(currentDAG);

        if (!flag) {//这个合并dag调度失败
				ArrayList<Task> taskList=currentDAG.gettasklist();
				//判断这个合并的作业中里面有哪些初始作业是成功的
				for(Task dag:taskList){
					/**
					 * mergeDAGEndNode、successMergeJob以前只有一个合并的dag，现在是有多个了
					 */
					if(mergeDAGEndNode.contains(dag.getid())){//这个里面存的要想想
						if(dag.getfillbackdone()){
							successMergeJob.add(dag.getOriDAGID());
						}
					}
				}
				System.out.println("原任务中调度成功的作业有"+successMergeJob.size());

				//如果原始作业中没有处理成功的，就按照以前的方式做
				if(successMergeJob.size()<=0){
					//恢复以前的task、slot情况
		            restoreSlotandTASK(SlotListInPestemp, TASKListInPestemp);

		            //将作业标记为未完成与过期
		            currentDAG.setfillbackdone(false);
		            currentDAG.setfillbackpass(true);

		            //将整个作业的任务都设置为过期
		            for (int j = 0; j < currentDAG.gettasklist().size(); j++) {
		                Task task = (Task) currentDAG.gettasklist().get(j);
		                task.setprefillbackdone(false);
		                task.setfillbackpass(true);
		                task.setready(false);
		                task.setprefillbackdone(false);
		                task.setprefillbackpass(true);
		                task.setprefillbackready(false);
		            }
		            
		            System.out.println("失败的dag："+currentDAG.getDAGId());
		            return;
				}else{
					DAGMapList.get(0).setfillbackdone(true);
					//System.out.println("这里会调用到吗？~~~~~~~~~~~~~~~~~~~~~");
					DAGMapList.get(0).setfillbackpass(false);
				}
				
				//得到这些成功作业的任务的现ID集合
				ArrayList<Integer> successTaskId=new ArrayList<Integer>();			
				for(Task suctaskask:taskList){
					int oriDAGTaskId=suctaskask.getOriDAGID();
					for(int p=0;p<successMergeJob.size();p++){
						if(successMergeJob.get(p)==oriDAGTaskId){
							successTaskId.add(suctaskask.getid());
							suctaskask.setfillbackdone(true);
						}else{
							suctaskask.setfillbackdone(false);
						}
					}
				}
			//	System.out.println("此时成功的任务有:"+successTaskId.get(0)+"\t"+successTaskId.get(1));
				//将这些成功的任务放置在处理器上
				// 数组0代表task开始时间，1代表task结束时间，2代表dagid，3代表id
				for(int j=0;j<peNumber;j++){
					HashMap<Integer, Integer[]> taskHashMap=TASKListInPes.get(j);
					HashMap<Integer, Integer[]> insteadTaskHashMap=new HashMap<>();
					int count=0;
					for(int k=0;k<taskHashMap.size();k++){
						Integer[] tempInfo=taskHashMap.get(k);
						if(successTaskId.contains(tempInfo[3])){
							insteadTaskHashMap.put(count, tempInfo);
							count++;
						}
					}
					System.out.println("count="+count);
					TASKListInPes.put(j, insteadTaskHashMap);
				}
        	
            //恢复以前的task、slot情况
//            restoreSlotandTASK(SlotListInPestemp, TASKListInPestemp);
//
//            //将作业标记为未完成与过期
//            currentDAG.setfillbackdone(false);
//            currentDAG.setfillbackpass(true);
//
//            //将整个作业的任务都设置为过期
//            for (int j = 0; j < currentDAG.gettasklist().size(); j++) {
//                Task task = (Task) currentDAG.gettasklist().get(j);
//                task.setprefillbackdone(false);
//                task.setfillbackpass(true);
//                task.setready(false);
//                task.setprefillbackdone(false);
//                task.setprefillbackpass(true);
//                task.setprefillbackready(false);
//            }
//            
//            System.out.println("失败的dag："+currentDAG.getDAGId());
        } else {
            currentDAG.setfillbackdone(true);
            System.out.println(currentDAG.getDAGId());
        }
    }
//    /**
//     * @param i
//     * @param SlotListInPestemp：备份的slot
//     * @param TASKListInPestemp:备份的task
//     * @throws
//     * @Title: scheduleOtherDAG
//     * @Description: schedule other DAG
//     */
//    public static void scheduleOtherDAG(int i, HashMap<Integer, ArrayList> SlotListInPestemp, HashMap<Integer, HashMap> TASKListInPestemp) throws Exception {
//
//        int arrive = DAGMapList.get(i).getsubmittime();
//        if (arrive > currentTime)
//            currentTime = arrive;
//
//        DAG currentDAG = DAGMapList.get(i);
//        //  System.out.println("当前调度的是："+currentDAG.getDAGId()+" "+currentDAG.gettasknumber());
//        //作业是否调度成功标记
//        boolean flag = fillBack(currentDAG);
//
//        if (!flag) {
//
//            //恢复以前的task、slot情况
//            restoreSlotandTASK(SlotListInPestemp, TASKListInPestemp);
//
//            //将作业标记为未完成与过期
//            currentDAG.setfillbackdone(false);
//            currentDAG.setfillbackpass(true);
//
//            //将整个作业的任务都设置为过期
//            for (int j = 0; j < currentDAG.gettasklist().size(); j++) {
//                Task task = (Task) currentDAG.gettasklist().get(j);
//                task.setprefillbackdone(false);
//                task.setfillbackpass(true);
//                task.setready(false);
//                task.setprefillbackdone(false);
//                task.setprefillbackpass(true);
//                task.setprefillbackready(false);
//            }
//            
//            System.out.println("失败的dag："+currentDAG.getDAGId());
//        } else {
//            currentDAG.setfillbackdone(true);
//            System.out.println(currentDAG.getDAGId());
//        }
//    }
//

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
                    Slot slotTemp = new Slot();
                    slotTemp = slotListinpe_ori.get(j);
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

        if (task.getfillbackpass() == false && task.getfillbackdone() == false) {//这个任务没有过期也没有被调度过

            if (time > task.getdeadline()) {//如果已经超过了这个dag的最晚开始时间，则该任务被设置为过期
                task.setfillbackpass(true);
                return false;
            }


            //获得该task的父task列表
            ArrayList<Integer> pre = task.getpre();

            if (pre.size() >= 0) {
                //如果存在父task
                for (int j = 0; j < pre.size(); j++) {
                    Task temp = null;

                    try {
                        temp = getTaskByDagIdAndTaskId(task.getdagid(), pre.get(j));
                    } catch (Exception e) {
                        System.out.println("父任务不存在。。。。" + task.getdagid() + "  父任务编号：" + pre.get(j) + "   自己编号:" + task.getid());
                    }

                    if (temp.getfillbackpass()) {//如果父task过期不就，则绪,自己也将被设置为过期
                        task.setfillbackpass(true);
                        return false;
                    }

                    if (!temp.getfillbackdone()) {//父任务调度失败，则不就绪
                        return false;
                    }
                }
            }
        }
//        }

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
    private static int initDAG_createDAGdepend_XML(int i, int preexist, int tasknumber, int arrivetimes, String pathXML) throws NumberFormatException, IOException, JDOMException {

        int back = 0;
        DAGDependMap_personal = new HashMap<Integer, Integer>();
        DAGDependValueMap_personal = new HashMap<String, Double>();
        ComputeCostMap = new HashMap<Integer, int[]>();
        AveComputeCostMap = new HashMap<Integer, Integer>();

        SAXBuilder builder = new SAXBuilder();

        Document doc = builder.build(pathXML + "/dag" + (i + 1) + ".xml");

        for (int j = 0; j < tasknumber; j++) {
            Task dag = new Task();
            Task dag_persional = new Task();

            dag.setid(Integer.valueOf(preexist + j).intValue());
            dag.setarrive(arrivetimes);
            dag.setdagid(i);
            dag_persional.setid(Integer.valueOf(j).intValue());
            dag_persional.setarrive(arrivetimes);
            dag_persional.setdagid(i);

            XPath path = XPath.newInstance("//job[@id='" + j + "']/@tasklength");
            List list = path.selectNodes(doc);
            Attribute attribute = (Attribute) list.get(0);
            int x = Integer.valueOf(attribute.getValue()).intValue();
            dag.setlength(x);
            dag.setts(x);
            dag_persional.setlength(x);
            dag_persional.setts(x);

            if (j == tasknumber - 1) {
                dag.setislast(true);
            }

            Task_queue.add(dag);
            TASK_queue_personal.add(dag_persional);

            int sum = 0;
            int[] bufferedDouble = new int[PEList.size()];
            for (int k = 0; k < PEList.size(); k++) {
                bufferedDouble[k] = Integer.valueOf(x / PEList.get(k).getability());
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

            XPath path2 = XPath.newInstance("//uses[@file='" + attribute1.getValue() + "']/@size");
            List list2 = path2.selectNodes(doc);
            Attribute attribute2 = (Attribute) list2.get(0);
            int datasize = Integer.valueOf(attribute2.getValue()).intValue();

            DAGDependMap.put(presuc[0], presuc[1]);
            DAGDependValueMap.put((presuc[0] + " " + presuc[1]), (double) datasize);
            Task_queue.get(presuc[0]).addToSuc(presuc[1]);
            Task_queue.get(presuc[1]).addToPre(presuc[0]);

            DAGDependMap_personal.put(Integer.valueOf(pre_suc[0]).intValue(), Integer.valueOf(pre_suc[1]).intValue());
            DAGDependValueMap_personal.put((pre_suc[0] + " " + pre_suc[1]), (double) datasize);

            int tem0 = Integer.parseInt(pre_suc[0]);
            int tem1 = Integer.parseInt(pre_suc[1]);
            TASK_queue_personal.get(tem0).addToSuc(tem1);
            TASK_queue_personal.get(tem1).addToPre(tem0);
        }

        back = preexist + tasknumber;
        return back;
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
    public static void initDagMap(DAGDepend dagdepend, PEComputerability vcc, String pathXML) throws Throwable {
        int pre_exist = 0;

        File file = new File(pathXML);
        String[] fileNames = file.list();
        //得到dag的数量
        int num = fileNames.length - 1;

        BufferedReader bd = new BufferedReader(new FileReader(pathXML + "Deadline.txt"));
        String buffered;

        for (int i = 0; i < num; i++) {

            DAG dagmap = new DAG();
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
            pre_exist = initDAG_createDAGdepend_XML(i, pre_exist, tasknum, arrivetime, pathXML);

            vcc.setComputeCostMap(ComputeCostMap);
            vcc.setAveComputeCostMap(AveComputeCostMap);

            //为作业中每个任务设定其deadline
            createDeadline_XML(deadline);

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

            setDAGProperty(dagmap);

            DAGMapList.add(dagmap);
        }

        //******************************对作业的调度顺序进行修改,修改
        changeInitDAGMapOrder();

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
    public static void outputResult(Long diff, String resultPath) throws IOException {
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
        System.out.println("WorkFlowBasedEdition3:");
        System.out.println("PE's use ratio is "
                + df.format((float) effective / (peNumber * tempp)));
        System.out.println("effective PE's use ratio is "
                + df.format((float) effective / (tempp * peNumber)));
        System.out.println("Task Completion Rates is "
                + df.format((float) suc / DAGMapList.size()));
        System.out.println();

        rateResult[0][0] = df.format((float) effective / (peNumber * tempp));//处理器利用率
        rateResult[0][1] = df.format((float) effective / (tempp * peNumber));//处理器有效利用率
        rateResult[0][2] = df.format((float) suc / DAGMapList.size());//任务完成利率
        rateResult[0][3] = df.format(diff);

        printInfile();

    }

    protected static void printInfile() throws IOException {
        String path = "D:\\semple.txt";
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true)));
            out.write(rateResult[0][0] + "\t" + rateResult[0][1]+"\t" + rateResult[0][2] +"\t"+rateResult[0][3]+"\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * @param
     * @return void
     * @throws
     * @Title: storeresultShow
     * @Description: 存储调度成功的作业结果用于展示
     */
    public static void storeResultShow() {
        int Taskcount = 0;
        for (DAG dagmap : DAGMapList) {

            if (dagmap.fillbackdone) {//只有调度成功的任务才会被展示
                //任务存储对象
                ArrayList<Task> DAGTaskList = new ArrayList<Task>();

                for (int i = 0; i < dagmap.gettasklist().size(); i++) {
                    Task dag = (Task) dagmap.gettasklist().get(i);
                    DAGTaskList.add(dag);
                    message[Taskcount][0] = dag.getdagid();
                    message[Taskcount][1] = dag.getid();
                    message[Taskcount][2] = dag.getfillbackpeid();
                    message[Taskcount][3] = dag.getfillbackstarttime();
                    message[Taskcount][4] = dag.getfillbackfinishtime();
                    
                    /**
                     * message里面都是调度成功的任务才会显示上去
                     * 0：作业id
                     * 1：task编号
                     * 2：处理器编号
                     * 3：开始时间
                     * 4：结束时间
                     */
                    Taskcount++;
                }
            }
        }
        finishTaskCount=Taskcount;
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
                Integer[] temp = new Integer[4];
                temp = (Integer[]) TASKListInPes.get(k).get(j);
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
        DAG targetDAG = null;

        for (int i = 0; i < DAGMapList.size(); i++) {
            if (DAGMapList.get(i).getDAGId() == DAGId) {
                targetDAG = DAGMapList.get(i);
            }
        }

        for (int i = 0; i < targetDAG.gettasknumber(); i++) {

            Task temp = (Task) targetDAG.gettasklist().get(i);
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

        DAG targetDAG = null;

        for (int i = 0; i < DAGMapList.size(); i++) {
            if (DAGMapList.get(i).getDAGId() == DAGId) {
                targetDAG = DAGMapList.get(i);
            }
        }

        return targetDAG;
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
        CombinationOfDAG fb = new CombinationOfDAG();
        DAGDepend dagdepend = new DAGDepend();
        PEComputerability vcc = new PEComputerability();

        //初始化处理器
        initPE();

        initDagMap(dagdepend, vcc, pathXML);

        Date begin = new Date();
        Long beginTime = begin.getTime();

        // 设置当前时间是第一个DAG 的到达时间
        currentTime = DAGMapList.get(0).getsubmittime();

        //开始其它作业的调度
        for (int i = 0; i < DAGMapList.size(); i++) {
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
     //   Long endTime = end.getTime();
        Long diff = (end.getTime() - begin.getTime())/1000;
        //控制台输出结果
        outputResult(diff, resultPath);
        storeResultShow();
    }

}
