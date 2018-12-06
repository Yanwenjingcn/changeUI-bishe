package org.temp;

import org.generate.DagFlowGenerater;

import java.io.*;
import java.util.HashMap;

public class TempMain {

    private static int count = 10;
    private static HashMap<String,String> resultMap=new HashMap<String, String>();


    public TempMain(int roundTime){
        count=roundTime;
    }

    public static void main(String[] args) throws Throwable {


        String schedulePath = System.getProperty("user.dir") + "\\DAG_XML\\";
        int count = 10;

//        WorkflowBased workflowBased = new WorkflowBased();

//        workflowBased.runMakespan(schedulePath,
//                "E:\\DagCasesResult\\");
//
//

        for (int i = 0; i < count; i++) {

            DagFlowGenerater dagBuilder = new DagFlowGenerater();
            dagBuilder.initDags();

            //就绪队列挑选顺序：
            WorkflowBasedEdition1 edition1 = new WorkflowBasedEdition1();
            edition1.runMakespan(schedulePath, "E:\\DagCasesResult\\");


            //就绪队列挑选顺序：随机
            WorkflowBasedEdition2 edition2 = new WorkflowBasedEdition2();
            edition2.runMakespan(schedulePath, "E:\\DagCasesResult\\");

            //优先级参考：任务数
            WorkflowBasedEdition3 edition3 = new WorkflowBasedEdition3();
            edition3.runMakespan(schedulePath, "E:\\DagCasesResult\\");

            // 优先级参考：deadline
            WorkflowBasedEdition4 edition4 = new WorkflowBasedEdition4();
            edition4.runMakespan(schedulePath, "E:\\DagCasesResult\\");


            // 优先级参考：随机优先级
            WorkflowBasedEdition5 edition5 = new WorkflowBasedEdition5();
            edition5.runMakespan(schedulePath, "E:\\DagCasesResult\\");

            // 优先级参考：deadline/任务数
            WorkflowBasedEdition6 edition6 = new WorkflowBasedEdition6();
            edition6.runMakespan(schedulePath, "E:\\DagCasesResult\\");

        }

        String path1 = "D:\\edition1.txt";
        calcute(path1, count);

        String path2 = "D:\\edition2.txt";
        calcute(path2, count);

        String path3 = "D:\\edition3.txt";
        calcute(path3,count);

        String path4 = "D:\\edition4.txt";
        calcute(path4,count);

        String path5 = "D:\\edition5.txt";
        calcute(path5, count);

        String path6 = "D:\\edition6.txt";
        calcute(path6,count);
    }


    private static void calcute(String path, int count) throws FileNotFoundException {


        File file = new File(path);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            double PE = 0;
            double Completion = 0;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                String[] split = tempString.split("\t");
                PE += Double.valueOf(split[0]);
                Completion += Double.valueOf(split[1]);
            }
            reader.close();

            File f = new File(path);
            FileWriter fw = new FileWriter(f);
            fw.write("");
            fw.close();
            resultMap.put(file.getName(),((double) PE / count) + "," + ((double) Completion / count));

            //System.out.println(path + "-->" + ((double) PE / count) + "\t" + ((double) Completion / count) + "\r\n");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }

    }

    public HashMap getResult(){

        return resultMap;
    }
}
