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

import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * In memory data source using using ByteArrayInputStream.
 *
 * @author AgileDelta
 * @author Sun
 * 
 */
class InMemoryDataSource extends DataSource {
    protected ByteArrayInputStream _in;
    protected int _size;
    
    protected InMemoryDataSource() {
    }
    
    public InputStream getInputStream() throws IOException {
        _in.reset();
        return new BufferedInputStream(_in);
    }
    
    public int getSize() throws IOException {
        return _size;
    }
    
    void initialize(byte[] content) throws IOException {
        _size = content.length;
        _in = new ByteArrayInputStream(content);
    }
}
