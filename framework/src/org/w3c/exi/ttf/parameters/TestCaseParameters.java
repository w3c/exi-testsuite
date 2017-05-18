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
import com.sun.japex.TestCase;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * TTFMS test case parameters.
 *
 * @author AgileDelta
 * @author Sun
 * @author Fujitsu
 */
public final class TestCaseParameters {
    
    /**
     * The input file parameter.
     */
    static public final String INPUT_FILE = com.sun.japex.Constants.INPUT_FILE;
    
    static public final String PARAM_RECORDDECODEDEVENTS 
            = "org.w3c.exi.ttf.recordDecodedEvents";
    
    /**
     * The preserve parameter
     */
    static public final String PRESERVE = "org.w3c.exi.ttf.preserve";
    static public final String IGNORE_FIDELITY_SETTINGS = "org.w3c.exi.ttf.ignoreFidelitySettings";
    
    /**
     * The schema location parameter
     */
    static public final String SCHEMA_LOCATION = "org.w3c.exi.ttf.schemaLocation";
    
    static public final String USE_CASES = "org.w3c.exi.ttf.useCases";
    
    static public final String SCHEMA_DEVIATIONS = "org.w3c.exi.ttf.schemaDeviations";
    static public final String FRAGMENTS = "org.w3c.exi.ttf.fragments";
    static public final String VALUE_PARTITION_CAPACITY = "org.w3c.exi.ttf.valuePartitionCapacity";
    static public final String VALUE_MAX_LENGTH = "org.w3c.exi.ttf.valueMaxLength";

    static public final String INCLUDE_OPTIONS = "org.w3c.exi.ttf.includeOptions";
    static public final String INCLUDE_SCHEMA_ID = "org.w3c.exi.ttf.includeSchemaId";
    static public final String INCLUDE_COOKIE = "org.w3c.exi.ttf.includeCookie";
    
    // convenient aliases for org.w3c.exi.ttf.applicationClass settings in DriverParameters
    static public final String USE_SCHEMAS = "org.w3c.exi.ttf.useSchemas";
    static public final String COMPRESSION = "org.w3c.exi.ttf.compression";
    
    static public final String BYTE_ALIGN = "org.w3c.exi.ttf.byteAlign";
    static public final String PRE_COMPRESSION = "org.w3c.exi.ttf.preCompression";
    
    static public final String SELF_CONTAINED_QNAMES = "org.w3c.exi.ttf.selfContainedQNames";
    static public final String DATATYPE_REPRESENTATION_MAP = "org.w3c.exi.ttf.datatypeRepresentationMap";
    static public final String BLOCK_SIZE = "org.w3c.exi.ttf.blockSize";
    
    static public final String DECODE_ONLY = "org.w3c.exi.ttf.decodeOnly";
    static public final String ENCODED_FILE = "org.w3c.exi.ttf.encodedFile";

    static public final String USE_PROFILE = "org.w3c.exi.ttf.useProfile";
    static public final String INCLUDE_PROFILE_VALUES = "org.w3c.exi.ttf.includeProfileValues";
    static public final String LOCAL_VALUE_PARTITIONS = "org.w3c.exi.ttf.localValuePartitions"; 
    static public final String MAX_BUILTIN_GRAMMARS = "org.w3c.exi.ttf.maxBuiltinGr"; 
    static public final String MAX_BUILTIN_PRODUCTIONS = "org.w3c.exi.ttf.maxBuiltinProd"; 
    
    static public final String UTC_TIME = "org.w3c.exi.ttf.utcTime";
    
    public final Params params;
    
    public Set<PreserveParam> preserves;
    public final boolean ignoreFidelitySettings;
    
    public String schemaLocation;
    
    public final String xmlFile;
    public final String xmlSystemId;
    
    public final String encodedFile;
    
    /**
     * Inidcate whether the read test record parse events or not.
     */
    public boolean traceRead;
    
    /**
     * Are deviations from the schema allowed?
     */
    public boolean schemaDeviations;
    
    /**
     * Are document fragments allowed?
     */
    public boolean fragments;
    
    public int valuePartitionCapacity;
    public int valueMaxLength;

    public boolean includeOptions;
    public boolean includeSchemaId;
    public boolean includeCookie;
    public boolean byteAlign;
    public boolean preCompression;
    public boolean compression;
    public boolean useSchemas;
    public boolean utcTime;
    public QName[] selfContainedQNames;
    public QName[] dtrMapTypes;
    public QName[] dtrMapRepresentations;
    public int blockSize;
    public boolean decodeOnly;
    public boolean useProfile;
    public boolean includeProfileValues;
    public boolean localValuePartitions;
    public int maxBuiltinGr;
    public int maxBuiltinProd;
    
