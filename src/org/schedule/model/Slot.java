package org.schedule.model;

import java.util.ArrayList;

/**
 * 
* @ClassName: PESlot
* @Description: The idle block of the processor
* @author YanWenjing
* @date 2018-1-15 обнГ2:32:14
 */
public class Slot { 

	public int PEId;

	public int slotId;

	public int slotstarttime;

	public int slotfinishtime;

	public ArrayList<String> below;
	
	public Slot(){
		below = new ArrayList<String>();
	}
	
	public ArrayList<String> getbelow(){
		return below;
	}
	
	public void setbelow(ArrayList<String> below_){
		for(int i=0;i<below_.size();i++)
		{
			below.add(below_.get(i));
		}
	}
	
	public void setPEId(int pe){
		this.PEId = pe;
	}
	
	public int getPEId(){
		return PEId;
	}
	
	public void setslotId(int id){
		this.slotId = id;
	}
	
	public int getslotId(){
		return slotId;
	}
	
	public void setslotstarttime(int start){
		this.slotstarttime = start;
	}
	
	public int getslotstarttime(){
		return slotstarttime;
	}
	
	public void setslotfinishtime(int finish){
		this.slotfinishtime = finish;
	}
	
	public int getslotfinishtime(){
		return slotfinishtime;
	}
}
