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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * File data source.
 * <p>
 * A temporary file is created to act as the source.
 *
 * @author AgileDelta
 * @author Sun
 * @author Fujitsu
 * 
 */
class FileDataSource extends DataSource {
    protected int _size;
    protected File _file;
    private final boolean _isTemp;
    
    protected  FileDataSource() {
      _isTemp = true;
    }

    protected FileDataSource(String pathName) {
    	this(new File(pathName));
    }
    
    protected FileDataSource(File file) {
      try {
        _file = file;
        _size = (int)file.length();
      } catch (Exception e) {
          throw new RuntimeException("Unexpected exception", e);
      }
      _isTemp = false;
    }

    public InputStream getInputStream() throws IOException {
        return new BufferedInputStream(new FileInputStream(_file));
    }
    
    public int getSize() throws IOException {
        return _size;
    }
    
    void initialize(byte[] content) throws IOException {
        if (_file == null) {
            _size = content.length;
            
            _file = File.createTempFile("EXI-TTF-", null);
            OutputStream out = new FileOutputStream(_file);
            out.write(content);
            out.close();
        }
    }
    
    public void close() throws IOException {
        if (_isTemp)
            _file.delete();
    }
}