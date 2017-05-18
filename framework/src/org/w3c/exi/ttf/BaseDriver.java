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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.w3c.exi.ttf.datasink.DataSink;
import org.w3c.exi.ttf.datasource.DataSource;
import org.w3c.exi.ttf.parameters.DriverParameters;
import org.w3c.exi.ttf.parameters.TestCaseParameters;

import com.sun.japex.Constants;
import com.sun.japex.Japex;
import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;
import com.sun.japex.TestCaseImpl;

/**
 * Base driver.  Candidate drivers should implement CustomDriver
 * or SAXDriver, and not BaseDriver.
 *
 * @author AgileDelta
 * @author Sun
 * @author Fujitsu
 *
 */
public abstract class BaseDriver extends JapexDriverBase {
    /**
     * The data sink to write to.
     */
    protected DataSink _dataSink = null;

    /**
     * The data source to read from.
     */
    protected DataSource _dataSource = null;

    /**
     * The driver parameters.
     * <p>
     * This field is valid and constant in the scope of preparation, warmup, run
     * and finish.
     */
    protected DriverParameters _driverParams;

    /**
     * The test case parameters.
     * <p>
     * This field is valid and constant in the scope of preparation, warmup, run
     * and finish.
     */
    public TestCaseParameters _testCaseParams;

    /**
     * Current test case number
     */
    private int _testCaseNum;
    
    
    /**
     * Driver name
     */
    protected String _driverName;
    
    /**
     * Test result 
     */
    protected Result _result;


    @Override
    public void initializeDriver() {
      super.initializeDriver();
      _driverName = _driver.getName();
      // Create the driver parameters
      _driverParams = new DriverParameters(this);
      
      switch (_driverParams.measure) {
        case iot_decode:
        case iot_encode:
        case iot_c14n_encode:
        	// Get the result manager
        	try {
        		_result = new Result(_driverName, Japex.TODAY,
        				_testSuite, _driverParams);
        	} catch (Exception e) {
        		throw new RuntimeException("Failed to initialze the driver: "
        				+ _driverName, e);
        	}
        	break;
      }
      _testCaseNum = -1;
    }

    @Override
    public void terminateDriver() {
      switch (_driverParams.measure) {
        case iot_decode:
        case iot_encode:
        case iot_c14n_encode:
        	try {
        		_result.complete();
        	} catch (Exception e) {
        		throw new RuntimeException("Failed to terminate the driver: "
        				+ _driverName, e);
        	}
        	break;
      }
    }

    @Override
    public void setTestCase(TestCaseImpl testCase) {
      super.setTestCase(testCase);
      ++_testCaseNum;
    }

    /**
     * Prepare the test-case.
     *
     * @param driverParameters
     *            The driver parameters.
     * @param testCaseParams
     *            Test case parameters.
     */
    protected abstract void prepareTestCase(DriverParameters driverParameters,
            TestCaseParameters testCaseParams) throws Exception;

    /**
     * Transcode the given text-xml.
     *
     * @param xmlInput
     *            the input stream of the text-xml
     * @param encodedOutput
     */
    protected abstract void transcodeTestCase(InputStream xmlInput,
            OutputStream encodedOutput) throws Exception;

    /**
     * Validate that driver can (re)parse input
     *
     * @param encodedInput
     *            the input stream of the encoded xml
     * @param originalInput
     *            input stream to the xml to encode
     * @param fatalIfDiff
     *            if set to true, diff is considered fatal when detected
     */
    protected abstract void validateStream(InputStream encodedInput,
            InputStream originalInput, boolean isIot) throws Exception;

