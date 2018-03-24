package com.ru.usty.scheduling;
import com.ru.usty.scheduling.process.ProcessInfo;
import java.util.Comparator;

/* Provides a policy-specific comparison of processes
 * This comparison is necessary to construct a custom queue,
 * That is, where queue is ordered by some comparison element
 * instead of a normal FIFO structure */
public class ProcessesComparator implements Comparator <Integer> {
	
	Scheduler scheduler;
	public ProcessesComparator (Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	// Implements the nature of comparison of two elements; in this case processes
	@Override public int compare (Integer lhs, Integer rhs) {
		
		// Acquire left hand side and right hand side processes information
		// These are two processes currently being compared
		ProcessInfo p1 = this.scheduler.processExecution.getProcessInfo(lhs);
		ProcessInfo p2 = this.scheduler.processExecution.getProcessInfo(rhs);
		
		switch (this.scheduler.policy) {
	
		
			/* SHORTEST PROCESS NEXT SPECIFIC COMPARISON */
			case SPN:
				// Processes ordered by shortest total service time
				if (	p1.totalServiceTime > p2.totalServiceTime) return 1;
				else if (p1.totalServiceTime < p2.totalServiceTime) return -1;
				else return 0;

				
			/* SHORTEST REMAINING TIME SPECIFIC COMPARISON */
			case SRT:
				// Processes ordered by shortest remaining time
				if (	p1.totalServiceTime-p1.elapsedExecutionTime > p2.totalServiceTime-p2.elapsedExecutionTime) return 1;
				else if (p1.totalServiceTime-p1.elapsedExecutionTime < p2.totalServiceTime-p2.elapsedExecutionTime) return -1;
				else return 0;
			
				
			/* HIGHEST RESPONSE RATIO NEXT SPECIFIC COMPARISON */
			case HRRN:
				// Processes ordered by HIGHEST response ratio
				// (which is calculated by the formula: (process waiting time + process run time)/process run time)
				double resRatio1 = ((double)(p1.elapsedWaitingTime + p1.totalServiceTime)/(double)p1.totalServiceTime);
				double resRatio2 = ((double)(p2.elapsedWaitingTime + p2.totalServiceTime)/(double)p2.totalServiceTime);
				if (resRatio1 < resRatio2) { return 1; }
				else if (resRatio1 > resRatio2) { return -1; }
				else return 0;
				
				
			/* NO OTHER POLICY REQUIRES COMPARISON OF PROCESSES */
			default: break;
		}
		
		return 0;
	}

}
