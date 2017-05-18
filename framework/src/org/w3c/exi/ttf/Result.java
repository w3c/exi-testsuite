/*
 * EXI Testing Task Force Measurement Suite: http://www.w3.org/XML/EXI/
 *
 * Copyright © [2006] World Wide Web Consortium, (Massachusetts Institute of
 * Technology, European Research Consortium for Informatics and Mathematics,
 * Keio University). All Rights Reserved. This work is distributed under the
 * W3C® Software License [1] in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */

package org.w3c.exi.ttf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

import org.w3c.exi.ttf.parameters.DriverParameters;
import org.w3c.exi.ttf.parameters.MeasureParam;
import org.w3c.exi.ttf.parameters.TestCaseParameters;

import com.sun.japex.Constants;
import com.sun.japex.Params;

/**
 * Result utility manages MANIFEST and the results
 * including CopyResult feature support.
 */
public final class Result {
	/**
	 * Candidate name for this report
	 */
	private final String candidate;
	/**
	 * Time stamp for CopyResult record
	 */
	private final String timestamp;
	/**
	 * Base directory for EXI encoded files
	 */
	private final File exiBaseDir;
	/**
	 * Report directory
	 */
	private final File reportDir;
	/**
	 * Depot directory
	 */
	public final File depotDir;
	/**
	 * Report directory for this candidate
	 */
	private File resultDir;
	/**
	 * MANIFEST file
	 */
	private File manifestFile;
	/**
	 * Manifest writer
	 */
	private BufferedWriter manifestWriter;
	/**
	 * Newly generated results
	 */
	private ArrayList<CopyItem> results;
	/**
	 * Record a new result
	 */
	private class CopyItem {
		final String path;
		final String line;

		CopyItem(String key, String value) {
			this.path = key;
			this.line = value;
		}
	}

	/**
	 * Date formatter for the copyResult record
	 */
	private static final SimpleDateFormat TIME_STAMP_RECORD
			= new SimpleDateFormat("\tyyyy-MM-dd HH:mm:ss");
	
	/**
	 * Date formatter for the reporting directory
	 * 
	 * This format is the same format which Japex output
	 */
	public static final SimpleDateFormat TIME_STAMP_REPORT
			= new SimpleDateFormat("yyyy_MM_dd_HH_mm");
	
	/**
	 * Date formatter for the result directory
	 */
	private static final SimpleDateFormat TIME_STAMP_RESULT
			= new SimpleDateFormat("_yyyy-MM-dd_HH_mm_ss");
	
	/**
	 * Encoding character set name for result files
	 */
	private static final String FILE_CHARSET_NAME = "UTF-8";
	
	/**
	 * Paramemter prefix
	 */
	private static final String FRAMEWORK_PREFIX = "org.w3c.exi.ttf.framework.";
	
	/**
	 * Feature name for CopyResult
	 */
	private static final String FEATURE_COPY_RESULT = FRAMEWORK_PREFIX + "copyResult";
	
	/**
	 * Feature flag for CopyResult
	 */
	private static final boolean feature_copy_result
			= Boolean.getBoolean(FEATURE_COPY_RESULT);
	
	/**
	 * Property name for Depot Directory
	 */
	private static final String PROPERTY_DEPOT_DIR = FRAMEWORK_PREFIX + "depotDir";
	
	/**
	 * Property value for Depot Directory
	 */
	private static final File explicitDepotDir;
	
	static {
		String value = System.getProperty(PROPERTY_DEPOT_DIR);
		if (value == null || value.length() == 0) {
			explicitDepotDir = null;
		} else {
			explicitDepotDir = new File(value);
		}
	}
	
	/**
	 * Directory name for the result depot 
	 */
	public static final String DEFAULT_DEPOT_DIR_NAME = "result";
	
	/**
	 * File name for MANIFEST
	 */
	public static final String MANIFEST_FILE_NAME = "MANIFEST";
	
