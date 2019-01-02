package org.schedule.model;

import java.util.ArrayList;

/**
 * 
* @ClassName: DAG
* @Description: task 
* @author YanWenjing
* @date 2018-1-15 ����2:43:10
 */
public class Task { 

	public int level;

	public int newlevel;

	public int slidelength;

	public int slidedeadline; 

	public int slidefinishdeadline;
	
	public int relax;
	
	public int weight;
	
	public int choosePEpre;

	public int ID;  //原本的task编号
	
	public int OriTaskId;//合并后的task编号

	public int getOriTaskId() {
		return OriTaskId;
	}



	public void setOriTaskId(int oriTaskId) {
		OriTaskId = oriTaskId;
	}

	public int dagID;	//原本所属DAGID
	
	public int OriDAGID;//合并后所属DAGID

	public int getOriDAGID() {
		return OriDAGID;
	}



	public void setOriDAGID(int oriDAGID) {
		OriDAGID = oriDAGID;
	}

	public int arrive_time;    

	public int length;  //长度 

	public int Ts;    //执行时长

	public int finish_time;    

	public int finish_time_suppose;		

	public int start_time;    

	public boolean done = false;   

	public boolean ready = false;	

	public boolean pass = false;	

	private double upRankValue;

	public ArrayList<Integer> pre_task;		//父task列表

	public ArrayList<Integer> suc_task;		//子task列表

	//任务最晚结束时间，再晚就会一定影响后续任务的执行
	public int Deadline;	

	public int NewDeadline;

	public int PEID;	//已调度上的处理器ID

	public boolean islast;     //是否为作业的最后一个task
	
	private boolean inserte;
	
	public double heftast;
	
	public double heftaft;

	public boolean inCriticalPath;    //是否在关键路径上

	public boolean iscriticalnode;     //是否是关键路径上的节点

	public int fillbackstarttime=-1;

	public int fillbackfinishtime;

	public int fillbackpeid;

	public boolean isfillback = false;//是否被调度
	
	public boolean fillbackdone = false;//是否调度完成

	public boolean fillbackready = false;//是否就绪

	public boolean fillbackpass = false;//是否已过期（即调度失败）

	public boolean prefillbackdone = false;  //父task都调度完成

	public boolean prefillbackready = false;//父task都就绪了

	public boolean prefillbackpass = false;//父task中存在调度失败的
	
	public Task(){
		pre_task = new ArrayList<Integer>(); 
		suc_task = new ArrayList<Integer>();
		islast =false;
		inserte = false;
		inCriticalPath = false;
		iscriticalnode = false;
	}
	
	
	
	public void setnewlevel(int level){
		this.newlevel = level;
	}
	
	public int getnewlevel(){
		return newlevel;
	}
	
	public void setlevel(int level){
		this.level = level;
	}
	
	public int getlevel(){
		return level;
	}
	
	public void setslidefinishdeadline(int slidedeadline){
		this.slidefinishdeadline = slidedeadline;
	}
	
	public int getslidefinishdeadline(){
		return slidefinishdeadline;
	}
	
	public void setslidedeadline(int slidedeadline){
		this.slidedeadline = slidedeadline;
	}
	
	public int getslidedeadline(){
		return slidedeadline;
	}
	
	public void setslidelength(int slidelength){
		this.slidelength = slidelength;
	}
	
	public int getslidelength(){
		return slidelength;
	}
	
	public void setrelax(int relax){
		this.relax = relax;
	}
	
	public int getrelax(){
		return relax;
	}
	
	public void setweight(int weight){
		this.weight = weight;
	}
	
	public int getweight(){
		return weight;
	}
	
	public void setchoosePEpre(int start){
		this.choosePEpre = start;
	}
	
	public int getchoosePEpre(){
		return choosePEpre;
	}
	
	public void setfillbackstarttime(int start){
		this.fillbackstarttime = start;
	}
	
	public int getfillbackstarttime(){
		return fillbackstarttime;
	}
	
	public void setfillbackfinishtime(int finish){
		this.fillbackfinishtime = finish;
	}
	
	public int getfillbackfinishtime(){
		return fillbackfinishtime;
	}
	
	public void setfillbackpeid(int peid){
		this.fillbackpeid = peid;
	}
	
	public int getfillbackpeid(){
		return fillbackpeid;
	}
	
	public void setiscriticalnode(boolean temp){
		this.iscriticalnode = temp;
	}
	
	public boolean getiscriticalnode(){
		return iscriticalnode;
	}
	
	public void setinCriticalPath(boolean temp){
		this.inCriticalPath = temp;
	}
	
	public boolean getinCriticalPath(){
		return inCriticalPath;
	}
	
