package org.generate;


import org.generate.model.DagEdge;
import org.generate.model.Processor;
import org.generate.model.RandomDag;
import org.generate.model.TaskNode;
import org.generate.util.CommonParametersUtil;
import org.generate.util.FileOutputUtil;
import org.generate.util.RandomParametersUtil;
import org.generate.util.XMLOutputUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
* @ClassName: DagBuilder
* @Description: generate dag flow
* @author YanWenjing
* @date 2018-1-15 ����1:38:59
 */
public class DagFlowGenerater {

	private int endNodeNumber = 0;

	public static int endTime = 100 * 10000;
	
	public static RandomParametersUtil randomCreater;

	public List<TaskNode> unCompleteTaskList;

	public List<RandomDag> dagList;

	public static List<RandomDag> finishDagList;

	public List<String> endNodeList;

	public void DAGFlowGenerate() {
		DagFlowGenerater dagBuilder = new DagFlowGenerater();
		dagBuilder.initDags();

	}

	/**
	 * 
	 * @Title: createProcessor
	 * @Description: initial every processor
	 * @return List<Processor>
	 * @throws
	 */
	public List<Processor> createProcessor(int number, int endTime) {
		List<Processor> processorList = new ArrayList<Processor>();
		for (int i = 1; i <= number; i++) {
			Processor processor = new Processor(i, endTime);
			processorList.add(processor);
		}
		return processorList;
	}
	
	/**
	 * 
	 * @Title: initList
	 * @Description: 
	 * @return void
	 * @throws
	 */
	public void initList(List<Processor> processorList) {
		int maxsize = 0;
		// 得到各处理器中最大的任务数 
		for (Processor processor : processorList) {
			int size = processor.nodeList.size();
			if (size > maxsize)
				maxsize = size;
		}

		//将所有处理器上的任务添加到unCompleteTaskList
		for (int i = 0; i < maxsize; i++)
			for (Processor processor : processorList) {
				if (i < processor.nodeList.size())
					unCompleteTaskList.add(processor.nodeList.get(i));
			}
		//将所有处理器上的最后一个任务添加到endNodeList
		for (Processor processor : processorList) {
			String endNodeId = processor.nodeList.get(processor.nodeList.size() - 1).nodeId;
			endNodeList.add(endNodeId);
		}
		
	}


	/**
	 * 
	 * @Title: isEndNode  
	 * @Description: 判断一个任务是否为某个处理器上的最后一个任务
	 * @param taskId
	 * @return
	 * @return boolean
	 */
	public boolean isEndNode(String taskId) {
		for (String endNodeId : endNodeList)
			if (taskId.equals(endNodeId))
				return true;
		return false;
	}

	/**
	 * 
	 * @Title: tryFinishDag
	 * @Description: Determine whether a node can final a DAG 
	 * @return boolean
	 * @throws
	 */
	public boolean tryFinishDag(TaskNode taskNode, RandomDag dag) {
		int m = 0;
		for (TaskNode leafTask : dag.leafNodeList)
			if (taskNode.startTime >= leafTask.endTime) {
				if (taskNode.startTime == leafTask.endTime)
				{
					if (taskNode.getProcessorId() != leafTask.getProcessorId())
						continue;
				}
				m++;
			}
		
		if (m == dag.leafNodeList.size())
		{
			dag.generateNode(taskNode);
			for (TaskNode leafTask : dag.leafNodeList)
				dag.generateEdge(leafTask, taskNode);
			dagList.remove(dag);
			finishDagList.add(dag);
			return true;
		}
		return false;
	}
	
	public void searchParentNode(List<TaskNode> unCompleteTaskList,List<RandomDag> dagList) {
		for (int i = 0; i < unCompleteTaskList.size(); i++) {
			TaskNode taskNode = unCompleteTaskList.get(i);

			Collections.shuffle(dagList);

			boolean match = false;

			for (int n = 0; n < dagList.size(); n++) 
			{
				RandomDag dag = dagList.get(n);
				if (dag.levelCount == dag.dagLevel + 1)
					match = tryFinishDag(taskNode, dag);
				if (match)
					break;
			}

			if (match)
				continue;

			if (isEndNode(taskNode.nodeId))
			{
				for (int k = 0; k < dagList.size(); k++) {
					RandomDag dag = dagList.get(k);
					match = tryFinishDag(taskNode, dag);
					if (match)
						break;
				}
			}
			
			if (match)
				continue;

			for (int k = 0; k < dagList.size(); k++) {
				RandomDag dag = dagList.get(k);
				if (dag.levelCount == dag.dagLevel + 1)
					continue;
				
				boolean matchFlag = false;
				int edgeNum = 0;

				for (int j = 0; j < dag.lastLevelList.size(); j++) {
					TaskNode leafNode = dag.lastLevelList.get(j);
					if (taskNode.startTime >= leafNode.endTime)
					{
						if (taskNode.startTime == leafNode.endTime)
						{
							if (taskNode.getProcessorId() != leafNode.getProcessorId()&& leafNode.getProcessorId() != 0)
								continue;
						}
						
						edgeNum++;
						matchFlag = true;
						match = true;
						
						dag.leafNodeList.remove(leafNode);
						
						if (!dag.containTaskNode(taskNode))
						{
							dag.addToNewLevel(taskNode);
							dag.generateNode(taskNode);
							dag.leafNodeList.add(taskNode);
						}
						
						if (edgeNum > 1)
						{
							if (Math.random() > 0.5)
								continue;
						}
						dag.generateEdge(leafNode, taskNode);
					}
				}
				if (matchFlag)
					break;
			}
			
			if (!match)
			{
				
				int foreDagTime;
				
				if (dagList.size() > 0)
					foreDagTime = dagList.get(dagList.size() - 1).submitTime;
				else
					foreDagTime = finishDagList.get(finishDagList.size() - 1).submitTime;
				RandomDag dag = new RandomDag(dagList.size()+ finishDagList.size() + 1, taskNode, foreDagTime);
				dagList.add(dag);
			}
		}
	}

	
	/**
	 * 
	 * @Title: generateDags  
	 * @Description: 生成一个DAG
	 * @param number
	 * @return void
	 */
	public void generateDags(int number) {
		for (int i = 1; i <= number; i++) {
			RandomDag dag = new RandomDag(i);
			dagList.add(dag);
		}
		searchParentNode(unCompleteTaskList, dagList);
	}

