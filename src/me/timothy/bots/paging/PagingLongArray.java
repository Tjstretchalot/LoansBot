package me.timothy.bots.paging;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Random;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class acts much like a long array, except that it has explicit
 * file paging over a certain size. This has specific support for sorting,
 * and is meant for data analysis for arrays which will certainly not exceed
 * disk space capacity but may blow up memory.
 * 
 * This will only use a small amount of memory in excess of the array. This 
 * class must be disposed when completed.
 * 
 * This class assumes a write then save then read model.
 * 
 * @author Timothy
 */
public class PagingLongArray {
	private static final Logger logger = LogManager.getLogger();
	
	private final String FILE_EXTENSION = ".dat";
	
	/**
	 * The maximum size of the array that we can hold in memory.
	 */
	private int maxArraySize;
	
	/**
	 * The current array we have in memory. It's length tells the capacity,
	 * memoryArrayLength tells how much we've used so far.
	 */
	private long[] memoryArray;
	
	/**
	 * How long the memory array length is
	 */
	private int memoryArrayLength;
	
	/**
	 * What index in the entire array does memoryArray[0] correspond with
	 */
	private int memoryArrayOffset;
	
	/**
	 * If the memory array is NOT SAVED and is AFTER the saved part
	 */
	private boolean memoryArrayIsTail;
	
	/**
	 * If this array has been paged yet
	 */
	private boolean paged;
	
	/**
	 * The folder which contains temporary files
	 */
	private Path tempFolder;
	
	/**
	 * The file that is being used for paging
	 */
	private Path pageFile;
	
	/**
	 * How big this array is in total.
	 */
	private int size;
	
	/**
	 * If this object has already been disposed
	 */
	private boolean disposed;
	
	/**
	 * Create a new paging long array that is in write mode and holds
	 * an array no greater than the specified size in memory.
	 * 
	 * @param maxArraySize maximum array size
	 */
	public PagingLongArray(int maxArraySize) {
		maxArraySize = (maxArraySize / 8) * 8;
		if(maxArraySize <= 0) {
			throw new RuntimeException("Max array size (rounded down to next multiple of 8) of " + maxArraySize + " is not valid (must be strictly positive)");
		}
		this.maxArraySize = maxArraySize;
	}
	
