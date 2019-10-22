package cs131.pa1.filter.concurrent;

/**
 * The filter for wc command
 * @author cs131a
 *
 */
public class WcFilter extends ConcurrentFilter {
	/**
	 * The count of lines found
	 */
	private int linecount;
	/**
	 * The count of words found
	 */
	private int wordcount;
	/**
	 * The count of characters found
	 */
	private int charcount;
	
	
	/**
	 * The constructor of the wc filter
	 */
	public WcFilter() {
		super();
	}
	
	
	/**
	 *  Counts the number of lines, words, characters 
	 */
	public void process() {
		if (prev.isDone() && input.isEmpty()) {
			output.offer(processLine(null));
			running = false; 
		}
		else {
			while (!prev.isDone() || !input.isEmpty()) {
				if (Thread.currentThread().isInterrupted()) {
					break;
				}
				String line = input.poll();
				if (line == null) {
					continue; 
				}
				line = processLine(line);
				if (line != null) {
					output.offer(line);
				}
			}
		}
	}
	
	
	/**
	 * Counts the number of lines, words and characters from the input queue
	 * @param line the line as got from the input queue
	 * @return the number of lines, words, and characters when finished, null otherwise
	 */
	public String processLine(String line) {
		//prints current result if ever passed a null
		if(line == null) {
			return linecount + " " + wordcount + " " + charcount;
		}
		if(isDone()) {
			String[] wct = line.split(" ");
			wordcount += wct.length;
			String[] cct = line.split("|");
			charcount += cct.length;
			return ++linecount + " " + wordcount + " " + charcount;
		} else {
			linecount++;
			String[] wct = line.split(" ");
			wordcount += wct.length;
			String[] cct = line.split("|");
			charcount += cct.length;
			return null;
		}
	}
	
	
	/**
	 * Runs the command 
	 */
	@Override 
	public void run() {
		while (!isDone()) {
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
			process();
		}
	}
	
	
	/**
	 * Checks if the command finishes 
	 */
	@Override 
	public boolean isDone() {
		return !running; 
	}
}
