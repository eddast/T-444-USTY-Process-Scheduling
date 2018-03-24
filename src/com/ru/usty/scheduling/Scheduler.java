package com.ru.usty.scheduling;

import com.ru.usty.scheduling.process.ProcessExecution;
import java.util.Queue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Scheduler {
	
	// Environment variables
	ProcessExecution processExecution;
	Policy policy;
	int quantum;
	
	// Constants of Feedback queues count and
	// total processes in scheme
	// Error return value to explicitly notify an error
	final public int FEEDBACKQUEUECOUNT = 7;
	final public int TOTALPROCESSESCOUNT = 15;
	final public int ERRORRETURNVALUE = -1;
	
	// ready queues for all policies except Feedback are a single queue
	// Feedback ready queue is a list of queues
	Queue<Integer> processes;
	ArrayList<Queue<Integer>> feedbackQueues;
	
	// Variables to keep track of current process and time slice thread
	Integer currentProcess = null;
	Thread RRFeedbackThread = null;
	
	// Turn-around time and response time calculation aids
	ArrayList<ProcessMeasurements> processMeasurements;
	
	
	// Set up scheduling schemes
	public Scheduler(ProcessExecution processExecution) {
		this.processExecution = processExecution;
		this.processMeasurements = new ArrayList<ProcessMeasurements>();
	}
	
	
	// Define initial behavior specific to policy
	public void startScheduling(Policy policy, int quantum) {

		// Display measurements after each policy
		if(this.processMeasurements.size() > 0) {
			System.out.println("Average Turnaround Time: " + ProcessMeasurements.calculateAverageTurnaroundTime(processMeasurements));
			System.out.println("Average Response Time: " + ProcessMeasurements.calculateAverageResponseTime(processMeasurements));
		}

		// Setup environment
		this.policy = policy; this.quantum = quantum;
		this.processMeasurements = new ArrayList<ProcessMeasurements>();

		switch (policy) {
		
		/* FIRST COME FIRST SERVED */
		case FCFS:
			/* SET READY QUEUE */
			// Ready queue is a FIFO queue implemented  by LinkedList
			this.processes = new LinkedList<Integer>();
			System.out.println("Starting new scheduling task: First-come-first-served");
			break;
			
		/* ROUND ROBIN */
		case RR:
			/* SET READY QUEUE */
			// Ready queue is a FIFO queue implemented  by LinkedList
			this.processes = new LinkedList<Integer>();
			System.out.println("Starting new scheduling task: Round robin, quantum = " + quantum);
			runInQuantumTime();
			break;
			
		/* SHORTEST PROCESS NEXT */
		case SPN:
			/* SET READY QUEUE */
			// Ready queue is a custom priority queue ordered by specific comparator
			// In this case by the total run time of processes
			this.processes = new PriorityQueue<Integer>(50, new ProcessesComparator(this));
			System.out.println("Starting new scheduling task: Shortest process next");
			break;
		
		/* SHORTEST REMAINING TIME */
		case SRT:
			/* SET READY QUEUE */
			// Ready queue is a custom priority queue ordered by specific comparator
			// In this case by the remaining run time of processes
			this.processes = new PriorityQueue<Integer>(50, new ProcessesComparator(this));
			System.out.println("Starting new scheduling task: Shortest remaining time");
			break;
			
		/* HIGHEST RESPONSE RATIO NEXT */
		case HRRN:
			/* SET READY QUEUE */
			// Ready queue is a custom priority queue ordered by specific comparator
			// In this case by the highest response ratio of processes
			this.processes = new PriorityQueue<Integer>(50, new ProcessesComparator(this));
			System.out.println("Starting new scheduling task: Highest response ratio next");
			break;
			
		/* FEEDBACK */
		case FB:	
			/* SET READY QUEUE */
			// Ready queue is a list of seven queues, all of which have priority
			// Corresponding to index in list (queue 0 in list is highest priority)
			this.feedbackQueues = new ArrayList<Queue<Integer>>();
			for (int i = 0; i < this.FEEDBACKQUEUECOUNT; i++) {
				this.feedbackQueues.add(new LinkedList<Integer>());
			}
			System.out.println("Starting new scheduling task: Feedback, quantum = " + quantum);
			runInQuantumTime();
			break;
			
		}

	}

	// Called whenever process wishes to enter ready queue
	public void processAdded(int processID) {
		
		this.processMeasurements.add(new ProcessMeasurements());
		
		switch (this.policy) {
		
			/* FCFS, RR, SPN AND HRRN WORK THE SAME */
			case FCFS:
			case RR:
			case SPN:
			case HRRN:
				/* ADD TO READY QUEUE */
				// Adds to ready queue and switches to process if queue was previously empty 
				this.processes.add(processID);
				if (this.processes.size() == 1) {
					this.currentProcess = processID;
					this.processExecution.switchToProcess(processID);
					this.processMeasurements.get(processID).setProcessingStartTime();
				}
				break;
				
				
			/* SHORTEST REMAINING TIME (PRIORTY QUEUE PREEMPTIVE) */
			case SRT:
				/* ADD TO READY QUEUE */
				// Adds to ready queue and switches to process if comparison
				// Determines the new process should be switched to next
				this.processes.add(processID);
				if (processID == this.processes.peek()) {
					this.currentProcess = processID;
					this.processExecution.switchToProcess(processID);
					this.processMeasurements.get(processID).setProcessingStartTime();
				}
				break;	
			
				
			/* FEEDBACK */
			case FB:
				/* ADD TO READY QUEUE */
				// Adds new process to highest priority queue in list
				// Switches to new process if queue was previously empty
				if (this.allFeebackQueuesEmpty()) {
					this.currentProcess = processID;
					this.processExecution.switchToProcess(processID);
					this.processMeasurements.get(processID).setProcessingStartTime();
				}
				this.feedbackQueues.get(0).add(processID);
				
				break;
			
				
			/* NO OTHER SCHEDULING POLICIES ARE SUPPORTED */
			default: break;
		}
	}
	
	// Called when process has finished running, i.e. has run for total of it's service time
	public void processFinished(int processID) {
		
		// Set completion time for a given process
		this.processMeasurements.get(processID).setCompletionTime();

		switch (this.policy) {
			
			/* ALL POLICIES EXCEPT FEEDBACK WORK ESSENTIALLY THE SAME */
			case FCFS:
			case RR:	
				removeFromReadyQueue(); break;
			case SPN:
			case SRT:
			case HRRN:
				removeFromReadyQueue(processID); break;

			/* FEEDBACK */
			case FB:
				// Feedback specific determination on how to switch to next process
				// Print out results if processes have finished since this is the last policy in test suite
				this.feedbackSwitchProcess();
				this.printMeasurementResults();
				break;
			
				
			/* NO OTHER POLICIES SUPPORTED */
			default: break;
		}
	}
	
	
	/***************************************
	 * 			 HELPER FUNCTIONS
	 ***************************************/	
	
	
	// RR and FCFS actions on removing process from ready queue
	private void removeFromReadyQueue() {
		/* REMOVE FROM READY QUEUE */
		// Remove finished process from queue
		// If any processes remain, switch to next process in queue
		this.processes.remove();
		if(this.processes.size() != 0) {
			this.currentProcess = this.processes.peek();
			this.processExecution.switchToProcess(this.processes.peek());
			this.processMeasurements.get(this.processes.peek()).setProcessingStartTime();
		}
	}
	
	// SPN, SRT and HRRN actions on removing process from ready queue
	private void removeFromReadyQueue(int processID) {
		/* REMOVE FROM READY QUEUE */
		// Remove finished process from queue
		// If any processes remain, switch to next process in queue
		this.processes.remove(currentProcess);
		if(this.processes.size() != 0) {
			this.currentProcess = this.processes.peek();
			this.processExecution.switchToProcess(this.processes.peek());
			this.processMeasurements.get(this.processes.peek()).setProcessingStartTime();
		}
	}
	
	
	/***********************************************
	 * 			FEEDBACK HELPER FUNCTIONS
	 ***********************************************/
	
	
	// Starts a thread which conducts time slicing via custom runnable class TimeSlicing
	// (Round Robin and Feedback)
	private void runInQuantumTime() {
		if (this.RRFeedbackThread == null) {
			this.RRFeedbackThread = new Thread(new TimeSlicing(this));
			RRFeedbackThread.start();
		}
	}
	
	// Switches process in a Feedback policy scheduling scheme
	public void feedbackSwitchProcess() {
		
		// Get queue of current process
		int queueOfCurrentProcess = this.feedbackFindQueueOfCurrentProcess();
		
		// If some process is currently being ran (that is, the function didn't return error (-1)
		// We perceed to remove the current process and to run the next process to be run
		// which is the first element the highest priority queue
		if(queueOfCurrentProcess != this.ERRORRETURNVALUE) {
			
			this.feedbackQueues.get(queueOfCurrentProcess).remove();
			int highestPriorityQueue = this.findFirstNonEmptyFeedbackQueue();
			
			if (highestPriorityQueue != this.ERRORRETURNVALUE) {
				
				this.processExecution.switchToProcess(this.feedbackQueues.get(highestPriorityQueue).peek());
				this.currentProcess = this.feedbackQueues.get(highestPriorityQueue).peek();
				this.processMeasurements.get(this.currentProcess).setProcessingStartTime();
				
			} else { this.currentProcess = this.ERRORRETURNVALUE; }
		}
	}
	
	// Gets queue of current process
	public int feedbackFindQueueOfCurrentProcess() {
		if(this.currentProcess != this.ERRORRETURNVALUE) {
			for (int i = 0; i < this.FEEDBACKQUEUECOUNT; i++) {
				if (this.feedbackQueues.get(i).peek() != null &&
					this.feedbackQueues.get(i).peek() == this.currentProcess) {
					return i;
				}
			}
		}
		
		// ERROR: No queue for a current process
		return this.ERRORRETURNVALUE;
	}
	
	// Prints results of turn-around time and response time
	private void printMeasurementResults() {
		if (allProcessesHaveFinished()) {
			System.out.println("Average Turnaround Time: " + ProcessMeasurements.calculateAverageTurnaroundTime(processMeasurements));
			System.out.println("Average Response Time: " + ProcessMeasurements.calculateAverageResponseTime(processMeasurements));
		}
	}
	
	// Checks if all processes have finished running
	// Feedback needs this check as it's the last one in test suite
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
	
	// Checks if all queues in list is empty (Feedback specific)
	boolean allFeebackQueuesEmpty() {
		for ( int i = 0; i < this.FEEDBACKQUEUECOUNT; i++ ) {
			if ( this.feedbackQueues.get(i).size() > 0 ) {
				return false;
			}
		}
		return true;
	}
	
	// Finds first non-empty queue of list in a Feedback policy scheme
	// This essentially also finds highest priority queue in list
	int findFirstNonEmptyFeedbackQueue() {
		for ( int i = 0; i < this.FEEDBACKQUEUECOUNT; i++ ) {
			if ( this.feedbackQueues.get(i).size() > 0 ) {
				return i;
			}
		}
		
		// ERROR: No non-empty queue
		return this.ERRORRETURNVALUE;
	}
}