	/**
	 * Saves the currently loaded array to the given path
	 * @param path the path to the file to save to
	 * @param append true to append, false not to
	 * @throws IOException if one occurs
	 */
	private void saveTo(Path path, boolean append) throws IOException {
		try (DataOutputStream stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path.toFile(), append)))) {
			for(int i = 0; i < memoryArrayLength; i++) {
				stream.writeLong(memoryArray[i]);
			}
		}
	}
	
	/**
	 * Load the memory array from the given path.
	 * @param path the path to load from
	 * @param seek the number of values to skip (negative to count backward from end)
	 * @param count the number of values to load (-1 for all)
	 * @param forceKeepArray if this is true,, this function will not delete the memory array
	 * @param arrayOffset the first index in memoryArray that is written. Only meaningful if forceKeepArray is true
	 * @throws IOException if one occurs
	 */
	private void loadFrom(Path path, int seek, int count, boolean forceKeepArray, int arrayOffset) throws IOException {
		if(count != -1 && count < 1)
			throw new IllegalArgumentException("Cannot fetch less than 0 things (count=" + count + ")");
		
		long lengthBytes = Files.size(path);
		int lengthLongs = (int)(lengthBytes / 8);
		
		if(seek < 0) {
			if(seek < -lengthLongs)
				throw new IllegalArgumentException(String.format("Cannot seek to before the beggining (there are %d values but you asked to seek to %d)", lengthLongs, seek));
			seek = lengthLongs - seek;
		}else {
			if(seek > lengthLongs)
				throw new IllegalArgumentException(String.format("Cannot seek to after the end (there are %d values but you asked to seek to %d)", lengthLongs, seek));
		}
		
		if(count == -1)
			count = lengthLongs;
		
		if(memoryArray == null || memoryArray.length < arrayOffset + count) {
			if(forceKeepArray)
				throw new IllegalArgumentException(String.format("Cannot keep the array because there is not enough space. Need to write to indexes %d to %d but array length = %d", 
						arrayOffset, arrayOffset + count, (memoryArray == null ? -1 : memoryArray.length)));
			
			memoryArray = new long[count];
		}
		
		try (DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(path.toFile())))) {
			if(seek > 0)
				stream.skipBytes(seek * 8);
			
			for(int i = 0; i < count; i++) {
				memoryArray[arrayOffset + i] = stream.readLong();
			}
		}catch(EOFException e) {
			// for whatever reason these have no message. let's add one!
			throw new EOFException("reached end of file (file size = " + lengthBytes + "bytes / " + lengthLongs + "longs)");
		}
		
		memoryArrayLength = count;
	}
	/**
	 * Save the memory array to file and create a new empty one in its place,
	 * updating the fileCounter and memoryArrayOffset. 
	 * @throws IOException if one occurs
	 */
	private void page() throws IOException {
		if(!memoryArrayIsTail)
			throw new IllegalStateException("cannot page when not at tail");
		
		if(memoryArrayLength == 0)
			return;
		
		if(!paged) {
			paged = true;
			Path basePath = FileSystems.getDefault().getPath("temp");
			
			Files.createDirectories(basePath);
			
			Random rnd = new Random();
			
			Path myBasePath = null;
			while(myBasePath == null) {
				int uuid = rnd.nextInt(Integer.MAX_VALUE);
				
				Path path = Paths.get(basePath.toString(), Integer.toHexString(uuid));
				if(!Files.exists(path))
					myBasePath = path;
			}
			
			tempFolder = myBasePath.toAbsolutePath().normalize();
			
			Files.createDirectories(tempFolder);
			
			pageFile = Paths.get(tempFolder.toString(), "page" + FILE_EXTENSION);
			Files.createFile(pageFile);
		}
		
		saveTo(pageFile, true);

		memoryArrayOffset = size;
		memoryArray = new long[maxArraySize];
		memoryArrayLength = 0;
		memoryArrayIsTail = true;
	}
	
	/**
	 * Add the given long to this array
	 * @param val the value to add
	 */
	public void add(long val) {
		if(disposed)
			throw new IllegalStateException("this object has been disposed!");
		
		if(memoryArray == null || !memoryArrayIsTail) {
			memoryArray = new long[maxArraySize];
			memoryArrayLength = 0;
			memoryArrayIsTail = true;
			memoryArrayOffset = size;
		}else if(memoryArray.length == memoryArrayLength) {
			try {
				page();
			} catch (IOException e) {
				logger.throwing(e);
				throw new RuntimeException(e);
			}
		}
		
		memoryArray[memoryArrayLength++] = val;
		size++;
	}
	
	/**
	 * Merges the two input files into the output file
	 * 
	 * @param in1 first in file, sorted 
	 * @param in2 second in file, sorted
	 * @param out the output file, which will be sorted
	 */
	private void mergeFiles(Path in1, Path in2, Path out) {
		try {
			int numLongs1 = (int) (Files.size(in1) / 8);
			int numLongs2 = (int) (Files.size(in2) / 8);
			
			int index1 = 1;
			int index2 = 1;
			long value1 = 0;
			long value2 = 0;
			
			try(DataInputStream instream1 = new DataInputStream(new BufferedInputStream(new FileInputStream(in1.toFile())))) {
				try(DataInputStream instream2 = new DataInputStream(new BufferedInputStream(new FileInputStream(in2.toFile())))) {
					try(DataOutputStream outstream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(out.toFile())))) {
						value1 = instream1.readLong();
						value2 = instream2.readLong();
						
						while(true) {
							if(value1 < value2) {
								outstream.writeLong(value1);
								index1++;
								
								if(index1 > numLongs1)
									break;
								
								value1 = instream1.readLong();
							}else {
								outstream.writeLong(value2);
								index2++;
								
								if(index2 > numLongs2)
									break;
								
								value2 = instream2.readLong();
							}
						}
						
						if(index1 <= numLongs1) {
							while(true) {
								outstream.writeLong(value1);
								index1++;
								if(index1 > numLongs1)
									break;
								value1 = instream1.readLong();
							}
						}else if(index2 <= numLongs2) {
							while(true) {
								outstream.writeLong(value2);
								index2++;
								if(index2 > numLongs2)
									break;
								value2 = instream2.readLong();
							}
						}
					}
				}
			} 
		}catch (IOException e) {
			logger.printf(Level.ERROR, "mergeFiles(in1 = %s, in2 = %s, out = %s) failed with error: %s", 
					in1.toString(), in2.toString(), out.toString(), e.getMessage());
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Merges the memory array with the given input file and outputs to the out file.
	 * 
	 * @param other the other input file, sorted
	 * @param out the file to merge into
	 */
	private void mergeWithMemory(Path other, Path out) {
		try {
			int numLongs = (int)(Files.size(other) / 8);
			
			int indexMemory = 1;
			int indexOther = 1;
			long valueMemory = 0;
			long valueOther = 0;
			
			try(DataInputStream otherStream = new DataInputStream(new BufferedInputStream(new FileInputStream(other.toFile())))) {
				try(DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(out.toFile())))) {
					valueMemory = memoryArray[0];
					valueOther = otherStream.readLong();

					while(true) {
						if(valueMemory < valueOther) {
							outStream.writeLong(valueMemory);
							indexMemory++;
							
							if(indexMemory > memoryArrayLength)
								break;
							
							valueMemory = memoryArray[indexMemory - 1];
						}else {
							outStream.writeLong(valueOther);
							indexOther++;
							
							if(indexOther > numLongs)
								break;
							
							valueOther = otherStream.readLong();
						}
					}
					
					if(indexMemory <= memoryArrayLength) {
						while(true) {
							outStream.writeLong(valueMemory);
							indexMemory++;
							if(indexMemory > memoryArrayLength)
								break;
							valueMemory = memoryArray[indexMemory - 1];
						}
					}else if(indexOther <= numLongs) {
						while(true) {
							outStream.writeLong(valueOther);
							indexOther++;
							if(indexOther > numLongs)
								break;
							valueOther = otherStream.readLong();
						}
					}
				}
			}
		}catch (IOException e) {
			logger.printf(Level.ERROR, "mergeWithMemory(other = %s, out = %s) failed with error: %s", 
					other.toString(), out.toString(), e.getMessage());
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * The the path that can be used for the specified power of 2 
	 * @param powerOf2
	 * @return
	 */
	private Path getPathForSort(int powerOf2) {
		return Paths.get(tempFolder.toString(), Integer.toString(powerOf2) + FILE_EXTENSION);
	}
	
	private Path getTempSortPath(int counter) {
		return Paths.get(tempFolder.toString(), "temp_" + Integer.toHexString(counter) + FILE_EXTENSION);
	}
	
	/**
	 * Sorts this array in ascending order. This will use a merge sort on files and the default
	 * sort implementation for in memory
	 */
	public void sort() {
		if(memoryArrayIsTail) {
			if(!paged) {
				Arrays.sort(memoryArray, 0, memoryArrayLength);
				return;
			}
			
			try {
				page();
			} catch (IOException e) {
				logger.printf(Level.ERROR, "Sorting required paging (at tail), but failed with error: %s", e.getMessage());
				logger.throwing(e);
				throw new RuntimeException(e);
			}
		}
		
		/*
		 * File sort:
		 * 
		 * Take first block, dump into first
		 * Take second block, merge with first into second. first is now empty
		 * Take third block, dump with first
		 * Take fourth block, move second into third. merge with first into second. merge second with third into fourth. move fourth to third
		 * 
		 * ---
		 * 
		 * [START]
		 * first empty -> dump to first
		 * first full -> second empty; merge memory and first to second.
		 * first empty -> dump to first
		 * first full -> second full -> third empty; move second to temp. merge memory and first to second. merge second and temp to third
		 * first empty -> dump to first
		 * first full -> second empty; merge memory and first to second.
		 * first empty -> dump to first
		 * first full -> second full -> third full -> fourth empty; move second to temp. move third to temp2. merge memory and first to second, 
		 * 														    merge second and temp to third. merge third and temp2 to fourth.
		 * first empty -> dump to first
		 * first full -> second empty; merge memory and first to second
		 * first empty -> dump to first
		 * NO MORE BLOCKS
		 * first full -> second full -> merge first and second to temp. move temp to second. second full, third empty -> move second to third. 
		 * 								third full, fourth full -> merge third and fourth to temp. move temp to fourth. NO MORE -> move temp 
		 * 								page file
		 * [FINISH]
		 */
		
		// we are going to use the first two files a lot
		Path firstSort = getPathForSort(0);
		Path secondSort = getPathForSort(1);
		
		try {
			int totalBlocks = (int)Math.ceil(size / (double)maxArraySize);
			
			if(totalBlocks <= 0) // if this happens we will loop forever
				throw new IllegalStateException("something has gone wrong; totalBlocks = " + totalBlocks);
			
			for(int block = 0; block < totalBlocks; block++) {
				loadFrom(pageFile, block * maxArraySize, Math.min(maxArraySize, size - (block*maxArraySize)), false, 0);
				Arrays.sort(memoryArray, 0, memoryArrayLength);
				if((block & 1) == 0) {
					// first empty -> dump to first
					saveTo(firstSort, false);
				}else if((block & (1 << 1)) == 0) {
					// first full -> second empty; merge memory and first to second
					mergeWithMemory(firstSort, secondSort);
				}else {
					int fileNum;
					for(fileNum = 1; (block & (1 << fileNum)) != 0; fileNum++) {
						Files.move(getPathForSort(fileNum), getTempSortPath(fileNum - 1), StandardCopyOption.REPLACE_EXISTING);
					}
					int lastMoved = fileNum - 1;
					mergeWithMemory(firstSort, secondSort);
					for(fileNum = 1; fileNum <= lastMoved; fileNum++) {
						mergeFiles(getPathForSort(fileNum), getTempSortPath(fileNum - 1), getPathForSort(fileNum + 1));
					}
				}
			}
			
			int lastBlock;
			for(lastBlock = 0; (totalBlocks & (1 << lastBlock)) == 0; lastBlock++) {
				Files.deleteIfExists(getPathForSort(lastBlock));
			}
			
			Path tempFile = getTempSortPath(0);
			int block = lastBlock + 1;
			while((1 << block) < totalBlocks) {
				if((totalBlocks & (1 << block)) != 0) {
					mergeFiles(getPathForSort(lastBlock), getPathForSort(block), tempFile);
					Files.delete(getPathForSort(lastBlock));
					lastBlock = block;
					Files.move(tempFile, getPathForSort(block), StandardCopyOption.REPLACE_EXISTING);
				}else {
					Files.delete(getPathForSort(block));
				}
				
				block++;
			}
			
			for(int i = 0; i < totalBlocks; i++) {
				Files.deleteIfExists(getTempSortPath(i));
			}
			
			Files.move(getPathForSort(lastBlock), pageFile, StandardCopyOption.REPLACE_EXISTING);
			loadFrom(pageFile, 0, maxArraySize, false, 0);
			memoryArrayIsTail = false;
			memoryArrayOffset = 0;
		}catch(IOException e) {
			logger.printf(Level.ERROR, "sort() failed during actual sort with message: %s", e.getMessage());
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Fetch the value in this array at the given index
	 * @param index the index to fetch
	 * @return the corresponding value at that index
	 */
	public long get(int index) {
		if(disposed)
			throw new IllegalStateException("this object has been disposed!");
		
		if(memoryArrayOffset <= index && memoryArrayOffset + memoryArrayLength > index) {
			return memoryArray[index - memoryArrayOffset];
		}
		
		if(index < 0 || index >= size)
			throw new IllegalArgumentException(String.format("index is outside of range! index=%d, size=%d", index, size));
		
		if(memoryArrayIsTail) {
			try {
				page();
			} catch (IOException e) {
				logger.printf(Level.ERROR, "Failed to get(%d) - needed to page because not at tail but got error: %s", index, e.getMessage());
				logger.throwing(e);
				throw new RuntimeException(e);
			}
		}
		
		int desiredOffset = (index / maxArraySize) * maxArraySize;
		int numToLoad = Math.min(maxArraySize, size - desiredOffset);
		
		try {
			loadFrom(pageFile, desiredOffset, numToLoad, false, 0);
		} catch (IOException e) {
			logger.printf(Level.ERROR, "Failed to get(%d) - wanted to do loadFrom(%s, %d, %s) but got error: %s", index, pageFile.toString(), desiredOffset, numToLoad, e.getMessage());
			logger.throwing(e);
			throw new RuntimeException(e);
		}
		
		memoryArrayIsTail = false;
		memoryArrayOffset = desiredOffset;
		return memoryArray[index - memoryArrayOffset];
	}
	
	/**
	 * How many values are in this array.
	 * @return the number of values in this array.
	 */
	public int size() {
		return size;
	}
	
	/**
	 * Dispose of this object and any backing file stores
	 */
	public void dispose() {
		memoryArray = null;
		memoryArrayLength = 0;
		disposed = true;
		
		if(paged) {
			try {
				Files.delete(pageFile);
				Files.delete(tempFolder);
			} catch (IOException e) {
				logger.printf(Level.ERROR, "Failed to delete temporary long value folder at %s: %s", tempFolder.toString(), e.getMessage());
				logger.catching(e);
			}
		}
	}
}