    public final String _frameworkSuffix;
    public final String _frameworkSchemaID;
    public final boolean _frameworkFaithful;
    public final boolean _frameworkFragment;
    public final Set<PreserveParam> _frameworkPreserve;
    public final String _frameworkTarget;
    public final String _frameworkCheck;
    public final String _frameworkDebug;
    public String _frameworkTestParams;

    public TestCaseParameters(Params params, DriverParameters driverParams) {
        this.params = params;
        
        _frameworkTarget = (params instanceof TestCase) ? ((TestCase)params).getName() : "*UNKNOWN*";
        
        _frameworkCheck = DriverParameters._frameworkCheck;
        _frameworkDebug = DriverParameters._frameworkDebug;
        
        _frameworkTestParams = "";
        
        xmlFile = params.getParam(INPUT_FILE);
        if (xmlFile != null) {
        	File inputFile = new File(xmlFile);
            try {
    			xmlSystemId = inputFile.toURI().toURL().toString();
            } catch (MalformedURLException ex) {
    			throw new RuntimeException("Failed to convert the input file to URL: " + xmlFile,
    					ex);
    		}
            if (!inputFile.canRead()) {
            	throw new RuntimeException("Input file is not readable: " + xmlFile);
            }
             	
        } else {
            throw new RuntimeException(INPUT_FILE + " not specified");
        }
       
        encodedFile = params.getParam(ENCODED_FILE);
        
        ignoreFidelitySettings = params.getBooleanParam(IGNORE_FIDELITY_SETTINGS);
        
        // Boolean parameters
        traceRead = params.getBooleanParam(PARAM_RECORDDECODEDEVENTS);
        schemaDeviations = params.getBooleanParam(SCHEMA_DEVIATIONS);
        fragments = params.getBooleanParam(FRAGMENTS);
        includeSchemaId = params.getBooleanParam(INCLUDE_SCHEMA_ID); // implies includeOptions
        includeOptions = params.getBooleanParam(INCLUDE_OPTIONS)     | includeSchemaId;
        includeCookie = params.getBooleanParam(INCLUDE_COOKIE);
        byteAlign = params.getBooleanParam(BYTE_ALIGN);
        preCompression = params.getBooleanParam(PRE_COMPRESSION);
        utcTime = params.getBooleanParam(UTC_TIME);
        if (useProfile = params.getBooleanParam(USE_PROFILE)) {
        	includeProfileValues = getBooleanOption(INCLUDE_PROFILE_VALUES, true);
        	localValuePartitions = getBooleanOption(LOCAL_VALUE_PARTITIONS, true);
        	maxBuiltinGr = getIntegerOption(MAX_BUILTIN_GRAMMARS);
        	maxBuiltinProd = getIntegerOption(MAX_BUILTIN_PRODUCTIONS);
        }
        else {
        	includeProfileValues = true;
            localValuePartitions = true;
            maxBuiltinGr = -1;
            maxBuiltinProd = -1;
        }
        
        final MeasureParam measure = driverParams.measure;
        final boolean isIot;
        switch (measure) {
        	case iot_decode:
        	case iot_encode:
        	case iot_c14n_encode:
        		isIot = true;
        		break;
        	default:
        		isIot = false;
        		break;
        }
      
        String workingSchemaLocation = params.getParam(SCHEMA_LOCATION);
        
        // Optional integer parameters. -1 means use default -- no limit set.
        valuePartitionCapacity = getIntegerOption(VALUE_PARTITION_CAPACITY);
        valueMaxLength = getIntegerOption(VALUE_MAX_LENGTH);        
        blockSize = getIntegerOption(BLOCK_SIZE);

        // collect the list of selfContainedQNames (if any) 
        String qnameList = params.getParam(SELF_CONTAINED_QNAMES);
        if (qnameList == null) 
                selfContainedQNames = new QName[0];
        else {
                String[] formattedQNames = qnameList.split("[\\s]+"); 
                selfContainedQNames = new QName[formattedQNames.length];
                for (int i=0; i< formattedQNames.length; i++) {
                        selfContainedQNames[i] = QName.valueOf(formattedQNames[i]);
                }
        }

        // collect the datatype representation map into two arrays of types and corresponding representations
        qnameList = params.getParam(DATATYPE_REPRESENTATION_MAP);
        if (qnameList == null)
                dtrMapTypes = dtrMapRepresentations = new QName[0];
        else {
                String[] qnamePairs = qnameList.split("[\\s]+");
                int numMappings = qnamePairs.length / 2;
                if (numMappings*2 != qnamePairs.length)
                        throw new RuntimeException(DATATYPE_REPRESENTATION_MAP + " must specify pairs of QName values");
                
                dtrMapTypes = new QName[numMappings];
                dtrMapRepresentations = new QName[numMappings];
                for (int i = 0; i < numMappings; i++) {
                        dtrMapTypes[i] = QName.valueOf(qnamePairs[i*2]);
                        dtrMapRepresentations[i] = QName.valueOf(qnamePairs[i*2+1]);
                }
        }

        if (isIot) {
          // Confine this behavior for now to enable reusing existing data files.
          compression = params.getBooleanParam(COMPRESSION);
          useSchemas = params.getBooleanParam(USE_SCHEMAS);
          if (!useSchemas)
            workingSchemaLocation = null;
        }
        else {
          compression = driverParams.isDocumentAnalysing;
          useSchemas = driverParams.isSchemaOptimizing;
        }
        if (decodeOnly = params.getBooleanParam(DECODE_ONLY)) {
          if (encodedFile == null)
            throw new RuntimeException(ENCODED_FILE + " not specified");
        }
        
        String _preserveOptions = ignoreFidelitySettings ? null : params.getParam(PRESERVE);
        preserves = PreserveParam.createPreserveSet(_preserveOptions, driverParams.measure);

        if (isIot) {
          // The "strict" element MUST NOT appear in an EXI options document when one of 
          // "dtd", "prefixes", "comments", "pis" or "selfContained" element is present. 
          if (preserves.contains(PreserveParam.comments) ||
              preserves.contains(PreserveParam.dtds) ||
              preserves.contains(PreserveParam.prefixes) ||
              preserves.contains(PreserveParam.pis) ||
              selfContainedQNames.length > 0) {
            schemaDeviations = true; // revoke "strict" mode
          }
        }
        
        // Check if schema is available
        if (!useSchemas) {
        	workingSchemaLocation = null;
        } else if (workingSchemaLocation == null) {
        	if (_frameworkCheck.indexOf(",verbose,") >= 0) {
        		System.err.printf("Warning: Possible missing schemaLocation for %s ?\n",
        				_frameworkTarget);
        	}
        	workingSchemaLocation = "";
        } else if (workingSchemaLocation.length() > 0) {
        	boolean available = false;
        	try {
        		available = new File(workingSchemaLocation).canRead();
        	} catch (Exception ex) {
        		String msg = "Schema file is not available: " + workingSchemaLocation;
        		if (isIot) {
        			throw new RuntimeException(msg, ex);
        		} else if (_frameworkCheck.indexOf(",verbose,") >= 0) {
        			System.err.println("Warning: " + msg);
        		}
        		available = false;
        	}
        	if (!available && isIot) {
        		String msg = "Schema file is not readable: " + workingSchemaLocation;
        		throw new RuntimeException(msg);
        	}
        }
         
        /*
         * Keep parameters for the framework before hiding them from the candidate
         */
        _frameworkFaithful = !schemaDeviations;
        _frameworkFragment = fragments;
        _frameworkPreserve = preserves;
        _frameworkSchemaID = workingSchemaLocation;

        if (!isIot) {
          if (workingSchemaLocation != null && workingSchemaLocation.length() == 0) {
              workingSchemaLocation = null;
          }
        }
        // Communicate schema in all cases. In Neither and Document modes
        // the resulting encoding MUST not be schema dependent
       	schemaLocation = workingSchemaLocation;
        
       	// Generate suffix before hiding options
        _frameworkSuffix = resultSuffix();
       	
        // Hide options from the candidate
        if (measure == MeasureParam.iot_decode) {
        	if (includeOptions) {
        		if (useSchemas && includeSchemaId) {
        	        schemaLocation = null;
        	        useSchemas = false;
        		}
        		preserves = PreserveParam.createPreserveSet(null, driverParams.measure);
        		schemaDeviations = false;
        		fragments = false;
        		valuePartitionCapacity = -1;
        		valueMaxLength = -1;
        		byteAlign = false;
        		preCompression = false;
        		compression = false;
        		selfContainedQNames = new QName[0];
        		dtrMapTypes = dtrMapRepresentations = new QName[0];
        		blockSize = -1;
        		if (useProfile && includeProfileValues) {
        			// Let the processor be prepared with least capacity, to be overriden by the values in stream.
                    localValuePartitions = false;
                    maxBuiltinGr = 0;
                    maxBuiltinProd = 0;
        		}
        	}
        	// Followings are instructions to encoders, but not to decoders.
        	includeCookie = false;
        	includeSchemaId = false;
        	includeOptions = false;
    		includeProfileValues = true;
    		utcTime = false;
        }
        
        // Flush Japex messages before running the test
        System.out.flush();
        System.err.flush();
    }
    
