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

package org.w3c.exi.ttf.datasource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.w3c.exi.ttf.parameters.DriverParameters;

/**
 * A data source that abstracts the type of source the data originates from.
 *
 * @author AgileDelta
 * @author Sun
 * @author Fujitsu
 * 
 */
public abstract class DataSource {    
    /**
     * Get an {@link InputStream} of the data source to read from.
     *
     * @return the {@link InputStream} to read from.
     */
    public abstract InputStream getInputStream() throws IOException;
    
    /**
     * Get the size of the data source, in bytes.
     *
     * @return the size of the data source.
     */
    public int getSize() throws IOException {
        InputStream s = getInputStream();
        byte[] b = new byte[4096];
        
        int size = 0;
        int read = 0;
        while ((read = s.read(b)) != -1) {
            size += read;
        }
        
        return size;
    }
    
    /**
     * Finishes writing to the data sink.
     */
    public void finish() throws IOException {
    }
    
    /**
     * Close the data source.
     *
     * <p>
     * Cleans up any resources associated with the source.
     */
    public void close() throws IOException {
    }
    
    /* package */ abstract void initialize(byte[] content) throws IOException;
    
    /**
     * Factory method to create a data source from Japex driver parameters and
     * the byte[] content that initializes the data source.
     */
    public static DataSource create(DriverParameters driverParams, 
            byte[] content) throws Exception {
        DataSource ds;
        
        switch (driverParams.dataSourceSink) {
            case file:
                 ds= new FileDataSource();
                 break;
            case network:
                ds = new NetworkSource(driverParams.dataSourceSinkURI);
                break;
            case memory:
            default:
                ds = new InMemoryDataSource();
        }
        
        ds.initialize(content);
        
        if (driverParams.isDocumentAnalysingUsingGZIP) {
            ds = new CompressedDataSourceAdapter(ds);
        }
        
        return ds;
    }

    /**
     * Factory method to create a file data source representing an existing
     * file at the specified location.
     */
    public static DataSource createFile(String pathName) {
      return new FileDataSource(pathName);
    }

    /**
     * Factory method to create a file data source representing an existing
     * file at the specified location.
     */
    public static DataSource createFile(File file) {
      return new FileDataSource(file);
    }

    /**
     * Factory method to create a in-memory data source from Japex driver
     * parameters.
     */
    public static DataSource createInMemory(DriverParameters driverParams, 
            byte[] content) throws Exception {
        DataSource ds = new InMemoryDataSource();        
        ds.initialize(content);

        if (driverParams.isDocumentAnalysingUsingGZIP) {
            ds = new CompressedDataSourceAdapter(ds);
        }
        
        return ds;
    }
}