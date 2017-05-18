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

package org.w3c.exi.ttf.fragments;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class FragmentsInputStream extends InputStream
{
    static byte[] pre;
    static byte[] post;
    
    boolean done = false;
    int pos = 0;
    int len = 0;
    final byte[] buffer;
    final InputStream in;
    
    static
    {
        try
        {
            pre = "<root>".getBytes("UTF-8");
            post = "</root>".getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public FragmentsInputStream(InputStream in)
    {
        this.buffer = new byte[1024];
        System.arraycopy(pre, 0, buffer, 0, pre.length);
        len = pre.length;
        this.in = in;
    }
    
    @Override
    public int read() throws IOException
    {
        if (pos == len)
        {
        	// check if finished writing the post tag
        	if (done)
        		return -1;

        	fill();
        }

        return buffer[pos++] & 0xFF;
    }

    @Override
    public int read(byte[] buf, int offset, int bufLen) throws IOException
    {
        if (pos == len)
        {
        	// check if finished writing the post tag
        	if (done)
        		return -1;

        	fill();
        }

        int cb = Math.min(len - pos, bufLen);
        System.arraycopy(buffer, pos, buf, offset, cb);
        pos += cb;
        return cb;
    }

    protected void fill() throws IOException
    {
    	// refill the buffer from the underlying stream
    	pos = 0;
        len = in.read(buffer, 0, buffer.length);

        if (len < 0)
        {
            System.arraycopy(post, 0, buffer, 0, post.length);
            len = post.length;
            done = true;
        }
    }
}
