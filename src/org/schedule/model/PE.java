package org.schedule.model;

/**
 * 
 * @ClassName: PE
 * @Description: processor 
 * @author YanWenjing
 * @date 2018-1-15 ����2:09:47
 */
public class PE {

	public int ability;

	public int ID;

	public int task;

	//释放这个处理器，证明这个处理器现在没有任务在其上运行，可以参与调度。本身没有什么价值
	boolean free;

	private double avail;

	private double[] ast;

	private double[] aft;
	
	public PE() {
		ast = new double[100];
		aft = new double[100];
	}

	public void setID(int id) {
		this.ID = id;
	}

	public int getID() {
		return ID;
	}

	public void setability(int temp) {
		this.ability = temp;
	}

	public int getability() {
		return ability;
	}

	public void setfree(boolean temp) {
		this.free = temp;
	}

	public boolean getfree() {
		return free;
	}

	public void settask(int task) {
		this.task = task;
	}

	public int gettask() {
		return task;
	}

	public void setAvail(double avail) {
		this.avail = avail;
	}

	public double getAvail() {
		return avail;
	}

	public void setast(int num, double ast) {
		this.ast[num] = ast;
	}

	public double getast(int num) {
		return ast[num];
	}

	public void setaft(int num, double aft) {
		this.aft[num] = aft;
	}

	public double getaft(int num) {
		return aft[num];
	}
}
