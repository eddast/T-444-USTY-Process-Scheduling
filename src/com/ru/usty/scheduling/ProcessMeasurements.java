package com.ru.usty.scheduling;

import java.util.ArrayList;

/* Provides measurement for processes, i.e. arrival time, start time and completion time
 * Maintains these values for processes and calculates the average turn-around time and average response time
 * For each scheduling algorithm scheme via the static functions prefixed by calculate */
public class ProcessMeasurements {

	private long arrivalTime;
	private long startProcessingTime;
	private long completionTime;
	
	// Initialize arrival time
	public ProcessMeasurements () {
		this.arrivalTime = System.currentTimeMillis();
		this.startProcessingTime = 0;
		this.completionTime = 0;
	}
	
	// Initialize start time; called whenever process is switched to
	// Never re-initialized in case process has been switched to before
	public void setProcessingStartTime() {
		if(this.startProcessingTime == 0) {
			this.startProcessingTime = System.currentTimeMillis();
		}
	}
	
	// Set completion time; called whenever process finishes
	public void setCompletionTime() {
		if(this.completionTime == 0) {
			this.completionTime = System.currentTimeMillis();
		}
	}
	
	// Calculates completion time, turn-around time and response time for current process
	public long getCompletionTime() { return this.completionTime; }
	public long calculateTurnaroundTime() { return this.completionTime-this.arrivalTime; }
	public long calculateResponseTime() { return this.startProcessingTime - this.arrivalTime; }
	
	// Calculates the average turn-around time for all processes in parameter list of process measurements
	public static long calculateAverageTurnaroundTime(ArrayList<ProcessMeasurements> processMeasurements) {
		long ttimesum = 0;
		for(int i = 0; i < processMeasurements.size(); i++) {
			ttimesum = ttimesum + processMeasurements.get(i).calculateTurnaroundTime();
		}
		
		return ttimesum/processMeasurements.size();
	}
	
	// Calculates the average response time for all processes in parameter list of process measurements
	public static long calculateAverageResponseTime(ArrayList<ProcessMeasurements> processMeasurements) {
		long rtimesum = 0;
		for(int i = 0; i < processMeasurements.size(); i++) {
			rtimesum = rtimesum + processMeasurements.get(i).calculateResponseTime();
		}
		
		return rtimesum/processMeasurements.size();
	}
}
