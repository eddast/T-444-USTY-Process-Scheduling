package com.ru.usty.scheduling;

import com.ru.usty.scheduling.process.ProcessExecution;
import java.util.Queue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Scheduler {
	
	final public int FEEDBACKQUEUECOUNT = 7;
	final public int TOTALPROCESSESCOUNT = 15;
	ProcessExecution processExecution;
	Policy policy;
	Queue<Integer> processes;
	ArrayList<Queue<Integer>> feedbackQueues;
	int quantum;
	Integer currentProcess = null;
	Thread RRFeedbackThread = null;
	ArrayList<ProcessMeasurements> processMeasurements;
	
	
	public Scheduler(ProcessExecution processExecution) {
		this.processExecution = processExecution;
		this.processMeasurements = new ArrayList<ProcessMeasurements>();
	}
	
	
	public void startScheduling(Policy policy, int quantum) {

		if(this.processMeasurements.size() > 0) {
			System.out.println("Average Turnaround Time: " + ProcessMeasurements.calculateAverageTurnaroundTime(processMeasurements));
			System.out.println("Average Response Time: " + ProcessMeasurements.calculateAverageResponseTime(processMeasurements));
		}

		this.policy = policy; this.quantum = quantum;
		this.processMeasurements = new ArrayList<ProcessMeasurements>();

		switch (policy) {
		
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
			
		/* SHORTEST PROCESS NEXT */
		case SPN:
			this.processes = new PriorityQueue<Integer>(50, new ProcessesComparator(this));
			System.out.println("Starting new scheduling task: Shortest process next");
			break;
		
		/* SHORTEST REMAINING TIME */
		case SRT:
			this.processes = new PriorityQueue<Integer>(50, new ProcessesComparator(this));
			System.out.println("Starting new scheduling task: Shortest remaining time");
			break;
			
		/* HIGHEST RESPONSE RATIO NEXT */
		case HRRN:
			this.processes = new PriorityQueue<Integer>(50, new ProcessesComparator(this));
			System.out.println("Starting new scheduling task: Highest response ratio next");
			break;
			
		/* FEEDBACK */
		case FB:	
			this.feedbackQueues = new ArrayList<Queue<Integer>>();
			for (int i = 0; i < this.FEEDBACKQUEUECOUNT; i++) {
				this.feedbackQueues.add(new LinkedList<Integer>());
			}
			System.out.println("Starting new scheduling task: Feedback, quantum = " + quantum);
			runInQuantumTime();
			break;
			
		}

	}

	public void processAdded(int processID) {
		
		this.processMeasurements.add(new ProcessMeasurements());
		
		switch (this.policy) {
		
			/* FIRST COME FIRST SERVED */
			case FCFS:
				this.processes.add(processID);
				if (processID == this.processes.peek()) {
					this.processExecution.switchToProcess(processID);
					this.processMeasurements.get(processID).setProcessingStartTime();
				}
				break;
				
			/* ROUND ROBIN*/
			case RR:
				this.processes.add(processID);
				if (processID == this.processes.peek()) {
					this.currentProcess = processID;
					this.processExecution.switchToProcess(processID);	
					this.processMeasurements.get(processID).setProcessingStartTime();

				}
				break;
				
			/* SHORTEST PROCESS NEXT */
			case SPN:
				this.processes.add(processID);
				if (this.processes.size() == 1 && processID == this.processes.peek()) {
					this.currentProcess = processID;
					this.processExecution.switchToProcess(processID);
					this.processMeasurements.get(processID).setProcessingStartTime();
				}
				break;
				
			/* SHORTEST REMAINING TIME */
			case SRT:
				this.processes.add(processID);
				if (processID == this.processes.peek()) {
					this.currentProcess = processID;
					this.processExecution.switchToProcess(processID);
					this.processMeasurements.get(processID).setProcessingStartTime();
				}
				break;
				
			/* HIGHEST RESPONSE RATIO NEXT */
			case HRRN:
				this.processes.add(processID);
				if (this.processes.size() == 1) {
					this.currentProcess = processID;
					this.processExecution.switchToProcess(processID);
					this.processMeasurements.get(processID).setProcessingStartTime();
				}
				break;	
			
			/* FEEDBACK */
			case FB:
				if (this.allFeebackQueuesEmpty()) {
					this.currentProcess = processID;
					this.processExecution.switchToProcess(processID);
					this.processMeasurements.get(processID).setProcessingStartTime();
				}
				this.feedbackQueues.get(0).add(processID);
				
				break;
			default:
				System.out.println("fuck off");
		}
	}
	
	private void runInQuantumTime() {
		if (this.RRFeedbackThread == null) {
			this.RRFeedbackThread = new Thread(new TimeSlicing(this));
			RRFeedbackThread.start();
		}
	}
	
	public void feedbackSwitchProcess() {
		int queueOfCurrentProcess = this.feedbackFindQueueOfCurrentProcess();
		if(queueOfCurrentProcess != -1) {
			this.feedbackQueues.get(queueOfCurrentProcess).remove();
			int highestPriorityQueue = this.findFirstNonEmptyFeedbackQueue();
			if(highestPriorityQueue != -1) {
				this.processExecution.switchToProcess(this.feedbackQueues.get(highestPriorityQueue).peek());
				this.currentProcess = this.feedbackQueues.get(highestPriorityQueue).peek();
				this.processMeasurements.get(this.currentProcess).setProcessingStartTime();
			} else {
				this.currentProcess = -1;
			}
		}
	}
	
	public int feedbackFindQueueOfCurrentProcess() {
		if(this.currentProcess != -1) {
			for (int i = 0; i < this.FEEDBACKQUEUECOUNT; i++) {
				if (this.feedbackQueues.get(i).peek() != null &&
					this.feedbackQueues.get(i).peek() == this.currentProcess) {
					return i;
				}
			}
		}
		return -1;
	}

	public void processFinished(int processID) {
		
		this.processMeasurements.get(processID).setCompletionTime();

		switch (this.policy) {
		
			/* FIRST COME FIRST SERVED */
			case FCFS:
				this.processes.remove();
				if(this.processes.size() != 0) {
					this.processExecution.switchToProcess(this.processes.peek());
					this.processMeasurements.get(this.processes.peek()).setProcessingStartTime();
				}
				break;
			
			/* ROUND ROBIN */
			case RR:	
				this.processes.remove();
				if(this.processes.size() != 0) {
					this.processExecution.switchToProcess(this.processes.peek());
					this.processMeasurements.get(this.processes.peek()).setProcessingStartTime();
				}
				break;
				
			/* SHORTEST PROCESS NEXT */
			case SPN:
				this.processes.remove(currentProcess);
				if(this.processes.size() != 0) {
					this.currentProcess = this.processes.peek();
					this.processExecution.switchToProcess(this.processes.peek());
					this.processMeasurements.get(this.processes.peek()).setProcessingStartTime();
				}
				break;
				
			/* SHORTEST REMAINING TIME */
			case SRT:
				this.processes.remove(currentProcess);
				if(this.processes.size() != 0) {
					this.currentProcess = this.processes.peek();
					this.processExecution.switchToProcess(this.processes.peek());
					this.processMeasurements.get(this.processes.peek()).setProcessingStartTime();
				}
				break;
			
			/* HIGHEST RESPONSE RATIO NEXT */
			case HRRN:
				this.processes.remove(currentProcess);
				if(this.processes.size() != 0) {
					this.currentProcess = this.processes.peek();
					this.processExecution.switchToProcess(this.processes.peek());
					this.processMeasurements.get(this.processes.peek()).setProcessingStartTime();
				}
				break;

			/* FEEDBACK */
			case FB:
				this.feedbackSwitchProcess();
				if(allProcessesHaveFinished()) {
					System.out.println("Average Turnaround Time: " + ProcessMeasurements.calculateAverageTurnaroundTime(processMeasurements));
					System.out.println("Average Response Time: " + ProcessMeasurements.calculateAverageResponseTime(processMeasurements));
				}
				break;
			
			default:
				break;
		}
	}
	
	boolean allProcessesHaveFinished() {
		if (this.processMeasurements.size() == this.TOTALPROCESSESCOUNT) {
			for(int i = 0; i < TOTALPROCESSESCOUNT; i++) {
				if(this.processMeasurements.get(i).getCompletionTime() == 0) {
					return false;
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	boolean allFeebackQueuesEmpty() {
		for ( int i = 0; i < this.FEEDBACKQUEUECOUNT; i++ ) {
			if ( this.feedbackQueues.get(i).size() > 0 ) {
				return false;
			}
		}
		return true;
	}
	
	int findFirstNonEmptyFeedbackQueue() {
		for ( int i = 0; i < this.FEEDBACKQUEUECOUNT; i++ ) {
			if ( this.feedbackQueues.get(i).size() > 0 ) {
				return i;
			}
		}
		return -1;
	}
}