	/**
	 * 
	 * @Title: fillDags
	 * @Description: add final node for every DAG
	 * @return void
	 * @throws
	 */
	public void fillDags() {
		for (int k = 0; k < dagList.size(); k++) {
			RandomDag dag = dagList.get(k);
			TaskNode footNode = new TaskNode("foot_" + (k + 1), 0, endTime,endTime);
			dag.generateNode(footNode);
			endNodeNumber++;
			for (TaskNode leafTask : dag.leafNodeList)
				dag.generateEdge(leafTask, footNode);
			finishDagList.add(dag);
		}
	}

	/**
	 * 
	 * @Title: finishDags
	 * @Description: generate deadline for every DAG
	 * @return void
	 * @throws
	 */
	public void finishDags() {
		for (RandomDag dag : finishDagList) {
			dag.computeDeadLine();
		}
	}

	/**
	 * 
	 * @Title: checkDags
	 * @Description: check result
	 * @return void
	 * @throws
	 */
	public void checkDags()
	{
		int nodeSum = 0;
		for (RandomDag dag : finishDagList) {
			nodeSum += dag.taskList.size();
			for (TaskNode node : dag.taskList)
				if (!unCompleteTaskList.contains(node)) {
					//System.err.print("�������ڵ㣺" + node.nodeId + " ");
				}
			for (DagEdge edge : dag.edgeList)
				if (edge.tail.startTime < edge.head.endTime)
					System.err.print("dege error��" + edge.head + "����>" + edge.tail+ "��");
		}

		System.err.println();

		int number = 0;	
		for (TaskNode taskNode : unCompleteTaskList) {

			boolean containflag = false;
			for (RandomDag dag : finishDagList)
				if (dag.taskList.contains(taskNode)) {
					containflag = true;
					break;
				}
			if (!containflag) {
				System.err.print(taskNode.nodeId + ":" + taskNode.startTime+ " ");
				number++;
			}
		}

		if (nodeSum == unCompleteTaskList.size() + 1 + endNodeNumber)
			System.out.println("Success");
		else
			System.out.println("check dags and fix bugs");

	}

	
	/**
	 * 
	 * @Title: dagsOutput  
	 * @Description: 将生成的DAG结果输出至txt、xml
	 * @return void
	 */
	public void dagsOutput() {

		FileOutputUtil fileDag = new FileOutputUtil();
		XMLOutputUtil xmldag = new XMLOutputUtil();

		fileDag.clearDir();
		xmldag.clearDir();

		try {
			String basePath = System.getProperty("user.dir") + "\\DAG_XML\\";
            System.out.println("............"+basePath);
			String filePathxml = basePath + "Deadline.txt";

			PrintStream out = System.out;
			PrintStream ps = new PrintStream(new FileOutputStream(filePathxml));
			System.setOut(ps); 
			for (int i = 1; i <= finishDagList.size(); i++) {
				for (RandomDag dag : finishDagList) {
					String[] number = dag.dagId.split("dag");
					if (i == Integer.valueOf(number[1]).intValue()) {
						System.out.println(dag.dagId + " "
								+ dag.taskList.size() + " " + dag.submitTime
								+ " " + dag.deadlineTime);
						break;
					}
				}
			}
			ps.close();
			System.setOut(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (RandomDag dag : finishDagList) {
			fileDag.writeDatatoTxt(dag);
			xmldag.writeDataToXML(dag);
		}
	}

	/**
	 * 
	 * @Title: initDags
	 * @Description: initial DAG basic information
	 * @return void
	 * @throws
	 */
	public void initDags() {
		unCompleteTaskList = new ArrayList<TaskNode>();
		dagList = new ArrayList<RandomDag>();
		finishDagList = new ArrayList<RandomDag>();
		endNodeList = new ArrayList<String>();
		randomCreater = new RandomParametersUtil();

		// 创建处理器
		List<Processor> processorList = createProcessor(
				CommonParametersUtil.processorNumber, CommonParametersUtil.timeWindow/ CommonParametersUtil.processorNumber);

		initList(processorList);

		generateDags(1);

		fillDags();

		finishDags();

		checkDags();

		dagsOutput();
	}	
}
