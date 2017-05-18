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

import java.io.OutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * <p>This class extends <code>GZIPOutputStream</code> in order to 
 * get access to the deflater <code>def</code>. It then overrides 
 * the method <code>finish()</code> to release any resources held 
 * by the deflater. </p>
 * 
 * <p>The <code>end()</code> method is also called by the GC via
 * <code>finalize()</code>, but this appears to be too late for our
 * purposes given that the benchmark runs out of memory fairly 
 * quickly. Note that calling <code>close()</code> is not an option
 * because it closes the underlying output stream which in the 
 * network case it is a socket that outlives instances of this 
 * class.</p>
 *
 * @author AgileDelta
 * @author Sun
 *
 */
class EXI_GZIPOutputStream extends GZIPOutputStream {
    
    private boolean closed = false;
    
    public EXI_GZIPOutputStream(OutputStream out) throws IOException {
        super(out);
    }
    
    @Override
    public void finish() throws IOException {
        super.finish();
        def.end();      // free native memory
    }
}