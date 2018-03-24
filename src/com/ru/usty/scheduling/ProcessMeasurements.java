package com.ru.usty.scheduling;

import java.util.ArrayList;

public class ProcessMeasurements {

	private long arrivalTime;
	private long startProcessingTime;
	private long completionTime;
	
	public ProcessMeasurements () {
		this.arrivalTime = System.currentTimeMillis();
		this.startProcessingTime = 0;
		this.completionTime = 0;
	}
	
	public void setProcessingStartTime() {
		if(this.startProcessingTime == 0) {
			this.startProcessingTime = System.currentTimeMillis();
		}
	}
	
	public void setCompletionTime() {
		if(this.completionTime == 0) {
			this.completionTime = System.currentTimeMillis();
		}
	}
	
	public long getCompletionTime() {
		return this.completionTime;
	}
	
	public long calculateTurnaroundTime() {
		return this.completionTime-this.arrivalTime;
	}
	
	public long calculateResponseTime() {
		return this.startProcessingTime - this.arrivalTime;
	}
	
	public static long calculateAverageTurnaroundTime(ArrayList<ProcessMeasurements> processMeasurements) {
		long ttimesum = 0;
		for(int i = 0; i < processMeasurements.size(); i++) {
			ttimesum = ttimesum + processMeasurements.get(i).calculateTurnaroundTime();
		}
		
		return ttimesum/processMeasurements.size();
	}
	
	public static long calculateAverageResponseTime(ArrayList<ProcessMeasurements> processMeasurements) {
		long rtimesum = 0;
		for(int i = 0; i < processMeasurements.size(); i++) {
			rtimesum = rtimesum + processMeasurements.get(i).calculateResponseTime();
		}
		
		return rtimesum/processMeasurements.size();
	}
}
