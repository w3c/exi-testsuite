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

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Adapter for a compressed data sink using GZIP.
 *
 * @author AgileDelta
 * @author Sun
 * 
 */
public class CompressedDataSinkAdapter extends DataSink {
    private GZIPOutputStream _gzOut;
    private DataSink _ds;
    
    public CompressedDataSinkAdapter(DataSink ds) {
        _ds = ds;
    }
    
    public OutputStream getOutputStream() throws IOException {
        return _gzOut = new EXI_GZIPOutputStream(_ds.getOutputStream());
    }

    public boolean hasSize() {
        return _ds.hasSize();
    }
    
    public int getSize() throws IOException {
        return _ds.getSize();
    }
    
    public void finish() throws IOException {
        _gzOut.finish();
        _ds.finish();
    }
    
    public void close() throws IOException {
        _ds.close();
    }
    
    public byte[] toByteArray() throws IOException {
        return _ds.toByteArray();
    }
    
}
