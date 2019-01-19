package org.generate.util;

import org.generate.model.DagEdge;
import org.generate.model.RandomDag;
import org.generate.model.TaskNode;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @ClassName: FileDag
 * @Description: output result to txt
 * @author YWJ56
 * @date 2017-9-9 ����3:12:
 */

/**
 * 
 * @ClassName: FileOutputUtil  
 * @Description: 调度结果输出
 * @author Wengie Yan 
 * @date 2019年1月18日  
 *
 */
public class FileOutputUtil {
	// file input path
	private String filePath;

	private String basePath = System.getProperty("user.dir") + "\\DAG_TXT\\";
	private File file;
	private FileWriter fileWriter;
	private List<String> nodeIdList;

	/**
	 * 
	 * @Title: clearDir
	 * @Description: clear input path
	 * @throws
	 */
	
	/**
	 * 
	 * @Title: clearDir  
	 * @Description: 清空输出路径下的所有文件
	 * @return void
	 */
	public void clearDir() {
		file = new File(basePath);
		if (!file.exists()){
			file.mkdir();
		}
		String[] fileNames = file.list();
		if (fileNames != null) {
			File tmp;
			for (int i = 0; i < fileNames.length; i++) {
				tmp = new File(basePath + fileNames[i]);
				tmp.delete();
			}
		}
	}

	
	/**
	 * 
	 * @Title: writeDatatoTxt  
	 * @Description: 将结果写出至txt文档
	 * @param dag
	 * @return void
	 */
	public void writeDatatoTxt(RandomDag dag) {
		try {

			filePath = basePath + dag.dagId + ".txt";

			nodeIdList = new ArrayList<String>();
			file = new File(filePath);
			fileWriter = new FileWriter(file, true);

			fileWriter.write(dag.taskList.size() + " " + dag.submitTime + " "
					+ dag.deadlineTime);
			fileWriter.write("\r\n");
			for (TaskNode node : dag.taskList) {
				nodeIdList.add(node.nodeId);
			}
			for (DagEdge dagEdge : dag.edgeList) {
				fileWriter.append(nodeIdList.indexOf(dagEdge.head.nodeId) + " "
						+ nodeIdList.indexOf(dagEdge.tail.nodeId) + " "
						+ dagEdge.transferData);
				fileWriter.append("\r\n");
			}

			fileWriter.flush();
			fileWriter.close();

			String path = "DAG_TXT/" + dag.dagId + "_.txt";
			PrintStream out = System.out;
			PrintStream ps = new PrintStream(new FileOutputStream(path));
			// redirect ouput path
			System.setOut(ps);
			int num = 0;

			for (TaskNode node : dag.taskList) {
				System.out.println(num + " " + (node.taskLength));
				num++;
			}

			ps.close();
			System.setOut(out);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
