package cs131.pa1.filter.concurrent;
import java.util.concurrent.LinkedBlockingQueue;

import cs131.pa1.filter.Filter;

/**
 * An abstract class that extends the Filter and implements the basic functionality of all filters. Each filter should
 * extend this class and implement functionality that is specific for that filter.
 * @author cs131a
 *
 */
public abstract class ConcurrentFilter extends Filter implements Runnable {
	/**
	 * The input queue for this filter
	 */
	protected LinkedBlockingQueue<String> input;
	/**
	 * The output queue for this filter
	 */
	protected LinkedBlockingQueue<String> output;
	/**
	 * Whether the current command is still running
	 */
	protected boolean running = true; 

	@Override
	public void setPrevFilter(Filter prevFilter) {
		prevFilter.setNextFilter(this);
	}


	@Override
	public void setNextFilter(Filter nextFilter) {
		if (nextFilter instanceof ConcurrentFilter){
			ConcurrentFilter sequentialNext = (ConcurrentFilter) nextFilter;
			this.next = sequentialNext;
			sequentialNext.prev = this;
			if (this.output == null){
				this.output = new LinkedBlockingQueue<String>();
			}
			sequentialNext.input = this.output;
		} else {
			throw new RuntimeException("Should not attempt to link dissimilar filter types.");
		}
	}


	/**
	 * Gets the next filter
	 * @return the next filter
	 */
	public Filter getNext() {
		return next;
	}


	/**
	 * Runs the current thread
	 */
	public void run() {
		process();
		if (!Thread.currentThread().isInterrupted()) {
			running = false; // when the process finishes, sets the running to false
		}
	}


	/**
	 * Processes the input queue and writes the result to the output queue
	 */
	public void process(){
		while (!isDone()) {
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
			String line = input.poll();
			if (line == null) {
				continue;
			}
			String processedLine = processLine(line);
			if (processedLine != null){
				output.offer(processedLine);
			}
		}
	}


	/**
	 * Checks if the previous filter is done
	 */
	@Override
	public boolean isDone() {
		if (this.prev == null || prev.isDone()) {
			return input.isEmpty();
		}
		return false;
	}

	/**
	 * Processes a single line
	 * @param line
	 * @return the processed line
	 */
	protected abstract String processLine(String line);

}