    public InputStream getXmlInputStream() throws IOException {
        return new BufferedInputStream(new FileInputStream(xmlFile));
    }
    
    public InputStream getEncodedInputStream() throws IOException {
      if (decodeOnly) {
        return new BufferedInputStream(new FileInputStream(encodedFile));
      }
      return null;
    }
        
    private int getIntegerOption(String option) {
    	String value = params.getParam(option);
    	return (value == null) ? -1 : Integer.parseInt(value);
    }
    
    private boolean getBooleanOption(String option, boolean defaultValue) {
    	return params.hasParam(option) ? params.getBooleanParam(option) : defaultValue;
    }
    
    private String resultSuffix() {
      StringBuilder sb = new StringBuilder(100);
      sb.append('%');
      final int startPos = sb.length();
      for (PreserveParam preserve : this.preserves) {
        /*
         * c - comments,
         * d - dtds,
         * l - lexicalvalues,
         * p - pis,
         * x- prefixes,
         */
        char ch = '\0';
        switch (preserve) {
          case comments:
            ch = 'c';
            break;
          case dtds:
            ch = 'd';
            break;
          case lexicalvalues:
            ch = 'l';
            break;
          case pis:
            ch = 'p';
            break;
          case prefixes:
            ch = 'x';
            break;
          default:
            break;
        }
        if (ch != '\0') {
          int i;
          final int len;
          for (i = startPos, len = sb.length(); i < len; i++) {
            if (sb.charAt(i) < ch)
              continue;
            else
              break;
          }
          sb.insert(i, ch);
        }
      }
      sb.append('%');
      /**
       * v - "strict" schema-informed grammar (whether the stream is schema-informed or schema-less)
       * See http://lists.w3.org/Archives/Member/member-exi-wg/2011Aug/0016.html for more details.
       */
      if (!this.schemaDeviations) {
        sb.append('v');
      }
      /**
       * "i" - schema-informed stream
       */
      if (this.useSchemas) {
        sb.append('i');
      }
      /*
       * f - fragments
       */
      if (this.fragments) {
        sb.append('f');
      }
      /*
       * r - selfContained
       */
      javax.xml.namespace.QName[] qnames;
      qnames = this.selfContainedQNames; 
      if (qnames != null && qnames.length != 0) {
        sb.append('r');
      }
      /*
       * c - compression
       * p - pre-compression
       * a - aligned to bytes
       */
      if (this.compression) {
        sb.append('c');
      }
      else if (this.preCompression) {
        sb.append('p');
      }
      else if (this.byteAlign) {
        sb.append('a');
      }
      /*
       * s - include options plus schemaId
       * o - include options (without schemaId)
       */
      if (this.includeSchemaId) {
        sb.append('s');
      }
      else if (this.includeOptions) {
        sb.append('o');
      }
      /*
       * m - include magic cookie
       */
      if (this.includeCookie) {
        sb.append('m');
      }
      /*
       * u - normalize dateTime to UTC
       */
      if (this.utcTime) {
        sb.append('u');
      }
      sb.append('%');
      if (this.valuePartitionCapacity != -1) {
        sb.append(new Integer(this.valuePartitionCapacity).toString());
      }
      sb.append('%');
      if (this.valueMaxLength != -1) {
        sb.append(new Integer(this.valueMaxLength).toString());
      }
      sb.append('%');
      if (this.blockSize != -1) {
        sb.append(new Integer(this.blockSize).toString());
      }
      sb.append('%');
      if (this.useProfile) {
    	  sb.append(this.includeProfileValues ? '1' : '0');
    	  sb.append(this.localValuePartitions ? '1' : '0');
    	  if (this.maxBuiltinGr != -1) {
        	  sb.append("@g" + this.maxBuiltinGr);
    	  }
    	  if (this.maxBuiltinProd != -1) {
        	  sb.append("@p" + this.maxBuiltinProd);
    	  }
      }
      sb.append('%');
      _frameworkTestParams = sb.toString();
      String schemaLocation = this.schemaLocation;
      if (schemaLocation != null && schemaLocation.length() != 0) {
    	  schemaLocation = schemaLocation.replace('\\', '/');
    	  schemaLocation = schemaLocation.replace('/', '@');
    	  sb.append(schemaLocation);
      }
      for (int dtr = 0; dtr < dtrMapTypes.length; dtr++) {
    	  sb.append('%');
    	  sb.append(dtrMapTypes[dtr].getLocalPart());
    	  sb.append('-');
    	  sb.append(dtrMapRepresentations[dtr].getLocalPart());
      }
    	  
      return sb.toString();
    }
}