	public ArrayList<Integer> getpre(){
		return pre_task;
	}
	
	public ArrayList<Integer> getsuc(){
		return suc_task;
	}
	
	public void setpre(ArrayList<Integer> pre_){
		for(int i=0;i<pre_.size();i++)
		{
			pre_task.add(pre_.get(i));
		}
	}
	
	public void setsuc(ArrayList<Integer> suc_){
		for(int i=0;i<suc_.size();i++)
		{
			suc_task.add(suc_.get(i));
		}
	}
	
	public void replacePre(ArrayList<Integer> pre) {
		pre_task=pre;
	}
	
	public void replaceChild(ArrayList<Integer> child) {
		suc_task=child;
	}
	
	public void addToPre(int preId){
		this.pre_task.add(preId);
	}

	public void addToSuc(int sucId){
		this.suc_task.add(sucId);
	}

	public void setislast(boolean islast_){
		this.islast = islast_;
	}
	
	public boolean getislast(){
		return islast;
	}
	
	public void setready(boolean ready_){
		this.ready = true;
	}
	
	public boolean getready(){
		return ready;
	}
	
	public void setdone(boolean done_){
		this.done = true;
	}
	
	public boolean getdone(){
		return done;
	}
	
	
	public void setid(int id){
		this.ID = id;
	}
	
	public int getid(){
		return ID;
	}
	
	public void setdagid(int id){
		this.dagID = id;
	}
	
	public int getdagid(){
		return dagID;
	}
	
	public void setarrive(int arrive){
		this.arrive_time = arrive;
	}
		
	public int getarrive(){
		return arrive_time;
	}

	public void setts(int ts){
		this.Ts = ts;
	}
		
	public int getts(){
		return Ts;
	}
	
	public void setfinish(int finish){
		this.finish_time = finish;
	}
		
	public int getfinish(){
		return finish_time;
	}
	
	public void setfinish_suppose(int finish_suppose){
		this.finish_time_suppose = finish_suppose;
	}
		
	public int getfinish_suppose(){
		return finish_time_suppose;
	}
	
	public void setstart(int start){
		this.start_time = start;
	}
		
	public int getstart(){
		return start_time;
	}
	
	public void setlength(int temp){
		this.length = temp;
	}
		
	public int getlength(){
		return length;
	}
	
	public void setdeadline(int deadline){
		this.Deadline = deadline;
	}
		
	public int getdeadline(){
		return Deadline;
	}
	
	public void setnewdeadline(int deadline){
		this.NewDeadline = deadline;
	}
		
	public int getnewdeadline(){
		return NewDeadline;
	}
	
	public void setpeid(int peid){
		this.PEID = peid;
	}
		
	public int getpeid(){
		return PEID;
	}
	
	public void setpass(boolean pass_){
		this.pass = true;
	}
	
	public boolean getpass(){
		return pass;
	}
	
	public void setUpRankValue(double upRankValue){
		this.upRankValue = upRankValue;
	}
	public double getUpRankValue(){
		return upRankValue;
	}
	
	public void setinserte(boolean temp){
		this.inserte = temp;
	}
	
	public boolean getinserte(){
		return inserte;
	}
	
	public void setheftast(double ast){
		this.heftast = ast;
	}
		
	public double getheftast(){
		return heftast;
	}
	
	public void setheftaft(double aft){
		this.heftaft = aft;
	}
		
	public double getheftaft(){
		return heftaft;
	}
	
	public void setfillbackpass(boolean pass_){
		this.fillbackpass = pass_;
	}
	
	public boolean getfillbackpass(){
		return fillbackpass;
	}
	
	public void setfillbackready(boolean ready_){
		this.fillbackready = ready_;
	}
	
	public boolean getfillbackready(){
		return fillbackready;
	}
	
	public void setfillbackdone(boolean done_){
		this.fillbackdone = done_;
	}
	
	public boolean getfillbackdone(){
		return fillbackdone;
	}
	
	public void setprefillbackpass(boolean pass_){
		this.prefillbackpass = pass_;
	}
	
	public boolean getprefillbackpass(){
		return prefillbackpass;
	}
	
	public void setprefillbackready(boolean ready_){
		this.prefillbackready = ready_;
	}
	
	public boolean getprefillbackready(){
		return prefillbackready;
	}
	
	public void setprefillbackdone(boolean done_){
		this.prefillbackdone = done_;
	}
	
	public boolean getprefillbackdone(){
		return prefillbackdone;
	}
	
	public void setisfillback(boolean isfillback){
		this.isfillback = isfillback;
	}
	
	public boolean getisfillback(){
		return isfillback;
	}
}
