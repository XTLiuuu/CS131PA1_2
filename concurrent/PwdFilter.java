package cs131.pa1.filter.concurrent;

/**
 * The filter for pwd command
 * @author cs131a
 *
 */
public class PwdFilter extends ConcurrentFilter {
	/**
	 * The constructor of the pwd filter, no parameters.
	 */
	public PwdFilter() {
		super();
	}
	
	/**
	 * Processes the command
	 */
	@Override 
	public void process() {
		output.offer(processLine(""));
	}
	
	
	/**
	 * Changes the working directory 
	 */
	@Override
	public String processLine(String line) {
		return ConcurrentREPL.currentWorkingDirectory;
	}
	
	
	/**
	 * Checks if the command finishes 
	 */
	@Override 
	public boolean isDone() {
		return !running; 
	}
}
