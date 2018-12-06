package org.schedule.utils;

import org.schedule.model.Task;

import java.util.ArrayList;

public class ReadyQueueUtil {

    /**
     * 目前这里调整与没有调整是没有什么区别的，因为现在每个task的到达时间都是一样的呀！【除非改变成里面每个任务的就绪时间成为它的到达时间】
     * 用到的也只有调度第一个作业的时候
     * @param readyTaskQueue
     */
    public static void changeReadyQueue(ArrayList<Task> readyTaskQueue){
        Task min = new Task();
        Task temp = new Task();


        for (int i = 0; i < readyTaskQueue.size(); i++) {
            int tag = i;
            min = readyTaskQueue.get(i);
            temp = readyTaskQueue.get(i);

            for (int j = i + 1; j < readyTaskQueue.size(); j++) {
                //谁的到达时间早就谁先被调度
                if (readyTaskQueue.get(j).getarrive() < min.getarrive()) {
                    min = readyTaskQueue.get(j);
                    tag = j;
                }
            }
            if (tag != i) {
                readyTaskQueue.set(i, min);
                readyTaskQueue.set(tag, temp);
            }
        }

    }
}
