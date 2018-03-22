package com.ru.usty.scheduling;

import com.ru.usty.scheduling.process.ProcessExecution;
import java.util.Queue;
import java.util.LinkedList;

public class Scheduler {

	ProcessExecution processExecution;
	Policy policy;
	Queue<Integer> processes;
	int currentProcess = -1;
	int quantum;
	
	
	public Scheduler(ProcessExecution processExecution) {
		this.processExecution = processExecution;
	}
	
	
	public void startScheduling(Policy policy, int quantum) {

		this.policy = policy; this.quantum = quantum;

		switch(policy) {
		
		/* FIRST COME FIRST SERVED */
		case FCFS:
			this.processes = new LinkedList<Integer>();
			System.out.println("Starting new scheduling task: First-come-first-served");
			break;
			
		/* ROUND ROBIN */
		case RR:
			this.processes = new LinkedList<Integer>();
			System.out.println("Starting new scheduling task: Round robin, quantum = " + quantum);
			runInQuantumTime();
			break;
			
		case SPN:	//Shortest process next
			System.out.println("Starting new scheduling task: Shortest process next");
			break;
			
		case SRT:	//Shortest remaining time
			System.out.println("Starting new scheduling task: Shortest remaining time");
			break;
			
		case HRRN:	//Highest response ratio next
			System.out.println("Starting new scheduling task: Highest response ratio next");
			break;
			
		case FB:	//Feedback
			System.out.println("Starting new scheduling task: Feedback, quantum = " + quantum);
			break;
			
		}

	}

	public void processAdded(int processID) {
		
		switch (this.policy) {
		
			/* FIRST COME FIRST SERVED */
			case FCFS:
				this.processes.add(processID);
				if(processID == this.processes.peek()) {
					this.processExecution.switchToProcess(processID);
				}
				break;
				
			/* ROUND ROBIN*/
			case RR:
				this.processes.add(processID);
				this.currentProcess = processID;
				if(processID == this.processes.peek()) {
					this.processExecution.switchToProcess(processID);
				}
				break;
				
			default:
				System.out.println("fuck off");
		}
	}
	
	public void runInQuantumTime() {
		Thread RRthread = new Thread(new RRFeedbackTimerThread(this));
		RRthread.start();
	}

	public void processFinished(int processID) {

		switch (this.policy) {
		
			/* FIRST COME FIRST SERVED */
			case FCFS:
				this.processes.remove();
				if(this.processes.size() != 0) {
					this.processExecution.switchToProcess(this.processes.peek());
				}
			break;
			
			/* ROUND ROBIN */
			case RR:	
				this.processes.remove();
				if(this.processes.size() != 0) {
					this.processExecution.switchToProcess(this.processes.peek());
				}
				
			break;
			
			default:
				System.out.println("fuck off");

				
		}

	}
}
