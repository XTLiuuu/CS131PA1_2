package cs131.pa1.filter.concurrent;

import cs131.pa1.filter.Message;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * The main implementation of the REPL loop (read-eval-print loop).
 * It reads commands from the user, parses them, executes them and displays the result.
 * @author cs131a
 *
 */
public class ConcurrentREPL {
	/**
	 * the path of the current working directory
	 */
	static String currentWorkingDirectory;
	/**
	 * All commands 
	 * key is the number, value is command (in thread list) (primarily used to kill) 
	 */
	static LinkedHashMap<Integer, ArrayList<Thread>> threads = new LinkedHashMap<Integer, ArrayList<Thread>>(); 
	/**
	 * All commands in the background 
	 * key is the number, value is the command (in string) 
	 */
	static LinkedHashMap<Integer, String> back = new LinkedHashMap<Integer, String>();
	/**
	 * All commands 
	 * key is the number, value is the head of the command list (primarily used to check done) 
	 */
	static LinkedHashMap<Integer, ConcurrentFilter> filters = new LinkedHashMap<Integer, ConcurrentFilter>(); 
	/**
	 * the number of total commands + 1 
	 * i.e. map key for the next command 
	 */
	static int filterNum; 
	
	/**
	 * The main method that will execute the REPL loop
	 * @param args not used
	 */
	public static void main(String[] args){
		currentWorkingDirectory = System.getProperty("user.dir");
		Scanner s = new Scanner(System.in);
		filterNum = 1; 
		System.out.print(Message.WELCOME);
		String command;
		while(true) {
			//obtaining the command from the user
			System.out.print(Message.NEWCOMMAND);
			command = s.nextLine();
			if(command.equals("exit")) {
				break;
			}
			else {
				// check if the current command is repl_jobs
				if (checkRepl(command)) {
					continue; 
				}
				// check if the current command is kill
				if (checkKill(command)) {
					continue; 
				}
				// other commands 
				if(!command.trim().equals("")) {
					// check if the current command contains a back sign 
					if (isBackground(command)) {
						back.put(filterNum, command); // add to the back map 
						createConcurrentCommand(command);
					}
					// foreground command 
					else {
						createNormalCommand(command);
					}
				}
			}
		}
		s.close();
		System.out.print(Message.GOODBYE);
	}

	
	/**
	 * Responds to get_repl
	 */
	public static void getReplJobs() {
		Iterator<Integer> iterator = back.keySet().iterator();
		List<Integer> done = new ArrayList<Integer>(); // list for done jobs
		while (iterator.hasNext()) {
			int num = iterator.next().intValue();
			// check if the current job is done 
			if (checkDone(filters.get(num))) {
				done.add(num);
				continue; 
			}
			System.out.println("	" + num + ". " + back.get(num));
		}
		// remove done commands
		for (int num: done) {
			removeCommand(num);
		}	
	}
	
	
	/**
	 * Check if the command is done 
	 * @param command the head command
	 * @return
	 */
	public static boolean checkDone(ConcurrentFilter command) {
		// get the final command 
		while (command.getNext() != null) {
			command = (ConcurrentFilter) command.getNext();
		}
		return command.isDone(); 
	}
	
	
	/**
	 * Kills a thread
	 * @param jobID the thread's key in the maps 
	 */
	public static void kill(int jobID) {
		if (jobID >= filterNum) {
			return ; 
		}
		if (!checkDone((ConcurrentFilter) filters.get(jobID))) {
			List<Thread> curThreads = threads.get(jobID);
			// stop all threads 
			for (Thread curThread: curThreads) {
				curThread.interrupt();
			}
			removeCommand(jobID);
		}
	}
	
	
	/**
	 * Creates concurrent commands 
	 * @param command the input command 
	 */
	public static void createConcurrentCommand(String command) {
		command = removeBackSign(command); // remove the "&"
		ConcurrentFilter filterlist = ConcurrentCommandBuilder.createFiltersFromCommand(command);
		filters.put(filterNum, filterlist); // add to the map (command head)
		ArrayList<Thread> curThread = new ArrayList<Thread>();
		while (filterlist != null) {
			Thread curT = new Thread(filterlist);
			curThread.add(curT);
			curT.start();
			filterlist = (ConcurrentFilter) filterlist.getNext();
		}
		threads.put(filterNum, curThread); // add to the map (thread list) 
		filterNum ++; 
	}
	
	
	/**
	 * Checks if the current command is a background command
	 * @param command the input command 
	 * @return true if it is a background command, false if it is not 
	 */
	public static boolean isBackground(String command) {
		if (command == null || command.length() == 0) {
			return false; 
		}
		return command.charAt(command.trim().length() - 1) == '&';
	}
	
	
	/**
	 * Removes the background command
	 * @param command the input command
	 * @return a command without the background sign 
	 */
	public static String removeBackSign(String command) {
		String[] commands = command.split("&");
		return commands[0].trim();
	}
	
	
	/**
	 * Checks if the current command is repl_jobs, and executes if it is
	 * @param command the input command
	 * @return true if it is repl_jobs, false if it is not 
	 */
	public static boolean checkRepl(String command) {
		if(command.trim().equals("repl_jobs")) {
			getReplJobs();
			return true;
		}
		return false; 
	}
	
	
	/**
	 * Checks if the current command is kill 
	 * @param command the input command
	 * @return true if it is kill, false if it is not 
	 */
	public static boolean checkKill(String command) {
		String[] checkKill = command.split("\\s+");
		if (checkKill[0].trim().equals("kill")) {
			// invalid number of parameter 
			if (checkKill.length == 1) {
				System.out.print(Message.REQUIRES_PARAMETER.with_parameter("kill"));
				return true;
			}
			try {
				kill(Integer.parseInt(checkKill[1]));
			}
			// if the number behind the kill is not an integer 
			catch (NumberFormatException e) {
				System.out.print(Message.INVALID_PARAMETER.with_parameter("kill" + " " + checkKill[1]));
			}
			return true;
		}
		return false; 
	}
	
	
	/**
	 * Creates normal commands 
	 * @param command the input command
	 */
	public static void createNormalCommand(String command) {
		ConcurrentFilter filterlist = ConcurrentCommandBuilder.createFiltersFromCommand(command);
		LinkedList<Thread> list = new LinkedList<Thread>(); // list for the current command
		while(filterlist != null) {
			Thread curThread = new Thread(filterlist);
			list.add(curThread);
			curThread.start();
			filterlist = (ConcurrentFilter) filterlist.getNext();
		}
		for (Thread l: list) {
			try {
				l.join(); // parent will wait for child to complete 
			} 
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	
	/**
	 * Removes the command from the maps
	 * @param num the command ID 
	 */
	public static void removeCommand(int num) {
		back.remove(num);
		threads.remove(num);
	}
	
}
