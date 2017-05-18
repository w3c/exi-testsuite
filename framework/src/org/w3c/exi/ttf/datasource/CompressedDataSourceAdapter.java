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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Adapter for a compressed data source using GZIP.
 *
 * @author AgileDelta
 * @author Sun
 * 
 */
class CompressedDataSourceAdapter extends DataSource {
    private EXI_GZIPInputStream _gzIn;
    
    /**
     * The adapted DataSource.
     */
    private DataSource _ds;
    
    public CompressedDataSourceAdapter(DataSource ds) throws IOException {
        _ds = ds;
    }
    
    public InputStream getInputStream() throws IOException {
        return _gzIn = new EXI_GZIPInputStream(_ds.getInputStream());
    }
    
    public int getSize() throws IOException {
        return _ds.getSize();
    }
    
    void initialize(byte[] content) throws IOException {
      /**
       * Since the content is supposed to be already gzipped,
       * gzipping it again results in doubly gzipped stream.
       * (tkamiya@us.fujitsu.com) 
       * 
       * ByteArrayOutputStream gzipBaos = new ByteArrayOutputStream();
       * try {
       *   GZIPOutputStream gzipOs = new GZIPOutputStream(gzipBaos);
       *   gzipOs.write(content);
       *   gzipOs.finish();
       * } catch (Exception e) {
       *   throw new RuntimeException(e);
       * }
       * _ds.initialize(gzipBaos.toByteArray());
       */
       _ds.initialize(content);
    }

    public void finish() throws IOException {
        _gzIn.finish();
        _ds.finish();
    }
    
    public void close() throws IOException {
        _ds.close();
    }
}