    /**
     * Set up the test-case run. Delegates to initialize() for driver specific
     * initialization.
     */
    public void prepare(TestCase testCase) {
        try {
          /*
           * Moved to initializeDriver. (2009-11-30 tkamiya)
           * // Create the driver parameters
           * _driverParams = new DriverParameters(this);
           */
            // Create the test case parameters
            _testCaseParams = new TestCaseParameters(testCase, _driverParams);        	
            
            // Flush Japex messages before preparing test case
            System.err.flush();
            System.out.print(_testCaseParams._frameworkTestParams);
            System.out.print(',');
            System.out.flush();

            // Prepare for the test case
            prepareTestCase(_driverParams, _testCaseParams);

            DataSource validateSrc;
            switch(_driverParams.measure) {
                case compactness:
                    _dataSink = DataSink.createInMemory(_driverParams);
                    transcodeTestCase(_testCaseParams.getXmlInputStream(),
                            _dataSink.getOutputStream());
                    _dataSink.finish();
                    validateSrc = DataSource.createInMemory(_driverParams, _dataSink.toByteArray());
                    validateStream(validateSrc.getInputStream(),
                                   _testCaseParams.getXmlInputStream(), false);
                    break;
                case encode:
                    _dataSink = DataSink.create(_driverParams);
                    break;
                case decode:
                    _dataSink = DataSink.createInMemory(_driverParams);
                    transcodeTestCase(_testCaseParams.getXmlInputStream(),
                            _dataSink.getOutputStream());
                    _dataSink.finish();

                    _dataSource = DataSource.create(_driverParams,
                            _dataSink.toByteArray());
                    break;
                case iot_decode: {
                    final Result.FileManager fm = _result.getFileManager(_testCaseParams);
                    _dataSource = DataSource.createFile(fm.getSourceFileEXI());
                    _dataSink = DataSink.createFile(fm.getSinkFileXML());
                	_result.manifest(testCase.getName(), fm.getSinkPathXML(false));
                   	break;
                }
                case iot_encode: {
                	final Result.FileManager fm = _result.getFileManager(_testCaseParams);
                	_dataSink = DataSink.createFile(fm.getSinkFileEXI());
                	OutputStream ostream = _dataSink.getOutputStream(); 
                	if (_testCaseParams.decodeOnly) {
                 		InputStream encodedStream = _testCaseParams.getEncodedInputStream();
                		byte[] bts = new byte[8192];
                		int n_bytes;
                		while ((n_bytes = encodedStream.read(bts)) >= 0) {
                			ostream.write(bts, 0, n_bytes);
                		}
                		_dataSink.finish();
                		DecodingValidator.validateDecodeOnly();
                	}
                	else {
                		transcodeTestCase(_testCaseParams.getXmlInputStream(), ostream);
                		_dataSink.finish();
                		validateSrc = DataSource.createInMemory(_driverParams, _dataSink.toByteArray());
                		validateStream(validateSrc.getInputStream(),
                				_testCaseParams.getXmlInputStream(), true);
                	}
                	_result.manifest(testCase.getName(), fm.getSinkPathEXI(false));
                  break;
                }
                case iot_c14n_encode: {
                	final Result.FileManager fm = _result.getFileManager(_testCaseParams);
                	_dataSink = DataSink.createFile(fm.getSinkFileEXI());
                    break;
                }
                default:
            }
        } catch (Exception e) {
        	try {
        		if (_dataSink != null) _dataSink.close();
        		if (_dataSource != null) _dataSource.close();
        	} catch (Exception _e) {
        		// ignore
        	}
        	throw getTestException("preparing", testCase, e);
       }
    }

    public void warmup(TestCase testCase) {
        // Is this due to a JIT problem ???
        // When using Japex with iterations instead of time
        // this will result in n^2 iterations for the warmup
        run(testCase);
    }

    public void finish(TestCase testCase) {
        try {
            switch (_driverParams.measure) {
                case compactness:
                    // Check if the size encoded data is 0
                    if (_dataSink.getSize() == 0) {
                        throw new RuntimeException("Encoded data size is 0");
                    }

                    testCase.setIntParam(Constants.RESULT_VALUE,
                            _dataSink.getSize());
                    _dataSink.close();
                    break;
                case decode:
                    _dataSource.close();
                case encode:
                    if (_dataSink.hasSize()) {
                        testCase.setIntParam(Constants.RESULT_VALUE_X,
                                _dataSink.getSize());
                    }
                    _dataSink.close();
                    break;
                case iot_decode:
                  // Check if the size encoded data is 0
                  if (_dataSink.getSize() == 0) {
                      throw new RuntimeException("Encoded data size is 0");
                  }
                  testCase.setIntParam(Constants.RESULT_VALUE,
                      _dataSink.getSize());
                  _dataSource.close();
                  _dataSink.close();
                  break;
                case iot_encode:
                case iot_c14n_encode:
                  // Check if the size encoded data is 0
                  if (_dataSink.getSize() == 0) {
                      throw new RuntimeException("Encoded data size is 0");
                  }
                  testCase.setIntParam(Constants.RESULT_VALUE,
                          _dataSink.getSize());
                  _dataSink.close();
                  break;
                default:
            }
        } catch (Exception e) {
        	throw getTestException("finishing", testCase, e);
        }
    }

    protected RuntimeException getTestException(String phase,
    		TestCase testCase, Exception cause) {
    	final String msg = "Error " + phase + " test: "
    		+ testCase.getName() + _testCaseParams._frameworkTestParams;
    	if (!(cause instanceof DecodingValidator.DiffException)) {
        	System.out.println();
    	}
    	ensureFlush(System.out);
    	System.err.println(msg);
    	cause.printStackTrace(System.err);
    	ensureFlush(System.err);
    	return new RuntimeException(msg, cause);
    }

    private void ensureFlush(PrintStream s) {
    	s.flush();
    	try {
    		Thread.sleep(5);
    	} catch (InterruptedException e) {
    		// ignore
    	}
    }

    /*
     * This method is no longer used by the framework but leave it as is for the compatibility
     */
    public static final String unescapeURI(String uri) throws UnsupportedEncodingException {
      final String fileName;
      int endIndex = uri.lastIndexOf('/');
      if (endIndex >= 0) {
        fileName = uri.substring(endIndex + 1);
        ++endIndex;
      }
      else {
        fileName = uri;
        endIndex = 0;
      }
      
      int current = 0;
      byte[] bts = new byte[fileName.length()];

      int i, len;
      for (i = 0, len = fileName.length(); i < len;) {
        char ch = fileName.charAt(i++);
        if (ch != '%' || i == len || i + 1 == len) {
          bts[current++] = (byte)ch;
        }
        else {
          byte upper = (byte)"0123456789ABCDEF".indexOf(fileName.charAt(i++));
          byte lower = (byte)"0123456789ABCDEF".indexOf(fileName.charAt(i++));
          byte ascii = (byte)((upper << 4) + lower);
          bts[current++] = ascii;
        }
      }
      String unescaped = new String(bts, 0, current, "UTF8");
      return uri.substring(0, endIndex) + unescaped;
    }

}
