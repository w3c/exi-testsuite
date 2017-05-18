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
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.net.URI;
import org.w3c.exi.ttf.NetworkHost;

/**
 * A network sink.
 *
 * @author AgileDelta
 * @author Sun
 * 
 */
public class NetworkSink extends DataSink {
    final private NetworkHost _mirrorServer;
    final private Socket _socket;
    final private OutputStream _outputStream;
    
    public NetworkSink(URI uri) throws IOException {
        if (uri.getHost().equals("localhost")) {
            _mirrorServer = NetworkHost.createLocalMirrorServer(uri.getPort());
        } else {
            _mirrorServer = null;
        }
        
        // connect to the server
        _socket = new Socket(uri.getHost(), uri.getPort());
        
        _outputStream = new BufferedOutputStream(_socket.getOutputStream());
        NetworkHost.writeConsumeHeader(_outputStream);
    }

    public OutputStream getOutputStream() throws IOException {
        return _outputStream;
    }

    public boolean hasSize() {
        return false;
    }
    
    public int getSize() throws IOException {
        return -1;
    }

    public byte[] toByteArray() throws IOException {
        return null;
    }
    
    public void close() throws IOException {
        _socket.shutdownOutput();
        _socket.close();
        
        if (_mirrorServer != null) {
            _mirrorServer.stop();
        }
    }
}