	/**
	 * File name for CHANGES
	 */
	public static final String CHANGES_FILE_NAME = "CHANGES";
	
	/**
	 * Directory name for EXI input data
	 */
	public static final String LOCATION_FILE_NAME = "LOCATION";
	
	/**
	 * Lock file name for the result depot
	 */
	private static final String DEPOT_LOCK_FILE_NAME = "depot.lock";
	
	/**
	 * Verbose option
	 */
	private final boolean verbose;
	
	/**
	 * Debug option
	 */
	private final boolean debug;
	
	/**
	 * Initialize driver and report	
	 * @param candidate a candidate name
	 * @param timestamp a time stamp for this test
	 * @param exiBaseDir a base directory for EXI encoded files
	 * @param testSuite a Japex test suite
	 */
	public Result(String candidate, Date timestamp,
			Params testSuite, DriverParameters driverParams) {
		this.candidate = candidate;
		this.timestamp = TIME_STAMP_RECORD.format(timestamp);
		this.exiBaseDir = driverParams.exiDataBaseDir;
		
		// framework options
		verbose = (DriverParameters._frameworkCheck.indexOf(",copying,") >= 0);
		debug = (DriverParameters._frameworkDebug.indexOf(",copyResult,") >= 0);
		
		try {
			/*
			 * Prepare report directory
			 */
			final String reportPath = testSuite.getParam(Constants.REPORTS_DIRECTORY);

			reportDir = new File(reportPath);
			reportDir.mkdirs();

			// Create the report directory for this candidate
			String resultPath = ""
				+ TIME_STAMP_REPORT.format(timestamp)
				+ File.separator
				+ candidate
				+ TIME_STAMP_RESULT.format(timestamp);
			resultDir = new File(reportDir, resultPath);
			resultDir.mkdirs();

			/*
			 * Record EXI data location
			 */
			final boolean iot_decode = (driverParams.measure == MeasureParam.iot_decode);
			if  (iot_decode) {
				if (validDataDir(exiBaseDir)) {
					BufferedWriter writer = getFileWriter(new File(resultDir, LOCATION_FILE_NAME));
					writer.write(exiBaseDir.toURI().toURL().toString());
					writer.newLine();
					writer.close();
				} else {
					throw new RuntimeException(
							"Bad EXI data directory: " + exiBaseDir.getAbsolutePath());
				}
			}
			
			/*
			 * Prepare MANIFEST
			 */
			manifestFile = new File(resultDir, MANIFEST_FILE_NAME);
			manifestWriter = getFileWriter(manifestFile);
			
			/*
			 * Prepare CopyResult
			 */
			if (iot_decode || !feature_copy_result) {
				// disable Copy Result
				results = null;
				if (debug) {
					System.err.print("Copy Result is not activated for ");
					System.err.print(candidate);
				}
			} else {
				// enable Copy Result only when being executed by a single run in a single thread 
				final int nThreads = testSuite.getIntParam(com.sun.japex.Constants.NUMBER_OF_THREADS);
				final int nRuns = testSuite.getIntParam(com.sun.japex.Constants.RUNS_PER_DRIVER);
				results = ((nThreads == 1 && nRuns == 1)
						? new ArrayList<CopyItem>() : null);
				if (verbose) {
					System.out.printf("Copy Result is disabled for %s due to Runs=%d and Threads=%d%n",
							candidate, nRuns, nThreads);
				}
				if (debug) {
					System.err.printf("CopyResult: %s for %s (Threads=%d Runs=%d)%n",
							((results == null) ? "disabled" : "enabled"),
							candidate,
							nThreads, nRuns);
				}
			}
			
			/**
			 * Set the depot directory
			 */
			if (explicitDepotDir != null) {
				depotDir = new File(explicitDepotDir, candidate);
			} else {
				depotDir = new File(reportDir,
						(DEFAULT_DEPOT_DIR_NAME + File.separator + candidate));
			}
			depotDir.mkdirs();
			if (debug) {
				System.err.print("DepotDir: ");
				System.err.println(depotDir.getAbsolutePath());
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("INTERNAL ERROR: This should not happen but ...", e);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("INTERNAL ERROR: This should not happen but ...", e);
		} catch (MalformedURLException e) {
			throw new RuntimeException("INTERNAL ERROR: This should not happen but ...", e);
		} catch (IOException e) {
			throw new RuntimeException("Unexpected I/O error", e);
		}
	}

	/**
	 * Get the MANIFEST file
	 * @return The MANIFEST file
	 */
	public File getManifest() {
		return manifestFile;
	}

	/**
	 * Write a line into MANIFEST and keep it for CopyResult
	 * @param testName Test name to write and keep
	 * @param fileName File name to write and keep
	 */
	public void manifest(String testName, String fileName) {
		try {
			manifestWriter.write(addResult(testName, fileName));
			manifestWriter.newLine();
		} catch (IOException e) {
			throw new RuntimeException("Unexpected I/O error", e);
		}
	}
	
	/**
	 * Add results
	 * @param testName test name
	 * @param fileName result file name
	 * @return a MANIFEST string
	 */
	private String addResult(String testName, String fileName) {
		String line = fileName + "\t" + testName;
		if (results != null) {
			results.add(new CopyItem(fileName, line));
		}
		return line;
	}

	
	/**
	 * Complete MANIFEST and execute CopyResult
	 */
	public void complete() {
		try {
			manifestWriter.close();
			updateDepot();
		} catch (IOException e) {
			throw new RuntimeException("Unexpected I/O error", e);
		}
	}	

	
	/**
	 * Copy new results to the result depot if CopyResult feature is enabled
	 * @throws IOException 
	 */
	private void updateDepot() throws IOException{
		if (results == null || results.size() <= 0) {
			return;
		}
		File depotDir = this.depotDir;
		System.out.printf("Orignal results are located at%n  %s%n"
				+ "Copying results ...%n  %s%n",
				resultDir.toURI().toString(),
				depotDir.toURI().toString());

		// update the result depot
		File copying = new File(depotDir, DEPOT_LOCK_FILE_NAME);
		FileOutputStream depotLock = null;
		try {
			// make sure the result depot exists
			depotDir.mkdirs();
			// make sure to release the lock on unexpected exit 
			copying.deleteOnExit();
			// lock the depot
			depotLock = new FileOutputStream(copying);
			depotLock.getChannel().lock();
			depotLock.write(resultDir.getAbsolutePath().getBytes(FILE_CHARSET_NAME));
			depotLock.flush();
			// copy results
			copyResults(depotDir);
			updateChanges(depotDir);
			updateManifest(depotDir);
		} finally {
			if (depotLock != null) depotLock.close();
		}
		copying.delete();
	}

	/**
	 * Copy newly created results
	 * @throws IOException
	 */
	private void copyResults(File depotDir) throws IOException {
		int size = results.size();
		for (int i = 0; i < size; i++) {
			CopyItem r = results.get(i);
			final String path = r.path;
			if (verbose) {
				System.out.println("    " + path);
			}
			copyResult(new File(resultDir, path), new File(depotDir, path));
		}
		
		System.out.printf("  %d file%s%n",
				size, ((size > 1) ? "s" : ""));
	}
	
	/**
	 * Copy a result to depot
	 * @param src Source file
	 * @param dst Destination file
	 * @throws IOException
	 */
	private void copyResult(File src, File dst) throws IOException {
		dst.getParentFile().mkdirs();
		copyFile(src, dst, false);
	}

	/**
	 * Update MANIFEST in the depot
	 * @throws IOException
	 */
	private void updateManifest(File depotDir) throws IOException {
		File manifest = new File(depotDir, MANIFEST_FILE_NAME);
		System.out.println("Updating MANIFEST ...");
		
		// Sort new results
		TreeMap<String,String> items = new TreeMap<String,String>();
		for (int i = results.size(); --i >= 0;) {
			CopyItem r = results.get(i);
			items.put(r.path, r.line + timestamp);   		
		}
		Iterator<String> result = items.values().iterator();
		
		// Merge new results to MANIFEST
		if (manifest.createNewFile()) {
			// This depot is new depot
			createManifest(manifest, result);
		} else {
			// Need to merge the new and the old contents
			File newManifest = new File(depotDir, MANIFEST_FILE_NAME + ".tmp");
			mergeManifest(manifest, newManifest, result);
			manifest.delete();
			newManifest.renameTo(manifest);
		}
	}

	/**
	 * Create a new MANIFEST file
	 * @param manifest A MANIFEST file
	 * @param iHave New contents
	 * @throws IOException
	 */
	private void createManifest(File manifest, Iterator<String> result)
			throws IOException {
		BufferedWriter m = null;
		try {
			m = getFileWriter(manifest);
			while (result.hasNext()) {
				m.write(result.next());
				m.newLine();
			}
		} finally {
			if (m != null) m.close();
		}
	}
	
	/**
	 * Create a new MANIFEST file with merging old and new results
	 * @param manifest Current MANIFEST file
	 * @param merged New MANIFEST file
	 * @param result New results
	 * @throws IOException
	 */
	private void mergeManifest(File manifest, File merged,
			Iterator<String> result) throws IOException {
		BufferedReader i = null;
		BufferedWriter o = null;
		try {
			i = getFileReader(manifest);
			try {
				o = getFileWriter(merged);
				/*
				 * existing results are already sorted,
				 * merge new results into existing results
				 * one by one while reading existing results
				 */
				String line = null;
				boolean hasRemaining = true;
				while (result.hasNext()) {
					// Get a new result
					String insert = result.next();
					// Get an existing result
					if (line == null && hasRemaining) {
						line = i.readLine();
					}
					// Find the insert point
					if (line != null) {
						int cmp = compareItem(insert, line);
						// Skip to the insert point
						while (cmp > 0) {
							o.write(line);
							o.newLine();
							line = i.readLine();
							if (line == null) {
								hasRemaining = false;
								break;
							}
							cmp = compareItem(insert, line);
						}
						// Abandon the old result if same
						if (cmp == 0) {
							line = null;
						}
					} else {
						hasRemaining = false;
					}
					// Insert a new result
					o.write(insert);
					o.newLine();
				}
				/*
				 * no more new results,
				 * flush rest of old results
				 */
				if (hasRemaining) {
					char[] b = new char[8192];
					int z;
					while ((z = i.read(b)) > 0) {
						o.write(b, 0, z);
					}			
				}
			} finally {
				if (o != null) o.close();
			}
		} finally {
			if (i != null) i.close();
		}
	}
	
	/**
	 * Compare old and new items
	 * @param insert New item
	 * @param line Old item
	 * @return Return 0 if they are same;
	 * Return -1 if the new item goes to first;
	 * Return 1 if the old item goes to first.
	 */
	private int compareItem(String insert, String line) {
		int z = insert.length();
		// Check if they are not same obviously
		if (z != line.length()
				|| (z = insert.indexOf('\t')) < 0
				|| line.charAt(z) != '\t') {
			// They are different and check them
			return insert.compareTo(line);
		}
		// Check precisely because they look same
		for (int i = 0; i < z; i++) {
			int cmp = insert.charAt(i) - line.charAt(i);
			if (cmp != 0) {
				return cmp;
			}
		}
		return 0;
	}
		
	/**
	 * Update a CHANGES file
	 * @throws IOException
	 */
	private void updateChanges(File depotDir) throws IOException {
		File changes = new File(depotDir, CHANGES_FILE_NAME);
		System.out.println("Updating CHANGES ...");
		if (changes.createNewFile()) {
			// New depot
			createChanges(changes, false);
		} else {
			// Need to be updated
			File newChanges = new File(depotDir, CHANGES_FILE_NAME + ".tmp");
			createChanges(newChanges, true);
			copyFile(changes, newChanges, true);
			changes.delete();
			newChanges.renameTo(changes);
		}
	}

	/**
	 * Create a new CHANGES file from the new results
	 * @param newChanges A new CHANGES file
	 * @param update If true, emit the empty line at the end.
	 * Otherwise, no empty line.
	 * @throws IOException
	 */
	private void createChanges(File changes, boolean update)
			throws IOException {
		PrintWriter c = null;
		try {
			c = new PrintWriter(getFileWriter(changes));
			int size = results.size();
			c.printf("%s\t(%d file%s)%n",
					timestamp.substring(1),
					size, ((size > 1) ?"s" : ""));
			for (int i = 0; i < size; i++) {
				CopyItem r = results.get(i);
				c.printf("\t%s%n", r.line);
			}
			if (update) {
				c.println();
			}
		} finally {
			if (c != null) c.close();
		}
	}
	

	/**
	 * Get a writer for the file using the default encoding
	 * @param file an output file
	 * @return a writer
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 */
	private BufferedWriter getFileWriter(File file)
			throws UnsupportedEncodingException, FileNotFoundException {
		return new BufferedWriter(
				new OutputStreamWriter(
						new FileOutputStream(file), FILE_CHARSET_NAME));
	}
	
	/**
	 * Get a reader for the file using the default encoding
	 * @param file an input file
	 * @return a reader
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 */
	private BufferedReader getFileReader(File file)
			throws UnsupportedEncodingException, FileNotFoundException {
		return new BufferedReader(
				new InputStreamReader(
						new FileInputStream(file), FILE_CHARSET_NAME));
	}
	
	/**
	 * Copy the source file contents to the destination file
	 * @param src a source file
	 * @param dst a destination file
	 * @param append append contents if {@code true}; otherwise overwrite
	 * @throws IOException
	 */
	public static void copyFile(File src, File dst, boolean append)
			throws IOException {
		FileChannel i = null;
		FileChannel o = null;
		try {
			i = new FileInputStream(src).getChannel();
			try {
				o = new FileOutputStream(dst, append).getChannel();
				i.transferTo(0, i.size(), o);
			} finally {
				if (o != null) o.close();
			}
		} finally {
			if (i != null) i.close();
		}
	}

	
	/**
	 * Check if the given EXI data directory is valid
	 * @param exiDataDir an EXI data directory
	 * @return {@code true} if the directory looks valid; otherwise {@code false}
	 */
	public boolean validDataDir(File exiDataDir) {
		final File manifest = new File(exiDataDir, MANIFEST_FILE_NAME);
		BufferedReader r = null;
		try {
			if (manifest.length() <= 0) {
				throw new RuntimeException("Zero-length MANIFEST file at "
						+ exiDataDir.getAbsolutePath());
			}
			r = getFileReader(manifest);
			String line;
			while ((line = r.readLine()) != null) {
				final int t = line.indexOf('\t');
				if (t <= 0) {
					if (verbose) {
						System.err.printf("Bad MANIFEST file for %s (missing tab): %s%n",
								candidate, line);
					}
					return false;
				}
				File file = new File(exiDataDir, line.substring(0, t));
				if (!file.isFile()) {
					if (verbose) {
						System.err.printf("Non-existing data file in MANIFEST for %s: %s%n",
								candidate, file.getAbsolutePath());
					}
				} else if (!file.canRead()) {
					if (verbose) {
						System.err.printf("Unreadable data file in MANIFEST for %s: %s%n",
								candidate, file.getAbsolutePath());
					}
				} else if (file.length() <= 0) {
					if (verbose) {
						System.err.printf("Zero-length data file in MANIFEST for %s: %s%n",
								candidate, file.getAbsolutePath());
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("INTERNAL ERROR: This should not happen but ...", e);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("MANIFEST file does not exists at "
					+ exiDataDir.getAbsolutePath(), e);
		} catch (IOException e) {
			throw new RuntimeException("Unexpected I/O error while checking MANIFEST file at "
					+ exiDataDir.getAbsolutePath(), e);
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (IOException e) {
					throw new RuntimeException("Unexpected I/O error after checking MANIFEST file at "
							+ exiDataDir.getAbsolutePath(), e);
				}
			}
		}
		return true;
	}
	
	
	/**
	 * Get a result file manager
	 * @param xmlPath Path for input XML file
	 * @param suffix Suffix for generated file name
	 * @return file name generator
	 */
	public FileManager getFileManager(TestCaseParameters testCase) {
		return new FileManager(exiBaseDir, resultDir,
				testCase.xmlFile,
				testCase._frameworkSuffix);
	}
	
	
	/**
	 * A class for the data file manager
	 */
	public static class FileManager {
		/**
		 * Base directory for the EXI data files
		 */
		private final File exiBaseDir;
		/**
		 * Base directory for the result data files
		 */
		private final File sinkBaseDir;
		/**
		 * Path for EXI file
		 */
		private final String exiPath;
		/**
		 * Path for XML file
		 */
		private final String xmlPath;
		/**
		 * Source for EXI
		 */
		private File exiSource;
		/**
		 * Source for XML
		 */
		private File xmlSource;
		/**
		 * Sink for EXI
		 */
		private File exiSink;
		/**
		 * Sink for XML
		 */
		private File xmlSink;
		

		/**
		 * Initialize the data file manager
		 * @param exiBaseDir
		 * @param basePath
		 * @param suffix
		 */
		private FileManager(File exiBaseDir, File resultDir, String basePath, String suffix) {
			this.exiBaseDir = exiBaseDir;
			this.xmlSource = new File(basePath);
			if (basePath.endsWith(".xml")) {
				basePath = basePath.substring(0, basePath.length()-4);
			}
			this.sinkBaseDir = resultDir;
			this.xmlPath = basePath + ".xml" + suffix;
			this.exiPath = basePath + ".exi" + suffix;
			
			// make sure that the sink directory exists
			new File(resultDir, basePath).getParentFile().mkdirs();
		}

		/**
		 * Get the EXI source file
		 * @return the EXI source file
		 */
		public File getSourceFileEXI() {
			if (exiSource == null) {
				exiSource = new File(exiBaseDir, exiPath);
			}
			return exiSource;
		}
		

		/**
		 * Get the EXI source file
		 * @return the EXI source file
		 */
		public File getSourceFileXML() {
			return xmlSource;
		}
		
		/**
		 * Get the EXI source file
		 * @return the EXI source file
		 */
		public File getSinkFileEXI() {
			if (exiSink == null) {
				exiSink = new File(sinkBaseDir, exiPath);
			}
			return exiSink;
			
		}
		
		/**
		 * Get the EXI source file
		 * @return the EXI source file
		 */
		public File getSinkFileXML() {
			if (xmlSink == null) {
				xmlSink = new File(sinkBaseDir, xmlPath);
			}
			return xmlSink;
		}
		
		/**
		 * Get the EXI source file
		 * @return the EXI source file
		 */
		public String getSinkPathEXI(boolean fullPath) {
			return fullPath ? getSinkFileEXI().getAbsolutePath() : exiPath;
		}
		
		/**
		 * Get the XML source file
		 * @return the XML source file
		 */
		public String getSinkPathXML(boolean fullPath) {
			return fullPath ? getSinkFileXML().getAbsolutePath() : xmlPath;
		}
		
		/**
		 * Get the EXI source file
		 * @return the EXI source file
		 */
		public String getSourcePathEXI(boolean fullPath) {
			return fullPath ? getSourceFileEXI().getAbsolutePath() : exiPath;
		}
		
		/**
		 * Get the EXI source file
		 * @return the EXI source file
		 */
		public String getSourcePathXML(boolean fullPath) {
			return fullPath ? getSourceFileXML().getAbsolutePath() : xmlPath;
		}
	}
}