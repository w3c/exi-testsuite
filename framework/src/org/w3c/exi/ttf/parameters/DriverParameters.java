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

package org.w3c.exi.ttf.parameters;

import com.sun.japex.Params;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * TTFMS driver parameters.
 *
 * @author AgileDelta
 * @author Sun
 * @author Fujitsu
 * 
 */
public final class DriverParameters {
    /**
     * The application class parameter.
     */
    static public final String APPLICATION_CLASS
    = "org.w3c.exi.ttf.applicationClass";

    static public final String APPLICATION_CLASS_DOCUMENT_ANALYSING_GZIP = 
            "org.w3c.exi.ttf.applicationClass.documentAnalysing.GZIP";
    
    static public final String MEASURE = "org.w3c.exi.ttf.measurementProperty";
    
    static public final String DRIVER_IS_XML_PROCESSOR =
          "org.w3c.exi.ttf.driver.isXmlProcessor";

    static public final String IGNORE_STRING_TABLE_BOUNDS
    = "org.w3c.exi.ttf.ignoreStringTableBounds";

    /**
     * The string interning parameter.
     * <p>
     * TODO: This parameter may only be relevant to Java-based decoders that 
     * perform string interning (see the SAX string interning property).
     * Some XML parsers like Xerces always perform string interning. Some
     * applications, like XML binders, rely on string interning by parsers
     * for efficient string comparison.
     */
    static public final String STRING_INTERNING 
            = "org.w3c.exi.ttf.stringInterning"; 
    
    /**
     * The URI of the data source/sink.
     */
    public static final String DATA_SOURCE_SINK_URI 
            = "org.w3c.exi.ttf.dataSourceSink.URI";

    /**
     * Specified path to the directory that contains encoded EXI files
     * used by decode in IOT. 
     */
    public static final String EXI_DATA_DIR 
            = "exiDataDir";

    public final Params params;

    /**
     * The application class.
     */
    public ApplicationClassParam applicationClass;

    /**
     * The measure property.
     */
    public final MeasureParam measure;

    /**
     * True if {@link #applicationClass} is document or both.
     */
    public boolean isDocumentAnalysing;
    
    /**
     * True if {@link #applicationClass} is document or both
     * and GZIP is used.
     */
    public final boolean isDocumentAnalysingUsingGZIP;
    
    /**
     * True if {@link #applicationClass} is schema or both.
     */
    public boolean isSchemaOptimizing;

    /**
     * True if the driver is a vanilla XML Processor (aka. XML Parser)
     */
    public final boolean isXmlProcessor;
    
    public final boolean ignoreStringTableBounds;
    
    /**
     * True if when decoding string interning should be performed.
     * <p>
     * String interning MUST conform to the SAX inter
     */
    public final boolean isStringInterning;

    /**
     * The URI representing the data source/sink.
     */
    public final URI dataSourceSinkURI;
    
    /**
     * The scheme of the data source/sink URI.
     */
    public final DataSourceSinkParam dataSourceSink;

    /**
     * The URI for EXI Data directory
     * 
     * This is no longer used by the framework but keep it for candidates
     */
    public final URI exiDataBaseURI;
    
    /**
     * The EXI data directory specified by the user
     * 
     * The framework now uses this instead of URI
     */
    public final File exiDataBaseDir;

    /**
     * Framework options
     */
    public static final String _frameworkCheck = getFrameworkOption("check");
    public static final String _frameworkDebug = getFrameworkOption("debug");
    
    public DriverParameters(Params params) {
        this.params = params;
               
        applicationClass = ApplicationClassParam.createApplicationClass(
                params.getParam(APPLICATION_CLASS));
        measure = MeasureParam.createMeasure(params.getParam(MEASURE));
        isDocumentAnalysing = (applicationClass == ApplicationClassParam.document || 
                applicationClass == ApplicationClassParam.both)
                ? true : false;
        isDocumentAnalysingUsingGZIP = (isDocumentAnalysing)
            ? params.getBooleanParam(
                    APPLICATION_CLASS_DOCUMENT_ANALYSING_GZIP)
            : false;
        isSchemaOptimizing = (applicationClass == ApplicationClassParam.schema || 
                applicationClass == ApplicationClassParam.both)
                ? true : false;
        
        isXmlProcessor = params.getBooleanParam(DRIVER_IS_XML_PROCESSOR);
        
        ignoreStringTableBounds = params.getBooleanParam(IGNORE_STRING_TABLE_BOUNDS);
        
        isStringInterning = params.getBooleanParam(STRING_INTERNING);

        try {
        	String uri = params.getParam(DATA_SOURCE_SINK_URI);
        	if (uri == null) uri = "memory:/";

        	dataSourceSinkURI = new URI(uri);
        	dataSourceSink = DataSourceSinkParam.createDataSourceSinkParam(
        			dataSourceSinkURI);
        } catch (URISyntaxException e) {
        	throw new IllegalArgumentException("Bad URI for DataSourceSink", e);
        }
        
        final boolean verbose = (_frameworkCheck.indexOf(",verbose,") >= 0);
        final boolean iot_decode = (measure == MeasureParam.iot_decode);

        String exiDataDir = params.getParam(EXI_DATA_DIR);
        String dir = exiDataDir;
        if (dir == null || dir.length() == 0) {
        	if (iot_decode && verbose) {
        		if (dir == null) {
        			System.err.println("WARNING: '" + EXI_DATA_DIR + "' is not specified for iot_decode");
        		}
        		System.err.println("WARNING: Assuming the current directory as the EXI data directory");
        	}
        	dir = ".";
        	exiDataDir = "";
        }
        exiDataBaseDir = new File(dir);
        exiDataBaseURI = exiDataBaseDir.toURI();
   } 
    
    /**
     * Resolve a string representing an uri into an absolute URI given a base URI.
     * Null is returned if the uri is null or the uri seems to be a relative one
     * with baseURI being null.
     * 
     * This method is no longer used but leave it as it was because it is public method
     */
    public static URI resolveURI(String uri, URI baseURI)
        throws URISyntaxException {
      URI resolved = null;
      if (uri != null) {
        int pos;
        if ((pos = uri.indexOf(':')) <= 1) {
          if (pos == 1) {
            char firstChar = uri.charAt(0);
            if ('A' <= firstChar && firstChar <= 'Z' ||
                'a' <= firstChar && firstChar <= 'z') {
              resolved = new File(uri).toURI();
            }
          }
          else { // relative URI
            if (baseURI != null)
              resolved = baseURI.resolve(uri);
            else
              return null;
          }
        }
        if (resolved == null)
          resolved = new URI(uri); // cross your fingers
      }
      return resolved;
    }
    
    private static String getFrameworkOption(String name) {
    	String value = System.getProperty("org.w3c.exi.ttf.framework." + name);
    	return (value == null) ? "" : ("," + value + ",");
    }
}
