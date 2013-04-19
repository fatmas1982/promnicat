package de.uni_potsdam.hpi.bpt.promnicat.bpa;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rami.eidsabbagh
 *
 */
public class SendingEvent extends Event{
	
	List<ReceivingEvent> postset = new ArrayList<ReceivingEvent>();
	
	/**
	 * @param eventid
	 * @param bpid
	 * @param label
	 * @param mult 
	 */
	public SendingEvent(int eventid, int bpid, String label, int[] mult) {
		super(eventid, bpid, label, mult);
		
		// TODO Auto-generated constructor stub
	}

	public void setPostset(List<ReceivingEvent> successors){
		postset = successors;
	}
	
	public List<ReceivingEvent> getPostset(){
		return postset;
	}
}
