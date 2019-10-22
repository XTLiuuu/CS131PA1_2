package cs131.pa1.filter.concurrent;


/**
 * The filter for printing in the console
 * @author cs131a
 *
 */
public class PrintFilter extends ConcurrentFilter {
	/**
	 * The constructor of the printer filter, no parameters.
	 */
	public PrintFilter() {
		super();
	}
	
	
	/**
	 * Processes the command
	 */
	@Override 
	public void process() {
		while (!isDone()) {
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
			String line = input.poll();
			if (line == null) {
				continue; 
			}
			processLine(line);
		}
	}
	
	
	/**
	 * Print a line 
	 */
	@Override 
	public String processLine(String line) {
		System.out.println(line);
		return null;
	}
	
	
	/**
	 * Runs the command
	 */
	@Override 
	public void run() {
		process();
		running = false; 
	}
}
