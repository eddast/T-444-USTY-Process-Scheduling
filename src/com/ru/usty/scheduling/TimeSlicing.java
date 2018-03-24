package com.ru.usty.scheduling;

public class TimeSlicing implements Runnable {
	
	Scheduler scheduler;

	TimeSlicing (Scheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	@Override public void run() {
		
		while (true) {
			
			switch(this.scheduler.policy) {
			
				
				case RR:
					try {
						Thread.sleep(50);
						if ( scheduler.processes.size() != 0 ) {
							try {
								Integer currentProcess = scheduler.processes.peek();
								Thread.sleep(scheduler.quantum);
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
					
				case FB:
				try {
					Thread.sleep(50);
					int highestPriorityQueue = this.scheduler.findFirstNonEmptyFeedbackQueue();
					if(highestPriorityQueue != -1) {
						Integer currentProcess = this.scheduler.currentProcess;
						Thread.sleep(scheduler.quantum);
						if (this.scheduler.currentProcess != -1) {
							if(currentProcess == this.scheduler.currentProcess) {
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
				default:
					break;
			}
		}
	}
}
