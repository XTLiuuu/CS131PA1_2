package cs131.pa1.filter.concurrent;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import cs131.pa1.filter.Filter;
import cs131.pa1.filter.Message;

/**
 * The filter for cat command
 * @author cs131a
 *
 */
public class CatFilter extends ConcurrentFilter{
	private Scanner reader;
	
	/**
	 * The constructor of the cat filter
	 * @param line the parameters for cat
	 * @throws Exception throws exception when there is an error with the given parameters,
	 * 			or when the file is not found
	 */
	public CatFilter(String line) throws Exception {
		super();
		//parsing the cat options
		String[] args = line.split(" ");
		String filename;
		//obviously incorrect number of parameters
		if(args.length == 1) {
			System.out.printf(Message.REQUIRES_PARAMETER.toString(), line);
			throw new Exception();
		} else {
			try {
				filename =  ConcurrentREPL.currentWorkingDirectory + Filter.FILE_SEPARATOR + args[1];
			} catch (Exception e) {
				System.out.printf(Message.REQUIRES_PARAMETER.toString(), line);
				throw new Exception();
			}
		}
		try {
			reader = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			System.out.printf(Message.FILE_NOT_FOUND.toString(), line);
			throw new FileNotFoundException();
		}
	}

	/**
	 * Overrides the process() method of ConcurrentFilter to simply
	 * check whether the file has more lines (through the reader object)
	 * and calls processLine() for each line 
	 */
	@Override
	public void process() {
		while(reader.hasNext()) {
			// stop if the current command is interrupted 
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
			String processedLine = processLine("");
			if(processedLine == null) {
				break;
			}
			output.offer(processedLine);
		}
		reader.close();
	}

	/**
	 * Processes each line by reading from the reader object and adding the result to the output queue
	 * @param line the line to be processed
	 */
	@Override
	public String processLine(String line) {
		if(reader.hasNextLine()) {
			return reader.nextLine();
		} else {
			return null;
		}
	}
	
	/**
	 * Checks if the current command is finished 
	 */
	@Override
	public boolean isDone() {
		return !running; 
	}
}
