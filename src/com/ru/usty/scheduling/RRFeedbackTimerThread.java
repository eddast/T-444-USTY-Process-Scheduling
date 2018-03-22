package com.ru.usty.scheduling;

import com.ru.usty.scheduling.process.ProcessExecution;

public class RRFeedbackTimerThread implements Runnable {
	
	Scheduler scheduler;

	RRFeedbackTimerThread (Scheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	@Override public void run() {
		while (true) {
			try {
				Thread.sleep(50);
				if ( scheduler.processes.size() != 0 ) {
					try {
						Integer currentProcess = scheduler.processes.peek();
						Thread.sleep(scheduler.quantum);
						if(currentProcess == scheduler.processes.peek()) {
							Integer moveProcessBack = scheduler.processes.poll();
							if(moveProcessBack != null) {
								scheduler.processes.add(moveProcessBack);
								scheduler.processExecution.switchToProcess(scheduler.processes.peek());
							}
						}
						
					} catch (InterruptedException e) { e.printStackTrace(); }
				}
			} catch (InterruptedException e1) { e1.printStackTrace(); }
		}
	}
}
