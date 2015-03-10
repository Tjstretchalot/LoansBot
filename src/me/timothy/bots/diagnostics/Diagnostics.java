package me.timothy.bots.diagnostics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import me.timothy.bots.LoansBotUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles assessing the runtime environment, such as 
 * memory, cputime, uptime, etc. and writing it to a file
 * 
 * @author Timothy
 *
 */
public class Diagnostics {
	@SuppressWarnings("unused")
	private Logger logger;
	private DateFormat dateFormatter;
	private File logFile;
	
	private long firstCreated;
	private long timeMSLast;
	
	private long memoryFreeLast;
	/**
	 * Initializes the diagnostics to append
	 * to the specified file.
	 * 
	 * @param file the file to append to
	 */
	public Diagnostics(File file) {
		logger = LogManager.getLogger();
		logFile = file;
		
		dateFormatter = DateFormat.getDateTimeInstance();
		
		firstCreated = System.currentTimeMillis();
		onInitialize();
	}
	
	/**
	 * Appends starting info, such as available
	 * processors, free memory, max memory, and total
	 * memory
	 */
	private void onInitialize() {
		
		Runtime runtime = Runtime.getRuntime();
		
		int avProcessors = runtime.availableProcessors();
		long freeMem = runtime.freeMemory();
		long maxMem = runtime.maxMemory();
		long totalMem = runtime.totalMemory();
		
		memoryFreeLast = freeMem;
		timeMSLast = firstCreated;
		
		Date startDate = new Date(firstCreated);
		
		try(FileWriter fw = new FileWriter(logFile, true)) {
			fw.append(String.format(
					"\n\n=== LoanBot Started ===\n" +
					"Date: %s\n" +
					"Available Processors: %d\n" +
					"Free Memory: %d (%f%% utilization)\n" +
					"Max Memory: %d\n",
					dateFormatter.format(startDate), avProcessors, freeMem, ((double)freeMem/maxMem) * 100., maxMem, totalMem));
		}catch(IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Dumps diagnostics to file and potentially gives
	 * memory warnings;
	 */
	public void diagnose() {
		Runtime runtime = Runtime.getRuntime();
		
		long maxMemory = runtime.maxMemory();
		long freeMemory = runtime.freeMemory();
		double percFreeMemory = ((double) freeMemory / maxMemory) * 100.;
		long freeMemDiffFromLast = freeMemory - memoryFreeLast;
		long elapsedMs = System.currentTimeMillis() - timeMSLast;
		long uptime = System.currentTimeMillis() - firstCreated;
		
		String elapsedTimeFormatted = LoansBotUtils.formatInterval(elapsedMs);
		String uptimeFormatted = LoansBotUtils.formatInterval(uptime);

		try(FileWriter fw = new FileWriter(logFile, true)) {
			fw.append(String.format(
					"--Tick--\n" +
					"Elapsed since last tick: %s\n" +
					"Uptime total: %s\n" +
					"Free memory: %d (%f%% utilization) (diff from las (pos is bad): %d)\n" +
					"Max Memory: %d\n",
					elapsedTimeFormatted, uptimeFormatted, freeMemory, percFreeMemory, freeMemDiffFromLast, maxMemory));
		}catch(IOException ex) {
			throw new RuntimeException(ex);
		}
		
		memoryFreeLast = freeMemory;
		timeMSLast = System.currentTimeMillis();
	}
}
