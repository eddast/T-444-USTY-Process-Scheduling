package com.ru.usty.scheduling;
import java.util.Comparator;

import com.ru.usty.scheduling.process.ProcessInfo;

public class ProcessesComparator implements Comparator <Integer> {
	
	Scheduler scheduler;
	public ProcessesComparator (Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override public int compare (Integer lhs, Integer rhs) {
		ProcessInfo p1 = this.scheduler.processExecution.getProcessInfo(lhs);
		ProcessInfo p2 = this.scheduler.processExecution.getProcessInfo(rhs);
		
		switch (this.scheduler.policy) {
			case SPN:
				if (	p1.totalServiceTime > p2.totalServiceTime) return 1;
				else if (p1.totalServiceTime < p2.totalServiceTime) return -1;
				else return 0;
			case SRT:
				if (	p1.totalServiceTime-p1.elapsedExecutionTime > p2.totalServiceTime-p2.elapsedExecutionTime) return 1;
				else if (p1.totalServiceTime-p1.elapsedExecutionTime < p2.totalServiceTime-p2.elapsedExecutionTime) return -1;
				else return 0;
			case HRRN:
				double resRatio1 = ((double)(p1.elapsedWaitingTime + p1.totalServiceTime)/(double)p1.totalServiceTime);
				double resRatio2 = ((double)(p2.elapsedWaitingTime + p2.totalServiceTime)/(double)p2.totalServiceTime);
				if (resRatio1 < resRatio2) { return 1; }
				else if (resRatio1 > resRatio2) { return -1; }
				else return 0;
			default:
				System.out.println("OH NO! Unreachable code reached!");
		}
		
		return 0;
	}

}
