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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.net.Socket;
import java.net.URI;
import org.w3c.exi.ttf.NetworkHost;

/**
 * A network source.
 *
 * @author AgileDelta
 * @author Sun
 * 
 */
public class NetworkSource extends DataSource {
    final private NetworkHost _mirrorServer;
    final private Socket _socket;
    private InputStream _inputStream;
    
    /** Creates a new instance of NetworkSource */
    public NetworkSource(URI uri) throws IOException {
        if (uri.getHost().equals("localhost")) {
            _mirrorServer = NetworkHost.createLocalMirrorServer(uri.getPort());
        } else {
            _mirrorServer = null;
        }
        
        // connect to the server
        _socket = new Socket(uri.getHost(), uri.getPort());
    }

    public InputStream getInputStream() throws IOException {
        _inputStream.reset();
        return _inputStream;
    }

    public void close() throws IOException {
        _socket.shutdownInput();
        _socket.close();
        
        if (_mirrorServer != null) {
            _mirrorServer.stop();
        }
    }
    
    void initialize(byte[] content) throws IOException {
        // send content
        OutputStream outputStream = _socket.getOutputStream();
        NetworkHost.writeRelayHeader(outputStream);
        outputStream.write(content);
        _socket.shutdownOutput();
        
	_inputStream = new PartitioningStream(new BufferedInputStream(_socket.getInputStream()),
                content.length);
    }    
    
    /**
     * InputStream directly off the socket will return an infinite stream
     * of bytes.  This wraps that stream and introduces and end-of-stream
     * after reading a specified number of bytes.  The reset() method
     * clears the count of ead bytes and allows parsing of the next
     * collection of bytes on the network stream.
     */
    private static class PartitioningStream extends InputStream
    {
        protected InputStream _rawInput;
        protected int _cbLeft;
        protected int _cbSize;

        public PartitioningStream(InputStream raw, int cb)
        {
            _rawInput = raw;
            _cbSize = _cbLeft = cb;
        }

        public void reset()
        {
            try
            {
                while (0 < _cbLeft)
                {
                    _rawInput.read();
                    _cbLeft--;
                }
            }
            catch (IOException e)
            {
            }
            _cbLeft = _cbSize;
        }

        public int read() throws IOException
        {
            if (_cbLeft <= 0)
                return -1;
            _cbLeft--;
            return _rawInput.read();
        }

        public int read(byte[] buf) throws IOException
        {
            if (_cbLeft == 0)
                return -1;
            int cb = Math.min(buf.length, _cbLeft);
            cb = _rawInput.read(buf, 0, cb);
            _cbLeft -= cb;
            return cb;
        }

        public int read(byte[] buf, int offset, int len) throws IOException
        {
            if (_cbLeft == 0)
                return -1;
            int cb = Math.min(len, _cbLeft);
            cb = _rawInput.read(buf, offset, cb);
            _cbLeft -= cb;
            return cb;
        }
    }
}
