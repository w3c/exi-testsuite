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

package org.w3c.exi.ttf.datasink;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.w3c.exi.ttf.parameters.DriverParameters;

/**
 * A data sink that abstracts the type of sink the data is sent to.
 *
 * @author AgileDelta
 * @author Sun
 * @author Fujitsu
 * 
 */
public abstract class DataSink {
    /**
     * Get an {@link OutputStream} of the data sink to write to.
     *
     * @return the {@link OutputStream} to write to.
     */
    public abstract OutputStream getOutputStream() throws IOException;
    
    /**
     * Get the size of the data sink, in bytes.
     *
     * @return the size of the data sink.
     */
    public abstract int getSize() throws IOException;
    
    /**
     * Finishes writing to the data sink.
     */
    public void finish() throws IOException {
    }
    
    /**
     * Convert the data in the sink to a byte array.
     *
     * @return the byte array.
     */
    public abstract byte[] toByteArray() throws IOException;
    
    /**
     * Close the data sink.
     *
     * <p>
     * Cleans up any resources associated with the sink.
     */
    public void close() throws IOException {
    }
    
    /**
     * Indicates if this data sink can return the number of bytes 
     * written to the underlying stream.
     */ 
    public boolean hasSize() {
        return true;
    }
    
    /**
     * Factory method to create a file data sink representing a file
     * at the specified location.
     */
    public static DataSink createFile(String pathName) {
      return new FileDataSink(pathName);
    }
    
    /**
     * Factory method to create a file data sink representing a file
     * at the specified location.
     */
    public static DataSink createFile(File file) {
      return new FileDataSink(file);
    }
    
    /**
     * Factory method to create a in-memory data sink from Japex driver
     * parameters.
     */
    public static DataSink createInMemory(DriverParameters driverParams)
    throws Exception {
        if (driverParams.isDocumentAnalysingUsingGZIP) {
            return new CompressedDataSinkAdapter(new InMemoryDataSink());
        } else {
            return new InMemoryDataSink();
        }
    }
    
    /**
     * Factory method to create a data sink from Japex driver parameters.
     */
    public static DataSink create(DriverParameters driverParams)
    throws Exception {
        DataSink ds;
        
        switch (driverParams.dataSourceSink) {
            case file:
                ds = new FileDataSink();
                break;
            case network:
                ds = new NetworkSink(driverParams.dataSourceSinkURI);
                break;
            case memory:
            default:
                ds = new InMemoryDataSink();
        }
        
        if (driverParams.isDocumentAnalysingUsingGZIP) {
            ds = new CompressedDataSinkAdapter(ds);
        }
        
        return ds;
    }
}