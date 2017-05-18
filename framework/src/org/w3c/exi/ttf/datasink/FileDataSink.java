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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * File data sink.
 * <p>
 * A temporary file is created to act as the sink.
 *
 * @author AgileDelta
 * @author Sun
 * @author Fujitsu
 * 
 */
class FileDataSink extends DataSink {
    protected File _file;
    protected OutputStream _out;

    protected FileDataSink() {
        try {
            _file = File.createTempFile("EXI-TTF-", null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected FileDataSink(String pathName) {
    	this(new File(pathName));
    }
    
    protected FileDataSink(File file) {
      _file = file; 
    }

    @Override
    public void finish() throws IOException {
      super.finish();
      if (_out != null)
        _out.close();
    }
    
    public OutputStream getOutputStream() throws IOException {
        _out = new BufferedOutputStream(new FileOutputStream(_file));
        return _out;
    }
    
    public byte[] toByteArray() throws IOException {
        byte[] b = new byte[(int)_file.length()];
        FileInputStream in = new FileInputStream(_file);
                
        int offset = 0;
        int read = 0;
        int toRead = b.length;
        while(toRead > 0 && (read = in.read(b, offset, toRead)) != -1) {
            offset += read;
            toRead -= read;
        }
        
        return b;
    }

    public int getSize() throws IOException {
        return (int)_file.length();
    }
    
}
