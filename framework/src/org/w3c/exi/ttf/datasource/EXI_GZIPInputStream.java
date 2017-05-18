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

import java.io.InputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * <p>This class extends <code>GZIPInputStream</code> in order to 
 * get access to the inflater <code>inf</code>.
 * 
 * <p>The <code>end()</code> method is also called by the GC via
 * <code>finalize()</code>, but this appears to be too late for our
 * purposes given that the benchmark runs out of memory fairly 
 * quickly. Note that calling <code>close()</code> is not an option
 * because it closes the underlying input stream which in the 
 * network case it is a socket that outlives instances of this 
 * class.</p>
 *
 * @author AgileDelta
 * @author Sun
 *
 */
class EXI_GZIPInputStream extends GZIPInputStream {
    
    public EXI_GZIPInputStream(InputStream in) throws IOException {
        super(in);
    }
    
    /**
     * Finishes reading the compressed data to from the input stream 
     * without closing the underlying stream.
     */
    public void finish() throws IOException {
        inf.end();      // free native memory
    }
}