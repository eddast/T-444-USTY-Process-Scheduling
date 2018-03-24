package com.ru.usty.scheduling;

/* Implements time slicing functionality for scheduling schemes appropriately
 * Necessary for Round Robin and Feedback implementations */
public class TimeSlicing implements Runnable {
	
	Scheduler scheduler;
	TimeSlicing (Scheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	// Defines thread behavior
	// Specific to policy used
	@Override public void run() {
		
		while (true) {
			
			switch (this.scheduler.policy) {
			
				/* ROUND ROBIN SPECIFIC TIME SLICING */
				case RR:
					
					try {
	
						Thread.sleep(50);
						
						// Time−slicing discontinued when no processes are in queue
						if ( scheduler.processes.size() != 0 ) {
							try {
								
								// Preserve current process before ordering thread to sleep for a quantum time before swapping process
								// Thread sleep period ensures process runs for a quantum time before swap
								Integer currentProcess = scheduler.processes.peek();
								Thread.sleep(scheduler.quantum);
								
								// Check if process currently running is the same process running as when before Thread sleep command
								// If not, that process had finished running while thread was sleeping
								// If it hadn't finished running, we move it to the back of the queue where it awaits it's turn
								if(currentProcess == scheduler.processes.peek()) {
									Integer moveProcessBack = scheduler.processes.poll();
									if ( moveProcessBack != null) {
										scheduler.processes.add(moveProcessBack);
										scheduler.processExecution.switchToProcess(scheduler.processes.peek());
										scheduler.processMeasurements.get(scheduler.processes.peek()).setProcessingStartTime();
									}
								}
							} catch (InterruptedException e) { e.printStackTrace(); }
						}
					} catch (InterruptedException e1) { e1.printStackTrace(); }
					break;
					
					
				/* FEEDBACK SPECIFIC TIME SLICING */
				case FB:
					
					try {
						
						Thread.sleep(50);
						
						// Find queue which currently has highest priority
						int highestPriorityQueue = this.scheduler.findFirstNonEmptyFeedbackQueue();
						
						// If list is non-empty, i.e. there is a non-empty queue in list we conduct time splicing
						// Else we discontinue time splicing scheme
						if(highestPriorityQueue != -1) {
							
							// Preserve current process before ordering thread to sleep for a quantum time before swapping process
							// Thread sleep period ensures process runs for a quantum time before swap
							Integer currentProcess = this.scheduler.currentProcess;
							Thread.sleep(scheduler.quantum);
							
							// If no process is currently running, no process await and we discontinue
							if (this.scheduler.currentProcess != -1) {
								
								// Check if process currently running is the same process running as when before Thread sleep command
								// If not, that process had finished running while thread was sleeping
								// If it hadn't finished running, we move it to next queue (less priority queue) where it awaits it's turn
								if(currentProcess == this.scheduler.currentProcess) {
									
									// Move process from it's current queue to the one above where it awaits it's turn
									int currentProcessQueue = this.scheduler.feedbackFindQueueOfCurrentProcess();
									if(currentProcessQueue != this.scheduler.FEEDBACKQUEUECOUNT-1) {
										this.scheduler.feedbackQueues.get(currentProcessQueue+1).add(this.scheduler.currentProcess);	
									} else {
										this.scheduler.feedbackQueues.get(currentProcessQueue).add(this.scheduler.currentProcess);
									}
									this.scheduler.feedbackSwitchProcess();
								}
							}
						}
					} catch (InterruptedException e) { e.printStackTrace(); }
					break;
				
					
				/* NO OTHER POLICY IMPLEMENTS TIME SLICING */
				default: break;
			}
		}
	}
}
