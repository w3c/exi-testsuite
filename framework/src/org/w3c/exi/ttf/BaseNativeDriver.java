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


import org.w3c.exi.ttf.datasink.DataSink;
import org.w3c.exi.ttf.parameters.DriverParameters;
import org.w3c.exi.ttf.parameters.TestCaseParameters;

import com.sun.japex.Constants;
import com.sun.japex.TestCase;
import com.sun.japex.jdsl.nativecode.JapexNativeDriver;
import org.w3c.exi.ttf.parameters.DataSourceSinkParam;

/**
 * This is is the base class for native drivers. Note that this class
 * extends JapexNativeDriver so it does not define the same behavior
 * as <code>org.w3c.exi.ttf.BaseDriver</code>. Instead, it follows the
 * contract defined by its superclass, which includes a prepare,
 * warmup, run and finish phases as defined by the Japex framework.
 *
 * In addition to what is already provided by its super class, it 
 * creates a DataSink which can be used by native drivers to compute
 * GZIP sizes. And it also provides convenient access to params
 * defined by the TTFMS framework.
 *
 * This class can instantiated directly or it can be subclassed. Note
 * that it does not override the native methods in its superclass.
 *
 * @author Sun
 * @author OSS Nokalva
 */
public class BaseNativeDriver extends JapexNativeDriver {
    
    /**
     * The data sink to write to.
     */
    protected DataSink _dataSink = null;
    
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
    
    @Override
    public void initializeDriver() {
        super.initializeDriver();
        
        // Create the driver parameters
        _driverParams = new DriverParameters(this);
        
        // Abort if this isn't an in-memory test 
        if (_driverParams.dataSourceSink != DataSourceSinkParam.memory) {
            throw new RuntimeException("Native driver '" + getClass().getName()
                + "' only supports in-memory mode.");
        }
    }
    
    public void prepare(TestCase testCase) {
        try {
            // Create the test case parameters
            _testCaseParams = new TestCaseParameters(testCase, _driverParams);
            
            // Call prepare in native code
            prepare(testCase, _userData);
            
            switch(_driverParams.measure) {
                case compactness:
                    _dataSink = DataSink.createInMemory(_driverParams);
                    break;
                case encode:
                    _dataSink = DataSink.create(_driverParams);
                    break;
                case decode:
                    _dataSink = DataSink.createInMemory(_driverParams);
                    break;
                default:                    
            }
        } catch (Exception e) {
            System.err.println("Error preparing test: " + testCase.getName());
            e.printStackTrace();
            try {
                if (_dataSink != null) _dataSink.close();
            } catch (Exception ex) {}
            throw new RuntimeException(e);
        }
    }
    
    public void warmup(TestCase testCase) {
        // Call run in native code
        run(testCase, _userData);
    }
    
    public void finish(TestCase testCase) {
        try {
            // Call finish in native code
            finish(testCase, _userData);
            
            switch (_driverParams.measure) {
                case compactness:
                    // Note that all drivers set _dataSink.getSize()
                    if (_dataSink.getSize() > 0) {
                        testCase.setIntParam(Constants.RESULT_VALUE,
                                _dataSink.getSize());
                    }
                    _dataSink.close();
                    break;
                case decode:
                case encode:
                    if (_dataSink.hasSize()) {
                        testCase.setIntParam(Constants.RESULT_VALUE_X,
                                _dataSink.getSize());
                    }
                    _dataSink.close();
                    break;
                default:
            }
        } catch (Exception e) {
            System.err.println("Error finishing test: " + testCase.getName());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
}
